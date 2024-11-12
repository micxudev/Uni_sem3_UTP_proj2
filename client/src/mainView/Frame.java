package mainView;

import managers.KeyEventManager;

import javax.swing.*;
import java.awt.*;

import static managers.ColorManager.CONTENT__BG;
import static managers.ConstManager.FRAME_MIN_SIZE;

public class Frame extends JFrame {
    public Frame() {
        super("Socket");
        KeyEventManager.setupGlobalKeyEvents(this);
        setVisible(true);
    }

    @Override
    protected void frameInit() {
        enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
        setLocale(JComponent.getDefaultLocale());
        setRootPane(createRootPane());
        setRootPaneCheckingEnabled(true);
        setUndecorated(true);
        setContentPane(new ContentPane(this));
        setMinimumSize(FRAME_MIN_SIZE);
        setBackground(CONTENT__BG);
        setLocationRelativeTo(null);
    }
}