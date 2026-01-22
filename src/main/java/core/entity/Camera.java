package core.entity;

import core.behavior.CameraBehavior;

public class Camera extends Entity<Camera> {

    private Entity<?> target;
    private float tweenFactor = 0.1f;
    private boolean active = true;

    public Camera(String name) {
        super(name);
    }

    public Camera(String name, float offsetX, float offsetY) {
        super(name);
        add(new CameraBehavior(offsetX, offsetY));
    }

    public Camera setTarget(Entity<?> target) {
        this.target = target;
        return this;
    }

    public Entity<?> getTarget() {
        return target;
    }

    public Camera setTweenFactor(float tweenFactor) {
        this.tweenFactor = tweenFactor;
        return this;
    }

    public float getTweenFactor() {
        return tweenFactor;
    }

    public Camera setActive(boolean active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return active;
    }

}
