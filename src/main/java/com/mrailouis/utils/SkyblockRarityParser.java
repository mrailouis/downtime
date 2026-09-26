package com.mrailouis.utils;

import com.mrailouis.data.SkyblockRarity;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public final class SkyblockRarityParser {
	private SkyblockRarityParser() {
	}

	public static Optional<SkyblockRarity> parse(ItemStack stack) {
		var lore = stack.get(DataComponents.LORE);
		if (lore == null) {
			return Optional.empty();
		}

		for (var line : lore.lines()) {
			var text = line.getString().toUpperCase(Locale.ROOT);

			if (text.contains("VERY SPECIAL")) {
				return Optional.of(SkyblockRarity.VERY_SPECIAL);
			}

			for (var rarity : SkyblockRarity.values()) {
				if (rarity == SkyblockRarity.VERY_SPECIAL) {
					continue;
				}

				if (text.contains(rarity.name())) {
					return Optional.of(rarity);
				}
			}
		}

		return Optional.empty();
	}
}
