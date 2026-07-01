package net.coderbot.iris.spectra;

public class SpectraProgramSource {
    private final String name;
    private final String vertexPath;
    private final String fragmentPath;
    private final String vertexSource;
    private final String fragmentSource;

    public SpectraProgramSource(String name, String vertexPath, String fragmentPath, String vertexSource, String fragmentSource) {
        this.name = name;
        this.vertexPath = vertexPath;
        this.fragmentPath = fragmentPath;
        this.vertexSource = vertexSource;
        this.fragmentSource = fragmentSource;
    }

    public String getName() {
        return name;
    }

    public String getVertexPath() {
        return vertexPath;
    }

    public String getFragmentPath() {
        return fragmentPath;
    }

    public String getVertexSource() {
        return vertexSource;
    }

    public String getFragmentSource() {
        return fragmentSource;
    }

    public boolean isValid() {
        return vertexSource != null && fragmentSource != null;
    }
}
