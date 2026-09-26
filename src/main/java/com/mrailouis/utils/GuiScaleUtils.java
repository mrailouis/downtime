package com.mrailouis.utils;

import net.minecraft.client.Minecraft;

public final class GuiScaleUtils {
	private GuiScaleUtils() {
	}

	public static float compensate(float targetEffectiveScale) {
		return targetEffectiveScale / Minecraft.getInstance().getWindow().getGuiScale();
	}
}
