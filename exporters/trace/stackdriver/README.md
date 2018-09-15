# OpenCensus Stackdriver Trace Exporter
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Stackdriver Trace Exporter* is a trace exporter that exports data to 
Stackdriver Trace. [Stackdriver Trace][stackdriver-trace] is a distributed 
tracing system that collects latency data from your applications and displays it in the Google 
Cloud Platform Console. You can track how requests propagate through your application and receive
detailed near real-time performance insights.

## Quickstart

### Prerequisites

To use this exporter, you must have an application that you'd like to trace. The app can be on 
Google Cloud Platform, on-premise, or another cloud platform.

In order to be able to push your traces to [Stackdriver Trace][stackdriver-trace], you must:

1. [Create a Cloud project](https://support.google.com/cloud/answer/6251787?hl=en).
2. [Enable billing](https://support.google.com/cloud/answer/6288653#new-billing).
3. [Enable the Stackdriver Trace API](https://console.cloud.google.com/apis/api/cloudtrace.googleapis.com/overview).

These steps enable the API but don't require that your app is hosted on Google Cloud Platform.

### Hello "Stackdriver Trace"

#### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.16.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-stackdriver</artifactId>
    <version>0.16.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.16.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.16.0'
compile 'io.opencensus:opencensus-exporter-trace-stackdriver:0.16.0'
runtime 'io.opencensus:opencensus-impl:0.16.0'
```

#### Register the exporter

This uses the default configuration for authentication and project ID.

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    StackdriverTraceExporter.createAndRegister(
        StackdriverTraceConfiguration.builder().build());
    // ...
  }
}
```

#### Authentication

This exporter uses [google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java),
for details about how to configure the authentication see [here](https://github.com/GoogleCloudPlatform/google-cloud-java#authentication).

If you prefer to manually set the credentials use:
```
StackdriverTraceExporter.createAndRegisterWithCredentialsAndProjectId(
    new GoogleCredentials(new AccessToken(accessToken, expirationTime)),
    "MyStackdriverProjectId");
```

#### Specifying a Project ID

This exporter uses [google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java),
for details about how to configure the project ID see [here](https://github.com/GoogleCloudPlatform/google-cloud-java#specifying-a-project-id).

If you prefer to manually set the project ID use:
```
StackdriverTraceExporter.createAndRegisterWithProjectId("MyStackdriverProjectId");
```

#### Enable Stackdriver Trace API access scope on Google Cloud Platform
If your Stackdriver Trace Exporter is running on Kubernetes Engine or Compute Engine,
you might need additional setup to explicitly enable the ```trace.append``` Stackdriver 
Trace API access scope. To do that, please follow the instructions for 
[GKE](https://cloud.google.com/trace/docs/setup/java#kubernetes_engine) or 
[GCE](https://cloud.google.com/trace/docs/setup/java#compute_engine).

#### Java Versions

Java 7 or above is required for using this exporter.

## FAQ
### Why do I not see some trace events in Stackdriver?
In all the versions before '0.9.1' the Stackdriver Trace exporter was implemented using the [v1 
API][stackdriver-v1-api-url] which is not fully compatible with the OpenCensus data model. Trace 
events like Annotations and NetworkEvents will be dropped.

### Why do I get a "StatusRuntimeException: NOT_FOUND: Requested entity was not found"?
One of the possible reasons is you are using a project id with bad format for the exporter.
Please double check the project id associated with the Stackdriver Trace exporter first. 
Stackdriver Trace backend will not do any sanitization or trimming on the incoming project id.
Project id with leading or trailing spaces will be treated as a separate non-existing project
(e.g "project-id" vs "project-id "), and will cause a NOT_FOUND exception.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-stackdriver/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-exporter-trace-stackdriver
[stackdriver-trace]: https://cloud.google.com/trace/
[stackdriver-v1-api-url]: https://cloud.google.com/trace/docs/reference/v1/rpc/google.devtools.cloudtrace.v1#google.devtools.cloudtrace.v1.TraceSpan
