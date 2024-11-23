package chatImplementation;

import mainView.ContentPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.HashMap;

import static managers.ConstManager.FRAME_BORDER_W;
import static managers.ConstManager.GLOBAL_CHAT_NAME;

public class ChatsController {
    private final ContentPanel contentPanel;
    private final JPanel chatsListPanel;
    private static final HashMap<String, ChatComponent> chatsStorage = new HashMap<>();
    private static ChatComponent lastHoveredChatComp;
    private static boolean chatPressed = false;
    private static boolean chatOpen = false;

    public ChatsController(ContentPanel contentPanel, JPanel chatsListPanel) {
        this.contentPanel = contentPanel;
        this.chatsListPanel = chatsListPanel;

        addNewChat("/resources/icons/socket128x128.png", GLOBAL_CHAT_NAME, String.valueOf(LocalDate.now()));
        SwingUtilities.invokeLater(this::returnToChatsListOnEscape);
    }

    public static boolean isChatPressed() {
        return chatPressed;
    }

    public static ChatComponent getLastHoveredChatComp() {
        return lastHoveredChatComp;
    }

    public static void setChatPressed(boolean pressed) {
        chatPressed = pressed;
    }

    public static void setLastHoveredChatComp(ChatComponent chatComp) {
        lastHoveredChatComp = chatComp;
    }

    // Main functions
    protected void addNewChat(String iconPath, String chatName, String lastMessageDate) {
        ChatComponent comp = new ChatComponent(this, iconPath, chatName, lastMessageDate);
        comp.createChatCompOpen();
        chatsListPanel.add(comp);
        chatsStorage.put(chatName, comp); // save for access from wherever
    }

    protected void enterChat(ChatComponent comp) {
        contentPanel.getSouthPanel().setVisible(false);
        contentPanel.getHolderPanel().setBorder(BorderFactory.createEmptyBorder(FRAME_BORDER_W,0,0,0));
        contentPanel.setCurrentPanel(comp.getChatCompOpen());
        chatOpen = true;
    }

    protected void returnToChatsList() {
        contentPanel.getSouthPanel().setVisible(true);
        contentPanel.getHolderPanel().setBorder(BorderFactory.createEmptyBorder(FRAME_BORDER_W,0,FRAME_BORDER_W,0));
        contentPanel.setCurrentPanel(contentPanel.getChatsPanel());
        chatOpen = false;
    }

    private void returnToChatsListOnEscape() {
        InputMap inputMap = chatsListPanel.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = chatsListPanel.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "returnToChatsList");
        actionMap.put("returnToChatsList", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chatOpen) {
                    returnToChatsList();
                }
            }
        });
    }

    public static HashMap<String, ChatComponent> getChatsStorage() {
        return chatsStorage;
    }
}