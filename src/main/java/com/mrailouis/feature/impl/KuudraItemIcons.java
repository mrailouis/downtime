package com.mrailouis.feature.impl;

import com.google.common.collect.Multimaps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mrailouis.data.ItemTextureTable;
import com.mrailouis.data.KuudraLootEntry;
import com.mrailouis.utils.ItemModelUtils;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ResolvableProfile;

public final class KuudraItemIcons {
	private static final UUID SKULL_PROFILE_UUID = UUID.fromString("d3cb85e2-3075-48a1-b213-a9bfb62360c1");

	private KuudraItemIcons() {
	}

	public static ItemStack iconFor(KuudraLootEntry entry) {
		if (entry.enchant() != null) {
			return new ItemStack(Items.ENCHANTED_BOOK);
		}

		var ref = ItemTextureTable.get(entry.item());
		if (ref == null) {
			return new ItemStack(Items.NETHER_STAR);
		}

		if (ref.skullUrl() != null) {
			return createSkull(ref.skullUrl());
		}

		var vanillaItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(ref.itemId()));
		var stack = new ItemStack(vanillaItem);

		if (ref.dyeColor() != null) {
			stack.set(DataComponents.DYED_COLOR, new DyedItemColor(ref.dyeColor()));
		}

		if (ref.itemModel() != null) {
			var itemModel = Identifier.parse(ref.itemModel());
			if (ItemModelUtils.isModelLoaded(itemModel)) {
				stack.set(DataComponents.ITEM_MODEL, itemModel);
			}
		}

		return stack;
	}

	private static ItemStack createSkull(String url) {
		var stack = new ItemStack(Items.PLAYER_HEAD);
		var payload = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
		var encoded = Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));

		var property = new Property("textures", encoded);
		var propertyMap = new PropertyMap(Multimaps.forMap(Map.of("textures", property)));
		var profile = new GameProfile(SKULL_PROFILE_UUID, "downtime", propertyMap);

		stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
		return stack;
	}
}
