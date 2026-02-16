package core.entity;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class GameObject extends Entity<GameObject> {

    private BufferedImage image;
    private Color fillColor = Color.BLUE;
    private Color edgeColor = Color.WHITE;

    private boolean contact = false;

    private Polygon polygon;
    private Rectangle2D boundingBox = new Rectangle2D.Double(0, 0, 0, 0);

    PhysicsType physicsType = PhysicsType.DYNAMIC;

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

    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }

    /**
     * Sets the bounding box for this GameObject.
     * 
     * @param box the bounding box to set
     * @return this GameObject instance
     */
    public GameObject setBoundingBox(Rectangle2D box) {
        this.boundingBox = box;
        return this;
    }

}
