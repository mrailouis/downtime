package com.mrailouis.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mrailouis.feature.impl.TestRollScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

public final class TestRollCommand {
	private TestRollCommand() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("testroll").executes(TestRollCommand::run));
	}

	private static int run(CommandContext<FabricClientCommandSource> context) {
		Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new TestRollScreen()));
		return 1;
	}
}
