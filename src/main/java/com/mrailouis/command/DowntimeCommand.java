package com.mrailouis.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mrailouis.config.ConfigManager;
import com.mrailouis.feature.impl.DowntimeScreen;
import com.mrailouis.utils.DurationFormatter;
import com.mrailouis.utils.GradientText;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class DowntimeCommand {
	private static final int GRADIENT_FROM_RGB = 0xFF0000;
	private static final int GRADIENT_TO_RGB = 0xFFFF00;

	private DowntimeCommand() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(ClientCommands.literal("downtime")
				.executes(DowntimeCommand::run)
				.then(ClientCommands.literal("time").executes(DowntimeCommand::runTime)));
		dispatcher.register(ClientCommands.literal("dt")
				.executes(DowntimeCommand::run)
				.then(ClientCommands.literal("time").executes(DowntimeCommand::runTime)));
	}

	private static int run(CommandContext<FabricClientCommandSource> context) {
		Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new DowntimeScreen()));
		return 1;
	}

	private static int runTime(CommandContext<FabricClientCommandSource> context) {
		var totalSeconds = ConfigManager.getConfig().getTotalDowntimeSeconds();
		var duration = DurationFormatter.format(totalSeconds);

		var message = Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
				.append(GradientText.of("mrai", GRADIENT_FROM_RGB, GRADIENT_TO_RGB))
				.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.literal(" Hey little chuddy! You've wasted " + duration + " watching kuudra chest gamba!").withStyle(ChatFormatting.GRAY));

		context.getSource().sendFeedback(message);
		return 1;
	}
}
