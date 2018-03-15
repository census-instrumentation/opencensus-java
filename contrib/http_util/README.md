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

Below is an example of how the customization for libraries/frameworks should be done:

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

// use the HttpRequest itself as Carrier.
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
```

### customization for span behaviors

Users can implement `HttpSpanCustomizer` to customize how spans are created and what operations
need to be done upon starting and ending of the span. This class also provides a default mapping
from HTTP status code to OpenCensus span Status, and users can override to use their own parsing logic.

Users can enable context propagation by specifying a valid `TextFormat`, which is used for
context serialization/deserialization.

Below is an example of how the customization for span behaviors should be done:

```java
// Use B3Format for propagation. You can also use other formats provided in
// HttpPropagationUtil.
TextFormat myTextFormat = Tracing.getPropagationComponent().getB3Format();
HttpSpanCustomizer<HttpRequest, HttpResponse> customizer =
    new HttpSpanCustomizer<HttpRequest, HttpResponse> {
      @Override
      public String getSpanName(
          HttpRequest request,
          HttpExtractor<HttpRequest, HttpResponse> extractor) {
        // user-defined span name
        return extractor.getPath() + " " + extractor.getMethod(request);
      }

      @Override
      public SpanBuilder customizeSpanBuilder(
          HttpRequest request,
          SpanBuilder spanBuilder,
          HttpExtractor<HttpRequest, HttpResponse> extractor) {
        // do not sample API for heartbeat.
        if ("/heartbeat".equals(extractor.getPath())) {
          spanBuilder.setSampler(Samplers.neverSample());
        }
        // always record spans locally.
        return spanBuilder.setRecordEvents(true);
      }

      // other methods that need to be overridden.
      // ...
    };
// The OpenCensus tracer. You can mock it for testing.
Tracer tracer = Tracing.getTracer();
```

### client

Users can create a `HttpClientHandler` to help instrument client-side HTTP request/response.

An example usage of the handler would be:

```java
HttpClientHandler<HttpRequest, HttpResponse> handler =
    new HttpClientHandler<HttpRequest, HttpResponse>(tracer, myTextFormat, extractor, customizer);

// Use #handleStart in client to start a new span.
Span span = handler.handleStart(myTextFormatSetter, request, request);
HttpResponse response = null;
Throwable error = null;
try {
  // Do something to send the request, and get response code from the server
  int responseCode = request.getResponseCode();

  // Optionally, use #handleMessageSent in client to log a SENT event and its size.
  handler.handleMessageSent(span, sentId++, extractor.getRequestSize(request));

  // Do something to read the message body.
  response = request.getResponse();

  // Optionally, use #handleMessageReceived in client to log a RECEIVED event and message size.
  handler.handleMessageReceived(span, recvId++, extractor.getResponseSize(response));
} catch (Throwable e) {
  error = e;
} finally {
  // Use #handleEnd in client to close the span.
  handler.handleEnd(response, error, span);
}
```

### server

Users can create a `HttpServerHandler` to help instrument server-side HTTP request/response.

An example usage of the handler would be:

```java
HttpServerHandler<HttpRequest, HttpResponse> handler =
    new HttpServerHandler<HttpRequest, HttpResponse>(tracer, myTextFormat, extractor, customizer);

// Use #handleStart in server to start a new span.
Span span = handler.handleStart(myTextFormatGetter, request, request);
HttpResponse response = constructResponse();
Throwable error = null;
try (Scope scope = tracer.withSpan(span)) {
  // Do something to decide whether to serve the request or early exit.
  // For example, client may expect a 100 Continue before sending the message body.
  if (extractor.getRequestSize(request) > REQUEST_LIMIT) {
    response.setStatus(413);
  } else {
    response.setStatus(100);
    String content = request.getContent();

    // Optionally, use #handleMessageReceived in server to log a RECEIVED event and its size.
    handler.handleMessageReceived(span, recvId++, extractor.getRequestSize(request));

    // Do something to prepare the response or exception.
    response.setStatus(201);
    response.write("OK");
    response.flush();

    // Optionally, use #handleMessageSent in server to log a SENT message event and its message size.
    handler.handleMessageSent(span, sentId++, extractor.getResponseSize(response));

  } catch (Throwable e) {
    error = e;
  } finally {
    // Use #handleEnd in server to close the span.
    handler.handleEnd(response, error, span);
  }
}
```

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-grpc-util/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-grpc-util
[grpc-url]: https://github.com/grpc/grpc-java
