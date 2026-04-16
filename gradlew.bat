@rem Gradle startup script for Windows
@if "%DEBUG%"=="" @echo off
@rem Set default JVM options
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
@rem Execute Gradle
"%JAVA_HOME%\bin\java.exe" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
