package com.aguilam.blockberg_terminal.feature;
import com.aguilam.blockberg_terminal.model.Regions.Region;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignText;

public class SignScan {
    private void scanSignsInBounds(Minecraft client, Region region) {
        if (client.level != null && client.player != null && region != null) {
            ClientLevel world = client.level;
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
            sendBarrelDataToServer(barrelDataList);
        }
    }
}
