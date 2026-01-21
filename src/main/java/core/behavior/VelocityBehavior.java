package core.behavior;

import core.entity.Entity;

public class VelocityBehavior implements Behavior<Entity<?>> {
    public void update(Entity<?> entity, float deltaTime) {
        entity.x += entity.vx * deltaTime;
        entity.y += entity.vy * deltaTime;
    }
}
