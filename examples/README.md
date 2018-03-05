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

### Bazel
```
$ bazel build :all
```

## To run "StatsRunner" example use

### Gradle
```
$ ./build/install/opencensus-examples/bin/StatsRunner
```

### Maven
```
$ ./target/appassembler/bin/StatsRunner
```

### Bazel
```
$ ./bazel-bin/StatsRunner
```

## To run "ZPagesTester"

### Gradle
```
$ ./build/install/opencensus-examples/bin/ZPagesTester
```

### Maven
```
$ ./target/appassembler/bin/ZPagesTester
```

### Bazel
```
$ ./bazel-bin/ZPagesTester
```

Available pages:
* For tracing page go to [localhost:8080/tracez][ZPagesTraceZLink]. 
* For tracing config page go to [localhost:8080/traceconfigz][ZPagesTraceConfigZLink].
* For RPC stats page go to [localhost:8080/rpcz][ZPagesRpcZLink].
* For stats and measures on all registered views go to [localhost:8080/statsz][ZPagesStatsZLink].

[ZPagesTraceZLink]: http://localhost:8080/tracez
[ZPagesTraceConfigZLink]: http://localhost:8080/traceconfigz
[ZPagesRpcZLink]: http://localhost:8080/rpcz
[ZPagesStatsZLink]: http://localhost:8080/statsz

## To run "QuickStart" example use

### Gradle
```
$ ./build/install/opencensus-examples/bin/QuickStart
```

### Maven
```
$ ./target/appassembler/bin/QuickStart
```

### Bazel
```
$ ./bazel-bin/QuickStart
```
