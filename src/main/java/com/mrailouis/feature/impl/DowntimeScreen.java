package com.mrailouis.feature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mrailouis.shader.AwtFontRenderer;
import com.mrailouis.shader.DowntimeRenderPipelines;
import com.mrailouis.shader.RoundedRectangleRenderer;
import java.awt.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DowntimeScreen extends Screen {
	private static final int PANEL_COLOR = 0xFF1D2C45;
	private static final int BORDER_COLOR = 0xFF3D3D3D;
	private static final int SHADOW_COLOR = 0x60000000;
	private static final int TITLE_COLOR = 0xFFFFFFFF;
	private static final float CORNER_RADIUS = 6.0f;
	private static final float BORDER_RADIUS = 7.0f;
	private static final float PANEL_SCREEN_FRACTION = 0.65f;
	private static final float OPEN_ANIMATION_SPEED = 0.18f;
	private static final int TOP_BAR_HEIGHT = 29;
	private static final int PANEL_GAP = 7;
	private static final int BORDER_WIDTH = 1;
	private static final int SHADOW_WIDTH = 10;
	private static final int TOP_BAR_PADDING = 12;
	private static final AwtFontRenderer TITLE_FONT = AwtFontRenderer.create("/assets/downtime/fonts/inter_variable.ttf", Font.BOLD, 13.0f);

	private RenderPipeline topBarShadowPipeline;
	private RenderPipeline topBarBorderPipeline;
	private RenderPipeline topBarPipeline;
	private RenderPipeline mainPanelShadowPipeline;
	private RenderPipeline mainPanelBorderPipeline;
	private RenderPipeline mainPanelPipeline;
	private int panelLeft;
	private int panelTop;
	private int panelWidth;
	private int mainPanelTop;
	private int mainPanelHeight;
	private float openAnimationProgress;

	public DowntimeScreen() {
		super(Component.literal("Downtime"));
	}

	@Override
	protected void init() {
		panelWidth = Math.round(width * PANEL_SCREEN_FRACTION);
		var panelHeight = Math.round(height * PANEL_SCREEN_FRACTION);

		panelLeft = (width - panelWidth) / 2;
		panelTop = (height - panelHeight) / 2;
		mainPanelTop = panelTop + TOP_BAR_HEIGHT + PANEL_GAP;
		mainPanelHeight = panelHeight - TOP_BAR_HEIGHT - PANEL_GAP;

		topBarShadowPipeline = DowntimeRenderPipelines.roundedRectangleShadow(
				"gui_top_bar_shadow",
				panelWidth + BORDER_WIDTH * 2 + SHADOW_WIDTH * 2,
				TOP_BAR_HEIGHT + BORDER_WIDTH * 2 + SHADOW_WIDTH * 2,
				BORDER_RADIUS,
				SHADOW_WIDTH);
		topBarBorderPipeline = DowntimeRenderPipelines.roundedRectangle("gui_top_bar_border", panelWidth + BORDER_WIDTH * 2, TOP_BAR_HEIGHT + BORDER_WIDTH * 2, BORDER_RADIUS);
		topBarPipeline = DowntimeRenderPipelines.roundedRectangle("gui_top_bar", panelWidth, TOP_BAR_HEIGHT, CORNER_RADIUS);

		mainPanelShadowPipeline = DowntimeRenderPipelines.roundedRectangleShadow(
				"gui_main_panel_shadow",
				panelWidth + BORDER_WIDTH * 2 + SHADOW_WIDTH * 2,
				mainPanelHeight + BORDER_WIDTH * 2 + SHADOW_WIDTH * 2,
				BORDER_RADIUS,
				SHADOW_WIDTH);
		mainPanelBorderPipeline = DowntimeRenderPipelines.roundedRectangle("gui_main_panel_border", panelWidth + BORDER_WIDTH * 2, mainPanelHeight + BORDER_WIDTH * 2, BORDER_RADIUS);
		mainPanelPipeline = DowntimeRenderPipelines.roundedRectangle("gui_main_panel", panelWidth, mainPanelHeight, CORNER_RADIUS);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		var openScale = openScale();

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(width * 0.5f, height * 0.5f);
		guiGraphics.pose().scale(openScale, openScale);
		guiGraphics.pose().translate(-width * 0.5f, -height * 0.5f);

		drawPanel(guiGraphics, topBarShadowPipeline, topBarBorderPipeline, topBarPipeline, panelLeft, panelTop, panelWidth, TOP_BAR_HEIGHT);
		drawPanel(guiGraphics, mainPanelShadowPipeline, mainPanelBorderPipeline, mainPanelPipeline, panelLeft, mainPanelTop, panelWidth, mainPanelHeight);

		TITLE_FONT.draw(guiGraphics, "Downtime", panelLeft + TOP_BAR_PADDING, panelTop + centeredY(TOP_BAR_HEIGHT, TITLE_FONT.height("Downtime")), TITLE_COLOR);

		guiGraphics.pose().popMatrix();

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static void drawPanel(GuiGraphicsExtractor guiGraphics, RenderPipeline shadowPipeline, RenderPipeline borderPipeline, RenderPipeline pipeline, int x, int y, int width, int height) {
		RoundedRectangleRenderer.fill(guiGraphics, shadowPipeline, x - BORDER_WIDTH - SHADOW_WIDTH, y - BORDER_WIDTH - SHADOW_WIDTH, width + BORDER_WIDTH * 2 + SHADOW_WIDTH * 2, height + BORDER_WIDTH * 2 + SHADOW_WIDTH * 2, SHADOW_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, borderPipeline, x - BORDER_WIDTH, y - BORDER_WIDTH, width + BORDER_WIDTH * 2, height + BORDER_WIDTH * 2, BORDER_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, pipeline, x, y, width, height, PANEL_COLOR);
	}

	private static int centeredY(int outerHeight, int innerHeight) {
		return (outerHeight - innerHeight) / 2;
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
