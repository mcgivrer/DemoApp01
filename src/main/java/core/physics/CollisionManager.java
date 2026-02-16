package core.physics;

import static core.App.log;

import java.util.List;
import java.util.Map;

import core.App;
import core.behavior.CollisionBehavior;
import core.entity.Entity;
import core.entity.GameObject;
import core.entity.PhysicsType;
import core.scene.Scene;
import core.utils.Configuration;
import core.utils.Service;

/**
 * Manages collision <strong>detection</strong> between {@link GameObject} entities
 * in a {@link Scene}. Uses AABB (Axis-Aligned Bounding Box) intersection tests
 * and computes overlap along each axis (SAT-lite).
 * <p>
 * Collision <strong>response</strong> is delegated to {@link CollisionBehavior}
 * instances attached to each entity. The manager filters each entity's behaviors
 * by {@code instanceof CollisionBehavior} and invokes
 * {@link CollisionBehavior#onCollision} with a {@link CollisionEvent} describing
 * the collision from that entity's perspective.
 *
 * @see CollisionBehavior
 * @see CollisionEvent
 * @see GameObject
 * @see PhysicsType
 * @see Scene
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.3
 */
public class CollisionManager extends Service {

    public CollisionManager(App app) {
        super(app);
    }

    /**
     * Initializes the CollisionManager with the given configuration.
     *
     * @param config Configuration properties for initialization.
     */
    @Override
    public void initialize(Configuration config) {
        log(CollisionManager.class, App.LogLevel.INFO, "CollisionManager initialized.");
    }

    /**
     * Detects collisions for all active {@link GameObject} pairs in
     * the scene. Each unique pair is processed exactly once.
     * <p>
     * When an intersection is found the manager builds a {@link CollisionEvent}
     * and dispatches it to every {@link CollisionBehavior} attached to each
     * involved entity (filtered via {@code instanceof}).
     *
     * @param scene     The current scene containing the entities.
     * @param deltaTime The elapsed time since the last frame (in seconds).
     * @param stats     A map to collect statistics during the update.
     */
    @Override
    public void update(Scene scene, float deltaTime, Map<String, Object> stats) {
        List<Entity<?>> entities = scene.getEntities();
        int size = entities.size();
        int collisionCount = 0;

        // Reset contact flags before detection pass
        for (var e : entities) {
            if (e instanceof GameObject go) {
                go.setContact(false);
            }
        }

        // Process each unique pair (i < j) to avoid duplicate resolution
        for (int i = 0; i < size - 1; i++) {
            Entity<?> a = entities.get(i);
            if (!a.isActive() || !(a instanceof GameObject goA)) continue;

            for (int j = i + 1; j < size; j++) {
                Entity<?> b = entities.get(j);
                if (!b.isActive() || !(b instanceof GameObject goB)) continue;

                // Determine physics types
                boolean aDynamic = goA.getPhysicsType() == PhysicsType.DYNAMIC;
                boolean bDynamic = goB.getPhysicsType() == PhysicsType.DYNAMIC;
                boolean aKinematic = goA.getPhysicsType() == PhysicsType.KINEMATIC;
                boolean bKinematic = goB.getPhysicsType() == PhysicsType.KINEMATIC;

                // At least one entity must be DYNAMIC for a collision response
                // Also allow DYNAMIC ↔ KINEMATIC interactions
                if (!aDynamic && !bDynamic) continue;

                // Skip pairs involving NONE physics type
                if (goA.getPhysicsType() == PhysicsType.NONE
                        || goB.getPhysicsType() == PhysicsType.NONE) continue;

                // --- Broad phase: AABB intersection ---
                if (!goA.getBounds().intersects(goB.getBounds())) continue;

                // --- Narrow phase: compute overlap on each axis ---
                float dx = goA.getCenterX() - goB.getCenterX();
                float dy = goA.getCenterY() - goB.getCenterY();

                float halfWidthSum = (goA.getWidth() + goB.getWidth()) / 2.0f;
                float halfHeightSum = (goA.getHeight() + goB.getHeight()) / 2.0f;

                float overlapX = halfWidthSum - Math.abs(dx);
                float overlapY = halfHeightSum - Math.abs(dy);

                if (overlapX <= 0 || overlapY <= 0) continue;

                // Mark contact
                goA.setContact(true);
                goB.setContact(true);
                collisionCount++;

                // Determine the collision normal (axis of minimum penetration)
                float nx, ny;
                float penetration;
                if (overlapX < overlapY) {
                    nx = dx > 0 ? 1.0f : -1.0f;
                    ny = 0;
                    penetration = overlapX;
                } else {
                    nx = 0;
                    ny = dy > 0 ? 1.0f : -1.0f;
                    penetration = overlapY;
                }

                // --- Delegate response to CollisionBehavior instances ---
                // Event for entity A: normal points away from B toward A
                CollisionEvent eventA = new CollisionEvent(goB, nx, ny, penetration, aDynamic, bDynamic, bKinematic);
                dispatchCollision(goA, eventA, deltaTime);

                // Event for entity B: inverted normal (points away from A toward B)
                CollisionEvent eventB = new CollisionEvent(goA, -nx, -ny, penetration, bDynamic, aDynamic, aKinematic);
                dispatchCollision(goB, eventB, deltaTime);
            }
        }
        stats.put("collisions", collisionCount);
    }

    /**
     * Filters the behaviors of {@code entity} to find {@link CollisionBehavior}
     * instances and invokes {@link CollisionBehavior#onCollision} on each of them.
     *
     * @param entity    The entity whose collision behaviors should be triggered.
     * @param event     The collision event describing the collision context.
     * @param deltaTime The elapsed time since the last frame (in seconds).
     */
    private void dispatchCollision(GameObject entity, CollisionEvent event, float deltaTime) {
        entity.getBehaviors().stream()
                .filter(CollisionBehavior.class::isInstance)
                .map(CollisionBehavior.class::cast)
                .forEach(cb -> cb.onCollision(entity, event, deltaTime));
    }

    /**
     * Disposes of the CollisionManager, releasing any resources.
     */
    @Override
    public void dispose() {
        log(CollisionManager.class, App.LogLevel.INFO, "CollisionManager disposed.");
    }
}