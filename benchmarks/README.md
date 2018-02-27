# OpenCensus Benchmarks

## To run the benchmark use

```
$ ./gradlew :opencensus-benchmarks:jmh
```

## To debug compilation errors
When you make incompatible changes in the Benchmarks classes you may get compilation errors which
are related to the old code not being compatible with the new code. Some of the reasons are:
* Any plugin cannot delete the generated code (jmh generates code) because if the user configured
the directory as the same as source code the plugin will delete users source code.
* After you run jmh a gradle demon will stay alive which may cache the generated code in memory and 
generates the same code even if the files were changed.

Run this commands to clean the Gradle's cache:
```bash
./gradlew --stop
rm -fr .gradle/
rm -fr benchmarks/build
```