package core.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import core.App;
import core.entity.World;

public class WorldRenderPlugin implements RenderPlugin<World> {
    public WorldRenderPlugin() {
    }

    @Override
    public Class<World> getSupportedEntityType() {
        return World.class;
    }

    @Override
    public void render(World entity, Graphics2D g) {
        g.setColor(entity.getSkyColor());
        g.fillRect((int) entity.getX() - entity.getWidth(), (int) entity.getY(), (int) entity.getWidth() * 3,
                (int) entity.getHeight() * 2);

        g.setColor(entity.getGroundColor());
        g.fillRect((int) entity.getX() - entity.getWidth(), (int) entity.getY() + entity.getHeight(),
                (int) entity.getWidth() * 3, (int) entity.getHeight() / 2);

        g.setColor(entity.getEdgeColor());
        Stroke originalStroke = g.getStroke();
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect((int) entity.getX() - entity.getWidth(), (int) entity.getY() + entity.getHeight(),
                (int) entity.getWidth() * 3, 2);
        if (App.mode.equals(App.AppMode.DEVELOPMENT)) {
            g.setStroke(new BasicStroke(0.5f));
            g.setColor(Color.DARK_GRAY);
            g.drawRect((int) entity.getX(), (int) entity.getY(), (int) entity.getWidth(), (int) entity.getHeight());
        }
        g.setStroke(originalStroke);
    }

}
