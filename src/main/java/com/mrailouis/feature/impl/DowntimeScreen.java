package com.mrailouis.feature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mrailouis.config.ConfigManager;
import com.mrailouis.shader.AwtFontRenderer;
import com.mrailouis.shader.DowntimeRenderPipelines;
import com.mrailouis.shader.RoundedRectangleRenderer;
import com.mrailouis.shader.ScreenBlurRenderer;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class DowntimeScreen extends Screen {
	private static final int PANEL_BLUR_COLOR = 0xFFFFFFFF;
	private static final int PANEL_FILL_COLOR = 0xBD000000;
	private static final int SHADOW_COLOR = 0x60000000;
	private static final int TITLE_COLOR = 0xFFFFFFFF;
	private static final int LABEL_COLOR = 0xFFFFFFFF;
	private static final float CORNER_RADIUS = 6.0f;
	private static final float PANEL_WIDTH_FRACTION = 0.4875f;
	private static final float OPEN_ANIMATION_SPEED = 0.18f;
	private static final int TOP_BAR_HEIGHT = 29;
	private static final int PANEL_GAP = 7;
	private static final int SHADOW_WIDTH = 10;
	private static final int TOP_BAR_PADDING = 12;
	private static final int ROW_HEIGHT = 20;
	private static final int ROW_GAP = 10;
	private static final int ROW_COUNT = 7;
	private static final int VALUE_GAP = 8;

	private static final int SLIDER_TRACK_WIDTH = 120;
	private static final int SLIDER_TRACK_HEIGHT = 4;
	private static final int SLIDER_KNOB_SIZE = 10;
	private static final int TOGGLE_WIDTH = 32;
	private static final int TOGGLE_HEIGHT = 16;
	private static final int TOGGLE_KNOB_SIZE = 12;
	private static final int RANGE_HANDLE_WIDTH = 8;
	private static final int RANGE_HANDLE_HEIGHT = 10;
	private static final int TRACK_COLOR = 0xFF3D3D3D;
	private static final int ACCENT_COLOR = 0xFF4C8BF5;
	private static final int KNOB_COLOR = 0xFFFFFFFF;

	private static final AwtFontRenderer TITLE_FONT = AwtFontRenderer.create("/assets/downtime/fonts/inter_variable.ttf", Font.BOLD, 13.0f);
	private static final AwtFontRenderer LABEL_FONT = AwtFontRenderer.create("/assets/downtime/fonts/inter_variable.ttf", Font.PLAIN, 11.0f);

	private final List<SliderControl> sliders = new ArrayList<>();
	private final List<ToggleControl> toggles = new ArrayList<>();
	private final List<RangeSliderControl> rangeSliders = new ArrayList<>();

	private RenderPipeline topBarShadowPipeline;
	private RenderPipeline topBarBlurPipeline;
	private RenderPipeline topBarPipeline;
	private RenderPipeline mainPanelShadowPipeline;
	private RenderPipeline mainPanelBlurPipeline;
	private RenderPipeline mainPanelPipeline;
	private RenderPipeline sliderTrackPipeline;
	private RenderPipeline sliderFillPipeline;
	private RenderPipeline sliderKnobPipeline;
	private RenderPipeline toggleTrackPipeline;
	private RenderPipeline toggleKnobPipeline;
	private RenderPipeline rangeMinHandlePipeline;
	private RenderPipeline rangeMaxHandlePipeline;
	private int panelLeft;
	private int panelTop;
	private int panelWidth;
	private int mainPanelTop;
	private int mainPanelHeight;
	private float openAnimationProgress;
	private SliderControl draggingSlider;
	private RangeSliderControl draggingRangeSlider;
	private boolean draggingRangeMin;
	private final Screen parent;

	public DowntimeScreen() {
		this(null);
	}

	public DowntimeScreen(Screen parent) {
		super(Component.literal("Downtime"));
		this.parent = parent;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}

	@Override
	protected void init() {
		panelWidth = Math.round(width * PANEL_WIDTH_FRACTION);
		mainPanelHeight = TOP_BAR_PADDING * 2 + ROW_COUNT * ROW_HEIGHT + (ROW_COUNT - 1) * ROW_GAP;
		var panelHeight = TOP_BAR_HEIGHT + PANEL_GAP + mainPanelHeight;

		panelLeft = (width - panelWidth) / 2;
		panelTop = (height - panelHeight) / 2;
		mainPanelTop = panelTop + TOP_BAR_HEIGHT + PANEL_GAP;

		topBarShadowPipeline = DowntimeRenderPipelines.roundedRectangleShadow(
				"gui_top_bar_shadow",
				panelWidth + SHADOW_WIDTH * 2,
				TOP_BAR_HEIGHT + SHADOW_WIDTH * 2,
				CORNER_RADIUS,
				SHADOW_WIDTH);
		topBarBlurPipeline = DowntimeRenderPipelines.panelBlur("gui_top_bar_blur", panelWidth, TOP_BAR_HEIGHT, CORNER_RADIUS);
		topBarPipeline = DowntimeRenderPipelines.roundedRectangle("gui_top_bar", panelWidth, TOP_BAR_HEIGHT, CORNER_RADIUS);

		mainPanelShadowPipeline = DowntimeRenderPipelines.roundedRectangleShadow(
				"gui_main_panel_shadow",
				panelWidth + SHADOW_WIDTH * 2,
				mainPanelHeight + SHADOW_WIDTH * 2,
				CORNER_RADIUS,
				SHADOW_WIDTH);
		mainPanelBlurPipeline = DowntimeRenderPipelines.panelBlur("gui_main_panel_blur", panelWidth, mainPanelHeight, CORNER_RADIUS);
		mainPanelPipeline = DowntimeRenderPipelines.roundedRectangle("gui_main_panel", panelWidth, mainPanelHeight, CORNER_RADIUS);

		sliderTrackPipeline = DowntimeRenderPipelines.roundedRectangle("gui_slider_track", SLIDER_TRACK_WIDTH, SLIDER_TRACK_HEIGHT, SLIDER_TRACK_HEIGHT / 2.0f);
		sliderFillPipeline = DowntimeRenderPipelines.roundedRectangle("gui_slider_fill", SLIDER_TRACK_WIDTH, SLIDER_TRACK_HEIGHT, SLIDER_TRACK_HEIGHT / 2.0f);
		sliderKnobPipeline = DowntimeRenderPipelines.roundedRectangle("gui_slider_knob", SLIDER_KNOB_SIZE, SLIDER_KNOB_SIZE, SLIDER_KNOB_SIZE / 2.0f);
		toggleTrackPipeline = DowntimeRenderPipelines.roundedRectangle("gui_toggle_track", TOGGLE_WIDTH, TOGGLE_HEIGHT, TOGGLE_HEIGHT / 2.0f);
		toggleKnobPipeline = DowntimeRenderPipelines.roundedRectangle("gui_toggle_knob", TOGGLE_KNOB_SIZE, TOGGLE_KNOB_SIZE, TOGGLE_KNOB_SIZE / 2.0f);
		rangeMinHandlePipeline = DowntimeRenderPipelines.triangle("gui_range_min_handle", RANGE_HANDLE_WIDTH, RANGE_HANDLE_HEIGHT, 1.0f);
		rangeMaxHandlePipeline = DowntimeRenderPipelines.triangle("gui_range_max_handle", RANGE_HANDLE_WIDTH, RANGE_HANDLE_HEIGHT, -1.0f);

		buildSettingsRows();
	}

	private void buildSettingsRows() {
		sliders.clear();
		toggles.clear();
		rangeSliders.clear();

		var config = ConfigManager.getConfig();
		var y = mainPanelTop + TOP_BAR_PADDING;

		toggles.add(new ToggleControl("Case-Opening Animation", y, config::isCaseOpeningAnimationEnabled, config::setCaseOpeningAnimationEnabled));
		y += ROW_HEIGHT + ROW_GAP;
		sliders.add(new SliderControl("Animation Duration", y, config::getCaseOpeningAnimationDurationSeconds, config::setCaseOpeningAnimationDurationSeconds, 3.0, 12.0, value -> "%.1fs".formatted(value)));
		y += ROW_HEIGHT + ROW_GAP;
		sliders.add(new SliderControl("Blur Strength", y, config::getBlurStrength, config::setBlurStrength, 0.0, 1.0, value -> "%.0f%%".formatted(value * 100.0)));
		y += ROW_HEIGHT + ROW_GAP;
		toggles.add(new ToggleControl("Ticker Sound", y, config::isTickerSoundEnabled, config::setTickerSoundEnabled));
		y += ROW_HEIGHT + ROW_GAP;
		sliders.add(new SliderControl("Ticker Sound Volume", y, config::getTickerSoundVolume, config::setTickerSoundVolume, 0.0, 1.0, value -> "%.0f%%".formatted(value * 100.0)));
		y += ROW_HEIGHT + ROW_GAP;
		rangeSliders.add(new RangeSliderControl("Bait Chance", y, config::getBaitChanceMin, config::setBaitChanceMin, config::getBaitChanceMax, config::setBaitChanceMax, 0.0, 1.0, value -> "%.0f%%".formatted(value * 100.0)));
		y += ROW_HEIGHT + ROW_GAP;
		toggles.add(new ToggleControl("Downtime Tracker", y, config::isDowntimeTrackerEnabled, config::setDowntimeTrackerEnabled));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		var openScale = openScale();

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(width * 0.5f, height * 0.5f);
		guiGraphics.pose().scale(openScale, openScale);
		guiGraphics.pose().translate(-width * 0.5f, -height * 0.5f);

		drawPanel(guiGraphics, topBarShadowPipeline, topBarBlurPipeline, topBarPipeline, panelLeft, panelTop, panelWidth, TOP_BAR_HEIGHT);
		drawPanel(guiGraphics, mainPanelShadowPipeline, mainPanelBlurPipeline, mainPanelPipeline, panelLeft, mainPanelTop, panelWidth, mainPanelHeight);

		TITLE_FONT.draw(guiGraphics, "Downtime", panelLeft + TOP_BAR_PADDING, panelTop + centeredY(TOP_BAR_HEIGHT, TITLE_FONT.height("Downtime")), TITLE_COLOR);

		drawSettings(guiGraphics);

		guiGraphics.pose().popMatrix();

		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		for (var toggle : toggles) {
			if (toggleContains(toggle, event.x(), event.y())) {
				toggle.setter().accept(!toggle.getter().getAsBoolean());
				return true;
			}
		}

		for (var range : rangeSliders) {
			if (rangeHandleContains(range, range.minProgress(), event.x(), event.y())) {
				draggingRangeSlider = range;
				draggingRangeMin = true;
				return true;
			}
			if (rangeHandleContains(range, range.maxProgress(), event.x(), event.y())) {
				draggingRangeSlider = range;
				draggingRangeMin = false;
				return true;
			}
		}

		for (var slider : sliders) {
			if (sliderContains(slider, event.x(), event.y())) {
				draggingSlider = slider;
				updateSliderFromMouse(slider, event.x());
				return true;
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (draggingSlider != null) {
			updateSliderFromMouse(draggingSlider, event.x());
			return true;
		}

		if (draggingRangeSlider != null) {
			updateRangeSliderFromMouse(draggingRangeSlider, draggingRangeMin, event.x());
			return true;
		}

		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (draggingSlider != null) {
			draggingSlider = null;
			return true;
		}

		if (draggingRangeSlider != null) {
			draggingRangeSlider = null;
			return true;
		}

		return super.mouseReleased(event);
	}

	@Override
	public void removed() {
		ConfigManager.save();
		super.removed();
	}

	private void updateSliderFromMouse(SliderControl slider, double mouseX) {
		var progress = Mth.clamp((mouseX - sliderTrackX()) / (double) SLIDER_TRACK_WIDTH, 0.0, 1.0);
		slider.setter().accept(slider.min() + (slider.max() - slider.min()) * progress);
	}

	private void updateRangeSliderFromMouse(RangeSliderControl range, boolean draggingMin, double mouseX) {
		var progress = Mth.clamp((mouseX - sliderTrackX()) / (double) SLIDER_TRACK_WIDTH, 0.0, 1.0);
		var value = range.min() + (range.max() - range.min()) * progress;

		if (draggingMin) {
			range.minSetter().accept(Math.min(value, range.maxGetter().getAsDouble()));
		} else {
			range.maxSetter().accept(Math.max(value, range.minGetter().getAsDouble()));
		}
	}

	private boolean sliderContains(SliderControl slider, double mouseX, double mouseY) {
		var trackX = sliderTrackX();
		var trackY = slider.rowY() + (ROW_HEIGHT - SLIDER_TRACK_HEIGHT) / 2.0;
		return mouseX >= trackX - 4 && mouseX < trackX + SLIDER_TRACK_WIDTH + 4 && mouseY >= trackY - 6 && mouseY < trackY + SLIDER_TRACK_HEIGHT + 6;
	}

	private boolean rangeHandleContains(RangeSliderControl range, double handleProgress, double mouseX, double mouseY) {
		var handleX = sliderTrackX() + Math.round(SLIDER_TRACK_WIDTH * handleProgress);
		var trackY = range.rowY() + (ROW_HEIGHT - SLIDER_TRACK_HEIGHT) / 2.0;
		return mouseX >= handleX - RANGE_HANDLE_WIDTH - 3 && mouseX < handleX + RANGE_HANDLE_WIDTH + 3 && mouseY >= trackY - 6 && mouseY < trackY + SLIDER_TRACK_HEIGHT + 6;
	}

	private boolean toggleContains(ToggleControl toggle, double mouseX, double mouseY) {
		var trackX = toggleTrackX();
		var trackY = toggle.rowY() + (ROW_HEIGHT - TOGGLE_HEIGHT) / 2.0;
		return mouseX >= trackX && mouseX < trackX + TOGGLE_WIDTH && mouseY >= trackY && mouseY < trackY + TOGGLE_HEIGHT;
	}

	private int sliderTrackX() {
		return panelLeft + panelWidth - TOP_BAR_PADDING - SLIDER_TRACK_WIDTH;
	}

	private int toggleTrackX() {
		return panelLeft + panelWidth - TOP_BAR_PADDING - TOGGLE_WIDTH;
	}

	private void drawSettings(GuiGraphicsExtractor guiGraphics) {
		for (var toggle : toggles) {
			var labelY = toggle.rowY() + centeredY(ROW_HEIGHT, LABEL_FONT.height(toggle.label()));
			LABEL_FONT.draw(guiGraphics, toggle.label(), panelLeft + TOP_BAR_PADDING, labelY, LABEL_COLOR);
			drawToggle(guiGraphics, toggleTrackX(), toggle.rowY() + (ROW_HEIGHT - TOGGLE_HEIGHT) / 2, toggle.getter().getAsBoolean());
		}

		for (var slider : sliders) {
			var labelY = slider.rowY() + centeredY(ROW_HEIGHT, LABEL_FONT.height(slider.label()));
			LABEL_FONT.draw(guiGraphics, slider.label(), panelLeft + TOP_BAR_PADDING, labelY, LABEL_COLOR);

			var valueText = slider.format().apply(slider.getter().getAsDouble());
			drawValue(guiGraphics, valueText, labelY);

			drawSlider(guiGraphics, sliderTrackX(), slider.rowY() + (ROW_HEIGHT - SLIDER_TRACK_HEIGHT) / 2, (float) slider.progress());
		}

		for (var range : rangeSliders) {
			var labelY = range.rowY() + centeredY(ROW_HEIGHT, LABEL_FONT.height(range.label()));
			LABEL_FONT.draw(guiGraphics, range.label(), panelLeft + TOP_BAR_PADDING, labelY, LABEL_COLOR);

			var valueText = range.format().apply(range.minGetter().getAsDouble()) + " - " + range.format().apply(range.maxGetter().getAsDouble());
			drawValue(guiGraphics, valueText, labelY);

			drawRangeSlider(guiGraphics, sliderTrackX(), range.rowY() + (ROW_HEIGHT - SLIDER_TRACK_HEIGHT) / 2, (float) range.minProgress(), (float) range.maxProgress());
		}
	}

	private void drawValue(GuiGraphicsExtractor guiGraphics, String valueText, int labelY) {
		var valueX = sliderTrackX() - VALUE_GAP - LABEL_FONT.width(valueText);
		LABEL_FONT.draw(guiGraphics, valueText, valueX, labelY, LABEL_COLOR);
	}

	private void drawSlider(GuiGraphicsExtractor guiGraphics, int x, int y, float progress) {
		var fillWidth = Math.max(SLIDER_TRACK_HEIGHT, Math.round(SLIDER_TRACK_WIDTH * progress));
		var knobX = x + Math.round((SLIDER_TRACK_WIDTH - SLIDER_KNOB_SIZE) * progress);
		var knobY = y - (SLIDER_KNOB_SIZE - SLIDER_TRACK_HEIGHT) / 2.0f;

		RoundedRectangleRenderer.fill(guiGraphics, sliderTrackPipeline, x, y, SLIDER_TRACK_WIDTH, SLIDER_TRACK_HEIGHT, TRACK_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, sliderFillPipeline, x, y, fillWidth, SLIDER_TRACK_HEIGHT, ACCENT_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, sliderKnobPipeline, knobX, knobY, SLIDER_KNOB_SIZE, SLIDER_KNOB_SIZE, KNOB_COLOR);
	}

	private void drawRangeSlider(GuiGraphicsExtractor guiGraphics, int x, int y, float minProgress, float maxProgress) {
		var minX = x + Math.round(SLIDER_TRACK_WIDTH * minProgress);
		var maxX = x + Math.round(SLIDER_TRACK_WIDTH * maxProgress);
		var fillX = Math.min(minX, maxX);
		var fillWidth = Math.max(SLIDER_TRACK_HEIGHT, Math.abs(maxX - minX));
		var handleY = y - (RANGE_HANDLE_HEIGHT - SLIDER_TRACK_HEIGHT) / 2.0f;

		RoundedRectangleRenderer.fill(guiGraphics, sliderTrackPipeline, x, y, SLIDER_TRACK_WIDTH, SLIDER_TRACK_HEIGHT, TRACK_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, sliderFillPipeline, fillX, y, fillWidth, SLIDER_TRACK_HEIGHT, ACCENT_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, rangeMinHandlePipeline, minX - RANGE_HANDLE_WIDTH, handleY, RANGE_HANDLE_WIDTH, RANGE_HANDLE_HEIGHT, KNOB_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, rangeMaxHandlePipeline, maxX, handleY, RANGE_HANDLE_WIDTH, RANGE_HANDLE_HEIGHT, KNOB_COLOR);
	}

	private void drawToggle(GuiGraphicsExtractor guiGraphics, int x, int y, boolean enabled) {
		var knobX = x + (enabled ? TOGGLE_WIDTH - TOGGLE_KNOB_SIZE - 2 : 2);
		var knobY = y + (TOGGLE_HEIGHT - TOGGLE_KNOB_SIZE) / 2.0f;

		RoundedRectangleRenderer.fill(guiGraphics, toggleTrackPipeline, x, y, TOGGLE_WIDTH, TOGGLE_HEIGHT, enabled ? ACCENT_COLOR : TRACK_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, toggleKnobPipeline, knobX, knobY, TOGGLE_KNOB_SIZE, TOGGLE_KNOB_SIZE, KNOB_COLOR);
	}

	private static void drawPanel(GuiGraphicsExtractor guiGraphics, RenderPipeline shadowPipeline, RenderPipeline blurPipeline, RenderPipeline pipeline, int x, int y, int width, int height) {
		RoundedRectangleRenderer.fill(guiGraphics, shadowPipeline, x - SHADOW_WIDTH, y - SHADOW_WIDTH, width + SHADOW_WIDTH * 2, height + SHADOW_WIDTH * 2, SHADOW_COLOR);
		ScreenBlurRenderer.draw(guiGraphics, blurPipeline, x, y, width, height, PANEL_BLUR_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, pipeline, x, y, width, height, PANEL_FILL_COLOR);
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

	private record SliderControl(String label, int rowY, DoubleSupplier getter, DoubleConsumer setter, double min, double max, DoubleFunction<String> format) {
		double progress() {
			return Mth.clamp((getter.getAsDouble() - min) / (max - min), 0.0, 1.0);
		}
	}

	private record RangeSliderControl(String label, int rowY, DoubleSupplier minGetter, DoubleConsumer minSetter, DoubleSupplier maxGetter, DoubleConsumer maxSetter, double min, double max, DoubleFunction<String> format) {
		double minProgress() {
			return Mth.clamp((minGetter.getAsDouble() - min) / (max - min), 0.0, 1.0);
		}

		double maxProgress() {
			return Mth.clamp((maxGetter.getAsDouble() - min) / (max - min), 0.0, 1.0);
		}
	}

	private record ToggleControl(String label, int rowY, BooleanSupplier getter, Consumer<Boolean> setter) {
	}
}
