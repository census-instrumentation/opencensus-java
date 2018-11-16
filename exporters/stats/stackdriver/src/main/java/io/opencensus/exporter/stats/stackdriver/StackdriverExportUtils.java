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
import com.google.api.LabelDescriptor;
import com.google.api.LabelDescriptor.ValueType;
import com.google.api.Metric;
import com.google.api.MetricDescriptor;
import com.google.api.MetricDescriptor.MetricKind;
import com.google.api.MonitoredResource;
import com.google.cloud.MetadataConfig;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.Timestamp;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.contrib.monitoredresource.util.MonitoredResourceUtils;
import io.opencensus.contrib.monitoredresource.util.ResourceKeyConstants;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.Distribution.BucketOptions.ExplicitOptions;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Value;
import io.opencensus.resource.Resource;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
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
  private static final String GCP_GKE_CONTAINER = "k8s_container";
  private static final String GCP_GCE_INSTANCE = "gce_instance";
  private static final String AWS_EC2_INSTANCE = "aws_ec2_instance";
  private static final String GLOBAL = "global";

  private static final Logger logger = Logger.getLogger(StackdriverExportUtils.class.getName());
  private static final String OPENCENSUS_TASK_VALUE_DEFAULT = generateDefaultTaskValue();
  private static final String PROJECT_ID_LABEL_KEY = "project_id";

  // Mappings for the well-known OC resources to applicable Stackdriver resources.
  private static final Map<String, String> GCP_RESOURCE_MAPPING = getGcpResourceLabelsMappings();
  private static final Map<String, String> GKE_RESOURCE_MAPPING = getGkeResourceLabelsMappings();
  private static final Map<String, String> AWS_RESOURCE_MAPPING = getAwsResourceLabelsMappings();

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
          // StackDriver doesn't handle Summary value.
          // TODO(mayurkale): decide what to do with Summary value.
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
    // TODO(mayurkale): decide what to do with Summary type.
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
      String domain) {
    List<TimeSeries> timeSeriesList = Lists.newArrayList();
    io.opencensus.metrics.export.MetricDescriptor metricDescriptor = metric.getMetricDescriptor();

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
    return Distribution.newBuilder()
        .setBucketOptions(createBucketOptions(distribution.getBucketOptions()))
        .addAllBucketCounts(createBucketCounts(distribution.getBuckets()))
        .setCount(distribution.getCount())
        .setMean(distribution.getCount() == 0 ? 0 : distribution.getSum() / distribution.getCount())
        .setSumOfSquaredDeviation(distribution.getSumOfSquaredDeviations())
        .build();
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

  // Convert a OpenCensus Buckets to a list of counts
  private static List<Long> createBucketCounts(List<Bucket> buckets) {
    List<Long> bucketCounts = new ArrayList<>();
    // The first bucket (underflow bucket) should always be 0 count because the Metrics first bucket
    // is [0, first_bound) but StackDriver distribution consists of an underflow bucket (number 0).
    bucketCounts.add(0L);
    for (Bucket bucket : buckets) {
      bucketCounts.add(bucket.getCount());
    }
    return bucketCounts;
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
    Resource autoDetectedResource = MonitoredResourceUtils.detectResource();
    if (autoDetectedResource == null || autoDetectedResource.getType() == null) {
      builder.setType(GLOBAL);
      if (MetadataConfig.getProjectId() != null) {
        // For default global resource, always use the project id from MetadataConfig. This allows
        // stats from other projects (e.g from GAE running in another project) to be collected.
        builder.putLabels(PROJECT_ID_LABEL_KEY, MetadataConfig.getProjectId());
      }
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

    Map<String, String> mappings = null;
    switch (type) {
      case ResourceKeyConstants.GCP_GCE_INSTANCE_TYPE:
        builder.setType(GCP_GCE_INSTANCE);
        mappings = GCP_RESOURCE_MAPPING;
        break;
      case ResourceKeyConstants.GCP_GKE_INSTANCE_TYPE:
        builder.setType(GCP_GKE_CONTAINER);
        mappings = GKE_RESOURCE_MAPPING;
        break;
      case ResourceKeyConstants.AWS_EC2_INSTANCE_TYPE:
        builder.setType(AWS_EC2_INSTANCE);
        mappings = AWS_RESOURCE_MAPPING;
        break;
      default:
        builder.setType(GLOBAL);
        return;
    }

    if (mappings != null) {
      Map<String, String> resLabels = autoDetectedResource.getLabels();
      for (Map.Entry<String, String> entry : mappings.entrySet()) {
        if (resLabels.containsKey(entry.getValue())) {
          builder.putLabels(entry.getKey(), resLabels.get(entry.getValue()));
        }
      }
      return;
    }

    throw new IllegalArgumentException("Unknown subclass of MonitoredResource.");
  }

  private static Map<String, String> getGcpResourceLabelsMappings() {
    Map<String, String> resourceLabels = new LinkedHashMap<String, String>();
    resourceLabels.put(PROJECT_ID_LABEL_KEY, ResourceKeyConstants.GCP_ACCOUNT_ID_KEY);
    resourceLabels.put("instance_id", ResourceKeyConstants.GCP_INSTANCE_ID_KEY);
    resourceLabels.put("zone", ResourceKeyConstants.GCP_ZONE_KEY);
    return resourceLabels;
  }

  private static Map<String, String> getGkeResourceLabelsMappings() {
    Map<String, String> resourceLabels = new LinkedHashMap<String, String>();
    resourceLabels.put(PROJECT_ID_LABEL_KEY, ResourceKeyConstants.GCP_ACCOUNT_ID_KEY);
    resourceLabels.put("location", ResourceKeyConstants.GCP_GKE_ZONE_KEY);
    resourceLabels.put("cluster_name", ResourceKeyConstants.GCP_GKE_CLUSTER_KEY);
    resourceLabels.put("namespace_name", ResourceKeyConstants.GCP_GKE_NAMESPACE_ID_KEY);
    resourceLabels.put("pod_name", ResourceKeyConstants.GCP_GKE_POD_ID_KEY);
    resourceLabels.put("container_name", ResourceKeyConstants.GCP_GKE_CONTAINER_KEY);
    return resourceLabels;
  }

  private static Map<String, String> getAwsResourceLabelsMappings() {
    Map<String, String> resourceLabels = new LinkedHashMap<String, String>();
    resourceLabels.put("instance_id", ResourceKeyConstants.AWS_INSTANCE_ID_KEY);
    resourceLabels.put("region", ResourceKeyConstants.AWS_REGION_KEY);
    resourceLabels.put("aws_account", ResourceKeyConstants.AWS_ACCOUNT_KEY);
    return resourceLabels;
  }

  private StackdriverExportUtils() {}
}
