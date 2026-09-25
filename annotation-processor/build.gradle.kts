plugins {
	`java-library`
	id("com.diffplug.spotless")
}

repositories {
	mavenCentral()
}

spotless {
	java {
		removeUnusedImports()
		importOrder()
		leadingSpacesToTabs()
		trimTrailingWhitespace()
		endWithNewline()
	}

	kotlinGradle {
		leadingSpacesToTabs()
		trimTrailingWhitespace()
		endWithNewline()
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

dependencies {
	compileOnly("org.projectlombok:lombok:${providers.gradleProperty("lombok_version").get()}")
	annotationProcessor("org.projectlombok:lombok:${providers.gradleProperty("lombok_version").get()}")
}
