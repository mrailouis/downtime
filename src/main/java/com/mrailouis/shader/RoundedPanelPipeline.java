package com.mrailouis.shader;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mrailouis.Downtime;
import net.minecraft.resources.Identifier;

public final class RoundedPanelPipeline {
	private static final Identifier SHADER = Identifier.fromNamespaceAndPath(Downtime.MOD_ID, "core/rounded_panel");

	private RoundedPanelPipeline() {
	}

	public static RenderPipeline build(int minX, int minY, int maxX, int maxY, float radius, float edgeSoftness) {
		return RenderPipeline.builder()
				.withLocation(Downtime.id("rounded_panel"))
				.withVertexShader(SHADER)
				.withFragmentShader(SHADER)
				.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
				.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
				.withDepthStencilState(DepthStencilState.DEFAULT)
				.withCull(false)
				.withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
				.withUniform("Projection", UniformType.UNIFORM_BUFFER)
				.withShaderDefine("RECT_MIN_X", (float) minX)
				.withShaderDefine("RECT_MIN_Y", (float) minY)
				.withShaderDefine("RECT_MAX_X", (float) maxX)
				.withShaderDefine("RECT_MAX_Y", (float) maxY)
				.withShaderDefine("RADIUS", radius)
				.withShaderDefine("EDGE_SOFTNESS", edgeSoftness)
				.build();
	}
}
