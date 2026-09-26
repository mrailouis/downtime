pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
	}

	plugins {
		id("net.fabricmc.fabric-loom") version providers.gradleProperty("loom_version")
		id("com.diffplug.spotless") version providers.gradleProperty("spotless_version")
	}
}

// Should match your modid
rootProject.name = "downtime"
