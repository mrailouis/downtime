package com.mrailouis.feature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import com.mrailouis.shader.DowntimeRenderPipelines;
import com.mrailouis.shader.ScreenBlurRenderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TestRollScreen extends Screen {
	private static final float CIRCLE_HEIGHT_FRACTION = 0.275f;
	private static final float CIRCLE_EDGE_SOFTNESS = 2.0f;

	private RenderPipeline blurPipeline;

	public TestRollScreen() {
		super(Component.literal("Downtime Roll"));
	}

	@Override
	protected void init() {
		var circleCenterX = width * 0.5f;
		var circleCenterY = height * 0.5f;
		var circleRadius = height * CIRCLE_HEIGHT_FRACTION;

		blurPipeline = DowntimeRenderPipelines.fullScreenBlur("gui_test_roll_blur", circleCenterX, circleCenterY, circleRadius, CIRCLE_EDGE_SOFTNESS);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		ScreenBlurRenderer.draw(guiGraphics, blurPipeline, 0, 0, width, height, 0xFFFFFFFF);

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
