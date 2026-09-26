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
	private static final double US_MINIMUM_WAGE_PER_HOUR = 7.25;

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
		var config = ConfigManager.getConfig();
		var kuudraSeconds = config.getKuudraDowntimeSeconds();
		var vesuviusSeconds = config.getVesuviusDowntimeSeconds();
		var croesusSeconds = config.getCroesusDowntimeSeconds();
		var totalSeconds = kuudraSeconds + vesuviusSeconds + croesusSeconds;

		var prefix = Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
				.append(GradientText.of("mrai", GRADIENT_FROM_RGB, GRADIENT_TO_RGB))
				.append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));

		context.getSource().sendFeedback(prefix.copy()
				.append(Component.literal(" Hey little chuddy! You've wasted " + DurationFormatter.format(totalSeconds) + " watching paid chest gamba!").withStyle(ChatFormatting.GRAY)));
		context.getSource().sendFeedback(Component.literal("  Kuudra: " + DurationFormatter.format(kuudraSeconds)).withStyle(ChatFormatting.GRAY));
		context.getSource().sendFeedback(Component.literal("  Vesuvius: " + DurationFormatter.format(vesuviusSeconds)).withStyle(ChatFormatting.GRAY));
		context.getSource().sendFeedback(Component.literal("  Croesus: " + DurationFormatter.format(croesusSeconds)).withStyle(ChatFormatting.GRAY));

		var minimumWageEarnings = (totalSeconds / 3600.0) * US_MINIMUM_WAGE_PER_HOUR;
		context.getSource().sendFeedback(Component.literal("In the USA, with a minimum wage job you would have made: $" + "%.2f".formatted(minimumWageEarnings)).withStyle(ChatFormatting.GOLD));

		return 1;
	}
}
