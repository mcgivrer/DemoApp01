package core.behavior;

import core.entity.Entity;
import core.entity.GameObject;
import core.entity.PhysicType;

/**
 * Behavior that applies a gravitational force to an entity by modifying its
 * vertical velocity (vy) over time. The gravity value determines the strength
 * of the gravitational pull. This behavior is commonly used in physics
 * simulations and games to simulate the effect of gravity on objects.
 * 
 * @see Entity
 */
public class GravityBehavior implements Behavior<GameObject> {
    public float gravity = 9.81f;
    public float gravityScale = 2500.0f;

    public GravityBehavior(float gravity) {
        this.gravity = gravity;
    }

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        entity.vy += gravity * gravityScale * deltaTime;
    }

}
