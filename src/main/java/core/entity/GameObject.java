package core.entity;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

public class GameObject extends Entity<GameObject> {

    private BufferedImage image;
    private Color fillColor = Color.BLUE;
    private Color edgeColor = Color.WHITE;

    private boolean contact = false;

    private Polygon polygon;

    PhysicsType physicsType = PhysicsType.DYNAMIC;

    /**
     * Velocity inherited from a KINEMATIC platform this object is standing on.
     * Reset each frame before collision detection.
     */
    private float platformVx = 0;
    private float platformVy = 0;

    public GameObject(String name) {
        super(name);
    }

    public GameObject setPolygon(Polygon polygon) {
        this.polygon = polygon;
        return this;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public GameObject setSprite(BufferedImage image) {
        this.image = image;
        return this;
    }

    public BufferedImage getSprite() {
        return image;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public Color getEdgeColor() {
        return edgeColor;
    }

    public GameObject setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public GameObject setEdgeColor(Color edgeColor) {
        this.edgeColor = edgeColor;
        return this;
    }

    public PhysicsType getPhysicsType() {
        return physicsType;
    }

    public GameObject setPhysicsType(PhysicsType physicsType) {
        this.physicsType = physicsType;
        return this;
    }

    public boolean isContact() {
        return contact;
    }

    public GameObject setContact(boolean contact) {
        this.contact = contact;
        return this;
    }

    public String[] getDebugInfo() {
        return new String[] { "GameObject ID: " + this.getId(), "Name: " + this.getName(),
                "Position: (" + this.getX() + ", " + this.getY() + ")",
                "Size: (" + this.getWidth() + " x " + this.getHeight() + ")",
                "Velocity: (" + this.getVx() + ", " + this.getVy() + ")", "physicsType: " + this.getPhysicsType(),
                "Contact: " + this.isContact() };
    }

    /**
     * Gets the platform velocity X component (inherited from KINEMATIC platform).
     * @return The platform velocity X in px/s.
     */
    public float getPlatformVx() {
        return platformVx;
    }

    /**
     * Gets the platform velocity Y component (inherited from KINEMATIC platform).
     * @return The platform velocity Y in px/s.
     */
    public float getPlatformVy() {
        return platformVy;
    }

    /**
     * Sets the platform velocity (inherited from KINEMATIC platform).
     * @param pvx Platform velocity X.
     * @param pvy Platform velocity Y.
     * @return this GameObject instance.
     */
    public GameObject setPlatformVelocity(float pvx, float pvy) {
        this.platformVx = pvx;
        this.platformVy = pvy;
        return this;
    }

    /**
     * Resets the platform velocity to zero.
     * Should be called at the start of each collision detection pass.
     * @return this GameObject instance.
     */
    public GameObject resetPlatformVelocity() {
        this.platformVx = 0;
        this.platformVy = 0;
        return this;
    }

}
