# OpenCensus Elasticsearch Exporter

The *OpenCensus Elasticsearch trace exporter* is a trace exporter that exports
data to [Elasticsearch](https://www.elastic.co/products/elasticsearch).

Elasticsearch is a a distributed, RESTful search and analytics engine.
It centrally stores your data so you can discover the expected and uncover the unexpected.
Using [Kibana](https://www.elastic.co/products/kibana) we can visualize our trace metrics
with self designed dashboards and easily search as well.



## Quickstart


### Prerequisites

#### Add the dependencies to your project

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
    <artifactId>opencensus-exporter-trace-elasticsearch</artifactId>
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
```groovy
compile 'io.opencensus:opencensus-api:0.12.2'
compile 'io.opencensus:opencensus-exporter-trace-elasticsearch:0.12.2'
runtime 'io.opencensus:opencensus-impl:0.12.2'
```

#### Register the exporter

The ElasticsearchConfig is the configurations required by the exporter.

**userName** : The username for *Basic Auth* required by elasticsearch restful api.
**password** : The password for *Basic Auth* required by elasticsearch restful api.
**elasticsearchUrl** : The elasticsearch url for your installation.
**elasticsearchIndex** : The elasticsearch index to store the data.


```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    ElasticsearchConfig elasticsearchConfig =
        new ElasticsearchConfig(null,null,"opencensus-trace","http://localhost:9200/");
    ElasticsearchTraceExporter.createAndRegister(
        ElasticsearchTraceExporter.builder().build());
    // ...
  }
}
```

#### Java Versions

Java 7 or above is required for using this exporter.


