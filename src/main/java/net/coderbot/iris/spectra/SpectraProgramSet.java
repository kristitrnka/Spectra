package net.coderbot.iris.spectra;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpectraProgramSet {
    private final Map<String, SpectraProgramSource> programs = new LinkedHashMap<>();

    public void addProgram(SpectraProgramSource program) {
        if (program != null && program.isValid()) {
            programs.put(program.getName(), program);
            System.out.println("[Spectra/Oculus] ProgramSet found valid program: " + program.getName());
        }
    }

    public SpectraProgramSource get(String name) {
        return programs.get(name);
    }

    public SpectraProgramSource getFirstPostProcessProgram() {
        SpectraProgramSource finalProgram = programs.get("final");
        if (finalProgram != null) {
            return finalProgram;
        }

        for (int i = 0; i < 16; i++) {
            String name = i == 0 ? "composite" : "composite" + i;
            SpectraProgramSource program = programs.get(name);

            if (program != null) {
                return program;
            }
        }

        return null;
    }

    public SpectraProgramSource getFirstGbuffersProgram() {
        String[] names = new String[] {
                "gbuffers_basic",
                "gbuffers_textured",
                "gbuffers_textured_lit",
                "gbuffers_terrain"
        };

        for (String name : names) {
            SpectraProgramSource program = programs.get(name);

            if (program != null) {
                return program;
            }
        }

        return null;
    }

    public int size() {
        return programs.size();
    }
}
