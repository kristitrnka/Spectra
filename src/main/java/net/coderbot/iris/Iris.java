package net.coderbot.iris;

import net.minecraftforge.fml.common.Mod;

@Mod(
        modid = "oculus",
        name = "Spectra",
        version = "0.0.10",
        clientSideOnly = true
)
public class Iris {
    public static final String MODID = "oculus";

    @Mod.EventHandler
    public void init(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(GuiEvents.class);
        System.out.println("[Spectra/Oculus] Loaded baseline mod!");
    }

    public static class GuiEvents {
        private static final int SHADER_PACKS_BUTTON_ID = 729431;

        private static boolean isVideoSettingsScreen(net.minecraft.client.gui.GuiScreen gui) {
            if (gui instanceof net.minecraft.client.gui.GuiVideoSettings) {
                return true;
            }

            String className = gui.getClass().getName().toLowerCase(java.util.Locale.ROOT);
            return className.contains("video")
                    || className.contains("sodium")
                    || className.contains("vintagium")
                    || className.contains("option");
        }

        @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
        public static void onInitGui(net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post event) {
            if (!isVideoSettingsScreen(event.getGui())) {
                return;
            }

            for (net.minecraft.client.gui.GuiButton button : event.getButtonList()) {
                if (button.id == SHADER_PACKS_BUTTON_ID) {
                    return;
                }
            }

            int buttonWidth = 150;
            int buttonHeight = 20;
            int x = event.getGui().width / 2 - buttonWidth / 2;
            int y = event.getGui().height - 28;

            event.getButtonList().add(new net.minecraft.client.gui.GuiButton(
                    SHADER_PACKS_BUTTON_ID,
                    x,
                    y,
                    buttonWidth,
                    buttonHeight,
                    "Shader Packs..."
            ));
        }

        @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
        public static void onActionPerformed(net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post event) {
            if (!isVideoSettingsScreen(event.getGui())) {
                return;
            }

            if (event.getButton().id != SHADER_PACKS_BUTTON_ID) {
                return;
            }

            final net.minecraft.client.gui.GuiScreen parent = event.getGui();
            net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new ShaderPackScreen(parent));
        }

        @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
        public static void onRenderOverlay(net.minecraftforge.client.event.RenderGameOverlayEvent.Post event) {
            if (event.getType() != net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ALL) {
                return;
            }

            SpectraShaderManager.renderTestOverlay();
        }
    }

    public static class ShaderPackScreen extends net.minecraft.client.gui.GuiScreen {
        private final net.minecraft.client.gui.GuiScreen parent;
        private java.util.List<java.io.File> shaderPacks = new java.util.ArrayList<>();
        private String selectedShaderPack = "OFF";
        private String appliedShaderPack = "OFF";

        public ShaderPackScreen(net.minecraft.client.gui.GuiScreen parent) {
            this.parent = parent;
        }

        private java.io.File getShaderPacksFolder() {
            return new java.io.File(
                    new java.io.File(System.getProperty("user.dir")),
                    "shaderpacks"
            );
        }

        private java.io.File getConfigFile() {
            java.io.File configFolder = new java.io.File(
                    new java.io.File(System.getProperty("user.dir")),
                    "config/spectra"
            );

            if (!configFolder.exists()) {
                configFolder.mkdirs();
            }

            return new java.io.File(configFolder, "selected_shaderpack.txt");
        }

        private void loadAppliedShaderPack() {
            java.io.File configFile = this.getConfigFile();

            if (!configFile.exists()) {
                this.appliedShaderPack = "OFF";
                this.selectedShaderPack = "OFF";
                return;
            }

            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(configFile));
                String line = reader.readLine();
                reader.close();

                if (line == null || line.trim().isEmpty()) {
                    line = "OFF";
                }

                this.appliedShaderPack = line.trim();
                this.selectedShaderPack = this.appliedShaderPack;
            } catch (Exception e) {
                e.printStackTrace();
                this.appliedShaderPack = "OFF";
                this.selectedShaderPack = "OFF";
            }
        }

        private void saveSelectedShaderPack() {
            java.io.File configFile = this.getConfigFile();

            try {
                java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(configFile, false));
                writer.println(this.selectedShaderPack);
                writer.close();
                this.appliedShaderPack = this.selectedShaderPack;
                System.out.println("[Spectra/Oculus] Applied shaderpack: " + this.appliedShaderPack);
                SpectraShaderManager.loadSelectedShaderPack(this.appliedShaderPack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void initGui() {
            this.buttonList.clear();
            this.shaderPacks.clear();

            this.loadAppliedShaderPack();
            java.io.File folder = this.getShaderPacksFolder();

            if (!folder.exists()) {
                folder.mkdirs();
            }

            java.io.File[] files = folder.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    String name = file.getName().toLowerCase(java.util.Locale.ROOT);

                    if (file.isDirectory() || name.endsWith(".zip")) {
                        this.shaderPacks.add(file);
                    }
                }
            }

            int y = 60;

            this.buttonList.add(new net.minecraft.client.gui.GuiButton(
                    100,
                    this.width / 2 - 100,
                    y,
                    200,
                    20,
                    "OFF"
            ));

            y += 24;

            for (int i = 0; i < this.shaderPacks.size(); i++) {
                java.io.File pack = this.shaderPacks.get(i);

                this.buttonList.add(new net.minecraft.client.gui.GuiButton(
                        1000 + i,
                        this.width / 2 - 100,
                        y,
                        200,
                        20,
                        pack.getName()
                ));

                y += 24;

                if (y > this.height - 70) {
                    break;
                }
            }

            this.buttonList.add(new net.minecraft.client.gui.GuiButton(
                    1,
                    this.width / 2 - 210,
                    this.height - 28,
                    100,
                    20,
                    "Open Folder"
            ));

            this.buttonList.add(new net.minecraft.client.gui.GuiButton(
                    3,
                    this.width / 2 - 105,
                    this.height - 28,
                    100,
                    20,
                    "Configure"
            ));

            this.buttonList.add(new net.minecraft.client.gui.GuiButton(
                    2,
                    this.width / 2,
                    this.height - 28,
                    100,
                    20,
                    "Apply"
            ));

            this.buttonList.add(new net.minecraft.client.gui.GuiButton(
                    0,
                    this.width / 2 + 105,
                    this.height - 28,
                    100,
                    20,
                    "Done"
            ));
        }

        @Override
        protected void actionPerformed(net.minecraft.client.gui.GuiButton button) {
            if (button.id == 0) {
                net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(this.parent);
                return;
            }

            if (button.id == 1) {
                java.io.File folder = this.getShaderPacksFolder();

                if (!folder.exists()) {
                    folder.mkdirs();
                }

                try {
                    java.awt.Desktop.getDesktop().open(folder);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return;
            }

            if (button.id == 2) {
                this.saveSelectedShaderPack();
                return;
            }

            if (button.id == 3) {
                net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new ShaderPackConfigScreen(this, this.selectedShaderPack));
                return;
            }

            if (button.id == 100) {
                this.selectedShaderPack = "OFF";
                return;
            }

            if (button.id >= 1000) {
                int index = button.id - 1000;

                if (index >= 0 && index < this.shaderPacks.size()) {
                    this.selectedShaderPack = this.shaderPacks.get(index).getName();
                }
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();

            this.drawCenteredString(
                    this.fontRenderer,
                    "Shader Packs",
                    this.width / 2,
                    20,
                    0xFFFFFF
            );

            this.drawCenteredString(
                    this.fontRenderer,
                    "Selected: " + this.selectedShaderPack,
                    this.width / 2,
                    40,
                    0xAAAAAA
            );

            this.drawCenteredString(
                    this.fontRenderer,
                    "Applied: " + this.appliedShaderPack,
                    this.width / 2,
                    52,
                    0xAAAAAA
            );

            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public static class SpectraShaderManager {
        private static String activeShaderPack = "OFF";
        private static boolean shaderPackLoaded = false;
        private static int lastCompileTestProgram = 0;
        private static net.coderbot.iris.spectra.SpectraPipeline activePipeline = null;
        private static int overlayTestProgram = 0;
        private static boolean renderTestLogged = false;
        private static boolean postProcessTintEnabled = false;

        public static void loadSelectedShaderPack(String shaderPackName) {
            activeShaderPack = shaderPackName == null || shaderPackName.trim().isEmpty()
                    ? "OFF"
                    : shaderPackName.trim();

            shaderPackLoaded = false;

            if ("OFF".equalsIgnoreCase(activeShaderPack)) {
                System.out.println("[Spectra/Oculus] Shaders disabled");
                return;
            }

            java.io.File shaderPacksFolder = new java.io.File(
                    new java.io.File(System.getProperty("user.dir")),
                    "shaderpacks"
            );

            java.io.File shaderPack = new java.io.File(shaderPacksFolder, activeShaderPack);

            System.out.println("[Spectra/Oculus] Loading shaderpack: " + shaderPack.getAbsolutePath());

            if (!shaderPack.exists()) {
                System.out.println("[Spectra/Oculus] Shaderpack not found: " + activeShaderPack);
                return;
            }

            if (shaderPack.isDirectory()) {
                loadDirectoryShaderPack(shaderPack);
                return;
            }

            if (shaderPack.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".zip")) {
                loadZipShaderPack(shaderPack);
                return;
            }

            System.out.println("[Spectra/Oculus] Unsupported shaderpack type: " + shaderPack.getName());
        }

        private static void loadDirectoryShaderPack(java.io.File shaderPack) {
            java.io.File shadersFolder = new java.io.File(shaderPack, "shaders");

            if (!shadersFolder.exists() || !shadersFolder.isDirectory()) {
                System.out.println("[Spectra/Oculus] Missing shaders/ folder in directory shaderpack");
                return;
            }

            java.util.List<String> shaderFiles = new java.util.ArrayList<>();
            collectShaderFilesFromDirectory(shadersFolder, shaderFiles, shadersFolder.getAbsolutePath().length() + 1);

            finishShaderPackLoad(shaderPack, shaderFiles);
        }

        private static void collectShaderFilesFromDirectory(java.io.File folder, java.util.List<String> shaderFiles, int prefixLength) {
            java.io.File[] files = folder.listFiles();

            if (files == null) {
                return;
            }

            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    collectShaderFilesFromDirectory(file, shaderFiles, prefixLength);
                    continue;
                }

                String name = file.getName().toLowerCase(java.util.Locale.ROOT);

                if (name.endsWith(".vsh") || name.endsWith(".fsh") || name.endsWith(".glsl")) {
                    String relativePath = file.getAbsolutePath().substring(prefixLength).replace(java.io.File.separatorChar, '/');
                    shaderFiles.add("shaders/" + relativePath);
                }
            }
        }

        private static void loadZipShaderPack(java.io.File shaderPack) {
            java.util.List<String> shaderFiles = new java.util.ArrayList<>();
            boolean foundShadersFolder = false;

            try {
                java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(shaderPack);
                java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    java.util.zip.ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    String lowerName = name.toLowerCase(java.util.Locale.ROOT);

                    if (lowerName.startsWith("shaders/")) {
                        foundShadersFolder = true;
                    }

                    if (!entry.isDirectory() && lowerName.startsWith("shaders/") &&
                            (lowerName.endsWith(".vsh") || lowerName.endsWith(".fsh") || lowerName.endsWith(".glsl"))) {
                        shaderFiles.add(name);
                    }
                }

                zipFile.close();
            } catch (Exception e) {
                System.out.println("[Spectra/Oculus] Failed to read shaderpack zip: " + shaderPack.getName());
                e.printStackTrace();
                return;
            }

            if (!foundShadersFolder) {
                System.out.println("[Spectra/Oculus] Missing shaders/ folder in zip shaderpack");
                return;
            }

            finishShaderPackLoad(shaderPack, shaderFiles);
        }

        private static void finishShaderPackLoad(java.io.File shaderPack, java.util.List<String> shaderFiles) {
            if (shaderFiles.isEmpty()) {
                System.out.println("[Spectra/Oculus] Shaderpack has shaders/ folder, but no .vsh/.fsh/.glsl files were found");
                return;
            }

            shaderPackLoaded = true;
            System.out.println("[Spectra/Oculus] Shaderpack activated: " + activeShaderPack);
            System.out.println("[Spectra/Oculus] Found shader files: " + shaderFiles.size());

            int limit = Math.min(shaderFiles.size(), 12);
            for (int i = 0; i < limit; i++) {
                System.out.println("[Spectra/Oculus] Shader file: " + shaderFiles.get(i));
            }

            if (shaderFiles.size() > limit) {
                System.out.println("[Spectra/Oculus] ... and " + (shaderFiles.size() - limit) + " more shader files");
            }

            System.out.println("[Spectra/Oculus] NOTE: shaderpack is selected and parsed; using Spectra lite ProgramSet pipeline now");
            compileFirstAvailableProgram(shaderPack, shaderFiles);
        }

        private static void compileFirstAvailableProgram(java.io.File shaderPack, java.util.List<String> shaderFiles) {
            net.coderbot.iris.spectra.SpectraProgramSet programSet = buildSpectraProgramSet(shaderPack, shaderFiles);
            System.out.println("[Spectra/Oculus] Spectra ProgramSet loaded programs: " + programSet.size());

            net.coderbot.iris.spectra.SpectraProgramSource postProgram = programSet.getFirstPostProcessProgram();
            if (postProgram != null) {
                System.out.println("[Spectra/Oculus] Spectra selected post-process program: " + postProgram.getName());
                int postProgramId = compileProgramSource(shaderPack, postProgram);
                if (postProgramId != 0) {
                    if (activePipeline == null) {
                        activePipeline = new net.coderbot.iris.spectra.SpectraPipeline();
                    }
                    activePipeline.setPostProcessProgramId(postProgramId);
                    compileGbuffersPrograms(shaderPack, programSet);
                }
                return;
            }

            net.coderbot.iris.spectra.SpectraProgramSource gbuffersProgram = programSet.getFirstGbuffersProgram();
            if (gbuffersProgram != null) {
                System.out.println("[Spectra/Oculus] Spectra selected gbuffers fallback program: " + gbuffersProgram.getName());
                compileProgramSource(shaderPack, gbuffersProgram);
                return;
            }

            System.out.println("[Spectra/Oculus] Spectra ProgramSet did not find a valid .vsh/.fsh program pair");
        }

        private static net.coderbot.iris.spectra.SpectraProgramSet buildSpectraProgramSet(java.io.File shaderPack, java.util.List<String> shaderFiles) {
            net.coderbot.iris.spectra.SpectraProgramSet programSet = new net.coderbot.iris.spectra.SpectraProgramSet();

            for (String vertexPath : shaderFiles) {
                if (!vertexPath.toLowerCase(java.util.Locale.ROOT).endsWith(".vsh")) {
                    continue;
                }

                String fragmentPath = vertexPath.substring(0, vertexPath.length() - 4) + ".fsh";

                if (!shaderFiles.contains(fragmentPath)) {
                    continue;
                }

                String programName = vertexPath.substring(0, vertexPath.length() - 4);
                if (programName.startsWith("shaders/")) {
                    programName = programName.substring("shaders/".length());
                }

                programSet.addProgram(new net.coderbot.iris.spectra.SpectraProgramSource(
                        programName,
                        vertexPath,
                        fragmentPath
                ));
            }

            return programSet;
        }

        private static void compileGbuffersPrograms(java.io.File shaderPack, net.coderbot.iris.spectra.SpectraProgramSet programSet) {
            if (activePipeline == null) {
                activePipeline = new net.coderbot.iris.spectra.SpectraPipeline();
            }

            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_basic");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_textured");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_terrain");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_block");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_block_translucent");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_water");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_hand");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_hand_water");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_entities");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_entities_translucent");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_particles");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_particles_translucent");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_skybasic");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_skytextured");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_clouds");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_weather");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_armor_glint");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_spidereyes");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_beaconbeam");
            compileNamedPipelineProgram(shaderPack, programSet, "gbuffers_damagedblock");

            clearSpectraGlErrors("after pipeline compile");
        }

        private static void clearSpectraGlErrors(String stage) {
            int error;
            int count = 0;

            while ((error = org.lwjgl.opengl.GL11.glGetError()) != org.lwjgl.opengl.GL11.GL_NO_ERROR && count < 32) {
                count++;
                System.out.println("[Spectra/Oculus] Cleared GL error " + error + " " + stage);
            }
        }

        private static void compileNamedPipelineProgram(java.io.File shaderPack, net.coderbot.iris.spectra.SpectraProgramSet programSet, String programName) {
            net.coderbot.iris.spectra.SpectraProgramSource source = programSet.get(programName);

            if (source == null) {
                source = findProgramEndingWith(programSet, "/" + programName);
            }

            if (source == null) {
                System.out.println("[Spectra/Oculus] Pipeline skipped " + programName + ": not found");
                return;
            }

            System.out.println("[Spectra/Oculus] Pipeline compiling " + programName + " from " + source.getName());
            int programId = compileProgramSource(shaderPack, source);

            if (programId == 0) {
                System.out.println("[Spectra/Oculus] Pipeline failed to compile " + programName);
                return;
            }

            activePipeline.setProgram(programName, programId);
        }

        private static net.coderbot.iris.spectra.SpectraProgramSource findProgramEndingWith(net.coderbot.iris.spectra.SpectraProgramSet programSet, String suffix) {
            for (net.coderbot.iris.spectra.SpectraProgramSource source : programSet.getPrograms()) {
                if (source.getName().endsWith(suffix)) {
                    return source;
                }
            }

            return null;
        }

        private static int compileProgramSource(java.io.File shaderPack, net.coderbot.iris.spectra.SpectraProgramSource programSource) {
            String vertexSource = programSource.getVertexSource();
            String fragmentSource = programSource.getFragmentSource();

            if (vertexSource == null) {
                vertexSource = readShaderSourceWithIncludes(shaderPack, programSource.getVertexPath());
            }

            if (fragmentSource == null) {
                fragmentSource = readShaderSourceWithIncludes(shaderPack, programSource.getFragmentPath());
            }

            return compileTestProgram(
                    shaderPack,
                    programSource.getName(),
                    programSource.getVertexPath(),
                    programSource.getFragmentPath(),
                    vertexSource,
                    fragmentSource
            );
        }


        private static int compileTestProgram(java.io.File shaderPack, String programName, String vertexPath, String fragmentPath, String vertexSource, String fragmentSource) {
            System.out.println("[Spectra/Oculus] Compile ProgramSet program: " + programName);

            if (vertexSource == null) {
                System.out.println("[Spectra/Oculus] Missing vertex shader source: " + vertexPath);
                return 0;
            }

            if (fragmentSource == null) {
                System.out.println("[Spectra/Oculus] Missing fragment shader source: " + fragmentPath);
                return 0;
            }

            int vertexShader = 0;
            int fragmentShader = 0;
            int program = 0;

            try {
                vertexSource = net.coderbot.iris.spectra.SpectraShaderPreprocessor.preprocess(vertexPath, vertexSource);
                fragmentSource = net.coderbot.iris.spectra.SpectraShaderPreprocessor.preprocess(fragmentPath, fragmentSource);

                vertexShader = compileShader(org.lwjgl.opengl.GL20.GL_VERTEX_SHADER, vertexPath, vertexSource);
                if (vertexShader == 0) {
                    return 0;
                }

                fragmentShader = compileShader(org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER, fragmentPath, fragmentSource);
                if (fragmentShader == 0) {
                    return 0;
                }

                program = org.lwjgl.opengl.GL20.glCreateProgram();
                org.lwjgl.opengl.GL20.glAttachShader(program, vertexShader);
                org.lwjgl.opengl.GL20.glAttachShader(program, fragmentShader);
                org.lwjgl.opengl.GL20.glLinkProgram(program);

                int linked = org.lwjgl.opengl.GL20.glGetProgrami(program, org.lwjgl.opengl.GL20.GL_LINK_STATUS);
                String linkLog = org.lwjgl.opengl.GL20.glGetProgramInfoLog(program, 8192);

                if (linked == org.lwjgl.opengl.GL11.GL_FALSE) {
                    System.out.println("[Spectra/Oculus] ProgramSet program link FAILED: " + programName);
                    if (linkLog != null && !linkLog.trim().isEmpty()) {
                        System.out.println("[Spectra/Oculus] Program link log: " + linkLog.trim());
                    }
                    return 0;
                }
                lastCompileTestProgram = program;
                program = 0;

                System.out.println("[Spectra/Oculus] ProgramSet program linked successfully: " + programName + " id=" + lastCompileTestProgram);
                if (linkLog != null && !linkLog.trim().isEmpty()) {
                    System.out.println("[Spectra/Oculus] Program link log: " + linkLog.trim());
                }
                return lastCompileTestProgram;
            } catch (Throwable t) {
                System.out.println("[Spectra/Oculus] ProgramSet compile crashed for program: " + programName);
                t.printStackTrace();
            } finally {
                if (program != 0) {
                    org.lwjgl.opengl.GL20.glDeleteProgram(program);
                }

                if (vertexShader != 0) {
                    org.lwjgl.opengl.GL20.glDeleteShader(vertexShader);
                }

                if (fragmentShader != 0) {
                    org.lwjgl.opengl.GL20.glDeleteShader(fragmentShader);
                }
            }

            return 0;
        }

        private static void compileTestProgram(java.io.File shaderPack, String programName, String vertexPath, String fragmentPath) {
            System.out.println("[Spectra/Oculus] Compile test program: " + programName);

            String vertexSource = readShaderSourceWithIncludes(shaderPack, vertexPath);
            String fragmentSource = readShaderSourceWithIncludes(shaderPack, fragmentPath);

            vertexSource = net.coderbot.iris.spectra.SpectraShaderPreprocessor.preprocess(vertexPath, vertexSource);
            fragmentSource = net.coderbot.iris.spectra.SpectraShaderPreprocessor.preprocess(fragmentPath, fragmentSource);

            if (vertexSource == null) {
                System.out.println("[Spectra/Oculus] Missing vertex shader source: " + vertexPath);
                return;
            }

            if (fragmentSource == null) {
                System.out.println("[Spectra/Oculus] Missing fragment shader source: " + fragmentPath);
                return;
            }

            int vertexShader = 0;
            int fragmentShader = 0;
            int program = 0;

            try {
                vertexShader = compileShader(org.lwjgl.opengl.GL20.GL_VERTEX_SHADER, vertexPath, vertexSource);
                if (vertexShader == 0) {
                    return;
                }

                fragmentShader = compileShader(org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER, fragmentPath, fragmentSource);
                if (fragmentShader == 0) {
                    return;
                }

                program = org.lwjgl.opengl.GL20.glCreateProgram();
                org.lwjgl.opengl.GL20.glAttachShader(program, vertexShader);
                org.lwjgl.opengl.GL20.glAttachShader(program, fragmentShader);
                org.lwjgl.opengl.GL20.glLinkProgram(program);

                int linked = org.lwjgl.opengl.GL20.glGetProgrami(program, org.lwjgl.opengl.GL20.GL_LINK_STATUS);
                String linkLog = org.lwjgl.opengl.GL20.glGetProgramInfoLog(program, 8192);

                if (linked == org.lwjgl.opengl.GL11.GL_FALSE) {
                    System.out.println("[Spectra/Oculus] Program link FAILED: " + programName);
                    if (linkLog != null && !linkLog.trim().isEmpty()) {
                        System.out.println("[Spectra/Oculus] Program link log: " + linkLog.trim());
                    }
                    return;
                }

                if (lastCompileTestProgram != 0) {
                    org.lwjgl.opengl.GL20.glDeleteProgram(lastCompileTestProgram);
                }

                lastCompileTestProgram = program;
                program = 0;

                System.out.println("[Spectra/Oculus] Program linked successfully: " + programName + " id=" + lastCompileTestProgram);
                if (linkLog != null && !linkLog.trim().isEmpty()) {
                    System.out.println("[Spectra/Oculus] Program link log: " + linkLog.trim());
                }
            } catch (Throwable t) {
                System.out.println("[Spectra/Oculus] Compile test crashed for program: " + programName);
                t.printStackTrace();
            } finally {
                if (program != 0) {
                    org.lwjgl.opengl.GL20.glDeleteProgram(program);
                }

                if (vertexShader != 0) {
                    org.lwjgl.opengl.GL20.glDeleteShader(vertexShader);
                }

                if (fragmentShader != 0) {
                    org.lwjgl.opengl.GL20.glDeleteShader(fragmentShader);
                }
            }
        }

        private static int compileShader(int type, String path, String source) {
            int shader = org.lwjgl.opengl.GL20.glCreateShader(type);
            org.lwjgl.opengl.GL20.glShaderSource(shader, source);
            org.lwjgl.opengl.GL20.glCompileShader(shader);

            int compiled = org.lwjgl.opengl.GL20.glGetShaderi(shader, org.lwjgl.opengl.GL20.GL_COMPILE_STATUS);
            String compileLog = org.lwjgl.opengl.GL20.glGetShaderInfoLog(shader, 8192);

            if (compiled == org.lwjgl.opengl.GL11.GL_FALSE) {
                System.out.println("[Spectra/Oculus] Shader compile FAILED: " + path);
                if (compileLog != null && !compileLog.trim().isEmpty()) {
                    System.out.println("[Spectra/Oculus] Shader compile log: " + compileLog.trim());
                }
                org.lwjgl.opengl.GL20.glDeleteShader(shader);
                return 0;
            }

            System.out.println("[Spectra/Oculus] Shader compiled successfully: " + path + " id=" + shader);
            if (compileLog != null && !compileLog.trim().isEmpty()) {
                System.out.println("[Spectra/Oculus] Shader compile log: " + compileLog.trim());
            }
            return shader;
        }

        private static int getOrCreateOverlayTestProgram() {
            if (overlayTestProgram != 0) {
                return overlayTestProgram;
            }

            String vertexSource = "#version 120\n" +
                    "void main() {\n" +
                    "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
                    "}\n";

            String fragmentSource = "#version 120\n" +
                    "void main() {\n" +
                    "    gl_FragColor = vec4(1.0, 0.0, 0.0, 0.55);\n" +
                    "}\n";

            int vertexShader = 0;
            int fragmentShader = 0;
            int program = 0;

            try {
                vertexShader = compileShader(org.lwjgl.opengl.GL20.GL_VERTEX_SHADER, "spectra_overlay_test.vsh", vertexSource);
                if (vertexShader == 0) {
                    return 0;
                }

                fragmentShader = compileShader(org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER, "spectra_overlay_test.fsh", fragmentSource);
                if (fragmentShader == 0) {
                    return 0;
                }

                program = org.lwjgl.opengl.GL20.glCreateProgram();
                org.lwjgl.opengl.GL20.glAttachShader(program, vertexShader);
                org.lwjgl.opengl.GL20.glAttachShader(program, fragmentShader);
                org.lwjgl.opengl.GL20.glLinkProgram(program);

                int linked = org.lwjgl.opengl.GL20.glGetProgrami(program, org.lwjgl.opengl.GL20.GL_LINK_STATUS);
                String linkLog = org.lwjgl.opengl.GL20.glGetProgramInfoLog(program, 8192);

                if (linked == org.lwjgl.opengl.GL11.GL_FALSE) {
                    System.out.println("[Spectra/Oculus] Overlay test program link FAILED");
                    if (linkLog != null && !linkLog.trim().isEmpty()) {
                        System.out.println("[Spectra/Oculus] Overlay test link log: " + linkLog.trim());
                    }
                    org.lwjgl.opengl.GL20.glDeleteProgram(program);
                    return 0;
                }

                overlayTestProgram = program;
                program = 0;
                System.out.println("[Spectra/Oculus] Overlay test program linked successfully id=" + overlayTestProgram);
                return overlayTestProgram;
            } catch (Throwable t) {
                System.out.println("[Spectra/Oculus] Overlay test program compile crashed");
                t.printStackTrace();
                return 0;
            } finally {
                if (program != 0) {
                    org.lwjgl.opengl.GL20.glDeleteProgram(program);
                }

                if (vertexShader != 0) {
                    org.lwjgl.opengl.GL20.glDeleteShader(vertexShader);
                }

                if (fragmentShader != 0) {
                    org.lwjgl.opengl.GL20.glDeleteShader(fragmentShader);
                }
            }
        }

        private static String readShaderSourceWithIncludes(java.io.File shaderPack, String path) {
            String source = readShaderSource(shaderPack, path);

            if (source == null) {
                return null;
            }

            return resolveShaderIncludes(shaderPack, path, source, 0);
        }

        private static String resolveShaderIncludes(java.io.File shaderPack, String currentPath, String source, int depth) {
            if (depth > 16) {
                System.out.println("[Spectra/Oculus] Include depth limit hit in: " + currentPath);
                return source;
            }

            StringBuilder output = new StringBuilder();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.StringReader(source));
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();

                    if (!trimmed.startsWith("#include")) {
                        output.append(line).append('\n');
                        continue;
                    }

                    String includePath = extractIncludePath(trimmed);

                    if (includePath == null || includePath.trim().isEmpty()) {
                        output.append("// Spectra/Oculus ignored bad include: ").append(line).append('\n');
                        continue;
                    }

                    String resolvedPath = resolveIncludePath(currentPath, includePath.trim());
                    String includeSource = readShaderSource(shaderPack, resolvedPath);

                    if (includeSource == null) {
                        System.out.println("[Spectra/Oculus] Missing include " + includePath + " resolved as " + resolvedPath + " from " + currentPath);
                        output.append("// Spectra/Oculus missing include: ").append(includePath).append('\n');
                        continue;
                    }

                    System.out.println("[Spectra/Oculus] Include " + includePath + " -> " + resolvedPath);
                    output.append("// Spectra/Oculus begin include: ").append(resolvedPath).append('\n');
                    output.append(resolveShaderIncludes(shaderPack, resolvedPath, includeSource, depth + 1));
                    output.append("// Spectra/Oculus end include: ").append(resolvedPath).append('\n');
                }
            } catch (Exception e) {
                System.out.println("[Spectra/Oculus] Failed while resolving includes for: " + currentPath);
                e.printStackTrace();
                return source;
            }

            return output.toString();
        }

        private static String extractIncludePath(String includeLine) {
            int quoteStart = includeLine.indexOf('"');
            int quoteEnd = includeLine.lastIndexOf('"');

            if (quoteStart >= 0 && quoteEnd > quoteStart) {
                return includeLine.substring(quoteStart + 1, quoteEnd);
            }

            int angleStart = includeLine.indexOf('<');
            int angleEnd = includeLine.lastIndexOf('>');

            if (angleStart >= 0 && angleEnd > angleStart) {
                return includeLine.substring(angleStart + 1, angleEnd);
            }

            return null;
        }

        private static String resolveIncludePath(String currentPath, String includePath) {
            String normalizedInclude = includePath.replace('\\', '/');

            if (normalizedInclude.startsWith("/")) {
                return normalizeShaderPath("shaders" + normalizedInclude);
            }

            String normalizedCurrent = currentPath.replace('\\', '/');
            int slash = normalizedCurrent.lastIndexOf('/');
            String parent = slash >= 0 ? normalizedCurrent.substring(0, slash + 1) : "";

            return normalizeShaderPath(parent + normalizedInclude);
        }

        private static String normalizeShaderPath(String path) {
            String[] parts = path.replace('\\', '/').split("/");
            java.util.List<String> stack = new java.util.ArrayList<>();

            for (String part : parts) {
                if (part == null || part.isEmpty() || ".".equals(part)) {
                    continue;
                }

                if ("..".equals(part)) {
                    if (!stack.isEmpty()) {
                        stack.remove(stack.size() - 1);
                    }
                    continue;
                }

                stack.add(part);
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < stack.size(); i++) {
                if (i > 0) {
                    builder.append('/');
                }
                builder.append(stack.get(i));
            }

            return builder.toString();
        }

        private static String readShaderSource(java.io.File shaderPack, String path) {
            if (shaderPack.isDirectory()) {
                java.io.File shaderFile = new java.io.File(shaderPack, path.replace('/', java.io.File.separatorChar));
                if (!shaderFile.exists()) {
                    return null;
                }

                try {
                    return readAllText(new java.io.FileInputStream(shaderFile));
                } catch (Exception e) {
                    System.out.println("[Spectra/Oculus] Failed to read shader file: " + shaderFile.getAbsolutePath());
                    e.printStackTrace();
                    return null;
                }
            }

            try {
                java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(shaderPack);
                java.util.zip.ZipEntry entry = zipFile.getEntry(path);

                if (entry == null) {
                    zipFile.close();
                    return null;
                }

                String text = readAllText(zipFile.getInputStream(entry));
                zipFile.close();
                return text;
            } catch (Exception e) {
                System.out.println("[Spectra/Oculus] Failed to read shader zip entry: " + path);
                e.printStackTrace();
                return null;
            }
        }

        private static String readAllText(java.io.InputStream inputStream) throws java.io.IOException {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }

            reader.close();
            return builder.toString();
        }

        public static String getActiveShaderPack() {
            return activeShaderPack;
        }

        public static boolean isShaderPackLoaded() {
            return shaderPackLoaded;
        }

        public static void renderTestOverlay() {
            if (!shaderPackLoaded) {
                return;
            }

            int postProgramId = activePipeline != null ? activePipeline.getPostProcessProgramId() : lastCompileTestProgram;
            net.coderbot.iris.spectra.SpectraPostProcessor.render(postProgramId);

            if (!postProcessTintEnabled) {
                return;
            }

            int programToUse = getOrCreateOverlayTestProgram();
            if (programToUse == 0) {
                return;
            }

            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getMinecraft();

            if (minecraft.currentScreen != null) {
                return;
            }

            if (!renderTestLogged) {
                renderTestLogged = true;
                System.out.println("[Spectra/Oculus] Post-process tint render hook is running with program id=" + programToUse);
            }

            int previousProgram = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM);
            int previousTexture = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D);
            int previousMatrixMode = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_MATRIX_MODE);

            org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS);

            try {
                org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
                org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
                org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_BLEND);
                org.lwjgl.opengl.GL11.glBlendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
                org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                org.lwjgl.opengl.GL11.glDepthMask(false);

                net.minecraft.client.gui.ScaledResolution scaledResolution = new net.minecraft.client.gui.ScaledResolution(minecraft);
                float width = scaledResolution.getScaledWidth();
                float height = scaledResolution.getScaledHeight();

                org.lwjgl.opengl.GL11.glMatrixMode(org.lwjgl.opengl.GL11.GL_PROJECTION);
                org.lwjgl.opengl.GL11.glPushMatrix();
                org.lwjgl.opengl.GL11.glLoadIdentity();
                org.lwjgl.opengl.GL11.glOrtho(0.0D, width, height, 0.0D, -1.0D, 1.0D);

                org.lwjgl.opengl.GL11.glMatrixMode(org.lwjgl.opengl.GL11.GL_MODELVIEW);
                org.lwjgl.opengl.GL11.glPushMatrix();
                org.lwjgl.opengl.GL11.glLoadIdentity();

                org.lwjgl.opengl.GL20.glUseProgram(programToUse);
                org.lwjgl.opengl.GL11.glBegin(org.lwjgl.opengl.GL11.GL_QUADS);
                org.lwjgl.opengl.GL11.glVertex2f(0.0F, 0.0F);
                org.lwjgl.opengl.GL11.glVertex2f(width, 0.0F);
                org.lwjgl.opengl.GL11.glVertex2f(width, height);
                org.lwjgl.opengl.GL11.glVertex2f(0.0F, height);
                org.lwjgl.opengl.GL11.glEnd();
                org.lwjgl.opengl.GL20.glUseProgram(0);

                org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_TEXTURE_2D);
                org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, previousTexture);
                org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                // Debug fallback tint disabled while porting the real shader pipeline.
            } catch (Throwable t) {
                System.out.println("[Spectra/Oculus] Render test overlay crashed; disabling compiled shader program");
                t.printStackTrace();

                if (overlayTestProgram != 0) {
                    org.lwjgl.opengl.GL20.glDeleteProgram(overlayTestProgram);
                    overlayTestProgram = 0;
                }
            } finally {
                org.lwjgl.opengl.GL20.glUseProgram(previousProgram);
                org.lwjgl.opengl.GL11.glBindTexture(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, previousTexture);
                org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                org.lwjgl.opengl.GL11.glDepthMask(true);

                org.lwjgl.opengl.GL11.glMatrixMode(org.lwjgl.opengl.GL11.GL_MODELVIEW);
                org.lwjgl.opengl.GL11.glPopMatrix();

                org.lwjgl.opengl.GL11.glMatrixMode(org.lwjgl.opengl.GL11.GL_PROJECTION);
                org.lwjgl.opengl.GL11.glPopMatrix();

                org.lwjgl.opengl.GL11.glMatrixMode(previousMatrixMode);
                org.lwjgl.opengl.GL11.glPopAttrib();
            }
        }
    }

    public static class ShaderPackConfigScreen extends net.minecraft.client.gui.GuiScreen {
        private final net.minecraft.client.gui.GuiScreen parent;
        private final String shaderPackName;

        public ShaderPackConfigScreen(net.minecraft.client.gui.GuiScreen parent, String shaderPackName) {
            this.parent = parent;
            this.shaderPackName = shaderPackName;
        }

        @Override
        public void initGui() {
            this.buttonList.clear();

            this.buttonList.add(new net.minecraft.client.gui.GuiButton(
                    0,
                    this.width / 2 - 100,
                    this.height - 28,
                    200,
                    20,
                    "Back"
            ));
        }

        @Override
        protected void actionPerformed(net.minecraft.client.gui.GuiButton button) {
            if (button.id == 0) {
                net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(this.parent);
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();

            this.drawCenteredString(
                    this.fontRenderer,
                    "Shader Pack Settings",
                    this.width / 2,
                    20,
                    0xFFFFFF
            );

            this.drawCenteredString(
                    this.fontRenderer,
                    "Pack: " + this.shaderPackName,
                    this.width / 2,
                    42,
                    0xAAAAAA
            );

            this.drawCenteredString(
                    this.fontRenderer,
                    "Options parser not implemented yet",
                    this.width / 2,
                    64,
                    0xAAAAAA
            );

            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }
}
