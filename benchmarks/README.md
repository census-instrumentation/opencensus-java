# OpenCensus Benchmarks

## To run the benchmark use

```
$ ./gradlew :opencensus-benchmarks:jmh
```

## To debug compilation errors
When you make incompatible changes in the Benchmarks classes you may get compilation errors which
are related to the old code not being compatible with the new code. Run this commands to clean 
the Gradle's cache:
```bash
./gradlew --stop
rm -fr .gradle/
rm -fr benchmarks/build
```