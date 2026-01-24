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
        // Only apply to Camera entities
        if ((cam instanceof Camera camera)) {
            Entity<?> target = camera.getTarget();
            if (target != null) {
                float targetCenterX = target.x + target.width * offsetX;
                float targetCenterY = target.y + target.height * offsetY;
                float cameraHalfWidth = camera.width * offsetX;
                float cameraHalfHeight = camera.height * offsetY;
                if (window == null) {
                    // Centre la caméra sur la target (offset relatif à la taille de la caméra)
                    float desiredX = targetCenterX - cameraHalfWidth;
                    float desiredY = targetCenterY - cameraHalfHeight;
                    camera.x += (desiredX - camera.x) * camera.getTweenFactor() * deltaTime;
                    camera.y += (desiredY - camera.y) * camera.getTweenFactor() * deltaTime;
                } else {
                    // Centre la target au centre de la fenêtre
                    float windowHalfWidth = window.getWidth() * offsetX;
                    float windowHalfHeight = window.getHeight() * offsetY;
                    float desiredX = targetCenterX - windowHalfWidth;
                    float desiredY = targetCenterY - windowHalfHeight;
                    camera.x += (desiredX - camera.x) * camera.getTweenFactor() * deltaTime;
                    camera.y += (desiredY - camera.y) * camera.getTweenFactor() * deltaTime;
                }
            }
        }
    }
}