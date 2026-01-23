package core.entity;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class GameObject extends Entity<GameObject> {

    private BufferedImage image;
    private Color fillColor = Color.BLUE;
    private Color edgeColor = Color.WHITE;

    private boolean solid = true;
    private boolean contact = false;

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


    public GameObject setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public GameObject setEdgeColor(Color edgeColor) {
        this.edgeColor = edgeColor;
        return this;
    }

    public boolean isSolid() {
        return solid;
    }

    public GameObject setSolid(boolean solid) {
        this.solid = solid;
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
        return new String[] { 
            "GameObject ID: " + this.getId(), 
            "Name: " + this.getName(),
            "Position: (" + this.getX() + ", " + this.getY() + ")",
            "Size: (" + this.getWidth() + " x " + this.getHeight() + ")",
            "Velocity: (" + this.getVx() + ", " + this.getVy() + ")",
            "Solid: " + this.isSolid(),
            "Contact: " + this.isContact()
         };
    }
}
