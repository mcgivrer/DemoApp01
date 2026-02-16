package core.behavior;

import core.entity.GameObject;
import core.entity.ShapeType;
import core.physics.CollisionEvent;

/**
 * Default collision response that applies positional correction, impulse-based
 * velocity adjustment (linear <em>and angular</em>), and Coulomb friction to
 * the <em>self</em> entity only.
 * <p>
 * <strong>Mass &amp; inertia-aware distribution:</strong> when both entities are
 * DYNAMIC, positional correction and impulse are distributed according to the
 * inverse mass ratio. The rotational contribution uses the moment of inertia
 * (rectangle: $I = m(w^2+h^2)/12$, circle: $I = mr^2/2$). The full impulse
 * denominator is:
 * <pre>
 *   1/m_self + 1/m_other + (r_self &times; n)&sup2; / I_self + (r_other &times; n)&sup2; / I_other
 * </pre>
 * <p>
 * <strong>Safety guards:</strong>
 * <ul>
 *   <li>Linear velocity clamped to &plusmn;{@value #MAX_LINEAR_VELOCITY}</li>
 *   <li>Angular velocity clamped to &plusmn;{@value #MAX_ANGULAR_VELOCITY}&deg;/s</li>
 *   <li>Near-zero linear velocity (&lt; {@value #MIN_LINEAR_VELOCITY}) zeroed</li>
 *   <li>Near-zero angular velocity (&lt; {@value #MIN_ANGULAR_VELOCITY}&deg;/s) zeroed</li>
 * </ul>
 *
 * @see CollisionBehavior
 * @see CollisionEvent
 * @see core.physics.CollisionManager
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.3
 */
public class DefaultCollisionResponseBehavior implements CollisionBehavior {

    /** Maximum linear velocity magnitude (px/s). */
    private static final float MAX_LINEAR_VELOCITY = 5000.0f;
    /** Maximum angular velocity magnitude (degrees/s). */
    private static final float MAX_ANGULAR_VELOCITY = 720.0f;
    /** Linear velocity below this threshold is zeroed to avoid jitter. */
    private static final float MIN_LINEAR_VELOCITY = 0.05f;
    /** Angular velocity below this threshold is zeroed to avoid jitter (degrees/s). */
    private static final float MIN_ANGULAR_VELOCITY = 0.1f;

    /**
     * Minimum relative velocity magnitude (px/s) required to rotate a
     * non-CIRCLE shape (RECTANGLE, LINE, etc.). Below this threshold
     * the angular impulse is suppressed entirely for those shapes.
     */
    private static final float ROTATION_VELOCITY_THRESHOLD = 3000.0f;

    @Override
    public void onCollision(GameObject self, CollisionEvent event, float deltaTime) {
        GameObject other = event.other();
        float nx = event.nx();
        float ny = event.ny();
        float penetration = event.penetration();
        boolean selfDynamic = event.selfDynamic();
        boolean otherDynamic = event.otherDynamic();
        boolean otherKinematic = event.otherKinematic();

        if (!selfDynamic) {
            return;
        }

        // --- KINEMATIC platform support ---
        // When a DYNAMIC object is on top of a KINEMATIC object (collision from below,
        // normal pointing upward: ny < 0), transfer the KINEMATIC's movement to the DYNAMIC.
        // Only transfer position, NOT velocity - the object keeps its own velocity.
        if (otherKinematic && ny < -0.5f) {
            // Apply platform displacement directly (position delta based on platform velocity)
            // Use the same time factor as VelocityBehavior (0.1f)
            float timeFactor = 0.1f;
            self.setPosition(
                    self.x + other.vx * deltaTime * timeFactor,
                    self.y + other.vy * deltaTime * timeFactor);
        }
        
        // For KINEMATIC objects, treat their velocity as zero for collision impulse calculations
        // This prevents the platform velocity from affecting the bounce/friction response
        float otherVxForImpulse = otherKinematic ? 0 : other.vx;
        float otherVyForImpulse = otherKinematic ? 0 : other.vy;

        // --- Inverse masses ---
        float selfMass = self.getMass();
        float otherMass = other.getMass();
        float invMassSelf = (selfMass > 0) ? 1.0f / selfMass : 0.0f;
        float invMassOther = (otherDynamic && otherMass > 0) ? 1.0f / otherMass : 0.0f;
        float invMassSum = invMassSelf + invMassOther;

        if (invMassSum <= 0) {
            return;
        }

        // --- Moments of inertia ---
        float inertiaSelf = computeInertia(self, selfMass);
        float inertiaOther = computeInertia(other, otherMass);

        // Relative linear speed (pre-check for rotation eligibility)
        // Use adjusted velocity for KINEMATIC
        float relSpeedSq = (self.vx - otherVxForImpulse) * (self.vx - otherVxForImpulse)
                + (self.vy - otherVyForImpulse) * (self.vy - otherVyForImpulse);

        // Only CIRCLE shapes rotate freely; RECTANGLE/LINE/others require
        // an extreme collision speed to receive any angular impulse.
        float invInertiaSelf = computeEffectiveInvInertia(
                self.getShapeType(), selfDynamic, inertiaSelf, relSpeedSq);
        float invInertiaOther = computeEffectiveInvInertia(
                other.getShapeType(), otherDynamic, inertiaOther, relSpeedSq);

        // --- Contact point lever arms ---
        // The lever arm goes from the gravity center to the contact edge.
        // Contact edge approximation: gravity center offset + half-size toward
        // the collision normal direction.
        // For self: contact is in the −n direction from its gravity center
        float selfGcX = self.getGravityCenterX();  // relative to top-left
        float selfGcY = self.getGravityCenterY();
        float otherGcX = other.getGravityCenterX();
        float otherGcY = other.getGravityCenterY();

        // Vector from gravity center to contact point (edge facing other)
        float rSelfX = -nx * (self.getWidth() / 2.0f) + (self.getWidth() / 2.0f - selfGcX);
        float rSelfY = -ny * (self.getHeight() / 2.0f) + (self.getHeight() / 2.0f - selfGcY);
        float rOtherX = nx * (other.getWidth() / 2.0f) + (other.getWidth() / 2.0f - otherGcX);
        float rOtherY = ny * (other.getHeight() / 2.0f) + (other.getHeight() / 2.0f - otherGcY);

        // --- Angular velocities (convert degrees/s → radians/s) ---
        float omegaSelf = (float) Math.toRadians(self.getVa());
        // For KINEMATIC, treat angular velocity as 0 for impulse calculations
        float omegaOther = otherKinematic ? 0 : (float) Math.toRadians(other.getVa());

        // --- Velocity at contact point = v_linear + ω × r ---
        // In 2D: ω × r = (−ω·ry , ω·rx)
        float vSelfX = self.vx - omegaSelf * rSelfY;
        float vSelfY = self.vy + omegaSelf * rSelfX;
        // Use adjusted velocity for KINEMATIC objects (treat as 0)
        float vOtherX = otherVxForImpulse - omegaOther * rOtherY;
        float vOtherY = otherVyForImpulse + omegaOther * rOtherX;

        // --- Positional correction (mass-weighted, before velocity changes) ---
        float correctionRatio = invMassSelf / invMassSum;
        self.setPosition(
                self.x + nx * penetration * correctionRatio,
                self.y + ny * penetration * correctionRatio);

        // --- Material properties ---
        float restitution = Math.min(
                self.getMaterial().restitution(),
                other.getMaterial().restitution());
        float friction = (self.getMaterial().friction() + other.getMaterial().friction()) / 2.0f;

        // --- Relative velocity at contact point along the normal ---
        float relVx = vSelfX - vOtherX;
        float relVy = vSelfY - vOtherY;
        float relVn = relVx * nx + relVy * ny;

        // Only resolve if approaching (relVn < 0)
        if (relVn >= 0) {
            clampVelocities(self);
            return;
        }

        // --- Cross products  r × n  (scalar in 2D) ---
        float rCrossNSelf = rSelfX * ny - rSelfY * nx;
        float rCrossNOther = rOtherX * ny - rOtherY * nx;

        // --- Normal impulse denominator (includes rotational inertia) ---
        float denomN = invMassSelf + invMassOther
                + rCrossNSelf * rCrossNSelf * invInertiaSelf
                + rCrossNOther * rCrossNOther * invInertiaOther;

        float jn = -(1 + restitution) * relVn / denomN;

        // Apply normal impulse — linear
        self.vx += jn * invMassSelf * nx;
        self.vy += jn * invMassSelf * ny;

        // Apply normal impulse — angular:  Δω = (r × j·n) / I
        float deltaOmegaN = jn * rCrossNSelf * invInertiaSelf;
        self.setVa(self.getVa() + (float) Math.toDegrees(deltaOmegaN));

        // --- Tangential (friction) impulse ---
        float tx = -ny;
        float ty = nx;
        float relVt = relVx * tx + relVy * ty;

        float rCrossTSelf = rSelfX * ty - rSelfY * tx;
        float rCrossTOther = rOtherX * ty - rOtherY * tx;

        float denomT = invMassSelf + invMassOther
                + rCrossTSelf * rCrossTSelf * invInertiaSelf
                + rCrossTOther * rCrossTOther * invInertiaOther;

        float jt = -relVt / denomT;

        // Coulomb friction clamp: |jt| ≤ μ · |jn|
        float maxFriction = friction * Math.abs(jn);
        jt = Math.max(-maxFriction, Math.min(maxFriction, jt));

        // Apply friction impulse — linear
        self.vx += jt * invMassSelf * tx;
        self.vy += jt * invMassSelf * ty;

        // Apply friction impulse — angular
        float deltaOmegaT = jt * rCrossTSelf * invInertiaSelf;
        self.setVa(self.getVa() + (float) Math.toDegrees(deltaOmegaT));

        // --- Corner stabilization for RECTANGLE shapes ---
        // When a rectangle touches a surface at a corner, rotate it so that
        // one of its faces becomes parallel to the contact surface.
        applyCornerStabilization(self, nx, ny);

        // --- Clamp to safe bounds ---
        clampVelocities(self);
    }

    /**
     * Stabilization threshold in degrees: when the rectangle's angle is within
     * this distance from a stable orientation, snap to that orientation.
     */
    private static final float CORNER_STABILIZATION_THRESHOLD = 3.0f;

    /**
     * Angular velocity applied to rotate the rectangle towards a stable face
     * when a corner touches a surface (degrees/s).
     */
    private static final float CORNER_STABILIZATION_TORQUE = 360.0f;

    /**
     * When a RECTANGLE shape touches a surface at a corner (i.e., its angle is not
     * aligned with the surface normal), this method applies a corrective angular
     * velocity to rotate it so that one of its faces becomes parallel to the surface.
     * <p>
     * The target angle is computed based on the collision normal direction. For example:
     * <ul>
     *   <li>Normal (0, -1) = horizontal floor → target angles: 0°, 90°, 180°, 270°</li>
     *   <li>Normal (-1, 0) = vertical right wall → target angles: 0°, 90°, 180°, 270°</li>
     *   <li>Normal at 45° slope → target angles offset by 45°</li>
     * </ul>
     * <p>
     * The rotation direction is determined by the gravity center position: the object
     * will naturally tip in the direction that brings the gravity center lower.
     *
     * @param self The rectangle game object.
     * @param nx   The collision normal X component.
     * @param ny   The collision normal Y component.
     */
    private void applyCornerStabilization(GameObject self, float nx, float ny) {
        // Only apply to RECTANGLE shapes
        if (self.getShapeType() != ShapeType.RECTANGLE) {
            return;
        }

        // Compute the surface angle from the collision normal
        // The normal points perpendicular to the surface, so the surface angle is normal angle + 90°
        // Normal angle = atan2(ny, nx), but we want the surface to be parallel
        float surfaceAngle = (float) Math.toDegrees(Math.atan2(ny, nx));
        // Convert to [0, 360)
        surfaceAngle = surfaceAngle % 360f;
        if (surfaceAngle < 0) surfaceAngle += 360f;

        // Normalize current angle to [0, 360)
        float angle = self.angle % 360f;
        if (angle < 0) angle += 360f;

        // For a rectangle to have a face parallel to the surface, its angle should be:
        // surfaceAngle + 90° (perpendicular to normal) + k*90° for each face
        // So stable angles are: (surfaceAngle + 90°), (surfaceAngle + 180°), (surfaceAngle + 270°), (surfaceAngle)
        // Simplified: target = surfaceAngle + 90° + k*90° = surfaceAngle + (1+k)*90°
        
        // Find the nearest stable angle (0°, 90°, 180°, 270° relative to surface)
        // The rectangle face should be perpendicular to the normal (parallel to surface)
        float baseStableAngle = surfaceAngle + 90f;
        baseStableAngle = baseStableAngle % 360f;
        if (baseStableAngle < 0) baseStableAngle += 360f;

        // Find closest stable angle among baseStableAngle + k*90° for k in {0,1,2,3}
        float bestTarget = baseStableAngle;
        float bestDistance = Float.MAX_VALUE;
        
        for (int k = 0; k < 4; k++) {
            float candidate = (baseStableAngle + k * 90f) % 360f;
            
            // Compute angular distance (shortest path)
            float diff = candidate - angle;
            // Normalize to [-180, 180]
            while (diff > 180f) diff -= 360f;
            while (diff < -180f) diff += 360f;
            
            float distance = Math.abs(diff);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestTarget = candidate;
            }
        }

        // If already near a stable orientation, snap and stop
        if (bestDistance < CORNER_STABILIZATION_THRESHOLD) {
            self.angle = bestTarget;
            // Damp angular velocity significantly
            self.setVa(self.getVa() * 0.1f);
            return;
        }

        // --- Determine rotation direction ---
        // Compute direction to target (shortest path)
        float diff = bestTarget - angle;
        while (diff > 180f) diff -= 360f;
        while (diff < -180f) diff += 360f;
        
        float targetDirection = Math.signum(diff);

        // Also consider gravity center influence for more natural tipping
        float gcX = self.getGravityCenterX();
        float gcY = self.getGravityCenterY();
        float halfW = self.getWidth() / 2.0f;
        float halfH = self.getHeight() / 2.0f;
        float gcOffsetX = gcX - halfW;
        float gcOffsetY = gcY - halfH;

        // Transform to world space
        float angleRad = (float) Math.toRadians(angle);
        float cosA = (float) Math.cos(angleRad);
        float sinA = (float) Math.sin(angleRad);
        float gcWorldX = gcOffsetX * cosA - gcOffsetY * sinA;

        // If gravity center has significant offset, let it influence direction
        float gcInfluenceThreshold = 0.5f;
        if (Math.abs(gcWorldX) > gcInfluenceThreshold) {
            // Gravity center pulls in this direction
            float gcDirection = -Math.signum(gcWorldX);
            // Blend with geometric shortest path (gravity center has 40% weight)
            targetDirection = targetDirection * 0.6f + gcDirection * 0.4f;
            targetDirection = Math.signum(targetDirection);
        }

        // Apply corrective torque proportional to distance from target
        // Stronger correction when farther from target
        float correctionStrength = Math.min(1.0f, bestDistance / 45f);
        float correction = targetDirection * CORNER_STABILIZATION_TORQUE * correctionStrength;
        
        // Blend with existing angular velocity
        self.setVa(self.getVa() * 0.7f + correction * 0.3f);
    }

    /**
     * Computes the moment of inertia for a {@link GameObject} about its
     * gravity center, using the parallel-axis (Huygens–Steiner) theorem
     * when the gravity center differs from the geometric center.
     *
     * @param go   The game object.
     * @param mass The mass of the object.
     * @return The moment of inertia (kg·px²), or 0 if mass ≤ 0.
     */
    private float computeInertia(GameObject go, float mass) {
        if (mass <= 0) return 0;
        float w = go.getWidth();
        float h = go.getHeight();

        // Inertia about geometric center
        float iCenter;
        if (go.getShapeType() == ShapeType.CIRCLE) {
            float r = w / 2.0f;
            iCenter = mass * r * r / 2.0f;
        } else {
            iCenter = mass * (w * w + h * h) / 12.0f;
        }

        // Parallel-axis theorem: I_gc = I_center + m·d²
        float dxGc = go.getGravityCenterX() - w / 2.0f;
        float dyGc = go.getGravityCenterY() - h / 2.0f;
        float dSq = dxGc * dxGc + dyGc * dyGc;
        return iCenter + mass * dSq;
    }

    /**
     * Returns the effective inverse inertia for an entity, taking its
     * {@link ShapeType} and the current collision speed into account.
     * <ul>
     *   <li>{@link ShapeType#CIRCLE}: always rotatable → full 1/I</li>
     *   <li>{@link ShapeType#RECTANGLE}, {@link ShapeType#LINE} and others:
     *       rotation is suppressed (returns 0) unless the squared relative
     *       velocity exceeds {@link #ROTATION_VELOCITY_THRESHOLD}².</li>
     * </ul>
     *
     * @param shape      The shape type of the entity.
     * @param isDynamic  Whether the entity is DYNAMIC.
     * @param inertia    The pre-computed moment of inertia.
     * @param relSpeedSq The squared magnitude of the relative linear velocity.
     * @return Effective inverse inertia (1/I or 0).
     */
    private float computeEffectiveInvInertia(ShapeType shape, boolean isDynamic,
                                             float inertia, float relSpeedSq) {
        if (!isDynamic || inertia <= 0) {
            return 0.0f;
        }
        // CIRCLE shapes always receive angular impulse
        if (shape == ShapeType.CIRCLE) {
            return 1.0f / inertia;
        }
        // RECTANGLE, LINE, etc.: only rotate under extreme collision speed
        float thresholdSq = ROTATION_VELOCITY_THRESHOLD * ROTATION_VELOCITY_THRESHOLD;
        if (relSpeedSq >= thresholdSq) {
            return 1.0f / inertia;
        }
        // Below threshold: no rotation
        return 0.0f;
    }

    /**
     * Zeroes near-zero velocities to prevent jitter and clamps excessive
     * velocities to keep the simulation stable.
     *
     * @param entity The entity whose velocities are to be clamped.
     */
    private void clampVelocities(GameObject entity) {
        // Zero out near-zero velocities
        if (Math.abs(entity.vx) < MIN_LINEAR_VELOCITY) entity.vx = 0;
        if (Math.abs(entity.vy) < MIN_LINEAR_VELOCITY) entity.vy = 0;
        if (Math.abs(entity.getVa()) < MIN_ANGULAR_VELOCITY) entity.setVa(0);

        // Clamp to maximum
        entity.vx = Math.max(-MAX_LINEAR_VELOCITY, Math.min(MAX_LINEAR_VELOCITY, entity.vx));
        entity.vy = Math.max(-MAX_LINEAR_VELOCITY, Math.min(MAX_LINEAR_VELOCITY, entity.vy));
        entity.setVa(Math.max(-MAX_ANGULAR_VELOCITY, Math.min(MAX_ANGULAR_VELOCITY, entity.getVa())));
    }
}
