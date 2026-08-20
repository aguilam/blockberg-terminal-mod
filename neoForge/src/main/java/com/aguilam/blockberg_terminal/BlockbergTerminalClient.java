package com.aguilam.blockberg_terminal;
import java.io.IOException;
import java.nio.file.Files;

import com.aguilam.blockberg_terminal.commands.MasterCommand;
import com.aguilam.blockberg_terminal.config.ConfigManager;
import com.aguilam.blockberg_terminal.config.ConfigScreen;
import com.aguilam.blockberg_terminal.feature.ProcessStorageScreen;
import com.aguilam.blockberg_terminal.region.RegionsManager;
import com.aguilam.blockberg_terminal.render.RenderBlocks;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public class BlockbergTerminalClient {
    public static void init(ModContainer modContainer) {
        ConfigManager.file = FMLPaths.CONFIGDIR.get().resolve("blockberg-terminal.json").toFile();
        ConfigManager.gameDir = FMLPaths.GAMEDIR.get().resolve("blockberg-terminal");
        try {
            Files.createDirectories(ConfigManager.gameDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create game directory", e);
        }
        RegionsManager.loadRegions();
        ConfigManager.load();

        NeoForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> {
            ProcessStorageScreen.checkStorageScreen(event.getScreen(), Minecraft.getInstance());
        });

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            ProcessStorageScreen.checkStorageTick();
        });
        
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> {
            var dispatcher = event.getDispatcher();
            dispatcher.register(MasterCommand.createMasterCommand());
        });

        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                RenderBlocks.renderHighlightedBlocks(event.getPoseStack());
            }
        });

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parentScreen) -> ConfigScreen.createConfigScreen(parentScreen));
    }
}
