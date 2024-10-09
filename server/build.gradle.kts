plugins {
    java
    application
    id("io.freefair.lombok") version "8.6"
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.log4j.core)
    implementation(libs.netty.all)
    implementation(project(":record-store"))
    implementation(project(":protocol"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
    useJUnitPlatform()
}

application {
    mainClass.set("io.graphys.wfdbjstore.server.Wave4jServer")
    applicationDefaultJvmArgs = listOf("--enable-preview", "-Dio.netty.leakDetectionLevel=PARANOID")
}
