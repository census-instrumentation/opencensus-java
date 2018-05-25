# OpenCensus Stackdriver Log Correlation

This subproject is currently experimental. It provides a Stackdriver Logging LoggingEnhancer that
automatically adds tracing data to log entries. The LoggingEnhancer adds the trace ID, which allows
Stackdriver to display log entries associated with each trace or filter logs based on trace ID. It
currently also adds the span ID and sampling decision.
