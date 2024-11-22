package chatImplementation;

import mainView.ContentPanel;

import javax.swing.*;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.HashMap;

import static managers.ConstManager.FRAME_BORDER_W;

public class ChatsController {
    private final ContentPanel contentPanel;
    private final JPanel chatsListPanel;
    private static final HashMap<String, AbstractMap.SimpleEntry<ChatComponent, ChatComponentOpen>> chatsStorage = new HashMap<>();
    private static ChatComponent lastHoveredChatComp;
    private static boolean chatPressed = false;
    private static boolean chatOpen = false;

    public ChatsController(ContentPanel contentPanel, JPanel chatsListPanel) {
        this.contentPanel = contentPanel;
        this.chatsListPanel = chatsListPanel;

        addNewChat("/resources/icons/socket128x128.png", "Global Chat", String.valueOf(LocalDate.now()));
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
        ChatComponent comp = new ChatComponent(this, iconPath, chatName, lastMessageDate);
        chatsListPanel.add(comp);
    }

    protected void enterChat(ChatComponent comp) {
        contentPanel.getSouthPanel().setVisible(false);
        contentPanel.getHolderPanel().setBorder(BorderFactory.createEmptyBorder(FRAME_BORDER_W,0,0,0));

        if (!chatsStorage.containsKey(comp.getChatName())) {
            chatsStorage.put(comp.getChatName(),
            new AbstractMap.SimpleEntry<>(comp, new ChatComponentOpen(this, comp)));
        }
        AbstractMap.SimpleEntry<ChatComponent, ChatComponentOpen> chatEntry = chatsStorage.get(comp.getChatName());
        contentPanel.setCurrentPanel(chatEntry.getValue());
        chatOpen = true;
    }

    protected void returnToChatsList() {
        contentPanel.getSouthPanel().setVisible(true);
        contentPanel.getHolderPanel().setBorder(BorderFactory.createEmptyBorder(FRAME_BORDER_W,0,FRAME_BORDER_W,0));
        contentPanel.setCurrentPanel(contentPanel.getChatsPanel());
        chatOpen = false;
    }

    public static HashMap<String, AbstractMap.SimpleEntry<ChatComponent, ChatComponentOpen>> getChatsStorage() {
        return chatsStorage;
    }
}