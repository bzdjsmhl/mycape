package com.mcooi.mycape.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.mcooi.mycape.MyCape;
import com.mcooi.mycape.util.CapeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CapeScreen extends Screen {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PREVIEW_WIDTH = 128;
    private static final int PREVIEW_HEIGHT = 64;
    private static final float ASPECT_RATIO = (float)PREVIEW_WIDTH / PREVIEW_HEIGHT;
    
    private String selectedCape = null;
    private Identifier previewTexture = null;
    private NativeImageBackedTexture previewTextureObject = null;
    private static final Identifier PREVIEW_TEXTURE_ID = new Identifier(MyCape.MOD_ID, "preview");
    private int actualPreviewWidth = PREVIEW_WIDTH;
    private int actualPreviewHeight = PREVIEW_HEIGHT;
    private final CapeManager capeManager;

    private final List<File> capeFiles = new ArrayList<>();
    private int currentFileIndex = -1;
    private boolean openFolder = false;

    public CapeScreen() {
        super(Text.translatable("screen.mycape.title"));
        this.capeManager = CapeManager.getInstance();
        loadCapeFiles();
    }

    private void loadCapeFiles() {
        capeFiles.clear();
        File[] files = MyCape.CAPES_FOLDER.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files != null && files.length > 0) {
            Arrays.sort(files);
            capeFiles.addAll(Arrays.asList(files));
            if (currentFileIndex == -1) {
                currentFileIndex = 0;
                setSelectedCape(capeFiles.get(currentFileIndex).getAbsolutePath());
            }
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int bottomY = this.height /2;
        int spacing = 10;

        this.addDrawableChild(new ButtonWidget(
            centerX - BUTTON_WIDTH - spacing,
            bottomY + 10,
            BUTTON_WIDTH ,
            BUTTON_HEIGHT,
            Text.translatable("button.mycape.prev_cape"),
            button -> previousCape()
        ));

        this.addDrawableChild(new ButtonWidget(
            centerX + spacing,
            bottomY + 10,
            BUTTON_WIDTH ,
            BUTTON_HEIGHT,
            Text.translatable("button.mycape.next_cape"),
            button -> nextCape()
        ));

        this.addDrawableChild(new ButtonWidget(
            centerX - BUTTON_WIDTH - spacing,
            bottomY + 35,
            BUTTON_WIDTH ,
            BUTTON_HEIGHT,
            Text.translatable("button.mycape.apply_cape"),
            button -> applyCape()
        ));

        this.addDrawableChild(new ButtonWidget(
            centerX + spacing,
            bottomY + 35,
            BUTTON_WIDTH ,
            BUTTON_HEIGHT,
            Text.translatable("button.mycape.open_folder"),
            button -> openCapesFolder()
        ));

        this.addDrawableChild(new ButtonWidget(
                centerX - BUTTON_WIDTH - spacing,
                bottomY + 60,
                2 * (BUTTON_WIDTH + spacing) ,
                BUTTON_HEIGHT,
                Text.translatable("button.mycape.change_mode"),
                button -> clearCpae()
        ));

        if (!MyCape.CAPES_FOLDER.exists()) {
            MyCape.CAPES_FOLDER.mkdirs();

            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("Created Folder: " +
                        MyCape.CAPES_FOLDER.getAbsolutePath()), false);
            }
        }
    }
    
    private void previousCape() {
        ReloadCapeFiles();
        if (capeFiles.isEmpty()) return;
        
        if (currentFileIndex > 0) {
            currentFileIndex--;
        } else {
            currentFileIndex = capeFiles.size() - 1;
        }
        
        setSelectedCape(capeFiles.get(currentFileIndex).getAbsolutePath());
    }
    
    private void nextCape() {
        ReloadCapeFiles();
        if (capeFiles.isEmpty()) return;
        
        if (currentFileIndex < capeFiles.size() - 1) {
            currentFileIndex++;
        } else {
            currentFileIndex = 0;
        }
        
        setSelectedCape(capeFiles.get(currentFileIndex).getAbsolutePath());
    }
    
    private void openCapesFolder() {
        try {
            Runtime.getRuntime().exec("explorer " + MyCape.CAPES_FOLDER.getAbsolutePath());
            openFolder = true;
        } catch (IOException e) {
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("Failed to open folder: " + e.getMessage()), false);
            }
        }
    }

    private void clearCpae() {
        capeManager.toggleCapeRendering();
        if (this.client != null && this.client.player != null) {
            boolean isEnabled = capeManager.isCapeRendered();
            String message = isEnabled ?  "已启用功能":"已禁用功能";
            this.client.player.sendMessage(Text.literal(message), true);
            MinecraftClient.getInstance().setScreen(null);
        }
    }
    private void applyCape() {
        if (selectedCape != null) {
            capeManager.setCurrentCape(selectedCape);
            MinecraftClient.getInstance().setScreen(null);
        }
    }

    private NativeImage resizeImage(NativeImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        float originalAspectRatio = (float)originalWidth / originalHeight;

        int targetWidth = PREVIEW_WIDTH;
        int targetHeight = PREVIEW_HEIGHT;

        if (originalAspectRatio > ASPECT_RATIO) {
            targetWidth = (int)(targetHeight * originalAspectRatio);
        } else {
            targetHeight = (int)(targetWidth / originalAspectRatio);
        }

        NativeImage resized = new NativeImage(targetWidth, targetHeight, true);

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int sourceX = x * originalWidth / targetWidth;
                int sourceY = y * originalHeight / targetHeight;
                int color = originalImage.getColor(sourceX, sourceY);
                resized.setColor(x, y, color);
            }
        }

        actualPreviewWidth = targetWidth;
        actualPreviewHeight = targetHeight;

        return resized;
    }

    public void setSelectedCape(String capePath) {
        this.selectedCape = capePath;
        if (capePath != null) {
            try {

                if (this.previewTextureObject != null) {
                    MinecraftClient.getInstance().getTextureManager().destroyTexture(PREVIEW_TEXTURE_ID);
                    this.previewTextureObject.close();
                }

                NativeImage originalImage;
                try (FileInputStream fileInputStream = new FileInputStream(capePath)) {
                    originalImage = NativeImage.read(fileInputStream);
                }
                NativeImage resizedImage = resizeImage(originalImage);
                originalImage.close();

                this.previewTextureObject = new NativeImageBackedTexture(resizedImage);
                resizedImage.close();

                MinecraftClient.getInstance().getTextureManager().registerTexture(PREVIEW_TEXTURE_ID, this.previewTextureObject);
                this.previewTexture = PREVIEW_TEXTURE_ID;

                MyCape.LOGGER.info("Loaded cape preview with size: {}x{}", actualPreviewWidth, actualPreviewHeight);
            } catch (IOException e) {
                MyCape.LOGGER.error("Failed to load cape preview: {}", e.getMessage());
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        if (previewTexture != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, previewTexture);

            int previewX = (this.width - actualPreviewWidth) / 2;
            int previewY = (this.height - actualPreviewHeight) / 2 - 30;

            fill(matrices, previewX - 2, previewY - 2 - 20,
                 previewX + actualPreviewWidth + 2, previewY + actualPreviewHeight + 2 - 20, 0xFF000000);

            drawTexture(matrices, previewX, previewY - 20, 0, 0, actualPreviewWidth, actualPreviewHeight,
                       actualPreviewWidth, actualPreviewHeight);

            if (!capeFiles.isEmpty() && currentFileIndex >= 0 && currentFileIndex < capeFiles.size()) {
                String filename = capeFiles.get(currentFileIndex).getName();
                drawCenteredText(matrices, this.textRenderer, 
                               Text.literal(filename), 
                               this.width / 2, 
                               previewY + actualPreviewHeight + 10 - 20,
                               0xFFFFFF);
            }
        } else if (capeFiles.isEmpty()) {
            drawCenteredText(matrices, this.textRenderer,
                            Text.translatable("screen.mycape.worn"),
                           this.width / 2, 
                           this.height / 2 - 30,
                           0xFFFFFF);
            drawCenteredText(matrices, this.textRenderer,
                            Text.translatable("screen.mycape.add"),
                           this.width / 2, 
                           this.height / 2 - 10,
                           0xAAAAAA);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        if (this.previewTextureObject != null) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(PREVIEW_TEXTURE_ID);
            this.previewTextureObject.close();
        }
    }

    public boolean isOpenFolder() {
        return openFolder;
    }
    private void  ReloadCapeFiles()
    {
        if(isOpenFolder()){
            loadCapeFiles();
            openFolder = false;
        }
    }
}