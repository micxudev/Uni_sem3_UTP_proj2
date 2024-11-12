package mainView;

import javax.swing.*;
import java.awt.*;

import static managers.ColorManager.MAINVIEW__BORDER;
import static managers.ConstManager.FRAME_BORDER_W;

public class ContentPane extends JPanel {
    public ContentPane(JFrame mainFrame) {
        setLayout(new BorderLayout());
        setBackground(MAINVIEW__BORDER);
        setBorder(BorderFactory.createEmptyBorder(FRAME_BORDER_W,FRAME_BORDER_W,FRAME_BORDER_W,FRAME_BORDER_W));

        add(new Titlebar(mainFrame), BorderLayout.NORTH);
        add(new ContentPanel(), BorderLayout.CENTER);
    }
}