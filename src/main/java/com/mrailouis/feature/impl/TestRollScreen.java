package com.mrailouis.feature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mrailouis.Downtime;
import com.mrailouis.api.RollCard;
import com.mrailouis.data.KuudraTier;
import com.mrailouis.extensions.net.minecraft.client.gui.GuiGraphicsExtractor.GuiGraphicsExtractorExtensions;
import com.mrailouis.shader.DowntimeRenderPipelines;
import com.mrailouis.shader.RoundedRectangleRenderer;
import java.util.Optional;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
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
	private static final int POINTER_COLOR = 0xFFFFD500;
	private static final float CIRCLE_ZOOM = 1.6f;

	private static final float LANDING_JITTER_FRACTION = 0.3f;

	private static final float REVEAL_DURATION_SECONDS = 0.4f;
	private static final float REVEAL_MIN_SCALE = 5.0f;
	private static final float REVEAL_MAX_SCALE = 9.0f;
	private static final float REVEAL_RISE_OFFSET = 20.0f;
	private static final float REVEAL_TEXT_MARGIN = 70.0f;

	private static final String TENTACLE_DYE_NAME = "Tentacle Dye";

	private final RollCard winner;
	private final Optional<KuudraTier> tierFilter;
	private final Screen restoreScreen;

	private LootReel reel;
	private RenderPipeline cardPipeline;
	private RenderPipeline pointerPipeline;
	private long startTimeNanos = -1;
	private long landedTimeNanos = -1;
	private int lastTickIndex;
	private int circleCenterX;
	private int cardCenterY;
	private float scrollStart;
	private float scrollEnd;

	public TestRollScreen(RollCard winner, Optional<KuudraTier> tierFilter, Screen restoreScreen) {
		super(Component.literal("Downtime Roll"));
		this.winner = winner;
		this.tierFilter = tierFilter;
		this.restoreScreen = restoreScreen;
	}

	@Override
	protected void init() {
		circleCenterX = Math.round(width * 0.5f);
		cardCenterY = Math.round(height * 0.5f);

		var cardStep = CARD_WIDTH + CARD_GAP;
		var cardsToSpanScreen = (int) Math.ceil((double) width / cardStep) + SCREEN_WIDTH_COVERAGE_MARGIN;
		var fillerBeforeWinner = Math.max(MIN_FILLER_BEFORE_WINNER, cardsToSpanScreen);
		var fillerAfterWinner = (int) Math.ceil((double) (width - circleCenterX) / cardStep) + SCREEN_WIDTH_COVERAGE_MARGIN;

		var random = new Random();
		reel = LootReel.build(winner, fillerBeforeWinner, fillerAfterWinner, tierFilter, random);
		var landingJitter = (random.nextFloat() * 2.0f - 1.0f) * CARD_WIDTH * LANDING_JITTER_FRACTION;
		scrollStart = 0.0f;
		scrollEnd = reel.winnerIndex() * cardStep - circleCenterX + landingJitter;
		lastTickIndex = Math.round((circleCenterX + scrollStart) / cardStep);

		cardPipeline = DowntimeRenderPipelines.itemCard("gui_test_roll_card", CARD_WIDTH, CARD_HEIGHT, CARD_RADIUS, CARD_BAR_HEIGHT, CARD_FADE_HEIGHT);
		pointerPipeline = DowntimeRenderPipelines.roundedRectangle("gui_test_roll_pointer", POINTER_WIDTH, CARD_HEIGHT * CIRCLE_ZOOM, 0.0f);
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

		if (progress < 1.0f) {
			playTickIfCardPassed(cardStep, scrollOffset);
		}

		drawReel(guiGraphics, cardStep, scrollOffset);

		GuiGraphicsExtractorExtensions.applyPostEffect(guiGraphics, Downtime.id("reel_blur"));

		drawPointer(guiGraphics);

		if (progress >= 1.0f) {
			if (landedTimeNanos < 0) {
				landedTimeNanos = System.nanoTime();
				if (winner.displayName().equals(TENTACLE_DYE_NAME)) {
					Minecraft.getInstance().gameRenderer.displayItemActivation(winner.icon());
				}
			}
			drawWinnerReveal(guiGraphics);
		}

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.minecraft.options.keyInventory.matches(event)) {
			onClose();
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(restoreScreen);
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
			var color = withAlpha(entry.rarity().color(), fadeAlpha);

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

	private void playTickIfCardPassed(int cardStep, float scrollOffset) {
		var pointerIndex = Math.round((circleCenterX + scrollOffset) / cardStep);
		if (pointerIndex != lastTickIndex) {
			lastTickIndex = pointerIndex;
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
		}
	}

	private void drawWinnerReveal(GuiGraphicsExtractor guiGraphics) {
		var revealElapsedSeconds = (System.nanoTime() - landedTimeNanos) / 1_000_000_000.0f;
		var revealProgress = Math.max(0.0f, Math.min(1.0f, revealElapsedSeconds / REVEAL_DURATION_SECONDS));
		var eased = easeOutQuart(revealProgress);

		var font = Minecraft.getInstance().font;
		var text = winner.displayName();
		var textWidth = font.width(text);
		var scale = REVEAL_MIN_SCALE + (REVEAL_MAX_SCALE - REVEAL_MIN_SCALE) * eased;
		var riseOffset = REVEAL_RISE_OFFSET * (1.0f - eased);
		var textY = cardCenterY - CARD_HEIGHT / 2.0f - REVEAL_TEXT_MARGIN + riseOffset;
		var color = withAlpha(winner.rarity().color(), eased);

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(circleCenterX, textY);
		guiGraphics.pose().scale(scale, scale);
		guiGraphics.text(font, text, -textWidth / 2, -font.lineHeight, color, true);
		guiGraphics.pose().popMatrix();
	}

	private void drawPointer(GuiGraphicsExtractor guiGraphics) {
		var pointerHeight = CARD_HEIGHT * CIRCLE_ZOOM;
		var pointerLeft = circleCenterX - POINTER_WIDTH / 2.0f;
		var pointerTop = cardCenterY - pointerHeight / 2.0f;
		RoundedRectangleRenderer.fill(guiGraphics, pointerPipeline, pointerLeft, pointerTop, POINTER_WIDTH, pointerHeight, POINTER_COLOR);
	}

	private void drawIcon(GuiGraphicsExtractor guiGraphics, RollCard entry, float iconCenterX, float iconCenterY) {
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(iconCenterX, iconCenterY);
		guiGraphics.pose().scale(ICON_SCALE, ICON_SCALE);
		guiGraphics.item(entry.icon(), -8, -8);
		guiGraphics.pose().popMatrix();
	}

	private static float easeOutQuart(float progress) {
		var t = Math.max(0.0f, Math.min(1.0f, progress)) - 1.0f;
		return 1.0f - t * t * t * t;
	}
}
