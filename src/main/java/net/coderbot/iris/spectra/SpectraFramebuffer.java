package net.coderbot.iris.spectra;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class SpectraFramebuffer {
    private static int framebufferId = 0;
    private static int colorTextureId = 0;
    private static int width = -1;
    private static int height = -1;

    public static int getOrCreateColorTexture() {
        Minecraft minecraft = Minecraft.getMinecraft();

        int targetWidth = minecraft.displayWidth;
        int targetHeight = minecraft.displayHeight;

        if (framebufferId != 0 && colorTextureId != 0 && width == targetWidth && height == targetHeight) {
            copyMinecraftFramebufferIntoTexture();
            return colorTextureId;
        }

        destroy();

        width = targetWidth;
        height = targetHeight;

        framebufferId = GL30.glGenFramebuffers();
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

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        GL30.glFramebufferTexture2D(
                GL30.GL_FRAMEBUFFER,
                GL30.GL_COLOR_ATTACHMENT0,
                GL11.GL_TEXTURE_2D,
                colorTextureId,
                0
        );

        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("[Spectra/Oculus] SpectraFramebuffer incomplete: " + status);
            destroy();
            return minecraft.getFramebuffer().framebufferTexture;
        }

        System.out.println("[Spectra/Oculus] SpectraFramebuffer created " + width + "x" + height + " texture=" + colorTextureId + " fbo=" + framebufferId);

        copyMinecraftFramebufferIntoTexture();
        return colorTextureId;
    }

    private static void copyMinecraftFramebufferIntoTexture() {
        Minecraft minecraft = Minecraft.getMinecraft();

        int previousReadFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousDrawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

        try {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, minecraft.getFramebuffer().framebufferObject);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebufferId);

            GL30.glBlitFramebuffer(
                    0,
                    0,
                    width,
                    height,
                    0,
                    0,
                    width,
                    height,
                    GL11.GL_COLOR_BUFFER_BIT,
                    GL11.GL_NEAREST
            );
        } catch (Throwable t) {
            System.out.println("[Spectra/Oculus] SpectraFramebuffer copy failed");
            t.printStackTrace();
        } finally {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousReadFramebuffer);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDrawFramebuffer);
        }
    }

    public static void destroy() {
        if (framebufferId != 0) {
            GL30.glDeleteFramebuffers(framebufferId);
            framebufferId = 0;
        }

        if (colorTextureId != 0) {
            GL11.glDeleteTextures(colorTextureId);
            colorTextureId = 0;
        }

        width = -1;
        height = -1;
    }
}
