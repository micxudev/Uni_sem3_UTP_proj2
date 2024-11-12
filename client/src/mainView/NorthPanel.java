package mainView;

import javax.swing.*;
import java.awt.*;

import static managers.ColorManager.*;
import static managers.ConstManager.*;
import static managers.FontManager.NORTHPANEL__STATUS_LABEL;

public class NorthPanel extends JPanel {
    private final JLabel serverIP;
    private final JLabel connectionStatus;

    public NorthPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        setPreferredSize(NORTH_SOUTH_PANEL_SIZE);
        setBorder(BorderFactory.createEmptyBorder(0,FRAME_INSET_W, 0,FRAME_INSET_W));

        JLabel serverLabel = createLabel("Server: ", NORTH__STATUS_LABEL__FG);
               serverIP    = createLabel("-", NORTH__STATUS_VALUE__FG);
        JPanel serverPanel = createPanel(serverLabel, serverIP);

        JLabel statusLabel = createLabel("Connection status: ", NORTH__STATUS_LABEL__FG);
          connectionStatus = createLabel("not connected", CONTENT__STATUS_LABEL__DEFAULT);
        JPanel statusPanel = createPanel(statusLabel, connectionStatus);

        add(createFinalPanel(serverPanel, statusPanel));
    }

    private JLabel createLabel(String text, Color fg) {
        return new JLabel(text) {
            {
                setFont(NORTHPANEL__STATUS_LABEL);
                setForeground(fg);
            }
        };
    }

    private JPanel createPanel(JLabel label, JLabel value) {
        return new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setOpaque(false);
                add(label);
                add(value);
            }
        };
    }

    private JPanel createFinalPanel(JPanel serverPanel, JPanel statusPanel) {
        return new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setOpaque(false);
                add(serverPanel);
                add(Box.createRigidArea(NORTHPANEL_VERTICAL_GAP));
                add(statusPanel);
            }
        };
    }

    public JLabel getServerIpLabel() {
        return serverIP;
    }

    public JLabel getStatusLabel() {
        return connectionStatus;
    }
}