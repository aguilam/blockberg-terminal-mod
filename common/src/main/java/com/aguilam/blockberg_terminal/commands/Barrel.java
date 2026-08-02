package com.aguilam.blockberg_terminal.commands;

import net.minecraft.client.Minecraft;
public class Barrel {

    public static void showBarrelContent(int barrelId){
        getBarrelInfo(Minecraft.getInstance(),barrelId);
    }

    public static void searchBarrels(String query, int page){
        highlightedBlocks.clear();
        searchBarrels(Minecraft.getInstance(), query, page);
    }

}
