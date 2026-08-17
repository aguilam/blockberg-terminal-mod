package com.aguilam.blockberg_terminal.feature;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

import com.aguilam.blockberg_terminal.config.ConfigManager;
import com.aguilam.blockberg_terminal.network.DataPost;;
public class ProcessStorageScreen {
    private static AbstractContainerMenu delayedHandler = null;
    private static int delayTicks = 0;

    public static void checkStorageScreen(Screen screen, Minecraft client) {
        if (!ConfigManager.isSendBarrels) return;

        if (screen instanceof ContainerScreen) {
            ContainerScreen containerScreen = (ContainerScreen) screen;
            Component title = containerScreen.getTitle();
            if (title.getContents() instanceof TranslatableContents translatable && "container.barrel".equals(translatable.getKey())) {
                if (client.hitResult instanceof BlockHitResult) {
                    BlockHitResult hitResult = (BlockHitResult) client.hitResult;
                    BlockPos pos = hitResult.getBlockPos();
                    if (client.level.getBlockState(pos).getBlock() == Blocks.BARREL) {
                        delayedHandler = containerScreen.getMenu();
                        delayTicks = 4; 
                    }
                }
            }
        }
    }

    public static void checkStorageTick() {
        if (delayTicks > 0) {
            delayTicks--;
            if (delayTicks == 0 && delayedHandler != null) {
                ProcessStorageScreen.processBarrelScreen(delayedHandler);
                delayedHandler = null;
            }
        }
    }
    
    public static void processBarrelScreen(AbstractContainerMenu handler) {
        try {
            Minecraft client = Minecraft.getInstance();

            if (!(client.hitResult instanceof BlockHitResult)) {
                return;
            }
            BlockHitResult hitResult = (BlockHitResult) client.hitResult;
            BlockPos pos = hitResult.getBlockPos();
    
            if (client.level == null || client.level.getBlockState(pos).getBlock() != Blocks.BARREL) {
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
    
            //if (x < REGION_MIN_X || x > REGION_MAX_X ||
            //    y < REGION_MIN_Y || y > REGION_MAX_Y ||
            //    z < REGION_MIN_Z || z > REGION_MAX_Z) {
            //    return;
            //}
    
            JsonArray itemsArray = new JsonArray();
            int totalSlots = Math.min(27, handler.slots.size());
            for (int i = 0; i < totalSlots; i++) {
                var slot = handler.slots.get(i);
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty()) {
                    String itemName = stack.getHoverName().getString();
                    if (itemName.isEmpty() || itemName.equals("item.minecraft.air")) {
                        itemName = stack.getItem().getDescriptionId();
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
    
            DataPost.sendBarrelItemsToServer(jsonObject);
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
