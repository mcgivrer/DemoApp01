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
import core.behavior.particle.ExplosionEmitterBehavior;
import core.behavior.particle.FountainEmitterBehavior;
import core.behavior.particle.ParticlePhysicsBehavior;
import core.behavior.particle.RainEmitterBehavior;
import core.entity.Camera;
import core.entity.GameObject;
import core.entity.ParticleSystem;
import core.entity.PhysicsType;
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
        Renderer renderer = Service.get(Renderer.class);
        renderer.registerPlugin(new core.graphics.ParticleRenderPlugin());
        JFrame window = Service.get(Renderer.class).getWindow();
        // add a world entity

        Layer foregroundLayer = new Layer("foreground", Layer.LayerType.FOREGROUND, 2);
        addEntity(foregroundLayer);

        Layer midLayer = new Layer("midground", Layer.LayerType.MIDGROUND, 1);
        addEntity(midLayer);

        World world = new World("earth").setGravity(9.81f).setSize(window.getWidth(), window.getHeight());
        addEntity(world);

        GameObject ground = new GameObject("ground").setPosition(0, window.getHeight() - 50)
                .setSize(window.getWidth(), 50).setFillColor(Color.DARK_GRAY).setEdgeColor(Color.BLACK)
                .setMaterial(Material.STONE).setMass(1000f).setPhysicsType(PhysicsType.STATIC)
                .add(new WorldContainedBehavior(world)).add(new DefaultCollisionResponseBehavior());

        midLayer.add(ground);
        addEntity(ground);

        GameObject leftWall = new GameObject("leftWall").setPosition(0, 0).setSize(50, window.getHeight())
                .setFillColor(Color.DARK_GRAY).setEdgeColor(Color.BLACK).setMaterial(Material.STONE).setMass(1000f)
                .setPhysicsType(PhysicsType.STATIC).add(new WorldContainedBehavior(world))
                .add(new DefaultCollisionResponseBehavior());
        midLayer.add(leftWall);
        addEntity(leftWall);

        GameObject rightWall = new GameObject("rightWall").setPosition(window.getWidth() - 50, 0)
                .setSize(50, window.getHeight()).setFillColor(Color.DARK_GRAY).setEdgeColor(Color.BLACK)
                .setMaterial(Material.STONE).setMass(1000f).setPhysicsType(PhysicsType.STATIC)
                .add(new WorldContainedBehavior(world)).add(new DefaultCollisionResponseBehavior());
        midLayer.add(rightWall);
        addEntity(rightWall);

        GameObject ceiling = new GameObject("ceiling").setPosition(0, 0).setSize(window.getWidth(), 50)
                .setFillColor(Color.DARK_GRAY).setEdgeColor(Color.BLACK).setMaterial(Material.STONE).setMass(1000f)
                .setPhysicsType(PhysicsType.STATIC).add(new WorldContainedBehavior(world))
                .add(new DefaultCollisionResponseBehavior());
        midLayer.add(ceiling);
        addEntity(ceiling);

        GameObject platform = new GameObject("platform").setPosition(200, 300).setSize(200, 20).setFillColor(Color.GRAY)
                .setEdgeColor(Color.BLACK).setMaterial(Material.WOOD).setMass(500f).setPhysicsType(PhysicsType.STATIC)
                .add(new WorldContainedBehavior(world)).add(new DefaultCollisionResponseBehavior());
        midLayer.add(platform);
        addEntity(platform);

        GameObject movingPlatform = new GameObject("movingPlatform").setPosition(400, 200).setSize(150, 20)
                .setFillColor(Color.GRAY).setEdgeColor(Color.BLACK).setMaterial(Material.WOOD).setMass(500f)
                .setPhysicsType(PhysicsType.KINEMATIC).add(new WorldContainedBehavior(world)).setVelocity(100f, 0f)
                .add(new MovingPlatformBehavior(200, 550, 200f)) // Controls velocity
                .add(new VelocityBehavior()) // Applies movement
                .add(new DefaultCollisionResponseBehavior());
        midLayer.add(movingPlatform);
        addEntity(movingPlatform);

        // add a player entity
        GameObject player = new GameObject("player")
                .setPosition((world.getWidth() - 24) / 2, (world.getHeight() - 32) / 2).setSize(24, 32)
                .setVelocity(0, 0).setGravityCenter(12, 28).setMaterial(Material.WOOD).setMass(70.0f).setDebugLevel(2)
                .setFillColor(Color.GREEN).setEdgeColor(Color.GREEN.darker().darker()).setPriority(10)
                .setAttribute("speed", 2000f).setAttribute("angularSpeed", 5f).setAttribute("jumpFactor", 4.0f)
                .add(new GravityBehavior(world.getGravity())).add(new VelocityBehavior())
                .add(new WorldContainedBehavior(world)).add(new PlayerInputBehavior(app.getInputHandler()))
                .add(new DefaultCollisionResponseBehavior());
        addEntity(player);

        generateBalls(world, foregroundLayer, 200, 50, 50);

        // Fontaine
        ParticleSystem fountain = new ParticleSystem("fountain", 200);
        fountain.setPosition(400, 500);
        fountain.add(new FountainEmitterBehavior().setEmissionRate(60));
        fountain.add(new ParticlePhysicsBehavior());
        fountain.setLayer(midLayer);
        addEntity(fountain);

        /// rainy day
        // Pluie orageuse
        ParticleSystem rain = new ParticleSystem("rain", 500);
        rain.setPosition(0, 0);
        rain.setLineRenderingForRain(); 
        rain.add(new RainEmitterBehavior()
            .presetDrizzle()
            .setRainColors(Color.BLUE,Color.BLUE.darker().darker().darker())
            .setEmissionWidth(window.getWidth())  // Couvre toute la fenêtre
            .setEmissionYOffset(-20));            // Spawn légèrement au-dessus
        rain.add(new ParticlePhysicsBehavior().presetRain());
        rain.setLayer(foregroundLayer);
        addEntity(rain);

        Camera camera = new Camera("cam01").setSize(600, 400).setTarget(player).setTweenFactor(5.0f).setActive(true)
                .add(new CameraBehavior(window, 0.5f, 0.75f));
        addEntity(camera);

        // dispatching entities across layers.
        midLayer.add(world);
        foregroundLayer.add(player);
        foregroundLayer.add(camera);
    }

    private void generateBalls(World world, Layer midLayer, int nb, int xOffset, int yOffset) {
        for (int i = 0; i < nb; i++) {
            int size = 8 + (int) (Math.random() * 4);
            Color baseColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
            GameObject ball = new GameObject("ball_" + i)
                    .setPosition((float) (xOffset + Math.random() * (world.getWidth() - size - xOffset)),
                            (float) (yOffset + Math.random() * (world.getHeight() - size - yOffset)))
                    .setSize(size, size).setShapeType(ShapeType.CIRCLE)
                    .setVelocity(1500f - (3000F * (float) Math.random()), 1500f - (3000F * (float) Math.random()))
                    .setFillColor(baseColor).setEdgeColor(baseColor.darker().darker().darker())
                    .setMaterial(Material.SUPERBALL).setMass(size * size / 100.0f)
                    .add(new GravityBehavior(world.getGravity())).add(new VelocityBehavior())
                    .add(new WorldContainedBehavior(world)).add(new DefaultCollisionResponseBehavior())
                    .setDebugLevel(3);
            midLayer.add(ball);
            addEntity(ball);
        }
    }

}
