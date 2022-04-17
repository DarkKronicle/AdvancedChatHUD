package io.github.darkkronicle.advancedchathud.util;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ScissorUtil {

    private ScissorUtil() {}

    public static void applyScissorBox(int x, int y, int width, int height) {
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(x, y, width, height);
    }

    public static void applyScissor(int x1, int y1, int x2, int y2) {
            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(x1, y1, x2 - x1, y2 - y1);
    }

    public static void resetScissor() {
        GlStateManager._disableScissorTest();
    }
}
