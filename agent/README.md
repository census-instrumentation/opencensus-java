# OpenCensus Agent for Java

[![Build Status][travis-image]][travis-url] [![Maven Central][maven-image]][maven-url]

The *OpenCensus Agent for Java* collects and sends latency data about your Java process to
OpenCensus backends such as Stackdriver Trace for analysis and visualization.


## Design Ideas

We see tracing as a cross-cutting concern which the *OpenCensus Agent for Java* weaves into
existing Java bytecode (the application and its libraries) at runtime, typically when first loading
the concerned bytecode.

This approach allows us to instrument arbitrary code without having to touch the source code of the
application or its dependencies. Furthermore, we don't require the application owner to upgrade any
of the application's third-party dependencies to specific versions. As long as the interface (e.g.
[java.sql.Driver#connect](https://docs.oracle.com/javase/8/docs/api/java/sql/Driver.html#connect-java.lang.String-java.util.Properties-))
stays as-is across the supported versions, the Java agent's bytecode weaver will be able to
instrument the code.

The *OpenCensus Agent for Java* uses [Byte Buddy](http://bytebuddy.net/), a widely used and
well-maintained bytecode manipulation library, for instrumenting selected Java methods at class
load-time. Which Java methods we want to intercept/instrument obviously depends on the library
(MongoDB vs. Redis, etc.) and the application.


## Features

The *OpenCensus Agent for Java* is in an early development stage. The following features will be
implemented in the initial version:


### Propagation of the trace context between threads

The *OpenCensus Agent for Java* instruments
[java.lang.Thread#start](http://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#start--)
and implementations of
[java.util.concurrent.Executor#execute](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html#execute-java.lang.Runnable-)
for automatic propagation of the caller thread's
[io.grpc.Context](http://www.grpc.io/grpc-java/javadoc/io/grpc/Context.html) to the thread executing
the passed code.

For implementation details, please refer to
[ThreadInstrumentation.java](src/main/java/io/opencensus/agent/ThreadInstrumentation.java)
and
[ExecutorInstrumentation.java](src/main/java/io/opencensus/agent/ExecutorInstrumentation.java).


## Installation

To enable the *OpenCensus Agent for Java* for your application, add the option
`-javaagent:path/to/opencensus-agent.jar` to the invocation of the `java` executable as shown in
the following example:

```shell
java -javaagent:path/to/opencensus-agent.jar ...
```


## Work in progress

The following features are work in progress.


### Configuration

The *OpenCensus Agent for Java* reads its configuration from
a file, which is searched for in the following locations (in order):

1. The pathname given in the `io.opencensus.agent.config` system property.
2. `opencensus-agent.conf` in the current working directory.
3. `opencensus-agent.conf` in the same directory as the agent's JAR file.
4. `/io/opencensus/agent/opencensus-agent.conf` bundled with the agent's
   JAR file.

Please refer to the configuration file bundled with the agent's JAR file for
the available configuration knobs and the default settings.


### Generic tracing instrumentation.

The *OpenCensus Agent for Java* allows for configuring and extending the set of Java methods that
will be instrumented. The configuration is loaded when the agent starts up.

For example, the default configuration specifies that the following Java methods are to be
intercepted and instrumented: (exact format TBD, using globbing here only for illustration. In
practise, the fully qualified method name and the method signature may be just good enough, but more
verbose)

- java.sql.Driver#connect
- *HttpRequest#execute
- java.sql.Statement#execute*

The user-supplied configuration may request the instrumentation of additional, application-specific
methods, e.g.:

- io.opencensus.agent.example.*#main
- io.opencensus.agent.example.*#doStuff


### Propagation of the trace context between processes

Inter-process propagation of the trace context, e.g. HTTP client to server.
