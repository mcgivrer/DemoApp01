package demo.scenes;

import java.awt.event.KeyEvent;

import javax.swing.JFrame;

import core.App;
import core.behavior.Behavior;
import core.behavior.GravityBehavior;
import core.behavior.PlayerInputBehavior;
import core.behavior.VelocityBehavior;
import core.behavior.WorldContainedBehavior;
import core.entity.Entity;
import core.entity.GameObject;
import core.entity.World;
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
        World world = new World("earth").setGravity(9.81f).setSize(window.getWidth(), window.getHeight()).setPosition(window.getWidth() / 2,
                -window.getHeight() / 3);
        addEntity(world);

        // add a player entity
        addEntity(new GameObject("player").setPosition((window.getWidth() - 24) / 2, (window.getHeight() - 32) / 2)
                .setSize(24, 32).setVelocity(0, 0).add(new GravityBehavior(9.81f)).add(new VelocityBehavior())
                .add(new WorldContainedBehavior(world)).add(new PlayerInputBehavior(app.getInputHandler())));

    }

}
