package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.common.items.*;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.*;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.awt.Color;
import java.util.ArrayList;

/** Extracts gadget state and submits previews through the Minecraft 26.2 renderer. */
public final class VBORenderer {
    private VBORenderer() {}

    public record PreviewState(ClientLevel level, BlockPos origin, ArrayList<StatePos> blocks,
                               ArrayList<BlockPos> unavailable, BlockPos selectionStart,
                               BlockPos selectionEnd, Color selectionColor,
                               BlockPos anchor, BlockPos bound) {}

    public static PreviewState extract(ClientLevel level, Player player, ItemStack gadget) {
        if (gadget.isEmpty()) return null;
        BlockHitResult hit = VectorHelper.getLookingAt(player, gadget);
        BlockPos anchor = GadgetNBT.getAnchorPos(gadget);
        boolean hasAnchor = anchor != null && !anchor.equals(GadgetNBT.nullPos);
        BlockPos origin = hasAnchor ? anchor : hit.getBlockPos();
        GlobalPos boundPos = GadgetNBT.getBoundPos(gadget);
        BlockPos bound = boundPos != null && boundPos.dimension().equals(level.dimension()) ? boundPos.pos() : null;

        ArrayList<StatePos> blocks = new ArrayList<>();
        ArrayList<BlockPos> unavailable = new ArrayList<>();
        BlockPos selectionStart = null;
        BlockPos selectionEnd = null;
        Color selectionColor = Color.GREEN;

        if (gadget.getItem() instanceof GadgetDestruction) {
            Direction side = GadgetNBT.getAnchorSide(gadget);
            if (side == null) side = hit.getDirection();
            if (!level.getBlockState(origin).isAir() && level.getBlockState(origin).getBlock() != Registration.RenderBlock.get()) {
                int energy = BuildingUtils.getEnergyStored(gadget);
                int cost = BuildingUtils.getEnergyCost(gadget);
                for (StatePos pos : GadgetUtils.getDestructionArea(level, origin, side, player, gadget)) {
                    if (player.isCreative() || energy >= cost) unavailable.add(pos.pos);
                    energy -= cost;
                }
            }
        } else if (gadget.getItem() instanceof GadgetBuilding || gadget.getItem() instanceof GadgetExchanger) {
            if (!level.getBlockState(origin).isAir() && level.getBlockState(origin).getBlock() != Registration.RenderBlock.get()) {
                BlockState chosen = GadgetNBT.getGadgetBlockState(gadget);
                if (!chosen.isAir()) {
                    BaseMode mode = GadgetNBT.getMode(gadget);
                    blocks = mode.collect(hit.getDirection(), player, origin, chosen);
                    if (!player.isCreative() && boundPos == null && chosen.getFluidState().isEmpty()) {
                        ItemStack needed = GadgetUtils.getItemForBlock(chosen, level, BlockPos.ZERO, player);
                        int available = BuildingUtils.countItemStacks(player, needed);
                        int energy = BuildingUtils.getEnergyStored(gadget);
                        int cost = BuildingUtils.getEnergyCost(gadget);
                        for (StatePos pos : blocks) {
                            if (available <= 0 || energy < cost) unavailable.add(pos.pos.offset(origin));
                            available--;
                            energy -= cost;
                        }
                    }
                }
            }
        } else if (gadget.getItem() instanceof GadgetCopyPaste || gadget.getItem() instanceof GadgetCutPaste) {
            BaseMode mode = GadgetNBT.getMode(gadget);
            String path = mode.getId().getPath();
            if (path.equals("copy") || path.equals("cut")) {
                selectionStart = GadgetNBT.getCopyStartPos(gadget);
                selectionEnd = GadgetNBT.getCopyEndPos(gadget);
                selectionColor = path.equals("copy") ? Color.GREEN : Color.RED;
            } else if (BG2DataClient.isClientUpToDate(gadget)) {
                ArrayList<StatePos> loaded = BG2DataClient.getLookupFromUUID(GadgetNBT.getUUID(gadget));
                if (loaded != null) blocks = loaded;
                origin = origin.above().offset(GadgetNBT.getRelativePaste(gadget));
            }
        }

        return new PreviewState(level, origin, blocks, unavailable, selectionStart,
                selectionEnd, selectionColor, hasAnchor ? anchor : null, bound);
    }

    public static void submit(SubmitCustomGeometryEvent event, PreviewState state) {
        if (state == null) return;
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack stack = event.getPoseStack();
        stack.pushPose();
        stack.translate(-camera.x(), -camera.y(), -camera.z());

        if (state.bound != null) outline(event, stack, state.bound, state.bound, Color.BLUE);
        if (state.anchor != null) outline(event, stack, state.anchor, state.anchor, Color.GREEN);
        if (state.selectionStart != null && state.selectionEnd != null
                && !state.selectionStart.equals(GadgetNBT.nullPos)
                && !state.selectionEnd.equals(GadgetNBT.nullPos)) {
            outline(event, stack, state.selectionStart, state.selectionEnd, state.selectionColor);
        }

        if (!state.blocks.isEmpty()) {
            stack.pushPose();
            stack.translate(state.origin.getX(), state.origin.getY(), state.origin.getZ());
            PreviewGeometry.submit(state.level, state.blocks, state.origin, stack,
                    event.getSubmitNodeCollector(), 0.5f);
            stack.popPose();
        }

        if (!state.unavailable.isEmpty()) {
            event.getSubmitNodeCollector().submitCustomGeometry(stack, OurRenderTypes.MissingBlockOverlay,
                    (pose, out) -> {
                        for (int i = 0; i < Math.min(state.unavailable.size(), PreviewGeometry.MAX_RENDER_BLOCKS); i++) {
                            MyRenderMethods.renderBoxSolid(pose.pose(), out, state.unavailable.get(i),
                                    1, 0, 0, 0.35f);
                        }
                    });
        }
        stack.popPose();
    }

    private static void outline(SubmitCustomGeometryEvent event, PoseStack stack,
                                BlockPos start, BlockPos end, Color color) {
        event.getSubmitNodeCollector().submitCustomGeometry(stack, RenderTypes.lines(),
                (pose, out) -> MyRenderMethods.renderCopy(pose, out, start, end, color));
    }
}
