# OpenCensus Jetty Plugin
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Http Servlet Plugin for Java* is a plugin for trace instrumentation when using HTTP Servlet 3.0

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
    <artifactId>opencensus-contrib-http-servlet</artifactId>
    <version>0.19.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.19.0'
compile 'io.opencensus:opencensus-contrib-http-servlet:0.19.0'
```

## Instrumenting HTTP Servlets

Users can instrument Http Servlets by simply adding OcHttpServletFilter.class to your servlet filter.

Below is an example of how a servlet is instrumented.

```java

import io.opencensus.contrib.http.servlet.OcHttpServletFilter;
import io.opencensus.contrib.http.util.HttpViews;
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.opencensus.exporter.trace.logging.LoggingTraceExporter;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

public class HelloWorldServer extends AbstractHandler {

  public static class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      PrintWriter pout = response.getWriter();

      pout.print("<html><body><h3>Hello Servlet Get</h3></body></html>");
      return;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

      PrintWriter pout = response.getWriter();
      pout.print("<html><body><h3>Hello Servlet Post</h3></body></html>");
      return;
    }
  }

  public void handle(
      String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("text/html;charset=utf-8");
    response.setStatus(HttpServletResponse.SC_OK);
    baseRequest.setHandled(true);
    response.getWriter().println("<h1>Hello World. default handle</h1>");
  }

  private static void initStatsExporter() throws IOException {
    HttpViews.registerAllServerViews();

    // Register stats exporter to a desired backend.
    PrometheusStatsCollector.createAndRegister();
    HTTPServer prometheusServer = new HTTPServer(9090, true);
  }

  private static void initTracing() {
    // Register trace exporter to a desired backend.
    LoggingTraceExporter.register();
  }

  public static void main(String[] args) throws Exception {
    initTracing();
    initStatsExporter();
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    Server server = new Server(8080);
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);

    // Add OcHttpServletFilter to enable tracing and monitoring stats.
    handler.addFilterWithMapping(
        OcHttpServletFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    handler.addServletWithMapping(HelloServlet.class, "/*");

    server.start();
    server.join();
  }
}
```

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-http-servlet/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-http-servlet
