package com.aguilam.blockberg_terminal.render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;

import com.aguilam.blockberg_terminal.render.HighlightedBlocks.HighlightedBlock;

public class RenderBlocks {
    public static void renderHighlightedBlocks(SubmitNodeCollector collector,CameraRenderState camera) {
        if (!HighlightedBlocks.drawShapeEnabled || HighlightedBlocks.blocks.isEmpty()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        try (Gizmos.TemporaryCollection _ = client.levelRenderer.collectPerFrameRenderThreadGizmos()) {
            for (HighlightedBlock block : HighlightedBlocks.blocks) {
                int a = (int) (block.alpha * 255) & 0xFF;
                int r = (int) (block.red * 255) & 0xFF;
                int g = (int) (block.green * 255) & 0xFF;
                int b = (int) (block.blue * 255) & 0xFF;
                int argb = (a << 24) | (r << 16) | (g << 8) | b;

                Gizmos.cuboid(block.pos, GizmoStyle.fill(argb)).setAlwaysOnTop();
            }
        }
    }
}
