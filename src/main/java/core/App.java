package core;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Properties;

public class App {
    public enum DebugLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL;
    }

    public enum AppMode {
        DEVELOPMENT,
        TESTING,
        PRODUCTION;

    }

    private Properties config = new Properties();
    public static int debug = 0;
    private static AppMode mode = AppMode.DEVELOPMENT;
    private static int timeout = 1000; // in milliseconds

    public App() {
        log(getClass(), DebugLevel.INFO, "Start App class...");
    }

    public void run(String[] args) {
        initialize(args);
        try {
            log(getClass(), DebugLevel.INFO, "Sleeping for %d ms...", timeout);
            Thread.sleep(timeout);
            log(getClass(), DebugLevel.INFO, "Execution completed after timeout.");
        } catch (InterruptedException e) {
            log(getClass(), DebugLevel.WARN, "Execution interrupted.");
        }
        log(getClass(), DebugLevel.INFO, "End App class.");
    }

    private void initialize(String[] args) {
        // set default values
        log(getClass(), DebugLevel.INFO, "Set default configuration");
        config.setProperty("debug", "0");
        config.setProperty("mode", "DEVELOPMENT");
        config.setProperty("timeout", "1000");
        parseConfiguration();
        log(App.class, DebugLevel.INFO, "  -> default configuration initialized");
        // parse configuration file
        try {
            log(getClass(), DebugLevel.INFO, "Load configuration from file");
            config.load(App.class.getResourceAsStream("/config.properties"));
            log(App.class, DebugLevel.INFO, "  loaded config.properties");
            parseConfiguration();
            log(App.class, DebugLevel.INFO, "  -> configuration loaded");
        } catch (IOException e) {
            log(App.class, DebugLevel.ERROR, "  cannot load config.properties: %s", e.getMessage());
        }
        // parse command line arguments
        parseCliArgs(args);
        parseConfiguration();
    }

    private void parseConfiguration() {
        for (String key : config.stringPropertyNames()) {
            parseConfig(key, config.getProperty(key));
        }
    }

    private void parseCliArgs(String[] args) {
        if (args.length > 0) {
            log(getClass(), DebugLevel.INFO, "parse args:");
            int i = 0;
            for (String arg : args) {
                log(getClass(), DebugLevel.INFO, "- arg[%d]: %s", i++, arg);
                parseArg(arg);
            }
        } else {
            log(getClass(), DebugLevel.INFO, "- no argument...");
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
                log(getClass(), DebugLevel.INFO, "  set debug level to %d", debug);
            }
            case "mode" -> {
                mode = AppMode.valueOf(value.toUpperCase());
                log(getClass(), DebugLevel.INFO, "  set app mode to %s", value);
            }
            case "timeout" -> {
                timeout = Integer.parseInt(value);
                log(getClass(), DebugLevel.INFO, "  set app timeout to %d ms", timeout);
            }
            case "h", "-h", "help", "-help" -> {
                log(getClass(), DebugLevel.INFO, "  help requested, exiting...");
                System.out.println("Usage: java -jar app.jar [key=value]...\n" +
                        "Available options:\n" +
                        "  debug=<level>       Set debug level (0=none, 1=some, 2=verbose)\n" +
                        "  mode=<mode>         Set application mode (DEVELOPMENT, TESTING, PRODUCTION)\n" +
                        "  timeout=<ms>        Set application timeout in milliseconds\n" +
                        "  help                Show this help message");
                System.exit(0);
            }
            default -> {
                log(getClass(), DebugLevel.WARN, "  unknown argument: %s=%s", key, value);
            }
        }
    }

    public static void main(String[] args) {
        App app = new App();
        app.run(args);
    }

    public static void log(Class<?> cls, DebugLevel level, String message, Object... args) {
        System.out.printf("%s;%s;[%s];%s%n",
                ZonedDateTime.now(),
                cls.getCanonicalName(),
                level.name(),
                message.formatted(args));
    }
}

