@rem ##########################################################################
@rem
@rem Script to set up Kokoro worker and run Windows tests
@rem
@rem ##########################################################################
@rem
@rem To run locally execute 'buildscript\kokoro\windows.bat'.
type c:\VERSION

@rem Enter repo root
cd /d %~dp0\..\..

@rem Clear JAVA_HOME to prevent a different Java version from being used
set JAVA_HOME=
set PATH=C:\Program Files\java\jdk1.8.0_152\bin;%PATH%

cmd.exe /C "%cd%\gradlew.bat" clean build || exit /b 1
pushd examples
cmd.exe /C "%cd%\gradlew.bat" clean assemble check --stacktrace || exit /b 1
popd

