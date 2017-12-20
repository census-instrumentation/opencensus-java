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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.api.Distribution;
import com.google.api.Distribution.BucketOptions;
import com.google.api.Distribution.BucketOptions.Explicit;
import com.google.api.LabelDescriptor;
import com.google.api.LabelDescriptor.ValueType;
import com.google.api.Metric;
import com.google.api.MetricDescriptor;
import com.google.api.MetricDescriptor.MetricKind;
import com.google.api.MonitoredResource;
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
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Util methods to convert OpenCensus Stats data models to StackDriver monitoring data models. */
final class StackdriverExportUtils {
  // TODO(songya): do we want these constants to be customizable?
  @VisibleForTesting static final String LABEL_DESCRIPTION = "OpenCensus TagKey";
  @VisibleForTesting static final String OPENCENSUS_TASK = "opencensus_task";
  @VisibleForTesting static final String OPENCENSUS_TASK_DESCRIPTION = "Opencensus task identifier";

  private static final Logger logger = Logger.getLogger(StackdriverExportUtils.class.getName());
  private static final String CUSTOM_METRIC_DOMAIN = "custom.googleapis.com";
  private static final String CUSTOM_OPENCENSUS_DOMAIN = CUSTOM_METRIC_DOMAIN + "/opencensus/";
  private static final String OPENCENSUS_TASK_VALUE_DEFAULT = generateDefaultTaskValue();

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

  // Construct a MetricDescriptor using a View.
  @javax.annotation.Nullable
  static MetricDescriptor createMetricDescriptor(View view, String projectId) {
    if (!(view.getWindow() instanceof Cumulative)) {
      // TODO(songya): Only Cumulative view will be exported to Stackdriver in this version.
      return null;
    }

    MetricDescriptor.Builder builder = MetricDescriptor.newBuilder();
    String viewName = view.getName().asString();
    String type = generateType(viewName);
    // Name format refers to
    // cloud.google.com/monitoring/api/ref_v3/rest/v3/projects.metricDescriptors/create
    builder.setName(String.format("projects/%s/metricDescriptors/%s", projectId, type));
    builder.setType(type);
    builder.setDescription(view.getDescription());
    builder.setUnit(view.getMeasure().getUnit());
    builder.setDisplayName("OpenCensus/" + viewName);
    for (TagKey tagKey : view.getColumns()) {
      builder.addLabels(createLabelDescriptor(tagKey));
    }
    builder.addLabels(
        LabelDescriptor.newBuilder()
            .setKey(OPENCENSUS_TASK)
            .setDescription(OPENCENSUS_TASK_DESCRIPTION)
            .setValueType(ValueType.STRING)
            .build());
    builder.setMetricKind(createMetricKind(view.getWindow()));
    builder.setValueType(createValueType(view.getAggregation(), view.getMeasure()));
    return builder.build();
  }

  private static String generateType(String viewName) {
    return CUSTOM_OPENCENSUS_DOMAIN + viewName;
  }

  // Construct a LabelDescriptor from a TagKey
  @VisibleForTesting
  static LabelDescriptor createLabelDescriptor(TagKey tagKey) {
    LabelDescriptor.Builder builder = LabelDescriptor.newBuilder();
    builder.setKey(tagKey.getName());
    builder.setDescription(LABEL_DESCRIPTION);
    // Now we only support String tags
    builder.setValueType(ValueType.STRING);
    return builder.build();
  }

  // Construct a MetricKind from an AggregationWindow
  @VisibleForTesting
  static MetricKind createMetricKind(AggregationWindow window) {
    return window.match(
        Functions.returnConstant(MetricKind.CUMULATIVE), // Cumulative
        // TODO(songya): We don't support exporting Interval stats to StackDriver in this version.
        Functions.returnConstant(MetricKind.UNRECOGNIZED), // Interval
        Functions.returnConstant(MetricKind.UNRECOGNIZED));
  }

  // Construct a MetricDescriptor.ValueType from an Aggregation and a Measure
  @VisibleForTesting
  static MetricDescriptor.ValueType createValueType(
      Aggregation aggregation, final Measure measure) {
    return aggregation.match(
        Functions.returnConstant(
            measure.match(
                Functions.returnConstant(MetricDescriptor.ValueType.DOUBLE), // Sum Double
                Functions.returnConstant(MetricDescriptor.ValueType.INT64), // Sum Long
                Functions.returnConstant(MetricDescriptor.ValueType.UNRECOGNIZED))),
        Functions.returnConstant(MetricDescriptor.ValueType.INT64), // Count
        Functions.returnConstant(MetricDescriptor.ValueType.DOUBLE), // Mean
        Functions.returnConstant(MetricDescriptor.ValueType.DISTRIBUTION), // Distribution
        Functions.returnConstant(MetricDescriptor.ValueType.UNRECOGNIZED));
  }

  // Convert ViewData to a list of TimeSeries, so that ViewData can be uploaded to Stackdriver.
  static List<TimeSeries> createTimeSeriesList(
      @javax.annotation.Nullable ViewData viewData, MonitoredResource monitoredResource) {
    List<TimeSeries> timeSeriesList = Lists.newArrayList();
    if (viewData == null) {
      return timeSeriesList;
    }
    View view = viewData.getView();
    if (!(view.getWindow() instanceof Cumulative)) {
      // TODO(songya): Only Cumulative view will be exported to Stackdriver in this version.
      return timeSeriesList;
    }

    // Shared fields for all TimeSeries generated from the same ViewData
    TimeSeries.Builder shared = TimeSeries.newBuilder();
    shared.setMetricKind(createMetricKind(view.getWindow()));
    shared.setResource(monitoredResource);
    shared.setValueType(createValueType(view.getAggregation(), view.getMeasure()));

    // Each entry in AggregationMap will be converted into an independent TimeSeries object
    for (Entry<List<TagValue>, AggregationData> entry : viewData.getAggregationMap().entrySet()) {
      TimeSeries.Builder builder = shared.clone();
      builder.setMetric(createMetric(view, entry.getKey()));
      builder.addPoints(
          createPoint(entry.getValue(), viewData.getWindowData(), view.getAggregation()));
      timeSeriesList.add(builder.build());
    }

    return timeSeriesList;
  }

  // Create a Metric using the TagKeys and TagValues.
  @VisibleForTesting
  static Metric createMetric(View view, List<? extends TagValue> tagValues) {
    Metric.Builder builder = Metric.newBuilder();
    // TODO(songya): use pre-defined metrics for canonical views
    builder.setType(generateType(view.getName().asString()));
    Map<String, String> stringTagMap = Maps.newHashMap();
    List<TagKey> columns = view.getColumns();
    checkArgument(
        tagValues.size() == columns.size(), "TagKeys and TagValues don't have same size.");
    for (int i = 0; i < tagValues.size(); i++) {
      TagKey key = columns.get(i);
      TagValue value = tagValues.get(i);
      if (value == null) {
        continue;
      }
      stringTagMap.put(key.getName(), value.asString());
    }
    stringTagMap.put(OPENCENSUS_TASK, OPENCENSUS_TASK_VALUE_DEFAULT);
    builder.putAllLabels(stringTagMap);
    return builder.build();
  }

  // Create Point from AggregationData, AggregationWindowData and Aggregation.
  @VisibleForTesting
  static Point createPoint(
      AggregationData aggregationData, AggregationWindowData windowData, Aggregation aggregation) {
    Point.Builder builder = Point.newBuilder();
    builder.setInterval(createTimeInterval(windowData));
    builder.setValue(createTypedValue(aggregation, aggregationData));
    return builder.build();
  }

  // Convert AggregationWindowData to TimeInterval, currently only support CumulativeData.
  @VisibleForTesting
  static TimeInterval createTimeInterval(AggregationWindowData windowData) {
    final TimeInterval.Builder builder = TimeInterval.newBuilder();
    windowData.match(
        new Function<CumulativeData, Void>() {
          @Override
          public Void apply(CumulativeData arg) {
            builder.setStartTime(convertTimestamp(arg.getStart()));
            builder.setEndTime(convertTimestamp(arg.getEnd()));
            return null;
          }
        },
        new Function<IntervalData, Void>() {
          @Override
          public Void apply(IntervalData arg) {
            // TODO(songya): we don't export IntervalData in this version.
            throw new IllegalArgumentException("IntervalData not supported");
          }
        },
        Functions.</*@Nullable*/ Void>throwIllegalArgumentException());
    return builder.build();
  }

  // Create a TypedValue using AggregationData and Aggregation
  // Note TypedValue is "A single strongly-typed value", i.e only one field should be set.
  @VisibleForTesting
  static TypedValue createTypedValue(
      final Aggregation aggregation, AggregationData aggregationData) {
    final TypedValue.Builder builder = TypedValue.newBuilder();
    aggregationData.match(
        new Function<SumDataDouble, Void>() {
          @Override
          public Void apply(SumDataDouble arg) {
            builder.setDoubleValue(arg.getSum());
            return null;
          }
        },
        new Function<SumDataLong, Void>() {
          @Override
          public Void apply(SumDataLong arg) {
            builder.setInt64Value(arg.getSum());
            return null;
          }
        },
        new Function<CountData, Void>() {
          @Override
          public Void apply(CountData arg) {
            builder.setInt64Value(arg.getCount());
            return null;
          }
        },
        new Function<MeanData, Void>() {
          @Override
          public Void apply(MeanData arg) {
            builder.setDoubleValue(arg.getMean());
            return null;
          }
        },
        new Function<DistributionData, Void>() {
          @Override
          public Void apply(DistributionData arg) {
            checkArgument(
                aggregation instanceof Aggregation.Distribution,
                "Aggregation and AggregationData mismatch.");
            builder.setDistributionValue(
                createDistribution(
                    arg, ((Aggregation.Distribution) aggregation).getBucketBoundaries()));
            return null;
          }
        },
        Functions.</*@Nullable*/ Void>throwIllegalArgumentException());
    return builder.build();
  }

  // Create a StackDriver Distribution from DistributionData and BucketBoundaries
  @VisibleForTesting
  static Distribution createDistribution(
      DistributionData distributionData, BucketBoundaries bucketBoundaries) {
    return Distribution.newBuilder()
        .setBucketOptions(createBucketOptions(bucketBoundaries))
        .addAllBucketCounts(distributionData.getBucketCounts())
        .setCount(distributionData.getCount())
        .setMean(distributionData.getMean())
        // TODO(songya): uncomment this once Stackdriver supports setting max and min.
        // .setRange(
        //    Range.newBuilder()
        //        .setMax(distributionData.getMax())
        //        .setMin(distributionData.getMin())
        //        .build())
        .setSumOfSquaredDeviation(distributionData.getSumOfSquaredDeviations())
        .build();
  }

  // Create BucketOptions from BucketBoundaries
  @VisibleForTesting
  static BucketOptions createBucketOptions(BucketBoundaries bucketBoundaries) {
    return BucketOptions.newBuilder()
        .setExplicitBuckets(Explicit.newBuilder().addAllBounds(bucketBoundaries.getBoundaries()))
        .build();
  }

  // Convert a Census Timestamp to a StackDriver Timestamp
  @VisibleForTesting
  static Timestamp convertTimestamp(io.opencensus.common.Timestamp censusTimestamp) {
    return Timestamp.newBuilder()
        .setSeconds(censusTimestamp.getSeconds())
        .setNanos(censusTimestamp.getNanos())
        .build();
  }

  private StackdriverExportUtils() {}
}
