package core.utils;

import static core.App.log;

import java.io.IOException;
import java.util.Properties;

import core.App;
import core.App.AppMode;
import core.App.LogLevel;

public class Configuration {

    private Properties config = new Properties();

    public Configuration(String[] args) {
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
            log(getClass(), LogLevel.INFO, "  loaded config.properties");
            parseConfiguration();
            log(App.class, LogLevel.INFO, "  -> configuration loaded");
        } catch (IOException e) {
            log(App.class, LogLevel.ERROR, "  cannot load config.properties: %s", e.getMessage());
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
            App.debug = Integer.parseInt(value);
            log(getClass(), LogLevel.INFO, "  set debug level to %d", App.debug);
        }
        case "mode" -> {
            App.mode = AppMode.valueOf(value.toUpperCase());
            log(getClass(), LogLevel.INFO, "  set app mode to %s", App.mode);
        }
        case "h", "-h", "help", "-help" -> {
            log(getClass(), LogLevel.INFO, "  help requested, exiting...");
            System.out.println("Usage: java -jar app.jar [key=value]...\n" + "Available options:\n"
                    + "  debug=<level>       Set debug level (0=none, 1=some, 2=verbose)\n"
                    + "  mode=<mode>         Set application mode (DEVELOPMENT, TESTING, PRODUCTION)\n"
                    + "  winsize=<WxH>       Set window size (e.g., 800x600)\n"
                    + "  help                Show this help message");
            System.exit(0);
        }
        default -> {
            log(getClass(), LogLevel.WARN, "  unknown argument: %s=%s", key, value);
        }
        }
    }

    /**
     * Get property value by key
     * 
     * @param key          the property key
     * @param defaultValue the default value to return if the key is not found
     * @return the property value associated with the key, or the default value if
     *         the key is not found
     */
    public String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    /**
     * Get property value by key
     * @param key the property key
     * @return the property value associated with the key, or null if the key is not found
     */
    public boolean containsKey(String key) {
       return config.containsKey(key);
    }
}
