plugins {
  `kotlin-dsl`
}

kotlinDslPluginOptions {
  experimentalWarning.set(false)
}

apply {
  from("../gradle/repositories.gradle.kts")
}
