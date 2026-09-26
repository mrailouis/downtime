package com.mrailouis.feature.impl;

import com.mrailouis.api.RollCard;
import com.mrailouis.data.KuudraLootEntry;
import com.mrailouis.data.SkyblockRarity;
import net.minecraft.world.item.ItemStack;

public record LootEntryRollCard(KuudraLootEntry entry) implements RollCard {
	@Override
	public String displayName() {
		return entry.item();
	}

	@Override
	public SkyblockRarity rarity() {
		return entry.skyblockRarity();
	}

	@Override
	public ItemStack icon() {
		return KuudraItemIcons.iconFor(entry);
	}
}
