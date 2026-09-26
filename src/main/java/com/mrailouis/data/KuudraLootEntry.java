package com.mrailouis.data;

public record KuudraLootEntry(String item, String amount, String enchant, Double weight, Double chance, String rarity) {
	public SkyblockRarity skyblockRarity() {
		return rarity == null ? null : SkyblockRarity.valueOf(rarity);
	}
}
