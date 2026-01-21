package core.graphics;

import static core.App.log;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import core.App;
import core.App.AppMode;
import core.entity.Entity;
import core.entity.GameObject;
import core.scene.Scene;
import core.utils.InputHandler;
import core.utils.Service;

public class Renderer extends Service {
    private App app;
    private JFrame window;
    private InputHandler inputHandler;

    private List<RenderPlugin<? extends Entity>> renderPlugins = new ArrayList<>();

    public Renderer(App app, InputHandler inputHandler) {
        super(app);
        this.inputHandler = inputHandler;
    }

    @Override
    public void initialize(Properties config) {

        Dimension winSize = new Dimension(800, 600);
        if (config.containsKey("window.width") && config.containsKey("window.height")) {
            int width = Integer.parseInt(config.getProperty("window.width"));
            int height = Integer.parseInt(config.getProperty("window.height"));
            winSize = new Dimension(width, height);
        }

        window = new JFrame(String.format(App.messages.getString("app.title"), App.messages.getString("app.name"),
                App.mode.name()));
        window.setLocationRelativeTo(null);
        window.setPreferredSize(winSize);
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                App.requestExit();
            }
        });
        window.addKeyListener(inputHandler);
        window.pack();
        window.setVisible(true);
        window.createBufferStrategy(3);
        window.requestFocusInWindow();

        registerPlugin(new GameObjectRenderPlugin());
        registerPlugin(new WorldRenderPlugin());

        log(Renderer.class, App.LogLevel.INFO, "Renderer initialized.");
    }

    private void registerPlugin(RenderPlugin<? extends Entity<?>> renderPlugin) {
        renderPlugins.add(renderPlugin);
    }

    @Override
    public void update(Scene scene, float deltaTime, Map<String, Object> stats) {
        if (window != null && window.isActive() && window.isDisplayable()) {
            BufferStrategy bs = window.getBufferStrategy();
            if (bs != null) {
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                // configure rendering
                g.setRenderingHints(Map.of(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON, java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
                // clear screen
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, window.getWidth(), window.getHeight());

                scene.entities.stream().filter(Entity::isActive).filter(Entity::isVisible).forEach(entity -> {
                    drawEntity(g, entity); // Draw each entity
                });

                if (App.mode.equals(AppMode.DEVELOPMENT) && App.debug > 0) {
                    // Update stats
                    stats.put("rendered.entities", scene.entities.size());
                    // Draw debug info
                    g.setColor(new Color(0.3f, 0.1f, 0.0f, 0.7f));
                    g.fillRect(0, window.getHeight() - 30, window.getWidth(), 30);

                    g.setColor(Color.ORANGE);
                    g.drawString(String.format("{ dbg:%d | mode: %s | fps: %d | time: %f | count: %d}", App.debug, App.mode.name(),
                            stats.get("fps"), stats.get("time"), stats.get("rendered.entities")), 20, window.getHeight() - 14);
                }

                // finalize rendering
                g.dispose();
                // show buffer
                bs.show();
            }
        }
    }

    private void drawEntity(Graphics2D g, Entity<?> entity) {
        renderPlugins.stream().filter(plugin -> plugin.getSupportedEntityType().isAssignableFrom(entity.getClass()))
                .findFirst().ifPresent(plugin -> {
                    // Safe to cast because of the isAssignableFrom check
                    @SuppressWarnings("unchecked")
                    RenderPlugin<Entity<?>> castedPlugin = (RenderPlugin<Entity<?>>) plugin;
                    castedPlugin.render(entity, g);
                });
    }

    public JFrame getWindow() {
        return window;
    }

    public void dispose() {
        if (window != null) {
            window.dispose();
        }
        log(Renderer.class, App.LogLevel.INFO, "Renderer disposed.");
    }
}
