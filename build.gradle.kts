plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.8.3"
  kotlin("plugin.spring") version "1.8.10"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

var awsSdkVersion = "1.12.543"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")

  implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.4")
  implementation("com.amazonaws:aws-java-sdk-sqs:$awsSdkVersion")
  implementation("com.amazonaws:aws-java-sdk-sns:$awsSdkVersion")
  implementation("com.amazonaws:aws-java-sdk-sts:$awsSdkVersion")
  implementation("org.springframework.cloud:spring-cloud-aws-messaging:2.2.6.RELEASE")

  // Spring uses 2.11.4 - using 2.12.3 breaks Spring.
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.4")

  runtimeOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation(platform("org.junit:junit-bom:5.9.2"))
  testRuntimeOnly("org.junit.jupiter:junit-jupiter")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
  testImplementation("org.mockito:mockito-core:5.1.1")
  testImplementation("org.awaitility:awaitility:4.2.0")
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
