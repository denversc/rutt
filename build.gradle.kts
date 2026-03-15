plugins {
  alias(libs.plugins.detekt)
  alias(libs.plugins.spotless)
  // this is necessary to avoid the plugins to be loaded multiple times
  // in each subproject's classloader
  alias(libs.plugins.composeHotReload) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
}

spotless {
  format("misc") {
    target("**/.gitignore", "**/*.properties", "**/*.toml", "**/*.xml")
    targetExclude("**/build/**", "**/.idea/**")
    trimTrailingWhitespace()
    leadingTabsToSpaces(2)
    endWithNewline()
  }
  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**", "**/.idea/**")
    ktfmt(libs.ktfmt.get().version)
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("**/build/**", "**/.idea/**")
    ktfmt(libs.ktfmt.get().version)
    trimTrailingWhitespace()
    endWithNewline()
  }
  flexmark {
    flexmark(libs.flexmark.get().version)
    target("**/*.md")
    targetExclude("**/build/**", "**/.idea/**")
    trimTrailingWhitespace()
    endWithNewline()
  }
}
