import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.BaseExtension
import com.android.build.api.dsl.SdkComponents

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("com.github.Aliucord:gradle:main-SNAPSHOT")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.aliucord(configuration: AliucordExtension.() -> Unit) = extensions.getByName<AliucordExtension>("aliucord").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "com.aliucord.gradle")

    aliucord {
        author("Koxx12", 378587857796726785L)
        updateUrl.set("https://raw.githubusercontent.com/koxx12-dev/aliucord-plugins/builds/updater.json")
        buildUrl.set("https://raw.githubusercontent.com/koxx12-dev/aliucord-plugins/builds/%s.zip")
    }

    android {
        compileSdkVersion(30)

        defaultConfig {
            minSdk = 24
            targetSdk = 30
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    dependencies {
        val discord by configurations
        val implementation by configurations

        discord("com.discord:discord:aliucord-SNAPSHOT")
        implementation("com.github.Aliucord:Aliucord:main-SNAPSHOT")

        implementation("androidx.appcompat:appcompat:1.3.1")
        implementation("com.google.android.material:material:1.4.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.0")

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            implementation(files(System.getProperty("user.home")+"/AppData/Local/Android/sdk/platforms/android-30/android.jar"))
        } else {
            implementation(files("/usr/local/lib/android/sdk/platforms/android-30/android.jar"))
        }
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
