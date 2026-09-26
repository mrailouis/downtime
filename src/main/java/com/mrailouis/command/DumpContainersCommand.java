package com.mrailouis.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mrailouis.feature.impl.ContainerDumper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;


// debugging, ignore
public final class DumpContainersCommand {
	private DumpContainersCommand() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("dumpcontainers").executes(DumpContainersCommand::run));
	}

	private static int run(CommandContext<FabricClientCommandSource> context) {
		var enabled = ContainerDumper.toggle();
		context.getSource().sendFeedback(Component.literal("Container dumping " + (enabled ? "enabled" : "disabled") + "."));
		return 1;
	}
}
