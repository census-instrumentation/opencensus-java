# OpenCensus Stackdriver Log Correlation

This subproject is currently experimental. It provides a Stackdriver Logging
[LoggingEnhancer](http://googlecloudplatform.github.io/google-cloud-java/google-cloud-clients/apidocs/com/google/cloud/logging/LoggingEnhancer.html)
that automatically adds tracing data to log entries. The LoggingEnhancer adds the trace ID, which
allows Stackdriver to display log entries associated with each trace or filter logs based on trace
ID. It currently also adds the span ID and sampling decision.

TODO(sebright): Add a demo to https://github.com/census-ecosystem/opencensus-experiments and link to
it.
