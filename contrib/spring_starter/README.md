# OpenCensus Spring Starter
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Spring Starter for Java* is a starter package that includes
packages required to enable tracing using opencensus when working with [Spring Web][spring-web-url].

This version is compatible with [Spring Boot 2.0][spring-boot-2.0-url].

## Servlet and AsyncRestTemplate Tracing

Enable tracing on RestController (server side) and AysncRestTemplate (client side) by simply including opencensus-contrib-spring-starter in your dependencies and
initializing exporter. It automatically traces your http request and collects stats associated with the
request.

It does require to register exporter and views.

### Depedencies

#### Maven

```xml
<dependencies>
    <dependency>
        <groupId>io.opencensus</groupId>
        <artifactId>opencensus-contrib-spring-starter</artifactId>
        <version>0.23.0</version>
    </dependency>
</dependencies>

```

#### Gradle
```gradle
dependencyManagement {
    imports {
        mavenBom "io.opencensus:opencensus-contrib-spring-starter:0.23.0"
    }
}
dependencies {
	compile 'io.opencensus:opencensus-contrib-spring:0.23.0'
}
```

### Tracing Properties

Optionally configure one or more Tracing Properties in application.properties file.


#### PublicEndpoint

If a servlet is serving publicEndpoints (untrusted boundary) then set this property to `true`. 
When set to true incoming trace context is added as a parent link instead of as a parent.
By default it is set to `false`. When set to `false` it uses incoming trace context as a parent.

```
opencensus.spring.trace.publicEndpoint = true
```

#### Propagation

opencensus.spring.trace.propagation = TRACE_PROPAGATION_B3

By default it is set to TRACE_PROPAGATION_TRACE_CONTEXT which uses [W3C Tracecontext](https://github.com/census-instrumentation/opencensus-java/blob/master/api/src/main/java/io/opencensus/trace/propagation/TextFormat.java)
propagator to propagate trace context. To use [B3 format](https://github.com/census-instrumentation/opencensus-java/blob/master/impl_core/src/main/java/io/opencensus/implcore/trace/propagation/B3Format.java) 
set the property to TRACE_PROPAGATION_B3

```
opencensus.spring.trace.propagation = TRACE_PROPAGATION_B3
```

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring

#### Java Versions

Java 6 or above is required for using this artifact.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring-starter/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring-starter
[spring-boot-2.0-url]: https://github.com/spring-projects/spring-boot/tree/2.0.x
[spring-web-url]: https://github.com/spring-projects/spring-framework/tree/master/spring-web
