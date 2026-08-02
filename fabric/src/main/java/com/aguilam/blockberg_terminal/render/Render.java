package com.aguilam.blockberg_terminal.render;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CoreShaders;

import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
public class Render {
    public static void renderHighlightedBlocks(WorldRenderContext context) {
        if (!drawShapeEnabled || highlightedBlocks.isEmpty()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        Camera camera = client.gameRenderer.getMainCamera();
        PoseStack matrices = context.matrixStack();
        matrices.pushPose();
        matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

        RenderSystem.disableDepthTest();

        for (HighlightedBlock block : highlightedBlocks) {
            renderCube(matrices, block);
        }

        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    private void renderCube(PoseStack matrices, HighlightedBlock block) {
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

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.DrawMode.QUADS, VertexFormat.POSITION_COLOR);

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
        

        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8F);
        BufferUploader.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();
    }
}
