package com.mrailouis.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class GradientText {
	private GradientText() {
	}

	public static MutableComponent of(String text, int fromRgb, int toRgb) {
		var result = Component.empty();
		var length = text.length();

		for (var i = 0; i < length; i++) {
			var progress = length == 1 ? 0.0f : (float) i / (length - 1);
			var color = lerpColor(fromRgb, toRgb, progress);
			result.append(Component.literal(String.valueOf(text.charAt(i))).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
		}

		return result;
	}

	private static int lerpColor(int fromRgb, int toRgb, float progress) {
		var fromRed = (fromRgb >> 16) & 0xFF;
		var fromGreen = (fromRgb >> 8) & 0xFF;
		var fromBlue = fromRgb & 0xFF;
		var toRed = (toRgb >> 16) & 0xFF;
		var toGreen = (toRgb >> 8) & 0xFF;
		var toBlue = toRgb & 0xFF;

		var red = Math.round(fromRed + (toRed - fromRed) * progress);
		var green = Math.round(fromGreen + (toGreen - fromGreen) * progress);
		var blue = Math.round(fromBlue + (toBlue - fromBlue) * progress);

		return (red << 16) | (green << 8) | blue;
	}
}
