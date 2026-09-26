package com.mrailouis.feature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mrailouis.config.ConfigManager;
import com.mrailouis.shader.AwtFontRenderer;
import com.mrailouis.shader.DowntimeRenderPipelines;
import com.mrailouis.shader.RoundedRectangleRenderer;
import com.mrailouis.shader.ScreenBlurRenderer;
import com.mrailouis.utils.GuiScaleUtils;
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
	private static final float PANEL_WIDTH_FRACTION = 0.4875f;
	private static final float OPEN_ANIMATION_SPEED = 0.18f;
	private static final float REFERENCE_GUI_SCALE = 3.0f;
	private static final float REFERENCE_SCALE_FACTOR = 0.65f;
	private static final int ROW_COUNT = 9;

	private static final float CORNER_RADIUS_BASE = 6.0f;
	private static final int TOP_BAR_HEIGHT_BASE = 29;
	private static final int PANEL_GAP_BASE = 7;
	private static final int SHADOW_WIDTH_BASE = 10;
	private static final int TOP_BAR_PADDING_BASE = 12;
	private static final int ROW_HEIGHT_BASE = 20;
	private static final int ROW_GAP_BASE = 10;
	private static final int VALUE_GAP_BASE = 8;

	private static final int SLIDER_TRACK_WIDTH_BASE = 120;
	private static final int SLIDER_TRACK_HEIGHT_BASE = 4;
	private static final int SLIDER_KNOB_SIZE_BASE = 10;
	private static final int TOGGLE_WIDTH_BASE = 32;
	private static final int TOGGLE_HEIGHT_BASE = 16;
	private static final int TOGGLE_KNOB_SIZE_BASE = 12;
	private static final int RANGE_HANDLE_WIDTH_BASE = 8;
	private static final int RANGE_HANDLE_HEIGHT_BASE = 10;
	private static final int TRACK_COLOR = 0xFF3D3D3D;
	private static final int ACCENT_COLOR = 0xFF4C8BF5;
	private static final int KNOB_COLOR = 0xFFFFFFFF;

	private static final float TITLE_FONT_SIZE_BASE = 13.0f;
	private static final float LABEL_FONT_SIZE_BASE = 11.0f;

	private final List<SliderControl> sliders = new ArrayList<>();
	private final List<ToggleControl> toggles = new ArrayList<>();
	private final List<RangeSliderControl> rangeSliders = new ArrayList<>();

	private AwtFontRenderer titleFont;
	private AwtFontRenderer labelFont;

	private float cornerRadius;
	private int topBarHeight;
	private int panelGap;
	private int shadowWidth;
	private int topBarPadding;
	private int rowHeight;
	private int rowGap;
	private int valueGap;
	private int sliderTrackWidth;
	private int sliderTrackHeight;
	private int sliderKnobSize;
	private int toggleWidth;
	private int toggleHeight;
	private int toggleKnobSize;
	private int rangeHandleWidth;
	private int rangeHandleHeight;

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
		var uiScale = sizeCompensation();

		cornerRadius = CORNER_RADIUS_BASE * uiScale;
		topBarHeight = Math.round(TOP_BAR_HEIGHT_BASE * uiScale);
		panelGap = Math.round(PANEL_GAP_BASE * uiScale);
		shadowWidth = Math.round(SHADOW_WIDTH_BASE * uiScale);
		topBarPadding = Math.round(TOP_BAR_PADDING_BASE * uiScale);
		rowHeight = Math.round(ROW_HEIGHT_BASE * uiScale);
		rowGap = Math.round(ROW_GAP_BASE * uiScale);
		valueGap = Math.round(VALUE_GAP_BASE * uiScale);
		sliderTrackWidth = Math.round(SLIDER_TRACK_WIDTH_BASE * uiScale);
		sliderTrackHeight = Math.round(SLIDER_TRACK_HEIGHT_BASE * uiScale);
		sliderKnobSize = Math.round(SLIDER_KNOB_SIZE_BASE * uiScale);
		toggleWidth = Math.round(TOGGLE_WIDTH_BASE * uiScale);
		toggleHeight = Math.round(TOGGLE_HEIGHT_BASE * uiScale);
		toggleKnobSize = Math.round(TOGGLE_KNOB_SIZE_BASE * uiScale);
		rangeHandleWidth = Math.round(RANGE_HANDLE_WIDTH_BASE * uiScale);
		rangeHandleHeight = Math.round(RANGE_HANDLE_HEIGHT_BASE * uiScale);

		titleFont = AwtFontRenderer.create("/assets/downtime/fonts/inter_variable.ttf", Font.BOLD, TITLE_FONT_SIZE_BASE * uiScale);
		labelFont = AwtFontRenderer.create("/assets/downtime/fonts/inter_variable.ttf", Font.PLAIN, LABEL_FONT_SIZE_BASE * uiScale);

		panelWidth = Math.round(width * PANEL_WIDTH_FRACTION);
		mainPanelHeight = topBarPadding * 2 + ROW_COUNT * rowHeight + (ROW_COUNT - 1) * rowGap;
		var panelHeight = topBarHeight + panelGap + mainPanelHeight;

		panelLeft = (width - panelWidth) / 2;
		panelTop = (height - panelHeight) / 2;
		mainPanelTop = panelTop + topBarHeight + panelGap;

		topBarShadowPipeline = DowntimeRenderPipelines.roundedRectangleShadow(
				"gui_top_bar_shadow",
				panelWidth + shadowWidth * 2,
				topBarHeight + shadowWidth * 2,
				cornerRadius,
				shadowWidth);
		topBarBlurPipeline = DowntimeRenderPipelines.panelBlur("gui_top_bar_blur", panelWidth, topBarHeight, cornerRadius);
		topBarPipeline = DowntimeRenderPipelines.roundedRectangle("gui_top_bar", panelWidth, topBarHeight, cornerRadius);

		mainPanelShadowPipeline = DowntimeRenderPipelines.roundedRectangleShadow(
				"gui_main_panel_shadow",
				panelWidth + shadowWidth * 2,
				mainPanelHeight + shadowWidth * 2,
				cornerRadius,
				shadowWidth);
		mainPanelBlurPipeline = DowntimeRenderPipelines.panelBlur("gui_main_panel_blur", panelWidth, mainPanelHeight, cornerRadius);
		mainPanelPipeline = DowntimeRenderPipelines.roundedRectangle("gui_main_panel", panelWidth, mainPanelHeight, cornerRadius);

		sliderTrackPipeline = DowntimeRenderPipelines.roundedRectangle("gui_slider_track", sliderTrackWidth, sliderTrackHeight, sliderTrackHeight / 2.0f);
		sliderFillPipeline = DowntimeRenderPipelines.roundedRectangle("gui_slider_fill", sliderTrackWidth, sliderTrackHeight, sliderTrackHeight / 2.0f);
		sliderKnobPipeline = DowntimeRenderPipelines.roundedRectangle("gui_slider_knob", sliderKnobSize, sliderKnobSize, sliderKnobSize / 2.0f);
		toggleTrackPipeline = DowntimeRenderPipelines.roundedRectangle("gui_toggle_track", toggleWidth, toggleHeight, toggleHeight / 2.0f);
		toggleKnobPipeline = DowntimeRenderPipelines.roundedRectangle("gui_toggle_knob", toggleKnobSize, toggleKnobSize, toggleKnobSize / 2.0f);
		rangeMinHandlePipeline = DowntimeRenderPipelines.triangle("gui_range_min_handle", rangeHandleWidth, rangeHandleHeight, 1.0f);
		rangeMaxHandlePipeline = DowntimeRenderPipelines.triangle("gui_range_max_handle", rangeHandleWidth, rangeHandleHeight, -1.0f);

		buildSettingsRows();
	}

	private void buildSettingsRows() {
		sliders.clear();
		toggles.clear();
		rangeSliders.clear();

		var config = ConfigManager.getConfig();
		var y = mainPanelTop + topBarPadding;

		toggles.add(new ToggleControl("Case-Opening Animation", y, config::isCaseOpeningAnimationEnabled, config::setCaseOpeningAnimationEnabled));
		y += rowHeight + rowGap;
		sliders.add(new SliderControl("Animation Duration", y, config::getCaseOpeningAnimationDurationSeconds, config::setCaseOpeningAnimationDurationSeconds, 3.0, 12.0, value -> "%.1fs".formatted(value)));
		y += rowHeight + rowGap;
		sliders.add(new SliderControl("Blur Strength", y, config::getBlurStrength, config::setBlurStrength, 0.0, 1.0, value -> "%.0f%%".formatted(value * 100.0)));
		y += rowHeight + rowGap;
		toggles.add(new ToggleControl("Ticker Sound", y, config::isTickerSoundEnabled, config::setTickerSoundEnabled));
		y += rowHeight + rowGap;
		sliders.add(new SliderControl("Ticker Sound Volume", y, config::getTickerSoundVolume, config::setTickerSoundVolume, 0.0, 1.0, value -> "%.0f%%".formatted(value * 100.0)));
		y += rowHeight + rowGap;
		toggles.add(new ToggleControl("Quick Open", y, config::isQuickOpenEnabled, config::setQuickOpenEnabled));
		y += rowHeight + rowGap;
		sliders.add(new SliderControl("Quick Open Volume", y, config::getQuickOpenVolume, config::setQuickOpenVolume, 0.0, 1.0, value -> "%.0f%%".formatted(value * 100.0)));
		y += rowHeight + rowGap;
		rangeSliders.add(new RangeSliderControl("Bait Chance", y, config::getBaitChanceMin, config::setBaitChanceMin, config::getBaitChanceMax, config::setBaitChanceMax, 0.0, 1.0, value -> "%.0f%%".formatted(value * 100.0)));
		y += rowHeight + rowGap;
		toggles.add(new ToggleControl("Downtime Tracker", y, config::isDowntimeTrackerEnabled, config::setDowntimeTrackerEnabled));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		var scale = openScale();

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(width * 0.5f, height * 0.5f);
		guiGraphics.pose().scale(scale, scale);
		guiGraphics.pose().translate(-width * 0.5f, -height * 0.5f);

		drawPanel(guiGraphics, topBarShadowPipeline, topBarBlurPipeline, topBarPipeline, panelLeft, panelTop, panelWidth, topBarHeight);
		drawPanel(guiGraphics, mainPanelShadowPipeline, mainPanelBlurPipeline, mainPanelPipeline, panelLeft, mainPanelTop, panelWidth, mainPanelHeight);

		titleFont.draw(guiGraphics, "Downtime", panelLeft + topBarPadding, panelTop + centeredY(topBarHeight, titleFont.height("Downtime")), TITLE_COLOR);

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

	private static float sizeCompensation() {
		return GuiScaleUtils.compensate(REFERENCE_GUI_SCALE * REFERENCE_SCALE_FACTOR);
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
		var progress = Mth.clamp((mouseX - sliderTrackX()) / (double) sliderTrackWidth, 0.0, 1.0);
		slider.setter().accept(slider.min() + (slider.max() - slider.min()) * progress);
	}

	private void updateRangeSliderFromMouse(RangeSliderControl range, boolean draggingMin, double mouseX) {
		var progress = Mth.clamp((mouseX - sliderTrackX()) / (double) sliderTrackWidth, 0.0, 1.0);
		var value = range.min() + (range.max() - range.min()) * progress;

		if (draggingMin) {
			range.minSetter().accept(Math.min(value, range.maxGetter().getAsDouble()));
		} else {
			range.maxSetter().accept(Math.max(value, range.minGetter().getAsDouble()));
		}
	}

	private boolean sliderContains(SliderControl slider, double mouseX, double mouseY) {
		var trackX = sliderTrackX();
		var trackY = slider.rowY() + (rowHeight - sliderTrackHeight) / 2.0;
		return mouseX >= trackX - 4 && mouseX < trackX + sliderTrackWidth + 4 && mouseY >= trackY - 6 && mouseY < trackY + sliderTrackHeight + 6;
	}

	private boolean rangeHandleContains(RangeSliderControl range, double handleProgress, double mouseX, double mouseY) {
		var handleX = sliderTrackX() + Math.round(sliderTrackWidth * handleProgress);
		var trackY = range.rowY() + (rowHeight - sliderTrackHeight) / 2.0;
		return mouseX >= handleX - rangeHandleWidth - 3 && mouseX < handleX + rangeHandleWidth + 3 && mouseY >= trackY - 6 && mouseY < trackY + sliderTrackHeight + 6;
	}

	private boolean toggleContains(ToggleControl toggle, double mouseX, double mouseY) {
		var trackX = toggleTrackX();
		var trackY = toggle.rowY() + (rowHeight - toggleHeight) / 2.0;
		return mouseX >= trackX && mouseX < trackX + toggleWidth && mouseY >= trackY && mouseY < trackY + toggleHeight;
	}

	private int sliderTrackX() {
		return panelLeft + panelWidth - topBarPadding - sliderTrackWidth;
	}

	private int toggleTrackX() {
		return panelLeft + panelWidth - topBarPadding - toggleWidth;
	}

	private void drawSettings(GuiGraphicsExtractor guiGraphics) {
		for (var toggle : toggles) {
			var labelY = toggle.rowY() + centeredY(rowHeight, labelFont.height(toggle.label()));
			labelFont.draw(guiGraphics, toggle.label(), panelLeft + topBarPadding, labelY, LABEL_COLOR);
			drawToggle(guiGraphics, toggleTrackX(), toggle.rowY() + (rowHeight - toggleHeight) / 2, toggle.getter().getAsBoolean());
		}

		for (var slider : sliders) {
			var labelY = slider.rowY() + centeredY(rowHeight, labelFont.height(slider.label()));
			labelFont.draw(guiGraphics, slider.label(), panelLeft + topBarPadding, labelY, LABEL_COLOR);

			var valueText = slider.format().apply(slider.getter().getAsDouble());
			drawValue(guiGraphics, valueText, labelY);

			drawSlider(guiGraphics, sliderTrackX(), slider.rowY() + (rowHeight - sliderTrackHeight) / 2, (float) slider.progress());
		}

		for (var range : rangeSliders) {
			var labelY = range.rowY() + centeredY(rowHeight, labelFont.height(range.label()));
			labelFont.draw(guiGraphics, range.label(), panelLeft + topBarPadding, labelY, LABEL_COLOR);

			var valueText = range.format().apply(range.minGetter().getAsDouble()) + " - " + range.format().apply(range.maxGetter().getAsDouble());
			drawValue(guiGraphics, valueText, labelY);

			drawRangeSlider(guiGraphics, sliderTrackX(), range.rowY() + (rowHeight - sliderTrackHeight) / 2, (float) range.minProgress(), (float) range.maxProgress());
		}
	}

	private void drawValue(GuiGraphicsExtractor guiGraphics, String valueText, int labelY) {
		var valueX = sliderTrackX() - valueGap - labelFont.width(valueText);
		labelFont.draw(guiGraphics, valueText, valueX, labelY, LABEL_COLOR);
	}

	private void drawSlider(GuiGraphicsExtractor guiGraphics, int x, int y, float progress) {
		var fillWidth = Math.max(sliderTrackHeight, Math.round(sliderTrackWidth * progress));
		var knobX = x + Math.round((sliderTrackWidth - sliderKnobSize) * progress);
		var knobY = y - (sliderKnobSize - sliderTrackHeight) / 2.0f;

		RoundedRectangleRenderer.fill(guiGraphics, sliderTrackPipeline, x, y, sliderTrackWidth, sliderTrackHeight, TRACK_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, sliderFillPipeline, x, y, fillWidth, sliderTrackHeight, ACCENT_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, sliderKnobPipeline, knobX, knobY, sliderKnobSize, sliderKnobSize, KNOB_COLOR);
	}

	private void drawRangeSlider(GuiGraphicsExtractor guiGraphics, int x, int y, float minProgress, float maxProgress) {
		var minX = x + Math.round(sliderTrackWidth * minProgress);
		var maxX = x + Math.round(sliderTrackWidth * maxProgress);
		var fillX = Math.min(minX, maxX);
		var fillWidth = Math.max(sliderTrackHeight, Math.abs(maxX - minX));
		var handleY = y - (rangeHandleHeight - sliderTrackHeight) / 2.0f;

		RoundedRectangleRenderer.fill(guiGraphics, sliderTrackPipeline, x, y, sliderTrackWidth, sliderTrackHeight, TRACK_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, sliderFillPipeline, fillX, y, fillWidth, sliderTrackHeight, ACCENT_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, rangeMinHandlePipeline, minX - rangeHandleWidth, handleY, rangeHandleWidth, rangeHandleHeight, KNOB_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, rangeMaxHandlePipeline, maxX, handleY, rangeHandleWidth, rangeHandleHeight, KNOB_COLOR);
	}

	private void drawToggle(GuiGraphicsExtractor guiGraphics, int x, int y, boolean enabled) {
		var knobX = x + (enabled ? toggleWidth - toggleKnobSize - 2 : 2);
		var knobY = y + (toggleHeight - toggleKnobSize) / 2.0f;

		RoundedRectangleRenderer.fill(guiGraphics, toggleTrackPipeline, x, y, toggleWidth, toggleHeight, enabled ? ACCENT_COLOR : TRACK_COLOR);
		RoundedRectangleRenderer.fill(guiGraphics, toggleKnobPipeline, knobX, knobY, toggleKnobSize, toggleKnobSize, KNOB_COLOR);
	}

	private void drawPanel(GuiGraphicsExtractor guiGraphics, RenderPipeline shadowPipeline, RenderPipeline blurPipeline, RenderPipeline pipeline, int x, int y, int width, int height) {
		RoundedRectangleRenderer.fill(guiGraphics, shadowPipeline, x - shadowWidth, y - shadowWidth, width + shadowWidth * 2, height + shadowWidth * 2, SHADOW_COLOR);
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
