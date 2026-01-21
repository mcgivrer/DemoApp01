package core.entity;

import java.util.ArrayList;
import java.util.List;

import core.behavior.Behavior;

public class Entity<T> {
    private static long idx = 0;
    private long id = idx++;

    private String name = "entity_" + id;

    private boolean active = true;
    private boolean visible = true;

    public float x, y;
    public int width, height;
    public float vx, vy;

    public List<Behavior<?>> behaviors = new ArrayList<>();

    public Entity(String name) {
        this.name = name;
    }

    public T add(Behavior<?> behavior) {
        behaviors.add(behavior);
        return (T) this;
    }

    public T setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        return (T) this;
    }

    public T setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return (T) this;
    }

    public T setVelocity(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    public float getCenterX() {
        return x + width / 2.0f;
    }

    public float getCenterY() {
        return y + height / 2.0f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getVx() {
        return vx;
    }

    public float getVy() {
        return vy;
    }

    public List<Behavior<?>> getBehaviors() {
        return behaviors;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isVisible() {
        return visible;
    }

    public T setVx(float f) {
        this.vx = f;
        return (T) this;
    }

    public T setVy(float f) {
        this.vy = f;
        return (T) this;
    }
}
