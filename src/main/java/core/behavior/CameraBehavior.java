package core.behavior;

import javax.swing.JFrame;

import core.entity.Camera;
import core.entity.Entity;

public class CameraBehavior implements Behavior<Camera> {
    float offsetX = 0.5f;
    float offsetY = 0.75f;
    private JFrame window;

    public CameraBehavior(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public CameraBehavior(JFrame window) {
        this.offsetX = 0.5f;
        this.offsetY = 0.75f;
        this.window = window;
    }

    public CameraBehavior(JFrame window, float offsetX, float offsetY) {
        this.window = window;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void update(Entity<?> cam, float deltaTime) {
        Camera camera = (Camera) cam;
        Entity<?> target = camera.getTarget();
        if (target != null) {
            float targetCenterX = target.x + target.width / 2.0f;
            float targetCenterY = target.y + target.height / 2.0f;
            float cameraHalfWidth = camera.width / 2.0f;
            float cameraHalfHeight = camera.height / 2.0f;
            if (window == null) {
                // Centre la caméra sur la target (offset relatif à la taille de la caméra)
                float desiredX = targetCenterX - cameraHalfWidth;
                float desiredY = targetCenterY - cameraHalfHeight;
                camera.x += (desiredX - camera.x) * camera.getTweenFactor() * deltaTime;
                camera.y += (desiredY - camera.y) * camera.getTweenFactor() * deltaTime;
            } else {
                // Centre la target au centre de la fenêtre
                float windowHalfWidth = window.getWidth() / 2.0f;
                float windowHalfHeight = window.getHeight() / 2.0f;
                float desiredX = targetCenterX - windowHalfWidth;
                float desiredY = targetCenterY - windowHalfHeight;
                camera.x += (desiredX - camera.x) * camera.getTweenFactor() * deltaTime;
                camera.y += (desiredY - camera.y) * camera.getTweenFactor() * deltaTime;
            }
        }
    }
}