package chatImplementation;

import javax.swing.*;

import java.awt.*;

import static managers.ColorManager.*;
import static managers.ConstManager.OPENCHAT_MAX_MESSAGE_WIDTH;
import static managers.FontManager.OPENCHAT__MESSAGE;

public class ChatMessageComponent extends JPanel {
    public ChatMessageComponent(String message) {
        setLayout(new BorderLayout());

        add(createMessageArea(message), BorderLayout.CENTER);
    }

    private JTextPane createMessageArea(String message) {
        return new JTextPane() {
            {
                setText(message);
                setBackground(MESSAGE__SENT__BG);
                setForeground(MESSAGE__SENT__FG);
                setCaretColor(MESSAGE__SENT__FG);
                setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                setFont(OPENCHAT__MESSAGE);
                setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                setEditable(false);
                setOpaque(true);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension pref = super.getPreferredSize();
                pref.width = Math.min(pref.width, OPENCHAT_MAX_MESSAGE_WIDTH);
                return pref;
            }
        };
    }
}