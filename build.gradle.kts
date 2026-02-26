plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.diskdesktop"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.diskdesktop.MainApp")
}

javafx {
    // JavaFX 21 совместим с OpenJDK 17
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // REST-клиент: Retrofit + OkHttp + Jackson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("org.slf4j:slf4j-simple:2.0.13")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
