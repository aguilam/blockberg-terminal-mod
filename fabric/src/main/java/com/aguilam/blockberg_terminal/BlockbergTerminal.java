package com.aguilam.blockberg_terminal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.aguilam.blockberg_terminal.commands.Regions;
import com.aguilam.blockberg_terminal.config.ConfigManager;
import com.aguilam.blockberg_terminal.commands.Barrel;
import com.aguilam.blockberg_terminal.render.RenderBlocks;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import com.aguilam.blockberg_terminal.feature.ProcessStorageScreen;
import com.aguilam.blockberg_terminal.local.LocalServer;

import net.fabricmc.loader.api.FabricLoader;
import com.aguilam.blockberg_terminal.region.RegionsManager;

@Environment(EnvType.CLIENT)
public class BlockbergTerminal implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.file = FabricLoader.getInstance().getConfigDir().resolve("blockberg-terminal.json").toFile();
        LocalServer.gameDir = FabricLoader.getInstance().getGameDir();
        RegionsManager.loadRegions();
        ConfigManager.load();
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ProcessStorageScreen.checkStorageScreen(screen, client);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ProcessStorageScreen.checkStorageTick();
        });
        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(Barrel.searchBarrels());
            dispatcher.register(Barrel.showBarrelContent());
            dispatcher.register(Barrel.searchSnapshot());
            dispatcher.register(Regions.setMin());
            dispatcher.register(Regions.setMax());
            dispatcher.register(Regions.addRegion());
            dispatcher.register(Regions.allRegions());
            dispatcher.register(Regions.scan());
            dispatcher.register(Regions.clearHighlighted());
        });

        WorldRenderEvents.LAST.register(context -> {
            RenderBlocks.renderHighlightedBlocks(context.matrixStack());
        });
    }
}