/*
 * Copyright 2017, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.exporter.stats.stackdriver;

import com.google.api.Distribution;
import com.google.api.Distribution.BucketOptions;
import com.google.api.Distribution.BucketOptions.Explicit;
import com.google.api.Distribution.Exemplar;
import com.google.api.LabelDescriptor;
import com.google.api.LabelDescriptor.ValueType;
import com.google.api.Metric;
import com.google.api.MetricDescriptor;
import com.google.api.MetricDescriptor.MetricKind;
import com.google.api.MonitoredResource;
import com.google.cloud.MetadataConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.SpanContext;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.contrib.exemplar.util.ExemplarUtils;
import io.opencensus.contrib.resource.util.AwsEc2InstanceResource;
import io.opencensus.contrib.resource.util.GcpGceInstanceResource;
import io.opencensus.contrib.resource.util.K8sContainerResource;
import io.opencensus.contrib.resource.util.ResourceUtils;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.Distribution.BucketOptions.ExplicitOptions;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.Value;
import io.opencensus.resource.Resource;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Util methods to convert OpenCensus Metrics data models to StackDriver monitoring data models. */
@SuppressWarnings("deprecation")
final class StackdriverExportUtils {

  // TODO(songya): do we want these constants to be customizable?
  @VisibleForTesting static final String OPENCENSUS_TASK = "opencensus_task";
  @VisibleForTesting static final String OPENCENSUS_TASK_DESCRIPTION = "Opencensus task identifier";
  @VisibleForTesting static final String STACKDRIVER_PROJECT_ID_KEY = "project_id";
  @VisibleForTesting static final String DEFAULT_DISPLAY_NAME_PREFIX = "OpenCensus/";
  @VisibleForTesting static final String CUSTOM_METRIC_DOMAIN = "custom.googleapis.com/";

  @VisibleForTesting
  static final String CUSTOM_OPENCENSUS_DOMAIN = CUSTOM_METRIC_DOMAIN + "opencensus/";
  // Stackdriver Monitoring v3 only accepts up to 200 TimeSeries per CreateTimeSeries call.
  @VisibleForTesting static final int MAX_BATCH_EXPORT_SIZE = 200;
  private static final String GCP_GKE_CONTAINER = "k8s_container";
  private static final String GCP_GCE_INSTANCE = "gce_instance";
  private static final String AWS_EC2_INSTANCE = "aws_ec2_instance";
  private static final String GLOBAL = "global";
  @VisibleForTesting static final String AWS_REGION_VALUE_PREFIX = "aws:";

  private static final Logger logger = Logger.getLogger(StackdriverExportUtils.class.getName());
  private static final String OPENCENSUS_TASK_VALUE_DEFAULT = generateDefaultTaskValue();

  // Mappings for the well-known OC resources to applicable Stackdriver resources.
  private static final Map<String, String> GCP_RESOURCE_MAPPING = getGcpResourceLabelsMappings();
  private static final Map<String, String> K8S_RESOURCE_MAPPING = getK8sResourceLabelsMappings();
  private static final Map<String, String> AWS_RESOURCE_MAPPING = getAwsResourceLabelsMappings();

  @VisibleForTesting
  static final LabelKey PERCENTILE_LABEL_KEY =
      LabelKey.create("percentile", "the value at a given percentile of a distribution");

  @VisibleForTesting
  static final String SNAPSHOT_SUFFIX_PERCENTILE = "_summary_snapshot_percentile";

  @VisibleForTesting static final String SUMMARY_SUFFIX_COUNT = "_summary_count";
  @VisibleForTesting static final String SUMMARY_SUFFIX_SUM = "_summary_sum";

  // Cached project ID only for Exemplar attachments. Without this we'll have to pass the project ID
  // every time when we convert a Distribution value.
  @javax.annotation.Nullable private static volatile String cachedProjectIdForExemplar = null;

  @VisibleForTesting
  static final String EXEMPLAR_ATTACHMENT_TYPE_STRING =
      "type.googleapis.com/google.protobuf.StringValue";

  @VisibleForTesting
  static final String EXEMPLAR_ATTACHMENT_TYPE_SPAN_CONTEXT =
      "type.googleapis.com/google.monitoring.v3.SpanContext";

  // TODO: add support for dropped label attachment.
  // private static final String EXEMPLAR_ATTACHMENT_TYPE_DROPPED_LABELS =
  //     "type.googleapis.com/google.monitoring.v3.DroppedLabels";

  // Constant functions for TypedValue.
  private static final Function<Double, TypedValue> typedValueDoubleFunction =
      new Function<Double, TypedValue>() {
        @Override
        public TypedValue apply(Double arg) {
          TypedValue.Builder builder = TypedValue.newBuilder();
          builder.setDoubleValue(arg);
          return builder.build();
        }
      };
  private static final Function<Long, TypedValue> typedValueLongFunction =
      new Function<Long, TypedValue>() {
        @Override
        public TypedValue apply(Long arg) {
          TypedValue.Builder builder = TypedValue.newBuilder();
          builder.setInt64Value(arg);
          return builder.build();
        }
      };
  private static final Function<io.opencensus.metrics.export.Distribution, TypedValue>
      typedValueDistributionFunction =
          new Function<io.opencensus.metrics.export.Distribution, TypedValue>() {
            @Override
            public TypedValue apply(io.opencensus.metrics.export.Distribution arg) {
              TypedValue.Builder builder = TypedValue.newBuilder();
              return builder.setDistributionValue(createDistribution(arg)).build();
            }
          };
  private static final Function<Summary, TypedValue> typedValueSummaryFunction =
      new Function<Summary, TypedValue>() {
        @Override
        public TypedValue apply(Summary arg) {
          TypedValue.Builder builder = TypedValue.newBuilder();
          return builder.build();
        }
      };

  // Constant functions for BucketOptions.
  private static final Function<ExplicitOptions, BucketOptions> bucketOptionsExplicitFunction =
      new Function<ExplicitOptions, BucketOptions>() {
        @Override
        public BucketOptions apply(ExplicitOptions arg) {
          BucketOptions.Builder builder = BucketOptions.newBuilder();
          Explicit.Builder explicitBuilder = Explicit.newBuilder();
          // The first bucket bound should be 0.0 because the Metrics first bucket is
          // [0, first_bound) but Stackdriver monitoring bucket bounds begin with -infinity
          // (first bucket is (-infinity, 0))
          explicitBuilder.addBounds(0.0);
          explicitBuilder.addAllBounds(arg.getBucketBoundaries());
          builder.setExplicitBuckets(explicitBuilder.build());
          return builder.build();
        }
      };

  private static String generateDefaultTaskValue() {
    // Something like '<pid>@<hostname>', at least in Oracle and OpenJdk JVMs
    final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
    // If not the expected format then generate a random number.
    if (jvmName.indexOf('@') < 1) {
      String hostname = "localhost";
      try {
        hostname = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        logger.log(Level.INFO, "Unable to get the hostname.", e);
      }
      // Generate a random number and use the same format "random_number@hostname".
      return "java-" + new SecureRandom().nextInt() + "@" + hostname;
    }
    return "java-" + jvmName;
  }

  // Convert a OpenCensus MetricDescriptor to a StackDriver MetricDescriptor
  static MetricDescriptor createMetricDescriptor(
      io.opencensus.metrics.export.MetricDescriptor metricDescriptor,
      String projectId,
      String domain,
      String displayNamePrefix) {

    MetricDescriptor.Builder builder = MetricDescriptor.newBuilder();
    String type = generateType(metricDescriptor.getName(), domain);
    // Name format refers to
    // cloud.google.com/monitoring/api/ref_v3/rest/v3/projects.metricDescriptors/create
    builder.setName("projects/" + projectId + "/metricDescriptors/" + type);
    builder.setType(type);
    builder.setDescription(metricDescriptor.getDescription());
    builder.setDisplayName(createDisplayName(metricDescriptor.getName(), displayNamePrefix));
    for (LabelKey labelKey : metricDescriptor.getLabelKeys()) {
      builder.addLabels(createLabelDescriptor(labelKey));
    }
    builder.addLabels(
        LabelDescriptor.newBuilder()
            .setKey(OPENCENSUS_TASK)
            .setDescription(OPENCENSUS_TASK_DESCRIPTION)
            .setValueType(ValueType.STRING)
            .build());

    builder.setUnit(metricDescriptor.getUnit());
    builder.setMetricKind(createMetricKind(metricDescriptor.getType()));
    builder.setValueType(createValueType(metricDescriptor.getType()));
    return builder.build();
  }

  private static String generateType(String metricName, String domain) {
    return domain + metricName;
  }

  private static String createDisplayName(String metricName, String displayNamePrefix) {
    return displayNamePrefix + metricName;
  }

  // Construct a LabelDescriptor from a LabelKey
  @VisibleForTesting
  static LabelDescriptor createLabelDescriptor(LabelKey labelKey) {
    LabelDescriptor.Builder builder = LabelDescriptor.newBuilder();
    builder.setKey(labelKey.getKey());
    builder.setDescription(labelKey.getDescription());
    // Now we only support String tags
    builder.setValueType(ValueType.STRING);
    return builder.build();
  }

  // Convert a OpenCensus Type to a StackDriver MetricKind
  @VisibleForTesting
  static MetricKind createMetricKind(Type type) {
    if (type == Type.GAUGE_INT64 || type == Type.GAUGE_DOUBLE) {
      return MetricKind.GAUGE;
    } else if (type == Type.CUMULATIVE_INT64
        || type == Type.CUMULATIVE_DOUBLE
        || type == Type.CUMULATIVE_DISTRIBUTION) {
      return MetricKind.CUMULATIVE;
    }
    return MetricKind.UNRECOGNIZED;
  }

  // Convert a OpenCensus Type to a StackDriver ValueType
  @VisibleForTesting
  static MetricDescriptor.ValueType createValueType(Type type) {
    if (type == Type.CUMULATIVE_DOUBLE || type == Type.GAUGE_DOUBLE) {
      return MetricDescriptor.ValueType.DOUBLE;
    } else if (type == Type.GAUGE_INT64 || type == Type.CUMULATIVE_INT64) {
      return MetricDescriptor.ValueType.INT64;
    } else if (type == Type.GAUGE_DISTRIBUTION || type == Type.CUMULATIVE_DISTRIBUTION) {
      return MetricDescriptor.ValueType.DISTRIBUTION;
    }
    return MetricDescriptor.ValueType.UNRECOGNIZED;
  }

  // Convert metric's timeseries to a list of TimeSeries, so that metric can be uploaded to
  // StackDriver.
  static List<TimeSeries> createTimeSeriesList(
      io.opencensus.metrics.export.Metric metric,
      MonitoredResource monitoredResource,
      String domain,
      String projectId) {
    List<TimeSeries> timeSeriesList = Lists.newArrayList();
    io.opencensus.metrics.export.MetricDescriptor metricDescriptor = metric.getMetricDescriptor();

    if (!projectId.equals(cachedProjectIdForExemplar)) {
      cachedProjectIdForExemplar = projectId;
    }

    // Shared fields for all TimeSeries generated from the same Metric
    TimeSeries.Builder shared = TimeSeries.newBuilder();
    shared.setMetricKind(createMetricKind(metricDescriptor.getType()));
    shared.setResource(monitoredResource);
    shared.setValueType(createValueType(metricDescriptor.getType()));

    // Each entry in timeSeriesList will be converted into an independent TimeSeries object
    for (io.opencensus.metrics.export.TimeSeries timeSeries : metric.getTimeSeriesList()) {
      // TODO(mayurkale): Consider using setPoints instead of builder clone and addPoints.
      TimeSeries.Builder builder = shared.clone();
      builder.setMetric(createMetric(metricDescriptor, timeSeries.getLabelValues(), domain));

      io.opencensus.common.Timestamp startTimeStamp = timeSeries.getStartTimestamp();
      for (io.opencensus.metrics.export.Point point : timeSeries.getPoints()) {
        builder.addPoints(createPoint(point, startTimeStamp));
      }
      timeSeriesList.add(builder.build());
    }
    return timeSeriesList;
  }

  // Create a Metric using the LabelKeys and LabelValues.
  @VisibleForTesting
  static Metric createMetric(
      io.opencensus.metrics.export.MetricDescriptor metricDescriptor,
      List<LabelValue> labelValues,
      String domain) {
    Metric.Builder builder = Metric.newBuilder();
    builder.setType(generateType(metricDescriptor.getName(), domain));
    Map<String, String> stringTagMap = Maps.newHashMap();
    List<LabelKey> labelKeys = metricDescriptor.getLabelKeys();
    for (int i = 0; i < labelValues.size(); i++) {
      String value = labelValues.get(i).getValue();
      if (value == null) {
        continue;
      }
      stringTagMap.put(labelKeys.get(i).getKey(), value);
    }
    stringTagMap.put(OPENCENSUS_TASK, OPENCENSUS_TASK_VALUE_DEFAULT);
    builder.putAllLabels(stringTagMap);
    return builder.build();
  }

  // Convert a OpenCensus Point to a StackDriver Point
  @VisibleForTesting
  static Point createPoint(
      io.opencensus.metrics.export.Point point,
      @javax.annotation.Nullable io.opencensus.common.Timestamp startTimestamp) {
    TimeInterval.Builder timeIntervalBuilder = TimeInterval.newBuilder();
    timeIntervalBuilder.setEndTime(convertTimestamp(point.getTimestamp()));
    if (startTimestamp != null) {
      timeIntervalBuilder.setStartTime(convertTimestamp(startTimestamp));
    }

    Point.Builder builder = Point.newBuilder();
    builder.setInterval(timeIntervalBuilder.build());
    builder.setValue(createTypedValue(point.getValue()));
    return builder.build();
  }

  // Convert a OpenCensus Value to a StackDriver TypedValue
  // Note TypedValue is "A single strongly-typed value", i.e only one field should be set.
  @VisibleForTesting
  static TypedValue createTypedValue(Value value) {
    return value.match(
        typedValueDoubleFunction,
        typedValueLongFunction,
        typedValueDistributionFunction,
        typedValueSummaryFunction,
        Functions.<TypedValue>throwIllegalArgumentException());
  }

  // Convert a OpenCensus Distribution to a StackDriver Distribution
  @VisibleForTesting
  static Distribution createDistribution(io.opencensus.metrics.export.Distribution distribution) {
    Distribution.Builder builder =
        Distribution.newBuilder()
            .setBucketOptions(createBucketOptions(distribution.getBucketOptions()))
            .setCount(distribution.getCount())
            .setMean(
                distribution.getCount() == 0 ? 0 : distribution.getSum() / distribution.getCount())
            .setSumOfSquaredDeviation(distribution.getSumOfSquaredDeviations());
    setBucketCountsAndExemplars(distribution.getBuckets(), builder);
    return builder.build();
  }

  // Convert a OpenCensus BucketOptions to a StackDriver BucketOptions
  @VisibleForTesting
  static BucketOptions createBucketOptions(
      @javax.annotation.Nullable
          io.opencensus.metrics.export.Distribution.BucketOptions bucketOptions) {
    final BucketOptions.Builder builder = BucketOptions.newBuilder();
    if (bucketOptions == null) {
      return builder.build();
    }

    return bucketOptions.match(
        bucketOptionsExplicitFunction, Functions.<BucketOptions>throwIllegalArgumentException());
  }

  // Convert OpenCensus Buckets to a list of bucket counts and a list of proto Exemplars, then set
  // them to the builder.
  private static void setBucketCountsAndExemplars(
      List<Bucket> buckets, Distribution.Builder builder) {
    // The first bucket (underflow bucket) should always be 0 count because the Metrics first bucket
    // is [0, first_bound) but StackDriver distribution consists of an underflow bucket (number 0).
    builder.addBucketCounts(0L);
    for (Bucket bucket : buckets) {
      builder.addBucketCounts(bucket.getCount());
      @javax.annotation.Nullable
      io.opencensus.metrics.export.Distribution.Exemplar exemplar = bucket.getExemplar();
      if (exemplar != null) {
        builder.addExemplars(toProtoExemplar(exemplar));
      }
    }
  }

  private static Exemplar toProtoExemplar(
      io.opencensus.metrics.export.Distribution.Exemplar exemplar) {
    Exemplar.Builder builder =
        Exemplar.newBuilder()
            .setValue(exemplar.getValue())
            .setTimestamp(convertTimestamp(exemplar.getTimestamp()));
    @javax.annotation.Nullable String traceId = null;
    @javax.annotation.Nullable String spanId = null;
    for (Map.Entry<String, String> attachment : exemplar.getAttachments().entrySet()) {
      String key = attachment.getKey();
      String value = attachment.getValue();
      if (ExemplarUtils.ATTACHMENT_KEY_TRACE_ID.equals(key)) {
        traceId = value;
      } else if (ExemplarUtils.ATTACHMENT_KEY_SPAN_ID.equals(key)) {
        spanId = value;
      } else { // Everything else will be treated as plain strings.
        builder.addAttachments(
            Any.newBuilder()
                .setTypeUrl(EXEMPLAR_ATTACHMENT_TYPE_STRING)
                .setValue(ByteString.copyFromUtf8(value))
                .build());
      }
    }
    if (traceId != null && spanId != null && cachedProjectIdForExemplar != null) {
      String spanName =
          String.format(
              "projects/%s/traces/%s/spans/%s", cachedProjectIdForExemplar, traceId, spanId);
      SpanContext spanContextProto = SpanContext.newBuilder().setSpanName(spanName).build();
      builder.addAttachments(
          Any.newBuilder()
              .setTypeUrl(EXEMPLAR_ATTACHMENT_TYPE_SPAN_CONTEXT)
              .setValue(spanContextProto.toByteString())
              .build());
    }
    return builder.build();
  }

  @VisibleForTesting
  static void setCachedProjectIdForExemplar(@javax.annotation.Nullable String projectId) {
    cachedProjectIdForExemplar = projectId;
  }

  // Convert a OpenCensus Timestamp to a StackDriver Timestamp
  @VisibleForTesting
  static Timestamp convertTimestamp(io.opencensus.common.Timestamp censusTimestamp) {
    if (censusTimestamp.getSeconds() < 0) {
      // StackDriver doesn't handle negative timestamps.
      return Timestamp.newBuilder().build();
    }
    return Timestamp.newBuilder()
        .setSeconds(censusTimestamp.getSeconds())
        .setNanos(censusTimestamp.getNanos())
        .build();
  }

  /* Return a self-configured StackDriver monitored resource. */
  static MonitoredResource getDefaultResource() {
    MonitoredResource.Builder builder = MonitoredResource.newBuilder();
    // Populate internal resource label for defaulting project_id label.
    // This allows stats from other projects (e.g from GAE running in another project) to be
    // collected.
    if (MetadataConfig.getProjectId() != null) {
      builder.putLabels(STACKDRIVER_PROJECT_ID_KEY, MetadataConfig.getProjectId());
    }

    Resource autoDetectedResource = ResourceUtils.detectResource();
    if (autoDetectedResource == null || autoDetectedResource.getType() == null) {
      builder.setType(GLOBAL);
      return builder.build();
    }

    setResourceForBuilder(builder, autoDetectedResource);
    return builder.build();
  }

  @VisibleForTesting
  static void setResourceForBuilder(
      MonitoredResource.Builder builder, Resource autoDetectedResource) {
    String type = autoDetectedResource.getType();
    if (type == null) {
      return;
    }

    Map<String, String> mappings;
    switch (type) {
      case GcpGceInstanceResource.TYPE:
        builder.setType(GCP_GCE_INSTANCE);
        mappings = GCP_RESOURCE_MAPPING;
        break;
      case K8sContainerResource.TYPE:
        builder.setType(GCP_GKE_CONTAINER);
        mappings = K8S_RESOURCE_MAPPING;
        break;
      case AwsEc2InstanceResource.TYPE:
        builder.setType(AWS_EC2_INSTANCE);
        mappings = AWS_RESOURCE_MAPPING;
        break;
      default:
        builder.setType(GLOBAL);
        return;
    }

    Map<String, String> resLabels = autoDetectedResource.getLabels();
    for (Map.Entry<String, String> entry : mappings.entrySet()) {
      if (entry.getValue() != null && resLabels.containsKey(entry.getValue())) {
        String resourceLabelKey = entry.getKey();
        String resourceLabelValue = resLabels.get(entry.getValue());
        if (AwsEc2InstanceResource.TYPE.equals(type) && "region".equals(resourceLabelKey)) {
          // Add "aws:" prefix to AWS EC2 region label. This is Stackdriver specific requirement.
          resourceLabelValue = AWS_REGION_VALUE_PREFIX + resourceLabelValue;
        }
        builder.putLabels(resourceLabelKey, resourceLabelValue);
      }
    }
  }

  @VisibleForTesting
  static List<io.opencensus.metrics.export.Metric> convertSummaryMetric(
      io.opencensus.metrics.export.Metric summaryMetric) {
    List<io.opencensus.metrics.export.Metric> metricsList = Lists.newArrayList();
    final List<io.opencensus.metrics.export.TimeSeries> percentileTimeSeries = new ArrayList<>();
    final List<io.opencensus.metrics.export.TimeSeries> summaryCountTimeSeries = new ArrayList<>();
    final List<io.opencensus.metrics.export.TimeSeries> summarySumTimeSeries = new ArrayList<>();
    for (final io.opencensus.metrics.export.TimeSeries timeSeries :
        summaryMetric.getTimeSeriesList()) {
      final List<LabelValue> labelValuesWithPercentile =
          new ArrayList<>(timeSeries.getLabelValues());
      final io.opencensus.common.Timestamp timeSeriesTimestamp = timeSeries.getStartTimestamp();
      for (io.opencensus.metrics.export.Point point : timeSeries.getPoints()) {
        final io.opencensus.common.Timestamp pointTimestamp = point.getTimestamp();
        point
            .getValue()
            .match(
                Functions.<Void>returnNull(),
                Functions.<Void>returnNull(),
                Functions.<Void>returnNull(),
                new Function<Summary, Void>() {
                  @Override
                  public Void apply(Summary summary) {
                    Long count = summary.getCount();
                    if (count != null) {
                      createTimeSeries(
                          timeSeries.getLabelValues(),
                          Value.longValue(count),
                          pointTimestamp,
                          timeSeriesTimestamp,
                          summaryCountTimeSeries);
                    }
                    Double sum = summary.getSum();
                    if (sum != null) {
                      createTimeSeries(
                          timeSeries.getLabelValues(),
                          Value.doubleValue(sum),
                          pointTimestamp,
                          timeSeriesTimestamp,
                          summarySumTimeSeries);
                    }
                    Snapshot snapshot = summary.getSnapshot();
                    for (ValueAtPercentile valueAtPercentile : snapshot.getValueAtPercentiles()) {
                      labelValuesWithPercentile.add(
                          LabelValue.create(valueAtPercentile.getPercentile() + ""));
                      createTimeSeries(
                          labelValuesWithPercentile,
                          Value.doubleValue(valueAtPercentile.getValue()),
                          pointTimestamp,
                          null,
                          percentileTimeSeries);
                      labelValuesWithPercentile.remove(labelValuesWithPercentile.size() - 1);
                    }
                    return null;
                  }
                },
                Functions.<Void>returnNull());
      }
    }

    // Metric for summary->count.
    if (summaryCountTimeSeries.size() > 0) {
      addMetric(
          metricsList,
          io.opencensus.metrics.export.MetricDescriptor.create(
              summaryMetric.getMetricDescriptor().getName() + SUMMARY_SUFFIX_COUNT,
              summaryMetric.getMetricDescriptor().getDescription(),
              "1",
              Type.CUMULATIVE_INT64,
              summaryMetric.getMetricDescriptor().getLabelKeys()),
          summaryCountTimeSeries);
    }

    // Metric for summary->sum.
    if (summarySumTimeSeries.size() > 0) {
      addMetric(
          metricsList,
          io.opencensus.metrics.export.MetricDescriptor.create(
              summaryMetric.getMetricDescriptor().getName() + SUMMARY_SUFFIX_SUM,
              summaryMetric.getMetricDescriptor().getDescription(),
              summaryMetric.getMetricDescriptor().getUnit(),
              Type.CUMULATIVE_DOUBLE,
              summaryMetric.getMetricDescriptor().getLabelKeys()),
          summarySumTimeSeries);
    }

    // Metric for summary->snapshot->percentiles.
    List<LabelKey> labelKeys = new ArrayList<>(summaryMetric.getMetricDescriptor().getLabelKeys());
    labelKeys.add(PERCENTILE_LABEL_KEY);
    addMetric(
        metricsList,
        io.opencensus.metrics.export.MetricDescriptor.create(
            summaryMetric.getMetricDescriptor().getName() + SNAPSHOT_SUFFIX_PERCENTILE,
            summaryMetric.getMetricDescriptor().getDescription(),
            summaryMetric.getMetricDescriptor().getUnit(),
            Type.GAUGE_DOUBLE,
            labelKeys),
        percentileTimeSeries);
    return metricsList;
  }

  private static void addMetric(
      List<io.opencensus.metrics.export.Metric> metricsList,
      io.opencensus.metrics.export.MetricDescriptor metricDescriptor,
      List<io.opencensus.metrics.export.TimeSeries> timeSeriesList) {
    metricsList.add(io.opencensus.metrics.export.Metric.create(metricDescriptor, timeSeriesList));
  }

  private static void createTimeSeries(
      List<LabelValue> labelValues,
      Value value,
      io.opencensus.common.Timestamp pointTimestamp,
      @javax.annotation.Nullable io.opencensus.common.Timestamp timeSeriesTimestamp,
      List<io.opencensus.metrics.export.TimeSeries> timeSeriesList) {
    timeSeriesList.add(
        io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
            labelValues,
            io.opencensus.metrics.export.Point.create(value, pointTimestamp),
            timeSeriesTimestamp));
  }

  private static Map<String, String> getGcpResourceLabelsMappings() {
    Map<String, String> resourceLabels = new LinkedHashMap<String, String>();
    resourceLabels.put("project_id", STACKDRIVER_PROJECT_ID_KEY);
    resourceLabels.put("instance_id", GcpGceInstanceResource.INSTANCE_ID_KEY);
    resourceLabels.put("zone", GcpGceInstanceResource.ZONE_KEY);
    return Collections.unmodifiableMap(resourceLabels);
  }

  private static Map<String, String> getK8sResourceLabelsMappings() {
    Map<String, String> resourceLabels = new LinkedHashMap<String, String>();
    resourceLabels.put("project_id", STACKDRIVER_PROJECT_ID_KEY);
    resourceLabels.put("location", GcpGceInstanceResource.ZONE_KEY);
    resourceLabels.put("instance_id", GcpGceInstanceResource.INSTANCE_ID_KEY);
    resourceLabels.put("cluster_name", K8sContainerResource.CLUSTER_NAME_KEY);
    resourceLabels.put("namespace_name", K8sContainerResource.NAMESPACE_NAME_KEY);
    resourceLabels.put("pod_name", K8sContainerResource.POD_NAME_KEY);
    resourceLabels.put("container_name", K8sContainerResource.CONTAINER_NAME_KEY);
    return Collections.unmodifiableMap(resourceLabels);
  }

  private static Map<String, String> getAwsResourceLabelsMappings() {
    Map<String, String> resourceLabels = new LinkedHashMap<String, String>();
    resourceLabels.put("project_id", STACKDRIVER_PROJECT_ID_KEY);
    resourceLabels.put("instance_id", AwsEc2InstanceResource.INSTANCE_ID_KEY);
    resourceLabels.put("region", AwsEc2InstanceResource.REGION_KEY);
    resourceLabels.put("aws_account", AwsEc2InstanceResource.ACCOUNT_ID_KEY);
    return Collections.unmodifiableMap(resourceLabels);
  }

  private StackdriverExportUtils() {}

  static String exceptionMessage(Throwable e) {
    return e.getMessage() != null ? e.getMessage() : e.getClass().getName();
  }

  static String getDomain(@javax.annotation.Nullable String metricNamePrefix) {
    String domain;
    if (Strings.isNullOrEmpty(metricNamePrefix)) {
      domain = CUSTOM_OPENCENSUS_DOMAIN;
    } else {
      if (!metricNamePrefix.endsWith("/")) {
        domain = metricNamePrefix + '/';
      } else {
        domain = metricNamePrefix;
      }
    }
    return domain;
  }

  static String getDisplayNamePrefix(@javax.annotation.Nullable String metricNamePrefix) {
    if (metricNamePrefix == null) {
      return DEFAULT_DISPLAY_NAME_PREFIX;
    } else {
      if (!metricNamePrefix.endsWith("/") && !metricNamePrefix.isEmpty()) {
        metricNamePrefix += '/';
      }
      return metricNamePrefix;
    }
  }
}
