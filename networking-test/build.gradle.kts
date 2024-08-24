plugins {
    id("java")
    id("io.freefair.lombok") version "8.6"
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.netty.all)
    implementation(libs.log4j.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.test {
    jvmArgs("--enable-preview");
    systemProperty("java.library.path", "/usr/local/lib")
    systemProperty("io.netty.leakDetectionLevel", "ADVANCED")
    useJUnitPlatform()
}


