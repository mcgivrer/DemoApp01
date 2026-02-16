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
    public void onCollision(GameObject self, CollisionEvent event) {
        GameObject other = event.other();
        float nx = event.nx();
        float ny = event.ny();
        float penetration = event.penetration();
        boolean selfDynamic = event.selfDynamic();
        boolean otherDynamic = event.otherDynamic();

        if (!selfDynamic) {
            return;
        }

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
        float relSpeedSq = (self.vx - other.vx) * (self.vx - other.vx)
                + (self.vy - other.vy) * (self.vy - other.vy);

        // Only CIRCLE shapes rotate freely; RECTANGLE/LINE/others require
        // an extreme collision speed to receive any angular impulse.
        float invInertiaSelf = computeEffectiveInvInertia(
                self.getShapeType(), selfDynamic, inertiaSelf, relSpeedSq);
        float invInertiaOther = computeEffectiveInvInertia(
                other.getShapeType(), otherDynamic, inertiaOther, relSpeedSq);

        // --- Contact point lever arms (rotation-aware) ---
        // Transform the collision normal into each entity's local frame to
        // find the support point (farthest corner toward the other entity),
        // then compute the lever arm from gravity center to that point
        // in world space.
        float selfGcX = self.getGravityCenterX();
        float selfGcY = self.getGravityCenterY();
        float otherGcX = other.getGravityCenterX();
        float otherGcY = other.getGravityCenterY();

        // Self: rotate −n into local frame → support point → lever arm
        float selfAngleRad = (float) Math.toRadians(self.getAngle());
        float selfCos = (float) Math.cos(selfAngleRad);
        float selfSin = (float) Math.sin(selfAngleRad);
        float selfLocalNx =  selfCos * (-nx) + selfSin * (-ny);
        float selfLocalNy = -selfSin * (-nx) + selfCos * (-ny);
        float selfHalfW = self.getWidth() / 2.0f;
        float selfHalfH = self.getHeight() / 2.0f;
        float sSupportX = selfHalfW + Math.signum(selfLocalNx) * selfHalfW;
        float sSupportY = selfHalfH + Math.signum(selfLocalNy) * selfHalfH;
        float rSLocalX = sSupportX - selfGcX;
        float rSLocalY = sSupportY - selfGcY;
        float rSelfX = selfCos * rSLocalX - selfSin * rSLocalY;
        float rSelfY = selfSin * rSLocalX + selfCos * rSLocalY;

        // Other: rotate n into local frame → support point → lever arm
        float otherAngleRad = (float) Math.toRadians(other.getAngle());
        float otherCos = (float) Math.cos(otherAngleRad);
        float otherSin = (float) Math.sin(otherAngleRad);
        float oLocalNx =  otherCos * nx + otherSin * ny;
        float oLocalNy = -otherSin * nx + otherCos * ny;
        float otherHalfW = other.getWidth() / 2.0f;
        float otherHalfH = other.getHeight() / 2.0f;
        float oSupportX = otherHalfW + Math.signum(oLocalNx) * otherHalfW;
        float oSupportY = otherHalfH + Math.signum(oLocalNy) * otherHalfH;
        float rOLocalX = oSupportX - otherGcX;
        float rOLocalY = oSupportY - otherGcY;
        float rOtherX = otherCos * rOLocalX - otherSin * rOLocalY;
        float rOtherY = otherSin * rOLocalX + otherCos * rOLocalY;

        // --- Cross products r × n (used for correction and impulse) ---
        float rCrossNSelf  = rSelfX  * ny - rSelfY  * nx;
        float rCrossNOther = rOtherX * ny - rOtherY * nx;

        // --- Angular velocities (convert degrees/s → radians/s) ---
        float omegaSelf = (float) Math.toRadians(self.getVa());
        float omegaOther = (float) Math.toRadians(other.getVa());

        // --- Velocity at contact point = v_linear + ω × r ---
        float vSelfX = self.vx - omegaSelf * rSelfY;
        float vSelfY = self.vy + omegaSelf * rSelfX;
        float vOtherX = other.vx - omegaOther * rOtherY;
        float vOtherY = other.vy + omegaOther * rOtherX;

        // --- Positional + angular correction (gravity-center aware) ---
        float correctionRatio = invMassSelf / invMassSum;
        float totalCorrection = penetration * correctionRatio;
        float angTerm = rCrossNSelf * rCrossNSelf * invInertiaSelf;
        float corrDenom = invMassSelf + angTerm;
        if (corrDenom > 1e-8f) {
            float linCorr = (invMassSelf / corrDenom) * totalCorrection;
            self.setPosition(
                    self.x + nx * linCorr,
                    self.y + ny * linCorr);
            if (angTerm > 1e-8f) {
                float angCorr = (rCrossNSelf * invInertiaSelf / corrDenom)
                        * totalCorrection;
                self.setAngle(self.getAngle() + (float) Math.toDegrees(angCorr));
            }
        } else {
            self.setPosition(
                    self.x + nx * totalCorrection,
                    self.y + ny * totalCorrection);
        }

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

        // --- Clamp to safe bounds ---
        clampVelocities(self);
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
