package managers;

import java.awt.*;

public class SystemInfoManager {
    public static final boolean   IS_MAC       = System.getProperty("os.name").toLowerCase().contains("mac");
    public static final String    MODIFIER_KEY = IS_MAC ? "⌘" : "CTRL";
    public static final Dimension SCREEN_SIZE  = Toolkit.getDefaultToolkit().getScreenSize();
}