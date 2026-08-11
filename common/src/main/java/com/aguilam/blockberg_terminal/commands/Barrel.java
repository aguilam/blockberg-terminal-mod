package com.aguilam.blockberg_terminal.commands;

import com.aguilam.blockberg_terminal.render.HighlightedBlocks;
import com.aguilam.blockberg_terminal.feature.GetBarrelInfo;
import com.aguilam.blockberg_terminal.feature.SearchBarrels;

import net.minecraft.client.Minecraft;
public class Barrel {

    public static void showBarrelContent(int barrelId){
        GetBarrelInfo.getBarrelInfo(Minecraft.getInstance(),barrelId);
    }

    public static void searchBarrels(String query, int page){
        HighlightedBlocks.clearBlocks();
        SearchBarrels.searchBarrels(Minecraft.getInstance(), query, page);
    }
}
