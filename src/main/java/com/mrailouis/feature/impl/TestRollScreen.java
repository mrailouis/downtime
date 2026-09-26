package com.mrailouis.feature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mrailouis.Downtime;
import com.mrailouis.data.KuudraLootEntry;
import com.mrailouis.extensions.net.minecraft.client.gui.GuiGraphicsExtractor.GuiGraphicsExtractorExtensions;
import com.mrailouis.shader.DowntimeRenderPipelines;
import com.mrailouis.shader.RoundedRectangleRenderer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public final class TestRollScreen extends Screen {
	private static final int CARD_WIDTH = 115;
	private static final int CARD_HEIGHT = 77;
	private static final int CARD_GAP = 14;
	private static final float CARD_RADIUS = 0.0f;
	private static final float CARD_BAR_HEIGHT = 6.0f;
	private static final float CARD_FADE_HEIGHT = 36.0f;
	private static final float ICON_SCALE = 2.4f;

	private static final float REEL_DURATION_SECONDS = 6.5f;
	private static final float REEL_FADE_ZONE_FRACTION = 0.2f;

	private static final int MIN_FILLER_BEFORE_WINNER = 30;
	private static final int SCREEN_WIDTH_COVERAGE_MARGIN = 4;

	private static final int POINTER_WIDTH = 3;
	private static final int POINTER_OVERSHOOT = 10;
	private static final int POINTER_COLOR = 0xFFFFD500;

	private final KuudraLootEntry winner;

	private LootReel reel;
	private RenderPipeline cardPipeline;
	private RenderPipeline pointerPipeline;
	private long startTimeNanos = -1;
	private int circleCenterX;
	private int cardCenterY;
	private float scrollStart;
	private float scrollEnd;

	public TestRollScreen(KuudraLootEntry winner) {
		super(Component.literal("Downtime Roll"));
		this.winner = winner;
	}

	@Override
	protected void init() {
		circleCenterX = Math.round(width * 0.5f);
		cardCenterY = Math.round(height * 0.5f);

		var cardStep = CARD_WIDTH + CARD_GAP;
		var cardsToSpanScreen = (int) Math.ceil((double) width / cardStep) + SCREEN_WIDTH_COVERAGE_MARGIN;
		var fillerBeforeWinner = Math.max(MIN_FILLER_BEFORE_WINNER, cardsToSpanScreen);
		var fillerAfterWinner = (int) Math.ceil((double) (width - circleCenterX) / cardStep) + SCREEN_WIDTH_COVERAGE_MARGIN;

		reel = LootReel.build(winner, fillerBeforeWinner, fillerAfterWinner, new Random());
		scrollStart = 0.0f;
		scrollEnd = reel.winnerIndex() * cardStep - circleCenterX;

		cardPipeline = DowntimeRenderPipelines.itemCard("gui_test_roll_card", CARD_WIDTH, CARD_HEIGHT, CARD_RADIUS, CARD_BAR_HEIGHT, CARD_FADE_HEIGHT);
		pointerPipeline = DowntimeRenderPipelines.roundedRectangle("gui_test_roll_pointer", POINTER_WIDTH, CARD_HEIGHT + POINTER_OVERSHOOT * 2.0f, 0.0f);
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

		drawReel(guiGraphics, cardStep, scrollOffset);

		GuiGraphicsExtractorExtensions.applyPostEffect(guiGraphics, Downtime.id("reel_blur"));

		drawPointer(guiGraphics);

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void drawReel(GuiGraphicsExtractor guiGraphics, int cardStep, float scrollOffset) {
		var entries = reel.entries();
		var fadeZoneWidth = width * REEL_FADE_ZONE_FRACTION;

		for (var index = 0; index < entries.size(); index++) {
			var cardCenterX = index * cardStep - scrollOffset;
			var cardLeft = cardCenterX - CARD_WIDTH / 2.0f;

			if (cardLeft + CARD_WIDTH < 0 || cardLeft > width) {
				continue;
			}

			var fadeAlpha = edgeFadeAlpha(cardCenterX, fadeZoneWidth);
			if (fadeAlpha <= 0.0f) {
				continue;
			}

			var entry = entries.get(index);
			var cardTop = cardCenterY - CARD_HEIGHT / 2.0f;
			var color = withAlpha(entry.skyblockRarity().color(), fadeAlpha);

			RoundedRectangleRenderer.fill(guiGraphics, cardPipeline, cardLeft, cardTop, CARD_WIDTH, CARD_HEIGHT, color);
			drawIcon(guiGraphics, entry, cardCenterX, cardTop + CARD_HEIGHT * 0.42f);
		}
	}

	private float edgeFadeAlpha(float cardCenterX, float fadeZoneWidth) {
		var distFromLeft = cardCenterX;
		var distFromRight = width - cardCenterX;
		var minDist = Math.min(distFromLeft, distFromRight);
		return Math.max(0.0f, Math.min(1.0f, minDist / fadeZoneWidth));
	}

	private static int withAlpha(int argb, float alphaMultiplier) {
		var alpha = Math.round(((argb >>> 24) & 0xFF) * alphaMultiplier);
		return (alpha << 24) | (argb & 0x00FFFFFF);
	}

	private void drawPointer(GuiGraphicsExtractor guiGraphics) {
		var pointerLeft = circleCenterX - POINTER_WIDTH / 2.0f;
		var pointerTop = cardCenterY - CARD_HEIGHT / 2.0f - POINTER_OVERSHOOT;
		RoundedRectangleRenderer.fill(guiGraphics, pointerPipeline, pointerLeft, pointerTop, POINTER_WIDTH, CARD_HEIGHT + POINTER_OVERSHOOT * 2.0f, POINTER_COLOR);
	}

	private void drawIcon(GuiGraphicsExtractor guiGraphics, KuudraLootEntry entry, float iconCenterX, float iconCenterY) {
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(iconCenterX, iconCenterY);
		guiGraphics.pose().scale(ICON_SCALE, ICON_SCALE);
		guiGraphics.item(KuudraItemIcons.iconFor(entry), -8, -8);
		guiGraphics.pose().popMatrix();
	}

	private static float easeOutQuart(float progress) {
		var t = Math.max(0.0f, Math.min(1.0f, progress)) - 1.0f;
		return 1.0f - t * t * t * t;
	}
}
