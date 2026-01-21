package core.utils;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {
    private boolean[] keys = new boolean[1024];

    public void keyPressed(KeyEvent key) {
        if (key.getKeyCode() < 0 || key.getKeyCode() >= keys.length) {
            return;
        }
        keys[key.getKeyCode()] = true;
    }

    public void keyReleased(KeyEvent key) {
        if (key.getKeyCode() < 0 || key.getKeyCode() >= keys.length) {
            return;
        }
        keys[key.getKeyCode()] = false;
    }

    public boolean isKeyPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) {
            return false;
        }
        return keys[keyCode];
    }

}
