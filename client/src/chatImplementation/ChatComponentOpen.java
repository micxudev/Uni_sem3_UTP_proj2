package chatImplementation;

import connectionImplementation.Connection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static managers.ColorManager.*;
import static managers.ConstManager.*;
import static managers.FontManager.OPENCHAT__BUTTON;
import static managers.FontManager.OPENCHAT__MESSAGE;

public class ChatComponentOpen extends JPanel {
    private final ChatsController chatsController;
    private final ChatComponent chatComponent;
    private JTextArea messageTA;
    private JButton sendMessageButton;
    private final JPanel messagesPanel;
    private final JScrollPane messagesScroll;
    private final GridBagConstraints gbc;

    public ChatComponentOpen(ChatsController chatsController, ChatComponent chatComponent) {
        this.chatsController = chatsController;
        this.chatComponent = chatComponent;
        setLayout(new BorderLayout());
        setBackground(CONTENT__BG);

        JPanel holderPanel = createCentralHolderPanel();
        gbc = createGBC();

        messagesPanel = new JPanel(new GridBagLayout());
        messagesPanel.setBackground(CONTENT__BG);
        messagesPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        messagesScroll = createScrollPane(messagesPanel, OPENCHAT_SCROLL_WIDTH, null);
        holderPanel.add(messagesScroll, BorderLayout.CENTER);

        add(createNorthPanel(), BorderLayout.NORTH);
        add(holderPanel, BorderLayout.CENTER);
        add(createSouthPanel(), BorderLayout.SOUTH);

        returnToChatsListOnEscape();
    }

    private void returnToChatsListOnEscape() {
        InputMap inputMap = chatComponent.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = chatComponent.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "returnToChatsList");
        actionMap.put("returnToChatsList", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ChatsController.isChatOpen())
                    chatsController.returnToChatsList();
            }
        });
    }

    // North
    private JPanel createNorthPanel() {
        return new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setBackground(CHAT__COMPONENT__BG);
                setBorder(BorderFactory.createEmptyBorder(0,FRAME_INSET_W,0,FRAME_INSET_W));

                JButton getBackButton = createButton("< Back", OPENCHAT_GETBACK_BUTTON_SIZE);
                getBackButton.addActionListener(_ -> chatsController.returnToChatsList());
                JLabel photoLabel = chatComponent.createChatPhoto(chatComponent.getChatIconPath(),OPENCHAT_ICON_SIZE,OPENCHAT_ACTUAL_IMAGE_SIZE,OPENCHAT_ICON_INSET);
                JLabel chatNameLabel = chatComponent.createChatNameLabel(chatComponent.getChatName());

                add(getBackButton);
                add(photoLabel);
                add(chatNameLabel);
            }
        };
    }

    // Center
    private JPanel createCentralHolderPanel() {
        return new JPanel(new BorderLayout()) {
            {
                setBorder(BorderFactory.createEmptyBorder(FRAME_INSET_W+FRAME_BORDER_W,FRAME_INSET_W,
                                                        2*(FRAME_INSET_W+FRAME_BORDER_W),FRAME_INSET_W));
            }
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(OPENCHAT__BORDER);
                g.fillRect(0,0,getWidth(),FRAME_BORDER_W);
                g.setColor(CONTENT__BG);
                g.fillRect(0,FRAME_BORDER_W,getWidth(),getHeight());
                g.setColor(OPENCHAT__BORDER);
                g.fillRect(0,getHeight()-FRAME_BORDER_W,getWidth(),FRAME_BORDER_W);
            }
        };
    }

    private JScrollPane createScrollPane(Component view, int scrollBarWidth, Component parent) {
        JScrollPane scrollPane = new JScrollPane(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER) {
            {
                setVerticalScrollBar(new mainView.ScrollBar(50, scrollBarWidth));
                setBorder(BorderFactory.createEmptyBorder());
                setOpaque(false);
                getViewport().setOpaque(false);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(view.getPreferredSize().width,
                        Math.min(view.getPreferredSize().height, 300));
            }
        };

        if (view instanceof JTextArea) {
            ((JTextArea) view).getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    parent.revalidate();
                    sendMessageButton.setVisible(!messageTA.getText().trim().isEmpty());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    parent.revalidate();
                    sendMessageButton.setVisible(!messageTA.getText().trim().isEmpty());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    parent.revalidate();
                    sendMessageButton.setVisible(!messageTA.getText().trim().isEmpty());
                }
            });
        }
        return scrollPane;
    }

    private GridBagConstraints createGBC() {
        return new GridBagConstraints() {
            {
                gridx = 0;
                gridy = GridBagConstraints.RELATIVE;
                anchor = GridBagConstraints.LINE_END;
                weightx = 1.0;
                insets = new Insets(0,0,2,OPENCHAT_MESSAGE_RIGHT_INSET);
            }
        };
    }

    // South
    private JPanel createSouthPanel() {
        return new JPanel(new BorderLayout()) {
            {
                setBackground(CHAT__COMPONENT__BG);
                setBorder(BorderFactory.createEmptyBorder(0,FRAME_INSET_W,FRAME_INSET_W,FRAME_INSET_W));

                messageTA = createMessageTextArea();
                add(createScrollPane(messageTA, 0, this), BorderLayout.CENTER);
                add(createSendMessageButtonPanel(), BorderLayout.EAST);
            }
        };
    }

    private JTextArea createMessageTextArea() {
        return new JTextArea(OPENCHAT_TA_PLACEHOLDER_TEXT, 1, 0) {
            {
                setLineWrap(true);
                setWrapStyleWord(true);
                setOpaque(false);
                setFont(OPENCHAT__MESSAGE);
                setForeground(OPENCHAT__MESSAGE_TEXT_NO_FOCUS);
                setCaretColor(OPENCHAT__MESSAGE_TEXT_CARET);
                setMinimumSize(OPENCHAT_TA_MIN_SIZE);
                int topBotPad = (OPENCHAT_SENDMSG_BUTTON_SIZE.height - getRowHeight()) / 2;
                setBorder(BorderFactory.createEmptyBorder(topBotPad+1, 10, topBotPad, 10));

                addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (getText().equals(OPENCHAT_TA_PLACEHOLDER_TEXT)) {
                            setText("");
                            setForeground(OPENCHAT__MESSAGE_TEXT);
                        }
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (getText().isEmpty()) {
                            setText(OPENCHAT_TA_PLACEHOLDER_TEXT);
                            setForeground(OPENCHAT__MESSAGE_TEXT_NO_FOCUS);
                        }
                    }
                });

                addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            if (e.isShiftDown()) {
                                insert("\n", getCaretPosition());
                            } else {
                                e.consume();
                                processSendMessageAttempt();
                            }
                        }
                    }
                });
            }
        };
    }

    private JPanel createSendMessageButtonPanel() {
        return new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setOpaque(false);
                sendMessageButton = createButton("Send", OPENCHAT_SENDMSG_BUTTON_SIZE);
                sendMessageButton.setVisible(false);
                sendMessageButton.addActionListener(_ -> processSendMessageAttempt());

                add(Box.createVerticalGlue());
                add(sendMessageButton);
            }
        };
    }

    // North/South
    private JButton createButton(String text, Dimension size) {
        return new JButton(text) {
            {
                setMinimumSize(size);
                setPreferredSize(size);
                setMaximumSize(size);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder());
                setForeground(OPENCHAT__BUTTON__FG);
                setFont(OPENCHAT__BUTTON);
                setBackground(CHAT__COMPONENT__BG);
                setFocusable(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(getBackground());
                g2.fillRect(0,0,getWidth(),getHeight());

                if (getModel().isPressed()) {
                    g2.setColor(OPENCHAT__BUTTON__BG__PRESSED);
                    g2.fillOval(5,5,getWidth()-10,getHeight()-10);
                    g2.setColor(OPENCHAT__BUTTON__FG__OVER);
                } else if (getModel().isRollover()){
                    g2.setColor(OPENCHAT__BUTTON__FG__OVER);
                } else {
                    g2.setColor(getForeground());
                }

                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - (fm.getDescent() / 2);
                g2.drawString(getText(),x,y);

                g2.dispose();
            }
        };
    }

    // Logic
    private void processSendMessageAttempt() {
        if (messageTA.getText().trim().isEmpty()) {
            return;
        }

        if (!Connection.isAlive()) {
            JOptionPane.showMessageDialog(this, "Connect to the server to send a message", "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            String message = messageTA.getText().trim();
            Connection.sendMessage(message);

            messagesPanel.add(new ChatMessageComponent(message), gbc);
            messageTA.setText("");
            messagesPanel.revalidate();
            SwingUtilities.invokeLater(() -> messagesScroll.getVerticalScrollBar().setValue(Integer.MAX_VALUE));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error sending a message to the server:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addChatMessageComponent(String message) {
        messagesPanel.add(new ChatMessageComponent(message), gbc);
        messageTA.setText(OPENCHAT_TA_PLACEHOLDER_TEXT);
        messagesPanel.revalidate();
        SwingUtilities.invokeLater(() -> messagesScroll.getVerticalScrollBar().setValue(Integer.MAX_VALUE));
    }
}