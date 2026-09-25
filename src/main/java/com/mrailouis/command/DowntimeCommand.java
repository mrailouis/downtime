package com.mrailouis.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mrailouis.feature.impl.DowntimeScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

public final class DowntimeCommand {
	private DowntimeCommand() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("downtime").executes(DowntimeCommand::run));
		dispatcher.register(ClientCommands.literal("dt").executes(DowntimeCommand::run));
	}

	private static int run(CommandContext<FabricClientCommandSource> context) {
		Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new DowntimeScreen()));
		return 1;
	}
}
