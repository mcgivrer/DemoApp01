package core.entity;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class GameObject extends Entity<GameObject> {

    private BufferedImage image;
    private Color fillColor = Color.BLUE;
    private Color edgeColor = Color.WHITE;

    private Rectangle2D boundingBox = new Rectangle2D.Double(0, 0, 0, 0);

    PhysicType physicType = PhysicType.DYNAMIC;

    public GameObject(String name) {
        super(name);
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

    public String[] getDebugInfo() {
        return new String[] { "GameObject ID: " + this.getId(), "Name: " + this.getName(),
                "Position: (" + this.getX() + ", " + this.getY() + ")",
                "Size: (" + this.getWidth() + " x " + this.getHeight() + ")" };
    }

    public GameObject setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public GameObject setEdgeColor(Color edgeColor) {
        this.edgeColor = edgeColor;
        return this;
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

    public PhysicType getPhysicType() {
        return physicType;
    }

    public GameObject setPhysicType(PhysicType physicType) {
        this.physicType = physicType;
        return this;
    }

}
