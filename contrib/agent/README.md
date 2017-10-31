# OpenCensus Agent for Java

[![Build Status][travis-image]][travis-url] [![Build status][appveyor-image]][appveyor-url] [![Maven Central][maven-image]][maven-url]

The *OpenCensus Agent for Java* collects and sends latency data about your Java process to
OpenCensus backends such as Zipkin, Stackdriver Trace, etc. for analysis and visualization.


## Features

The *OpenCensus Agent for Java* is in an early development stage. The following features are
currently implemented:

TODO(stschmidt): Update README.md along with implementation.


### Automatic context propagation for Executors

The context of the caller of [Executor#execute](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html#execute-java.lang.Runnable-)
is automatically propagated to the submitted Runnable.


### Automatic context propagation for Threads

The context of the caller of [Thread#start](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#start--)
is automatically propagated to the new thread.


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


## Installation and Usage

Download the latest version of the *OpenCensus Agent for Java* `.jar` file
from [Maven Central][maven-url]. Store it somewhere on disk.

To enable the *OpenCensus Agent for Java* for your application, add the option
`-javaagent:path/to/opencensus-contrib-agent-X.Y.Z.jar` to the invocation of the `java`
executable as shown in the following example. Replace `X.Y.Z` with the actual version number.

```shell
java -javaagent:path/to/opencensus-contrib-agent-X.Y.Z.jar ...
```


## Configuration

The *OpenCensus Agent for Java* uses [Typesafe's configuration
library](https://github.com/lightbend/config) for all user-configurable settings. Please refer to
[reference.conf](src/main/resources/reference.conf) for the available configuration knobs and their
defaults.

You can override the default configuration in [different
ways](https://github.com/lightbend/config#standard-behavior). For example, to disable the automatic
context propagation for Executors, add a system property as follows:

```shell
java -javaagent:path/to/opencensus-contrib-agent-X.Y.Z.jar \
     -Dopencensus.contrib.agent.context-propagation.executor=false \
     ...
```


[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/instrumentationjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-agent/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-agent
