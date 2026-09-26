package com.mrailouis.extensions.net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mrailouis.api.PostEffectHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;


// modern version slop
public final class GuiGraphicsExtractorExtensions {
	private GuiGraphicsExtractorExtensions() {
	}

	public static void applyPostEffect(GuiGraphicsExtractor extractor, Identifier id) {
		extractor.guiRenderState.nextStratum();
		if (extractor.guiRenderState instanceof PostEffectHolder holder) {
			holder.downtime$applyPostEffect(id);
		}
	}
}
