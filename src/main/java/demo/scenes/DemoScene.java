package demo.scenes;

import javax.swing.JFrame;

import core.App;
import core.behavior.GravityBehavior;
import core.behavior.VelocityBehavior;
import core.behavior.WorldContainedBehavior;
import core.entity.GameObject;
import core.graphics.Renderer;
import core.scene.Scene;
import core.utils.Service;

public class DemoScene extends Scene {
    public DemoScene(String name) {
        super(name);
    }

    @Override
    public void create(App app) {
        JFrame window =Service.get(Renderer.class).getWindow();
        
        // add a player entity
        addEntity(new GameObject("player")
                .setPosition((window.getWidth() - 8)/2, (window.getHeight() - 8)/2)
                .setSize(16, 16)
                .setVelocity(0, 0)
                .add(new GravityBehavior(9.81f))
                .add(new VelocityBehavior())
                .add(new WorldContainedBehavior(800, 600)));
    }
}
