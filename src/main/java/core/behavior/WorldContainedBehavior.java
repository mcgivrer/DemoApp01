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
        float restitution = entity.getMaterial().restitution();
        float friction = entity.getMaterial().friction();

        if (entity.getPhysicsType() != PhysicsType.DYNAMIC) {
            return;
        }
        
        boolean hadContact = false;
        
        // Left boundary
        if (entity.x < world.x) {
            entity.x = world.x;
            hadContact = true;
            entity.vx *= -restitution;
            // Apply friction to tangential velocity (Y)
            entity.vy *= (1.0f - friction);
        } 
        // Right boundary
        else if (entity.x + entity.width > world.width + world.x) {
            entity.x = world.width + world.x - entity.width;
            hadContact = true;
            entity.vx *= -restitution;
            // Apply friction to tangential velocity (Y)
            entity.vy *= (1.0f - friction);
        }

        // Top boundary
        if (entity.y < world.y) {
            entity.y = world.y;
            hadContact = true;
            entity.vy *= -restitution;
            // Apply friction to tangential velocity (X)
            entity.vx *= (1.0f - friction);
        } 
        // Bottom boundary
        else if (entity.y + entity.height > world.height + world.y) {
            entity.y = world.height + world.y - entity.height;
            hadContact = true;
            entity.vy *= -restitution;
            // Apply friction to tangential velocity (X)
            entity.vx *= (1.0f - friction);
        }
        
        entity.setContact(hadContact);
    }

}
