package core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import core.App;
import core.physics.PhysicsEngine;
import core.scene.Scene;

public class Service {
    private static List<Service> services = new ArrayList<>();
    public App app;
    public boolean active = false;

    public Service(App app) {
        this.app = app;
        register();
    }

    public void register() {
        services.add(this);
    }

    public void initialize(Properties config) {
    }

    public void start() {
        this.active = true;
        App.log(this.getClass(), App.LogLevel.INFO, this.getClass().getSimpleName() + " started.");
    }

    public void stop() {
        this.active = false;
        App.log(this.getClass(), App.LogLevel.INFO, this.getClass().getSimpleName() + " stopped.");
    }

    public void update(Scene scene, float deltaTime, Map<String, Object> stats) {
    }

    public void dispose() {

    }

    public static void initializeAll(Properties config) {
        for (var service : services) {
            service.initialize(config);
        }
    }

    public static void startAll() {
        for (var service : services) {
            service.start();
        }
    }

    public static void stopAll() {
        for (var service : services) {
            service.stop();
        }
    }

    public static void disposeAll() {
        for (var service : services) {
            service.dispose();
        }
    }

    public static List<Service> getServices() {
        return services;
    }

    public static <T extends Service> T get(Class<T> serviceClass) {
        return (T) services.stream().filter(s -> serviceClass.isInstance(s)).findFirst().get();
    }
}
