package com.mrailouis.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class ItemModelUtils {
	private ItemModelUtils() {
	}

	public static boolean isModelLoaded(Identifier itemModel) {
		return Minecraft.getInstance().getModelManager().bakedItemStackModels.containsKey(itemModel);
	}
}
