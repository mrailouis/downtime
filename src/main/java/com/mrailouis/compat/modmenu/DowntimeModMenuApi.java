package com.mrailouis.compat.modmenu;

import com.mrailouis.feature.impl.DowntimeScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;


// why do people even use mod menu lmfao
public final class DowntimeModMenuApi implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return DowntimeScreen::new;
	}
}
