package net.coderbot.iris.spectra.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobalSpectraTerrain {
    private int spectraPreviousTerrainProgram = 0;
    private boolean spectraBoundTerrainProgram = false;

    private static boolean spectraLoggedTerrain = false;
    private static boolean spectraLoggedWater = false;
    private static int spectraFrameCounter = 0;
    private static final long SPECTRA_START_TIME_NANOS = System.nanoTime();

    @Inject(method = "func_174977_a", at = @At("HEAD"), remap = false)
    private void spectra$bindTerrainShader(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn, CallbackInfoReturnable<Integer> cir) {
        int programId = spectra$getProgramForLayer(blockLayerIn);

        if (programId == 0) {
            return;
        }

        spectraPreviousTerrainProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        spectraBoundTerrainProgram = true;

        GL20.glUseProgram(programId);
        spectra$setupCommonUniforms(programId);

        if (blockLayerIn == BlockRenderLayer.TRANSLUCENT) {
            if (!spectraLoggedWater) {
                spectraLoggedWater = true;
                System.out.println("[Spectra/Oculus] Bound water pipeline shader for renderBlockLayer " + blockLayerIn + " id=" + programId);
            }
        } else {
            if (!spectraLoggedTerrain) {
                spectraLoggedTerrain = true;
                System.out.println("[Spectra/Oculus] Bound terrain pipeline shader for renderBlockLayer " + blockLayerIn + " id=" + programId);
            }
        }
    }

    @Inject(method = "func_174977_a", at = @At("RETURN"), remap = false)
    private void spectra$restoreTerrainShader(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn, CallbackInfoReturnable<Integer> cir) {
        if (!spectraBoundTerrainProgram) {
            return;
        }

        GL20.glUseProgram(spectraPreviousTerrainProgram);
        spectraPreviousTerrainProgram = 0;
        spectraBoundTerrainProgram = false;
    }

    private static void spectra$setupCommonUniforms(int programId) {
        Minecraft minecraft = Minecraft.getMinecraft();

        int width = Math.max(1, minecraft.displayWidth);
        int height = Math.max(1, minecraft.displayHeight);

        float frameTimeCounter = (System.nanoTime() - SPECTRA_START_TIME_NANOS) / 1000000000.0F;
        int worldTime = minecraft.world != null ? (int) (minecraft.world.getWorldTime() % 24000L) : 0;

        spectraFrameCounter++;

        spectra$uniform1i(programId, "texture", 0);
        spectra$uniform1i(programId, "gtexture", 0);
        spectra$uniform1i(programId, "colortex0", 0);

        spectra$uniform1f(programId, "viewWidth", width);
        spectra$uniform1f(programId, "viewHeight", height);
        spectra$uniform1f(programId, "screenWidth", width);
        spectra$uniform1f(programId, "screenHeight", height);
        spectra$uniform1f(programId, "aspectRatio", (float) width / (float) height);
        spectra$uniform1f(programId, "frameTimeCounter", frameTimeCounter);

        spectra$uniform1i(programId, "worldTime", worldTime);
        spectra$uniform1i(programId, "frameCounter", spectraFrameCounter);
    }

    private static void spectra$uniform1i(int programId, String name, int value) {
        int location = GL20.glGetUniformLocation(programId, name);
        if (location >= 0) {
            GL20.glUniform1i(location, value);
        }
    }

    private static void spectra$uniform1f(int programId, String name, float value) {
        int location = GL20.glGetUniformLocation(programId, name);
        if (location >= 0) {
            GL20.glUniform1f(location, value);
        }
    }

    private static int spectra$getProgramForLayer(BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.TRANSLUCENT) {
            int waterProgram = Iris.SpectraShaderManager.getWaterPipelineProgramId();
            if (waterProgram != 0) {
                return waterProgram;
            }
        }

        if (layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.TRANSLUCENT) {
            return Iris.SpectraShaderManager.getTerrainPipelineProgramId();
        }

        return 0;
    }
}
