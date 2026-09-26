package com.mrailouis.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RoundedRectangleRenderer {
	private RoundedRectangleRenderer() {
	}

	public static void fill(GuiGraphicsExtractor extractor, RenderPipeline pipeline, float x, float y, float width, float height, int color) {
		extractor.guiRenderState.addGuiElement(new RoundedRectangleRenderState(
				pipeline,
				extractor.pose(),
				x,
				y,
				width,
				height,
				color,
				extractor.scissorStack.peek()));
	}
}
