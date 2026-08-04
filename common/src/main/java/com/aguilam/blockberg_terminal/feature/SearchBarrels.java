package com.aguilam.blockberg_terminal.feature;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.aguilam.blockberg_terminal.model.Barrel.BarrelSearchBody;
import com.aguilam.blockberg_terminal.model.Barrel.BarrelSearchItem;
import com.aguilam.blockberg_terminal.network.DataPost;
import com.aguilam.blockberg_terminal.render.HighlightedBlocks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
public class SearchBarrels {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void searchBarrels(Minecraft client, String searchTerm, int page) {
        try {
            client.player.displayClientMessage(Component.literal("§a[BarrelSearch] §fПоиск по запросу: §e" + searchTerm + " §f(Страница " + page + ")..."), false);

            DataPost.searchBarrels(searchTerm, page).thenAccept(response -> {
                Type listType = new TypeToken<BarrelSearchBody>(){}.getType();
                BarrelSearchBody offers = GSON.fromJson(response.body(), listType); 
                HighlightedBlocks.drawShapeEnabled = true;
                HighlightedBlocks.clearBlocks();

                ChatFormatting[] rankColors = new ChatFormatting[] {
                    ChatFormatting.YELLOW, ChatFormatting.GOLD, ChatFormatting.RED,
                    ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, ChatFormatting.GREEN,
                    ChatFormatting.BLUE, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_PURPLE,
                    ChatFormatting.DARK_RED
                };

                client.player.displayClientMessage(Component.literal("§a[BarrelSearch] §fНайдено " + offers.total + "" + " предложений (Страница " + offers.page + "):"), false);
                for (int i = 0; i < offers.barrels.length; i++) {
                    BarrelSearchItem offer = offers.barrels[i];
                    ChatFormatting rankColor = rankColors[i % rankColors.length];
                    MutableComponent formattedMessage = Component.literal((i + 1) + ". ")
                            .withStyle(style -> style.withColor(rankColor.getColor()))
                            .append(Component.literal(offer.name)
                            .withStyle(style -> style.withColor(rankColor.getColor())))
                            .append(Component.literal(" §7| Продавец: §6" + offer.seller))
                            .append(Component.literal(" §7| Цена: §e" + offer.price))
                            .append(Component.literal(" §7| Кол-во: §b" + offer.quantity))
                            .append(Component.literal(" §7| XYZ: §5" + String.format("(%d, %d, %d)", offer.x, offer.y, offer.z)));
                    try {
                        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    
                        Date parsedDate = isoFormat.parse(offer.recordDate);
                        String formattedDate = sdf.format(parsedDate); 
                    
                        formattedMessage = formattedMessage.append(Component.literal(" §7| Recorded: §9" + formattedDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    client.player.displayClientMessage(formattedMessage, false);
                    if (offer.snapshotsCount > 0) {
                        client.player.displayClientMessage(Component.literal("[Посмотреть содержимое бочки]")
                            .withStyle(style -> style.withColor(0x00AAFF)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/showbarrelcontent " + offer.id))), false);
                    }

                    Integer colorValue = rankColor.getColor();
                    if (colorValue == null) {
                        colorValue = switch(rankColor) {
                            case YELLOW       -> 0xFFFF55;
                            case GOLD         -> 0xFFAA00;
                            case RED          -> 0xFF5555;
                            case LIGHT_PURPLE -> 0xFF55FF;
                            case AQUA         -> 0x55FFFF;
                            case GREEN        -> 0x55FF55;
                            case BLUE         -> 0x5555FF;
                            case DARK_GREEN   -> 0x00AA00;
                            case DARK_PURPLE  -> 0xAA00AA;
                            case DARK_RED     -> 0xAA0000;
                            default           -> 0xFFFFFF;
                        };
                    }
                    float rNorm = ((colorValue >> 16) & 0xFF) / 255f;
                    float gNorm = ((colorValue >> 8) & 0xFF) / 255f;
                    float bNorm = (colorValue & 0xFF) / 255f;
                    HighlightedBlocks.blocks.add(new HighlightedBlocks.HighlightedBlock(new BlockPos(offer.x, offer.y, offer.z), rNorm, gNorm, bNorm, i + 1));
                }

                MutableComponent paginationText = Component.literal("");
                if (offers.page > 1) {
                    paginationText = paginationText.append(
                        Component.literal("[Предыдущая страница]")
                            .withStyle(style -> style.withColor(ChatFormatting.AQUA.getColor())
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/searchbarrel " + (offers.page - 1) + " " + searchTerm)))
                    );
                }
                int totalPages = (offers.total + offers.limit - 1) / offers.limit;
                if (totalPages > offers.page) {
                    paginationText = paginationText.append(Component.literal(" "));
                    paginationText = paginationText.append(
                            Component.literal("[Следующая страница]")
                                .withStyle(style -> style.withColor(ChatFormatting.AQUA.getColor())
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/searchbarrel " + (offers.page + 1) + " " + searchTerm)))
                    );
                }
                client.player.displayClientMessage(paginationText, false);
            });
        }
        catch (Exception e) {
            client.player.displayClientMessage(Component.literal("§cОшибка: " + e.getMessage()), false);
            e.printStackTrace();
        }
    }
}
