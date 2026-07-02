package net.coderbot.iris.gl.blending;

/**
 * 1.12.2 legacy no-op adapter while the original Oculus pipeline is being ported.
 */
public class BlendModeStorage {
    private static BlendMode savedBlendMode;

    public static void saveBlend() {
        savedBlendMode = null;
    }

    public static void restoreBlend() {
        savedBlendMode = null;
    }

    public static void overrideBlend(BlendMode blendMode) {
        savedBlendMode = blendMode;
    }

    public static void overrideBufferBlend(int drawBuffer, BlendMode blendMode) {
        savedBlendMode = blendMode;
    }
}
