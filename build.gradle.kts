
plugins {
    application
    kotlin("jvm") version embeddedKotlinVersion
}

application {
  mainClassName = "com.github.jangalinski.tidesoftime.ApplicationKt"
}

dependencies {
  compile(kotlin("stdlib-jdk8"))
  compile(kotlin("reflect"))

  compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
  compile("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.1.0")

  testCompile("org.junit.jupiter:junit-jupiter-api:5.3.1")
  testCompile("org.junit.jupiter:junit-jupiter-params:5.3.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
  testCompile("org.assertj:assertj-core:3.11.1")
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
  }
}

repositories {
    jcenter()
}
