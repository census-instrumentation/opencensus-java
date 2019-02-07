# OpenCensus JAX-RS
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus JAX-RS for Java* is a container and client filter  for trace instrumentation when using JAX-RS for REST implementation in Java.

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.19.2</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-http-jaxrs</artifactId>
    <version>0.19.2</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.19.2'
compile 'io.opencensus:opencensus-contrib-http-jaxrs:0.19.2'
```

### Usage

#### Container Filter

The container filter should be added to the JAX-RS `Application` class and endpoints should be annotated
with `@Metrics` annotation.

```java
class MyApplication extends Application {
  @Override
  public Set<Class<?>> getClasses() {
      Set<Class<?>> providers = new HashSet<>(super.getClasses());
      providers.add(JaxrsContainerFilter.class);
      return providers;
  }
}
```

It is possible to customize the filter by using the custom constructor. The below will
use the `B3Format` for context propagation instead of the W3C text context format.

```java
class MyApplication extends Application {
  @Override
  public Set<Object> getSingletons() {
    Set<Object> singletons = new HashSet<>(super.getSingletons());
    singletons.add(new JaxrsContainerFilter(
        new JaxrsContainerExtractor(),
        Tracing.getPropagationComponent().getB3Format(),
        /* publicEndpoint= */ true));
    return singletons;
  }
}
```

```java
@Metrics
@Path("/resource")
class MyResource {
  @GET
  public Response resource() {
    ...
  }
}
```

The annotation may also be applied on method level.

#### Client Filter

Filter should be added to the `WebTarget` instance when using JAX-RS as client.

```java
WebTarget target = ClientBuilder.newClient().target("endpoint");
target.register(JaxrsClientFilter.class);
```

It is possible to customize the filter using the custom constructor. The
below will use the `B3Format` for context propagation instead of the default W3C
text context format.

```java
WebTarget target = ClientBuilder.newClient().target("endpoint");
target.register(new JaxrsClientFilter(
    new JaxrsContainerExtractor(),
    Tracing.getPropagationComponent().getB3Format()));
```


[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-http-jetty-client/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-jetty-client
