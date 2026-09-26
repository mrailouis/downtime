package com.mrailouis.data;

import com.mrailouis.Downtime;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class DowntimeSounds {
	public static final SoundEvent QUICK_OPEN = register("quick_open");

	private DowntimeSounds() {
	}

	public static void init() {
	}

	private static SoundEvent register(String path) {
		var id = Downtime.id(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}
}
