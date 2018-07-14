# spring
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

Provides annotation support for projects that use Spring.  

## Quickstart

### Add the dependencies to your project.

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <!-- census -->
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.15.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-spring</artifactId>
    <version>0.15.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.15.0</version>
    <scope>runtime</scope>
  </dependency>
  
  <!-- spring aspects -->
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
    <version>SPRING_VERSION</version>
    <scope>runtime</scope>
  </dependency>
  
</dependencies>
```

For Gradle add to your dependencies:
```gradle
compile 'io.opencensus:opencensus-api:0.15.0'
compile 'io.opencensus:opencensus-contrib-spring:0.15.0'
runtime 'io.opencensus:opencensus-impl:0.15.0'
runtime 'org.springframework:spring-aspects:SPRING_VERSION'
```

### Configure Spring

To configure annotation support within Spring, include the following with your
spring xml configuration.

```xml
  <!-- Enable @AspectJ annotation support  -->
  <aop:aspectj-autoproxy/>

  <!-- traces explicit calls to @Trace -->
  <bean id="censusAspect" class="io.opencensus.contrib.spring.aop.CensusSpringAspect"/>

  <!-- traces all SQL calls e.g. New Relic -->
  <bean id="censusSQLAspect" class="io.opencensus.contrib.spring.aop.CensusSpringSQLAspect"/>
```

### Usage 

Once configured, you can use the `@Trace` annotation to indicate that a method should be traces.

```java
  @Trace()
  void example1() {
    // do work
  }
  
  // a custom span name can also be provided to @Trace  
  @Trace(name = "custom-label")
  void example2() {
    // do moar work
  }
```

#### Notes

Spring support only enables annotations.  You'll still need to configure opencensus and register exporters / views.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring
