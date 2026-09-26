package com.mrailouis.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mrailouis.data.KuudraLootEntry;
import com.mrailouis.data.KuudraLootRoller;
import com.mrailouis.data.KuudraLootTable;
import com.mrailouis.data.KuudraTier;
import com.mrailouis.feature.impl.LootEntryRollCard;
import com.mrailouis.feature.impl.TestRollScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

// debugging, ignore
public final class TestRollCommand {
	private static final Random RANDOM = new Random();

	private TestRollCommand() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("testroll")
				.executes(context -> run(context, null))
				.then(ClientCommands.argument("item", StringArgumentType.word())
						.executes(context -> run(context, StringArgumentType.getString(context, "item")))));
	}

	private static int run(CommandContext<FabricClientCommandSource> context, String requestedItemId) {
		if (requestedItemId == null) {
			var winner = KuudraLootRoller.pickWeighted(allWeightedEntries(), RANDOM);
			Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new TestRollScreen(new LootEntryRollCard(winner), Optional.empty(), null)));
			return 1;
		}

		var resolved = resolveItem(requestedItemId);
		if (resolved.isEmpty()) {
			context.getSource().sendError(Component.literal("Unknown Kuudra loot item: " + requestedItemId));
			return 0;
		}

		Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new TestRollScreen(new LootEntryRollCard(resolved.get()), Optional.empty(), null)));
		return 1;
	}

	private static List<KuudraLootEntry> allWeightedEntries() {
		var entries = new ArrayList<KuudraLootEntry>();
		for (var tier : KuudraTier.values()) {
			var chest = KuudraLootTable.get(tier);
			entries.addAll(chest.paidChestSlot1());
			entries.addAll(chest.paidChestSlot2());
		}
		return entries;
	}

	private static Optional<KuudraLootEntry> resolveItem(String rawId) {
		var normalized = rawId.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");

		if (normalized.equals("TENTACLE_DYE")) {
			var tentacleDyeChance = KuudraLootTable.get(KuudraTier.INFERNAL).tentacleDyeChance();
			if (tentacleDyeChance != null) {
				return Optional.of(new KuudraLootEntry("Tentacle Dye", "1", null, null, tentacleDyeChance, "LEGENDARY"));
			}
		}

		for (var tier : KuudraTier.values()) {
			var chest = KuudraLootTable.get(tier);
			for (var entry : chest.paidChestSlot1()) {
				if (KuudraLootRoller.toId(entry).equals(normalized)) {
					return Optional.of(entry);
				}
			}
			for (var entry : chest.paidChestSlot2()) {
				if (KuudraLootRoller.toId(entry).equals(normalized)) {
					return Optional.of(entry);
				}
			}
			for (var entry : chest.guaranteed()) {
				if (KuudraLootRoller.toId(entry).equals(normalized)) {
					return Optional.of(entry);
				}
			}
		}

		return Optional.empty();
	}
}
