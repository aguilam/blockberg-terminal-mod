package com.aguilam.blockberg_terminal.commands;

import com.aguilam.blockberg_terminal.render.HighlightedBlocks;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.aguilam.blockberg_terminal.feature.GetBarrelInfo;
import com.aguilam.blockberg_terminal.feature.SearchBarrels;
import com.aguilam.blockberg_terminal.feature.SearchSnapshots;

import net.minecraft.client.Minecraft;
public class Barrel {
    
    public static <S> LiteralArgumentBuilder<S> showBarrelContent(){
        return LiteralArgumentBuilder.<S>literal("showbarrelcontent")
        .then(RequiredArgumentBuilder.<S, Integer>argument("barrelIndex", IntegerArgumentType.integer(0))
            .executes(context -> {
                int barrelId = IntegerArgumentType.getInteger(context, "barrelIndex");
                GetBarrelInfo.getBarrelInfo(Minecraft.getInstance(),barrelId);
                return 1;
            })
        );
    }

    public static <S> LiteralArgumentBuilder<S> searchBarrels(){
        return LiteralArgumentBuilder.<S>literal("searchbarrel")
        .then(RequiredArgumentBuilder.<S, Integer>argument("Page", IntegerArgumentType.integer(1))
            .then(RequiredArgumentBuilder.<S, String>argument("Название товара", StringArgumentType.greedyString())
                .executes(context -> {
                    String searchTerm = StringArgumentType.getString(context, "Название товара");
                    int page = IntegerArgumentType.getInteger(context, "Page");
                    HighlightedBlocks.clearBlocks();
                    SearchBarrels.searchBarrels(Minecraft.getInstance(), searchTerm, page);
                    return 1;
                })
            )
        )
        .then(RequiredArgumentBuilder.<S, String>argument("Название товара", StringArgumentType.greedyString())
            .executes(context -> {
                String searchTerm = StringArgumentType.getString(context, "Название товара");
                HighlightedBlocks.clearBlocks();
                SearchBarrels.searchBarrels(Minecraft.getInstance(), searchTerm, 1);
                return 1;
            })
        );
    }

    public static <S> LiteralArgumentBuilder<S> searchSnapshot() {
        return LiteralArgumentBuilder.<S>literal("searchitems")
        .then(RequiredArgumentBuilder.<S,Integer>argument("Page", IntegerArgumentType.integer(1))
            .then(RequiredArgumentBuilder.<S,String>argument("Item name", StringArgumentType.greedyString()))
                .executes(context -> {
                  String itemName = StringArgumentType.getString(context, "Page");
                  int page = IntegerArgumentType.getInteger(context, "Item name");
                  SearchSnapshots.SearchBySnapshots(Minecraft.getInstance(), itemName, page);
                  return 1;
                })
        )
        .then(RequiredArgumentBuilder.<S,String>argument("Item name", StringArgumentType.greedyString())
            .executes(content -> {
                String itemName = StringArgumentType.getString(content, "Item name");
                SearchSnapshots.SearchBySnapshots(Minecraft.getInstance(), itemName, 1);
                return 1;
            })
        );
    }

}
