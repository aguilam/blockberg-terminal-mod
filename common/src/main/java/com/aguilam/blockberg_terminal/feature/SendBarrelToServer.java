package com.aguilam.blockberg_terminal.feature;

import com.google.gson.JsonArray;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class SendBarrelToServer {
    private void sendBarrelDataToServer(JsonArray barrelDataList) {
        try {    
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.displayClientMessage(Component.literal("Отправляю " + barrelDataList.size() + " записей..."), false);
            }
        
            //Add Network request
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
