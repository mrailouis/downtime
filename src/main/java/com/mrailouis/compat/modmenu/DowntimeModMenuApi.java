package com.mrailouis.compat.modmenu;

import com.mrailouis.feature.impl.DowntimeScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class DowntimeModMenuApi implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return DowntimeScreen::new;
	}
}
