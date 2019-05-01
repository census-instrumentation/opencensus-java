# OpenCensus Elasticsearch Exporter

The *OpenCensus Elasticsearch trace exporter* is a trace exporter that exports
data to [Elasticsearch](https://www.elastic.co/products/elasticsearch).

Elasticsearch is a distributed, RESTful search and analytics engine.
It centrally stores your data so you can discover the expected and uncover the unexpected.
Using [Kibana](https://www.elastic.co/products/kibana) we can visualize our trace metrics
with self designed dashboards and easily search as well.

Once the trace is exported to Elasticsearch, you can search traces to find more data on trace.



## Quickstart


### Prerequisites

#### Add the dependencies to your project

For Maven add to your `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>..</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-elasticsearch</artifactId>
    <version>..</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>..</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:

```groovy
compile 'io.opencensus:opencensus-api:..'
compile 'io.opencensus:opencensus-exporter-trace-elasticsearch:..'
runtime 'io.opencensus:opencensus-impl:..'
```

#### Register the exporter

The ElasticsearchConfig is the configurations required by the exporter.

```java
private final static String ELASTIC_SEARCH_URL= "http://localhost:9200";
private final static String INDEX_FOR_TRACE= "opencensus";
private final static String TYPE_FOR_TRACE= "trace";
private final static String APP_NAME= "sample-opencensus";

public static void main(String[] args) throws Exception{
      
  ElasticsearchTraceConfiguration elasticsearchTraceConfiguration = ElasticsearchTraceConfiguration.builder()
  .setAppName(MICROSERVICE)
  .setElasticsearchUrl(ELASTIC_SEARCH_URL)
  .setElasticsearchIndex(INDEX_FOR_TRACE)
  .setElasticsearchType(TYPE_FOR_TRACE).build();
  ElasticsearchTraceExporter.createAndRegister(elasticsearchTraceConfiguration);
    
}
```


![Sample Traces exported to Elasticsearch](https://raw.githubusercontent.com/malike/distributed-tracing/master/opencensus/distributed_tracing_elk_discover.png?raw=true)


#### Java Versions

Java 6 or above is required for using this exporter.


