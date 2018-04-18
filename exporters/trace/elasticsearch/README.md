# OpenCensus Elasticsearch Exporter

The *OpenCensus Elasticsearch trace exporter* is a trace exporter that exports
data to [Elasticsearch](https://www.elastic.co/products/elasticsearch).

Elasticsearch is a a distributed, RESTful search and analytics engine.
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
    <version>0.13.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-elasticsearch</artifactId>
        <version>0.13.0</version>
  </dependency>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
   <version>0.13.0</version>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-api:0.13.0'
compile 'io.opencensus:opencensus-exporter-trace-elasticsearch:0.13.0'
runtime 'io.opencensus:opencensus-impl:0.13.0'
```

#### Register the exporter

The ElasticsearchConfig is the configurations required by the exporter.

      private final static String ELASTIC_SEARCH_URL= "http://localhost:9200";
      private final static String INDEX_FOR_TRACE= "opencensus";
      private final static String TYPE_FOR_TRACE= "trace";
      private final static String MICROSERVICE= "sample-opencensus";



    ElasticsearchConfiguration elasticsearchConfiguration
            = new ElasticsearchConfiguration(MICROSERVICE,null, null,ELASTIC_SEARCH_URL,
            INDEX_FOR_TRACE,TYPE_FOR_TRACE);
    ElasticsearchTraceExporter.createAndRegister(elasticsearchConfiguration);

#### Java Versions

Java 7 or above is required for using this exporter.

