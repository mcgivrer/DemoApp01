package core;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import core.graphics.Renderer;
import core.physics.CollisionManager;
import core.physics.PhysicsEngine;
import core.scene.Scene;
import core.utils.Configuration;
import core.utils.InputHandler;
import core.utils.Service;

/**
 * The main application class that initializes and runs the core components of
 * the application, including configuration parsing, service management, scene
 * handling, and the main application loop.
 * 
 * @see Service
 * @see Scene
 * @see Renderer
 * @see PhysicsEngine
 * @see InputHandler
 * 
 * @author Frédéric Delorme<frederic.delorme@gmail.com>
 * @version 0.0.1
 * @since 2026
 */
public class App {
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR, FATAL;
    }

    public enum AppMode {
        DEVELOPMENT, TESTING, PRODUCTION;
    }

    public static ResourceBundle messages = ResourceBundle.getBundle("i18n/messages");

    public static int debug = 0;
    public static AppMode mode = AppMode.DEVELOPMENT;

    public static boolean exit = false;
    public static boolean pause = false;

    public Configuration config = null;
    private PhysicsEngine physicsEngine;
    private Renderer renderer;
    public InputHandler inputHandler;

    private CollisionManager collisionManager;

    public App() {
        log(getClass(), LogLevel.INFO, "Start App class...");
    }

    public void run(String[] args) {
        initialize(args);

        inputHandler = new InputHandler();
        physicsEngine = new PhysicsEngine(this);
        collisionManager = new CollisionManager(this);
        renderer = new Renderer(this, inputHandler);

        // initialize services
        Service.initializeAll(config);
        Service.startAll();

        // initialize scenes
        Scene.initialize(config);
        run();
    }

    private void initialize(String[] args) {
        log(getClass(), LogLevel.INFO, "Initialize App class...");
        config = new Configuration(args);

        log(App.class, LogLevel.INFO, "  -> configuration from args parsed");
    }

    public void run() {
        String defaultSceneName = config.getProperty("defaultscene", "demo");
        Scene.activate(this, defaultSceneName);

        loop();
        dispose();
        log(getClass(), LogLevel.INFO, "End App class.");
        System.exit(0);
    }

    private void loop() {
        long startTime = System.nanoTime();
        long endTime = startTime;
        long elapsed = 0;
        long timeFrame = 0, frameCount = 0, internalTime = 0;
        int FPS = 60;
        Map<String, Object> stats = new HashMap<>();
        stats.put("fps", FPS);
        do {
            // Main application loop logic goes here
            if (!pause) {
                startTime = endTime;
                for (int i = 0; i < 5; i++) {
                    update(elapsed / (5 * 1_000_000_000f), stats);
                }
                draw(elapsed / 1_000_000_000, stats);
            }
            frameCount++;
            timeFrame += elapsed;
            internalTime += elapsed;
            stats.put("time", internalTime / 1_000_000_000f);
            if (timeFrame >= 1_000_000_000f) {
                stats.put("fps", frameCount);
                frameCount = 0;
                timeFrame = 0;
            }
            try {
                Thread.sleep((int) (((FPS / 1_000_000_000) - (elapsed) > 0) ? (FPS / 1_000_000_000) - (elapsed) : 1f));
            } catch (InterruptedException e) {
                log(App.class, LogLevel.ERROR, "  application loop interrupted: %s", e.getMessage());
            }
            endTime = System.nanoTime();
            elapsed = endTime - startTime;
        } while (!exit);
    }

    private void update(float elapsed, Map<String, Object> stats) {
        if (Scene.currentScene != null) {
            physicsEngine.update(Scene.currentScene, elapsed, stats);
            collisionManager.update(Scene.currentScene, elapsed, stats);
        }

    }

    private void draw(float elapsed, Map<String, Object> stats) {
        renderer.update(Scene.getActiveScene(), elapsed, stats);

    }

    public static void main(String[] args) {
        App app = new App();
        app.run(args);
    }

    public static void log(Class<?> cls, LogLevel level, String message, Object... args) {
        System.out.printf("%s;%s;[%s];%s%n", ZonedDateTime.now(), cls.getCanonicalName(), level.name(),
                message.formatted(args));
    }

    private void dispose() {
        Service.stopAll();
        Service.disposeAll();
    }

    public static void requestExit() {
        log(App.class, LogLevel.INFO, "Request to exit");
        exit = true;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

}
