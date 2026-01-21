package core.behavior;

import core.entity.Entity;

public class GravityBehavior implements Behavior<Entity<?>> {
    public float gravity = 9.81f;

    public GravityBehavior(float gravity) {
        this.gravity = gravity;
    }

    public void update(Entity<?> entity, float deltaTime) {
        entity.vy += gravity * deltaTime;
    }

}
