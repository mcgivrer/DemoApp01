package core.behavior;

import java.awt.event.KeyEvent;

import core.entity.Entity;
import core.entity.GameObject;
import core.utils.InputHandler;

/**
 * Behavior that processes player input to control an entity's movement. The
 * entity's velocity is adjusted based on keyboard input, allowing for
 * responsive control of the entity in a game environment. This behavior
 * typically responds to arrow keys or QSWZ (querty) keys for directional
 * movement.
 * 
 * @see Entity
 * @see GameObject
 */
public class PlayerInputBehavior implements Behavior<GameObject> {

    private InputHandler inputHandler;

    public PlayerInputBehavior(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    @Override
    public void update(Entity<?> entity, float deltaTime) {
        float speed = 200.0f;
        if (inputHandler.isKeyPressed(KeyEvent.VK_LEFT) || inputHandler.isKeyPressed(KeyEvent.VK_Q)) {
            entity.setVx(-speed);
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_RIGHT) || inputHandler.isKeyPressed(KeyEvent.VK_S)) {
            entity.setVx(speed);
        } else {
            entity.setVx(entity.getVx() * 0.8f);
        }
        if (inputHandler.isKeyPressed(KeyEvent.VK_UP) || inputHandler.isKeyPressed(KeyEvent.VK_Z)) {
            entity.setVy(-speed * 2.5f);
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_DOWN) || inputHandler.isKeyPressed(KeyEvent.VK_W)) {
            entity.setVy(speed);
        } else {
            entity.setVy(entity.getVy() * 0.8f);
        }
    }

}
