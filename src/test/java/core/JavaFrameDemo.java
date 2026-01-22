package core;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.Thread;
import java.time.ZonedDateTime;
import java.lang.Exception;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * JavaFrameDemo serves as a basic demonstration of how to build a graphical
 * application using Java AWT and manage a custom rendering loop with double
 * buffering. This application displays the elapsed time in a `Frame` window
 * with support for basic keyboard interaction to terminate the program.
 * <p>
 * The class also provides mechanisms to manage window events, custom rendering
 * to a buffered image, and drawing text with anti-aliasing enabled.
 * <p>
 * Implements the `KeyListener` interface to handle keyboard events for user
 * interaction.
 */
public class JavaFrameDemo implements KeyListener {
    private static int debug = 0;

    /**
     * Represents a generic behavior that can be applied to an entity in the
     * application. This interface provides a mechanism for defining and updating
     * the state and appearance of an entity during its lifecycle, such as handling
     * interactions, animations, or custom behaviors. Both the update and draw
     * methods can be overridden to define specific behavior logic.
     *
     * @param <Entity> the type of entity to which this behavior is applied
     */
    public interface Behavior<Entity> {
        default void init(JavaFrameDemo app, Entity e) {
        }

        default void update(JavaFrameDemo app, double elapsed, Entity e) {
        }

        default void draw(JavaFrameDemo app, Graphics2D g, Entity e) {
        }

        default void destroy(JavaFrameDemo app, Entity e) {
        }
    }

    public enum PhysicType {
        DYNAMIC, STATIC, NONE;
    }

    /**
     * Represents a movable and drawable entity in a graphical application. This
     * class extends Rectangle2D.Double, providing functionality to define the
     * entity's position, size, rotation, colors, and image. It also includes
     * methods for updating the entity's state and rendering it.
     */
    public static class Entity extends Rectangle2D.Double {
        private static long index = 0;
        private long id = index++;
        public String name = "entity_%d".formatted(id);

        public List<Behavior> behaviors = new ArrayList<>();

        public double dx, dy;
        public double r, dr;

        public boolean active = true;

        public boolean jump;
        public boolean grounded;

        public Color color = Color.BLACK, fillColor = Color.RED;
        public BufferedImage image;

        private Entity parent;
        private List<Entity> children = new ArrayList<>();

        private int contact = 0;

        public PhysicType physicType = PhysicType.DYNAMIC;
        private List<Vector2d> forces = new ArrayList<>();
        public Vector2d position = new Vector2d();
        public Vector2d velocity = new Vector2d();
        public Vector2d acceleration = new Vector2d();
        private Material material = Material.DEFAULT;

        public Entity(double x, double y, double width, double height) {
            super(x, y, width, height);
        }

        public Entity(String name) {
            this(0, 0, 0, 0);
            this.name = name;
        }

        public Entity add(Entity child) {
            child.parent = this;
            children.add(child);
            return this;
        }

        public Entity apply(Vector2d f) {
            forces.add(f);
            return this;
        }

        public Entity add(Behavior<Entity> b) {
            behaviors.add(b);
            return this;
        }

        public Entity setPosition(double x, double y) {
            this.x = x;
            this.y = y;
            this.position = new Vector2d(x, y);
            return this;
        }

        public Entity setVelocity(double dx, double dy) {
            this.dx = dx;
            this.dy = dy;
            this.velocity = new Vector2d(dx, dy);
            return this;
        }

        public Entity setAcceleration(double ax, double ay) {
            this.acceleration = new Vector2d(ax, ay);
            return this;
        }

        public Entity setSize(double width, double height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Entity setColor(Color color) {
            this.color = color;
            return this;
        }

        public Entity setFillColor(Color color) {
            this.fillColor = color;
            return this;
        }

        public Entity setActive(boolean active) {
            this.active = active;
            return this;
        }

        public Entity setMaterial(Material material) {
            this.material = material;
            return this;
        }

        public Entity setPhysicType(PhysicType physicType) {
            this.physicType = physicType;
            return this;
        }

        /**
         * Draws the current entity onto the provided graphics context. This method
         * renders the entity with its current position, rotation, colors, and image. If
         * an image is associated with the entity, it will be drawn at the specified
         * position. Otherwise, the entity will be rendered as a filled and outlined
         * shape.
         *
         * @param g the graphics context on which the entity will be drawn
         */
        public void draw(Graphics2D g) {
            g.rotate(r, x, y);
            if (image != null) {
                g.drawImage(image, (int) x, (int) y, null);
            } else {
                if (this.fillColor != null) {
                    g.setColor(this.fillColor);
                    g.fill(this);
                }
                if (this.color != null) {
                    g.setColor(this.color);
                    g.draw(this);
                }
            }
        }

        public String[] getDebugInfo() {
            return new String[] { "id: " + id, "name: " + name, "s: %3.2fx%3.2f".formatted(width, height),
                    "p: %3.2f,%3.2f".formatted(x, y), "r: %3.2f".formatted(r), "v: %3.2f,%3.2f".formatted(dx, dy),
                    "dr: %3.2f".formatted(r), };
        }

        public void drawDebug(Graphics2D g) {
            g.setColor(Color.ORANGE);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 8.5f));
            if (JavaFrameDemo.debug > 0) {
                int ix = 0;
                for (String s : getDebugInfo()) {
                    if (JavaFrameDemo.debug > ix) {
                        g.drawString(s, (int) (x + width + 4),
                                (int) (y + height - (g.getFontMetrics().getHeight() * ix++)));
                    }
                }
                g.setColor(Color.YELLOW);
                // draw the resulting velocity
                g.drawLine((int) (x + width / 2), (int) (y + height / 2), (int) ((x + width / 2) + dx * 50.0),
                        (int) ((y + height / 2) + dy * 50.0));
                g.setColor(Color.GREEN);
                // draw all applied forces
                getForces().forEach(f -> {
                    g.drawLine((int) (x + width / 2), (int) (y + height / 2), (int) ((x + width / 2) + f.getX() * 50.0),
                            (int) ((y + height / 2) + f.getY() * 50.0));
                });
            }
        }

        public void update(double elapsed) {

        }

        public Vector2d getPosition() {
            return position;
        }

        public Vector2d getVelocity() {
            return velocity;
        }

        public Vector2d getAcceleration() {
            return acceleration;
        }

        public boolean isActive() {
            return active;
        }

        public Material getMaterial() {
            return material;
        }

        public int getContact() {
            return contact;
        }

        public void addContact(int i) {
            contact += i;
        }

        public void resetContact() {
            contact = 0;
        }

        public void resetForces() {
            forces.clear();
        }

        public PhysicType getPhysicType() {
            return physicType;
        }

        public Collection<Vector2d> getForces() {
            return forces;
        }
    }

    /**
     * Represents a physical world or environment within which entities exist and
     * interact. This class extends the {@link Entity} class, inheriting its
     * properties while introducing specific functionality such as defining gravity
     * in the environment.
     */
    public static class World extends Entity {
        private Vector2d gravity = new Vector2d(0, 0.981);

        public World(double x, double y, double width, double height) {
            super(x, y, width, height);
        }

        public World(String name) {
            super(name);
            setMaterial(Material.DEFAULT);
        }

        public World setGravity(Vector2d gravity) {
            this.gravity = gravity;
            return this;
        }

        @Override
        public void draw(Graphics2D g) {
            g.setColor(fillColor);
            for (double ix = x; ix < x + width; ix += 32) {
                for (double iy = y; iy < y + height; iy += 32) {
                    g.setColor(fillColor.darker());
                    g.fill(new Ellipse2D.Double(ix + 4, iy + 4, 24, 24));
                    g.setColor(fillColor);
                    g.draw(new Ellipse2D.Double(ix, iy, 32, 32));
                }
            }
            g.setColor(color);
            g.draw(this);
        }
    }

    /**
     * Represents a camera entity that can follow a target entity with a smooth
     * transition. The camera adjusts its position based on the target's position,
     * ensuring a smooth movement by applying a tweening factor.
     */
    public static class Camera extends Entity {
        private Entity target;
        private double tweenFactor = 0.005;

        public Camera(String name) {
            super(name);
        }

        public Camera setTarget(Entity target) {
            this.target = target;
            return this;
        }

        public Camera setTweenFactor(double tweenFactor) {
            this.tweenFactor = tweenFactor;
            return this;
        }

        public void update(double elapsed) {
            if (target != null) {
                this.x += (target.x - this.x - (width + target.width) * 0.5) * tweenFactor * elapsed;
                this.y += (target.y - this.y - (height + target.height) * 0.75) * tweenFactor * elapsed;
            }
        }
    }

    /**
     * Represents a material with specific physical properties such as name,
     * elasticity, and friction. The material can be used to define the behavior of
     * objects in a simulation or application, such as handling collisions or
     * surface interactions.
     * <p>
     * This record includes predefined constants for commonly used materials: -
     * DEFAULT: A generic material with standard elasticity and friction values. -
     * WOOD: A material representing wood with higher elasticity and average
     * friction. - ROCK: A material representing rock with high elasticity and
     * average friction. - GLASS: A material representing glass with average
     * elasticity and high friction. - SAND: A material representing sand with
     * average values for both elasticity and friction. - STONE: A material with
     * properties similar to sand. - ICE: A material with very low elasticity and
     * very high friction, simulating slippery surfaces.
     *
     * @param name       the name of the material
     * @param elasticity the elasticity of the material, indicating how much it
     *                   rebounds or deforms upon impact
     * @param friction   the friction level of the material, representing the
     *                   resistance to sliding or movement along its surface
     */
    public record Material(String name, double elasticity, double friction) {
        public static final Material DEFAULT = new Material("default", 0.5, 0.5);
        public static final Material WOOD = new Material("wood", 0.8, 0.99);
        public static final Material ROCK = new Material("rock", 1.0, 0.5);
        public static final Material GLASS = new Material("glass", 0.5, 0.98);
        public static final Material SAND = new Material("sand", 0.5, 0.5);
        public static final Material STONE = new Material("stone", 0.5, 0.5);
        public static final Material ICE = new Material("ice", 0.2, 0.998);
        public static final Material AIR = new Material("air", 0.0, 0.9998);

    }

    /**
     * Represents a two-dimensional vector with basic vector operations. This class
     * extends Point2D.Double to provide additional functionality such as vector
     * addition, subtraction, scalar multiplication, normalization, distance
     * calculation, and other vector-based operations.
     */
    public static class Vector2d extends Point2D.Double {
        public Vector2d() {
            super();
        }

        public Vector2d(double x, double y) {
            super(x, y);
        }

        public Vector2d add(Vector2d b) {
            return new Vector2d(this.x + b.x, this.y + b.y);
        }

        public Vector2d mul(double m) {
            return new Vector2d(this.x * m, this.y * m);
        }

        public Vector2d sub(Vector2d b) {
            return new Vector2d(this.x - b.x, this.y - b.y);
        }

        public double distance(Vector2d v1) {
            return sub(v1).length();
        }

        public double length() {
            return Math.sqrt(x * x + y * y);
        }

        public double dot(Vector2d v1) {

            return v1.x * y + v1.y * x;
        }

        public Vector2d divide(double f) {
            return new Vector2d(x / f, y / f);
        }

        public Vector2d normalize() {
            return divide(length());
        }

        public Vector2d negate() {
            return new Vector2d(-x, -y);
        }

        public double angle(Vector2d v1) {
            double vDot = this.dot(v1) / (this.length() * v1.length());
            if (vDot < -1.0)
                vDot = -1.0;
            if (vDot > 1.0)
                vDot = 1.0;
            return Math.acos(vDot);

        }

        public Vector2d addAll(List<Vector2d> forces) {
            Vector2d sum = new Vector2d();
            for (Vector2d f : forces) {
                sum = sum.add(f);
            }
            return sum;
        }
    }

    private static final long FPS = 60;
    private static final ResourceBundle messages = ResourceBundle.getBundle("i18n.messages");
    private static final Properties config = new Properties();

    private Frame window;
    private boolean exit = false;
    private BufferedImage buffer;

    private World world;
    private final List<Entity> entities = new ArrayList<>();
    private Camera camera;
    private Entity player;

    private final boolean[] keys = new boolean[1024];

    /**
     * Initializes a new instance of the JavaFrameDemo class and starts the
     * application. This constructor serves as the entry point for initializing
     * basic setup and logging the application's start.
     */
    public JavaFrameDemo() {
        log(JavaFrameDemo.class, "INFO", "Start test Application...");
    }

    /**
     * Starts the main application loop, initializing configurations, creating the
     * window and buffer, and handling the main rendering and event loop. Releases
     * resources upon completion.
     *
     * @param args command-line arguments that can be used to override default
     *             configurations.
     */
    public void run(String[] args) {
        loadAndParseConfig("/config.properties", args);
        createWindow();
        createBuffer();
        initialize();
        loop();
        dispose();
    }

    private void initialize() {
        world = (World) new World("earth").setGravity(new Vector2d(0, 0.981)).setSize(32 * 30, 32 * 20)
                .setColor(Color.GRAY).setFillColor(Color.DARK_GRAY).setMaterial(Material.AIR);
        add(world);

        player = new Entity("player").setSize(24, 32).setPosition(world.getWidth() * 0.5, world.getHeight() * 0.75)
                .setMaterial(Material.WOOD).add(new Behavior<Entity>() {
                    @Override
                    public void update(JavaFrameDemo app, double elapsed, Entity e) {
                        double step = 10.0f;
                        if (isKeyPressed(KeyEvent.VK_LEFT)) {
                            e.apply(new Vector2d(-step, 0));
                        }
                        if (isKeyPressed(KeyEvent.VK_RIGHT)) {
                            e.apply(new Vector2d(step, 0));
                        }
                        if (isKeyPressed(KeyEvent.VK_UP) && !e.jump) {
                            e.apply(new Vector2d(0, -step * 150));
                            e.jump = true;
                            e.grounded = false;
                        }
                        if (isKeyPressed(KeyEvent.VK_DOWN)) {
                            e.apply(new Vector2d(0, step));
                        }
                    }
                });
        add(player);

        Random rand = new Random(12345);
        for (int i = 0; i < 20; i++) {

            Entity e = new Entity("ball" + i).setSize(16, 16)
                    .setPosition((rand.nextDouble() * world.width) - 16, (rand.nextDouble() * world.height) - 16)
                    .setFillColor(Color.BLUE).setColor(Color.BLUE.darker())
                    .setMaterial(new Material("bouncing", 0.9998, 1.0)).add(new Behavior<Entity>() {
                        @Override
                        public void update(JavaFrameDemo app, double elapsed, Entity e) {
                            if (player.x + (player.width - e.width / 2) > e.x)
                                e.dx = 100.0 * rand.nextDouble();
                            if (player.y + (player.height - e.height / 2) > e.y)
                                e.dy = 100.0 * rand.nextDouble();
                            if (player.x + (player.width - e.width / 2) < e.x)
                                e.dx = -100.0 * rand.nextDouble();
                            if (player.y + (player.height - e.height / 2) < e.y)
                                e.dy = -100.0 * rand.nextDouble();
                        }
                    });
            add(e);
        }

        camera = (Camera) new Camera("cam01").setTarget(player).setTweenFactor(10.0).setSize(buffer.getWidth(),
                buffer.getHeight());
    }

    private void add(Entity e) {
        entities.add(e);
    }

    /**
     * Loads a configuration file and updates its properties based on the provided
     * arguments. The method attempts to load a configuration file from the given
     * path and overrides specific properties using the provided key-value pairs in
     * the arguments array.
     *
     * @param s    the relative path to the configuration file to be loaded
     * @param args an array of strings representing key-value pairs (in the format
     *             key=value) to override default configurations from the file
     */
    private void loadAndParseConfig(String s, String[] args) {
        try {
            config.load(getClass().getResourceAsStream(s));
            if (args.length > 0) {
                for (String arg : args) {
                    if (arg.contains("=")) {
                        String[] keyValue = arg.split("=");
                        config.setProperty(keyValue[0], keyValue[1]);
                    }
                }
            }
            debug = Integer.parseInt((String) config.getOrDefault("app.debug", "0"));
        } catch (Exception e) {
            log(JavaFrameDemo.class, "ERROR", "Unable to load config file", e);
        }
    }

    /**
     * Creates and initializes the application's main window.
     * <p>
     * The method retrieves configuration settings for the window size and applies
     * them during the initialization. It sets up a listener for window closing
     * events, ensuring the application exits gracefully when the window is closed.
     * It also adds a key listener for handling keyboard input and creates a
     * triple-buffered drawing strategy for improved rendering performance.
     * <p>
     * Behavior: - Retrieves the window size from the provided configuration,
     * defaulting to 640x480 if not specified. - Initializes the application window
     * with the specified size and title. - Assigns a window-closing event listener
     * to set the application state as terminated. - Adds a key listener for user
     * input handling. - Makes the window visible and enables triple buffering.
     */
    private void createWindow() {
        String winSize = (String) config.getOrDefault("app.window.size", "640x480");

        window = new Frame(messages.getString("app.name"));
        String[] size = winSize.split("x");
        window.setSize(Integer.parseInt(size[0]), Integer.parseInt(size[1]));

        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit = true;
            }
        });
        window.addKeyListener(this);

        window.setVisible(true);
        window.createBufferStrategy(3);
    }

    /**
     * Creates and initializes the rendering buffer for the application.
     * <p>
     * The method retrieves the buffer resolution from the configuration file or
     * falls back to a default value of 640x480 if not specified. It then parses the
     * resolution to determine the width and height of the buffer and creates a new
     * BufferedImage object with the specified dimensions and an alpha-enabled pixel
     * format (ARGB). This buffer is used for off-screen rendering and is essential
     * for the application's graphical rendering process.
     * <p>
     * Behavior: - Reads the buffer resolution configuration key
     * (`app.renderer.buffer.resolution`). - Defaults to "640x480" if no
     * configuration is provided. - Splits the resolution string into width and
     * height. - Initializes a BufferedImage object with the specified resolution
     * and ARGB type.
     */
    private void createBuffer() {
        String bufferReso = (String) config.getOrDefault("app.renderer.buffer.resolution", "640x480");

        String[] bufferSize = bufferReso.split("x");
        buffer = new BufferedImage(Integer.parseInt(bufferSize[0]), Integer.parseInt(bufferSize[1]),
                BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Handles the main application rendering and event loop.
     * <p>
     * This method contains the core logic for the rendering process and time
     * tracking. It continuously executes in a loop until the application state is
     * set to exit, rendering updates to the screen, calculating elapsed time, and
     * maintaining a smooth execution cycle by delaying the loop execution using
     * thread sleep. The method also manages resources efficiently by properly
     * disposing of graphics objects after use.
     * <p>
     * Behavior: - Tracks elapsed time and displays it in the console and the
     * window. - Continuously renders a graphical representation of the elapsed time
     * on a double-buffered display. - Clears the rendering buffer with a black
     * background. - Draws formatted elapsed time as text using antialiasing for
     * better visuals. - Ensures proper disposal of rendering resources to maintain
     * performance. - Uses triple buffering to render the updated frame onto the
     * screen. - Sleeps for 100 ms after each iteration to maintain a consistent
     * update cycle.
     * <p>
     * The loop terminates when the `exit` field is set to true, ensuring a smooth
     * application shutdown.
     */
    private void loop() {
        long startTime = 0, endTime = System.currentTimeMillis(), elapsed = 0, elapsedTime = 0;
        while (!exit) {
            startTime = endTime;
            elapsedTime += elapsed;
            System.out.printf("loop %s\r", getFormatedTime(elapsedTime));
            // update game logic
            update((double) elapsed / 1000f);
            // render everything.
            render((double) (elapsedTime / 1000f));
            // reset Entity's applied forces
            postUpdate((double) (elapsed / 1000f));
            // wait next frame
            waitUntilNextFrame(elapsed);
            endTime = System.currentTimeMillis();
            elapsed = endTime - startTime;
        }
        log(JavaFrameDemo.class, "INFO", String.format("loop %s%n", getFormatedTime(elapsedTime)));
    }

    private static void waitUntilNextFrame(long elapsed) {
        try {
            Thread.sleep((1000 / FPS) - elapsed > 0 ? (1000 / FPS) - elapsed : 1);
        } catch (Exception e) {
            log(JavaFrameDemo.class, "ERROR", String.format("Unable to wait for %d ms", elapsed), e);
        }
    }

    private void update(double elapsed) {
        entities.stream().filter(e -> !(e instanceof World) && e.isActive()).forEach(e -> {
            updateEntity(e, elapsed);
            e.update(elapsed);
            keepEntityIntoWorld(e, world);
        });
        if (camera != null) {
            camera.update(elapsed);
        }
    }

    private void postUpdate(double elapsed) {
        entities.stream().filter(e -> !(e instanceof World) && e.isActive()).forEach(Entity::resetForces);
    }

    /**
     * Updates the position and rotation of the entity based on the elapsed time.
     * This method modifies the entity's x, y, and rotation values by multiplying
     * their respective velocities (dx, dy, and dr) with the provided elapsed time.
     *
     * @param elapsed the time elapsed since the last update, in milliseconds
     */
    public void updateEntity(Entity e, double elapsed) {
        // apply logic update from behaviors
        e.behaviors.forEach(b -> {
            b.update(this, elapsed, e);
        });

        Material material = e.getMaterial();
        if (e.getPhysicType().equals(PhysicType.DYNAMIC)) {
            // add gravity
            e.getForces().add(new Vector2d(world.gravity.getX(), world.gravity.getY()));
            // compute the resulting velocity according to applied forces
            for (Vector2d f : e.getForces()) {
                e.dx += f.getX();
                e.dy += f.getY();
            }

            // compute new position
            e.x += e.dx * elapsed;
            e.y += e.dy * elapsed;
            // compute new rotation
            e.r += e.dr * elapsed;

            // compute friction according to possible contact(s)
            if (e.getContact() > 0) {
                // apply the entity's material friction factor.
                e.dx *= material.friction;
                e.dy *= material.friction;
                e.dr *= material.friction;
            } else {
                // no contact, only apply the World atmoshpere's métaerial friction.
                e.dx *= world.getMaterial().friction;
                e.dy *= world.getMaterial().friction;
                e.dr *= world.getMaterial().friction;
            }
        }

    }

    private void keepEntityIntoWorld(Entity e, World world) {
        e.resetContact();
        if (!world.contains(e)) {
            if (e.x < world.x) {
                e.x = world.x;
                e.dx = -e.dx * e.material.elasticity;
                e.addContact(1);
            }
            if (e.y < world.y) {
                e.y = world.y;
                e.dy = -e.dy * e.material.elasticity;
                e.jump = false;
                e.addContact(2);
            }
            if (e.x + e.width > world.x + world.width) {
                e.x = world.x + world.width - e.width;
                e.dx = -e.dx * e.material.elasticity;
                e.addContact(4);
            }
            if (e.y + e.height > world.y + world.height) {
                e.y = world.y + world.height - e.height;
                // e.dy = -e.dy * e.material.elasticity;
                e.jump = false;
                e.grounded = true;
                e.addContact(8);
            }
        }
    }

    private void render(double elapsedTime) {
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHints(Map.of(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        // clear buffer
        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
        // draw everything!
        entities.stream().filter(Entity::isActive).forEach(e -> {
            if (camera != null)
                g.translate(-camera.x, -camera.y);
            e.draw(g);
            e.behaviors.forEach(b -> b.draw(this, g, e));
            e.drawDebug(g);
            if (camera != null)
                g.translate(camera.x, camera.y);
        });

        // dispose API
        g.dispose();
        // copy on screen
        BufferStrategy bs = window.getBufferStrategy();
        Graphics2D gs = (Graphics2D) bs.getDrawGraphics();
        gs.drawImage(buffer, 0, 0, window.getWidth(), window.getHeight(), 0, 0, buffer.getWidth(), buffer.getHeight(),
                null);
        // draw time on buffer
        if (debug > 0) {
            gs.setColor(Color.ORANGE);
            gs.setFont(g.getFont().deriveFont(Font.BOLD, 10.0f));
            gs.drawString("[ time:%s | debug %d | entity:%d]".formatted(getFormatedTime((long) elapsedTime), debug,
                    entities.size()), 10, window.getHeight() - 12);
        }
        // free graphics API
        gs.dispose();
        // switch buffer
        bs.show();
    }

    /**
     * Releases resources and performs necessary cleanup operations for the
     * application.
     * <p>
     * This method disposes of the main application window and prints a log message
     * to indicate the termination of the application. It is typically called at the
     * end of the application's lifecycle to ensure a proper shutdown.
     * <p>
     * Behavior: - Disposes of the main window resource, releasing associated memory
     * and system resources. - Logs a message to the standard output to signal the
     * end of the application.
     */
    private void dispose() {
        window.dispose();
        log(JavaFrameDemo.class, "INFO", "End test Application.");
    }

    public static void main(String[] args) {
        JavaFrameDemo app = new JavaFrameDemo();
        app.run(args);
    }

    public static void log(Class<?> clazz, String level, String message, Object... args) {
        System.out.printf("%s;%s;%s;%s%n", ZonedDateTime.now(), clazz.getSimpleName(), level, message);
    }

    /**
     * Formats the given time duration in milliseconds into a human-readable string
     * representation. The output format is "HH:mm:ss.SSS", where HH represents
     * hours, mm represents minutes, ss represents seconds, and SSS represents
     * milliseconds.
     *
     * @param time the time duration in milliseconds to be formatted
     * @return a formatted string representing the time duration in "HH:mm:ss.SSS"
     *         format
     */
    public static String getFormatedTime(long time) {
        return "%02d:%02d:%02d.%03d".formatted(((time / 1000) * 3600) % 24, ((time / 1000) / 60) % 60,
                ((time / 1000) % 60), time % 1000);
    }

    /*------------------ Manage key listener ---------------*/

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
        switch (e.getKeyCode()) {
        // quit the demo
        case KeyEvent.VK_ESCAPE -> {
            exit = true;
        }
        // switch level of the debug display /log
        case KeyEvent.VK_D -> {
            if (e.isControlDown()) {
                debug = (debug + 1) % 6;
            }
        }
        // reverse gravity
        case KeyEvent.VK_G -> {
            if (e.isControlDown()) {
                world.gravity = new Vector2d(-world.gravity.getX(), -world.gravity.getY());
            }
        }
        // others cases, do nothing
        default -> {
            // do nothing
        }
        }
    }

    public boolean isKeyPressed(int keyCode) {
        return keys[keyCode];
    }

}