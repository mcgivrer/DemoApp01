package core.behavior;

import core.entity.Entity;

public class WorldContainedBehavior implements Behavior<Entity<?>> {

    private float worldWidth;
    private float worldHeight;

    public WorldContainedBehavior(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        if (entity.x < 0) {
            entity.x = 0;
            entity.vx = 0;
        } else if (entity.x + entity.width > worldWidth) {
            entity.x = worldWidth - entity.width;
            entity.vx = 0;
        }

        if (entity.y < 0) {
            entity.y = 0;
            entity.vy = 0;
        } else if (entity.y + entity.height > worldHeight) {
            entity.y = worldHeight - entity.height;
            entity.vy = 0;
        }
    }

}
