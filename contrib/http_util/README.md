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
    <version>0.17.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-http-util</artifactId>
    <version>0.17.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.17.0'
compile 'io.opencensus:opencensus-contrib-http-util:0.17.0'
```

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-grpc-util/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-grpc-util
[grpc-url]: https://github.com/grpc/grpc-java
