package com.aguilam.blockberg_terminal.feature;
import java.util.List;

import com.aguilam.blockberg_terminal.model.Regions.Region;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;

public class SignScan {
    public static JsonArray scanSignsInBounds(Minecraft client, Region region) {
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
                    if (blockEntity instanceof SignBlockEntity sign && sign.getBlockState().getBlock() instanceof WallSignBlock) {
                        Direction signFacing = sign.getBlockState().getValue(WallSignBlock.FACING);
                        BlockPos attachedBlockPos = blockPos.relative(signFacing.getOpposite());
                        BlockState storage = world.getBlockState(attachedBlockPos);
                        if(storage.getBlock() instanceof BarrelBlock) {
                            StringBuilder signText = new StringBuilder();
                            List<Component> signContent = sign.getText(SignTextSlot.FRONT).getMessages(false);
                            boolean hasText = false;
                            for (int i = 0; i < 4; i++) {
                                String lineText = signContent.get(i).getString();
                                if (lineText.isBlank()) {
                                    continue;
                                }
                                hasText = true;
                                signText.append(lineText);
                                if (i < 3) {
                                    signText.append("\n");
                                }
                            }
                            if (!hasText) {
                                continue;
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
        return barrelDataList;
    }
}
