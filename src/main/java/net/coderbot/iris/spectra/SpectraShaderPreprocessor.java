package net.coderbot.iris.spectra;

public class SpectraShaderPreprocessor {
    private static boolean loggedOnce = false;

    public static String preprocess(String path, String source) {
        if (source == null) {
            return null;
        }

        String normalized = source.replace("\r\n", "\n").replace("\r", "\n");

        normalized = hoistExtensions(path, normalized);
        normalized = injectDefaultDefines(normalized);
        normalized = sanitizePreprocessorLines(path, normalized);

        if (!loggedOnce) {
            loggedOnce = true;
            System.out.println("[Spectra/Oculus] SpectraShaderPreprocessor enabled");
        }

        return normalized;
    }

    private static String hoistExtensions(String path, String source) {
        String[] lines = source.split("\n", -1);
        java.util.List<String> extensions = new java.util.ArrayList<String>();
        java.util.List<String> body = new java.util.ArrayList<String>();

        String versionLine = null;
        boolean movedAny = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (versionLine == null && trimmed.startsWith("#version")) {
                versionLine = line;
                continue;
            }

            if (trimmed.startsWith("#extension")) {
                if (!extensions.contains(line)) {
                    extensions.add(line);
                }
                movedAny = true;
                continue;
            }

            body.add(line);
        }

        StringBuilder out = new StringBuilder(source.length() + 256);

        if (versionLine != null) {
            out.append(versionLine).append('\n');
        } else {
            out.append("#version 120\n");
        }

        for (String extension : extensions) {
            out.append(extension).append('\n');
        }

        for (String line : body) {
            out.append(line).append('\n');
        }

        if (movedAny) {
            System.out.println("[Spectra/Oculus] Preprocessor hoisted #extension lines in " + path + ": " + extensions.size());
        }

        return out.toString();
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

        int insertPos = source.indexOf('\n', versionIndex);
        if (insertPos < 0) {
            return source + defines;
        }

        insertPos++;

        while (insertPos < source.length()) {
            int lineEnd = source.indexOf('\n', insertPos);
            if (lineEnd < 0) {
                lineEnd = source.length();
            }

            String line = source.substring(insertPos, lineEnd).trim();

            if (line.startsWith("#extension")) {
                insertPos = lineEnd < source.length() ? lineEnd + 1 : lineEnd;
                continue;
            }

            if (line.length() == 0 || line.startsWith("//")) {
                insertPos = lineEnd < source.length() ? lineEnd + 1 : lineEnd;
                continue;
            }

            break;
        }

        return source.substring(0, insertPos) + defines + source.substring(insertPos);
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
                    System.out.println("[Spectra/Oculus] Preprocessor sanitized unsafe " + keyword + " in " + path + ": " + expression);
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
