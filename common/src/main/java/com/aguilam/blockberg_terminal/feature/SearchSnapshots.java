package com.aguilam.blockberg_terminal.feature;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.aguilam.blockberg_terminal.model.Barrel.BarrelItem;
import com.aguilam.blockberg_terminal.model.Barrel.SnapshotsSearchBody;
import com.aguilam.blockberg_terminal.model.Barrel.SnapshotsSearchItem;
import com.aguilam.blockberg_terminal.network.DataPost;
import com.aguilam.blockberg_terminal.render.HighlightedBlocks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
public class SearchSnapshots {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void SearchBySnapshots(Minecraft client, String itemName, int page) {
        try {
            client.player.displayClientMessage(Component.translatable("blockberg_terminal.search_started",itemName,page), false);
            DataPost.searchSnapshots(itemName, page).thenAccept(response -> {
                Type type = new TypeToken<SnapshotsSearchBody>(){}.getType();
                SnapshotsSearchBody snapshots = GSON.fromJson(response.body(), type);
                HighlightedBlocks.drawShapeEnabled = true;
                HighlightedBlocks.clearBlocks();
                client.player.displayClientMessage(Component.translatable("blockberg_terminal.snapshot_search_results",snapshots.total,snapshots.page), false);
                for (SnapshotsSearchItem snapshot : snapshots.items) {
                    MutableComponent itemsMessage = Component.translatable("blockberg_terminal.snapshot_info",snapshot.x,snapshot.y,snapshot.z);
                    HighlightedBlocks.blocks.add(new HighlightedBlocks.HighlightedBlock(new BlockPos(snapshot.x,snapshot.y,snapshot.z), 1.0f,1.0f, 85f /255f,0));
                    for (BarrelItem item : snapshot.items) {
                        MutableComponent itemText = Component.literal("\n- ").withStyle(ChatFormatting.GRAY)
                        .append(item.name).withStyle(ChatFormatting.GOLD)
                        .append(" x " + String.valueOf(item.quantity)).withStyle(ChatFormatting.AQUA);
                        itemsMessage.append(itemText);
                    }
                    try {
                        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                        
                        Date parsedDate = isoFormat.parse(snapshot.recordDate);
                        String formattedDate = sdf.format(parsedDate); 
                    
                        itemsMessage.append(Component.translatable("blockberg_terminal.barrel_recorded_date",formattedDate).withStyle(ChatFormatting.DARK_GRAY));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    client.player.displayClientMessage(itemsMessage, false);
                    MutableComponent paginationText = Component.literal("");
                    if (snapshots.page > 1) {
                        paginationText = paginationText.append(
                            Component.translatable("blockberg_terminal.previous_page")
                                .withStyle(style -> style.withColor(ChatFormatting.AQUA.getColor())
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/searchitems " + (snapshots.page - 1) + " " + itemName)))
                        );
                    }
                    int totalPages = (snapshots.total + snapshots.limit - 1) / snapshots.limit;
                    if (totalPages > snapshots.page) {
                        paginationText = paginationText.append(Component.literal(" "));
                        paginationText = paginationText.append(
                                Component.translatable("blockberg_terminal.next_page")
                                    .withStyle(style -> style.withColor(ChatFormatting.AQUA.getColor())
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            "/searchitems" + (snapshots.page + 1) + " " + itemName)))
                        );
                    }
                    client.player.displayClientMessage(paginationText, false);
                }
            });           
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
