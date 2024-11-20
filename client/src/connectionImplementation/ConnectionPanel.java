package connectionImplementation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import static managers.ColorManager.*;
import static managers.ConstManager.*;
import static managers.FontManager.*;

public class ConnectionPanel extends JPanel {
    private static ConnectionController connectionController;

    public ConnectionPanel(JLabel northServerIp, JLabel northConnectionStatus) {
        setLayout(new GridBagLayout());
        setBackground(CONTENT__BG);

        JLabel titleLabel = createTitleLabel();

        JTextField ipTextField = createTextField("IP address");
        ipTextField.setText("localhost");

        JTextField portTextField = createTextField("Port (0-65535)");
        portTextField.setText("25575");

        JTextField usernameTextField = createTextField("Name [a-z0-9_] [5;32]");

        JLabel statusLabel = createStatusLabel();

        JButton connectButton = createConnectButton();
        connectionController = new ConnectionController(ipTextField, portTextField, usernameTextField, statusLabel, connectButton, northServerIp, northConnectionStatus);
        connectButton.addActionListener(_ -> connectionController.buttonClick());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.insets.bottom = 30;
        add(titleLabel, gbc);
        gbc.gridy = 1;
        gbc.insets.bottom = 15;
        add(ipTextField, gbc);
        gbc.gridy = 2;
        add(portTextField, gbc);
        gbc.gridy = 3;
        gbc.insets.bottom = 0;
        add(usernameTextField, gbc);
        gbc.gridy = 4;
        gbc.insets.bottom = 15;
        add(statusLabel, gbc);
        gbc.gridy = 5;
        add(connectButton, gbc);
    }

    private JLabel createTitleLabel() {
        return new JLabel("Server Connection") {
            {
                setFont(CONNECTION__TITLE);
                setForeground(CONTENT__TITLE__FG);
                setPreferredSize(CONNECTION_TITLE_PREF_SIZE);
                setHorizontalAlignment(CENTER);
            }
        };
    }

    private JTextField createTextField(String placeholderText) {
        return new JTextField(placeholderText) {
            {
                setOpaque(false);
                setFocusable(true);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                setMinimumSize(CONNECTION_TF_SIZE);
                setPreferredSize(CONNECTION_TF_SIZE);
                setMaximumSize(CONNECTION_TF_SIZE);
                setFont(CONNECTION__TEXTFIELD);
                setForeground(CONTENT__TEXTFIELD__FG__NO_FOCUS);
                setCaretColor(CONTENT__TEXTFIELD__FG);
                setBackground(CONTENT__TEXTFIELD__BG);

                addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (getText().equals(placeholderText)) {
                            setText("");
                            setForeground(CONTENT__TEXTFIELD__FG);
                        }
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (getText().isEmpty()) {
                            setText(placeholderText);
                            setForeground(CONTENT__TEXTFIELD__FG__NO_FOCUS);
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0,0,getWidth(),getHeight(),CONNECTION_COMP_ARC,CONNECTION_COMP_ARC);
                super.paintComponent(g);
                g2.dispose();
            }
        };
    }

    private JLabel createStatusLabel() {
        return new JLabel("not connected") {
            {
                setFont(CONNECTION__STATUS_LABEL);
                setForeground(CONTENT__STATUS_LABEL__DEFAULT);
                setPreferredSize(CONNECTION_STATUS_PREF_SIZE);
                setHorizontalAlignment(CENTER);
            }
        };
    }

    private JButton createConnectButton() {
        return new JButton("Connect") {
            {
                setMinimumSize(CONNECTION_BUTTON_SIZE);
                setPreferredSize(CONNECTION_BUTTON_SIZE);
                setMaximumSize(CONNECTION_BUTTON_SIZE);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder());
                setFont(CONNECTION__BUTTON);
                setForeground(CONTENT__BUTTON__FG);
                setBackground(CONTENT__BUTTON__BG);
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isEnabled() && getModel().isPressed() ? CONTENT__BUTTON__BG__PRESSED : getBackground());
                g2.fillRoundRect(0,0,getWidth(),getHeight(),CONNECTION_COMP_ARC,CONNECTION_COMP_ARC);

                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(),x,y);
                g2.dispose();
            }
        };
    }

    public static ConnectionController getConnectionController() {
        return connectionController;
    }
}