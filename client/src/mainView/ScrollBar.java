package mainView;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

import static managers.ColorManager.SCROLL__BG;
import static managers.ColorManager.SCROLL__FG;
import static managers.ConstManager.*;

public class ScrollBar extends JScrollBar {
    public ScrollBar(int unitIncrement, int scrollBarWidth) {
        setUI(new MyScrollBarUI());
        setPreferredSize(new Dimension(scrollBarWidth, 0));
        setBackground(SCROLL__BG);
        setForeground(SCROLL__FG);
        setUnitIncrement(unitIncrement);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        setFocusable(false);
    }

    private static class MyScrollBarUI extends BasicScrollBarUI {

        @Override
        protected Dimension getMaximumThumbSize() {
            return new Dimension(0, scrollbar.getHeight());
        }

        @Override
        protected Dimension getMinimumThumbSize() {
            return SCROLL_THUMB_MIN_SIZE;
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createEmptyButton();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createEmptyButton();
        }

        private JButton createEmptyButton() {
            return new JButton() {
                {
                    setMinimumSize(SCROLL_BUTTON_SIZE);
                    setPreferredSize(SCROLL_BUTTON_SIZE);
                    setMaximumSize(SCROLL_BUTTON_SIZE);
                }
            };
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(scrollbar.getBackground());
            g.fillRect(trackBounds.x+trackBounds.width/4,trackBounds.y,trackBounds.width/2,trackBounds.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(scrollbar.getForeground());
            g2.fillRoundRect(thumbBounds.x,thumbBounds.y,thumbBounds.width,thumbBounds.height,SCROLL_THUMB_ARC,SCROLL_THUMB_ARC);
        }
    }
}