package com.mrailouis.shader;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;

public final class AwtFontRenderer {
	private static final int RASTER_MARGIN_X = 5;
	private static final int RASTER_MARGIN_TOP = 2;
	private static final int SHADOW_OFFSET = 1;
	private static final int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;

	private final Font font;
	private final Map<String, RenderedText> cache = new HashMap<>();

	private AwtFontRenderer(Font font) {
		this.font = font;
	}

	public static AwtFontRenderer create(String resourcePath, float size) {
		return create(resourcePath, Font.PLAIN, size);
	}

	public static AwtFontRenderer create(String resourcePath, int style, float size) {
		try (InputStream inputStream = AwtFontRenderer.class.getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				throw new IllegalStateException("Missing font resource: " + resourcePath);
			}

			var baseFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
			var attributes = new HashMap<TextAttribute, Object>();
			attributes.put(TextAttribute.SIZE, size);
			if ((style & Font.BOLD) != 0) {
				attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
			}
			if ((style & Font.ITALIC) != 0) {
				attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
			}

			return new AwtFontRenderer(baseFont.deriveFont(attributes));
		} catch (IOException | FontFormatException exception) {
			throw new IllegalStateException("Failed to load font resource: " + resourcePath, exception);
		}
	}

	public int width(String text) {
		return text(text, DEFAULT_TEXT_COLOR).logicalTextWidth;
	}

	public int height(String text) {
		return text(text, DEFAULT_TEXT_COLOR).logicalTextHeight;
	}

	public void draw(GuiGraphicsExtractor extractor, String text, int x, int y) {
		draw(extractor, text, x, y, DEFAULT_TEXT_COLOR);
	}

	public void draw(GuiGraphicsExtractor extractor, String text, int x, int y, int color) {
		var renderedText = text(text, color);
		extractor.blit(
				renderedText.texture.getTextureView(),
				renderedText.texture.getSampler(),
				x - renderedText.logicalMarginX,
				y - renderedText.logicalMarginTop,
				x - renderedText.logicalMarginX + renderedText.logicalTextureWidth,
				y - renderedText.logicalMarginTop + renderedText.logicalTextureHeight,
				0.0f,
				1.0f,
				0.0f,
				1.0f);
	}

	private RenderedText text(String text, int color) {
		var scaleFactor = scaleFactor();
		return cache.computeIfAbsent(text + "|" + color + "|" + scaleFactor, ignored -> rasterize(text, color, scaleFactor));
	}

	private RenderedText rasterize(String text, int color, int scaleFactor) {
		var renderFont = font.deriveFont(font.getSize2D() * scaleFactor);
		var metricsImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		var metricsGraphics = metricsImage.createGraphics();

		applyQualityHints(metricsGraphics);
		metricsGraphics.setFont(renderFont);
		var metrics = metricsGraphics.getFontMetrics();
		var marginX = RASTER_MARGIN_X * scaleFactor;
		var marginTop = RASTER_MARGIN_TOP * scaleFactor;
		var shadowOffset = SHADOW_OFFSET * scaleFactor;
		var textWidth = (int) Math.ceil(metrics.getStringBounds(text, metricsGraphics).getWidth());
		var textHeight = metrics.getHeight();
		var width = Math.max(1, textWidth + marginX * 2 + shadowOffset);
		var height = Math.max(1, textHeight + marginTop + shadowOffset + scaleFactor);
		var baseline = metrics.getAscent();
		metricsGraphics.dispose();

		var bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		var graphics = bufferedImage.createGraphics();

		applyQualityHints(graphics);
		graphics.setFont(renderFont);
		graphics.setColor(new Color(0, 0, 0, 72));
		graphics.drawString(text, marginX + shadowOffset, marginTop + baseline + shadowOffset);
		graphics.setColor(new Color(color, true));
		graphics.drawString(text, marginX, marginTop + baseline);
		graphics.dispose();

		var nativeImage = new NativeImage(width, height, false);
		for (var y = 0; y < height; y++) {
			for (var x = 0; x < width; x++) {
				nativeImage.setPixelABGR(x, y, argbToAbgr(bufferedImage.getRGB(x, y)));
			}
		}

		var texture = new LinearDynamicTexture(() -> "downtime/awt_font/" + text, nativeImage);
		return new RenderedText(
				texture,
				divideUp(width, scaleFactor),
				divideUp(height, scaleFactor),
				divideUp(textWidth, scaleFactor),
				divideUp(textHeight, scaleFactor),
				divideUp(marginX, scaleFactor),
				divideUp(marginTop, scaleFactor));
	}

	private static void applyQualityHints(Graphics2D graphics) {
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	}

	private static int divideUp(int value, int divisor) {
		return (value + divisor - 1) / divisor;
	}

	private static int scaleFactor() {
		return Math.max(1, Minecraft.getInstance().getWindow().getGuiScale());
	}

	private static int argbToAbgr(int argb) {
		var alpha = argb & 0xFF000000;
		var red = argb & 0x00FF0000;
		var green = argb & 0x0000FF00;
		var blue = argb & 0x000000FF;
		return alpha | (blue << 16) | green | (red >>> 16);
	}

	private static final class LinearDynamicTexture extends DynamicTexture {
		private LinearDynamicTexture(Supplier<String> label, NativeImage pixels) {
			super(label, pixels);
			sampler = RenderSystem.getSamplerCache().getSampler(
					AddressMode.CLAMP_TO_EDGE,
					AddressMode.CLAMP_TO_EDGE,
					FilterMode.LINEAR,
					FilterMode.LINEAR,
					false);
		}
	}

	private record RenderedText(
			DynamicTexture texture,
			int logicalTextureWidth,
			int logicalTextureHeight,
			int logicalTextWidth,
			int logicalTextHeight,
			int logicalMarginX,
			int logicalMarginTop) {
	}
}
