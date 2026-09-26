package com.mrailouis.compat;

import com.mrailouis.data.KuudraTier;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.DisplaySlot;

public final class KuudraTierTracker {
	private static final Pattern TIER_PATTERN = Pattern.compile("\\(T([1-5])\\)");
	// need more elegant method
	private KuudraTierTracker() {
	}

	public static Optional<KuudraTier> currentTier() {
		var minecraft = Minecraft.getInstance();
		var level = minecraft.level;
		if (level == null) {
			return Optional.empty();
		}

		var scoreboard = level.getScoreboard();
		var objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (objective == null) {
			return Optional.empty();
		}

		for (var entry : scoreboard.listPlayerScores(objective)) {
			var line = entry.display() != null ? entry.display().getString() : entry.owner();
			var matcher = TIER_PATTERN.matcher(line);
			if (matcher.find()) {
				return Optional.of(tierFromNumber(Integer.parseInt(matcher.group(1))));
			}
		}

		return Optional.empty();
	}

	private static KuudraTier tierFromNumber(int number) {
		return switch (number) {
			case 1 -> KuudraTier.BASIC;
			case 2 -> KuudraTier.HOT;
			case 3 -> KuudraTier.BURNING;
			case 4 -> KuudraTier.FIERY;
			default -> KuudraTier.INFERNAL;
		};
	}
}
