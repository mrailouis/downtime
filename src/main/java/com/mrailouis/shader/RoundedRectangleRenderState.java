package com.mrailouis.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public record RoundedRectangleRenderState(
		RenderPipeline pipeline,
		TextureSetup textureSetup,
		Matrix3x2fc pose,
		float x,
		float y,
		float width,
		float height,
		int color,
		ScreenRectangle scissorArea,
		ScreenRectangle bounds) implements GuiElementRenderState {
	public RoundedRectangleRenderState(RenderPipeline pipeline, Matrix3x2fc pose, float x, float y, float width, float height, int color, ScreenRectangle scissorArea) {
		this(
				pipeline,
				TextureSetup.noTexture(),
				new Matrix3x2f(pose),
				x,
				y,
				width,
				height,
				color,
				scissorArea,
				new ScreenRectangle(Math.round(x), Math.round(y), Math.round(width), Math.round(height)).transformMaxBounds(pose));
	}

	@Override
	public void buildVertices(VertexConsumer vertexConsumer) {
		var x1 = x + width;
		var y1 = y + height;

		vertexConsumer.addVertexWith2DPose(pose, x, y).setUv(0.0f, 0.0f).setColor(color);
		vertexConsumer.addVertexWith2DPose(pose, x, y1).setUv(0.0f, height).setColor(color);
		vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(width, height).setColor(color);
		vertexConsumer.addVertexWith2DPose(pose, x1, y).setUv(width, 0.0f).setColor(color);
	}
}
