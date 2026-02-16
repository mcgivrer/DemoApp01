package demo.scenes;

import core.entity.Entity;
import core.entity.GameObject;

/**
 * Behavior that controls a moving platform's velocity, oscillating horizontally
 * between minX and maxX. The actual movement is handled by VelocityBehavior.
 */
public class MovingPlatformBehavior implements core.behavior.Behavior<core.entity.GameObject> {

    private int minX;
    private int maxX;
    private float speed;

    public MovingPlatformBehavior(int minX, int maxX, float speed) {
        this.minX = minX;
        this.maxX = maxX;
        this.speed = speed;
    }

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        if (!(entity instanceof GameObject go)) return;
        
        float currentX = entity.getX();
        
        // Reverse direction at boundaries
        if (currentX <= minX && speed < 0) {
            speed = -speed;
        } else if (currentX >= maxX && speed > 0) {
            speed = -speed;
        }
        
        // Set velocity - VelocityBehavior will handle actual movement
        go.vx = speed;
    }

}
