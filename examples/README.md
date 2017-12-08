# OpenCensus Examples

## To build the examples use

### Gradle
```
$ ./gradlew installDist
```

### Maven
```
$ mvn package appassembler:assemble
```

## To run "StatsRunner" example use

### Gradle
```
$ ./build/install/examples/bin/StatsRunner
```

### Maven
```
$ ./target/appassembler/bin/StatsRunner
```

## To run "ZPagesTester"

### Gradle
```
$ ./build/install/examples/bin/ZPagesTester
```

### Maven
```
$ ./target/appassembler/bin/ZPagesTester
```

Available pages:
* For tracing page go to [localhost:8080/tracez][ZPagesTraceZLink]. 
* For tracing config page go to [localhost:8080/traceconfigz][ZPagesTraceConfigZLink].

[ZPagesTraceZLink]: http://localhost:8080/tracez
[ZPagesTraceConfigZLink]: http://localhost:8080/traceconfigz
