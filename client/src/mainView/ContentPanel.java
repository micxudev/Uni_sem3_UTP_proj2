package mainView;

import chatImplementation.ChatsPanel;
import connectionImplementation.ConnectionPanel;
import settingsImplementation.SettingsPanel;

import javax.swing.*;
import java.awt.*;

import static managers.ColorManager.CONTENT__BG;
import static managers.ColorManager.MAINVIEW__BORDER;
import static managers.ConstManager.FRAME_BORDER_W;

public class ContentPanel extends JPanel {
    private final NorthPanel northPanel;
    private final ChatsPanel chatsPanel;
    private final JPanel holderPanel;
    private JPanel currentPanel;
    private final JPanel southPanel;

    public ContentPanel() {
        setLayout(new BorderLayout());
        setBackground(CONTENT__BG);

        northPanel = new NorthPanel();
        ConnectionPanel connectionPanel = new ConnectionPanel(northPanel.getServerIpLabel(), northPanel.getStatusLabel());
        chatsPanel = new ChatsPanel(this);
        SettingsPanel settingsPanel = new SettingsPanel();

        holderPanel = createHolderPanel();
        holderPanel.add(currentPanel = chatsPanel, BorderLayout.CENTER);
        southPanel = new SouthPanel(this, connectionPanel, chatsPanel, settingsPanel);

        add(northPanel, BorderLayout.NORTH);
        add(holderPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createHolderPanel() {
        return new JPanel(new BorderLayout()) {
            {
                setBackground(MAINVIEW__BORDER);
                setBorder(BorderFactory.createEmptyBorder(FRAME_BORDER_W,0,FRAME_BORDER_W,0));
            }
        };
    }

    public void setCurrentPanel(JPanel newCurrentPanel) {
        if (currentPanel == newCurrentPanel) return;
        holderPanel.remove(currentPanel);
        holderPanel.add(currentPanel = newCurrentPanel, BorderLayout.CENTER);
        holderPanel.repaint();
        holderPanel.revalidate();
    }


    public ChatsPanel getChatsPanel() {
        return chatsPanel;
    }

    public NorthPanel getNorthPanel() {
        return northPanel;
    }

    public JPanel getSouthPanel() {
        return southPanel;
    }

    public JPanel getHolderPanel() {
        return holderPanel;
    }
}