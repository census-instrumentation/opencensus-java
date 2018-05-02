@rem ##########################################################################
@rem
@rem Script to set up Kokoro worker and run Windows tests
@rem
@rem ##########################################################################
@rem
type c:\VERSION

@rem Enter repo root
cd /d %~dp0\..\..

cmd.exe /C "%cd%\gradlew.bat" clean build || exit /b 1
pushd examples
cmd.exe /C "%cd%\gradlew.bat" clean assemble check --stacktrace || exit /b 1
popd

