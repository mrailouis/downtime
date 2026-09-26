package com.mrailouis.data;

import java.util.List;
import java.util.Locale;
import java.util.random.RandomGenerator;

public final class KuudraLootRoller {
	private KuudraLootRoller() {
	}

	public static KuudraLootEntry pickWeighted(List<KuudraLootEntry> pool, RandomGenerator random) {
		var totalWeight = 0.0;
		for (var entry : pool) {
			totalWeight += entry.weight() != null ? entry.weight() : 0.0;
		}

		var roll = random.nextDouble() * totalWeight;
		var cursor = 0.0;

		for (var entry : pool) {
			cursor += entry.weight() != null ? entry.weight() : 0.0;
			if (roll < cursor) {
				return entry;
			}
		}

		return pool.get(pool.size() - 1);
	}

	public static boolean isSafeFiller(KuudraLootEntry entry) {
		if (entry.enchant() != null) {
			return false;
		}

		var item = entry.item();
		if (item.equals("Crimson Essence")) {
			return true;
		}

		return item.matches("(Aurora|Crimson|Terror|Hollow|Fervor) (Helmet|Chestplate|Leggings|Boots)");
	}

	public static String toId(KuudraLootEntry entry) {
		var base = entry.enchant() != null ? entry.enchant() : entry.item();
		return base.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
	}
}
