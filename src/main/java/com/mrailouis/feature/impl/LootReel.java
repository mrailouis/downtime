package com.mrailouis.feature.impl;

import com.mrailouis.data.KuudraLootEntry;
import com.mrailouis.data.KuudraLootRoller;
import com.mrailouis.data.KuudraLootTable;
import com.mrailouis.data.KuudraTier;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class LootReel {
	private final List<KuudraLootEntry> entries;
	private final int winnerIndex;

	private LootReel(List<KuudraLootEntry> entries, int winnerIndex) {
		this.entries = entries;
		this.winnerIndex = winnerIndex;
	}

	public static LootReel build(KuudraLootEntry winner, int fillerBeforeWinner, int fillerAfterWinner, RandomGenerator random) {
		var fillerPool = new ArrayList<KuudraLootEntry>();
		for (var tier : KuudraTier.values()) {
			var chest = KuudraLootTable.get(tier);
			for (var entry : chest.paidChestSlot1()) {
				if (KuudraLootRoller.isSafeFiller(entry)) {
					fillerPool.add(entry);
				}
			}
			for (var entry : chest.paidChestSlot2()) {
				if (KuudraLootRoller.isSafeFiller(entry)) {
					fillerPool.add(entry);
				}
			}
		}

		var entries = new ArrayList<KuudraLootEntry>();
		var totalFiller = fillerBeforeWinner + fillerAfterWinner;
		for (var i = 0; i < totalFiller; i++) {
			if (i == fillerBeforeWinner) {
				entries.add(winner);
			}
			entries.add(fillerPool.get(random.nextInt(fillerPool.size())));
		}

		return new LootReel(entries, fillerBeforeWinner);
	}

	public List<KuudraLootEntry> entries() {
		return entries;
	}

	public int winnerIndex() {
		return winnerIndex;
	}
}
