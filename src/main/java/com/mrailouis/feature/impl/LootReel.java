package com.mrailouis.feature.impl;

import com.mrailouis.api.RollCard;
import com.mrailouis.data.KuudraLootRoller;
import com.mrailouis.data.KuudraLootTable;
import com.mrailouis.data.KuudraTier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public final class LootReel {
	private static final int BAIT_MIN_OFFSET = 1;
	private static final int BAIT_MAX_OFFSET = 3;

	private final List<RollCard> entries;
	private final int winnerIndex;

	private LootReel(List<RollCard> entries, int winnerIndex) {
		this.entries = entries;
		this.winnerIndex = winnerIndex;
	}

	public static LootReel build(RollCard winner, int fillerBeforeWinner, int fillerAfterWinner, Optional<KuudraTier> tierFilter, double baitChanceMin, double baitChanceMax, RandomGenerator random) {
		var fillerPool = new ArrayList<RollCard>();
		var tiers = tierFilter.map(tier -> List.of(tier)).orElse(List.of(KuudraTier.values()));
		for (var tier : tiers) {
			var chest = KuudraLootTable.get(tier);
			for (var entry : chest.paidChestSlot1()) {
				if (KuudraLootRoller.isSafeFiller(entry)) {
					fillerPool.add(new LootEntryRollCard(entry));
				}
			}
			for (var entry : chest.paidChestSlot2()) {
				if (KuudraLootRoller.isSafeFiller(entry)) {
					fillerPool.add(new LootEntryRollCard(entry));
				}
			}
		}

		var baitChance = baitChanceMin + (baitChanceMax - baitChanceMin) * random.nextDouble();
		var baitIndex = pickBaitIndex(fillerBeforeWinner, baitChance, random);

		var entries = new ArrayList<RollCard>();
		var totalFiller = fillerBeforeWinner + fillerAfterWinner;
		for (var i = 0; i < totalFiller; i++) {
			if (i == fillerBeforeWinner) {
				entries.add(winner);
			}

			if (i == baitIndex) {
				entries.add(BaitPool.random(random));
			} else {
				entries.add(fillerPool.get(random.nextInt(fillerPool.size())));
			}
		}

		return new LootReel(entries, fillerBeforeWinner);
	}

	private static int pickBaitIndex(int fillerBeforeWinner, double baitChance, RandomGenerator random) {
		if (fillerBeforeWinner <= BAIT_MIN_OFFSET || random.nextDouble() >= baitChance) {
			return -1;
		}

		var maxOffset = Math.min(BAIT_MAX_OFFSET, fillerBeforeWinner);
		var offset = BAIT_MIN_OFFSET + random.nextInt(maxOffset - BAIT_MIN_OFFSET + 1);
		return fillerBeforeWinner - offset;
	}

	public List<RollCard> entries() {
		return entries;
	}

	public int winnerIndex() {
		return winnerIndex;
	}
}
