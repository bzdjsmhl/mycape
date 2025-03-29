package com.mcooi.mycape;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

public class MyCape implements ModInitializer {
    public static final String MOD_ID = "mycape";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static KeyBinding openScreenKey;
    public static final File CAPES_FOLDER = new File("capes");
    public static final File CONFIG_FOLDER = new File("config/cape");

    @Override
    public void onInitialize() {

        openScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mycape.open_screen",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_K,
            "category.mycape.general"
        ));

        if (!CAPES_FOLDER.exists()) {
            CAPES_FOLDER.mkdirs();
        }

        if (!CONFIG_FOLDER.exists()) {
            CONFIG_FOLDER.mkdirs();
        }

        LOGGER.info("MyCape mod initialized!");
    }
}