package chatImplementation;

import mainView.ContentPanel;

import javax.swing.*;

import java.time.LocalDate;

import static managers.ConstManager.FRAME_BORDER_W;

public class ChatsController {
    private final ContentPanel contentPanel;
    private final JPanel chatsListPanel;

    private static ChatComponent lastHoveredChatComp;
    private static boolean chatPressed = false;
    private static boolean chatOpen = false;

    public ChatsController(ContentPanel contentPanel, JPanel chatsListPanel) {
        this.contentPanel = contentPanel;
        this.chatsListPanel = chatsListPanel;

        addNewChat("/resources/icons/socket128x128.png", "Server Chat", String.valueOf(LocalDate.now()));
    }

    public static boolean isChatOpen() {
        return chatOpen;
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
        chatsListPanel.add(new ChatComponent(this, iconPath, chatName, lastMessageDate));
    }

    protected void enterChat(ChatComponent comp) {
        contentPanel.getSouthPanel().setVisible(false);
        contentPanel.getHolderPanel().setBorder(BorderFactory.createEmptyBorder(FRAME_BORDER_W,0,0,0));
        contentPanel.setCurrentPanel(new ChatComponentOpen(this, comp));
        chatOpen = true;
    }

    protected void returnToChatsList() {
        contentPanel.getSouthPanel().setVisible(true);
        contentPanel.getHolderPanel().setBorder(BorderFactory.createEmptyBorder(FRAME_BORDER_W,0,FRAME_BORDER_W,0));
        contentPanel.setCurrentPanel(contentPanel.getChatsPanel());
        chatOpen = false;
    }
}