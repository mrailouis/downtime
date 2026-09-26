package com.mrailouis.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.mrailouis.Downtime;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class ItemTextureTable {
	private static final Gson GSON = new Gson();
	private static final String RESOURCE_PATH = "/assets/" + Downtime.MOD_ID + "/data/item_textures.json";
	private static final Map<String, ItemTextureRef> REFS = load();

	private ItemTextureTable() {
	}

	public static ItemTextureRef get(String item) {
		return REFS.get(item);
	}

	private static Map<String, ItemTextureRef> load() {
		try (var stream = ItemTextureTable.class.getResourceAsStream(RESOURCE_PATH)) {
			if (stream == null) {
				throw new IllegalStateException("Missing bundled resource: " + RESOURCE_PATH);
			}

			try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				Map<String, ItemTextureRef> refs = GSON.fromJson(reader, new TypeToken<Map<String, ItemTextureRef>>() {
				}.getType());

				Downtime.LOGGER.info("Loaded {} item texture references", refs.size());
				return refs;
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to load bundled resource: " + RESOURCE_PATH, exception);
		}
	}
}
