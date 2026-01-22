package core.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import core.App;
import core.entity.Camera;
import core.entity.Entity;

/**
 * Represents a scene in the application, managing a collection of entities.
 * Scenes can be loaded, initialized, created, and disposed of. The class also
 * handles activation of scenes and retrieval of the active scene. Each scene
 * extends the Entity class, allowing it to have properties and behaviors like
 * other entities.
 * 
 * @see Entity
 * @see App
 * 
 * @author Frédéric Delorme<frederic.delorme@gmail.com>
 * @version 0.0.1
 */
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

    /**
     * Initializes scenes from configuration properties. Read the scenes key from
     * the config and load the corresponding scene classes.
     * 
     * 
     * @param config Configuration properties for scenes initialization.
     */
    public static void initialize(Properties config) {
        scenes.clear();
        // Loading scene classes and creae instances from configuration.
        String[] parts = config.getProperty("scenes", "demo:demo.scenes.DemoScene").split(",");
        for (String part : parts) {
            String[] sceneInfo = part.split(":");
            String sceneName = sceneInfo[0];
            String sceneClassName = sceneInfo[1];
            try {
                Class<?> sceneClass = Class.forName(sceneClassName);
                Scene sceneInstance = (Scene) sceneClass.getDeclaredConstructor(String.class).newInstance(sceneName);
                scenes.add(sceneInstance);
            } catch (Exception e) {
                App.log(Scene.class, App.LogLevel.ERROR,
                        "Failed to load scene: " + sceneName + " (" + sceneClassName + ")", e);
            }
        }
    }

    /**
     * Get the active camera in the scene. If multiple cameras are active, returns
     * the first one found.
     * 
     * @return The active Camera instance, or null if no active camera is found.
     */
    public Camera getCameras() {
        return entities.stream().filter(Camera.class::isInstance).map(Camera.class::cast).filter(Camera::isActive)
                .findFirst().orElse(null);
    }

    /**
     * Set the active camera in the scene. Deactivates all other cameras.
     * 
     * @param camera The camera to set as active.
     */
    public void setActiveCamera(Camera camera) {
        entities.stream().filter(Camera.class::isInstance).map(Camera.class::cast).forEach(c -> c.setActive(false));
        camera.setActive(true);
    }

    /**
     * Set the active camera by its name. Deactivates all other cameras in the
     * scene.
     * 
     * @param cameraName The name of the camera to set as active.
     */
    public void setActiveCamera(String cameraName) {
        entities.stream().filter(Camera.class::isInstance).map(Camera.class::cast).forEach(c -> c.setActive(false));
        entities.stream().filter(Camera.class::isInstance).map(Camera.class::cast)
                .filter(c -> c.getName().equals(cameraName)).findFirst().ifPresent(c -> c.setActive(true));
    }

}
