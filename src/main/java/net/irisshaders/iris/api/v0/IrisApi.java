package net.irisshaders.iris.api.v0;

public final class IrisApi {
    private static final IrisApi INSTANCE = new IrisApi();
    private final Config config = new Config();

    private IrisApi() {
    }

    public static IrisApi getInstance() {
        return INSTANCE;
    }

    public boolean isShaderPackInUse() {
        return false;
    }

    public boolean isRenderingShadowPass() {
        return false;
    }

    public Config getConfig() {
        return config;
    }

    public static final class Config {
        public void setShadersEnabledAndApply(boolean enabled) {
        }
    }
}
