package core.entity;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class GameObject extends Entity<GameObject> {

    private BufferedImage image;
    private Color fillColor = Color.BLUE;
    private Color edgeColor = Color.WHITE;

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
        return new String[] {
            "GameObject ID: " + this.getId(),
            "Name: " + this.getName(),
            "Position: (" + this.getX() + ", " + this.getY() + ")",
            "Size: (" + this.getWidth() + " x " + this.getHeight() + ")"
        };
    }

}
