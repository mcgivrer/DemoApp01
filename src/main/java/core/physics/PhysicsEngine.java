package core.physics;

import static core.App.log;

import java.util.Map;
import java.util.Properties;

import core.App;
import core.entity.Entity;
import core.entity.GameObject;
import core.entity.PhysicType;
import core.scene.Scene;

/**
 * Physics engine responsible for updating the physics state of entities within
 * a scene. It processes entity behaviors and updates their positions based on
 * velocity.
 * 
 * @see Entity
 * @see Scene
 * @see core.utils.Service
 * 
 * @author Frédéric Delorme<frederic.delorme@gmail.com>
 * @since 2026
 * @version 0.0.1
 */
public class PhysicsEngine extends core.utils.Service {

    /**
     * Constructor of the PhysicsEngine.
     * 
     * @param app The main application instance.
     */
    public PhysicsEngine(App app) {
        super(app);
    }

    /**
     * Updates the physics state of the given scene. It applies behaviors to the
     * scene itself and to each active entity within the scene.
     * 
     * @param scene     The scene to update.
     * @param deltaTime The time elapsed since the last update (in seconds).
     * @param stats     A map to collect statistics during the update.
     * @see Scene
     * @see Entity
     * @see core.behavior.Behavior
     * 
     */
    @Override
    public void update(Scene scene, float deltaTime, Map<String, Object> stats) {
        // Apply Scene behaviors
        for (var behavior : scene.getBehaviors()) {
            behavior.update((Entity<?>) scene, deltaTime);
        }
        // update scene entities.
        scene.getEntities().stream()
            .filter(Entity::isActive)
            .forEach(entity -> {
                entity.getBehaviors()
                    .forEach(behavior -> behavior.update((Entity<?>) entity, deltaTime));
        });
    }

    /**
     * Initializes the PhysicsEngine with the given configuration properties.
     * 
     * @param config Configuration properties for initialization.
     */
    public void initialize(Properties config) {
        log(PhysicsEngine.class, App.LogLevel.INFO, "PhysicsEngine initialized.");
    }

    /**
     * Disposes of the PhysicsEngine, releasing any resources if necessary.
     */
    @Override
    public void dispose() {
        log(PhysicsEngine.class, App.LogLevel.INFO, "PhysicsEngine disposed.");

    }
}
