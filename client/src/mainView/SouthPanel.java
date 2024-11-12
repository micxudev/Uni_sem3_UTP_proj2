package mainView;

import chatImplementation.ChatsPanel;
import connectionImplementation.ConnectionPanel;
import settingsImplementation.SettingsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

import static managers.ColorManager.SOUTH__BUTTON__BG__SELECTED;
import static managers.ColorManager.SOUTH__BUTTON__FG;
import static managers.ConstManager.*;
import static managers.FontManager.SOUTHPANEL__BUTTON;

public class SouthPanel extends JPanel {
    private final ContentPanel contentPanel;

    public SouthPanel(ContentPanel contentPanel, ConnectionPanel connectionPanel, ChatsPanel chatsPanel, SettingsPanel settingsPanel) {
        this.contentPanel = contentPanel;
        setLayout(new GridLayout(1,3));
        setOpaque(false);
        setPreferredSize(NORTH_SOUTH_PANEL_SIZE);
        setBorder(BorderFactory.createEmptyBorder(0,FRAME_INSET_W,FRAME_INSET_W,FRAME_INSET_W));

        JRadioButton connectionButton = createBottomButton("Server", "/resources/icons/server.png", connectionPanel);
        JRadioButton chatsButton = createBottomButton("Chats", "/resources/icons/chat.png", chatsPanel);
        JRadioButton settingsButton = createBottomButton("Settings", "/resources/icons/settings.png", settingsPanel);

        ButtonGroup bg = new ButtonGroup();
        bg.add(connectionButton);
        bg.add(chatsButton);
        bg.add(settingsButton);
        bg.setSelected(chatsButton.getModel(), true);

        add(connectionButton);
        add(chatsButton);
        add(settingsButton);
    }

    private JRadioButton createBottomButton(String text, String iconPath, JPanel dedicatedPanel) {
        return new JRadioButton(text) {
            private final ImageIcon defaultIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(iconPath)));
            private final ImageIcon selectedIcon = createSelectedIcon(defaultIcon);
            {
                setOpaque(false);
                setAlignmentY(Component.TOP_ALIGNMENT);
                setBorder(BorderFactory.createEmptyBorder());
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setForeground(SOUTH__BUTTON__FG);
                setFont(SOUTHPANEL__BUTTON);
                addActionListener(_ -> contentPanel.setCurrentPanel(dedicatedPanel));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ImageIcon iconToDraw = isSelected() ? selectedIcon : defaultIcon;
                int iconX = (getWidth() - iconToDraw.getIconWidth()) / 2;
                int iconY = (getHeight() - iconToDraw.getIconHeight() - g.getFontMetrics().getHeight()) / 2;
                g2.drawImage(iconToDraw.getImage(), iconX, iconY, this);

                g2.setColor(isSelected() ? SOUTH__BUTTON__BG__SELECTED : SOUTH__BUTTON__FG);
                int textX = (getWidth() - g.getFontMetrics().stringWidth(getText())) / 2;
                int textY = iconY + iconToDraw.getIconHeight() + g.getFontMetrics().getHeight();
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }

            private ImageIcon createSelectedIcon(ImageIcon initIcon) {
                BufferedImage image = new BufferedImage(initIcon.getIconWidth(), initIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = image.createGraphics();
                g2.drawImage(initIcon.getImage(), 0, 0, null);
                g2.setComposite(AlphaComposite.SrcAtop);
                g2.setColor(SOUTH__BUTTON__BG__SELECTED);
                g2.fillRect(0, 0, image.getWidth(), image.getHeight());
                g2.dispose();
                return new ImageIcon(image);
            }
        };
    }
}