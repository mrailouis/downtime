package com.mrailouis;

import com.mrailouis.config.ConfigManager;
import com.mrailouis.data.DowntimeSounds;
import com.mrailouis.data.KuudraLootTable;
import com.mrailouis.data.KuudraTier;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Downtime implements ModInitializer {
	public static final String MOD_ID = "downtime";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Downtime initialized");

		ConfigManager.load();
		DowntimeSounds.init();
		KuudraLootTable.get(KuudraTier.BASIC);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
