package com.aguilam.blockberg_terminal.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


import com.aguilam.blockberg_terminal.model.Barrel.BarrelInfo;
import com.aguilam.blockberg_terminal.model.Barrel.BarrelItem;
import com.aguilam.blockberg_terminal.network.DataPost;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
public class GetBarrelInfo {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static void getBarrelInfo(Minecraft client, int barrelid){
        try {
            DataPost.getBarrelInfo(barrelid).thenAccept(response -> {
                client.execute(() -> {
                    try {
                        Type type = new TypeToken<BarrelInfo>(){}.getType();
                        BarrelInfo info = GSON.fromJson(response.body(), type);
                        MutableComponent fullContentMessage = Component.translatable("blockberg_terminal.barrel_full_content").withColor(TextColor.GRAY)
                                .append(Component.translatable("blockberg_terminal.barrel_name").withColor(TextColor.WHITE)
                                    .append(Component.literal(info.name).withColor(TextColor.GOLD)))
                                .append(Component.translatable("blockberg_terminal.barrel_seller").withColor(TextColor.WHITE)
                                    .append(Component.literal(info.seller).withColor(TextColor.AQUA)))
                                .append(Component.translatable("blockberg_terminal.barrel_price").withColor(TextColor.WHITE)
                                    .append(Component.literal(String.format("%d", info.price)).withColor(TextColor.YELLOW)))
                                .append(Component.translatable("blockberg_terminal.barrel_quantity").withColor(TextColor.WHITE)
                                    .append(Component.literal(String.valueOf(info.quantity)).withColor(TextColor.GREEN)))
                                .append(Component.translatable("blockberg_terminal.barrel_coordinates").withColor(TextColor.WHITE)
                                    .append(Component.literal(String.format("(%d, %d, %d)", info.x, info.y, info.z)).withColor(TextColor.LIGHT_PURPLE)))
                                .append(Component.literal("\n"));
                            
                            if (info.barrelItems != null) {
                                fullContentMessage = fullContentMessage.append(Component.translatable("blockberg_terminal.barrel_contents").withColor(TextColor.WHITE));
                                for (BarrelItem item : info.barrelItems.items) {
                                    MutableComponent itemText = Component.literal("\n- ").withColor(TextColor.GRAY)
                                        .append(item.name).withColor(TextColor.GOLD)
                                        .append(" x " + String.valueOf(item.quantity)).withColor(TextColor.AQUA);
                                    fullContentMessage.append(itemText);
                                }
                                try {
                                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                    
                                    Date parsedDate = isoFormat.parse(info.barrelItems.recordDate);
                                    String formattedDate = sdf.format(parsedDate); 
                                
                                    fullContentMessage = fullContentMessage.append(Component.translatable("blockberg_terminal.barrel_recorded_date",formattedDate).withColor(TextColor.DARK_GRAY));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                fullContentMessage = fullContentMessage.append(Component.translatable("blockberg_terminal.barrel_contents_unknown").withColor(TextColor.RED));
                            }
                            client.player.sendSystemMessage(fullContentMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });   
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
