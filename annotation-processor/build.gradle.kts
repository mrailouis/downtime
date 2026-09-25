plugins {
	`java-library`
}

repositories {
	mavenCentral()
}

java {
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

dependencies {
	compileOnly("org.projectlombok:lombok:${providers.gradleProperty("lombok_version").get()}")
	annotationProcessor("org.projectlombok:lombok:${providers.gradleProperty("lombok_version").get()}")
}
