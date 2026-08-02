package com.aguilam.blockberg_terminal.commands;

import com.aguilam.blockberg_terminal.model.Regions.Region;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class Regions {

    public static void setMin(){
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            tempMinPos = client.player.position();
            client.player.displayClientMessage(Component.literal("Временная минимальная позиция установлена: " + tempMinPos), false);
        }
    }

    public static void setMax(){
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            tempMaxPos = client.player.position();
            client.player.displayClientMessage(Component.literal("Временная максимальная позиция установлена: " + tempMaxPos), false);
        }
    }

    public static void allRegions() {
        Minecraft client = Minecraft.getInstance();
        StringBuilder sb = new StringBuilder();
    
        for (Region region : regions) {
            String regionInfo = region.getRegionName() + " | min: " + region.getMinPos().toShortString() 
                                + ", max: " + region.getMaxPos().toShortString();
            sb.append(regionInfo).append("\n");
        }
        client.player.displayClientMessage(Component.literal(sb.toString()), false);
    }

    public static void addRegion() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            if (tempMinPos != null && tempMaxPos != null) {
                Region newRegion = new Region(tempMinPos, tempMaxPos, regionName);
                regions.add(newRegion);
                client.player.displayClientMessage(Component.literal("Регион добавлен: " + newRegion), false);
                saveRegions();
                tempMinPos = null;
                tempMaxPos = null;
            } else {
                client.player.displayClientMessage(Component.literal("Сначала установите обе временные позиции с помощью /setmin и /setmax."), false);
            }
        }
    }

    public static void regionScan(String regionName) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            Region region = findRegionByName(regionName);
            if (region != null) {
                scanSignsInBounds(client, region);
            } else {
                client.player.displayClientMessage(Component.literal("Регион с именем " + regionName + " не найден."), false);
            }
        }
    }

}
