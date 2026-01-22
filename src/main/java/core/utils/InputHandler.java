package core.utils;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import core.App;

/**
 * Input handler that processes keyboard events. It keeps track of the state of
 * keys (pressed or released) and provides methods to check the status of
 * specific keys. It also handles special key events such as exiting the
 * application and adjusting debug levels.
 * 
 * @see KeyAdapter
 * @see KeyEvent
 * @see App
 * @author Frédéric Delorme<frederic.delorme@gmail.com>
 * @since 2026
 * @version 0.0.1
 */
public class InputHandler extends KeyAdapter {
    private boolean[] keys = new boolean[1024];

    /**
     * Handle key press events by marking the corresponding key as pressed.
     * 
     * @param key the key event
     */
    @Override
    public void keyPressed(KeyEvent key) {
        if (key.getKeyCode() < 0 || key.getKeyCode() >= keys.length) {
            return;
        }
        keys[key.getKeyCode()] = true;
    }

    /**
     * Handle key release events by marking the corresponding key as released. It
     * also processes special keys for application control.
     * 
     * @param key the key event
     */
    @Override
    public void keyReleased(KeyEvent key) {
        if (key.getKeyCode() < 0 || key.getKeyCode() >= keys.length) {
            return;
        }
        keys[key.getKeyCode()] = false;
        switch (key.getKeyCode()) {
        // Request exiting the application
        case KeyEvent.VK_ESCAPE -> App.requestExit();
        // debug level controls
        case KeyEvent.VK_D -> {
            if (App.mode.equals(App.AppMode.DEVELOPMENT) && key.isControlDown()) {
                App.debug = App.debug + 1 < 10 ? App.debug + 1 : 0;
            }
        }

        default -> {
        }
        }
    }

    /**
     * Check if a specific key is currently pressed.
     * 
     * @param keyCode the key code to check
     * @return true if the key is pressed, false otherwise
     */
    public boolean isKeyPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) {
            return false;
        }

        return keys[keyCode];
    }

}
