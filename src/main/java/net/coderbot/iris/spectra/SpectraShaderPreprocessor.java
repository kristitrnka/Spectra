package net.coderbot.iris.spectra;

public class SpectraShaderPreprocessor {
    private static boolean loggedOnce = false;

    public static String preprocess(String path, String source) {
        if (source == null) {
            return null;
        }

        String normalized = source.replace("\r\n", "\n").replace("\r", "\n");

        normalized = injectDefaultDefines(normalized);
        normalized = sanitizePreprocessorLines(path, normalized);

        if (!loggedOnce) {
            loggedOnce = true;
            System.out.println("[Spectra/Oculus] SpectraShaderPreprocessor enabled");
        }

        return normalized;
    }

    private static String injectDefaultDefines(String source) {
        String defines =
                "\n" +
                "#ifndef MC_VERSION\n" +
                "#define MC_VERSION 11202\n" +
                "#endif\n" +
                "#ifndef IS_IRIS\n" +
                "#define IS_IRIS 1\n" +
                "#endif\n" +
                "#ifndef IRIS_VERSION\n" +
                "#define IRIS_VERSION 10800\n" +
                "#endif\n" +
                "#ifndef OS_MAC\n" +
                "#define OS_MAC 1\n" +
                "#endif\n" +
                "#ifndef VERTEX_SHADER\n" +
                "#define VERTEX_SHADER 0\n" +
                "#endif\n" +
                "#ifndef FRAGMENT_SHADER\n" +
                "#define FRAGMENT_SHADER 0\n" +
                "#endif\n" +
                "\n";

        int versionIndex = source.indexOf("#version");
        if (versionIndex < 0) {
            return "#version 120\n" + defines + source;
        }

        int endOfVersionLine = source.indexOf('\n', versionIndex);
        if (endOfVersionLine < 0) {
            return source + defines;
        }

        return source.substring(0, endOfVersionLine + 1) + defines + source.substring(endOfVersionLine + 1);
    }

    private static String sanitizePreprocessorLines(String path, String source) {
        String[] lines = source.split("\n", -1);
        StringBuilder result = new StringBuilder(source.length() + 512);

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("#if ") || trimmed.startsWith("#elif ")) {
                String keyword = trimmed.startsWith("#elif ") ? "#elif" : "#if";
                String expression = trimmed.substring(keyword.length()).trim();

                expression = expression.replaceAll("defined\\s+([A-Za-z_][A-Za-z0-9_]*)", "defined($1)");
                expression = expression.replaceAll("!\\s+defined\\s*\\(", "!defined(");

                if (looksUnsafeForAppleGlsl(expression)) {
                    result.append(keyword).append(" 1");
                    result.append(" // Spectra sanitized from: ").append(expression.replace('\t', ' '));
                    result.append('\n');
                    continue;
                }

                result.append(keyword).append(' ').append(expression).append('\n');
                continue;
            }

            if (trimmed.startsWith("#elifdef ")) {
                String name = trimmed.substring("#elifdef".length()).trim();
                result.append("#elif defined(").append(name).append(")\n");
                continue;
            }

            if (trimmed.startsWith("#elifndef ")) {
                String name = trimmed.substring("#elifndef".length()).trim();
                result.append("#elif !defined(").append(name).append(")\n");
                continue;
            }

            if (trimmed.startsWith("#ifdef ")) {
                String name = trimmed.substring("#ifdef".length()).trim();
                result.append("#if defined(").append(name).append(")\n");
                continue;
            }

            if (trimmed.startsWith("#ifndef ")) {
                String name = trimmed.substring("#ifndef".length()).trim();
                result.append("#if !defined(").append(name).append(")\n");
                continue;
            }

            result.append(line).append('\n');
        }

        return result.toString();
    }

    private static boolean looksUnsafeForAppleGlsl(String expression) {
        if (expression.length() == 0) {
            return true;
        }

        if (expression.indexOf('[') >= 0 || expression.indexOf(']') >= 0) {
            return true;
        }

        if (expression.indexOf('{') >= 0 || expression.indexOf('}') >= 0) {
            return true;
        }

        if (expression.indexOf(';') >= 0) {
            return true;
        }

        if (expression.indexOf('#') >= 0) {
            return true;
        }

        return false;
    }
}
