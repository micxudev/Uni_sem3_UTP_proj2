package chatImplementation;

import mainView.ContentPanel;

import javax.swing.*;
import java.awt.*;

import static javax.swing.ScrollPaneConstants.*;
import static managers.ColorManager.CONTENT__BG;
import static managers.ConstManager.*;

public class ChatsPanel extends JPanel {
    public ChatsPanel(ContentPanel contentPanel) {
        setLayout(new BorderLayout());
        setBackground(CONTENT__BG);
        setBorder(BorderFactory.createEmptyBorder(0,FRAME_INSET_W,0,FRAME_INSET_W));

        JPanel chatsListPanel = createChatsListPanel();
        new ChatsController(contentPanel, chatsListPanel);
        add(createScroll(chatsListPanel), BorderLayout.CENTER);
    }

    private JPanel createChatsListPanel() {
        return new JPanel() {
            {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };
    }

    private JScrollPane createScroll(JPanel chatsPanel) {
        return new JScrollPane(chatsPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER) {
            {
                setVerticalScrollBar(new mainView.ScrollBar(CHAT_COMPONENT_HEIGHT, CHAT_SCROLL_WIDTH));
                setBorder(BorderFactory.createEmptyBorder());
                setOpaque(false);
                getViewport().setOpaque(false);
            }
        };
    }
}