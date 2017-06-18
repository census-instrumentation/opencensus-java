# OpenCensus Agent for Java

[![Build Status](https://travis-ci.org/census-instrumentation/instrumentation-java.svg?branch=master)](https://travis-ci.org/census-instrumentation/instrumentation-java) [![Build status](https://ci.appveyor.com/api/projects/status/v5dbthkucuewsu33/branch/master?svg=true)](https://ci.appveyor.com/project/instrumentationjavateam/instrumentation-java/branch/master)

The *OpenCensus Agent for Java* collects and sends latency data about your Java process to
OpenCensus backends such as Zipkin, Stackdriver Trace, etc. for analysis and visualization.


## Features

The *OpenCensus Agent for Java* is in an early development stage. The following features are
currently implemented:

TODO(stschmidt): Update README.md along with implementation.


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


## Installation

To enable the *OpenCensus Agent for Java* for your application, add the option
`-javaagent:path/to/opencensus-agent.jar` to the invocation of the `java` executable as shown in
the following example:

```shell
java -javaagent:path/to/opencensus-agent.jar ...
```
