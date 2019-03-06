# OpenCensus Examples

## To build the examples use

### Gradle
```bash
./gradlew installDist
```

### Maven
```bash
mvn package appassembler:assemble
```

## To run "TagContextExample" use

### Gradle
```bash
./build/install/opencensus-examples/bin/TagContextExample
```

### Maven
```bash
./target/appassembler/bin/TagContextExample
```

## To run "ZPagesTester"

### Gradle
```bash
./build/install/opencensus-examples/bin/ZPagesTester
```

### Maven
```bash
./target/appassembler/bin/ZPagesTester
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
```bash
./build/install/opencensus-examples/bin/QuickStart
```

### Maven
```bash
./target/appassembler/bin/QuickStart
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
```bash
./build/install/opencensus-examples/bin/HelloWorldServer serverPort cloudProjectId zPagePort prometheusPort
./build/install/opencensus-examples/bin/HelloWorldClient user host serverPort cloudProjectId zPagePort
```

### Maven
```bash
./target/appassembler/bin/HelloWorldServer serverPort cloudProjectId zPagePort prometheusPort
./target/appassembler/bin/HelloWorldClient user host serverPort cloudProjectId zPagePort
```

## To run "Repl" example

See the full tutorial on [OpenCensus website](https://opencensus.io/quickstart/java/metrics/).

First run:

### Gradle
```bash
./build/install/opencensus-examples/bin/Repl
```

### Maven
```bash
./target/appassembler/bin/Repl
```

Then start the Prometheus process:
```bash
cd src/main/java/io/opencensus/examples/quickstart/
prometheus --config.file=prometheus.yaml
```

Stats will be shown on Prometheus UI on http://localhost:9090.

## To run "StackdriverQuickstart" use

See the full tutorial on [OpenCensus website](https://opencensus.io/guides/exporters/supported-exporters/java/stackdriver/).

### Gradle
```bash
./build/install/opencensus-examples/bin/StackdriverQuickstart
```

### Maven
```
./target/appassembler/bin/StackdriverQuickstart
```

## To run HTTP Server and Client

`HttpJettyServer` is a web service using Jetty Server on top of http-servlet.
`HttpJettyClient` is a web client using Jetty Client that sends request to `HttpettyServer`.
Both `HttpJettyServer` and `HttpJettyClient` are instrumented with OpenCensus.

Traces from both client and server can be viewed in their respective logs on console.
Stats are available from Prometheus server running at
- http://localhost:9091/metrics - for client stats
- http://localhost:9090/metrics - for server stats
  

### Gradle
```bash
./build/install/opencensus-examples/bin/HttpJettyServer
./build/install/opencensus-examples/bin/HttpJettyClient
```

### Maven
```bash
./target/appassembler/bin/HttpJettyServer
./target/appassembler/bin/HttpJettyClient
```

## To run OcAgentExportersQuickStart

### Gradle
```bash
./build/install/opencensus-examples/bin/OcAgentExportersQuickStart agentEndpoint # default is localhost:56678
```

### Maven
```bash
./target/appassembler/bin/OcAgentExportersQuickStart agentEndpoint # default is localhost:56678
```

You also need to install and start OpenCensus-Agent in order to receive the traces and metrics.
For more information on setting up Agent, see [tutorial](https://opencensus.io/agent/).
