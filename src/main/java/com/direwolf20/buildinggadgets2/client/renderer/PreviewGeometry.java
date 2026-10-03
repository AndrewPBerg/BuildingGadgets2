package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.util.FakeRenderingWorld;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.List;

/** Submits bounded ghost geometry through the Minecraft 26.2 render-state pipeline. */
public final class PreviewGeometry {
    // A preview remains responsive for large templates; the actual paste queue is not limited here.
    public static final int MAX_RENDER_BLOCKS = 2048;

    private PreviewGeometry() {}

    public static void submit(ClientLevel level, List<StatePos> blocks, BlockPos origin,
                              PoseStack stack, SubmitNodeCollector collector, float alpha) {
        if (blocks == null || blocks.isEmpty()) return;

        ArrayList<StatePos> positions = new ArrayList<>(Math.min(blocks.size(), MAX_RENDER_BLOCKS));
        for (StatePos block : blocks) {
            if (block == null || block.pos == null || block.state == null || block.state.isAir()) continue;
            positions.add(block);
            if (positions.size() == MAX_RENDER_BLOCKS) break;
        }
        if (positions.isEmpty()) return;
        FakeRenderingWorld fake = new FakeRenderingWorld(level, positions, origin);
        BlockAndTintGetter tintWorld = new FakeWorldTintAdapter(fake, level);
        ModelBlockRenderer renderer = new ModelBlockRenderer(true, false, Minecraft.getInstance().getBlockColors());

        for (int i = 0; i < positions.size(); i++) {
            StatePos position = positions.get(i);
            if (position == null || position.pos == null) continue;
            BlockState state = fake.getBlockStateWithoutReal(position.pos);
            if (state == null || state.isAir()) continue;

            stack.pushPose();
            stack.translate(position.pos.getX(), position.pos.getY(), position.pos.getZ());

            if (!state.getFluidState().isEmpty()) {
                collector.submitCustomGeometry(stack, OurRenderTypes.MissingBlockOverlay,
                        (pose, out) -> MyRenderMethods.renderBoxSolid(pose.pose(), out, BlockPos.ZERO,
                                0.2f, 0.4f, 1.0f, alpha));
            } else {
                BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
                BlockPos worldPos = position.pos.offset(origin);
                long seed = state.getSeed(worldPos);
                collector.submitCustomGeometry(stack, OurRenderTypes.RenderBlockFade, (pose, out) -> {
                    try {
                        renderer.tesselateBlock((x, y, z, quad, instance) -> {
                            for (int vertex = 0; vertex < 4; vertex++) {
                                instance.setColor(vertex, ARGB.color(alpha, instance.getColor(vertex)));
                            }
                            out.putBakedQuad(pose, quad, instance);
                        }, 0, 0, 0, tintWorld, position.pos, state, model, seed);
                    } catch (RuntimeException ignored) {
                        // Some modded models require a real world. Keep the rest of the preview visible.
                    }
                });
            }
            stack.popPose();
        }
    }

    private static final class FakeWorldTintAdapter implements BlockAndTintGetter {
        private final FakeRenderingWorld fake;
        private final ClientLevel real;

        private FakeWorldTintAdapter(FakeRenderingWorld fake, ClientLevel real) {
            this.fake = fake;
            this.real = real;
        }

        @Override public CardinalLighting cardinalLighting() { return CardinalLighting.DEFAULT; }
        @Override public int getBlockTint(BlockPos pos, ColorResolver color) { return real.getBlockTint(pos, color); }
        @Override public BlockEntity getBlockEntity(BlockPos pos) { return fake.getBlockEntity(pos); }
        @Override public BlockState getBlockState(BlockPos pos) { return fake.getBlockState(pos); }
        @Override public FluidState getFluidState(BlockPos pos) { return fake.getFluidState(pos); }
        @Override public int getHeight() { return real.getHeight(); }
        @Override public int getMinY() { return real.getMinY(); }
        @Override public LevelLightEngine getLightEngine() { return real.getLightEngine(); }
    }
}
