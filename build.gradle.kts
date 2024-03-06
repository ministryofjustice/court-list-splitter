plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.13.0"
  kotlin("plugin.spring") version "1.8.22"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {

  implementation("org.springframework.boot:spring-boot-starter-validation")

  implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:1.3.1")

  // Spring uses 2.11.4 - using 2.12.3 breaks Spring.
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.4")

  runtimeOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("org.testcontainers:localstack:1.18.1")
}

tasks {
  compileKotlin {
    kotlinOptions {
      jvmTarget = "19"
    }
  }

  compileTestKotlin {
    kotlinOptions {
      jvmTarget = "19"
    }
  }

  test {
    useJUnitPlatform()
    testLogging.showExceptions = true
    testLogging.showStackTraces = true
    exclude("**/*IntTest*")

    val failedTests = mutableListOf<TestDescriptor>()
    val skippedTests = mutableListOf<TestDescriptor>()

    // See https://github.com/gradle/kotlin-dsl/issues/836
    addTestListener(object : TestListener {
      override fun beforeSuite(suite: TestDescriptor) {}
      override fun beforeTest(testDescriptor: TestDescriptor) {}
      override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        when (result.resultType) {
          TestResult.ResultType.FAILURE -> failedTests.add(testDescriptor)
          TestResult.ResultType.SKIPPED -> skippedTests.add(testDescriptor)
        }
      }

      override fun afterSuite(suite: TestDescriptor, result: TestResult) {
        if (suite.parent == null) { // root suite
          logger.lifecycle("----")
          logger.lifecycle("Test result: ${result.resultType}")
          logger.lifecycle(
            "Test summary: ${result.testCount} tests, " +
              "${result.successfulTestCount} succeeded, " +
              "${result.failedTestCount} failed, " +
              "${result.skippedTestCount} skipped"
          )
          if (failedTests.isNotEmpty()) {
            logger.lifecycle("\tFailed Tests:")
            failedTests.forEach {
              parent?.let { parent ->
                logger.lifecycle("\t\t${parent.name} - ${it.name}")
              } ?: logger.lifecycle("\t\t${it.name}")
            }
          }

          if (skippedTests.isNotEmpty()) {
            logger.lifecycle("\tSkipped Tests:")
            skippedTests.forEach {
              parent?.let { parent ->
                logger.lifecycle("\t\t${parent.name} - ${it.name}")
              } ?: logger.lifecycle("\t\t${it.name}")
            }
          }
        }
      }
    })
  }
}

task<Test>("integrationTest") {
  description = "Runs the integration tests"
  group = "verification"
  testLogging.showExceptions = true
  testLogging.showStackTraces = true
  include("**/*IntTest*")
}
repositories {
  mavenCentral()
}

tasks.register<Copy>("installGitHooks") {
  from(layout.projectDirectory.dir("hooks"))
  into(layout.projectDirectory.dir(".git/hooks"))
}
