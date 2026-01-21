package core.graphics;

import java.awt.Graphics2D;

import core.entity.Entity;

public interface RenderPlugin<T extends Entity<?>> {
    Class<T> getSupportedEntityType();

    void render(T entity, Graphics2D g);
}
