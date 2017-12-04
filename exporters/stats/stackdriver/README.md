# OpenCensus Stackdriver Stats Exporter

The *OpenCensus Stackdriver Stats Exporter* is a stats exporter that exports data to 
Stackdriver Monitoring. [Stackdriver Monitoring][stackdriver-monitoring] provides visibility into 
the performance, uptime, and overall health of cloud-powered applications. Stackdriver ingests that 
data and generates insights via dashboards, charts, and alerts.

## Quickstart

### Prerequisites

To use this exporter, you must have an application that you'd like to monitor. The app can be on 
Google Cloud Platform, on-premise, or another cloud platform.

In order to be able to push your stats to [Stackdriver Monitoring][stackdriver-monitoring], you must:

1. [Create a Cloud project](https://support.google.com/cloud/answer/6251787?hl=en).
2. [Enable billing](https://support.google.com/cloud/answer/6288653#new-billing).
3. [Enable the Stackdriver Monitoring API](https://app.google.stackdriver.com/).
4. [Make sure you have a Premium Stackdiver account](https://cloud.google.com/monitoring/accounts/tiers).

These steps enable the API but don't require that your app is hosted on Google Cloud Platform.

### Hello "Stackdriver Stats"

#### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.9.1</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-stats-stackdriver</artifactId>
    <version>0.9.1</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.9.1</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.9.1'
compile 'io.opencensus:opencensus-exporter-stats-stackdriver:0.9.1'
runtime 'io.opencensus:opencensus-impl:0.9.1'
```

#### Register the exporter

This uses the default configuration for authentication and a given project ID.

```java
public class MyMainClass {
  public static void main(String[] args) {
    // Exporter will export to Stackdriver every 10 seconds.
    StackdriverStatsExporter.createWithProjectId("MyStackdriverProjectId", Duration.create(10, 0));
  }
}
```

#### Set Monitored Resource for exporter

By default, the Stackdriver Stats Exporter uses [a global Stackdriver monitored resource with no 
labels](https://cloud.google.com/monitoring/api/resources#tag_global). If you want to use a custom Monitored Resource, do:

```java
public class MyMainClass {
  public static void main(String[] args) {
    // Set a custom MonitoredResource. Please make sure each Stackdriver Stats Exporter has a 
    // unique MonitoredResource.      
    StackdriverStatsExporter.createAndRegisterWithProjectIdAndMonitoredResource("MyStackdriverProjectId", Duration.create(10, 0), myResource);
  }
}
```

For a complete list of available Stackdriver monitored resources, please refer to [Stackdriver 
Documentation](https://cloud.google.com/monitoring/api/resources).

#### Authentication

This exporter uses [google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java),
for details about how to configure the authentication see [here](https://github.com/GoogleCloudPlatform/google-cloud-java#authentication).

If you prefer to manually set the credentials use:
```
StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
    new GoogleCredentials(new AccessToken(accessToken, expirationTime)), 
    "MyStackdriverProjectId",
    Duration.create(10, 0));
```

#### Specifying a Project ID

This exporter uses [google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java),
for details about how to configure the project ID see [here](https://github.com/GoogleCloudPlatform/google-cloud-java#specifying-a-project-id).

If you prefer to manually set the project ID use:
```
StackdriverStatsExporter.createAndRegisterWithProjectId("MyStackdriverProjectId", Duration.create(10, 0));
```

#### Java Versions

Java 7 or above is required for using this exporter.

## FAQ
### Why did I get a PERMISSION_DENIED error from Stackdriver when using this exporter?
To use our Stackdriver Stats exporter, your Stackdriver account needs to have permission to [create
custom metrics](https://cloud.google.com/monitoring/custom-metrics/creating-metrics), and that 
requires a [Premium tier Stackdriver account](https://cloud.google.com/monitoring/accounts/tiers#this_request_is_only_available_in_the_premium_tier). 
Please note that by default all new Stackdriver accounts are Basic tier. To upgrade to a Premium 
tier Stackdriver account, follow the instructions [here](https://cloud.google.com/monitoring/accounts/tiers#start-premium).

[stackdriver-monitoring]: https://cloud.google.com/monitoring/