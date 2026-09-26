package com.mrailouis.utils;

public final class DurationFormatter {
	private DurationFormatter() {
	}

	public static String format(double totalSeconds) {
		var seconds = (long) totalSeconds;
		var days = seconds / 86400;
		var hours = (seconds % 86400) / 3600;
		var minutes = (seconds % 3600) / 60;
		var remainingSeconds = seconds % 60;

		if (days > 0) {
			return "%dd %dh %dm".formatted(days, hours, minutes);
		}

		if (hours > 0) {
			return "%dh %dm %ds".formatted(hours, minutes, remainingSeconds);
		}

		if (minutes > 0) {
			return "%dm %ds".formatted(minutes, remainingSeconds);
		}

		return "%.1fs".formatted(totalSeconds);
	}
}
