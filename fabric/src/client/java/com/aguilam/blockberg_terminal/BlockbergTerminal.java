package com.aguilam.blockberg_terminal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.blockberg_terminal.config.ConfigManager;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.Formatting;
import net.minecraft.text.MutableText;
import net.minecraft.block.WallSignBlock;
import net.minecraft.util.math.Direction;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.text.ClickEvent;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.Blocks;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class PepePriceClient implements ClientModInitializer {
    //private static BlockPos tempMinPos;
    //private static BlockPos tempMaxPos;

    //private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    //private final List<HighlightedBlock> highlightedBlocks = new ArrayList<>();
    //private boolean isHighlightingActive = false;
    //private boolean drawShapeEnabled = false;

    private static final AtomicReference<ScreenHandler> delayedHandler = new AtomicReference<>();
    private static final AtomicInteger delayTicks = new AtomicInteger(0);
    @Override
    public void onInitializeClient() {
        loadRegions();
        ConfigManager.load();
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!ConfigManager.isSendBarrels) return;

            if (screen instanceof GenericContainerScreen) {
                GenericContainerScreen containerScreen = (GenericContainerScreen) screen;
                String title = containerScreen.getTitle().getString();
                if (title.contains("Бочка") || title.contains("Barrel")) {
                    if (client.crosshairTarget instanceof BlockHitResult) {
                        BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
                        BlockPos pos = hitResult.getBlockPos();
                        if (client.world.getBlockState(pos).getBlock() == Blocks.BARREL) {
                            delayedHandler.set(containerScreen.getScreenHandler());
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
                        //setmin command
                        return 1;
                    });

            LiteralArgumentBuilder<FabricClientCommandSource> allRegionsCommand = LiteralArgumentBuilder
            .<FabricClientCommandSource>literal("allregions")
            .executes(context -> {
                //allregions command
                return 1;
            });

            LiteralArgumentBuilder<FabricClientCommandSource> setMaxCommand = LiteralArgumentBuilder
                    .<FabricClientCommandSource>literal("setmax")
                    .executes(context -> {
                        //setmax command
                        return 1;
                    });

            LiteralArgumentBuilder<FabricClientCommandSource> addRegionCommand =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("addregion")
                            .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("regionName", StringArgumentType.word())
                                    .executes(context -> {
                                        String regionName = StringArgumentType.getString(context, "regionName");
                                        //addregion command
                                        return 1;
                                    }));

            LiteralArgumentBuilder<FabricClientCommandSource> scanRegionCommand =
            LiteralArgumentBuilder.<FabricClientCommandSource>literal("scan")
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("regionName", StringArgumentType.word())
                    .executes(context -> {
                        String regionName = StringArgumentType.getString(context, "regionName");
                        //scanRegion command
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
                            searchBarrels(searchTerm,page);
                            return 1;
                        })
                    )
                )
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("Название товара", StringArgumentType.greedyString())
                    .executes(context -> {
                        String searchTerm = StringArgumentType.getString(context, "Название товара");
                        searchBarrels(searchTerm, 1);
                        return 1;
                    })
                );

            LiteralArgumentBuilder<FabricClientCommandSource> showBarrelContentCommand =
                LiteralArgumentBuilder.<FabricClientCommandSource>literal("showbarrelcontent")
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, Integer>argument("barrelIndex", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            int barrelId = IntegerArgumentType.getInteger(context, "barrelIndex");
                            //showbarrelinfo command
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

        WorldRenderEvents.LAST.register(this::renderHighlightedBlocks);
    }
}