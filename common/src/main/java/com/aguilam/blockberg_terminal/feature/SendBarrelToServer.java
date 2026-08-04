package com.aguilam.blockberg_terminal.feature;

import com.google.gson.JsonArray;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.aguilam.blockberg_terminal.network.DataPost;
public class SendBarrelToServer {
    public static void sendBarrelDataToServer(JsonArray barrelDataList) {
        try {    
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.displayClientMessage(Component.literal("Отправляю " + barrelDataList.size() + " записей..."), false);
                DataPost.postBarrelData(barrelDataList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
