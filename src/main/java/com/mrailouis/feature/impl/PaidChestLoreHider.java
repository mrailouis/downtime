package com.mrailouis.feature.impl;

import com.mrailouis.config.ConfigManager;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;

public final class PaidChestLoreHider {
	private static final String KUUDRA_SCREEN_PREFIX = "Kuudra - ";
	private static final String PAID_CHEST_ITEM_NAME = "Paid Chest";
	private static final String HIDDEN_LORE_TEXT = "???";
	private static final ItemLore HIDDEN_LORE = new ItemLore(List.of(Component.literal(HIDDEN_LORE_TEXT)));

	private PaidChestLoreHider() {
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> onScreenOpen(screen));
	}

	private static void onScreenOpen(Screen screen) {
		if (!(screen instanceof AbstractContainerScreen<?> containerScreen) || !containerScreen.getTitle().getString().startsWith(KUUDRA_SCREEN_PREFIX)) {
			return;
		}

		ScreenEvents.afterTick(screen).register(s -> hideLoreIfPresent(containerScreen));
	}

	private static void hideLoreIfPresent(AbstractContainerScreen<?> screen) {
		if (!ConfigManager.getConfig().isCaseOpeningAnimationEnabled()) {
			return;
		}

		for (var slot : screen.getMenu().slots) {
			var stack = slot.getItem();
			if (stack.isEmpty() || !stack.getHoverName().getString().equals(PAID_CHEST_ITEM_NAME)) {
				continue;
			}

			var lore = stack.get(DataComponents.LORE);
			// hidden loot lore
			if (lore != null && lore.lines().size() == 1 && lore.lines().getFirst().getString().equals(HIDDEN_LORE_TEXT)) {
				continue;
			}

			stack.set(DataComponents.LORE, HIDDEN_LORE);
		}
	}
}
