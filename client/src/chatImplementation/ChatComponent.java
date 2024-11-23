package chatImplementation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;

import static managers.ColorManager.*;
import static managers.ConstManager.*;
import static managers.FontManager.CHAT__CHATNAME;
import static managers.FontManager.CHAT__DATE;

public class ChatComponent extends JPanel {
    private final ChatsController chatsController;
    private ChatComponentOpen chatCompOpen;
    private final String chatIconPath;
    private final JLabel chatNameLabel;

    public ChatComponent(ChatsController chatsController, String iconPath, String chatName, String lastMessageDate) {
        this.chatsController = chatsController;
        this.chatIconPath = iconPath;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(CHAT__COMPONENT__BG);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        addMouseListener(this);

        JLabel chatPhoto = createChatPhoto(iconPath,CHAT_ICON_SIZE,CHAT_ACTUAL_IMAGE_SIZE,CHAT_ICON_INSET);
        chatNameLabel = createChatNameLabel(chatName);
        JLabel lastMessageDateLabel = createLastMessageDateLabel(lastMessageDate);

        add(chatPhoto);
        add(chatNameLabel);
        add(Box.createHorizontalGlue());
        add(Box.createRigidArea(CHAT_CHATNAME_DATE_GAP));
        add(lastMessageDateLabel);
        add(Box.createRigidArea(CHAT_CHATNAME_AFTER_DATE_GAP));
    }

    protected JLabel createChatPhoto(String chatIconPath, Dimension compSize, Dimension iconSize, int inset) {
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(chatIconPath)));

        return new JLabel() {
            {
                setMinimumSize(compSize);
                setPreferredSize(compSize);
                setMaximumSize(compSize);
            }

            @Override
            protected void paintComponent(Graphics g) {
                BufferedImage img = new BufferedImage(iconSize.width, iconSize.height,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillOval(0,0, iconSize.width, iconSize.height);
                g2.setComposite(AlphaComposite.SrcIn);
                g2.drawImage(icon.getImage(),0,0, iconSize.width, iconSize.height,null);
                g2.dispose();
                g.drawImage(img,inset,inset,null);
            }
        };
    }

    // TODO: find a fix for long names (make them add ...)
    protected JLabel createChatNameLabel(String chatName) {
        return new JLabel(chatName) {
            {
                setFont(CHAT__CHATNAME);
                setForeground(CHAT__CHATNAME_FG);
                setAlignmentY(Component.BOTTOM_ALIGNMENT);
            }
        };
    }

    private JLabel createLastMessageDateLabel(String lastMessageDate) {
        return new JLabel(lastMessageDate) {
            {
                setFont(CHAT__DATE);
                setForeground(CHAT__DATE_FG);
                setAlignmentY(Component.BOTTOM_ALIGNMENT);
            }
        };
    }

    private void addMouseListener(ChatComponent comp) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ChatsController.setChatPressed(true);
                setBackground(CHAT__COMPONENT__BG__PRESSED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ChatsController.setChatPressed(false);
                setBackground(CHAT__COMPONENT__BG);

                if(contains(e.getPoint())) {
                    chatsController.enterChat(comp);
                } else {
                    if (ChatsController.getLastHoveredChatComp() != null &&
                        ChatsController.getLastHoveredChatComp() != comp) {
                        ChatsController.getLastHoveredChatComp().setBackground(CHAT__COMPONENT__BG__ENTERED);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!ChatsController.isChatPressed()) {
                    setBackground(CHAT__COMPONENT__BG__ENTERED);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                ChatsController.setLastHoveredChatComp(comp);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!ChatsController.isChatPressed()) {
                    setBackground(CHAT__COMPONENT__BG);
                }
                ChatsController.setLastHoveredChatComp(null);
            }
        });
    }

    public String getChatIconPath() {
        return chatIconPath;
    }

    public String getChatName() {
        return chatNameLabel.getText();
    }

    public void createChatCompOpen() {
        chatCompOpen = new ChatComponentOpen(chatsController, this);
    }

    public ChatComponentOpen getChatCompOpen() {
        return chatCompOpen;
    }
}