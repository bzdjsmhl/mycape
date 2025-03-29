package com.mcooi.mycape;

import com.mcooi.mycape.screen.CapeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class MyCapeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (MyCape.openScreenKey.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new CapeScreen());
            }
        });
    }
} 