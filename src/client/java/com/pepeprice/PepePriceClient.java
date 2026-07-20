package com.pepeprice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.pepeprice.config.ConfigManager;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.Formatting;
import net.minecraft.text.MutableText;
import net.minecraft.block.WallSignBlock;
import net.minecraft.util.math.Direction;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.text.ClickEvent;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.Blocks;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class PepePriceClient implements ClientModInitializer {
    private static BlockPos tempMinPos;
    private static BlockPos tempMaxPos;
    private static final List<Region> regions = new ArrayList<>();

    private static final File regionsFile = new File("regions.json");

    private static final int REGION_MIN_X = -162;
    private static final int REGION_MAX_X = 220;
    private static final int REGION_MIN_Y = 0;
    private static final int REGION_MAX_Y = 5;
    private static final int REGION_MIN_Z = -265;
    private static final int REGION_MAX_Z = 235;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static class BarrelSearchBody {
        int total;
        int page;
        int limit;
        BarrelSearchItem[] barrels;
    }

    private static class BarrelSearchItem {
        int id;
        String name;
        String seller;
        int price;
        int quantity;
        int x;
        int y;
        int z;
        double benefitRation;
        String recordDate;
    }
    private static class BarrelOffer {
        String name;
        String seller;
        double price;
        int quantity;
        int x;
        int y;
        int z;
        double benefitRation;
        BarrelItems[] barrelItems;
    }

    private static class BarrelItems {
        String items;
        int x;
        int y;
        int z;
        String createdAt;
    }
    private static class GlobalData {
        private static List<BarrelOffer> lastOffers = new ArrayList<>();
    
        public static void setLastOffers(List<BarrelOffer> offers) {
            lastOffers = offers;
        }
    
        public static List<BarrelOffer> getLastOffers() {
            return lastOffers;
        }
    }
    private static List<BarrelOffer> highlightedBarrels = new ArrayList<>();

    private static class HighlightedBlock {
        BlockPos pos;
        float red, green, blue, alpha;
        int rank;
        
        public HighlightedBlock(BlockPos pos, float red, float green, float blue, int rank) {
            this.pos = pos;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = 0.8f;
            this.rank = rank;
        }
    }
    
    private final List<HighlightedBlock> highlightedBlocks = new ArrayList<>();
    private boolean isHighlightingActive = false;
    private boolean drawShapeEnabled = false;

    private static final AtomicReference<ScreenHandler> delayedHandler = new AtomicReference<>();
    private static final AtomicInteger delayTicks = new AtomicInteger(0);
    @Override
    public void onInitializeClient() {
        loadRegions();
        ConfigManager.load();
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!ConfigManager.isSendBarrels) return;

            if (screen instanceof GenericContainerScreen) {
                GenericContainerScreen containerScreen = (GenericContainerScreen) screen;
                String title = containerScreen.getTitle().getString();
                if (title.contains("Бочка") || title.contains("Barrel")) {
                    if (client.crosshairTarget instanceof BlockHitResult) {
                        BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
                        BlockPos pos = hitResult.getBlockPos();
                        if (client.world.getBlockState(pos).getBlock() == Blocks.BARREL) {
                            delayedHandler.set(containerScreen.getScreenHandler());
                            delayTicks.set(4); 
                        }
                    }
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (delayTicks.get() > 0) {
                delayTicks.decrementAndGet();
                if (delayTicks.get() == 0 && delayedHandler.get() != null) {
                    processBarrelScreen(delayedHandler.get());
                    delayedHandler.set(null);
                }
            }
        });
        

        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> setMinCommand = LiteralArgumentBuilder
                    .<FabricClientCommandSource>literal("setmin")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player != null) {
                            tempMinPos = client.player.getBlockPos();
                            client.player.sendMessage(Text.literal("Временная минимальная позиция установлена: " + tempMinPos), false);
                        }
                        return 1;
                    });

            LiteralArgumentBuilder<FabricClientCommandSource> allRegionsCommand = LiteralArgumentBuilder
            .<FabricClientCommandSource>literal("allregions")
            .executes(context -> {
                allRegions();
                return 1;
            });

            LiteralArgumentBuilder<FabricClientCommandSource> setMaxCommand = LiteralArgumentBuilder
                    .<FabricClientCommandSource>literal("setmax")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player != null) {
                            tempMaxPos = client.player.getBlockPos();
                            client.player.sendMessage(Text.literal("Временная максимальная позиция установлена: " + tempMaxPos), false);
                        }
                        return 1;
                    });

            LiteralArgumentBuilder<FabricClientCommandSource> addRegionCommand =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("addregion")
                            .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("regionName", StringArgumentType.word())
                                    .executes(context -> {
                                        MinecraftClient client = MinecraftClient.getInstance();
                                        String regionName = StringArgumentType.getString(context, "regionName");
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
                                        return 1;
                                    }));

            LiteralArgumentBuilder<FabricClientCommandSource> scanRegionCommand =
            LiteralArgumentBuilder.<FabricClientCommandSource>literal("scan")
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("regionName", StringArgumentType.word())
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        String regionName = StringArgumentType.getString(context, "regionName");
                        String apiKey = ConfigManager.apiKey;
                        if (client.player != null) {
                            Region region = findRegionByName(regionName);
                            if (region != null) {
                                scanSignsInBounds(client, region, apiKey);
                            } else {
                                client.player.sendMessage(Text.literal("Регион с именем " + regionName + " не найден."), false);
                            }
                        }
                        return 1;
                    })
                );

            LiteralArgumentBuilder<FabricClientCommandSource> searchBarrelCommand =
            LiteralArgumentBuilder.<FabricClientCommandSource>literal("searchbarrel")
                .executes(context -> {
                    highlightedBlocks.clear();
                    return 1;
                })
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, Integer>argument("Страница", IntegerArgumentType.integer(1))
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("Название товара", StringArgumentType.greedyString())
                        .executes(context -> {
                            String searchTerm = StringArgumentType.getString(context, "Название товара");
                            int page = IntegerArgumentType.getInteger(context, "Страница");
                            searchBarrels(MinecraftClient.getInstance(), searchTerm, page);
                            return 1;
                        })
                    )
                )
                .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("Название товара", StringArgumentType.greedyString())
                    .executes(context -> {
                        String searchTerm = StringArgumentType.getString(context, "Название товара");
                        searchBarrels(MinecraftClient.getInstance(), searchTerm, 1);
                        return 1;
                    })
                );
                LiteralArgumentBuilder<FabricClientCommandSource> showBarrelContentCommand =
                LiteralArgumentBuilder.<FabricClientCommandSource>literal("showbarrelcontent")
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, Integer>argument("barrelIndex", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            int barrelIndex = IntegerArgumentType.getInteger(context, "barrelIndex");
                            MinecraftClient client = MinecraftClient.getInstance();
                            List<BarrelOffer> offers = GlobalData.getLastOffers();
                            
                            if (barrelIndex < 0 || barrelIndex >= offers.size()) {
                                client.player.sendMessage(Text.literal("Неверный индекс бочки."), false);
                                return 0;
                            }
                            BarrelOffer offer = offers.get(barrelIndex);
                            
                            MutableText fullContentMessage = Text.literal("Полное содержимое бочки:\n").formatted(Formatting.GRAY)
                                .append(Text.literal("Название: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(offer.name).formatted(Formatting.GOLD)))
                                .append(Text.literal("\nПродавец: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(offer.seller).formatted(Formatting.AQUA)))
                                .append(Text.literal("\nЦена: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(String.format("%.2f", offer.price)).formatted(Formatting.YELLOW)))
                                .append(Text.literal("\nКоличество: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(String.valueOf(offer.quantity)).formatted(Formatting.GREEN)))
                                .append(Text.literal("\nКоординаты: ").formatted(Formatting.WHITE)
                                    .append(Text.literal(String.format("(%d, %d, %d)", offer.x, offer.y, offer.z)).formatted(Formatting.LIGHT_PURPLE)))
                                .append(Text.literal("\n"));
                            
                            if (offer.barrelItems != null && offer.barrelItems.length > 0) {
                                fullContentMessage = fullContentMessage.append(Text.literal("\nСодержимое:").formatted(Formatting.WHITE));
                                for (BarrelItems bi : offer.barrelItems) {
                                    String[] parts = bi.items.split(",");
                                    MutableText biText = Text.literal("\n - ").formatted(Formatting.GRAY);
                                    for (int i = 0; i < parts.length; i += 2) {
                                        if (i + 1 < parts.length) {
                                            String itemName = parts[i].trim();
                                            String itemQuantity = parts[i + 1].trim();
                                            MutableText pairText = Text.literal(itemName)
                                                .styled(style -> style.withColor(Formatting.GOLD.getColorValue()))
                                                .append(Text.literal(" x" + itemQuantity)
                                                .styled(style -> style.withColor(Formatting.AQUA.getColorValue())));
                                            biText = biText.append(pairText);
                                            if (i + 2 < parts.length) {
                                                biText = biText.append(Text.literal(", "));
                                            }
                                        }
                                    }
                                    try {
                                        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                                    
                                        Date parsedDate = isoFormat.parse(bi.createdAt);
                                        String formattedDate = sdf.format(parsedDate); 
                                    
                                        biText = biText.append(Text.literal(" (от " + formattedDate + ")").formatted(Formatting.DARK_GRAY));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    fullContentMessage = fullContentMessage.append(biText);
                                }
                            } else {
                                fullContentMessage = fullContentMessage.append(Text.literal("\nНет содержимого в бочке.").formatted(Formatting.RED));
                            }
                            
                            client.player.sendMessage(fullContentMessage, false);
                            return 1;
                        })
                    );
        

            dispatcher.register(setMinCommand);
            dispatcher.register(setMaxCommand);
            dispatcher.register(addRegionCommand);
            dispatcher.register(scanRegionCommand);
            dispatcher.register(searchBarrelCommand);
            dispatcher.register(showBarrelContentCommand);
            dispatcher.register(allRegionsCommand);
        });

        WorldRenderEvents.LAST.register(this::renderHighlightedBlocks);

    }

    private static Region findRegionByName(String regionName) {
        for (Region region : regions) {
            if (region.getRegionName().equalsIgnoreCase(regionName)) {
                return region;
            }
        }
        return null;
    }
    private static Region allRegions() {
        MinecraftClient client = MinecraftClient.getInstance();
        StringBuilder sb = new StringBuilder();
    
        for (Region region : regions) {
            String regionInfo = region.getRegionName() + " | min: " + region.getMinPos().toShortString() 
                                + ", max: " + region.getMaxPos().toShortString();
            sb.append(regionInfo).append("\n");
        }
        client.player.sendMessage(Text.literal(sb.toString()), false);
        return null;
    }
    
    private void renderHighlightedBlocks(WorldRenderContext context) {
        if (!drawShapeEnabled || highlightedBlocks.isEmpty()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Camera camera = client.gameRenderer.getCamera();
        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);

        RenderSystem.disableDepthTest();

        for (HighlightedBlock block : highlightedBlocks) {
            renderCube(matrices, block);
        }

        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    private void renderCube(MatrixStack matrices, HighlightedBlock block) {
        Matrix4f transformationMatrix = matrices.peek().getPositionMatrix();

        float x0 = block.pos.getX();
        float y0 = block.pos.getY();
        float z0 = block.pos.getZ();
        float x1 = x0 + 1.0f;
        float y1 = y0 + 1.0f;
        float z1 = z0 + 1.0f;

        int a = (int) (block.alpha * 255) & 0xFF;
        int r = (int) (block.red * 255) & 0xFF;
        int g = (int) (block.green * 255) & 0xFF;
        int b = (int) (block.blue * 255) & 0xFF;
        int argb = (a << 24) | (r << 16) | (g << 8) | b;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(transformationMatrix, x0, y0, z1).color(argb);
        buffer.vertex(transformationMatrix, x0, y1, z1).color(argb);
        buffer.vertex(transformationMatrix, x1, y1, z1).color(argb);
        buffer.vertex(transformationMatrix, x1, y0, z1).color(argb);
        
        buffer.vertex(transformationMatrix, x1, y0, z0).color(argb);
        buffer.vertex(transformationMatrix, x1, y1, z0).color(argb);
        buffer.vertex(transformationMatrix, x0, y1, z0).color(argb);
        buffer.vertex(transformationMatrix, x0, y0, z0).color(argb);
        
        buffer.vertex(transformationMatrix, x0, y0, z0).color(argb);
        buffer.vertex(transformationMatrix, x0, y1, z0).color(argb);
        buffer.vertex(transformationMatrix, x0, y1, z1).color(argb);
        buffer.vertex(transformationMatrix, x0, y0, z1).color(argb);
        
        buffer.vertex(transformationMatrix, x1, y0, z1).color(argb);
        buffer.vertex(transformationMatrix, x1, y1, z1).color(argb);
        buffer.vertex(transformationMatrix, x1, y1, z0).color(argb);
        buffer.vertex(transformationMatrix, x1, y0, z0).color(argb);
        
        buffer.vertex(transformationMatrix, x0, y1, z1).color(argb);
        buffer.vertex(transformationMatrix, x0, y1, z0).color(argb);
        buffer.vertex(transformationMatrix, x1, y1, z0).color(argb);
        buffer.vertex(transformationMatrix, x1, y1, z1).color(argb);
        
        buffer.vertex(transformationMatrix, x0, y0, z0).color(argb);
        buffer.vertex(transformationMatrix, x0, y0, z1).color(argb);
        buffer.vertex(transformationMatrix, x1, y0, z1).color(argb);
        buffer.vertex(transformationMatrix, x1, y0, z0).color(argb);
        

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
    }
    
    private void scanSignsInBounds(MinecraftClient client, Region region, String apiKey) {
        if (client.world != null && client.player != null && region != null) {
            ClientWorld world = client.world;
            BlockPos minPos = region.getMinPos();
            BlockPos maxPos = region.getMaxPos();
            int startX = Math.min(minPos.getX(), maxPos.getX());
            int endX = Math.max(minPos.getX(), maxPos.getX());
            int startY = Math.min(minPos.getY(), maxPos.getY());
            int endY = Math.max(minPos.getY(), maxPos.getY());
            int startZ = Math.min(minPos.getZ(), maxPos.getZ());
            int endZ = Math.max(minPos.getZ(), maxPos.getZ());
    
            JsonArray barrelDataList = new JsonArray();
    
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    for (int z = startZ; z <= endZ; z++) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        BlockEntity blockEntity = world.getBlockEntity(blockPos);
                        if (blockEntity instanceof SignBlockEntity sign) {
                            if (sign.getCachedState().getBlock() instanceof WallSignBlock) {
                                Direction signFacing = sign.getCachedState().get(WallSignBlock.FACING);
                                BlockPos attachedBlockPos = blockPos.offset(signFacing.getOpposite());
                                
                                StringBuilder signText = new StringBuilder();
                                SignText signContent = sign.getText(true);
                                for (int i = 0; i < 4; i++) {
                                    String lineText = signContent.getMessage(i, false).getString();
                                    signText.append(lineText).append("\n");
                                }
                                
                                JsonObject json = new JsonObject();
                                json.addProperty("x", attachedBlockPos.getX());
                                json.addProperty("y", attachedBlockPos.getY());
                                json.addProperty("z", attachedBlockPos.getZ());
                                json.addProperty("message", signText.toString().trim());
                                
                                barrelDataList.add(json);
                            }
                        }
                    }
                }
            }
            sendBarrelDataToServer(barrelDataList, apiKey);
        }
    }
    

    private void sendBarrelDataToServer(JsonArray barrelDataList, String apiKey) {
        try {    
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Отправляю " + barrelDataList.size() + " записей..."), false);
            }
        
            HttpClient httpClient = HttpClient.newHttpClient();
            System.out.println(ConfigManager.apiUrl);
            System.out.println(URI.create(ConfigManager.apiUrl));
            System.out.println(URI.create(ConfigManager.apiUrl + "/barrels"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ConfigManager.apiUrl + "/barrels"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(barrelDataList.toString()))
                    .build();
        
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {

                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    

    private void sendBarrelItemsToServer(JsonObject json) {
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

    private void searchBarrels(MinecraftClient client, String searchTerm, int page) {
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
                                    //final int index = i;
                                    //if (offer.barrelItems != null && offer.barrelItems.length > 0) {
                                    //    client.player.sendMessage(Text.literal("[Посмотреть содержимое бочки]")
                                    //        .styled(style -> style.withColor(0x00AAFF)
                                    //        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    //            "/showbarrelcontent " + index))), false);
                                    //}

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
    
    
    private void processBarrelScreen(ScreenHandler handler) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!(client.crosshairTarget instanceof BlockHitResult)) {
            client.player.sendMessage(Text.literal("Нет цели под прицелом."), false);
            return;
        }
        BlockHitResult hitResult = (BlockHitResult) client.crosshairTarget;
        BlockPos pos = hitResult.getBlockPos();

        if (client.world == null || client.world.getBlockState(pos).getBlock() != Blocks.BARREL) {
            return;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (x < 0) {
            x++;
        }
        if (z < 0) {
            z++;
        }

        if (x < REGION_MIN_X || x > REGION_MAX_X ||
            y < REGION_MIN_Y || y > REGION_MAX_Y ||
            z < REGION_MIN_Z || z > REGION_MAX_Z) {
            return;
        }

        JsonArray itemsArray = new JsonArray();
        int totalSlots = Math.min(27, handler.slots.size());
        for (int i = 0; i < totalSlots; i++) {
            var slot = handler.slots.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                String itemName = stack.getName().getString();
                if (itemName.isEmpty() || itemName.equals("item.minecraft.air")) {
                    itemName = stack.getItem().getTranslationKey();
                }
                if (itemName.isEmpty()) {
                    itemName = "Unknown item";
                }
                JsonObject item = new JsonObject();
                item.addProperty("name", itemName);
                item.addProperty("quantity", stack.getCount());
                itemsArray.add(item);
            }
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("items", itemsArray);
        jsonObject.addProperty("x", x);
        jsonObject.addProperty("y", y);
        jsonObject.addProperty("z", z);

        sendBarrelItemsToServer(jsonObject);
    }
    
    private static void saveRegions() {
        try (FileWriter writer = new FileWriter(regionsFile)) {
            gson.toJson(regions, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadRegions() {
        if (regionsFile.exists()) {
            try (FileReader reader = new FileReader(regionsFile)) {
                Type listType = new TypeToken<List<Region>>() {}.getType();
                List<Region> loadedRegions = gson.fromJson(reader, listType);
                if (loadedRegions != null) {
                    regions.clear();
                    regions.addAll(loadedRegions);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Region {
        private BlockPosWrapper minPos;
        private BlockPosWrapper maxPos;
        private String regionName;

        public Region() {}
        public Region(BlockPos minPos, BlockPos maxPos, String regionName) {
            this.minPos = new BlockPosWrapper(minPos);
            this.maxPos = new BlockPosWrapper(maxPos);
            this.regionName = regionName;
        }
        public BlockPos getMinPos() { return minPos.toBlockPos(); }
        public BlockPos getMaxPos() { return maxPos.toBlockPos(); }
        public String getRegionName() { return regionName; }
        @Override
        public String toString() {
            return "{regionName: " + regionName + ", minPos: " + minPos + ", maxPos: " + maxPos + "}";
        }
    }

    private static class BlockPosWrapper {
        private int x, y, z;
        public BlockPosWrapper() {}
        public BlockPosWrapper(BlockPos pos) { this.x = pos.getX(); this.y = pos.getY(); this.z = pos.getZ(); }
        public BlockPos toBlockPos() { return new BlockPos(x, y, z); }
        @Override
        public String toString() { return "(" + x + ", " + y + ", " + z + ")"; }
    }
}