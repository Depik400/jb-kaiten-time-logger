plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "com.depik400"
version = "1.0.0"  // лучше использовать семантическую версию без SNAPSHOT для релиза

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

        // Подключаем Git4Idea (если нужен доступ к git-репозиториям)
        bundledPlugin("Git4Idea")

        // Если используете Java-модули, раскомментируйте:
        // bundledPlugin("com.intellij.java")
    }

    // Сетевые запросы и JSON
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

intellijPlatform {
    pluginConfiguration {
        name = "Kaiten Time Logger"
        ideaVersion {
            sinceBuild = "251"
            untilBuild = "251.*" // Ограничиваем версиями 2025.1
        }
        changeNotes = """
            <h3>Version 1.0.0</h3>
            <ul>
                <li>Логирование времени в Kaiten</li>
                <li>Авто-определение карточки из ветки</li>
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
        sinceBuild.set("251")
        untilBuild.set("251.*")
    }

    runIde {
        // Если у вас локально установлена IDEA, можно указать путь
        // ideDir.set(file(System.getenv("IDEA_HOME")))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

// Чтобы избежать предупреждений о дублировании ресурсов
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}