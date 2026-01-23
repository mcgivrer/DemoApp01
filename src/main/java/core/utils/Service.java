package core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import core.App;
import core.scene.Scene;

/**
 * Base class for services that provide various functionalities within the
 * application. Services can be registered, started, stopped, and updated as
 * needed. This class manages a static list of all registered services and
 * provides methods to control their lifecycle.
 * 
 * @see App
 * @see Scene
 * @author Frédéric Delorme<frederic.delorme@gmail.com>
 * @since 2026
 * @version 0.0.1
 */
public class Service {
    private static List<Service> services = new ArrayList<>();
    public App app;
    public boolean active = false;

    public Service(App app) {
        this.app = app;
        register();
    }

    /**
     * Register this service in the global service list.
     */
    public void register() {
        services.add(this);
    }

    /**
     * Initialize the service with the given configuration.
     * 
     * @param config Configuration properties for the service.
     */
    public void initialize(Configuration config) {
    }

    /**
     * Start the service.
     */
    public void start() {
        this.active = true;
        App.log(this.getClass(), App.LogLevel.INFO, this.getClass().getSimpleName() + " started.");
    }

    /**
     * Stop the service.
     */
    public void stop() {
        this.active = false;
        App.log(this.getClass(), App.LogLevel.INFO, this.getClass().getSimpleName() + " stopped.");
    }

    /**
     * Update the service. This method is called periodically with the current
     * scene, elapsed time, and statistics.
     * 
     * @param scene     the current scene
     * @param deltaTime the elapsed time since the last update
     * @param stats     a map to collect statistics
     */
    public void update(Scene scene, float deltaTime, Map<String, Object> stats) {
    }

    /**
     * Dispose of the service and release any resources.
     */
    public void dispose() {

    }

    /**
     * Initialize all registered services with the given configuration.
     * 
     * @param config Configuration properties for the services.
     */
    public static void initializeAll(Configuration config) {
        for (var service : services) {
            service.initialize(config);
        }
    }

    /**
     * Start all registered services.
     */
    public static void startAll() {
        for (var service : services) {
            service.start();
        }
    }

    /**
     * Stop all registered services.
     */
    public static void stopAll() {
        for (var service : services) {
            service.stop();
        }
    }

    /**
     * Dispose of all registered services.
     */
    public static void disposeAll() {
        for (var service : services) {
            service.dispose();
        }
    }

    public static List<Service> getServices() {
        return services;
    }

    /**
     * Get a service by its class type.
     * 
     * @param <T>          The type of the service.
     * @param serviceClass The class of the service to retrieve.
     * @return The service instance of the specified class.
     */
    public static <T extends Service> T get(Class<T> serviceClass) {
        return (T) services.stream().filter(s -> serviceClass.isInstance(s)).findFirst().get();
    }
}
