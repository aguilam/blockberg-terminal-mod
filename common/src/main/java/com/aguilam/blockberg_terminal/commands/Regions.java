package com.aguilam.blockberg_terminal.commands;

import com.aguilam.blockberg_terminal.model.Regions.Region;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import com.aguilam.blockberg_terminal.region.RegionsManager;
import com.aguilam.blockberg_terminal.render.HighlightedBlocks;
import com.google.gson.JsonArray;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.aguilam.blockberg_terminal.feature.SignScan;
import com.aguilam.blockberg_terminal.feature.SendBarrelToServer;

public class Regions {
    public static <S> LiteralArgumentBuilder<S> setMin() {
        return LiteralArgumentBuilder
        .<S>literal("setmin")
        .executes(context -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                RegionsManager.tempMinPos = client.player.blockPosition();
                if (RegionsManager.tempMaxPos != null && RegionsManager.tempMinPos != null) {
                    RegionsManager.regionsForRender.clear();
                    RegionsManager.regionsForRender.add(new Region(RegionsManager.tempMinPos, RegionsManager.tempMaxPos, "selected"));
                    RegionsManager.isDrawEnabled = true;
                }
                client.player.sendSystemMessage(Component.translatable("blockberg_terminal.temp_min_set", RegionsManager.tempMinPos));
            }
            return 1;
        });
    }

    public static <S> LiteralArgumentBuilder<S> setMax () {
        return LiteralArgumentBuilder
        .<S>literal("setmax")
        .executes(context -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                RegionsManager.tempMaxPos = client.player.blockPosition();
                if (RegionsManager.tempMaxPos != null && RegionsManager.tempMinPos != null) {
                    RegionsManager.regionsForRender.clear();
                    RegionsManager.regionsForRender.add(new Region(RegionsManager.tempMinPos, RegionsManager.tempMaxPos, "selected"));
                    RegionsManager.isDrawEnabled = true;
                }
                client.player.sendSystemMessage(Component.translatable("blockberg_terminal.temp_max_set", RegionsManager.tempMaxPos));
            }
            return 1;
        });
    }


    public static <S> LiteralArgumentBuilder<S> allRegions () {
        return LiteralArgumentBuilder
        .<S>literal("allregions")
        .executes(context -> {
            Minecraft client = Minecraft.getInstance();
            StringBuilder sb = new StringBuilder();
        
            for (Region region : RegionsManager.regions) {
                String regionInfo = region.getRegionName() + " | min: " + region.getMinPos().toShortString() 
                                    + ", max: " + region.getMaxPos().toShortString();
                sb.append(regionInfo).append("\n");
            }
            client.player.sendSystemMessage(Component.literal(sb.toString()));
            return 1;
        });
    }

    public static <S> LiteralArgumentBuilder<S> addRegion() {
        return LiteralArgumentBuilder.<S>literal("addregion")
        .then(RequiredArgumentBuilder.<S, String>argument("regionName", StringArgumentType.word())
                .executes(context -> {
                    String regionName = StringArgumentType.getString(context, "regionName");
                    Minecraft client = Minecraft.getInstance();
                    if (client.player != null) {
                        if (RegionsManager.tempMinPos != null && RegionsManager.tempMaxPos != null) {
                            Region newRegion = new Region(RegionsManager.tempMinPos, RegionsManager.tempMaxPos, regionName);
                            RegionsManager.regions.add(newRegion);
                            client.player.sendSystemMessage(Component.translatable("blockberg_terminal.region_added", newRegion));
                            RegionsManager.saveRegions();
                            RegionsManager.tempMinPos = null;
                            RegionsManager.tempMaxPos = null;
                        } else {
                            client.player.sendSystemMessage(Component.translatable("blockberg_terminal.positions_required"));
                        }
                    }
                    return 1;
                }));
    }

    public static <S> LiteralArgumentBuilder<S> deleteRegion() {
        return LiteralArgumentBuilder.<S>literal("deletereg")
        .then(RequiredArgumentBuilder.<S,String>argument("regionName", StringArgumentType.word())
            .suggests((context, builder) ->
                SharedSuggestionProvider.suggest(
                    RegionsManager.regions.stream()
                        .map(Region::getRegionName),
                    builder
                )
            )
            .executes(context ->{
                String regionName = StringArgumentType.getString(context, "regionName");
                RegionsManager.deleteRegion(regionName);
                return 1;
            }));
    }

    public static <S> LiteralArgumentBuilder<S> renderRegions() {
        return LiteralArgumentBuilder.<S>literal("showreg")
        .executes(context ->{
            RegionsManager.isDrawEnabled = !RegionsManager.isDrawEnabled;
            return 1;
        });
    }
    public static <S> LiteralArgumentBuilder<S> scan() {
        return LiteralArgumentBuilder.<S>literal("scan")
        .then(RequiredArgumentBuilder.<S, String>argument("regionName", StringArgumentType.word())
            .suggests((context, builder) ->
                SharedSuggestionProvider.suggest(
                    RegionsManager.regions.stream()
                        .map(Region::getRegionName),
                    builder
                )
            )
            .executes(context -> {
                String regionName = StringArgumentType.getString(context, "regionName");
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                    Region region = RegionsManager.findRegionByName(regionName);
                    if (region != null) {
                        JsonArray barrelDataList = SignScan.scanSignsInBounds(client, region);
                        SendBarrelToServer.sendBarrelDataToServer(barrelDataList);
                    } else {
                        client.player.sendSystemMessage(Component.translatable("blockberg_terminal.region_not_found",regionName));
                    }
                }
                return 1;
            }));
    }

    public static <S> LiteralArgumentBuilder<S> clearHighlighted() {
        return LiteralArgumentBuilder.<S>literal("clearhl")
        .executes(context -> {
            HighlightedBlocks.clearBlocks();
            HighlightedBlocks.drawShapeEnabled = false;
            return 1;
        }); 
    }
}
