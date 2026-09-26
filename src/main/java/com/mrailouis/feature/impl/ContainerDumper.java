package com.mrailouis.feature.impl;

import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ContainerDumper {
	private static final Logger LOGGER = LoggerFactory.getLogger("downtime/dumpcontainers");

	private static boolean enabled = false;

	private ContainerDumper() {
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> onScreenOpen(screen));
	}

	public static boolean toggle() {
		enabled = !enabled;
		return enabled;
	}

	private static void onScreenOpen(Screen screen) {
		if (!enabled || !(screen instanceof AbstractContainerScreen<?> containerScreen)) {
			return;
		}

		var snapshot = new HashMap<Integer, Integer>();
		dump(containerScreen, "opened");
		updateSnapshot(containerScreen, snapshot);

		ScreenEvents.afterTick(screen).register(s -> {
			if (hasChanged(containerScreen, snapshot)) {
				dump(containerScreen, "updated");
				updateSnapshot(containerScreen, snapshot);
			}
		});

		ScreenEvents.remove(screen).register(closedScreen -> dump(containerScreen, "closed"));
	}

	private static boolean hasChanged(AbstractContainerScreen<?> screen, Map<Integer, Integer> snapshot) {
		var player = Minecraft.getInstance().player;
		if (player == null) {
			return false;
		}

		for (var slot : screen.getMenu().slots) {
			if (slot.container == player.getInventory()) {
				continue;
			}

			var hash = ItemStack.hashItemAndComponents(slot.getItem());
			if (snapshot.getOrDefault(slot.index, 0) != hash) {
				return true;
			}
		}

		return false;
	}

	private static void updateSnapshot(AbstractContainerScreen<?> screen, Map<Integer, Integer> snapshot) {
		var player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		for (var slot : screen.getMenu().slots) {
			if (slot.container == player.getInventory()) {
				continue;
			}

			snapshot.put(slot.index, ItemStack.hashItemAndComponents(slot.getItem()));
		}
	}

	private static void dump(AbstractContainerScreen<?> screen, String phase) {
		var player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		var gson = new GsonBuilder().setPrettyPrinting().create();
		var builder = new StringBuilder();
		builder.append("Container [").append(phase).append("] title=").append(screen.getTitle().getString()).append('\n');

		var dumped = 0;
		for (var slot : screen.getMenu().slots) {
			if (slot.container == player.getInventory()) {
				continue;
			}

			var stack = slot.getItem();
			if (stack.isEmpty()) {
				continue;
			}

			dumped++;
			builder.append("Slot ").append(slot.index).append(": ").append(stack.getItem()).append(" x").append(stack.getCount()).append('\n');

			for (var component : stack.getComponents()) {
				builder.append("  ").append(component.type()).append(" = ");
				var encoded = component.encodeValue(JsonOps.INSTANCE).result();
				builder.append(encoded.isPresent() ? gson.toJson(encoded.get()) : String.valueOf(component.value()));
				builder.append('\n');
			}
		}

		LOGGER.info(builder.toString());
		player.sendOverlayMessage(Component.literal("Dumped " + dumped + " item(s) from \"" + screen.getTitle().getString() + "\" (" + phase + ") to the log."));
	}
}
