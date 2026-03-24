// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("org.sonarqube") version "7.2.3.7755" apply true
}

sonarqube {
    properties {
        property("sonar.projectKey", "sofiaconst_2is70-project")
        property("sonar.organization", "sofiaconst")
        property("sonar.projectName", "2is70-project")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.token", System.getenv("SONAR_TOKEN") ?: "")

        property(
            "sonar.java.binaries",
            "app/build/tmp/kotlin-classes/debug,app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes"
        )
//        property("sonar.test.inclusions", "app/src/test/**/*.java")

        property("sonar.junit.reportPaths", "app/build/test-results/testDebugUnitTest")
        property("sonar.coverage.jacoco.xmlReportPaths", "${rootProject.projectDir}/app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        property("sonar.exclusions", "**/R.class, **/R$*.class, **/BuildConfig.*, **/Manifest*.*, build/**, **/*.xml")
    }
}
