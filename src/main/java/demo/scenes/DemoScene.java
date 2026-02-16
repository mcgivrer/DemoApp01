package demo.scenes;

import java.awt.Color;

import javax.swing.JFrame;

import core.App;
import core.behavior.CameraBehavior;
import core.behavior.DefaultCollisionResponseBehavior;
import core.behavior.GravityBehavior;
import core.behavior.PlayerInputBehavior;
import core.behavior.VelocityBehavior;
import core.behavior.WorldContainedBehavior;
import core.entity.Camera;
import core.entity.GameObject;
import core.entity.ShapeType;
import core.entity.World;
import core.graphics.Layer;
import core.graphics.Renderer;
import core.physics.Material;
import core.scene.Scene;
import core.utils.Service;

/**
 * A demo scene showcasing a simple world with a player-controlled entity and
 * multiple balls affected by gravity. The scene includes layers for rendering
 * and a camera that follows the player.
 * 
 * @see Scene
 * @see World
 * @see GameObject
 * @see Camera
 * @see Layer
 * @see GravityBehavior
 * @see PlayerInputBehavior
 * @see VelocityBehavior
 * @see WorldContainedBehavior
 * @see CameraBehavior
 * 
 * @author Frédéric Delorme<frederic.delorme@gmail.com>
 * @version 0.0.1
 * @since 2026
 */
public class DemoScene extends Scene {
    /**
     * Constructor for DemoScene.
     * 
     * @param name The name of the scene.
     */
    public DemoScene(String name) {
        super(name);
    }

    /**
     * Creates the demo scene by initializing the world, player entity, balls, and
     * camera. The scene is set up with appropriate layers and behaviors for each
     * entity.
     * 
     * @param app The application instance used to access input handlers and other
     *            services.
     */
    @Override
    public void create(App app) {
        JFrame window = Service.get(Renderer.class).getWindow();
        // add a world entity

        Layer foregroundLayer = new Layer("foreground", Layer.LayerType.FOREGROUND, 2);
        addEntity(foregroundLayer);

        Layer midLayer = new Layer("midground", Layer.LayerType.MIDGROUND, 1);
        addEntity(midLayer);

        World world = new World("earth").setGravity(9.81f).setSize(window.getWidth(), window.getHeight());
        addEntity(world);

        // add a player entity
        GameObject player = new GameObject("player")
                .setPosition((world.getWidth() - 24) / 2, (world.getHeight() - 32) / 2).setSize(24, 32)
                .setVelocity(0, 0).setFillColor(Color.GREEN).setEdgeColor(Color.GREEN.darker().darker())
                .setMaterial(Material.ICE).setDebugLevel(2).setPriority(10).setAttribute("speed", 2000f)
                .setAttribute("angularSpeed", 5f).setAttribute("jumpFactor", 4.0f)
                .add(new GravityBehavior(world.getGravity())).add(new VelocityBehavior())
                .add(new WorldContainedBehavior(world)).add(new PlayerInputBehavior(app.getInputHandler()))
                .add(new DefaultCollisionResponseBehavior());
        addEntity(player);

        generateBalls(world, foregroundLayer, 200);

        Camera camera = new Camera("cam01").setSize(600, 400).setTarget(player).setTweenFactor(5.0f).setActive(true)
                .add(new CameraBehavior(window, 0.5f, 0.75f));
        addEntity(camera);

        // dispatching entities across layers.
        midLayer.add(world);
        foregroundLayer.add(player);
        foregroundLayer.add(camera);
    }

    private void generateBalls(World world, Layer midLayer, int nb) {
        for (int i = 0; i < nb; i++) {
            int size = 8 + (int) (Math.random() * 4);
            Color baseColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
            GameObject ball = new GameObject("ball_" + i)
                    .setPosition((float) (Math.random() * (world.getWidth() - size)),
                            (float) (Math.random() * (world.getHeight() - size)))
                    .setSize(size, size).setShapeType(ShapeType.CIRCLE)
                    .setVelocity(1500f - (3000F * (float) Math.random()), 1500f - (3000F * (float) Math.random()))
                    .setFillColor(baseColor).setEdgeColor(baseColor.darker().darker().darker())
                    .setMaterial(Material.SUPERBALL).add(new GravityBehavior(world.getGravity()))
                    .add(new VelocityBehavior()).add(new WorldContainedBehavior(world))
                    .add(new DefaultCollisionResponseBehavior()).setDebugLevel(3);
            midLayer.add(ball);
            addEntity(ball);
        }
    }

}
