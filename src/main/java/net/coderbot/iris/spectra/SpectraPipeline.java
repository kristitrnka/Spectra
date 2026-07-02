package net.coderbot.iris.spectra;

import org.lwjgl.opengl.GL20;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpectraPipeline {
    private int postProcessProgramId;
    private final Map<String, Integer> programs = new LinkedHashMap<String, Integer>();

    public void setPostProcessProgramId(int programId) {
        deleteProgram(this.postProcessProgramId);
        this.postProcessProgramId = programId;
        setProgram("final", programId);
        System.out.println("[Spectra/Oculus] Pipeline post-process program id=" + programId);
    }

    public void setProgram(String name, int programId) {
        Integer oldProgram = programs.get(name);
        if (oldProgram != null && oldProgram != programId) {
            deleteProgram(oldProgram);
        }

        programs.put(name, programId);
        System.out.println("[Spectra/Oculus] Pipeline program " + name + " id=" + programId);
    }

    public int getProgram(String name) {
        Integer programId = programs.get(name);
        return programId != null ? programId : 0;
    }

    public int getPostProcessProgramId() {
        return postProcessProgramId;
    }

    public int getGbuffersBasicProgramId() {
        return getProgram("gbuffers_basic");
    }

    public int getGbuffersTexturedProgramId() {
        return getProgram("gbuffers_textured");
    }

    public int getGbuffersTerrainProgramId() {
        return getProgram("gbuffers_terrain");
    }

    public int getTerrainProgramId() {
        return firstExistingProgram(
                "gbuffers_terrain",
                "gbuffers_block",
                "gbuffers_textured",
                "gbuffers_basic"
        );
    }

    public int getWaterProgramId() {
        return firstExistingProgram(
                "gbuffers_water",
                "gbuffers_block_translucent",
                "gbuffers_textured",
                "gbuffers_basic"
        );
    }

    public int getSkyBasicProgramId() {
        return firstExistingProgram(
                "gbuffers_skybasic",
                "gbuffers_basic"
        );
    }

    public int getSkyTexturedProgramId() {
        return firstExistingProgram(
                "gbuffers_skytextured",
                "gbuffers_textured",
                "gbuffers_basic"
        );
    }

    public int getEntitiesProgramId() {
        return firstExistingProgram(
                "gbuffers_entities",
                "gbuffers_entities_translucent",
                "gbuffers_textured",
                "gbuffers_basic"
        );
    }

    public int getWeatherProgramId() {
        return firstExistingProgram(
                "gbuffers_weather",
                "gbuffers_textured",
                "gbuffers_basic"
        );
    }

    public int getCloudsProgramId() {
        return firstExistingProgram(
                "gbuffers_clouds",
                "gbuffers_textured",
                "gbuffers_basic"
        );
    }

    private int firstExistingProgram(String... names) {
        for (String name : names) {
            int programId = getProgram(name);
            if (programId != 0) {
                return programId;
            }
        }

        return 0;
    }

    public boolean hasPostProcessProgram() {
        return postProcessProgramId != 0;
    }

    public boolean hasProgram(String name) {
        return getProgram(name) != 0;
    }

    public boolean hasAnyGbuffersProgram() {
        for (String name : programs.keySet()) {
            if (name.startsWith("gbuffers_") && getProgram(name) != 0) {
                return true;
            }
        }

        return false;
    }

    public void destroy() {
        java.util.HashSet<Integer> deletedPrograms = new java.util.HashSet<Integer>();

        if (postProcessProgramId != 0 && deletedPrograms.add(postProcessProgramId)) {
            deleteProgram(postProcessProgramId);
        }

        for (Integer programId : programs.values()) {
            if (programId != null && programId != 0 && deletedPrograms.add(programId)) {
                deleteProgram(programId);
            }
        }

        postProcessProgramId = 0;
        programs.clear();

        SpectraFramebuffer.destroy();

        System.out.println("[Spectra/Oculus] Pipeline destroyed");
    }

    private static void deleteProgram(int programId) {
        if (programId != 0) {
            GL20.glDeleteProgram(programId);
        }
    }
}
