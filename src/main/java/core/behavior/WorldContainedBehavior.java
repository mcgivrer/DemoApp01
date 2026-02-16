package core.behavior;

import core.entity.Entity;
import core.entity.GameObject;
import core.entity.PhysicsType;
import core.entity.World;

public class WorldContainedBehavior implements Behavior<Entity<?>> {
    private World world;

    public WorldContainedBehavior(World world) {
        this.world = world;
    }

    @Override
    public void update(Entity<?> e, float deltaTime) {
        GameObject entity = (GameObject) e;
        float restitution = 1.0f - entity.getMaterial().friction();

        if (entity.getPhysicsType() != PhysicsType.DYNAMIC) {
            return;
        }
        entity.setContact(false);
        if (entity.x < world.x) {
            entity.x = world.x;
            entity.setContact(true);
            entity.vx *= -restitution;
        } else if (entity.x + entity.width > world.width + world.x) {
            entity.x = world.width + world.x - entity.width;
            entity.setContact(true);
            entity.vx *= -restitution;
        }

        if (entity.y < world.y) {
            entity.y = world.y;
            entity.setContact(true);
            entity.vy *= -restitution;
        } else if (entity.y + entity.height > world.height + world.y) {
            entity.y = world.height + world.y - entity.height;
            entity.setContact(true);
            entity.vy *= -restitution;
        }
    }

}
