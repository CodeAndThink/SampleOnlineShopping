plugins {
    id("java")
    id("org.springframework.boot") version "4.1.1"
}

group = "com.truongngo"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(
        org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
    ))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa") //ánh xạ đối tượng Java với dữ liệu trong database
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    runtimeOnly("org.postgresql:postgresql") //giúp Java kết nối PostgreSQL.
    runtimeOnly("org.flywaydb:flyway-database-postgresql") //quản lý thay đổi cấu trúc database bằng các file SQL có phiên bản.

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

tasks.test {
    useJUnitPlatform()
}