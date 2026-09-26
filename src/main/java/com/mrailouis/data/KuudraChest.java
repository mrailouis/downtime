package com.mrailouis.data;

import java.util.List;

public record KuudraChest(
		List<KuudraLootEntry> paidChestSlot1,
		List<KuudraLootEntry> paidChestSlot2,
		List<KuudraLootEntry> guaranteed,
		Double tentacleDyeChance) {
}
