package net.coderbot.iris.spectra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class SpectraPostProcessor {
    private static boolean loggedMissingProgram = false;
    private static boolean loggedRender = false;
    private static int lastLoggedProgram = 0;
    private static int frameCounter = 0;
    private static final long START_TIME_NANOS = System.nanoTime();

    public static void render(int programId) {
        if (programId == 0) {
            if (!loggedMissingProgram) {
                loggedMissingProgram = true;
                System.out.println("[Spectra/Oculus] SpectraPostProcessor skipped: no active program");
            }
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.currentScreen != null) {
            return;
        }

        ScaledResolution scaledResolution = new ScaledResolution(minecraft);
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        int framebufferWidth = minecraft.displayWidth;
        int framebufferHeight = minecraft.displayHeight;
        float frameTimeCounter = (System.nanoTime() - START_TIME_NANOS) / 1000000000.0F;
        int worldTime = minecraft.world != null ? (int) (minecraft.world.getWorldTime() % 24000L) : 0;
        frameCounter++;

        int previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        int previousMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        int framebufferTexture = SpectraFramebuffer.getOrCreateColorTexture();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        try {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(false);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebufferTexture);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0D, width, height, 0.0D, -1.0D, 1.0D);

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();

            setUniform1i(programId, "colortex0", 0);
            setUniform1i(programId, "gcolor", 0);
            setUniform1i(programId, "texture", 0);
            setUniform1f(programId, "viewWidth", framebufferWidth);
            setUniform1f(programId, "viewHeight", framebufferHeight);
            setUniform1f(programId, "screenWidth", framebufferWidth);
            setUniform1f(programId, "screenHeight", framebufferHeight);
            setUniform1f(programId, "aspectRatio", framebufferWidth / (float) framebufferHeight);
            setUniform1f(programId, "frameTimeCounter", frameTimeCounter);
            setUniform1i(programId, "worldTime", worldTime);
            setUniform1i(programId, "frameCounter", frameCounter);

            GL20.glUseProgram(programId);

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0.0F, 1.0F);
            GL11.glVertex2f(0.0F, 0.0F);
            GL11.glTexCoord2f(1.0F, 1.0F);
            GL11.glVertex2f(width, 0.0F);
            GL11.glTexCoord2f(1.0F, 0.0F);
            GL11.glVertex2f(width, height);
            GL11.glTexCoord2f(0.0F, 0.0F);
            GL11.glVertex2f(0.0F, height);
            GL11.glEnd();

            GL20.glUseProgram(0);

            if (!loggedRender || lastLoggedProgram != programId) {
                loggedRender = true;
                lastLoggedProgram = programId;
                System.out.println("[Spectra/Oculus] SpectraPostProcessor rendered fullscreen pass with program id=" + programId + " colortex0=" + framebufferTexture + " size=" + framebufferWidth + "x" + framebufferHeight);
            }
        } catch (Throwable t) {
            System.out.println("[Spectra/Oculus] SpectraPostProcessor render crashed");
            t.printStackTrace();
        } finally {
            GL20.glUseProgram(previousProgram);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
            GL13.glActiveTexture(previousActiveTexture);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDepthMask(true);

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();

            GL11.glMatrixMode(previousMatrixMode);
            GL11.glPopAttrib();
        }
    }

    private static void setUniform1i(int programId, String name, int value) {
        int location = GL20.glGetUniformLocation(programId, name);
        if (location >= 0) {
            GL20.glUseProgram(programId);
            GL20.glUniform1i(location, value);
        }
    }

    private static void setUniform1f(int programId, String name, float value) {
        int location = GL20.glGetUniformLocation(programId, name);
        if (location >= 0) {
            GL20.glUseProgram(programId);
            GL20.glUniform1f(location, value);
        }
    }
}
