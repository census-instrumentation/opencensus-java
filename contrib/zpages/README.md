# OpenCensus Z-Pages
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Z-Pages for Java* is a collection of HTML pages to display stats and trace data and
allows library configuration control.

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.12.2</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-zpages</artifactId>
    <version>0.12.2</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.12.2</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.12.2'
compile 'io.opencensus:opencensus-contrib-zpages:0.12.2'
runtime 'io.opencensus:opencensus-impl:0.12.2'
```

### Register the Z-Pages

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    ZPageHandlers.startHttpServerAndRegisterAll(8080);
    // ... do work
  }
}
```

### View stats and spans on Z-Pages

#### View RPC stats on /rpcz page

The /rpcz page displays the canonical RPC stats broken down by RPC methods. Example:

![rpcz-example](https://user-images.githubusercontent.com/10536136/37544216-74f04012-2921-11e8-8fa2-3d5d00edd400.png)

#### View measures and stats for all exported views on /statsz page

The /statsz page displays measures and stats for all exported views. Views are grouped into directories 
according to their namespace. Example:

![statsz-example-1](https://user-images.githubusercontent.com/10536136/37544231-7ec913ac-2921-11e8-938f-4457f6bffefb.png)
![statsz-example-2](https://user-images.githubusercontent.com/10536136/37544240-84621f20-2921-11e8-8758-839655273a41.png)

#### View trace spans on /tracez page

The /tracez page displays information about all active spans and all sampled spans based on latency 
and errors. Example:

![tracez-example](https://user-images.githubusercontent.com/10536136/37544247-8a044e94-2921-11e8-8b14-ef73bf7f3046.png)

#### View and update tracing configuration on /traceconfigz page

The /traceconfigz page displays information about the current active tracing configuration and 
allows users to change it. Example:

![traceconfigz-example](https://user-images.githubusercontent.com/10536136/37544258-8f3e3816-2921-11e8-9469-b6f97a516a28.png)


### FAQ

#### Why do I not see sampled spans based on latency and error codes for a given span name?
Sampled spans based on latency and error codes are available only for registered span names. 
For more details see [SampledSpanStore][sampledspanstore-url].

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-zpages/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-zpages
[sampledspanstore-url]: https://github.com/census-instrumentation/opencensus-java/blob/master/api/src/main/java/io/opencensus/trace/export/SampledSpanStore.java
