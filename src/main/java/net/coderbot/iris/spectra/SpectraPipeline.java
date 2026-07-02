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
