package core.behavior;

import core.entity.Entity;

/**
 * Behavior that updates an entity's position based on its velocity. The
 * entity's x and y coordinates are incremented by its vx and vy values
 * multiplied by the elapsed time (deltaTime). This behavior is typically used
 * to simulate movement in a game or simulation.
 * 
 * @see Entity
 * 
 */
public class VelocityBehavior implements Behavior<Entity<?>> {
    @Override
    public void update(Entity<?> entity, float deltaTime) {
        entity.x += entity.vx * deltaTime;
        entity.y += entity.vy * deltaTime;
    }
}
