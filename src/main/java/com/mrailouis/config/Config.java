package com.mrailouis.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// default
public class Config {
	private boolean caseOpeningAnimationEnabled = true;
	private double caseOpeningAnimationDurationSeconds = 6.5;
	private double blurStrength = 1.0;
	private boolean tickerSoundEnabled = true;
	private double tickerSoundVolume = 1.0;
	private boolean quickOpenEnabled = true;
	private double quickOpenVolume = 1.0;
	private double baitChanceMin = 0.05;
	private double baitChanceMax = 0.25;
	private boolean downtimeTrackerEnabled = true;
	private double kuudraDowntimeSeconds = 0.0;
	private double vesuviusDowntimeSeconds = 0.0;
	private double croesusDowntimeSeconds = 0.0;

	public double getTotalDowntimeSeconds() {
		return kuudraDowntimeSeconds + vesuviusDowntimeSeconds + croesusDowntimeSeconds;
	}
}
