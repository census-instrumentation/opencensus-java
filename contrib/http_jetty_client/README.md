# OpenCensus Jetty HttpClient
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Jetty HttpClient for Java* is a wrapper for trace instrumentation when using Jetty as HTTP client.

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.19.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-http-jetty-client</artifactId>
    <version>0.19.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.19.0'
compile 'io.opencensus:opencensus-contrib-http-jetty-client:0.19.0'
```

## Instrumenting Jetty Http Client

Users can instrument Jetty Http Client by simply creating HttpClient using wrapper OcJettyHttpClient.

Below is an example of how it is instrumented.

```java

import io.opencensus.contrib.http.jetty.client.OcJettyHttpClient;
import io.opencensus.contrib.http.util.HttpViews;
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.opencensus.exporter.trace.logging.LoggingTraceExporter;


class Application {
  private static void initStatsExporter() throws IOException {
    
    HttpViews.registerAllClientViews();
    
    // Register stats exporter for desired backend.
    PrometheusStatsCollector.createAndRegister();
    HTTPServer prometheusServer = new HTTPServer(9091, true);
  }

  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();

    // Register trace exporter for desired backend. 
    LoggingTraceExporter.register();

    // Initialize Stats Exporter
    initStatsExporter();

    OcJettyHttpClient httpClient = new OcJettyHttpClient();

    httpClient.start();

    HttpRequest request =
        (HttpRequest) httpClient.newRequest("http://example.com/").method(HttpMethod.GET);
    request.send();

    HttpRequest postRequest =
        (HttpRequest) httpClient.newRequest("http://example.com/").method(HttpMethod.POST);
    postRequest.content(new StringContentProvider("{\"hello\": \"world\"}"), "application/json");
    postRequest.send();
  }
}
```

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-http-jetty-client/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-jetty-client
