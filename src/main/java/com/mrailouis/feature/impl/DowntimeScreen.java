package com.mrailouis.feature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import com.mrailouis.shader.RoundedPanelPipeline;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DowntimeScreen extends Screen {
	private static final int PANEL_COLOR = 0xD9000000;
	private static final float CORNER_RADIUS = 6.0f;
	private static final float EDGE_SOFTNESS = 1.5f;
	private static final float PANEL_SCREEN_FRACTION = 0.65f;
	private static final float OPEN_ANIMATION_SPEED = 0.18f;

	private RenderPipeline panelPipeline;
	private int panelLeft;
	private int panelTop;
	private int panelRight;
	private int panelBottom;
	private float openAnimationProgress;

	public DowntimeScreen() {
		super(Component.literal("Downtime"));
	}

	@Override
	protected void init() {
		var panelWidth = Math.round(width * PANEL_SCREEN_FRACTION);
		var panelHeight = Math.round(height * PANEL_SCREEN_FRACTION);

		panelLeft = (width - panelWidth) / 2;
		panelTop = (height - panelHeight) / 2;
		panelRight = panelLeft + panelWidth;
		panelBottom = panelTop + panelHeight;

		panelPipeline = RoundedPanelPipeline.build(panelLeft, panelTop, panelRight, panelBottom, CORNER_RADIUS, EDGE_SOFTNESS);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		var openScale = openScale();

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(width * 0.5f, height * 0.5f);
		guiGraphics.pose().scale(openScale, openScale);
		guiGraphics.pose().translate(-width * 0.5f, -height * 0.5f);

		guiGraphics.fill(panelPipeline, panelLeft, panelTop, panelRight, panelBottom, PANEL_COLOR);

		guiGraphics.pose().popMatrix();

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private float openScale() {
		openAnimationProgress += (1.0f - openAnimationProgress) * OPEN_ANIMATION_SPEED;
		if (openAnimationProgress > 0.995f) {
			openAnimationProgress = 1.0f;
		}
		return easeOutBack(openAnimationProgress);
	}

	private static float easeOutBack(float progress) {
		var t = Math.max(0.0f, Math.min(1.0f, progress)) - 1.0f;
		var c = 1.70158f;
		return 1.0f + (c + 1.0f) * t * t * t + c * t * t;
	}
}
