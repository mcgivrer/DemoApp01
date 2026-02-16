package core.behavior;

import core.entity.GameObject;
import core.physics.CollisionEvent;

/**
 * Default collision response that applies positional correction, impulse-based
 * velocity adjustment, and friction to the <em>self</em> entity only.
 * <p>
 * When both entities are DYNAMIC the correction and impulse are halved so that
 * each entity's own {@code DefaultCollisionResponseBehavior} handles its share.
 * When only {@code self} is DYNAMIC it receives the full correction.
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
 * @version 0.0.1
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

        // --- Positional correction ---
        float correctionFactor = otherDynamic ? 0.5f : 1.0f;
        self.setPosition(
                self.x + nx * penetration * correctionFactor,
                self.y + ny * penetration * correctionFactor);

        // --- Velocity response using Material properties ---
        float restitution = Math.min(
                self.getMaterial().restitution(),
                other.getMaterial().restitution());
        float friction = (self.getMaterial().friction() + other.getMaterial().friction()) / 2.0f;

        // Relative velocity projected onto the collision normal
        float relVn = (self.vx - other.vx) * nx + (self.vy - other.vy) * ny;

        // Only resolve if the entities are approaching along the normal
        if (relVn <= 0) {
            return;
        }

        float impulse = -(1 + restitution) * relVn;
        float impulseFactor = otherDynamic ? 0.5f : 1.0f;

        self.vx += impulse * impulseFactor * nx;
        self.vy += impulse * impulseFactor * ny;

        // --- Friction on tangential velocity ---
        float tx = -ny;
        float ty = nx;
        float tangentVel = self.vx * tx + self.vy * ty;
        self.vx -= friction * tangentVel * tx;
        self.vy -= friction * tangentVel * ty;
    }
}
