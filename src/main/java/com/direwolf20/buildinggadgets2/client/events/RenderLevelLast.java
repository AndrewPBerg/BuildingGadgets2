package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.client.renderer.VBORenderer;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

public final class RenderLevelLast {
    private static final ContextKey<VBORenderer.PreviewState> PREVIEW_KEY = new ContextKey<>(
            Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, "preview"));

    private RenderLevelLast() {}

    @SubscribeEvent
    public static void extract(ExtractLevelRenderStateEvent event) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack gadget = BaseGadget.getGadget(player);
        if (gadget.isEmpty()) return;
        var preview = VBORenderer.extract(event.getLevel(), player, gadget);
        if (preview != null) event.getRenderState().setRenderData(PREVIEW_KEY, preview);
    }

    @SubscribeEvent
    public static void submit(SubmitCustomGeometryEvent event) {
        VBORenderer.submit(event, event.getLevelRenderState().getRenderData(PREVIEW_KEY));
    }
}
