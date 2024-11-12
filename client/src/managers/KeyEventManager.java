package managers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static managers.SystemInfoManager.IS_MAC;

public class KeyEventManager {
    public static void setupGlobalKeyEvents(JFrame frame) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (isMinimiseShortcut(e)) {
                    frame.setState(Frame.ICONIFIED);
                    return true;
                }

                if (isCloseShortcut(e)) {
                    System.exit(0);
                    return true;
                }
            }
            return false;
        });
    }

    private static boolean isMinimiseShortcut(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_M) return false;
        return IS_MAC ? e.isMetaDown() : e.isControlDown();
    }

    private static boolean isCloseShortcut(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_Q) return false;
        return IS_MAC ? e.isMetaDown() : e.isControlDown();
    }
}