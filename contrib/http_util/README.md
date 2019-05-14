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
    <version>0.22.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-http-util</artifactId>
    <version>0.22.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.22.0'
compile 'io.opencensus:opencensus-contrib-http-util:0.22.0'
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

### Client

Users can create a `HttpClientHandler` to help instrument client-side HTTP request/response.

An example usage of the handler would be:

```java
HttpClientHandler<HttpRequest, HttpResponse, HttpRequest> handler =
    new HttpClientHandler<HttpRequest, HttpResponse>(
        tracer, extractor, myTextFormat, myTextFormatSetter);

// Use #handleStart in client to start a new span.
// Use `null` if you want to use current Span as the parent Span.
HttpRequestContext context = handler.handleStart(null, request, request);
HttpResponse response = null;
Throwable error = null;
try {
  // Do something to send the request, and get response code from the server
  response = getResponse(request);

  // Optionally, use #handleMessageSent in client to log a SENT event and its size.
  handler.handleMessageSent(context, request.getContentLength());

  // Optionally, use #handleMessageReceived in client to log a RECEIVED event and message size.
  handler.handleMessageReceived(context, response.getContentLength());
} catch (Throwable e) {
  error = e;
} finally {
  // Use #handleEnd in client to close the span.
  handler.handleEnd(context, request, response, error);
}
```

### Server

Users can create a `HttpServerHandler` to help instrument server-side HTTP request/response.

An example usage of the handler would be:

```java
HttpServerHandler<HttpRequest, HttpResponse> handler =
    new HttpServerHandler<HttpRequest, HttpResponse, HttpRequest>(
        tracer, extractor, myTextFormat, myTextFormatGetter,
        false /* true if it is public endpoint */);

// Use #handleStart in server to start a new span.
HttpRequestContext context = handler.handleStart(request, request);
HttpResponse response = constructResponse();
Throwable error = null;
try (Scope scope = tracer.withSpan(handler.getSpanFromContext(context))) {
  // Do something to decide whether to serve the request or early exit.
  // For example, client may expect a 100 Continue before sending the message body.
  if (extractor.getRequestSize(request) > REQUEST_LIMIT) {
    response.setStatus(413);
  } else {
    response.setStatus(100);
    String content = request.getContent();

    // Optionally, use #handleMessageReceived in server to log a RECEIVED event and its size.
    handler.handleMessageReceived(context, request.getContentLength());

    // Do something to prepare the response or exception.
    response.setStatus(201);
    response.write("OK");
    response.flush();

    // Optionally, use #handleMessageSent in server to log a SENT message event and its message size.
    handler.handleMessageSent(context, response.getContentLength());
  } catch (Throwable e) {
    error = e;
  } finally {
    // Use #handleEnd in server to close the span.
    handler.handleEnd(context, request, response, error);
  }
}
```

### handling async calls

In asynchronous HTTP calls, message receiving and sending may happen in different
threads. Users need to ensure the started span (as well as scope, if any) is
closed or ended no matter the call is successful or not.

To do that, store current scope and span somewhere, e.g. the context of the channel,
and close them before the channel exits.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-grpc-util/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-grpc-util
[grpc-url]: https://github.com/grpc/grpc-java
