package core.physics;

import static core.App.log;

import java.util.Map;
import java.util.Properties;

import core.App;
import core.entity.Entity;
import core.scene.Scene;

public class PhysicsEngine extends core.utils.Service {

    public PhysicsEngine(App app) {
        super(app);
    }

    @Override
    public void update(Scene scene, float deltaTime, Map<String, Object> stats) {
        // Apply Scene behaviors
        for (var behavior : scene.getBehaviors()) {
            behavior.update((Entity<?>) scene, deltaTime);
        }
        // update scene entities.
        scene.getEntities().stream().filter(Entity::isActive).forEach(entity -> {
            for (var behavior : entity.getBehaviors()) {
                // apply Entity behaviors
                behavior.update((Entity<?>) entity, deltaTime);
            }
        });
    }

    public void initialize(Properties config) {
        log(PhysicsEngine.class, App.LogLevel.INFO, "PhysicsEngine initialized.");
    }

    @Override
    public void dispose() {
        log(PhysicsEngine.class, App.LogLevel.INFO, "PhysicsEngine disposed.");

    }
}
