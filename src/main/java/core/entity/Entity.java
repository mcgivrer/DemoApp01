package core.entity;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.behavior.Behavior;
import core.graphics.Layer;
import core.physics.Material;

public class Entity<T> {
    private static long idx = 0;
    private long id = idx++;

    private String name = "entity_" + id;

    private boolean active = true;
    private boolean visible = true;

    private int debugLevel = 1;

    private int priority = -1;

    public float x, y;
    public int width, height;
    public float vx, vy;
    // Axe de rotation (en degrés)
    public float angle = 0f;
    // Vitesse angulaire (en degrés/seconde)
    public float va = 0f;

    private Material material = Material.DEFAULT;
    private float mass = 1.0f;

    // Centre de gravité relatif au coin supérieur gauche de l'entité.
    // Par défaut, il est au centre géométrique (mis à jour via setSize).
    private float gravityCenterX = 0f;
    private float gravityCenterY = 0f;

    private ShapeType shapeType = ShapeType.RECTANGLE;

    Layer layer = null;

    protected List<Behavior<?>> behaviors = new ArrayList<>();

    protected Map<String, Object> attributes = new HashMap<>();
    private Rectangle2D boundingBox = new Rectangle2D.Float();

    public Entity(String name) {
        this.name = name;
    }

    public T add(Behavior<?> behavior) {
        behaviors.add(behavior);
        return (T) this;
    }

    public T remove(Behavior<?> behavior) {
        behaviors.remove(behavior);
        return (T) this;
    }

    public T setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
        return (T) this;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public <Y> T setAttribute(String key, Y value) {
        attributes.put(key, value);
        return (T) this;
    }

    public <Y> Y getAttribute(String key, Y defaultValue) {
        return (Y) attributes.getOrDefault(key, defaultValue);
    }

    public T setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        boundingBox.setRect(x, y, width, height);
        return (T) this;
    }

    public T setSize(int width, int height) {
        this.width = width;
        this.height = height;
        // Recalculate default gravity center to geometric center
        this.gravityCenterX = width / 2.0f;
        this.gravityCenterY = height / 2.0f;
        boundingBox.setRect(x, y, width, height);
        return (T) this;
    }

    public T setVelocity(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
        return (T) this;
    }

    public T setPriority(int priority) {
        this.priority = priority;
        return (T) this;
    }

    public int getPriority() {
        return priority;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the X coordinate of the gravity center in world space.
     * Defaults to geometric center unless overridden via
     * {@link #setGravityCenterX(float)}.
     */
    public float getCenterX() {
        return x + gravityCenterX;
    }

    /**
     * Returns the Y coordinate of the gravity center in world space.
     * Defaults to geometric center unless overridden via
     * {@link #setGravityCenterY(float)}.
     */
    public float getCenterY() {
        return y + gravityCenterY;
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

    public T setDebugLevel(int level) {
        this.debugLevel = level;
        return (T) this;
    }

    public Layer getLayer() {
        return layer;
    }

    public T setMaterial(Material material) {
        this.material = material;
        return (T) this;
    }

    public T setMass(float mass) {
        this.mass = mass;
        return (T) this;
    }

    public float getMass() {
        return mass;
    }

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

    public Material getMaterial() {
        return material;
    }

    /**
     * Sets the gravity center X offset relative to the entity's top-left corner.
     * @param gcx X offset in pixels.
     * @return this entity.
     */
    public T setGravityCenterX(float gcx) {
        this.gravityCenterX = gcx;
        return (T) this;
    }

    /**
     * Sets the gravity center Y offset relative to the entity's top-left corner.
     * @param gcy Y offset in pixels.
     * @return this entity.
     */
    public T setGravityCenterY(float gcy) {
        this.gravityCenterY = gcy;
        return (T) this;
    }

    /**
     * Sets both gravity center offsets at once.
     * @param gcx X offset relative to top-left.
     * @param gcy Y offset relative to top-left.
     * @return this entity.
     */
    public T setGravityCenter(float gcx, float gcy) {
        this.gravityCenterX = gcx;
        this.gravityCenterY = gcy;
        return (T) this;
    }

    public float getGravityCenterX() {
        return gravityCenterX;
    }

    public float getGravityCenterY() {
        return gravityCenterY;
    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public String[] getDebugInfo() {
        return new String[] { "id=" + id, "name=" + name, "pos=(%4.2f,%4.2f)".formatted(x, y),
                "size=(" + width + "x" + height + ")", "vel=(%4.2f,%4.2f)".formatted(vx, vy),
                "angle=%4.2f".formatted(angle), "va=%4.2f".formatted(va),
                "gc=(%4.2f,%4.2f)".formatted(gravityCenterX, gravityCenterY) };
    }

    public Rectangle2D getBounds() {
        return this.boundingBox;
    }

}
