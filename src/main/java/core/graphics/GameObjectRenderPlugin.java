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

        if (entity.getSprite() != null) {
            g2.drawImage(entity.getSprite(), x, y, width, height, null);
            g2.dispose();
            return;
        }

        g2.setColor(entity.getFillColor());
        g2.fillRect(x, y, width, height);

        g2.setColor(entity.getEdgeColor());
        g2.setStroke(new BasicStroke(0.5f));
        g2.drawRect(x, y, width, height);
        g2.dispose();
    }

}
