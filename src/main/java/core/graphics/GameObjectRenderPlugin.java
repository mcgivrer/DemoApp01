package core.graphics;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

import core.entity.GameObject;

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

        if (entity.getSprite() != null) {
            g.drawImage(entity.getSprite(), x, y, width, height, null);
            return;
        }

        g.setColor(entity.getFillColor());
        g.fillRect(x, y, width, height);

        g.setColor(entity.getEdgeColor());
        g.setStroke(new BasicStroke(0.5f));
        g.drawRect(x, y, width, height);
    }

}
