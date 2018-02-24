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

### customization

Users need to implement their own `HttpExtractor` to extract information from
the request/response entity defined in their libraries/frameworks.

Besides, users need to explicitly specify which `TextFormat` they want to use
in HTTP propagation, and provide framework specific `TextFormat.Setter` and
`TextFormat.Getter` to inject/extract information into/from the `Carrier` of the
request. The `Carrier` could be the request itself or other classes,
as long as it provides functionalities of setting/getting HTTP attributes.

Below is an example of how the customization should be done:

```java
// // Http request entity in the library/framework.
// public class HttpRequest {
//   ...
// }
//
// // Http response entity in the library/framework.
// public class HttpResponse {
//   ...
// }

TextFormat mytextFormat = HttpPropagationUtil.getCloudTraceFormat();
TextFormat.Setter<HttpRequest> myTextFormatSetter =
    new TextFormat.Setter<HttpRequest>() {
      @Override
      public void put(HttpRequest carrier, String key, String value) {
        carrier.setHeader(key, value);
      }
    };
TextFormat.Getter<HttpRequest> myTextFormatGetter =
    new TextFormat.Getter<HttpRequest>() {
      @Override
      public String get(HttpRequest carrier, String key) {
        return carrier.getHeader(key);
      }
    };
HttpExtractor<HttpRequest, HttpResponse> extractor =
    new HttpExtractor<HttpRequest, HttpResponse>() {
      @Override
      public Integer getStatusCode(HttpResponse response) {
        return response.getStatusCode();
      }

      // other methods that need to be overridden
      // ...
    };
Tracer tracer = Tracing.getTracer();
```

### client

Users can create a `HttpClientHandler` to help instrument client-side HTTP request/response.

An example usage of the handler would be:

```java
HttpClientHandler<HttpRequest, HttpResponse> handler =
    new HttpClientHandler<HttpRequest, HttpResponse>(tracer, myTextFormat, extractor);
Span span = handler.handleSend(myTextFormatSetter, request, request, "Client.send");
try (Scope scope = tracer.withSpan(span)) {
  Throwable error = null;
  HttpResponse response = null;
  // do something to send the request
  // do something to receive the response or handle error.
  // ...

  // handle the response, and optionally close current span.
  handler.handleRecv(response, error, span, true);
}
```

### server

Users can create a `HttpServerHandler` to help instrument server-side HTTP request/response.

An example usage of the handler would be:

```java
HttpServerHandler<HttpRequest, HttpResponse> handler =
    new HttpServerHandler<HttpRequest, HttpResponse>(tracer, myTextFormat, extractor);
Span span = handler.handleRecv(myTextFormatGetter, request, request, "Server.recv");
try (Scope scope = tracer.withSpan(span)) {
  Throwable error = null;
  HttpResponse response = null;
  // do something to receive the request
  // do something to prepare the response or exception.
  // ...

  // handle the response send, and optionally close current span.
  handler.handleSend(response, error, span, true);
}
```

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-grpc-util/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-grpc-util
[grpc-url]: https://github.com/grpc/grpc-java
