package com.mrailouis.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.mrailouis.Downtime;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class KuudraLootTable {
	private static final Gson GSON = new Gson();
	private static final String RESOURCE_PATH = "/assets/" + Downtime.MOD_ID + "/data/kuudra_loot.json";
	private static final Map<KuudraTier, KuudraChest> TIERS = load();

	private KuudraLootTable() {
	}

	public static KuudraChest get(KuudraTier tier) {
		return TIERS.get(tier);
	}

	private static Map<KuudraTier, KuudraChest> load() {
		try (var stream = KuudraLootTable.class.getResourceAsStream(RESOURCE_PATH)) {
			if (stream == null) {
				throw new IllegalStateException("Missing bundled resource: " + RESOURCE_PATH);
			}

			try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				Map<String, KuudraChest> raw = GSON.fromJson(reader, new TypeToken<Map<String, KuudraChest>>() {
				}.getType());

				var tiers = new EnumMap<KuudraTier, KuudraChest>(KuudraTier.class);
				raw.forEach((key, chest) -> tiers.put(KuudraTier.valueOf(key.toUpperCase(Locale.ROOT)), chest));

				Downtime.LOGGER.info("Loaded Kuudra loot table with {} tiers", tiers.size());
				return tiers;
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to load bundled resource: " + RESOURCE_PATH, exception);
		}
	}
}
