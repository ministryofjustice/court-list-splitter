plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.10"
  kotlin("plugin.spring") version "1.5.31"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web:2.5.6") {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:9.0.54")
    implementation("org.apache.tomcat.embed:tomcat-embed-el:9.0.54")
    implementation("org.apache.tomcat.embed:tomcat-embed-websocket:9.0.54")
    because("Unpatched vulnerability CVE-2021-42340 in tomcat-embed-*:9.0.53")
  }

  implementation("com.microsoft.azure:applicationinsights-spring-boot-starter:2.6.3")
  implementation("com.amazonaws:aws-java-sdk-sqs:1.12.96")
  implementation("com.amazonaws:aws-java-sdk-sns:1.12.96")
  implementation("org.springframework.cloud:spring-cloud-aws-messaging:2.2.6.RELEASE")
  implementation("org.springframework.boot:spring-boot-starter-validation")

  // Spring uses 2.11.4 - using 2.12.3 breaks Spring.
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.11.4")

  runtimeOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation(platform("org.junit:junit-bom:5.8.1"))
  testRuntimeOnly("org.junit.jupiter:junit-jupiter")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("com.github.tomakehurst:wiremock-jre8:2.31.0")
  testImplementation("org.mockito:mockito-core:4.0.0")
  testImplementation("org.awaitility:awaitility:4.1.0")
}

tasks {
  compileKotlin {
    kotlinOptions {
      jvmTarget = "15"
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
