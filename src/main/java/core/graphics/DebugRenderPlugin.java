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
            if (entity.getDebugInfo() != null) {
                g.setFont(g.getFont().deriveFont(10f));
                g.setColor(Color.ORANGE);
                for (int i = 0; i < entity.getDebugInfo().length; i++) {
                    g.drawString(entity.getDebugInfo()[i], (int) entity.getX() + entity.width + 4+0.5f,
                            (int) entity.getY() + (i * 11)+0.5f);
                }
            }
            g.setColor(Color.CYAN);
            g.drawLine(
                (int) (entity.x + entity.width / 2), 
                (int) (entity.y + entity.height / 2),
                (int) (entity.x + entity.width / 2 + entity.vx * 0.25f),
                (int) (entity.y + entity.height / 2 + entity.vy * 0.25f));

        }

    }

}
