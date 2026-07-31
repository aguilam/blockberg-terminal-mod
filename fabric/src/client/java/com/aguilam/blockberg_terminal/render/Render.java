package com.aguilam.blockberg_terminal.render;

public class Render {
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
}
