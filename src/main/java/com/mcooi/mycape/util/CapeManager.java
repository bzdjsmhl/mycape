package com.mcooi.mycape.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mcooi.mycape.MyCape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

public class CapeManager {
    private static CapeManager instance;
    private String currentCape;
    private Identifier currentCapeTexture;
    private static final Identifier CAPE_TEXTURE_ID = new Identifier(MyCape.MOD_ID, "cape");
    private NativeImageBackedTexture currentTexture;
    private static final File CONFIG_FILE = new File(MyCape.CONFIG_FOLDER, "mycape.json");
    private static final File LAST_CAPE_FILE = new File(MyCape.CONFIG_FOLDER, "last_cape");
    private static final Gson GSON = new Gson();
    private boolean shouldRenderCape = true;
    private CapeManager() {
        loadLastCape();
        loadConfig();
    }

    private void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try {
                String content = new String(Files.readAllBytes(CONFIG_FILE.toPath()));
                if (!content.isEmpty()) {
                    JsonObject config = GSON.fromJson(content, JsonObject.class);
                    if (config.has("shouldRenderCape")) {
                        shouldRenderCape = config.get("shouldRenderCape").getAsBoolean();
                    }
                }
            } catch (IOException e) {
                MyCape.LOGGER.error("Failed to load config: {}", e.getMessage());
            }
        }
    }

    private void loadLastCape() {
        if (LAST_CAPE_FILE.exists()) {
            try {
                String lastCapePath = new String(java.nio.file.Files.readAllBytes(LAST_CAPE_FILE.toPath()));
                if (new File(lastCapePath).exists()) {
                    setCurrentCape(lastCapePath);
                }
            } catch (IOException e) {
                MyCape.LOGGER.error("Failed to load last cape: {}", e.getMessage());
            }
        }
    }

    public static CapeManager getInstance() {
        if (instance == null) {
            instance = new CapeManager();
        }
        return instance;
    }

    public void setCurrentCape(String capePath) {
        this.currentCape = getCurrentCape(capePath);
        try {
            java.nio.file.Files.write(LAST_CAPE_FILE.toPath(), capePath.getBytes());

            if (this.currentTexture != null) {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(CAPE_TEXTURE_ID);
                this.currentTexture.close();
            }

            NativeImage nativeImage;
            try (FileInputStream fileInputStream = new FileInputStream(capePath)) {
                nativeImage = NativeImage.read(fileInputStream);
            }

            this.currentTexture = new NativeImageBackedTexture(nativeImage);

            MinecraftClient.getInstance().getTextureManager().registerTexture(CAPE_TEXTURE_ID, this.currentTexture);
            this.currentCapeTexture = CAPE_TEXTURE_ID;

            MyCape.LOGGER.info("Successfully loaded cape texture from: {} with size: {}x{}", capePath, nativeImage.getWidth(), nativeImage.getHeight());
        } catch (IOException e) {
            MyCape.LOGGER.error("Failed to load cape: {}", e.getMessage());
            this.currentCapeTexture = null;
            if (this.currentTexture != null) {
                this.currentTexture.close();
                this.currentTexture = null;
            }
        }
    }
    private void saveConfig() {
        try {
            JsonObject config = new JsonObject();
            config.addProperty("shouldRenderCape", shouldRenderCape);

            if (!CONFIG_FILE.exists()) {
                CONFIG_FILE.createNewFile();
            }

            Files.write(CONFIG_FILE.toPath(), GSON.toJson(config).getBytes());
        } catch (IOException e) {
            MyCape.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public boolean toggleCapeRendering() {
        shouldRenderCape = !shouldRenderCape;
        saveConfig();
        return shouldRenderCape;
    }
    public Identifier getCurrentCapeTexture() {
        return shouldRenderCape ? currentCapeTexture : null;
    }
    public boolean isCapeRendered() {
        return shouldRenderCape;
    }

    public String getCurrentCape(String capePath) {
        currentCape = capePath;
        return currentCape;
    }
}