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
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import core.App;
import core.entity.Entity;
import core.entity.GameObject;
import core.physics.PhysicsEngine;
import core.scene.Scene;
import core.utils.InputHandler;
import core.utils.Service;

public class Renderer extends Service {
    private App app;
    private JFrame window;
    private InputHandler inputHandler;

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

        log(Renderer.class, App.LogLevel.INFO, "Renderer initialized.");
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

                // update stats
                stats.put("rendered.entities", scene.entities.size());

                if (App.debug > 0) {
                    g.setColor(new Color(0.3f, 0.1f, 0.0f, 0.7f));
                    g.fillRect(10, window.getHeight() - 30, window.getWidth(), 30);
                    g.setColor(Color.ORANGE);
                    g.drawString(String.format("{ dbg:%d | mode: %s | fps: %d | time: %d }", App.debug, App.mode.name(),
                            stats.get("fps"), stats.get("time")), 20, window.getHeight() - 14);
                }

                // finalize rendering
                g.dispose();
                // show buffer
                bs.show();
            }
        }
    }

    private void drawEntity(Graphics2D g, Entity<?> entity) {
        if (entity instanceof GameObject go) {
            if (go.getSprite() != null) {
                g.drawImage(go.getSprite(), (int) entity.getX(), (int) entity.getY(), (int) entity.getWidth(),
                        (int) entity.getHeight(), null);
                return;
            } else {
                g.setColor(go.getFillColor());
                g.fillRect((int) (int) entity.getX(), (int) entity.getY(), (int) entity.getWidth(),
                        (int) entity.getHeight());
                g.setColor(go.getEdgeColor());
                g.drawRect((int) (int) entity.getX(), (int) entity.getY(), (int) entity.getWidth(),
                        (int) entity.getHeight());
            }
        } else {
            g.setColor(Color.ORANGE);
            Stroke bk = g.getStroke();
            g.setStroke(new BasicStroke(0.5f));
            g.drawRect((int) (int) entity.getX(), (int) entity.getY(), (int) entity.getWidth(),
                    (int) entity.getHeight());
            g.setStroke(bk);
        }
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
