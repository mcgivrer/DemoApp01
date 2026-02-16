package core.graphics;

import java.awt.Color;
import java.awt.Graphics2D;

import core.App;
import core.entity.Entity;

/**
 * A render plugin that displays debug information for entities. It shows
 * details such as ID, name, position, size, and velocity next to the entity.
 * This plugin is useful for development and debugging purposes.
 * 
 * @see Entity#getDebugInfo()
 * @see RenderPlugin
 * 
 */
public class DebugRenderPlugin implements RenderPlugin<Entity<?>> {

    @Override
    public Class<Entity<?>> getSupportedEntityType() {
        return (Class<Entity<?>>) (Class<?>) Entity.class;
    }

    @Override
    public void render(Entity<?> entity, Graphics2D g) {
        if (App.debug > 0) {
            g.setColor(Color.CYAN);
            // Affiche la direction de la vitesse
            g.drawLine((int) entity.getCenterX(), (int) entity.getCenterY(),
                    (int) (entity.getCenterX() + entity.vx * 0.005f),
                    (int) (entity.getCenterY() + entity.vy * 0.005f));

            // Affiche l'axe de rotation
            double angleRad = Math.toRadians(entity.getAngle());
            int cx = (int) entity.getCenterX();
            int cy = (int) entity.getCenterY();
            int len = Math.min(entity.width, entity.height) / 2;
            int ax = (int) (cx + Math.cos(angleRad) * len);
            int ay = (int) (cy + Math.sin(angleRad) * len);
            g.setColor(Color.MAGENTA);
            g.drawLine(cx, cy, ax, ay);
            if (App.debug > entity.getDebugLevel()) {
                if (entity.getDebugInfo() != null) {
                    g.setFont(g.getFont().deriveFont(10f));
                    g.setColor(Color.ORANGE);
                    for (int i = 0; i < entity.getDebugInfo().length; i++) {
                        g.drawString(entity.getDebugInfo()[i], (int) entity.getX() + entity.width + 4 + 0.5f,
                                (int) entity.getY() + (i * 11) + 0.5f);
                    }
                }

            }
        }

    }

}
