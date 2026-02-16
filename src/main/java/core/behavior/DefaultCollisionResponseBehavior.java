package core.behavior;

import core.entity.GameObject;
import core.physics.CollisionEvent;

/**
 * Default collision response that applies positional correction, impulse-based
 * velocity adjustment, and friction to the <em>self</em> entity only.
 * <p>
 * <strong>Mass-aware distribution:</strong> when both entities are DYNAMIC,
 * positional correction and impulse are distributed according to the inverse
 * mass ratio so that lighter entities are pushed more than heavier ones.
 * The standard physics impulse formula is used:
 * <pre>
 *   j = -(1 + e) &middot; v_rel &middot; n&#x0302; / (1/m_self + 1/m_other)
 * </pre>
 * Each entity then receives &Delta;v = j / m.
 * <p>
 * When only {@code self} is DYNAMIC it receives the full correction (the
 * other entity acts as an infinite-mass wall).
 * <p>
 * Material properties ({@link core.physics.Material#restitution()} and
 * {@link core.physics.Material#friction()}) drive the response intensity.
 *
 * @see CollisionBehavior
 * @see CollisionEvent
 * @see core.physics.CollisionManager
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.2
 */
public class DefaultCollisionResponseBehavior implements CollisionBehavior {

    @Override
    public void onCollision(GameObject self, CollisionEvent event) {
        GameObject other = event.other();
        float nx = event.nx();
        float ny = event.ny();
        float penetration = event.penetration();
        boolean selfDynamic = event.selfDynamic();
        boolean otherDynamic = event.otherDynamic();

        if (!selfDynamic) {
            // Nothing to do — this entity is STATIC
            return;
        }

        // --- Inverse masses ---
        float selfMass = self.getMass();
        float otherMass = other.getMass();
        float invMassSelf = (selfMass > 0) ? 1.0f / selfMass : 0.0f;
        float invMassOther = (otherDynamic && otherMass > 0) ? 1.0f / otherMass : 0.0f;
        float invMassSum = invMassSelf + invMassOther;

        // Safety: avoid division by zero (shouldn't happen if self is DYNAMIC)
        if (invMassSum <= 0) {
            return;
        }

        // --- Positional correction (mass-weighted) ---
        // self's share = invMassSelf / invMassSum  (lighter → larger share)
        float correctionRatio = invMassSelf / invMassSum;
        self.setPosition(
                self.x + nx * penetration * correctionRatio,
                self.y + ny * penetration * correctionRatio);

        // --- Velocity response using Material properties ---
        float restitution = Math.min(
                self.getMaterial().restitution(),
                other.getMaterial().restitution());
        float friction = (self.getMaterial().friction() + other.getMaterial().friction()) / 2.0f;

        // Relative velocity projected onto the collision normal
        // relVn < 0 means self is moving toward other (approaching) → must resolve
        // relVn >= 0 means self is moving away from other (separating) → skip
        float relVn = (self.vx - other.vx) * nx + (self.vy - other.vy) * ny;

        if (relVn >= 0) {
            return;
        }

        // Impulse magnitude:  j = -(1+e) * relVn / (1/m_self + 1/m_other)
        float j = -(1 + restitution) * relVn / invMassSum;

        // Apply impulse to self:  Δv = j / m_self  =  j * invMassSelf
        self.vx += j * invMassSelf * nx;
        self.vy += j * invMassSelf * ny;

        // --- Friction on tangential velocity ---
        float tx = -ny;
        float ty = nx;
        float tangentVel = self.vx * tx + self.vy * ty;
        self.vx -= friction * tangentVel * tx;
        self.vy -= friction * tangentVel * ty;
    }
}
