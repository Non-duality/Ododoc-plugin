plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.16.1"
  id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.ssafy"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.openjfx:javafx:21.0.3")
  implementation("com.googlecode.json-simple:json-simple:1.1.1")
  implementation("org.java-websocket:Java-WebSocket:1.5.2")
  implementation("org.springframework:spring-webflux:5.3.9")
  implementation("io.projectreactor.netty:reactor-netty:1.0.17")
  implementation("org.jetbrains:annotations:23.0.0")
}

javafx {
  version = "21.0.3"
  modules = listOf("javafx.controls", "javafx.fxml", "javafx.web")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2023.3.4")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf("com.intellij.java"))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }

  patchPluginXml {
    sinceBuild.set("231")
    untilBuild.set("241.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}
