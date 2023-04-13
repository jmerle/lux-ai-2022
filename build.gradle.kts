import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.jmerle.luxai2022"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20220924")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        suppressWarnings = true
    }
}

// val agentDirs: Array<File> = project.rootDir.resolve("src/main/kotlin/dev/jmerle/luxai2022").listFiles()!!
val agentDirs: Array<File> = project.rootDir.resolve("src").listFiles()!!

agentDirs.forEach { agentDir ->
    val name = agentDir.name
    val outputDir = project.buildDir.resolve("agents/$name")

    sourceSets.create(name) {
        java.srcDir(agentDir)
        compileClasspath += sourceSets["main"].compileClasspath
    }

    task<ShadowJar>("${name}Jar") {
        group = "build"
        description = "Build agent $name and prepare for submission."

        from(sourceSets[name].output)
        configurations.add(project.configurations.compileClasspath.get())

        destinationDirectory.set(outputDir)

        archiveBaseName.set("agent")
        archiveClassifier.set("")
        archiveVersion.set("")

        manifest {
            attributes("Main-Class" to "dev.jmerle.luxai2022.MainKt")
        }
    }

    task<Tar>(name) {
        group = "lux"
        description = "Create submission archive for agent $name."

        dependsOn("${name}Jar")

        doFirst {
            agentDir.resolve("main.py").copyTo(outputDir.resolve("main.py"), overwrite = true)
        }

        into("/") {
            from(outputDir)
            include("agent.jar", "main.py")
        }

        compression = Compression.GZIP

        destinationDirectory.set(outputDir)

        archiveBaseName.set(name)
        archiveClassifier.set("")
        archiveVersion.set("")
        archiveExtension.set("tar.gz")
    }
}
