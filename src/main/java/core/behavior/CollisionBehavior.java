package core.behavior;

import core.entity.Entity;
import core.entity.GameObject;
import core.physics.CollisionEvent;

/**
 * Specialisation of {@link Behavior} dedicated to collision response.
 * <p>
 * Implementations receive a {@link CollisionEvent} describing the collision
 * from the perspective of {@code self}. The {@link core.physics.CollisionManager}
 * filters entity behaviors by {@code instanceof CollisionBehavior} to invoke
 * {@link #onCollision} when an intersection is detected.
 * <p>
 * The inherited {@link #update} method is a no-op by default because collision
 * behaviors are event-driven, not frame-driven.
 *
 * @see CollisionEvent
 * @see core.physics.CollisionManager
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.1
 */
public interface CollisionBehavior extends Behavior<GameObject> {

    /**
     * Called by the {@link core.physics.CollisionManager} when {@code self}
     * collides with another entity.
     *
     * @param self      The entity that owns this behavior.
     * @param event     Collision data (other entity, normal, penetration, physics types).
     * @param deltaTime The elapsed time since the last frame (in seconds).
     */
    void onCollision(GameObject self, CollisionEvent event, float deltaTime);

    /**
     * No-op — collision behaviors are triggered via {@link #onCollision},
     * not during the regular physics update pass.
     */
    @Override
    default void update(Entity<?> entity, float deltaTime) {
        // intentionally empty
    }
}
