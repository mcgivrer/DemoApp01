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
        float speed = entity.getAttribute("speed", 200.0f);
        float jumpFactor = entity.getAttribute("jumpFactor", 4.0f);
        float friction = 1.0f - entity.getMaterial().friction();

        // horizontal movement
        if (inputHandler.isKeyPressed(KeyEvent.VK_LEFT) || inputHandler.isKeyPressed(KeyEvent.VK_Q)) {
            entity.setVx(-speed);
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_RIGHT) || inputHandler.isKeyPressed(KeyEvent.VK_S)) {
            entity.setVx(speed);
        } else {
            // Apply friction only to horizontal movement (player-controlled)
            entity.setVx(entity.getVx() * friction);
        }

        // vertical movement (jump/down input only)
        // Do NOT apply friction to vy - gravity handles vertical physics
        if (inputHandler.isKeyPressed(KeyEvent.VK_UP) || inputHandler.isKeyPressed(KeyEvent.VK_Z)) {
            // Apply jump impulse (contact check would require collision detection before input)
            entity.setVy(-speed * jumpFactor);
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_DOWN) || inputHandler.isKeyPressed(KeyEvent.VK_W)) {
            entity.setVy(speed);
        }
        // No else - let gravity control vy naturally

        // rotation
        float angularSpeed = entity.getAttribute("angularSpeed", 90.0f); // degrés/seconde
        if (inputHandler.isKeyPressed(KeyEvent.VK_A)) {
            entity.setVa(-angularSpeed);
        } else if (inputHandler.isKeyPressed(KeyEvent.VK_E)) {
            entity.setVa(angularSpeed);
        } else {
            entity.setVa(entity.getVa() * friction);
        }
    }

}
