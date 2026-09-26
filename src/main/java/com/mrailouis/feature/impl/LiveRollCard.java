package com.mrailouis.feature.impl;

import com.mrailouis.api.RollCard;
import com.mrailouis.data.SkyblockRarity;
import net.minecraft.world.item.ItemStack;

public record LiveRollCard(String displayName, SkyblockRarity rarity, ItemStack icon) implements RollCard {
}
