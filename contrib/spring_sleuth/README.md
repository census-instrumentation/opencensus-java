# OpenCensus Spring Sleuth
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Spring Sleuth for Java* is a library for automatically
propagating the OpenCensus trace context when working with [Spring Sleuth][spring-sleuth-url].

This is an __experimental component__, please bring feedback to
https://gitter.im/census-instrumentation/Lobby not the usual
sleuth channel https://gitter.im/spring-cloud/spring-cloud-sleuth 

This version is compatible with [Spring Boot 1.5.x][spring-boot-1.5-url].

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-spring-sleuth</artifactId>
    <version>0.15.0</version>
    <exclusions>
      <exclusion>
	    <groupId>org.springframework.cloud</groupId>
	    <artifactId>spring-cloud-build</artifactId>
	  </exclusion>
	  <exclusion>
	    <groupId>org.springframework.cloud</groupId>
	    <artifactId>spring-cloud-starter-sleuth</artifactId>
   	  </exclusion>
    </exclusions>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-contrib-spring-sleuth:0.15.0'
```

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring-sleuth/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring-sleuth
[sprint-boot-1.5-url]: https://github.com/spring-projects/spring-boot/tree/1.5.x
[spring-sleuth-url]: https://github.com/spring-cloud/spring-cloud-sleuth
