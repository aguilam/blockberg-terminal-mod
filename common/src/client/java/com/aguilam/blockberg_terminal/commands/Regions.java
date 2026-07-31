package com.blockberg_terminal.commands;

public class Regions {
    public void setMin(){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            tempMinPos = client.player.getBlockPos();
            client.player.sendMessage(Text.literal("Временная минимальная позиция установлена: " + tempMinPos), false);
        }
    }
    public void setMax(){
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            tempMaxPos = client.player.getBlockPos();
            client.player.sendMessage(Text.literal("Временная максимальная позиция установлена: " + tempMaxPos), false);
        }
    }
    public void allRegions() {
        MinecraftClient client = MinecraftClient.getInstance();
        StringBuilder sb = new StringBuilder();
    
        for (Region region : regions) {
            String regionInfo = region.getRegionName() + " | min: " + region.getMinPos().toShortString() 
                                + ", max: " + region.getMaxPos().toShortString();
            sb.append(regionInfo).append("\n");
        }
        client.player.sendMessage(Text.literal(sb.toString()), false);
    }
    public void addRegion() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            if (tempMinPos != null && tempMaxPos != null) {
                Region newRegion = new Region(tempMinPos, tempMaxPos, regionName);
                regions.add(newRegion);
                client.player.sendMessage(Text.literal("Регион добавлен: " + newRegion), false);
                saveRegions();
                tempMinPos = null;
                tempMaxPos = null;
            } else {
                client.player.sendMessage(Text.literal("Сначала установите обе временные позиции с помощью /setmin и /setmax."), false);
            }
        }
    }
    public void regionScan(String regionName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Region region = findRegionByName(regionName);
            if (region != null) {
                scanSignsInBounds(client, region);
            } else {
                client.player.sendMessage(Text.literal("Регион с именем " + regionName + " не найден."), false);
            }
        }
    }
}
