package net.coderbot.iris;

import net.minecraftforge.fml.common.Mod;

@Mod(
        modid = "oculus",
        name = "Spectra",
        version = "@MOD_VERSION@",
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

            finishShaderPackLoad(shaderFiles);
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

            finishShaderPackLoad(shaderFiles);
        }

        private static void finishShaderPackLoad(java.util.List<String> shaderFiles) {
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

            System.out.println("[Spectra/Oculus] NOTE: shaderpack is selected and parsed, but real GLSL rendering is not implemented yet");
        }

        public static String getActiveShaderPack() {
            return activeShaderPack;
        }

        public static boolean isShaderPackLoaded() {
            return shaderPackLoaded;
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
