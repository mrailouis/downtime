package com.mrailouis.compat.modmenu;

import com.mrailouis.compat.yacl.DowntimeYaclScreenFactory;
import com.mrailouis.feature.impl.DowntimeScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public final class DowntimeModMenuApi implements ModMenuApi {
	private static final String YACL_MOD_ID = "yet_another_config_lib_v3";

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (FabricLoader.getInstance().isModLoaded(YACL_MOD_ID)) {
			return DowntimeYaclScreenFactory::create;
		}

		return DowntimeScreen::new;
	}
}
