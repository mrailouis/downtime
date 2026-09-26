package com.mrailouis.api;

import com.mrailouis.data.SkyblockRarity;
import net.minecraft.world.item.ItemStack;

public interface RollCard {
	String displayName();

	SkyblockRarity rarity();

	ItemStack icon();
}
