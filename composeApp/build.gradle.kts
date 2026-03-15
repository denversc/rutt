import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeHotReload)
  alias(libs.plugins.detekt)
}

dependencies {
  detektPlugins(libs.detekt.compose)
}

detekt {
  buildUponDefaultConfig = true
  config.setFrom(file("$rootDir/detekt.yml"))
  source.setFrom("src")
}

kotlin {
  jvm()

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutinesSwing)
    }
  }
}

compose.desktop {
  application {
    mainClass = "rutt.MainKt"
    // Suppress warnings from org.jetbrains.skiko.Library calling java.lang.System::load
    jvmArgs += listOf("--enable-native-access=ALL-UNNAMED")

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "Rutt"
      packageVersion = "1.0.0"
    }
  }
}
