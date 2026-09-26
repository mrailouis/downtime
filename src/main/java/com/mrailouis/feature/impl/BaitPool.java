package com.mrailouis.feature.impl;

import com.mrailouis.api.RollCard;
import com.mrailouis.data.KuudraLootEntry;
import com.mrailouis.data.KuudraLootTable;
import com.mrailouis.data.KuudraTier;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

final class BaitPool {
	private static final List<String> BAIT_ITEM_NAMES = List.of("Tormentor", "Burning Kuudra Core", "Kuudra Tentacle");

	private BaitPool() {
	}

	static RollCard random(RandomGenerator random) {
		var pool = new ArrayList<RollCard>();

		for (var tier : KuudraTier.values()) {
			var chest = KuudraLootTable.get(tier);
			addBaitEntries(pool, chest.paidChestSlot1());
			addBaitEntries(pool, chest.paidChestSlot2());
		}

		var tentacleDyeChance = KuudraLootTable.get(KuudraTier.INFERNAL).tentacleDyeChance();
		if (tentacleDyeChance != null) {
			pool.add(new LootEntryRollCard(new KuudraLootEntry("Tentacle Dye", "1", null, null, tentacleDyeChance, "LEGENDARY")));
		}

		return pool.get(random.nextInt(pool.size()));
	}

	private static void addBaitEntries(List<RollCard> pool, List<KuudraLootEntry> entries) {
		for (var entry : entries) {
			if (BAIT_ITEM_NAMES.contains(entry.item())) {
				pool.add(new LootEntryRollCard(entry));
			}
		}
	}
}
