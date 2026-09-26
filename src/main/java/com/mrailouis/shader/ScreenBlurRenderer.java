package com.mrailouis.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public final class ScreenBlurRenderer {
	private static final GpuSampler BLUR_SAMPLER = RenderSystem.getSamplerCache().getSampler(
			AddressMode.CLAMP_TO_EDGE,
			AddressMode.CLAMP_TO_EDGE,
			FilterMode.LINEAR,
			FilterMode.LINEAR,
			false);
	private static GpuTexture copyTexture;
	private static GpuTextureView copyTextureView;
	private static int copyWidth = -1;
	private static int copyHeight = -1;

	private ScreenBlurRenderer() {
	}

	public static void draw(GuiGraphicsExtractor extractor, RenderPipeline pipeline, int x, int y, int width, int height, int color) {
		extractor.guiRenderState.addGuiElement(new LazyBlurRenderState(
				pipeline,
				extractor.pose(),
				x,
				y,
				width,
				height,
				color,
				extractor.scissorStack.peek()));
	}

	private static GpuTextureView snapshotScreen() {
		var screenTexture = Minecraft.getInstance().getMainRenderTarget().getColorTexture();
		var width = screenTexture.getWidth(0);
		var height = screenTexture.getHeight(0);
		ensureCopyTexture(screenTexture, width, height);
		RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
				screenTexture,
				copyTexture,
				0,
				0,
				0,
				0,
				0,
				width,
				height);
		return copyTextureView;
	}

	private static void ensureCopyTexture(GpuTexture source, int width, int height) {
		if (copyTexture != null && !copyTexture.isClosed() && copyTextureView != null && !copyTextureView.isClosed() && copyWidth == width && copyHeight == height) {
			return;
		}
		closeCopyTexture();
		var usage = GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST;
		copyTexture = RenderSystem.getDevice().createTexture("downtime/screen_blur_copy", usage, source.getFormat(), width, height, 1, 1);
		copyTextureView = RenderSystem.getDevice().createTextureView(copyTexture);
		copyWidth = width;
		copyHeight = height;
	}

	private static void closeCopyTexture() {
		if (copyTextureView != null && !copyTextureView.isClosed()) {
			copyTextureView.close();
		}
		if (copyTexture != null && !copyTexture.isClosed()) {
			copyTexture.close();
		}
		copyTextureView = null;
		copyTexture = null;
		copyWidth = -1;
		copyHeight = -1;
	}

	private record LazyBlurRenderState(
			RenderPipeline pipeline,
			Matrix3x2fc pose,
			int x,
			int y,
			int width,
			int height,
			int color,
			ScreenRectangle scissorArea,
			ScreenRectangle bounds) implements GuiElementRenderState {
		private LazyBlurRenderState(RenderPipeline pipeline, Matrix3x2fc pose, int x, int y, int width, int height, int color, ScreenRectangle scissorArea) {
			this(
					pipeline,
					new Matrix3x2f(pose),
					x,
					y,
					width,
					height,
					color,
					scissorArea,
					new ScreenRectangle(x, y, width, height).transformMaxBounds(pose));
		}

		@Override
		public TextureSetup textureSetup() {
			return TextureSetup.singleTexture(snapshotScreen(), BLUR_SAMPLER);
		}

		@Override
		public void buildVertices(VertexConsumer vertexConsumer) {
			var x1 = x + width;
			var y1 = y + height;

			vertexConsumer.addVertexWith2DPose(pose, x, y).setUv(0.0f, 0.0f).setColor(color);
			vertexConsumer.addVertexWith2DPose(pose, x, y1).setUv(0.0f, height).setColor(color);
			vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(width, height).setColor(color);
			vertexConsumer.addVertexWith2DPose(pose, x1, y).setUv(width, 0.0f).setColor(color);
		}
	}
}
