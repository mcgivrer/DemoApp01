package core.physics;

import static core.App.log;

import java.util.List;
import java.util.Map;

import core.App;
import core.entity.Entity;
import core.entity.GameObject;
import core.entity.PhysicsType;
import core.scene.Scene;
import core.utils.Configuration;
import core.utils.Service;

/**
 * Manages collision detection and resolution between {@link GameObject} entities
 * in a {@link Scene}. Uses AABB (Axis-Aligned Bounding Box) intersection tests
 * and resolves collisions along the axis of minimum overlap (SAT-lite).
 * <p>
 * Collision response respects {@link PhysicsType}: STATIC entities are never moved,
 * DYNAMIC entities receive positional correction and velocity adjustment using
 * the {@link Material} restitution and friction coefficients.
 *
 * @see GameObject
 * @see PhysicsType
 * @see Material
 * @see Scene
 *
 * @author Frédéric Delorme
 * @since 2026
 * @version 0.0.2
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
     * Detects and resolves collisions for all active {@link GameObject} pairs in
     * the scene. Each unique pair is processed exactly once to avoid duplicate
     * resolution.
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

                // Skip pairs where both are NONE
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

                // --- Positional correction ---
                // Distribute correction based on physics types
                if (aDynamic && bDynamic) {
                    float half = penetration / 2.0f;
                    goA.setPosition(goA.x + nx * half, goA.y + ny * half);
                    goB.setPosition(goB.x - nx * half, goB.y - ny * half);
                } else if (aDynamic) {
                    // Only A moves (B is STATIC)
                    goA.setPosition(goA.x + nx * penetration, goA.y + ny * penetration);
                } else {
                    // Only B moves (A is STATIC)
                    goB.setPosition(goB.x - nx * penetration, goB.y - ny * penetration);
                }

                // --- Velocity response using Material properties ---
                float restitution = Math.min(
                        goA.getMaterial().restitution(),
                        goB.getMaterial().restitution());
                float friction = (goA.getMaterial().friction() + goB.getMaterial().friction()) / 2.0f;

                // Relative velocity along collision normal
                float relVn = (goA.vx - goB.vx) * nx + (goA.vy - goB.vy) * ny;

                // Only resolve if entities are approaching each other
                if (relVn < 0) continue;

                float impulse = -(1 + restitution) * relVn;

                // Distribute impulse based on physics types
                if (aDynamic && bDynamic) {
                    float halfImpulse = impulse / 2.0f;
                    goA.vx += halfImpulse * nx;
                    goA.vy += halfImpulse * ny;
                    goB.vx -= halfImpulse * nx;
                    goB.vy -= halfImpulse * ny;
                } else if (aDynamic) {
                    goA.vx += impulse * nx;
                    goA.vy += impulse * ny;
                } else {
                    goB.vx -= impulse * nx;
                    goB.vy -= impulse * ny;
                }

                // Apply friction to tangential velocity
                float tx = -ny;
                float ty = nx;
                if (aDynamic) {
                    float tangentVelA = goA.vx * tx + goA.vy * ty;
                    goA.vx -= friction * tangentVelA * tx;
                    goA.vy -= friction * tangentVelA * ty;
                }
                if (bDynamic) {
                    float tangentVelB = goB.vx * tx + goB.vy * ty;
                    goB.vx -= friction * tangentVelB * tx;
                    goB.vy -= friction * tangentVelB * ty;
                }
            }
        }
        stats.put("collisions", collisionCount);
    }

    /**
     * Disposes of the CollisionManager, releasing any resources.
     */
    @Override
    public void dispose() {
        log(CollisionManager.class, App.LogLevel.INFO, "CollisionManager disposed.");
    }
}