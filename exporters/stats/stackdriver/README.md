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

These steps enable the API but don't require that your app is hosted on Google Cloud Platform.

### Hello "Stackdriver Stats"

#### Add the dependencies to your project

For Maven add to your `pom.xml`: TODO

For Gradle add to your dependencies: TODO

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

[stackdriver-monitoring]: https://cloud.google.com/monitoring/