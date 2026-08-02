package com.aguilam.blockberg_terminal.model;

import net.minecraft.core.BlockPos;

public class Regions {
    public class Region {
        private BlockPosWrapper minPos;
        private BlockPosWrapper maxPos;
        private String regionName;
    
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

    public class BlockPosWrapper {
        private int x, y, z;
        public BlockPosWrapper(BlockPos pos) { this.x = pos.getX(); this.y = pos.getY(); this.z = pos.getZ(); }
        public BlockPos toBlockPos() { return new BlockPos(x, y, z); }
        @Override
        public String toString() { return "(" + x + ", " + y + ", " + z + ")"; }
    }    
}

