plugins {
    id("java")
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("io.freefair.lombok") version "8.10.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    version = "21"
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.controlsfx)
    implementation(libs.richtextfx)
    implementation(project(":driver"))
    implementation(project(":protocol"))
    implementation(libs.slf4j.api)
    implementation(libs.slf4j2.impl)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.grafysi.wfdbconsole.Launcher");
}