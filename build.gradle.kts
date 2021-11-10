plugins {
    java
    id("com.github.spotbugs") version "4.7.0"                   // https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs-gradle-plugin
    id("com.diffplug.spotless") version "6.0.0"                 // https://mvnrepository.com/artifact/com.diffplug.spotless/spotless-plugin-gradle
    id("pl.allegro.tech.build.axion-release") version "1.13.5"  // https://mvnrepository.com/artifact/pl.allegro.tech.build.axion-release/pl.allegro.tech.build.axion-release.gradle.plugin?repo=gradle-plugins
}

project.version = scmVersion.version

allprojects {
    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.github.spotbugs")

    group = "org.creek.core"

    java {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "maven-publish")

    project.version = project.parent?.version!!

    extra.apply {
        set("spotBugsVersion", "4.4.2")         // https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs-annotations

        set("guavaVersion", "31.0.1-jre")       // https://mvnrepository.com/artifact/com.google.guava/guava
        set("log4jVersion", "2.14.1")           // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core

        set("junitVersion", "5.8.1")            // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
        set("junitPioneerVersion", "1.4.2")     // https://mvnrepository.com/artifact/org.junit-pioneer/junit-pioneer
        set("mockitoVersion", "4.0.0")          // https://mvnrepository.com/artifact/org.mockito/mockito-junit-jupiter
        set("hamcrestVersion", "2.2")           // https://mvnrepository.com/artifact/org.hamcrest/hamcrest-core
    }

    val guavaVersion : String by extra
    val log4jVersion : String by extra
    val junitVersion: String by extra
    val junitPioneerVersion: String by extra
    val mockitoVersion: String by extra
    val hamcrestVersion : String by extra

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testImplementation("org.junit-pioneer:junit-pioneer:$junitPioneerVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
        testImplementation("org.hamcrest:hamcrest-core:$hamcrestVersion")
        testImplementation("com.google.guava:guava-testlib:$guavaVersion")
        testImplementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
        testImplementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
        testImplementation("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    }

    tasks.compileJava {
        options.compilerArgs.add("-Xlint:all,-serial")
        options.compilerArgs.add("-Werror")
    }

    tasks.test {
        useJUnitPlatform()
        setForkEvery(1)
        maxParallelForks = 4
        testLogging {
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showCauses = true
            showExceptions = true
            showStackTraces = true
        }
    }

    spotless {
        java {
            googleJavaFormat("1.12.0").aosp()
            indentWithSpaces()
            importOrder()
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    spotbugs {
        tasks.spotbugsMain {
            reports.create("html") {
                isEnabled = true
                setStylesheet("fancy-hist.xsl")
            }
        }
        tasks.spotbugsTest {
            reports.create("html") {
                isEnabled = true
                setStylesheet("fancy-hist.xsl")
            }
        }
    }

    tasks.jar {
        archiveBaseName.set("creek-${project.name}")
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifactId = "creek-${project.name}"
            }
        }
    }

    tasks.register("format") {
        dependsOn("spotlessCheck", "spotlessApply")
    }

    tasks.register("static") {
        dependsOn("checkstyleMain", "checkstyleTest", "spotbugsMain", "spotbugsTest")
    }
}
