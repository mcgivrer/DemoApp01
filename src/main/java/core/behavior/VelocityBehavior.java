package core.behavior;

import core.entity.Entity;
import core.entity.GameObject;
import core.entity.PhysicsType;

/**
 * Behavior that updates an entity's position based on its velocity. The
 * entity's x and y coordinates are incremented by its vx and vy values
 * multiplied by the elapsed time (deltaTime). This behavior is typically used
 * to simulate movement in a game or simulation.
 * 
 * @see Entity
 * 
 */
public class VelocityBehavior implements Behavior<Entity<?>> {
    float timeFactor = 0.1f;

    @Override
    public void update(Entity<?> entity, float deltaTime) {

        // Only apply to DYNAMIC GameObjects
        if ((entity instanceof GameObject go) && (go.getPhysicsType().equals(PhysicsType.DYNAMIC))) {
            entity.setPosition(entity.x + entity.vx * deltaTime * timeFactor,
                    entity.y + entity.vy * deltaTime * timeFactor);
            // Si l'entité possède une vitesse angulaire (va), on met à jour l'angle
            try {
                java.lang.reflect.Field vaField = entity.getClass().getField("va");
                float va = vaField.getFloat(entity);
                entity.setAngle(entity.getAngle() + va * deltaTime * timeFactor);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Pas de champ va, on ignore
            }
        }
    }
}
