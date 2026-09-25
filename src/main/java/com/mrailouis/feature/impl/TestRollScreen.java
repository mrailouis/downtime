package com.mrailouis.feature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import com.mrailouis.data.SkyblockRarity;
import com.mrailouis.shader.DowntimeRenderPipelines;
import com.mrailouis.shader.RoundedRectangleRenderer;
import com.mrailouis.shader.ScreenBlurRenderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TestRollScreen extends Screen {
	private static final float CIRCLE_HEIGHT_FRACTION = 0.275f;
	private static final float CIRCLE_EDGE_SOFTNESS = 2.0f;
	private static final float CIRCLE_OVERLAY_ALPHA = 0.25f;

	private static final int CARD_WIDTH = 115;
	private static final int CARD_HEIGHT = 77;
	private static final float CARD_RADIUS = 0.0f;
	private static final float CARD_BAR_HEIGHT = 6.0f;
	private static final float CARD_FADE_HEIGHT = 36.0f;
	private static final float CARD_ZOOM = 1.15f;
	private static final float ICON_SCALE = 2.4f;
	private static final ItemStack EXAMPLE_ITEM = new ItemStack(Items.NETHER_STAR);
	private static final SkyblockRarity EXAMPLE_RARITY = SkyblockRarity.LEGENDARY;

	private RenderPipeline blurPipeline;
	private RenderPipeline cardPipeline;
	private int cardLeft;
	private int cardTop;

	public TestRollScreen() {
		super(Component.literal("Downtime Roll"));
	}

	@Override
	protected void init() {
		var circleCenterX = width * 0.5f;
		var circleCenterY = height * 0.5f;
		var circleRadius = height * CIRCLE_HEIGHT_FRACTION;

		cardLeft = Math.round(circleCenterX - CARD_WIDTH * 0.5f);
		cardTop = Math.round(circleCenterY - CARD_HEIGHT * 0.5f);

		blurPipeline = DowntimeRenderPipelines.fullScreenBlur("gui_test_roll_blur", circleCenterX, circleCenterY, circleRadius, CIRCLE_EDGE_SOFTNESS, CIRCLE_OVERLAY_ALPHA);
		cardPipeline = DowntimeRenderPipelines.itemCard("gui_test_roll_card", CARD_WIDTH, CARD_HEIGHT, CARD_RADIUS, CARD_BAR_HEIGHT, CARD_FADE_HEIGHT);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		ScreenBlurRenderer.draw(guiGraphics, blurPipeline, 0, 0, width, height, 0xFFFFFFFF);

		drawExampleCard(guiGraphics);

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	private void drawExampleCard(GuiGraphicsExtractor guiGraphics) {
		var cardCenterX = cardLeft + CARD_WIDTH * 0.5f;
		var cardCenterY = cardTop + CARD_HEIGHT * 0.5f;

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(cardCenterX, cardCenterY);
		guiGraphics.pose().scale(CARD_ZOOM, CARD_ZOOM);
		guiGraphics.pose().translate(-cardCenterX, -cardCenterY);

		RoundedRectangleRenderer.fill(guiGraphics, cardPipeline, cardLeft, cardTop, CARD_WIDTH, CARD_HEIGHT, EXAMPLE_RARITY.color());

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(cardCenterX, cardTop + CARD_HEIGHT * 0.42f);
		guiGraphics.pose().scale(ICON_SCALE, ICON_SCALE);
		guiGraphics.item(EXAMPLE_ITEM, -8, -8);
		guiGraphics.pose().popMatrix();

		guiGraphics.pose().popMatrix();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
