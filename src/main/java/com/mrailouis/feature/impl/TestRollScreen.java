package com.mrailouis.feature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mrailouis.data.KuudraLootEntry;
import com.mrailouis.shader.DowntimeRenderPipelines;
import com.mrailouis.shader.RoundedRectangleRenderer;
import com.mrailouis.shader.ScreenBlurRenderer;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TestRollScreen extends Screen {
	private static final float CIRCLE_HEIGHT_FRACTION = 0.275f;
	private static final float CIRCLE_EDGE_SOFTNESS = 2.0f;
	private static final float CIRCLE_OVERLAY_ALPHA = 0.25f;

	private static final int CARD_WIDTH = 115;
	private static final int CARD_HEIGHT = 77;
	private static final int CARD_GAP = 14;
	private static final float CARD_RADIUS = 0.0f;
	private static final float CARD_BAR_HEIGHT = 6.0f;
	private static final float CARD_FADE_HEIGHT = 36.0f;
	private static final float LENS_ZOOM = 1.1f;
	private static final float OUTER_SCALE = 0.75f;
	private static final float OUTER_ALPHA = 0.55f;
	private static final float ICON_SCALE = 2.4f;

	private static final float REEL_DURATION_SECONDS = 6.5f;
	private static final float ACTIVE_ZONE_MARGIN_FRACTION = 0.2f;
	private static final float REEL_FADE_ZONE_FRACTION = 0.05f;

	private static final int MIN_FILLER_BEFORE_WINNER = 30;
	private static final int SCREEN_WIDTH_COVERAGE_MARGIN = 4;

	private final KuudraLootEntry winner;

	private LootReel reel;
	private RenderPipeline blurPipeline;
	private RenderPipeline cardPipeline;
	private long startTimeNanos = -1;
	private int circleCenterX;
	private int cardCenterY;
	private float circleRadius;
	private float scrollStart;
	private float scrollEnd;

	public TestRollScreen(KuudraLootEntry winner) {
		super(Component.literal("Downtime Roll"));
		this.winner = winner;
	}

	@Override
	protected void init() {
		circleCenterX = Math.round(width * 0.5f);
		var circleCenterY = Math.round(height * 0.5f);
		circleRadius = height * CIRCLE_HEIGHT_FRACTION;
		cardCenterY = circleCenterY;

		var cardStep = CARD_WIDTH + CARD_GAP;
		var cardsToSpanScreen = (int) Math.ceil((double) width / cardStep) + SCREEN_WIDTH_COVERAGE_MARGIN;
		var fillerBeforeWinner = Math.max(MIN_FILLER_BEFORE_WINNER, cardsToSpanScreen);
		var fillerAfterWinner = (int) Math.ceil((double) (width - circleCenterX) / cardStep) + SCREEN_WIDTH_COVERAGE_MARGIN;

		reel = LootReel.build(winner, fillerBeforeWinner, fillerAfterWinner, new Random());
		scrollStart = 0.0f;
		scrollEnd = reel.winnerIndex() * cardStep - circleCenterX;

		blurPipeline = DowntimeRenderPipelines.fullScreenBlur("gui_test_roll_blur", circleCenterX, circleCenterY, circleRadius, CIRCLE_EDGE_SOFTNESS, CIRCLE_OVERLAY_ALPHA);
		cardPipeline = DowntimeRenderPipelines.itemCard("gui_test_roll_card", CARD_WIDTH, CARD_HEIGHT, CARD_RADIUS, CARD_BAR_HEIGHT, CARD_FADE_HEIGHT);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (startTimeNanos < 0) {
			startTimeNanos = System.nanoTime();
		}

		var elapsedSeconds = (System.nanoTime() - startTimeNanos) / 1_000_000_000.0f;
		var progress = Math.min(1.0f, elapsedSeconds / REEL_DURATION_SECONDS);
		var eased = easeOutQuart(progress);

		var cardStep = CARD_WIDTH + CARD_GAP;
		var scrollOffset = scrollStart + (scrollEnd - scrollStart) * eased;
		var activeLeft = width * ACTIVE_ZONE_MARGIN_FRACTION;
		var activeRight = width * (1.0f - ACTIVE_ZONE_MARGIN_FRACTION);
		var lensExtent = Math.round(circleRadius + CARD_WIDTH * (LENS_ZOOM - 1.0f));

		guiGraphics.enableScissor(circleCenterX - lensExtent, cardCenterY - lensExtent, circleCenterX + lensExtent, cardCenterY + lensExtent);
		drawReelPass(guiGraphics, cardStep, scrollOffset, LENS_ZOOM, 1.0f, activeLeft, activeRight);
		guiGraphics.disableScissor();

		ScreenBlurRenderer.draw(guiGraphics, blurPipeline, 0, 0, width, height, 0xFFFFFFFF);

		guiGraphics.enableScissor(Math.round(activeLeft), cardCenterY - lensExtent, circleCenterX - lensExtent, cardCenterY + lensExtent);
		drawReelPass(guiGraphics, cardStep, scrollOffset, OUTER_SCALE, OUTER_ALPHA, activeLeft, activeRight);
		guiGraphics.disableScissor();

		guiGraphics.enableScissor(circleCenterX + lensExtent, cardCenterY - lensExtent, Math.round(activeRight), cardCenterY + lensExtent);
		drawReelPass(guiGraphics, cardStep, scrollOffset, OUTER_SCALE, OUTER_ALPHA, activeLeft, activeRight);
		guiGraphics.disableScissor();

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void drawReelPass(GuiGraphicsExtractor guiGraphics, int cardStep, float scrollOffset, float scale, float alphaMultiplier, float activeLeft, float activeRight) {
		var entries = reel.entries();
		var fadeZoneWidth = (activeRight - activeLeft) * REEL_FADE_ZONE_FRACTION;

		for (var index = 0; index < entries.size(); index++) {
			var cardCenterX = index * cardStep - scrollOffset;
			var cardLeft = cardCenterX - CARD_WIDTH / 2.0f;

			if (cardLeft + CARD_WIDTH < activeLeft || cardLeft > activeRight) {
				continue;
			}

			var fadeAlpha = edgeFadeAlpha(cardCenterX, activeLeft, activeRight, fadeZoneWidth) * alphaMultiplier;
			if (fadeAlpha <= 0.0f) {
				continue;
			}

			var entry = entries.get(index);
			var cardTop = cardCenterY - CARD_HEIGHT / 2.0f;
			var color = withAlpha(entry.skyblockRarity().color(), fadeAlpha);

			guiGraphics.pose().pushMatrix();
			guiGraphics.pose().translate(cardCenterX, cardCenterY);
			guiGraphics.pose().scale(scale, scale);
			guiGraphics.pose().translate(-cardCenterX, -cardCenterY);

			RoundedRectangleRenderer.fill(guiGraphics, cardPipeline, cardLeft, cardTop, CARD_WIDTH, CARD_HEIGHT, color);
			drawIcon(guiGraphics, entry, cardCenterX, cardTop + CARD_HEIGHT * 0.42f);

			guiGraphics.pose().popMatrix();
		}
	}

	private void drawIcon(GuiGraphicsExtractor guiGraphics, KuudraLootEntry entry, float iconCenterX, float iconCenterY) {
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(iconCenterX, iconCenterY);
		guiGraphics.pose().scale(ICON_SCALE, ICON_SCALE);
		guiGraphics.item(KuudraItemIcons.iconFor(entry), -8, -8);
		guiGraphics.pose().popMatrix();
	}

	private static float edgeFadeAlpha(float cardCenterX, float activeLeft, float activeRight, float fadeZoneWidth) {
		var distFromLeft = cardCenterX - activeLeft;
		var distFromRight = activeRight - cardCenterX;
		var minDist = Math.min(distFromLeft, distFromRight);
		return Math.max(0.0f, Math.min(1.0f, minDist / fadeZoneWidth));
	}

	private static int withAlpha(int argb, float alphaMultiplier) {
		var alpha = Math.round(((argb >>> 24) & 0xFF) * alphaMultiplier);
		return (alpha << 24) | (argb & 0x00FFFFFF);
	}

	private static float easeOutQuart(float progress) {
		var t = Math.max(0.0f, Math.min(1.0f, progress)) - 1.0f;
		return 1.0f - t * t * t * t;
	}
}
