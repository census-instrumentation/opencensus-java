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
    <version>0.16.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-spring</artifactId>
    <version>0.16.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.16.0</version>
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
compile 'io.opencensus:opencensus-api:c'
compile 'io.opencensus:opencensus-contrib-spring:0.16.0'
runtime 'io.opencensus:opencensus-impl:0.16.0'
runtime 'org.springframework:spring-aspects:SPRING_VERSION'
```

### Features

#### Traced Annotation

The `opencensus-contrib-spring` package provides support for a `@Traced` annotation 
that can be applied to methods.  When applied, the method will be wrapped in a 
Span, [https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/Span.md](https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/Span.md)

If the method throws an exception, the `Span` will be marked with a status of `Status.UNKNOWN`
and the stack trace will be added to the span as an annotation.

To enable the `@Traced` annotation, include the `CensusSpringAspect` bean.

```xml
  <!-- traces explicit calls to Traced -->
  <bean id="censusAspect" class="io.opencensus.contrib.spring.aop.CensusSpringAspect">
    <constructor-arg ref="tracer"/>
  </bean>
```

#### Database Support

The `opencensus-contrib-spring` package also includes support for tracing database
calls.  When database support is included, all calls to `java.sql.PreparedStatement.execute*`
will be wrapped in a Span in the same way that `@Traced` wraps methods.

To enable database support, include the `CensusSpringSqlAspect` bean.

```xml
  <!-- traces all SQL calls -->
  <bean id="censusSQLAspect" class="io.opencensus.contrib.spring.aop.CensusSpringSqlAspect">
    <constructor-arg ref="tracer"/>
  </bean>
```

#### Complete Spring XML configuration

The following contains a complete spring xml file to configure `opencensus-contrib-spring` 
with support for both `@Traced` and database connection tracing.

**Note:** This example does not include the configuration of any exporters. That will 
need to be done separately.

**TBD:*** Include examples of spring with exporters.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.2.xsd http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">

  <aop:aspectj-autoproxy/>

  <!-- traces explicit calls to Traced -->
  <bean id="censusAspect" class="io.opencensus.contrib.spring.aop.CensusSpringAspect">
    <constructor-arg ref="tracer"/>
  </bean>

  <!-- traces all SQL calls -->
  <bean id="censusSQLAspect" class="io.opencensus.contrib.spring.aop.CensusSpringSqlAspect">
    <constructor-arg ref="tracer"/>
  </bean>

  <!-- global tracer -->
  <bean id="tracer" class="io.opencensus.trace.Tracing" factory-method="getTracer"/>
</beans>
```

### Traced Usage 

Once configured, you can use the `@Traced` annotation to indicate that a method should 
be wrapped with a `Span`.  By default, `@Traced` will use the name of the method as the
span name.  However, `@Traced` supports an optional name attribute to allow a custom
span name to be specified.

```java
  @Traced()
  void example1() {
    // do work
  }
  
  // a custom span name can also be provided to Traced
  @Traced(name = "custom-span-name")
  void example2() {
    // do moar work
  }
```

#### Notes

`opencensus-contrib-spring` support only enables annotations.  You will still need to configure opencensus and register exporters / views.

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-spring
