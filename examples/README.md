# OpenCensus Examples

## To build the examples use

```
$ ./gradlew installDist
```

Note: If you are building a SNAPSHOT instead of a release, please install the opencensus-java main project to your local repo first:

```
$ cd .. && gradlew install
```

## To run "StatsRunner" example use

```
$ ./examples/build/install/examples/bin/StatsRunner
```

## To run "ZPagesTester"
```
$ ./examples/build/install/examples/bin/ZPagesTester
```

Available pages:
* For tracing page go to [localhost:8080/tracez][ZPagesTraceZLink]. 
* For tracing config page go to [localhost:8080/traceconfigz][ZPagesTraceConfigZLink].

[ZPagesTraceZLink]: http://localhost:8080/tracez
[ZPagesTraceConfigZLink]: http://localhost:8080/traceconfigz
