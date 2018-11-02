# OpenCensus HTTP Util
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus HTTP Util for Java* is a collection of utilities for trace instrumentation when
working with HTTP.

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.13.2</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-http-util</artifactId>
    <version>0.13.2</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.13.2'
compile 'io.opencensus:opencensus-contrib-http-util:0.13.2'
```

## Instrumenting HTTP libraries/frameworks

### customization for libraries/frameworks

Users can implement `HttpExtractor` to customize what information are extracted from the HTTP
request/response entity.

If context propagation is enabled, users need to provide framework specific `TextFormat.Setter`
and `TextFormat.Getter`. They are used to inject/extract information into/from the `Carrier` of
the request. The `Carrier` can be the request itself or other objects, as long as it provides
functionalities of setting/getting HTTP attributes.

TBD: Add a link to Extractor usage in Jetty HttpClient instrumentation here.

### client

Users can create a `HttpClientHandler` to help instrument client-side HTTP request/response.

TBD: Add a link to Jetty Http Client insturmentation.

### server

Users can create a `HttpServerHandler` to help instrument server-side HTTP request/response.

TBD: Add a link to Http Servlet 3.0 instrumentation here.


### handling async calls

In asynchronous HTTP calls, message receiving and sending may happen in different
threads. Users need to ensure the started span (as well as scope, if any) is
closed or ended no matter the call is successful or not.

To do that, store current scope and span somewhere, e.g. the context of the channel,
and close them before the channel exits.

TBD: Add a link to Http Servlet 3.0 instrumentation here.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-http-util/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-http-util
