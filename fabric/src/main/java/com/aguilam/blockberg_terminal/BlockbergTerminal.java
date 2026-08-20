package com.aguilam.blockberg_terminal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.aguilam.blockberg_terminal.config.ConfigManager;

import java.io.IOException;
import java.nio.file.Files;

import com.aguilam.blockberg_terminal.commands.MasterCommand;
import com.aguilam.blockberg_terminal.render.RenderBlocks;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import com.aguilam.blockberg_terminal.feature.ProcessStorageScreen;

import net.fabricmc.loader.api.FabricLoader;
import com.aguilam.blockberg_terminal.region.RegionsManager;

@Environment(EnvType.CLIENT)
public class BlockbergTerminal implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.file = FabricLoader.getInstance().getConfigDir().resolve("blockberg-terminal.json").toFile();
        ConfigManager.gameDir = FabricLoader.getInstance().getGameDir().resolve("blockberg-terminal");
        try {
            Files.createDirectories(ConfigManager.gameDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create game directory", e);
        }
        ConfigManager.load();
        RegionsManager.loadRegions();
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ProcessStorageScreen.checkStorageScreen(screen, client);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ProcessStorageScreen.checkStorageTick();
        });
        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(MasterCommand.createMasterCommand());
        });

        WorldRenderEvents.LAST.register(context -> {
            RenderBlocks.renderHighlightedBlocks(context.matrixStack());
        });
    }
}