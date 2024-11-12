package mainView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static java.awt.Cursor.*;
import static managers.ColorManager.*;
import static managers.ConstManager.*;
import static managers.FontManager.TITLEBAR__TITLE;
import static managers.SystemInfoManager.IS_MAC;

public class Titlebar extends JPanel {
    private final JFrame mainFrame;

    private final JLabel title;
    private final JButton minimiseButton;
    private final JButton maximiseButton;
    private final JButton closeButton;

    private int pX, pY;
    private boolean paintButtonSign = false;
    private boolean isWindowMaximised = false;
    private final Rectangle savedWindowBounds = new Rectangle(0,0,0,0);

    private enum ButtonClickAction {
        MINIMISE,
        MAXIMISE,
        CLOSE
    }

    public Titlebar(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(TITLEBAR__BG);

        title          = createTitle();
        minimiseButton = createButton(TITLEBAR_MINIMISE_BUTTON_TOOLTIP, ButtonClickAction.MINIMISE);
        maximiseButton = createButton(TITLEBAR_MAXIMISE_BUTTON_TOOLTIP, ButtonClickAction.MAXIMISE);
        closeButton    = createButton(TITLEBAR_CLOSE_BUTTON_TOOLTIP,    ButtonClickAction.CLOSE);

        addWindowFocusListener();
        addTitlebarListeners();
        addFrameListener();

        if (IS_MAC) {
            add(Box.createRigidArea(TITLEBAR_BUTTON_GAP));
            add(createButtonPanel());
            add(Box.createHorizontalGlue());
            add(title);
            add(Box.createHorizontalGlue());
            add(Box.createRigidArea(TITLEBAR_SIDE_GAP));
        } else {
            add(Box.createRigidArea(TITLEBAR_SIDE_GAP));
            add(Box.createHorizontalGlue());
            add(title);
            add(Box.createHorizontalGlue());
            add(createButtonPanel());
            add(Box.createRigidArea(TITLEBAR_BUTTON_GAP));
        }
    }

    private JLabel createTitle() {
        return new JLabel(mainFrame.getTitle()) {
            {
                setFont(TITLEBAR__TITLE);
                setForeground(TITLEBAR__TITLE__FG___BUTTON__BG__PRESSED);
            }
        };
    }

    private JButton createButton(String tooltip, ButtonClickAction action) {
        return new JButton("") {
            {
                setToolTipText(tooltip);
                setFocusable(false);
                setBorder(BorderFactory.createEmptyBorder());
                setMinimumSize(TITLEBAR_BUTTON_SIZE);
                setPreferredSize(TITLEBAR_BUTTON_SIZE);
                setMaximumSize(TITLEBAR_BUTTON_SIZE);
                setForeground(TITLEBAR__BUTTON__FG);
                setBackground(TITLEBAR__BUTTON__BG);

                switch (action) {
                    case MINIMISE -> addActionListener(_ -> mainFrame.setState(Frame.ICONIFIED));
                    case MAXIMISE -> addActionListener(_ -> handleWindowMaximiseAction(true));
                    case CLOSE    -> addActionListener(_ -> System.exit(0));
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? TITLEBAR__TITLE__FG___BUTTON__BG__PRESSED : getBackground());
                if (IS_MAC)
                    g2.fillOval(0,0,getWidth(),getHeight());
                else
                    g2.fillRect(0,0,getWidth(),getHeight());

                if (!paintButtonSign) {
                    g2.dispose();
                    return;
                }

                g2.setColor(getForeground());
                if (this == minimiseButton) {
                    int pad = TITLEBAR_BUTTON_SIZE.width/6;
                    int w = TITLEBAR_BUTTON_SIZE.width-2*pad;
                    int h = 2;
                    int y = (TITLEBAR_BUTTON_SIZE.height-h)/2;
                    g2.fillRect(pad,y,w,h);
                }

                else if (this == maximiseButton) {
                    int s = TITLEBAR_BUTTON_SIZE.width/2;
                    int x = TITLEBAR_BUTTON_SIZE.width/4;
                    int y = TITLEBAR_BUTTON_SIZE.height/4;

                    int[] xP1, yP1, xP2, yP2;
                    if (!isWindowMaximised) {
                        xP1 = new int[]{x,x+s-x/2,x};
                        yP1 = new int[]{y,y,y+s-y/2};
                        xP2 = new int[]{x+s,x+s,x+x/2};
                        yP2 = new int[]{y+s,y+y/2,y+s};
                    } else {
                        xP1 = new int[]{s,s,x/2};
                        yP1 = new int[]{s,y/2,s};
                        xP2 = new int[]{s,s+s-x/2,s};
                        yP2 = new int[]{s,s,s+s-y/2};
                    }

                    if (!IS_MAC) {
                        reflectHorizontally(yP1, s);
                        reflectHorizontally(yP2, s);
                    }
                    g2.fillPolygon(xP1,yP1,3);
                    g2.fillPolygon(xP2,yP2,3);
                }

                else if (this == closeButton) {
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                    int pad = TITLEBAR_BUTTON_SIZE.width/4;
                    int x = TITLEBAR_BUTTON_SIZE.width-pad-1;
                    int y = TITLEBAR_BUTTON_SIZE.height-pad-1;
                    g2.drawLine(pad,pad,x,y);
                    g2.drawLine(pad,y,x,pad);
                }
                g2.dispose();
            }
        };
    }

    private void reflectHorizontally(int[] yPoints, int yReflect) {
        for (int i = 0; i < yPoints.length; i++)
            yPoints[i] = 2 * yReflect - yPoints[i];
    }

    private JPanel createButtonPanel() {
        JPanel bp = new JPanel();
        bp.setOpaque(false);
        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));

        MouseAdapter panelMouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isAnyButtonPressed()) return;

                if (!mainFrame.isFocused())
                    setAllButtonsBackground(TITLEBAR__BUTTON__BG);

                setPaintSignAndRepaint(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!mainFrame.isFocused())
                    setAllButtonsBackground(TITLEBAR__BUTTON__BG__WINDOW_NO_FOCUS);

                if (isAnyButtonPressed()) return;

                setPaintSignAndRepaint(false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!bp.contains(SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), bp)))
                    setPaintSignAndRepaint(false);
            }
        };

        bp.addMouseListener(panelMouseListener);
        minimiseButton.addMouseListener(panelMouseListener);
        maximiseButton.addMouseListener(panelMouseListener);
        closeButton.addMouseListener(panelMouseListener);

        if (IS_MAC) {
            bp.add(closeButton);
            bp.add(Box.createRigidArea(TITLEBAR_BUTTON_GAP));
            bp.add(minimiseButton);
            bp.add(Box.createRigidArea(TITLEBAR_BUTTON_GAP));
            bp.add(maximiseButton);
        } else {
            bp.add(minimiseButton);
            bp.add(Box.createRigidArea(TITLEBAR_BUTTON_GAP));
            bp.add(maximiseButton);
            bp.add(Box.createRigidArea(TITLEBAR_BUTTON_GAP));
            bp.add(closeButton);
        }

        return bp;
    }

    private boolean isAnyButtonPressed() {
        return minimiseButton.getModel().isPressed() ||
               maximiseButton.getModel().isPressed() ||
               closeButton.getModel().isPressed();
    }

    private void setAllButtonsBackground(Color color) {
        minimiseButton.setBackground(color);
        maximiseButton.setBackground(color);
        closeButton.setBackground(color);
    }

    private void setPaintSignAndRepaint(boolean paint) {
        paintButtonSign = paint;
        minimiseButton.repaint();
        maximiseButton.repaint();
        closeButton.repaint();
    }


    // ---------- Actions ----------
    // frame focus
    private void addWindowFocusListener() {
        mainFrame.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                title.setForeground(TITLEBAR__TITLE__FG___BUTTON__BG__PRESSED);
                setAllButtonsBackground(TITLEBAR__BUTTON__BG);
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                title.setForeground(TITLEBAR__TITLE__FG__WINDOW_NO_FOCUS);
                setAllButtonsBackground(TITLEBAR__BUTTON__BG__WINDOW_NO_FOCUS);
            }
        });
    }

    // frame movement + enter/quit fullscreen extra options + resize (when within Titlebar)
    private void addTitlebarListeners() {
        addMouseListener(new MouseAdapter() {
            // 2nd Option to enter/quit fullscreen (double-click on Titlebar)
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleWindowMaximiseAction(false);
                }
            }

            // save press positions  and  frame bounds (only when not in fullscreen)
            @Override
            public void mousePressed(MouseEvent e) {
                pX = e.getX();
                pY = e.getY();
                if (!isWindowMaximised) saveFrameBounds();
            }

            // 3rd option to enter fullscreen (bump North edge of the screen)
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getYOnScreen() < FRAME_RESIZE_W)
                    handleWindowMaximiseAction(false);
            }
        });

        // 3rd option to quit fullscreen (drag inside Titlebar),
        // default frame move (when not fullscreen)  or  resize (within Titlebar bounds - 5 directions)
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mainFrame.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
                    if (isWindowMaximised) {
                        isWindowMaximised = false;
                        int ratioX = savedWindowBounds.width * pX / mainFrame.getWidth();
                        mainFrame.setBounds(pX-ratioX,0, savedWindowBounds.width, savedWindowBounds.height);
                        pX = ratioX;
                    } else {
                        mainFrame.setLocation(e.getXOnScreen()-pX-FRAME_BORDER_W, Math.max(e.getYOnScreen()-pY-FRAME_BORDER_W,0));
                    }
                } else {
                    resizeFrame(e);
                }
            }

            // set proper cursor within Titlebar bounds (5 directions + default)
            @Override
            public void mouseMoved(MouseEvent e) {
                // prevent setting cursor (and resizing) when in fullscreen mode
                if (isWindowMaximised) return;

                // fast check for being on the edge or not (within Titlebar bounds)
                if (e.getX() < FRAME_INSET_W || e.getX() >= getWidth() - FRAME_INSET_W || e.getY() < FRAME_INSET_W) {
                    // we are on the edge -> proceed and set the exact cursor

                    // check corners first to decrease the number of checks for sides

                    // NW
                    if ((e.getX() < FRAME_CORNER_INSET_W && e.getY() < FRAME_INSET_W) ||
                        (e.getX() < FRAME_INSET_W && e.getY() < FRAME_CORNER_INSET_W)) {
                        mainFrame.setCursor(getPredefinedCursor(NW_RESIZE_CURSOR));
                    }
                    // NE
                    else if ((e.getY() < FRAME_INSET_W && e.getX() >= getWidth() - FRAME_CORNER_INSET_W) ||
                            (e.getY() < FRAME_CORNER_INSET_W && e.getX() >= getWidth() - FRAME_INSET_W)) {
                        mainFrame.setCursor(getPredefinedCursor(NE_RESIZE_CURSOR));
                    }

                    // no need to check second coordinate, because it would catch it before (on the corners)

                    // N
                    else if (e.getY() < FRAME_INSET_W) {
                        mainFrame.setCursor(getPredefinedCursor(N_RESIZE_CURSOR));
                    }
                    // W
                    else if (e.getX() < FRAME_INSET_W) {
                        mainFrame.setCursor(getPredefinedCursor(W_RESIZE_CURSOR));
                    }
                    // E
                    else if (e.getX() >= getWidth() - FRAME_INSET_W) {
                        mainFrame.setCursor(getPredefinedCursor(E_RESIZE_CURSOR));
                    }
                } else {
                    mainFrame.setCursor(getPredefinedCursor(DEFAULT_CURSOR));
                }
            }
        });
    }

    // set cursor (outside the Titlebar) + resize frame
    private void addFrameListener() {
        mainFrame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // prevent setting cursor (and resizing) when in fullscreen mode
                if (isWindowMaximised) return;

                // fast check for being on the edge (within full bounds including border)
                if (e.getX() < FRAME_RESIZE_W || e.getX() >= mainFrame.getWidth() - FRAME_RESIZE_W ||
                    e.getY() < FRAME_RESIZE_W || e.getY() >= mainFrame.getHeight() - FRAME_RESIZE_W) {

                    if ((e.getX() >= mainFrame.getWidth() - FRAME_RESIZE_W && e.getY() >= mainFrame.getHeight() - FRAME_CORNER_RESIZE_W) ||
                        (e.getX() >= mainFrame.getWidth() - FRAME_CORNER_RESIZE_W && e.getY() >= mainFrame.getHeight() - FRAME_RESIZE_W))
                        mainFrame.setCursor(getPredefinedCursor(SE_RESIZE_CURSOR));
                    else if ((e.getX() < FRAME_RESIZE_W && e.getY() >= mainFrame.getHeight() - FRAME_CORNER_RESIZE_W) ||
                             (e.getX() < FRAME_CORNER_RESIZE_W && e.getY() >= mainFrame.getHeight() - FRAME_RESIZE_W))
                        mainFrame.setCursor(getPredefinedCursor(SW_RESIZE_CURSOR));
                    else if ((e.getX() >= mainFrame.getWidth() - FRAME_CORNER_RESIZE_W && e.getY() < FRAME_RESIZE_W) ||
                             (e.getX()) >= mainFrame.getWidth() - FRAME_RESIZE_W && e.getY() < FRAME_CORNER_RESIZE_W)
                        mainFrame.setCursor(getPredefinedCursor(NE_RESIZE_CURSOR));
                    else if ((e.getX() < FRAME_CORNER_RESIZE_W && e.getY() < FRAME_RESIZE_W) ||
                             (e.getX() < FRAME_RESIZE_W && e.getY() < FRAME_CORNER_RESIZE_W))
                        mainFrame.setCursor(getPredefinedCursor(NW_RESIZE_CURSOR));
                    else if (e.getY() < FRAME_RESIZE_W)
                        mainFrame.setCursor(getPredefinedCursor(N_RESIZE_CURSOR));
                    else if (e.getX() < FRAME_RESIZE_W)
                        mainFrame.setCursor(getPredefinedCursor(W_RESIZE_CURSOR));
                    else if (e.getY() >= mainFrame.getHeight() - FRAME_RESIZE_W)
                        mainFrame.setCursor(getPredefinedCursor(S_RESIZE_CURSOR));
                    else if (e.getX() >= mainFrame.getWidth() - FRAME_RESIZE_W)
                        mainFrame.setCursor(getPredefinedCursor(E_RESIZE_CURSOR));
                } else {
                    mainFrame.setCursor(getPredefinedCursor(DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (mainFrame.getCursor().getType() != Cursor.DEFAULT_CURSOR)
                    resizeFrame(e);
            }
        });
    }

    // resize frame based on cursor
    private void resizeFrame(MouseEvent e) {
        if (isWindowMaximised) return;

        int eX = e.getX();
        int eY = e.getY();

        switch (mainFrame.getCursor().getType()) {
            case NW_RESIZE_CURSOR -> {
                if (eY > 0 && mainFrame.getHeight() - eY < FRAME_ACTUAL_MIN_H) eY = mainFrame.getHeight() - FRAME_ACTUAL_MIN_H;
                if (eX > 0 && mainFrame.getWidth() - eX < FRAME_ACTUAL_MIN_W) eX = mainFrame.getWidth() - FRAME_ACTUAL_MIN_W;
                mainFrame.setBounds(mainFrame.getX() + eX, mainFrame.getY() + eY, mainFrame.getWidth() - eX, mainFrame.getHeight() - eY);
            }
            case SW_RESIZE_CURSOR -> {
                if (eX > 0 && mainFrame.getWidth() - eX < FRAME_ACTUAL_MIN_W) eX = mainFrame.getWidth() - FRAME_ACTUAL_MIN_W;
                mainFrame.setBounds(mainFrame.getX() + eX, mainFrame.getY(), mainFrame.getWidth() - eX, eY);
            }
            case NE_RESIZE_CURSOR -> {
                if (eY > 0 && mainFrame.getHeight() - eY < FRAME_ACTUAL_MIN_H) eY = mainFrame.getHeight() - FRAME_ACTUAL_MIN_H;
                mainFrame.setBounds(mainFrame.getX(), mainFrame.getY() + eY, eX, mainFrame.getHeight() - eY);
            }
            case N_RESIZE_CURSOR -> {
                if (eY > 0 && mainFrame.getHeight() - eY < FRAME_ACTUAL_MIN_H) eY = mainFrame.getHeight() - FRAME_ACTUAL_MIN_H;
                mainFrame.setBounds(mainFrame.getX(), mainFrame.getY() + eY, mainFrame.getWidth(), mainFrame.getHeight() - eY);
            }
            case W_RESIZE_CURSOR -> {
                if (eX > 0 && mainFrame.getWidth() - eX < FRAME_ACTUAL_MIN_W) eX = mainFrame.getWidth() - FRAME_ACTUAL_MIN_W;
                mainFrame.setBounds(mainFrame.getX() + eX, mainFrame.getY(), mainFrame.getWidth() - eX, mainFrame.getHeight());
            }
            case SE_RESIZE_CURSOR -> mainFrame.setBounds(mainFrame.getX(), mainFrame.getY(), eX, eY);
            case E_RESIZE_CURSOR -> mainFrame.setBounds(mainFrame.getX(), mainFrame.getY(), eX, mainFrame.getHeight());
            case S_RESIZE_CURSOR -> mainFrame.setBounds(mainFrame.getX(), mainFrame.getY(), mainFrame.getWidth(), eY);
        }
    }

    // enter/quit fullscreen handler
    public void handleWindowMaximiseAction(boolean saveBounds) {
        if (!isWindowMaximised) {
            isWindowMaximised = true;
            if (saveBounds) saveFrameBounds();
            mainFrame.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
            mainFrame.setCursor(getPredefinedCursor(DEFAULT_CURSOR));
        } else {
            isWindowMaximised = false;
            mainFrame.setBounds(savedWindowBounds);
        }
    }

    // save last position before switching default and fullscreen modes
    private void saveFrameBounds() {
        savedWindowBounds.x = mainFrame.getX();
        savedWindowBounds.y = mainFrame.getY();
        savedWindowBounds.width = mainFrame.getWidth();
        savedWindowBounds.height = mainFrame.getHeight();
    }
}