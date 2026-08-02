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
import com.aguilam.blockberg_terminal.commands.Barrel;
import com.aguilam.blockberg_terminal.render.Render;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.BlockHitResult;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class BlockbergTerminal implements ClientModInitializer {
    //private static BlockPos tempMinPos;
    //private static BlockPos tempMaxPos;
    
    //private final List<HighlightedBlock> highlightedBlocks = new ArrayList<>();
    //private boolean isHighlightingActive = false;
    //private boolean drawShapeEnabled = false;

    private static final AtomicReference<AbstractContainerMenu> delayedHandler = new AtomicReference<>();
    private static final AtomicInteger delayTicks = new AtomicInteger(0);
    @Override
    public void onInitializeClient() {
        loadRegions();
        ConfigManager.load();
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!ConfigManager.isSendBarrels) return;

            if (screen instanceof ContainerScreen) {
                ContainerScreen containerScreen = (ContainerScreen) screen;
                String title = containerScreen.getTitle().getString();
                if (title.contains("Бочка") || title.contains("Barrel")) {
                    if (client.crosshairTarget instanceof BlockHitResult) {
                        BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
                        BlockPos pos = hitResult.getBlockPos();
                        if (client.level.getBlockState(pos).getBlock() == Blocks.BARREL) {
                            delayedHandler.set(containerScreen.getMenu());
                            delayTicks.set(4); 
                        }
                    }
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (delayTicks.get() > 0) {
                delayTicks.decrementAndGet();
                if (delayTicks.get() == 0 && delayedHandler.get() != null) {
                    processBarrelScreen(delayedHandler.get());
                    delayedHandler.set(null);
                }
            }
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
                Regions.allRegions();();
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
                                        Regions.addRegion();
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
        

            dispatcher.register(setMinCommand);
            dispatcher.register(setMaxCommand);
            dispatcher.register(addRegionCommand);
            dispatcher.register(scanRegionCommand);
            dispatcher.register(searchBarrelCommand);
            dispatcher.register(showBarrelContentCommand);
            dispatcher.register(allRegionsCommand);
        });

        WorldRenderEvents.LAST.register(Render::renderHighlightedBlocks);
    }
}