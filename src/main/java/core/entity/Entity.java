package core.entity;

import java.util.ArrayList;
import java.util.List;

import core.behavior.Behavior;
import core.graphics.Layer;
import core.physics.Material;

public class Entity<T> {
    private static long idx = 0;
    private long id = idx++;

    private String name = "entity_" + id;

    private boolean active = true;
    private boolean visible = true;

    public float x, y;
    public int width, height;
    public float vx, vy;
    // Axe de rotation (en degrés)
    public float angle = 0f;
    // Vitesse angulaire (en degrés/seconde)
    public float va = 0f;
        public T setVa(float va) {
            this.va = va;
            return (T) this;
        }

        public float getVa() {
            return va;
        }
    public T setAngle(float angle) {
        this.angle = angle;
        return (T) this;
    }

    public float getAngle() {
        return angle;
    }

    Layer layer = null;

    protected List<Behavior<?>> behaviors = new ArrayList<>();
    private Material material = Material.DEFAULT;

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

    public long getId() {
        return id;
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

    public T setActive(boolean active) {
        this.active = active;
        return (T) this;
    }

    public T setVisible(boolean visible) {
        this.visible = visible;
        return (T) this;
    }

    public T setLayer(Layer layer) {
        this.layer = layer;
        return (T) this;
    }

    public Layer getLayer() {
        return layer;
    }

    public T setMaterial(Material material) {
        this.material = material;
        return (T) this;
    }

    public Material getMaterial() {
        return material;
    }

    public String[] getDebugInfo() {
        return new String[] {
            "id=" + id,
            "name=" + name,
            "pos=(%4.2f,%4.2f)".formatted(x, y),
            "size=(" + width + "x" + height + ")",
            "vel=(%4.2f,%4.2f)".formatted(vx, vy),
            "angle=%4.2f".formatted(angle),
            "va=%4.2f".formatted(va)
        };
    }

}
