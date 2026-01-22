package core.entity;

import java.awt.Color;

public class World extends Entity<World> {
    private float gravity = 9.81f;
    private Color skyColor = Color.CYAN.darker();
    private Color groundColor = Color.GREEN.darker().darker();
    private Color edgeColor = Color.BLACK;

    public World(String name) {
        super(name);
    }

    public float getGravity() {
        return gravity;
    }

    public World setGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    public Color getSkyColor() {
        return skyColor;
    }

    public World setSkyColor(Color skyColor) {
        this.skyColor = skyColor;
        return this;
    }

    public Color getGroundColor() {
        return groundColor;
    }

    public World setGroundColor(Color groundColor) {
        this.groundColor = groundColor;
        return this;
    }

    public Color getEdgeColor() {
        return edgeColor;
    }

    public World setEdgeColor(Color edgeColor) {
        this.edgeColor = edgeColor;
        return this;
    }

    public String[] getDebugInfo() {
        return new String[] { "World ID: " + this.getId(), "Name: " + this.getName(),
                "Position: (" + this.getX() + ", " + this.getY() + ")",
                "Size: (" + this.getWidth() + " x " + this.getHeight() + ")", "Gravity: " + this.getGravity() + "m/s" };
    }

}
