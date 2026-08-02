package com.aguilam.blockberg_terminal.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;

public class DataPost {
    public void postBarrelData(JsonArray barrelDataList) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ConfigManager.apiUrl + "/barrels"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + ConfigManager.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(barrelDataList.toString()))
                .build();
    
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {

            })
            .exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
    }

    public static void sendBarrelItemsToServer(JsonObject json) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ConfigManager.apiUrl + "/barrels/items"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
                    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        System.out.println("STATUS: " + response.statusCode());
                        System.out.println("BODY: " + response.body());
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void getBarrelInfo(Minecraft client, int barrelid) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            String url = ConfigManager.apiUrl + "/barrels/" + barrelid;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    try {
                        Type type = new TypeToken<BarrelInfo>(){}.getType();
                        BarrelInfo info = gson.fromJson(response.body(), type);
                        MutableText fullContentMessage = Text.literal("Полное содержимое бочки:\n").formatted(Formatting.GRAY)
                                .append(Text.literal("Название: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(info.name).formatted(Formatting.GOLD)))
                                .append(Text.literal("\nПродавец: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(info.seller).formatted(Formatting.AQUA)))
                                .append(Text.literal("\nЦена: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(String.format("%d", info.price)).formatted(Formatting.YELLOW)))
                                .append(Text.literal("\nКоличество: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(String.valueOf(info.quantity)).formatted(Formatting.GREEN)))
                                .append(Text.literal("\nКоординаты: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(String.format("(%d, %d, %d)", info.x, info.y, info.z)).formatted(Formatting.LIGHT_PURPLE)))
                                .append(Text.literal("\n"));
                            
                            if (info.barrelItems != null) {
                                fullContentMessage = fullContentMessage.append(Text.literal("\nСодержимоe:").formatted(Formatting.WHITE));
                                for (BarrelItem item : info.barrelItems.items) {
                                    MutableText itemText = Text.literal("\n- ").formatted(Formatting.GRAY)
                                        .append(item.name).formatted(Formatting.GOLD)
                                        .append(" x " + String.valueOf(item.quantity)).formatted(Formatting.AQUA);
                                    fullContentMessage.append(itemText);
                                }
                                try {
                                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                    
                                    Date parsedDate = isoFormat.parse(info.barrelItems.recordDate);
                                    String formattedDate = sdf.format(parsedDate); 
                                
                                    fullContentMessage = fullContentMessage.append(Text.literal(" (от " + formattedDate + ")").formatted(Formatting.DARK_GRAY));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                fullContentMessage = fullContentMessage.append(Text.literal("\nСодержимое в бочке не известно").formatted(Formatting.RED));
                            }
                            client.player.sendMessage(fullContentMessage, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void searchBarrels(Minecraft client, String searchTerm, int page) {
        if (client.player == null) return;

        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            String encodedSearchTerm = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            String requestUrl = ConfigManager.apiUrl + "/barrels" + "?query=" + encodedSearchTerm + "&page=" + page + "&page_size=10";
            client.player.sendMessage(Text.literal("§a[BarrelSearch] §fПоиск по запросу: §e" + searchTerm + " §f(Страница " + page + ")..."), false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        try {
                            Type listType = new TypeToken<BarrelSearchBody>(){}.getType();
                            BarrelSearchBody offers = gson.fromJson(response.body(), listType); 
                            client.execute(() -> {
                                drawShapeEnabled = true;
                                highlightedBlocks.clear();

                                Formatting[] rankColors = new Formatting[] {
                                    Formatting.YELLOW, Formatting.GOLD, Formatting.RED,
                                    Formatting.LIGHT_PURPLE, Formatting.AQUA, Formatting.GREEN,
                                    Formatting.BLUE, Formatting.DARK_GREEN, Formatting.DARK_PURPLE,
                                    Formatting.DARK_RED
                                };

                                client.player.sendMessage(Text.literal("§a[BarrelSearch] §fНайдено " + offers.total + "" + " предложений (Страница " + offers.page + "):"), false);
                                for (int i = 0; i < offers.barrels.length; i++) {
                                    BarrelSearchItem offer = offers.barrels[i];
                                    Formatting rankColor = rankColors[i % rankColors.length];
                                
                                    MutableText formattedMessage = Text.literal((i + 1) + ". ")
                                            .styled(style -> style.withColor(rankColor.getColorValue()))
                                            .append(Text.literal(offer.name)
                                            .styled(style -> style.withColor(rankColor.getColorValue())))
                                            .append(Text.literal(" §7| Продавец: §6" + offer.seller))
                                            .append(Text.literal(" §7| Цена: §e" + offer.price))
                                            .append(Text.literal(" §7| Кол-во: §b" + offer.quantity))
                                            .append(Text.literal(" §7| XYZ: §5" + String.format("(%d, %d, %d)", offer.x, offer.y, offer.z)));
                                    try {
                                        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                    
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                    
                                        Date parsedDate = isoFormat.parse(offer.recordDate);
                                        String formattedDate = sdf.format(parsedDate); 
                                    
                                        formattedMessage = formattedMessage.append(Text.literal(" §7| Recorded: §9" + formattedDate));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    client.player.sendMessage(formattedMessage, false);
                                    if (offer.snapshotsCount > 0) {
                                        client.player.sendMessage(Text.literal("[Посмотреть содержимое бочки]")
                                            .styled(style -> style.withColor(0x00AAFF)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                "/showbarrelcontent " + offer.id))), false);
                                    }

                                    Integer colorValue = rankColor.getColorValue();
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
                                    highlightedBlocks.add(new HighlightedBlock(new BlockPos(offer.x, offer.y, offer.z), rNorm, gNorm, bNorm, i + 1));
                                }

                                MutableText paginationText = Text.literal("");
                                if (offers.page > 1) {
                                    paginationText = paginationText.append(
                                        Text.literal("[Предыдущая страница]")
                                            .styled(style -> style.withColor(Formatting.AQUA.getColorValue())
                                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                    "/searchbarrel " + (offers.page - 1) + " " + searchTerm)))
                                    );
                                }
                                int totalPages = (offers.total + offers.limit - 1) / offers.limit;
                                if (totalPages > offers.page) {
                                    paginationText = paginationText.append(Text.literal(" "));
                                    paginationText = paginationText.append(
                                            Text.literal("[Следующая страница]")
                                                .styled(style -> style.withColor(Formatting.AQUA.getColorValue())
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                        "/searchbarrel " + (offers.page + 1) + " " + searchTerm)))
                                    );
                                }
                                client.player.sendMessage(paginationText, false);
                            });
                        } catch (Exception e) {
                            client.execute(() -> {
                                client.player.sendMessage(Text.literal("§cОшибка при обработке ответа: " + e.getMessage()), false);
                            });
                            e.printStackTrace();
                        }
                    })
                    .exceptionally(e -> {
                        client.execute(() -> {
                            client.player.sendMessage(Text.literal("§cОшибка при отправке запроса: " + e.getMessage()), false);
                        });
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            client.player.sendMessage(Text.literal("§cОшибка: " + e.getMessage()), false);
            e.printStackTrace();
        }
    }
}