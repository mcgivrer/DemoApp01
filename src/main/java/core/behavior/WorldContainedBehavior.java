package core.behavior;

import core.entity.Entity;
import core.entity.World;

public class WorldContainedBehavior implements Behavior<Entity<?>> {
    private World world;

    public WorldContainedBehavior(World world) {
        this.world = world;
    }

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        if (entity.x < world.x) {
            entity.x = world.x;
            entity.vx *= -entity.getMaterial().restitution();
        } else if (entity.x + entity.width > world.width + world.x) {
            entity.x = world.width + world.x - entity.width;
            entity.vx *= -entity.getMaterial().restitution();
        }

        if (entity.y < world.y) {
            entity.y = world.y;
            entity.vy *= -entity.getMaterial().restitution();
        } else if (entity.y + entity.height > world.height + world.y) {
            entity.y = world.height + world.y - entity.height;
            entity.vy *= -entity.getMaterial().restitution();
        }
    }

}
