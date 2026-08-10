package com.aguilam.blockberg_terminal.commands;

import com.aguilam.blockberg_terminal.model.Regions.Region;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.aguilam.blockberg_terminal.region.RegionsManager;
import com.google.gson.JsonArray;
import com.aguilam.blockberg_terminal.feature.SignScan;
import com.aguilam.blockberg_terminal.feature.SendBarrelToServer;

public class Regions {

    public static void setMin(){
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            RegionsManager.tempMinPos = client.player.blockPosition();
            client.player.displayClientMessage(Component.translatable("blockberg_terminal.temp_min_set" + RegionsManager.tempMinPos), false);
        }
    }

    public static void setMax(){
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            RegionsManager.tempMaxPos = client.player.blockPosition();
            client.player.displayClientMessage(Component.translatable("blockberg_terminal.temp_max_set" + RegionsManager.tempMaxPos), false);
        }
    }

    public static void allRegions() {
        Minecraft client = Minecraft.getInstance();
        StringBuilder sb = new StringBuilder();
    
        for (Region region : RegionsManager.regions) {
            String regionInfo = region.getRegionName() + " | min: " + region.getMinPos().toShortString() 
                                + ", max: " + region.getMaxPos().toShortString();
            sb.append(regionInfo).append("\n");
        }
        client.player.displayClientMessage(Component.literal(sb.toString()), false);
    }

    public static void addRegion(String regionName) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            if (RegionsManager.tempMinPos != null && RegionsManager.tempMaxPos != null) {
                Region newRegion = new Region(RegionsManager.tempMinPos, RegionsManager.tempMaxPos, regionName);
                RegionsManager.regions.add(newRegion);
                client.player.displayClientMessage(Component.translatable("blockberg_terminal.region_added", newRegion), false);
                RegionsManager.saveRegions();
                RegionsManager.tempMinPos = null;
                RegionsManager.tempMaxPos = null;
            } else {
                client.player.displayClientMessage(Component.translatable("blockberg_terminal.positions_required"), false);
            }
        }
    }

    public static void regionScan(String regionName) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            Region region = RegionsManager.findRegionByName(regionName);
            if (region != null) {
                JsonArray barrelDataList = SignScan.scanSignsInBounds(client, region);
                SendBarrelToServer.sendBarrelDataToServer(barrelDataList);
            } else {
                client.player.displayClientMessage(Component.translatable("blockberg_terminal.region_not_found",regionName), false);
            }
        }
    }

}
