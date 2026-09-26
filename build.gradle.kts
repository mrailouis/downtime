plugins {
	id("net.fabricmc.fabric-loom")
	id("com.diffplug.spotless")
	`maven-publish`
}

repositories {
	maven {
		name = "DevAuth"
		url = uri("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
	}
	maven {
		name = "Hypixel"
		url = uri("https://repo.hypixel.net/repository/Hypixel/")
	}
	maven {
		name = "Modrinth"
		url = uri("https://api.modrinth.com/maven")
	}
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

loom {
	accessWidenerPath.set(file("src/main/resources/downtime.accesswidener"))

	runConfigs.named("client") {
		jvmArguments.add("-Ddevauth.enabled=true")
		jvmArguments.add("-Ddevauth.account=main")
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

	implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")

	implementation("net.hypixel:mod-api:${providers.gradleProperty("hypixel_mod_api_version").get()}")
	implementation("maven.modrinth:hypixel-mod-api:${providers.gradleProperty("hypixel_mod_api_fabric_version").get()}")

	//soft-compat, never required at runtime
	compileOnly("maven.modrinth:modmenu:${providers.gradleProperty("modmenu_version").get()}")

	//dev-instance only, so modmenu/yacl are loaded when running the client for testing
	localRuntime("maven.modrinth:modmenu:${providers.gradleProperty("modmenu_version").get()}")
	localRuntime("maven.modrinth:yacl:${providers.gradleProperty("yacl_version").get()}")

	//lombok my beloved <3
	compileOnly("org.projectlombok:lombok:${providers.gradleProperty("lombok_version").get()}")
	annotationProcessor("org.projectlombok:lombok:${providers.gradleProperty("lombok_version").get()}")

	//devauth
	runtimeOnly("me.djtheredstoner:DevAuth-fabric:${providers.gradleProperty("devauth_version").get()}")
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

// maven
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	repositories {
		//
	}
}
