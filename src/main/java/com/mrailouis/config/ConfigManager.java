package com.mrailouis.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrailouis.Downtime;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigManager {
	// atomic
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("downtime.json");

	@Getter
	private static Config config = new Config();

	private ConfigManager() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			Downtime.LOGGER.info("No config file found at {}, creating one", CONFIG_PATH);
			save();
			return;
		}

		Downtime.LOGGER.info("Loading config from {}", CONFIG_PATH);

		try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
			var loaded = GSON.fromJson(reader, Config.class);
			config = loaded != null ? loaded : new Config();
			Downtime.LOGGER.info("Loaded config from {}", CONFIG_PATH);
		} catch (IOException exception) {
			Downtime.LOGGER.error("Failed to load config from {}, falling back to defaults", CONFIG_PATH, exception);
			config = new Config();
		}
	}

	public static void save() {
		Downtime.LOGGER.info("Saving config to {}", CONFIG_PATH);

		try {
			Files.createDirectories(CONFIG_PATH.getParent());

			var tempFile = Files.createTempFile(CONFIG_PATH.getParent(), "downtime", ".json.tmp");

			try (var writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}

			try {
				Files.move(tempFile, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException exception) {
				Downtime.LOGGER.warn("Atomic move not supported for {}, falling back to a non-atomic replace", CONFIG_PATH);
				Files.move(tempFile, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
			}

			Downtime.LOGGER.info("Saved config to {}", CONFIG_PATH);
		} catch (IOException exception) {
			Downtime.LOGGER.error("Failed to save config to {}", CONFIG_PATH, exception);
		}
	}
}
