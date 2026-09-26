package com.mrailouis.mixin.interfacemixins;

import com.mrailouis.api.PostEffectHolder;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderState.class)
public abstract class GuiRenderStateMixin implements PostEffectHolder {
	@Shadow
	private int firstStratumAfterBlur;

	@Shadow
	public abstract void blurBeforeThisStratum();

	@Unique
	private Identifier downtime$postEffect;

	@Inject(method = "reset", at = @At("TAIL"))
	private void downtime$resetPostEffect(CallbackInfo ci) {
		this.downtime$postEffect = null;
	}

	@Override
	public void downtime$applyPostEffect(Identifier id) {
		this.firstStratumAfterBlur = Integer.MAX_VALUE;
		this.blurBeforeThisStratum();
		this.downtime$postEffect = id;
	}

	@Override
	public Identifier downtime$getPostEffect() {
		return this.downtime$postEffect;
	}
}
