package com.mrailouis.compat;

import java.util.Locale;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;


public final class HypixelLocationTracker {
	private static volatile ClientboundLocationPacket latest;

	private HypixelLocationTracker() {
	}

	public static void init() {
		HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket.class);
		HypixelModAPI.getInstance().registerHandler(ClientboundLocationPacket.class, packet -> latest = packet);
	}

	public static boolean isOnCrimsonIsle() {
		var packet = latest;
		return packet != null && packet.getMap().map(HypixelLocationTracker::containsCrimson).orElse(false);
	}

	public static boolean isLikelyInKuudra() {
		var packet = latest;
		if (packet == null) {
			return false;
		}

		var mode = packet.getMode().map(HypixelLocationTracker::lower).orElse("");
		var map = packet.getMap().map(HypixelLocationTracker::lower).orElse("");
		return mode.contains("kuudra") || map.contains("kuudra");
	}

	private static boolean containsCrimson(String map) {
		return lower(map).contains("crimson");
	}

	private static String lower(String value) {
		return value.toLowerCase(Locale.ROOT);
	}
}
