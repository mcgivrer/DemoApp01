package core.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import core.App;
import core.entity.Entity;
import demo.DemoScene;

public class Scene extends Entity<Scene> {

    public static List<Scene> scenes = new ArrayList<>();
    public static Scene currentScene = null;
    public List<Entity<?>> entities = new ArrayList<>();

    public Scene(String name) {
        super(name);
    }

    public static void add(Scene scene) {
        scenes.add(scene);
    }

    public void load(App app) {

    }

    public void init(App app) {

    }

    public void create(App app) {

    }

    public void dispose(App app) {
    }

    public Scene addEntity(Entity<?> entity) {
        if (entities.contains(entity)) {
            return this;
        }
        entities.add(entity);
        return this;
    }

    public Scene removeEntity(Entity<?> entity) {
        entities.remove(entity);
        return this;
    }

    public List<Entity<?>> getEntities() {
        return entities;
    }

    public static Scene activate(App app, String name) {
        scenes.stream().filter(s -> s.getName().equals(name)).findFirst().ifPresentOrElse(s -> currentScene = s, null);
        currentScene.load(app);
        currentScene.init(app);
        currentScene.create(app);
        return currentScene;
    }

    public static Scene getActiveScene() {
        return currentScene;
    }

    public static void initialize(Properties config) {
        scenes.clear();
        // temporary loading of demo scenes
        scenes.add(new DemoScene("demo"));
    }
}
