plugins {
    `java-library`
    `maven-publish`
}

group = "net.prismclient"
version = "1.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
}

dependencies {
    // These are provided by Minecraft
    compileOnly("net.sf.jopt-simple:jopt-simple:4.5")
    compileOnly("org.apache.logging.log4j:log4j-core:2.0-beta9")
    compileOnly("org.apache.logging.log4j:log4j-api:2.0-beta9")

    // These are provided by Prism
    compileOnly("org.ow2.asm:asm:9.7")
    compileOnly("org.ow2.asm:asm-commons:9.7")
    compileOnly("org.ow2.asm:asm-tree:9.7")
    compileOnly("org.ow2.asm:asm-util:9.7")
    compileOnly("org.ow2.asm:asm-analysis:9.7")
}

publishing {
    repositories {
        maven {
            name = "nexus"
            url = uri("https://nexus.prsm.wtf/repository/maven-releases/")
            credentials {
                username = providers.environmentVariable("MAVEN_USER").orNull
                    ?: providers.gradleProperty("nexusUser").orNull
                password = providers.environmentVariable("MAVEN_PASSWORD").orNull
                    ?: providers.gradleProperty("nexusPassword").orNull
            }
        }
    }

    publications {
        create<MavenPublication>(project.name) {
            version = project.version.toString()

            from(components["java"])
        }
    }
}