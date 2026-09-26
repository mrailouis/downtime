package com.mrailouis.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrailouis.api.PostEffectHolder;
import com.mrailouis.mixin.accessor.GameRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
	@Shadow
	@Final
	GuiRenderState renderState;

	@Unique
	private Identifier downtime$currentEffect;

	@Inject(method = "prepare", at = @At("TAIL"))
	private void downtime$capturePostEffect(CallbackInfo ci) {
		this.downtime$currentEffect = this.renderState instanceof PostEffectHolder holder ? holder.downtime$getPostEffect() : null;
	}

	@WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;processBlurEffect()V"))
	private void downtime$applyCustomPostEffect(GameRenderer instance, Operation<Void> original) {
		if (this.downtime$currentEffect != null && instance instanceof GameRendererAccessor accessor) {
			var minecraft = Minecraft.getInstance();
			var chain = minecraft.getShaderManager().getPostChain(this.downtime$currentEffect, LevelTargetBundle.MAIN_TARGETS);
			if (chain != null) {
				chain.process(minecraft.getMainRenderTarget(), accessor.downtime$getResourcePool());
				return;
			}
		}

		original.call(instance);
	}
}
