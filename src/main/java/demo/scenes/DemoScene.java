package demo.scenes;

import java.awt.Color;

import javax.swing.JFrame;

import core.App;
import core.behavior.CameraBehavior;
import core.behavior.GravityBehavior;
import core.behavior.PlayerInputBehavior;
import core.behavior.VelocityBehavior;
import core.behavior.WorldContainedBehavior;
import core.entity.Camera;
import core.entity.GameObject;
import core.entity.World;
import core.graphics.Layer;
import core.graphics.Renderer;
import core.scene.Scene;
import core.utils.Service;

public class DemoScene extends Scene {
    public DemoScene(String name) {
        super(name);
    }

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
                .setVelocity(0, 0).add(new GravityBehavior(9.81f)).add(new VelocityBehavior())
                .add(new WorldContainedBehavior(world)).add(new PlayerInputBehavior(app.getInputHandler()));
        addEntity(player);

        generateBalls(world, foregroundLayer, 200);

        Camera camera = new Camera("cam01").setSize(600, 400).setTarget(player).setTweenFactor(5.0f).setActive(true)
                .add(new CameraBehavior(window, 0.5f, 0.75f));
        addEntity(camera);

        midLayer.add(world);
        foregroundLayer.add(player);
        foregroundLayer.add(camera);
    }

    private void generateBalls(World world, Layer midLayer, int nb) {
        for (int i = 0; i < nb; i++) {
            GameObject ball = new GameObject("ball_" + i)
                    .setPosition((float) (Math.random() * (world.getWidth() - 16)),
                            (float) (Math.random() * (world.getHeight() - 16)))
                    .setSize(16, 16).setVelocity(0, 0)
                    .setFillColor(Color.RED)
                    .setEdgeColor(Color.RED.darker().darker())
                    .add(new GravityBehavior(9.81f))
                    .add(new VelocityBehavior())
                    .add(new WorldContainedBehavior(world));
            midLayer.add(ball);
            addEntity(ball);
        }
    }

}
