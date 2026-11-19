package com.aguilam.blockberg_terminal.feature;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aguilam.blockberg_terminal.model.Barrel.BarrelSearchBody;
import com.aguilam.blockberg_terminal.model.Barrel.BarrelSearchItem;
import com.aguilam.blockberg_terminal.network.DataPost;
import com.aguilam.blockberg_terminal.render.HighlightedBlocks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
public class SearchBarrels {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void searchBarrels(Minecraft client, String searchTerm, int page) {
        try {
            client.player.sendSystemMessage(Component.translatable("blockberg_terminal.search_started",searchTerm,page));

            DataPost.searchBarrels(searchTerm, page).thenAccept(response -> {
                client.execute(() -> {
                    Type listType = new TypeToken<BarrelSearchBody>(){}.getType();
                    BarrelSearchBody offers = GSON.fromJson(response.body(), listType); 
                    HighlightedBlocks.drawShapeEnabled = true;
                    HighlightedBlocks.clearBlocks();
    
                    TextColor[] rankColors = new TextColor[] {
                        TextColor.YELLOW, TextColor.GOLD, TextColor.RED,
                        TextColor.LIGHT_PURPLE, TextColor.AQUA, TextColor.GREEN,
                        TextColor.BLUE, TextColor.DARK_GREEN, TextColor.DARK_PURPLE,
                        TextColor.DARK_RED
                    };
                    client.player.sendSystemMessage(Component.translatable("blockberg_terminal.search_results",offers.total,offers.page));
                    for (int i = 0; i < offers.barrels.length; i++) {
                        BarrelSearchItem offer = offers.barrels[i];
                        TextColor rankColor = rankColors[i % rankColors.length];
                        MutableComponent formattedMessage = Component.literal((i + 1) + ". ")
                                .withColor(rankColor.getValue())
                                .append(Component.literal(offer.name))
                                .withColor(rankColor.getValue())
                                .append(Component.translatable("blockberg_terminal.search_seller", offer.seller))
                                .append(Component.translatable("blockberg_terminal.search_price", offer.price))
                                .append(Component.translatable("blockberg_terminal.search_quantity", offer.quantity))
                                .append(Component.literal(" §7| XYZ: §5" + String.format("(%d, %d, %d)", offer.x, offer.y, offer.z)));
                        try {
                            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        
                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                        
                            Date parsedDate = isoFormat.parse(offer.recordDate);
                            String formattedDate = sdf.format(parsedDate); 
                        
                            formattedMessage = formattedMessage.append(Component.translatable("blockberg_terminal.search_recorded", formattedDate));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        client.player.sendSystemMessage(formattedMessage);
                        if (offer.snapshotsCount > 0) {
                            client.player.sendSystemMessage(Component.translatable("blockberg_terminal.view_barrel_contents")
                                .withColor(0x00AAFF)
                                .withStyle(style -> 
                                    style.withClickEvent(new ClickEvent.RunCommand("/bbt showbarrelcontent " + offer.id))));
                        }
    
                        int colorValue = rankColor.getValue();
                        float rNorm = ((colorValue >> 16) & 0xFF) / 255f;
                        float gNorm = ((colorValue >> 8) & 0xFF) / 255f;
                        float bNorm = (colorValue & 0xFF) / 255f;
                        HighlightedBlocks.blocks.add(new HighlightedBlocks.HighlightedBlock(new BlockPos(offer.x, offer.y, offer.z), rNorm, gNorm, bNorm, i + 1));
                    }
    
                    MutableComponent paginationText = Component.literal("");
                    if (offers.page > 1) {
                        paginationText = paginationText.append(
                            Component.translatable("blockberg_terminal.previous_page")
                                .withColor(TextColor.AQUA)
                                .withStyle(style -> 
                                    style.withClickEvent(new ClickEvent.RunCommand("/searchbarrel " + (offers.page - 1) + " " + searchTerm)))
                        );
                    }
                    int totalPages = (offers.total + offers.limit - 1) / offers.limit;
                    if (totalPages > offers.page) {
                        paginationText = paginationText.append(Component.literal(" "));
                        paginationText = paginationText.append(
                                Component.translatable("blockberg_terminal.next_page")
                                    .withColor(TextColor.AQUA)
                                    .withStyle(style ->
                                        style.withClickEvent(new ClickEvent.RunCommand("/searchbarrel " + (offers.page + 1) + " " + searchTerm)))
                        );
                    }
                    client.player.sendSystemMessage(paginationText);
                });
            }).exceptionally(e -> {
                client.execute(() -> client.player.sendSystemMessage(
                    Component.translatable("blockberg_terminal.error", e.getMessage())
                ));
                return null;
            });
                
        }
        catch (Exception e) {
            client.player.sendSystemMessage(Component.translatable("blockberg_terminal.error", e.getMessage()));
            e.printStackTrace();
        }
    }
}
