package com.aguilam.blockberg_terminal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.aguilam.blockberg_terminal.commands.Regions;
import com.aguilam.blockberg_terminal.config.ConfigManager;
import com.aguilam.blockberg_terminal.commands.Barrel;
import com.aguilam.blockberg_terminal.render.RenderBlocks;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
            LiteralArgumentBuilder<FabricClientCommandSource> setMinCommand = LiteralArgumentBuilder
                    .<FabricClientCommandSource>literal("setmin")
                    .executes(context -> {
                        Regions.setMin();
                        return 1;
                    });

            LiteralArgumentBuilder<FabricClientCommandSource> allRegionsCommand = LiteralArgumentBuilder
            .<FabricClientCommandSource>literal("allregions")
            .executes(context -> {
                Regions.allRegions();
                return 1;
            });

            LiteralArgumentBuilder<FabricClientCommandSource> setMaxCommand = LiteralArgumentBuilder
                    .<FabricClientCommandSource>literal("setmax")
                    .executes(context -> {
                        Regions.setMax();
                        return 1;
                    });

            LiteralArgumentBuilder<FabricClientCommandSource> addRegionCommand =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("addregion")
                            .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("regionName", StringArgumentType.word())
                                    .executes(context -> {
                                        String regionName = StringArgumentType.getString(context, "regionName");
                                        Regions.addRegion(regionName);
                                        return 1;
                                    }));

            LiteralArgumentBuilder<FabricClientCommandSource> scanRegionCommand =
            LiteralArgumentBuilder.<FabricClientCommandSource>literal("scan")
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("regionName", StringArgumentType.word())
                    .executes(context -> {
                        String regionName = StringArgumentType.getString(context, "regionName");
                        Regions.regionScan(regionName);
                        return 1;
                    })
                );

            LiteralArgumentBuilder<FabricClientCommandSource> searchBarrelCommand =
            LiteralArgumentBuilder.<FabricClientCommandSource>literal("searchbarrel")
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, Integer>argument("Страница", IntegerArgumentType.integer(1))
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("Название товара", StringArgumentType.greedyString())
                        .executes(context -> {
                            String searchTerm = StringArgumentType.getString(context, "Название товара");
                            int page = IntegerArgumentType.getInteger(context, "Страница");
                            Barrel.searchBarrels(searchTerm,page);
                            return 1;
                        })
                    )
                )
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("Название товара", StringArgumentType.greedyString())
                    .executes(context -> {
                        String searchTerm = StringArgumentType.getString(context, "Название товара");
                        Barrel.searchBarrels(searchTerm, 1);
                        return 1;
                    })
                );

            LiteralArgumentBuilder<FabricClientCommandSource> showBarrelContentCommand =
                LiteralArgumentBuilder.<FabricClientCommandSource>literal("showbarrelcontent")
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, Integer>argument("barrelIndex", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            int barrelId = IntegerArgumentType.getInteger(context, "barrelIndex");
                            Barrel.showBarrelContent(barrelId);
                            return 1;
                        })
                    );
            
            LiteralArgumentBuilder<FabricClientCommandSource> clearBlocksHighlighted =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("clearhl")
                        .executes(context -> {
                            Regions.clearHighlighted();
                            return 1;
                        });

            dispatcher.register(setMinCommand);
            dispatcher.register(setMaxCommand);
            dispatcher.register(addRegionCommand);
            dispatcher.register(scanRegionCommand);
            dispatcher.register(searchBarrelCommand);
            dispatcher.register(showBarrelContentCommand);
            dispatcher.register(allRegionsCommand);
            dispatcher.register(clearBlocksHighlighted);
        });

        WorldRenderEvents.LAST.register(context -> {
            RenderBlocks.renderHighlightedBlocks(context.matrixStack());
        });    
    }
}