package com.aguilam.blockberg_terminal.feature;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public class ProcessStorageScreen {
    private void processBarrelScreen(AbstractContainerMenu handler) {
        Minecraft client = Minecraft.getInstance();

        if (!(client.hitResult instanceof BlockHitResult)) {
            client.player.displayClientMessage(Component.literal("Нет цели под прицелом."), false);
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
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                String itemName = stack.getHoverName().getString();
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
}
