package demo.scenes;

import core.entity.Entity;

public class MovingPlatformBehavior implements core.behavior.Behavior<core.entity.GameObject> {

    private int minX;
    private int maxX;
    private float speed;

    public MovingPlatformBehavior(int i, int j, float f) {
        minX = i;
        maxX = j;
        speed = f;
    }

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        float newX = entity.getX() + speed * deltaTime;
        if (newX < minX) {
            newX = minX;
            speed = -speed; // Reverse direction
        } else if (newX > maxX) {
            newX = maxX;
            speed = -speed; // Reverse direction
        }
        entity.setPosition(newX, entity.getY());
    }

}
