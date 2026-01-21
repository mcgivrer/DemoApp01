package core;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import core.graphics.Renderer;
import core.physics.PhysicsEngine;
import core.scene.Scene;
import core.utils.InputHandler;
import core.utils.Service;
import demo.scenes.DemoScene;

public class App implements Runnable {
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR, FATAL;
    }

    public enum AppMode {
        DEVELOPMENT, TESTING, PRODUCTION;
    }

    public static ResourceBundle messages = ResourceBundle.getBundle("i18n/messages");
    private Properties config = new Properties();

    public static int debug = 0;
    public static AppMode mode = AppMode.DEVELOPMENT;
    private static int timeout = 1000; // in milliseconds

    Thread appThread;
    public static boolean exit = false;

    private PhysicsEngine physicsEngine;
    private Renderer renderer;
    private InputHandler inputHandler;

    public App() {
        log(getClass(), LogLevel.INFO, "Start App class...");
    }

    public void run(String[] args) {
        initialize(args);

        inputHandler = new InputHandler();
        physicsEngine = new PhysicsEngine(this);
        renderer = new Renderer(this, inputHandler);

        // initialize services
        Service.initializeAll(config);
        Service.startAll();

        // initialize scenes
        Scene.initialize(config);

        // start application thread
        appThread = new Thread(this);
        appThread.start();
    }

    private void initialize(String[] args) {
        // set default values
        log(getClass(), LogLevel.INFO, "Set default configuration");
        config.setProperty("debug", "0");
        config.setProperty("mode", "DEVELOPMENT");
        config.setProperty("timeout", "1000");
        parseConfiguration();
        log(App.class, LogLevel.INFO, "  -> default configuration initialized");
        // parse configuration file
        try {
            log(getClass(), LogLevel.INFO, "Load configuration from file");
            config.load(App.class.getResourceAsStream("/config.properties"));
            log(App.class, LogLevel.INFO, "  loaded config.properties");
            parseConfiguration();
            log(App.class, LogLevel.INFO, "  -> configuration loaded");
        } catch (IOException e) {
            log(App.class, LogLevel.ERROR, "  cannot load config.properties: %s", e.getMessage());
        }
        // parse command line arguments
        parseCliArgs(args);
        parseConfiguration();
        log(App.class, LogLevel.INFO, "  -> configuration from args parsed");
    }

    @Override
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
            startTime = endTime;
            for (int i = 0; i < 5; i++) {
                update(elapsed / (5 * 1_000_000_000), stats);
            }
            draw(elapsed / 1_000_000_000, stats);
            frameCount++;
            timeFrame += elapsed;
            internalTime += elapsed;
            stats.put("time", internalTime / 1_000_000);
            if (timeFrame >= 1_000_000_000) {
                stats.put("fps", frameCount);
                frameCount = 0;
                timeFrame = 0;
            }
            try {
                Thread.sleep((int) (((FPS / 1_000_000f) - (elapsed) > 0) ? (FPS / 1_000_000f) - (elapsed) : 1f));
            } catch (InterruptedException e) {
                log(App.class, LogLevel.ERROR, "  application loop interrupted: %s", e.getMessage());
            }
            endTime = System.nanoTime();
            elapsed = endTime - startTime;
        } while (!exit);
    }

    private void update(long elapsed, Map<String, Object> stats) {
        if (Scene.currentScene != null) {
            physicsEngine.update(Scene.currentScene, elapsed, stats);
        }

    }

    private void draw(long elapsed, Map<String, Object> stats) {
        renderer.update(Scene.getActiveScene(), elapsed, stats);

    }

    private void parseConfiguration() {
        for (String key : config.stringPropertyNames()) {
            parseConfig(key, config.getProperty(key));
        }
    }

    private void parseCliArgs(String[] args) {
        if (args.length > 0) {
            log(getClass(), LogLevel.INFO, "parse args:");
            int i = 0;
            for (String arg : args) {
                log(getClass(), LogLevel.INFO, "- arg[%d]: %s", i++, arg);
                parseArg(arg);
            }
        } else {
            log(getClass(), LogLevel.INFO, "- no argument...");
        }
    }

    private void parseArg(String arg) {
        if (arg.contains("=")) {
            String[] key = arg.split("=", 2);
            config.setProperty(key[0], key[1]);
        } else {
            config.setProperty(arg, "true");
        }
    }

    private void parseConfig(String key, String value) {
        // first is config key.value, then last possible cli key args.
        switch (key.toLowerCase().trim()) {
        // set debug level from config file or CLI key-value pair
        case "debug" -> {
            debug = Integer.parseInt(value);
            log(getClass(), LogLevel.INFO, "  set debug level to %d", debug);
        }
        case "mode" -> {
            mode = AppMode.valueOf(value.toUpperCase());
            log(getClass(), LogLevel.INFO, "  set app mode to %s", value);
        }
        case "timeout" -> {
            timeout = Integer.parseInt(value);
            log(getClass(), LogLevel.INFO, "  set app timeout to %d ms", timeout);
        }

        case "h", "-h", "help", "-help" -> {
            log(getClass(), LogLevel.INFO, "  help requested, exiting...");
            System.out.println("Usage: java -jar app.jar [key=value]...\n" + "Available options:\n"
                    + "  debug=<level>       Set debug level (0=none, 1=some, 2=verbose)\n"
                    + "  mode=<mode>         Set application mode (DEVELOPMENT, TESTING, PRODUCTION)\n"
                    + "  timeout=<ms>        Set application timeout in milliseconds\n"
                    + "  winsize=<WxH>       Set window size (e.g., 800x600)\n"
                    + "  help                Show this help message");
            System.exit(0);
        }
        default -> {
            log(getClass(), LogLevel.WARN, "  unknown argument: %s=%s", key, value);
        }
        }
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

}
