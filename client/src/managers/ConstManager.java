package managers;

import java.awt.*;

import static managers.SystemInfoManager.IS_MAC;
import static managers.SystemInfoManager.MODIFIER_KEY;

public class ConstManager {
    /*--------------- FRAME ---------------*/
    public static final int       FRAME_CONTENT_MIN_W   = 505;
    public static final int       FRAME_CONTENT_MIN_H   = 550;
    public static final int       FRAME_BORDER_W        = 1;
    public static final int       FRAME_INSET_W         = 5;
    public static final int       FRAME_CORNER_INSET_W  = 2*FRAME_INSET_W;
    public static final int       FRAME_RESIZE_W        = FRAME_INSET_W+FRAME_BORDER_W;
    public static final int       FRAME_CORNER_RESIZE_W = FRAME_RESIZE_W+FRAME_INSET_W;
    public static final int       FRAME_EXTRA_TOTAL     = 2*FRAME_BORDER_W;
    public static final int       FRAME_ACTUAL_MIN_W    = FRAME_CONTENT_MIN_W +FRAME_EXTRA_TOTAL;
    public static final int       FRAME_ACTUAL_MIN_H    = FRAME_CONTENT_MIN_H +FRAME_EXTRA_TOTAL;
    public static final Dimension FRAME_MIN_SIZE        = new Dimension(FRAME_ACTUAL_MIN_W,FRAME_ACTUAL_MIN_H);


    /*--------------- TITLEBAR ---------------*/
    public static final int       TITLEBAR_HEIGHT                  = 28;
    public static final int       TITLEBAR_BUTTON_COUNT            = 3;
    public static final int       TITLEBAR_BTN_DIM                 = IS_MAC ? 12 : 16;
    public static final Dimension TITLEBAR_BUTTON_SIZE             = new Dimension(TITLEBAR_BTN_DIM,TITLEBAR_BTN_DIM);
    public static final Dimension TITLEBAR_BUTTON_GAP              = new Dimension(8,0);
    public static final Dimension TITLEBAR_SIDE_GAP                = new Dimension(TITLEBAR_BUTTON_COUNT*(TITLEBAR_BUTTON_SIZE.width+TITLEBAR_BUTTON_GAP.width),TITLEBAR_HEIGHT);
    public static final String    TITLEBAR_MINIMISE_BUTTON_TOOLTIP = "Minimise (" + MODIFIER_KEY + " + M" + ")";
    public static final String    TITLEBAR_MAXIMISE_BUTTON_TOOLTIP = "Maximise";
    public static final String    TITLEBAR_CLOSE_BUTTON_TOOLTIP    = "Close (" + MODIFIER_KEY + " + Q" + ")";


    /*--------------- NORTH_SOUTH PANEL ---------------*/
    public static final int       NORTH_SOUTH_PANEL_HEIGHT = 50;
    public static final Dimension NORTH_SOUTH_PANEL_SIZE   = new Dimension(0,NORTH_SOUTH_PANEL_HEIGHT);

    /*--------------- NORTHPANEL ---------------*/
    public static final Dimension NORTHPANEL_VERTICAL_GAP  = new Dimension(0,5);


    /*--------------- SCROLLBAR ---------------*/
    public static final Dimension SCROLL_BUTTON_SIZE    = new Dimension(0,0);
    public static final Dimension SCROLL_THUMB_MIN_SIZE = new Dimension(0,50);
    public static final int       SCROLL_THUMB_ARC      = 10;


    /*--------------- CONNECTIONPANEL ---------------*/
    public static final int       CONNECTION_COMP_W            = 200;
    public static final int       CONNECTION_COMP_ARC          = 15;
    public static final Dimension CONNECTION_TITLE_PREF_SIZE   = new Dimension(CONNECTION_COMP_W,20);
    public static final Dimension CONNECTION_TF_SIZE           = new Dimension(CONNECTION_COMP_W,30);
    public static final Dimension CONNECTION_STATUS_PREF_SIZE  = new Dimension(CONNECTION_COMP_W,30);
    public static final Dimension CONNECTION_BUTTON_SIZE       = new Dimension(CONNECTION_COMP_W / 2,40);


    /*--------------- CHATPANEl ---------------*/
    public static final int       CHAT_COMPONENT_HEIGHT        = 70;
    public static final int       CHAT_ICON_INSET              = 10;
    public static final Dimension CHAT_ICON_SIZE               = new Dimension(CHAT_COMPONENT_HEIGHT,CHAT_COMPONENT_HEIGHT);
    public static final Dimension CHAT_ACTUAL_IMAGE_SIZE       = new Dimension(CHAT_ICON_SIZE.width-2*CHAT_ICON_INSET,CHAT_ICON_SIZE.height-2*CHAT_ICON_INSET);
    public static final Dimension CHAT_CHATNAME_DATE_GAP       = new Dimension(20,0);
    public static final Dimension CHAT_CHATNAME_AFTER_DATE_GAP = new Dimension(10,0);
    public static final int       CHAT_SCROLL_WIDTH            = 4;


    /*--------------- OPEN CHAT ---------------*/
    public static final int       OPENCHAT_NORTHPANEL_HEIGHT   = 60;
    public static final int       OPENCHAT_ICON_INSET          = 5;
    public static final Dimension OPENCHAT_ICON_SIZE           = new Dimension(OPENCHAT_NORTHPANEL_HEIGHT,OPENCHAT_NORTHPANEL_HEIGHT);
    public static final Dimension OPENCHAT_ACTUAL_IMAGE_SIZE   = new Dimension(OPENCHAT_ICON_SIZE.width-2*OPENCHAT_ICON_INSET,OPENCHAT_ICON_SIZE.height-2*OPENCHAT_ICON_INSET);
    public static final Dimension OPENCHAT_GETBACK_BUTTON_SIZE = new Dimension(OPENCHAT_NORTHPANEL_HEIGHT,OPENCHAT_NORTHPANEL_HEIGHT);
    public static final Dimension OPENCHAT_SENDMSG_BUTTON_SIZE = new Dimension(50,50);
    public static final Dimension OPENCHAT_TA_MIN_SIZE         = new Dimension(0,0);
    public static final String    OPENCHAT_TA_PLACEHOLDER_TEXT = "Write a message...";
    public static final int       OPENCHAT_MESSAGE_RIGHT_INSET = 10;
    public static final int       OPENCHAT_SCROLL_WIDTH        = 8;
    public static final int       OPENCHAT_MAX_MESSAGE_WIDTH   = FRAME_CONTENT_MIN_W - 2*FRAME_INSET_W - OPENCHAT_MESSAGE_RIGHT_INSET - OPENCHAT_SCROLL_WIDTH;


    /*--------------- SETTINGSPANEl ---------------*/
}