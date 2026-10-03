package com.direwolf20.buildinggadgets2.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;

import java.awt.Color;

/** Geometry helpers used by the 26.2 submit-node preview path. */
public final class MyRenderMethods {
    private MyRenderMethods() {}

    public static void renderBoxSolid(Matrix4f matrix, VertexConsumer buffer, BlockPos pos,
                                      float red, float green, float blue, float alpha) {
        float x0 = pos.getX() - 0.001f;
        float y0 = pos.getY() - 0.001f;
        float z0 = pos.getZ() - 0.001f;
        float x1 = pos.getX() + 1.001f;
        float y1 = pos.getY() + 1.001f;
        float z1 = pos.getZ() + 1.001f;
        quad(matrix, buffer, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, red, green, blue, alpha);
        quad(matrix, buffer, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, red, green, blue, alpha);
        quad(matrix, buffer, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, red, green, blue, alpha);
        quad(matrix, buffer, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, red, green, blue, alpha);
        quad(matrix, buffer, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, red, green, blue, alpha);
        quad(matrix, buffer, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, red, green, blue, alpha);
    }

    private static void quad(Matrix4f pose, VertexConsumer out,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float red, float green, float blue, float alpha) {
        out.addVertex(pose, ax, ay, az).setColor(red, green, blue, alpha);
        out.addVertex(pose, bx, by, bz).setColor(red, green, blue, alpha);
        out.addVertex(pose, cx, cy, cz).setColor(red, green, blue, alpha);
        out.addVertex(pose, dx, dy, dz).setColor(red, green, blue, alpha);
    }

    public static void renderCopy(PoseStack.Pose pose, VertexConsumer out,
                                  BlockPos start, BlockPos end, Color color) {
        if (start == null || end == null) return;
        int x0 = Math.min(start.getX(), end.getX());
        int y0 = Math.min(start.getY(), end.getY());
        int z0 = Math.min(start.getZ(), end.getZ());
        int x1 = Math.max(start.getX(), end.getX()) + 1;
        int y1 = Math.max(start.getY(), end.getY()) + 1;
        int z1 = Math.max(start.getZ(), end.getZ()) + 1;
        line(pose, out, x0, y0, z0, x1, y0, z0, color);
        line(pose, out, x0, y0, z0, x0, y1, z0, color);
        line(pose, out, x0, y0, z0, x0, y0, z1, color);
        line(pose, out, x1, y1, z1, x0, y1, z1, color);
        line(pose, out, x1, y1, z1, x1, y0, z1, color);
        line(pose, out, x1, y1, z1, x1, y1, z0, color);
        line(pose, out, x1, y0, z0, x1, y1, z0, color);
        line(pose, out, x1, y0, z0, x1, y0, z1, color);
        line(pose, out, x0, y1, z0, x1, y1, z0, color);
        line(pose, out, x0, y1, z0, x0, y1, z1, color);
        line(pose, out, x0, y0, z1, x1, y0, z1, color);
        line(pose, out, x0, y0, z1, x0, y1, z1, color);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer out,
                             float ax, float ay, float az, float bx, float by, float bz, Color color) {
        out.addVertex(pose, ax, ay, az).setColor(color.getRGB()).setNormal(pose, 0, 1, 0).setLineWidth(2);
        out.addVertex(pose, bx, by, bz).setColor(color.getRGB()).setNormal(pose, 0, 1, 0).setLineWidth(2);
    }
}
