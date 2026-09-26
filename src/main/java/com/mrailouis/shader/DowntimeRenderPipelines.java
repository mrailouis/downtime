package com.mrailouis.shader;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mrailouis.Downtime;
import net.minecraft.client.renderer.RenderPipelines;

public final class DowntimeRenderPipelines {
	private DowntimeRenderPipelines() {
	}

	public static RenderPipeline roundedRectangle(String name, float width, float height, float radius) {
		return roundedRectangle(name, width, height, radius, 0.0f);
	}

	public static RenderPipeline roundedRectangleShadow(String name, float width, float height, float radius, float shadowWidth) {
		return roundedRectangle(name, width, height, radius, shadowWidth);
	}

	public static RenderPipeline itemCard(String name, float width, float height, float radius, float barHeight, float fadeHeight) {
		return RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
				.withLocation(Downtime.id("pipeline/" + name))
				.withVertexShader(Downtime.id("core/gui_item_card"))
				.withFragmentShader(Downtime.id("core/gui_item_card"))
				.withShaderDefine("RECT_WIDTH", width)
				.withShaderDefine("RECT_HEIGHT", height)
				.withShaderDefine("RECT_RADIUS", radius)
				.withShaderDefine("BAR_HEIGHT", barHeight)
				.withShaderDefine("FADE_HEIGHT", fadeHeight)
				.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
				.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
				.build());
	}

	public static RenderPipeline panelBlur(String name, float width, float height, float radius) {
		return RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
				.withLocation(Downtime.id("pipeline/" + name))
				.withVertexShader(Downtime.id("core/gui_full_screen_blur"))
				.withFragmentShader(Downtime.id("core/gui_panel_blur"))
				.withShaderDefine("RECT_WIDTH", width)
				.withShaderDefine("RECT_HEIGHT", height)
				.withShaderDefine("RECT_RADIUS", radius)
				.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
				.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
				.build());
	}

	public static RenderPipeline triangle(String name, float width, float height, float pointDirection) {
		return RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
				.withLocation(Downtime.id("pipeline/" + name))
				.withVertexShader(Downtime.id("core/gui_rounded_rectangle"))
				.withFragmentShader(Downtime.id("core/gui_triangle"))
				.withShaderDefine("RECT_WIDTH", width)
				.withShaderDefine("RECT_HEIGHT", height)
				.withShaderDefine("POINT_DIRECTION", pointDirection)
				.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
				.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
				.build());
	}

	private static RenderPipeline roundedRectangle(String name, float width, float height, float radius, float shadowWidth) {
		return RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
				.withLocation(Downtime.id("pipeline/" + name))
				.withVertexShader(Downtime.id("core/gui_rounded_rectangle"))
				.withFragmentShader(Downtime.id("core/gui_rounded_rectangle"))
				.withShaderDefine("RECT_WIDTH", width)
				.withShaderDefine("RECT_HEIGHT", height)
				.withShaderDefine("RECT_RADIUS", radius)
				.withShaderDefine("SHADOW_WIDTH", shadowWidth)
				.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
				.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
				.build());
	}
}
