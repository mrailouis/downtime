package com.mrailouis;

import com.mrailouis.command.DowntimeCommand;
import com.mrailouis.command.DumpContainersCommand;
import com.mrailouis.command.TestRollCommand;
import com.mrailouis.compat.HypixelLocationTracker;
import com.mrailouis.feature.impl.ContainerDumper;
import com.mrailouis.feature.impl.PaidChestInterceptor;
import com.mrailouis.feature.impl.PaidChestLoreHider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class DowntimeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			DowntimeCommand.register(dispatcher);
			TestRollCommand.register(dispatcher);
			DumpContainersCommand.register(dispatcher);
		});

		ContainerDumper.init();
		HypixelLocationTracker.init();
		PaidChestInterceptor.init();
		PaidChestLoreHider.init();
	}
}
