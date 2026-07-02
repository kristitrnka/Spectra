package net.coderbot.iris.features;

import java.util.List;

public enum FeatureFlags {
    SEPARATE_HARDWARE_SAMPLERS,
    COMPUTE_SHADERS,
    CUSTOM_IMAGES,
    SSBO,
    TESSELLATION_SHADERS,
    GEOMETRY_SHADERS,
    UNKNOWN;

    public boolean isInvalid() {
        return this == UNKNOWN;
    }

    public boolean isUsable() {
        return true;
    }

    public String getHumanReadableName() {
        return name();
    }

    public static boolean isInvalid(String value) {
        return getValue(value) == UNKNOWN;
    }

    public static FeatureFlags getValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }

        String normalized = value.trim().toUpperCase()
            .replace('-', '_')
            .replace(' ', '_');

        for (FeatureFlags flag : values()) {
            if (flag.name().equals(normalized)) {
                return flag;
            }
        }

        return UNKNOWN;
    }

    public static String getInvalidStatus(List<FeatureFlags> flags) {
        if (flags == null || flags.isEmpty()) {
            return "";
        }
        return flags.toString();
    }
}
