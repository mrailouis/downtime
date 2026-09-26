package com.mrailouis.api;

import net.minecraft.resources.Identifier;

public interface PostEffectHolder {
	void downtime$applyPostEffect(Identifier id);

	Identifier downtime$getPostEffect();
}
