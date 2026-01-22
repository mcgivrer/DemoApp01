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
    public void render(World world, Graphics2D g) {
        g.setColor(world.getSkyColor());
        g.fillRect((int) world.getX() - world.getWidth(), (int) world.getY()-world.getHeight(), (int) world.getWidth() * 3,
                (int) world.getHeight() * 2);

        g.setColor(world.getGroundColor());
        g.fillRect((int) world.getX() - world.getWidth(), (int) world.getY() + world.getHeight(),
                (int) world.getWidth() * 3, (int) world.getHeight() / 2);

        g.setColor(world.getEdgeColor());
        Stroke originalStroke = g.getStroke();
        g.drawRect((int) world.getX() - world.getWidth(), (int) world.getY() + world.getHeight(),
                (int) world.getWidth() * 3, 2);
        if (App.mode.equals(App.AppMode.DEVELOPMENT)) {
            // Ligne en pointillé
            float[] dash = { 8.0f, 8.0f };
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            g.setColor(Color.DARK_GRAY);
            g.drawRect((int) world.getX(), (int) world.getY(), (int) world.getWidth(), (int) world.getHeight());
        }
        g.setStroke(originalStroke);
    }

}
