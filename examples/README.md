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

## To run "gRPC Hello World" example use

Please note all the arguments are optional. If you do not specify these arguments, default values
will be used:

* host and serverPort will be "localhost:50051"
* user will be "world"
* cloudProjectId will be null (which means no stats/spans will be exported to Stackdriver)
* server zPagePort will be 3000
* client zPagePort will be 3001
* Prometheus port will be 9090


However, if you want to specify any of these arguements, please make sure they are in order.

### Gradle
```
$ ./build/install/opencensus-examples/bin/HelloWorldServer serverPort cloudProjectId zPagePort prometheusPort
$ ./build/install/opencensus-examples/bin/HelloWorldClient user host serverPort cloudProjectId zPagePort
```

### Maven
```
$ ./target/appassembler/bin/HelloWorldServer serverPort cloudProjectId zPagePort prometheusPort
$ ./target/appassembler/bin/HelloWorldClient user host serverPort cloudProjectId zPagePort
```

### Bazel
```
$ ./bazel-bin/HelloWorldServer serverPort cloudProjectId zPagePort prometheusPort
$ ./bazel-bin/HelloWorldClient user host serverPort cloudProjectId zPagePort
```
