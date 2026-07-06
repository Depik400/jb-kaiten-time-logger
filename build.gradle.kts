plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "com.depik400"
version = "1.2.5"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // IntelliJ Platform dependency
    intellijPlatform {
        create("IC", "2025.1.4.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        bundledPlugin("Git4Idea")
    }

    // Сетевые запросы и JSON
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

intellijPlatform {
    pluginConfiguration {
        name = "Kaiten Time Logger"
        ideaVersion {
            sinceBuild = "241.*"
            untilBuild = "262.*"
        }
        changeNotes = """
            <h3>Version 1.2.5</h3>
            <ul>
                <li>Логирование времени в Kaiten</li>
                <li>Авто-определение карточки из ветки</li>
                <li>Авто-вызов окна при коммите</li>
                <li>Выбор роли</li>
            </ul>
        """.trimIndent()
        description = "Логирование времени в Kaiten прямо из IntelliJ IDEA"
        vendor {
            name = "Depik400"
            email = "depik400@yandex.ru"
            url = "https://github.com/Depik400"
        }
    }

    // Настройки публикации (опционально)
    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }
    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    patchPluginXml {
        sinceBuild.set("241.*")
        untilBuild.set("262.*")
    }

    runIde {
        // ideDir.set(file(System.getenv("IDEA_HOME")))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}