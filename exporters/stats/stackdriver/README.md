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
3. [Enable the Stackdriver Monitoring API](https://console.cloud.google.com/apis/dashboard).

These steps enable the API but don't require that your app is hosted on Google Cloud Platform.

### Hello "Stackdriver Stats"

#### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>0.21.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-stats-stackdriver</artifactId>
    <version>0.21.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>0.21.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.21.0'
compile 'io.opencensus:opencensus-exporter-stats-stackdriver:0.21.0'
runtime 'io.opencensus:opencensus-impl:0.21.0'
```

#### Register the exporter

This uses the default configuration for authentication and a given project ID.

```java
public class MyMainClass {
  public static void main(String[] args) {
    StackdriverStatsExporter.createAndRegister(
        StackdriverStatsConfiguration.builder().build());
  }
}
```

#### Set Monitored Resource for exporter

By default, Stackdriver Stats Exporter will try to automatically detect the environment if your 
application is running on GCE, GKE or AWS EC2, and generate a corresponding Stackdriver GCE/GKE/EC2 
monitored resource. For GKE particularly, you may want to set up some environment variables so that 
Exporter can correctly identify your pod, cluster and container. Follow the Kubernetes instruction 
[here](https://cloud.google.com/kubernetes-engine/docs/tutorials/custom-metrics-autoscaling#exporting_metrics_from_the_application) 
and [here](https://kubernetes.io/docs/tasks/inject-data-application/environment-variable-expose-pod-information/).

Otherwise, Exporter will use [a global Stackdriver monitored resource with a project_id label](https://cloud.google.com/monitoring/api/resources#tag_global), 
and it works fine when you have only one exporter running. 

If you want to have multiple processes exporting stats for the same metric concurrently, and your 
application is running on some different environment than GCE, GKE or AWS EC2 (for example DataFlow), 
please associate a unique monitored resource with each exporter if possible. 
Please note that there is also an "opencensus_task" metric label that uniquely identifies the 
uploaded stats.

To set a custom MonitoredResource:

```java
public class MyMainClass {
  public static void main(String[] args) {
    // A sample DataFlow monitored resource.
    MonitoredResource myResource = MonitoredResource.newBuilder()
                                               .setType("dataflow_job")
                                               .putLabels("project_id", "my_project")
                                               .putLabels("job_name", "my_job")
                                               .putLabels("region", "us-east1")
                                               .build();
    
    // Set a custom MonitoredResource. Please make sure each Stackdriver Stats Exporter has a 
    // unique MonitoredResource.      
    StackdriverStatsExporter.createAndRegister(
        StackdriverStatsConfiguration.builder().setMonitoredResource(myResource).build());
  }
}
```

For a complete list of valid Stackdriver monitored resources, please refer to [Stackdriver 
Documentation](https://cloud.google.com/monitoring/custom-metrics/creating-metrics#which-resource).
Please also note that although there are a lot of monitored resources available on [Stackdriver](https://cloud.google.com/monitoring/api/resources), 
only [a small subset of them](https://cloud.google.com/monitoring/custom-metrics/creating-metrics#which-resource) 
are compatible with the Opencensus Stackdriver Stats Exporter.

#### Authentication

This exporter uses [google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java),
for details about how to configure the authentication see [here](https://github.com/GoogleCloudPlatform/google-cloud-java#authentication).

If you prefer to manually set the credentials use:
```
StackdriverStatsExporter.createAndRegister(
    StackdriverStatsConfiguration.builder()
        .setCredentials(new GoogleCredentials(new AccessToken(accessToken, expirationTime)))
        .setProjectId("MyStackdriverProjectId")
        .setExportInterval(Duration.create(10, 0))
        .build());
```

#### Specifying a Project ID

This exporter uses [google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java),
for details about how to configure the project ID see [here](https://github.com/GoogleCloudPlatform/google-cloud-java#specifying-a-project-id).

If you prefer to manually set the project ID use:
```
StackdriverStatsExporter.createAndRegister(
    StackdriverStatsConfiguration.builder().setProjectId("MyStackdriverProjectId").build());
```

#### Java Versions

Java 7 or above is required for using this exporter.

## FAQ
### Why did I get a PERMISSION_DENIED error from Stackdriver when using this exporter?
To use our Stackdriver Stats exporter, you need to set up billing for your cloud project, since
creating and uploading custom metrics to Stackdriver Monitoring is
[not free](https://cloud.google.com/stackdriver/pricing_v2#monitoring-costs).

To enable billing, follow the instructions [here](https://support.google.com/cloud/answer/6288653#new-billing).

### What is "opencensus_task" metric label ?
Stackdriver requires that each Timeseries to be updated only by one task at a time. A
`Timeseries` is uniquely identified by the `MonitoredResource` and the `Metric`'s labels.
Stackdriver exporter adds a new `Metric` label for each custom `Metric` to ensure the uniqueness
of the `Timeseries`. The format of the label is: `{LANGUAGE}-{PID}@{HOSTNAME}`, if `{PID}` is not
available a random number will be used.

You have the option to override the "opencensus_task" metric label with custom constant labels using
`StackdriverStatsConfiguration.Builder.setConstantLabels()`. If you do so, make sure that the 
monitored resource together with these labels is unique to the current process. This is to ensure 
that there is only a single writer to each time series in Stackdriver.

You can also set `StackdriverStatsConfiguration.Builder.setConstantLabels()` to an empty map to 
avoid getting the default "opencensus_task" label. You should only do this if you know that the 
monitored resource uniquely identifies this process.

### Why did I get an error "java.lang.NoSuchMethodError: com.google.common...", like "java.lang.NoSuchMethodError:com.google.common.base.Throwables.throwIfInstanceOf"?
This is probably because there is a version conflict on Guava in the dependency tree.

For example, `com.google.common.base.Throwables.throwIfInstanceOf` is introduced to Guava 20.0.
If your application has a dependency that bundles a Guava with version 19.0 or below
(for example, gRPC 1.10.0), it might cause a `NoSuchMethodError` since
`com.google.common.base.Throwables.throwIfInstanceOf` doesn't exist before Guava 20.0.

In this case, please either add an explicit dependency on a newer version of Guava that has the 
new method (20.0 in the previous example), or if possible, upgrade the dependency that depends on 
Guava to a newer version that depends on the newer Guava (for example, upgrade to gRPC 1.12.0).

[stackdriver-monitoring]: https://cloud.google.com/monitoring/
