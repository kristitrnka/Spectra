package net.coderbot.iris.spectra;

import org.lwjgl.opengl.GL20;

public class SpectraPipeline {
    private int postProcessProgramId;
    private int gbuffersBasicProgramId;
    private int gbuffersTexturedProgramId;
    private int gbuffersTerrainProgramId;

    public void setPostProcessProgramId(int programId) {
        deleteProgram(this.postProcessProgramId);
        this.postProcessProgramId = programId;
        System.out.println("[Spectra/Oculus] Pipeline post-process program id=" + programId);
    }

    public void setGbuffersBasicProgramId(int programId) {
        deleteProgram(this.gbuffersBasicProgramId);
        this.gbuffersBasicProgramId = programId;
        System.out.println("[Spectra/Oculus] Pipeline gbuffers_basic program id=" + programId);
    }

    public void setGbuffersTexturedProgramId(int programId) {
        deleteProgram(this.gbuffersTexturedProgramId);
        this.gbuffersTexturedProgramId = programId;
        System.out.println("[Spectra/Oculus] Pipeline gbuffers_textured program id=" + programId);
    }

    public void setGbuffersTerrainProgramId(int programId) {
        deleteProgram(this.gbuffersTerrainProgramId);
        this.gbuffersTerrainProgramId = programId;
        System.out.println("[Spectra/Oculus] Pipeline gbuffers_terrain program id=" + programId);
    }

    public int getPostProcessProgramId() {
        return postProcessProgramId;
    }

    public int getGbuffersBasicProgramId() {
        return gbuffersBasicProgramId;
    }

    public int getGbuffersTexturedProgramId() {
        return gbuffersTexturedProgramId;
    }

    public int getGbuffersTerrainProgramId() {
        return gbuffersTerrainProgramId;
    }

    public boolean hasPostProcessProgram() {
        return postProcessProgramId != 0;
    }

    public boolean hasAnyGbuffersProgram() {
        return gbuffersBasicProgramId != 0 || gbuffersTexturedProgramId != 0 || gbuffersTerrainProgramId != 0;
    }

    public void destroy() {
        deleteProgram(postProcessProgramId);
        deleteProgram(gbuffersBasicProgramId);
        deleteProgram(gbuffersTexturedProgramId);
        deleteProgram(gbuffersTerrainProgramId);

        postProcessProgramId = 0;
        gbuffersBasicProgramId = 0;
        gbuffersTexturedProgramId = 0;
        gbuffersTerrainProgramId = 0;

        SpectraFramebuffer.destroy();

        System.out.println("[Spectra/Oculus] Pipeline destroyed");
    }

    private static void deleteProgram(int programId) {
        if (programId != 0) {
            GL20.glDeleteProgram(programId);
        }
    }
}
