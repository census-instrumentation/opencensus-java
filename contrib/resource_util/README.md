# OpenCensus Resources Util
[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]
[![Maven Central][maven-image]][maven-url]

The *OpenCensus Resource Util for Java* is a collection of utilities that defines a set of 
common resources (aws_ec2_instance, gcp_gce_instance, k8c_container, etc.) and offers auto detection
for some of the resources, based on the environment where the application is running.

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-resource-util</artifactId>
    <version>0.20.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-contrib-resource-util:0.20.0'
```

[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
[maven-image]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-resource-util/badge.svg
[maven-url]: https://maven-badges.herokuapp.com/maven-central/io.opencensus/opencensus-contrib-resource-util
