# OpenCensus Stackdriver Log Correlation Demo

An application that demonstrates log correlation in Stackdriver, using
`opencensus-contrib-log-correlation-stackdriver`. The application contains SLF4J log statements and
OpenCensus tracing instrumentation. It configures logging with a Logback XML configuration and
exports logs using the
[Stackdriver Logging Logback appender](https://cloud.google.com/logging/docs/setup/java#logback_appender).
It also exports traces using `opencensus-exporter-trace-stackdriver`, so that Stackdriver can show
the log entries associated with each trace.
