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

                // At least one entity must be DYNAMIC for a collision response
                boolean aDynamic = goA.getPhysicsType() == PhysicsType.DYNAMIC;
                boolean bDynamic = goB.getPhysicsType() == PhysicsType.DYNAMIC;
                if (!aDynamic && !bDynamic) continue;

                // Skip pairs involving NONE physics type
                if (goA.getPhysicsType() == PhysicsType.NONE
                        || goB.getPhysicsType() == PhysicsType.NONE) continue;

                // --- Broad phase: AABB intersection (enclosing rotated OBBs) ---
                if (!goA.getBounds().intersects(goB.getBounds())) continue;

                // --- Narrow phase: SAT on Oriented Bounding Boxes ---
                float[] cornersA = goA.getRotatedCorners();
                float[] cornersB = goB.getRotatedCorners();
                float[] satResult = satCollision(cornersA, cornersB);
                if (satResult == null) continue;

                // Mark contact
                goA.setContact(true);
                goB.setContact(true);
                collisionCount++;

                float nx = satResult[0];
                float ny = satResult[1];
                float penetration = satResult[2];

                // Ensure normal points from B toward A
                float dx = goA.getCenterX() - goB.getCenterX();
                float dy = goA.getCenterY() - goB.getCenterY();
                if (dx * nx + dy * ny < 0) {
                    nx = -nx;
                    ny = -ny;
                }

                // --- Delegate response to CollisionBehavior instances ---
                // Event for entity A: normal points away from B toward A
                CollisionEvent eventA = new CollisionEvent(goB, nx, ny, penetration, aDynamic, bDynamic);
                dispatchCollision(goA, eventA);

                // Event for entity B: inverted normal (points away from A toward B)
                CollisionEvent eventB = new CollisionEvent(goA, -nx, -ny, penetration, bDynamic, aDynamic);
                dispatchCollision(goB, eventB);
            }
        }
        stats.put("collisions", collisionCount);
    }

    /**
     * SAT (Separating Axis Theorem) collision test between two Oriented
     * Bounding Boxes represented by their rotated corner arrays.
     * Each corner array is {@code [x0,y0, x1,y1, x2,y2, x3,y3]}.
     *
     * @param cA corners of OBB A.
     * @param cB corners of OBB B.
     * @return {@code float[]{nx, ny, penetration}} (Minimum Translation
     *         Vector), or {@code null} if no collision.
     */
    private float[] satCollision(float[] cA, float[] cB) {
        float minPen = Float.MAX_VALUE;
        float bestNx = 0, bestNy = 0;

        // Test 4 axes: 2 unique edge-normals per OBB (edges 0→1 and 1→2)
        for (int shape = 0; shape < 2; shape++) {
            float[] c = (shape == 0) ? cA : cB;
            for (int edge = 0; edge < 2; edge++) {
                int i = edge;
                int j = edge + 1;
                float ex = c[j * 2] - c[i * 2];
                float ey = c[j * 2 + 1] - c[i * 2 + 1];

                // Outward normal (perpendicular to edge)
                float ax = -ey;
                float ay = ex;
                float len = (float) Math.sqrt(ax * ax + ay * ay);
                if (len < 1e-8f) continue;
                ax /= len;
                ay /= len;

                // Project all corners of A onto this axis
                float minA = Float.MAX_VALUE, maxA = -Float.MAX_VALUE;
                for (int k = 0; k < 4; k++) {
                    float p = cA[k * 2] * ax + cA[k * 2 + 1] * ay;
                    if (p < minA) minA = p;
                    if (p > maxA) maxA = p;
                }

                // Project all corners of B onto this axis
                float minB = Float.MAX_VALUE, maxB = -Float.MAX_VALUE;
                for (int k = 0; k < 4; k++) {
                    float p = cB[k * 2] * ax + cB[k * 2 + 1] * ay;
                    if (p < minB) minB = p;
                    if (p > maxB) maxB = p;
                }

                // Check overlap on this axis
                float overlap = Math.min(maxA - minB, maxB - minA);
                if (overlap <= 0) return null; // Separating axis found

                if (overlap < minPen) {
                    minPen = overlap;
                    bestNx = ax;
                    bestNy = ay;
                }
            }
        }

        return new float[] { bestNx, bestNy, minPen };
    }

    /**
     * Filters the behaviors of {@code entity} to find {@link CollisionBehavior}
     * instances and invokes {@link CollisionBehavior#onCollision} on each of them.
     *
     * @param entity The entity whose collision behaviors should be triggered.
     * @param event  The collision event describing the collision context.
     */
    private void dispatchCollision(GameObject entity, CollisionEvent event) {
        entity.getBehaviors().stream()
                .filter(CollisionBehavior.class::isInstance)
                .map(CollisionBehavior.class::cast)
                .forEach(cb -> cb.onCollision(entity, event));
    }

    /**
     * Disposes of the CollisionManager, releasing any resources.
     */
    @Override
    public void dispose() {
        log(CollisionManager.class, App.LogLevel.INFO, "CollisionManager disposed.");
    }
}