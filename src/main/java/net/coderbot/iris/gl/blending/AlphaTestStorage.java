package net.coderbot.iris.gl.blending;

/**
 * 1.12.2 legacy adapter.
 *
 * Modern Iris stores/restores Blaze3D alpha test state through mixin accessors.
 * MC 1.12.2 does not have com.mojang.blaze3d GlStateManager, so this is kept
 * as a no-op bridge while porting the original shaderpack/pipeline code.
 */
public class AlphaTestStorage {
    private static AlphaTest savedAlphaTest;

    public static void saveAlphaTest() {
        savedAlphaTest = null;
    }

    public static void restoreAlphaTest() {
        savedAlphaTest = null;
    }

    public static void overrideAlphaTest(AlphaTest alphaTest) {
        savedAlphaTest = alphaTest;
    }
}
