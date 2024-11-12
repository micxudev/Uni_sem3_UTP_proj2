package settingsImplementation;

import javax.swing.*;
import java.awt.*;

import static managers.ColorManager.CONTENT__BG;

public class SettingsPanel extends JPanel {

    public SettingsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CONTENT__BG);

        JLabel label = new JLabel("Settings (Later)");
        label.setForeground(Color.white);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(label);
    }
}