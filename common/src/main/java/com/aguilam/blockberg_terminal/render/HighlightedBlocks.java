package com.aguilam.blockberg_terminal.render;

import net.minecraft.core.BlockPos;

public class HighlightedBlocks {
    public static class HighlightedBlock {
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
}

