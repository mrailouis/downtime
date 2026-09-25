package com.mrailouis.data;

public enum SkyblockRarity {
	COMMON(0xFFFFFFFF),
	UNCOMMON(0xFF55FF55),
	RARE(0xFF5555FF),
	EPIC(0xFFAA00AA),
	LEGENDARY(0xFFFFAA00),
	MYTHIC(0xFFFF55FF),
	SPECIAL(0xFFFF5555),
	VERY_SPECIAL(0xFFAA0000),
	DIVINE(0xFF55FFFF);

	private final int color;

	SkyblockRarity(int color) {
		this.color = color;
	}

	public int color() {
		return color;
	}
}
