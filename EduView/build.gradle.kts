// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
//    id("org.sonarqube") version "7.2.2.6593" apply true
}

//sonar {
//    properties {
//        property("sonar.projectKey", "sofiaconst_2is70-project")
//        property("sonar.organization", "sofiaconst")
//        property("sonar.projectName", "2is70-project") // Display name shown in SonarQube UI
//        property("sonar.host.url", "https://sonarcloud.io") //location of server for SonarCloud
//        property("sonar.token", System.getenv("SONAR_TOKEN") ?: "") // Authentication token
//
//        property("sonar.sources", "app/src/main/java,app/src/main/kotlin") // Application source code paths
//        property("sonar.tests", "app/src/test/java,app/src/test/kotlin") // Unit test source paths
//        property("sonar.test.inclusions", "**/*Test*.kt,**/*Test*.java")  // Identifies test classes
//        property("sonar.java.binaries", "app/build/tmp/kotlin-classes/debug,app/build/intermediates/javac/debug") // Compiled bytecode required for static analysis
//        property("sonar.junit.reportPaths", "app/build/test-results/testDebugUnitTest")  // JUnit test execution reports
//        property("sonar.exclusions", "**/R.class, **/R$*.class, **/BuildConfig.*, **/Manifest*.*, build/**, **/*.xml")  // Exclude generated and irrelevant files
//
//        property("sonar.scm.disabled", "true") // Temporarily disable SCM detection to avoid Git autodetection error
//
//        //Integrate JacoCo to SonarQube
//        property("sonar.coverage.jacoco.xmlReportPaths", "${rootProject.projectDir}/app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
//    }
//}