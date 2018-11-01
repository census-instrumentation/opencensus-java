# OpenCensus Stackdriver Log Correlation

The `opencensus-contrib-log-correlation-stackdriver` artifact provides a
[Stackdriver Logging](https://cloud.google.com/logging/)
[`LoggingEnhancer`](http://googlecloudplatform.github.io/google-cloud-java/google-cloud-clients/apidocs/com/google/cloud/logging/LoggingEnhancer.html)
that automatically adds tracing data to log entries. The class name is
`OpenCensusTraceLoggingEnhancer`. `OpenCensusTraceLoggingEnhancer` adds the current trace and span
ID to each log entry, which allows Stackdriver to display the log entries associated with each
trace, or filter logs based on trace or span ID. It currently also adds the sampling decision using
the label "`opencensusTraceSampled`".

## Instructions

### Prerequisites

This log correlation feature requires a project that is using the
[`com.google.cloud:google-cloud-logging`](https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-clients/google-cloud-logging)
library to export logs to Stackdriver. `google-cloud-logging` must be version `1.33.0` or later.
The application can run on Google Cloud Platform, on-premise, or on
another cloud platform. See https://cloud.google.com/logging/docs/setup/java for instructions for
setting up `google-cloud-logging`.

**Note that this artifact does not support logging done through the Stackdriver Logging agent.**

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-log-correlation-stackdriver</artifactId>
    <version>0.16.1</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
runtime 'io.opencensus:opencensus-contrib-log-correlation-stackdriver:0.16.1'
```

### Configure the `OpenCensusTraceLoggingEnhancer`

#### Setting the project ID

By default, `OpenCensusTraceLoggingEnhancer` looks up the project ID from `google-cloud-java`. See
[here](https://github.com/GoogleCloudPlatform/google-cloud-java#specifying-a-project-id) for
instructions for configuring the project ID with `google-cloud-java`.

To override the project ID, set the following property as a system property or as a
`java.util.logging` property:

`io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer.projectId`

Other aspects of configuring the `OpenCensusTraceLoggingEnhancer` depend on the logging
implementation and `google-cloud-logging` adapter in use.

#### Logback with `google-cloud-logging-logback` `LoggingAppender`

The `LoggingAppender` should already be configured in `logback.xml` as described in
https://cloud.google.com/logging/docs/setup/java#logback_appender. Add
"`io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer`" to the list of
enhancers. Optionally, set the `projectId` property described above as a system property.

Here is an example `logback.xml`, based on the
[`google-cloud-logging-logback` example](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/a2b04b20d81ee631439a9368fb99b44849519e28/logging/logback/src/main/resources/logback.xml).
It specifies the `LoggingEnhancer` class and sets the optional project ID property:

```xml
<configuration>
  <property scope="system" name="io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer.projectId" value="my-project-id" />
  <appender name="CLOUD" class="com.google.cloud.logging.logback.LoggingAppender">
    <enhancer>io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer</enhancer>
  </appender>

  <root level="info">
    <appender-ref ref="CLOUD" />
  </root>
</configuration>
```

See
https://github.com/census-ecosystem/opencensus-experiments/tree/master/java/log_correlation/stackdriver/logback
for a full example.

#### `java.util.logging` with `google-cloud-logging` `LoggingHandler`

The `LoggingHandler` should already be configured in a logging `.properties` file, as described in
https://cloud.google.com/logging/docs/setup/java#jul_handler. Add
"`io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer`" to the list of
enhancers. Optionally, set the `projectId` property described above in the properties file.

Here is an example `.properties` file, based on the
[`google-cloud-logging` example](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/a2b04b20d81ee631439a9368fb99b44849519e28/logging/jul/src/main/resources/logging.properties).
It specifies the `LoggingEnhancer` class and sets the optional project ID property:

```properties
.level = INFO

com.example.MyClass.handlers=com.google.cloud.logging.LoggingHandler

com.google.cloud.logging.LoggingHandler.enhancers=io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer
io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer.projectId=my-project-id
```

See
https://github.com/census-ecosystem/opencensus-experiments/tree/master/java/log_correlation/stackdriver/java_util_logging
for a full example.

#### Custom `google-cloud-logging` adapter

The `google-cloud-logging` adapter needs to instantiate the `OpenCensusTraceLoggingEnhancer`,
possibly by looking up the class name of the `LoggingEnhancer` in a configuration file and
instantiating it with reflection. Then the adapter needs to call the `LoggingEnhancer`'s
`enhanceLogEntry` method on all `LogEntry`s that will be passed to `google-cloud-logging`'s
`Logging.write` method. `enhanceLogEntry` must be called in the same thread that executed the log
statement, in order to provide the current trace and span ID.

#### Java Versions

Java 7 or above is required for using this artifact.
