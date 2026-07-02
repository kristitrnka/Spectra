package net.coderbot.iris.spectra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class SpectraFramebuffer {
    private static int colorTextureId = 0;
    private static int width = -1;
    private static int height = -1;
    private static boolean loggedCopy = false;

    public static int getOrCreateColorTexture() {
        Minecraft minecraft = Minecraft.getMinecraft();

        int targetWidth = minecraft.displayWidth;
        int targetHeight = minecraft.displayHeight;

        if (colorTextureId == 0 || width != targetWidth || height != targetHeight) {
            destroy();
            createTexture(targetWidth, targetHeight);
        }

        copyMinecraftFramebufferIntoTexture();
        return colorTextureId != 0 ? colorTextureId : minecraft.getFramebuffer().framebufferTexture;
    }

    private static void createTexture(int targetWidth, int targetHeight) {
        width = targetWidth;
        height = targetHeight;

        colorTextureId = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTextureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA8,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                (java.nio.ByteBuffer) null
        );

        System.out.println("[Spectra/Oculus] SpectraFramebuffer created copy texture " + width + "x" + height + " texture=" + colorTextureId);
    }

    private static void copyMinecraftFramebufferIntoTexture() {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (colorTextureId == 0 || width <= 0 || height <= 0) {
            return;
        }

        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        try {
            minecraft.getFramebuffer().bindFramebuffer(false);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTextureId);
            GL11.glCopyTexSubImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    0,
                    0,
                    width,
                    height
            );

            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, 0);

            if (!loggedCopy) {
                loggedCopy = true;
                System.out.println("[Spectra/Oculus] SpectraFramebuffer copied Minecraft framebuffer into texture=" + colorTextureId);
            }
        } catch (Throwable t) {
            System.out.println("[Spectra/Oculus] SpectraFramebuffer copy failed, falling back to vanilla framebuffer texture");
            t.printStackTrace();
        } finally {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
        }
    }

    public static void destroy() {
        if (colorTextureId != 0) {
            GL11.glDeleteTextures(colorTextureId);
            colorTextureId = 0;
        }

        width = -1;
        height = -1;
        loggedCopy = false;
    }
}
