package core.graphics;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

import core.entity.GameObject;

/**
 * A render plugin for rendering GameObject entities. It handles drawing the
 * GameObject's sprite if available, or a filled rectangle with edge color
 * otherwise.
 * 
 * @see GameObject
 * @see RenderPlugin
 */
public class GameObjectRenderPlugin implements RenderPlugin<GameObject> {

    @Override
    public Class<GameObject> getSupportedEntityType() {
        return GameObject.class;
    }

    @Override
    public void render(GameObject entity, Graphics2D g) {
        int x = (int) entity.getX();
        int y = (int) entity.getY();
        int width = (int) entity.getWidth();
        int height = (int) entity.getHeight();

        // Appliquer la rotation autour du centre de l'entité
        Graphics2D g2 = (Graphics2D) g.create();
        double angleRad = Math.toRadians(entity.getAngle());
        g2.rotate(angleRad, x + width / 2.0, y + height / 2.0);

        switch (entity.getShapeType()) {
        case POINT -> {
            if (entity.getEdgeColor() != null) {
                g2.setColor(entity.getEdgeColor());
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(x, y, x, y);
            }
        }
        case LINE -> {
            if (entity.getEdgeColor() != null) {
                g2.setColor(entity.getEdgeColor());
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(x, y, x + width, y + height);
            }
        }
        case CIRCLE -> {
            if (entity.getFillColor() != null) {
                g2.setColor(entity.getFillColor());
                g2.fillOval(x, y, width, height);
            }
            if (entity.getEdgeColor() != null) {
                g2.setColor(entity.getEdgeColor());
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawOval(x, y, width, height);
            }
        }
        case RECTANGLE -> {
            if (entity.getFillColor() != null) {
                g2.setColor(entity.getFillColor());
                g2.fillRect(x, y, width, height);
            }
            if (entity.getEdgeColor() != null) {
                g2.setColor(entity.getEdgeColor());
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawRect(x, y, width, height);
            }
        }
        case POLYGON -> {
            if (entity.getFillColor() != null && entity.getPolygon() != null) {
                g2.setColor(entity.getFillColor());
                g2.fillPolygon(entity.getPolygon());
            }
            if (entity.getEdgeColor() != null && entity.getPolygon() != null) {
                g2.setColor(entity.getEdgeColor());
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawPolygon(entity.getPolygon());
            }
        }
        case SPRITE -> {
            if (entity.getSprite() != null) {
                g2.drawImage(entity.getSprite(), x, y, width, height, null);
            }
        }
        default -> throw new IllegalArgumentException("Unexpected value: " + entity.getShapeType());
        }
        g2.dispose();
    }

}
