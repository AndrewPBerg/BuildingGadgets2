package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import org.joml.Matrix3x2f;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

/** Template Manager's rotatable preview, submitted through Minecraft 26.2 PiP rendering. */
public final class GuiTemplatePreview extends PictureInPictureRenderer<GuiTemplatePreview.State> {
    @Override
    public Class<State> getRenderStateClass() {
        return State.class;
    }

    @Override
    protected String getTextureLabel() {
        return "bg2 template preview";
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0f;
    }

    @Override
    protected void renderToTexture(State state, PoseStack stack, SubmitNodeCollector collector) {
        ArrayList<StatePos> blocks = state.statePosList;
        ClientLevel level = Minecraft.getInstance().level;
        if (blocks == null || blocks.isEmpty() || level == null) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (StatePos block : blocks) {
            if (block == null || block.pos == null) continue;
            BlockPos pos = block.pos;
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        if (minX == Integer.MAX_VALUE) return;

        float width = Math.max(1, maxX - minX + 1);
        float height = Math.max(1, maxY - minY + 1);
        float depth = Math.max(1, maxZ - minZ + 1);
        float panel = Math.min(state.x1 - state.x0, state.y1 - state.y0);
        float baseScale = panel / (Math.max(width, Math.max(height, depth)) * 2.2f);
        float scale = baseScale * Math.max(0.1f, Math.min(10f, 1f + state.zoom * 0.01f));

        stack.pushPose();
        stack.translate(state.panX, -state.panY, 0);
        stack.scale(scale, scale, scale);
        stack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(state.rotX)));
        stack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(state.rotY)));
        stack.translate(-(minX + maxX + 1) * 0.5f,
                -(minY + maxY + 1) * 0.5f,
                -(minZ + maxZ + 1) * 0.5f);
        PreviewGeometry.submit(level, blocks, BlockPos.ZERO, stack, collector, 1.0f);
        stack.popPose();
    }

    public static final class State implements PictureInPictureRenderState {
        public final int x0, y0, x1, y1;
        public final float scale;
        public final Matrix3x2f pose;
        public final @Nullable ScreenRectangle scissorArea;
        public final @Nullable ScreenRectangle bounds;
        public final float rotX, rotY, zoom, panX, panY;
        public final int guiScale;
        public final @Nullable UUID templateUuid;
        public final @Nullable ArrayList<StatePos> statePosList;

        public State(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea,
                     int guiScale, float rotX, float rotY, float zoom, float panX, float panY,
                     @Nullable UUID templateUuid, @Nullable ArrayList<StatePos> statePosList) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.scale = 1.0f;
            this.pose = pose;
            this.scissorArea = scissorArea;
            this.bounds = PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea);
            this.guiScale = guiScale;
            this.rotX = rotX;
            this.rotY = rotY;
            this.zoom = zoom;
            this.panX = panX;
            this.panY = panY;
            this.templateUuid = templateUuid;
            this.statePosList = statePosList;
        }

        @Override public int x0() { return x0; }
        @Override public int x1() { return x1; }
        @Override public int y0() { return y0; }
        @Override public int y1() { return y1; }
        @Override public float scale() { return scale; }
        @Override public Matrix3x2f pose() { return pose; }
        @Override public @Nullable ScreenRectangle scissorArea() { return scissorArea; }
        @Override public @Nullable ScreenRectangle bounds() { return bounds; }
    }
}
