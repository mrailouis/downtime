package com.mrailouis.feature.impl;

import com.mrailouis.compat.KuudraTierTracker;
import com.mrailouis.data.SkyblockRarity;
import com.mrailouis.utils.SkyblockRarityParser;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class PaidChestInterceptor {
	private static final String PAID_CHEST_TITLE = "Paid Chest";
	private static final int RAREST_SLOT_INDEX = 11;

	private static final Map<Screen, Integer> handledRewardHash = new WeakHashMap<>();

	private PaidChestInterceptor() {
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> onScreenOpen(screen));
	}

	private static void onScreenOpen(Screen screen) {
		if (!(screen instanceof AbstractContainerScreen<?> containerScreen) || !containerScreen.getTitle().getString().equals(PAID_CHEST_TITLE)) {
			return;
		}

		ScreenEvents.afterTick(screen).register(s -> tryIntercept(containerScreen));
	}

	private static void tryIntercept(AbstractContainerScreen<?> containerScreen) {
		if (Minecraft.getInstance().screen != containerScreen) {
			return;
		}

		var reward = findRarestReward(containerScreen);
		if (reward.isEmpty()) {
			return;
		}

		var rewardHash = ItemStack.hashItemAndComponents(reward);
		if (handledRewardHash.getOrDefault(containerScreen, 0) == rewardHash) {
			return;
		}

		handledRewardHash.put(containerScreen, rewardHash);

		var rarity = SkyblockRarityParser.parse(reward).orElse(SkyblockRarity.COMMON);
		var card = new LiveRollCard(reward.getHoverName().getString(), rarity, reward.copy());
		var tier = KuudraTierTracker.currentTier();

		Minecraft.getInstance().setScreen(new TestRollScreen(card, tier, containerScreen));
	}

	private static ItemStack findRarestReward(AbstractContainerScreen<?> screen) {
		var slots = screen.getMenu().slots;

		if (RAREST_SLOT_INDEX < slots.size()) {
			var candidate = slots.get(RAREST_SLOT_INDEX).getItem();
			if (isEligibleReward(candidate)) {
				return candidate;
			}
		}

		var best = ItemStack.EMPTY;
		var bestRarityRank = -1;

		for (var slot : slots) {
			var stack = slot.getItem();
			if (!isEligibleReward(stack)) {
				continue;
			}

			var rarity = SkyblockRarityParser.parse(stack);
			if (rarity.isPresent() && rarity.get().ordinal() > bestRarityRank) {
				bestRarityRank = rarity.get().ordinal();
				best = stack;
			}
		}

		return best;
	}

	private static boolean isEligibleReward(ItemStack stack) {
		return !stack.isEmpty() && !stack.is(Items.BLACK_STAINED_GLASS_PANE);
	}
}
