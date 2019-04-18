/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.exporter.stats.prometheus;

import static io.prometheus.client.Collector.doubleToGoString;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Distribution;
import io.opencensus.metrics.export.Distribution.BucketOptions;
import io.opencensus.metrics.export.Distribution.BucketOptions.ExplicitOptions;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.Value;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * Util methods to convert OpenCensus Metrics data models to Prometheus data models.
 *
 * <p>Each OpenCensus {@link MetricDescriptor} will be converted to a Prometheus {@link
 * MetricFamilySamples} with no {@link Sample}s, and is used for registering Prometheus {@code
 * Metric}s.
 *
 * <p>Each OpenCensus {@link Metric} will be converted to a Prometheus {@link MetricFamilySamples},
 * and each {@code Point} of the {@link Metric} will be converted to Prometheus {@link Sample}s.
 *
 * <p>{@code io.opencensus.metrics.export.Value.ValueDouble}, {@code
 * io.opencensus.metrics.export.Value.ValueLong} will be converted to a single {@link Sample}.
 * {@code io.opencensus.metrics.export.Value.ValueSummary} will be converted to two {@code Sample}s
 * sum and count. {@code io.opencensus.metrics.export.Value.ValueDistribution} will be converted to
 * a list of {@link Sample}s that have the sum, count and histogram buckets.
 *
 * <p>{@link LabelKey} and {@link LabelValue} will be converted to Prometheus {@code LabelName} and
 * {@code LabelValue}. {@code Null} {@link LabelValue} will be converted to an empty string.
 *
 * <p>Please note that Prometheus Metric and Label name can only have alphanumeric characters and
 * underscore. All other characters will be sanitized by underscores.
 */
final class PrometheusExportUtils {

  @VisibleForTesting static final String SAMPLE_SUFFIX_BUCKET = "_bucket";
  @VisibleForTesting static final String SAMPLE_SUFFIX_COUNT = "_count";
  @VisibleForTesting static final String SAMPLE_SUFFIX_SUM = "_sum";
  @VisibleForTesting static final String LABEL_NAME_BUCKET_BOUND = "le";
  @VisibleForTesting static final String LABEL_NAME_QUANTILE = "quantile";

  // Converts a Metric to a Prometheus MetricFamilySamples.
  static MetricFamilySamples createMetricFamilySamples(Metric metric, String namespace) {
    MetricDescriptor metricDescriptor = metric.getMetricDescriptor();
    String name = getNamespacedName(metricDescriptor.getName(), namespace);
    Type type = getType(metricDescriptor.getType());
    List<String> labelNames = convertToLabelNames(metricDescriptor.getLabelKeys());
    List<Sample> samples = Lists.newArrayList();

    for (io.opencensus.metrics.export.TimeSeries timeSeries : metric.getTimeSeriesList()) {
      for (io.opencensus.metrics.export.Point point : timeSeries.getPoints()) {
        samples.addAll(getSamples(name, labelNames, timeSeries.getLabelValues(), point.getValue()));
      }
    }
    return new MetricFamilySamples(name, type, metricDescriptor.getDescription(), samples);
  }

  // Converts a MetricDescriptor to a Prometheus MetricFamilySamples.
  // Used only for Prometheus metric registry, should not contain any actual samples.
  static MetricFamilySamples createDescribableMetricFamilySamples(
      MetricDescriptor metricDescriptor, String namespace) {
    String name = getNamespacedName(metricDescriptor.getName(), namespace);
    Type type = getType(metricDescriptor.getType());
    List<String> labelNames = convertToLabelNames(metricDescriptor.getLabelKeys());

    if (containsDisallowedLeLabelForHistogram(labelNames, type)) {
      throw new IllegalStateException(
          "Prometheus Histogram cannot have a label named 'le', "
              + "because it is a reserved label for bucket boundaries. "
              + "Please remove this key from your view.");
    }

    if (containsDisallowedQuantileLabelForSummary(labelNames, type)) {
      throw new IllegalStateException(
          "Prometheus Summary cannot have a label named 'quantile', "
              + "because it is a reserved label. Please remove this key from your view.");
    }

    return new MetricFamilySamples(
        name, type, metricDescriptor.getDescription(), Collections.<Sample>emptyList());
  }

  private static String getNamespacedName(String metricName, String namespace) {
    if (!namespace.isEmpty()) {
      if (!namespace.endsWith("/") && !namespace.endsWith("_")) {
        namespace += '_';
      }
      metricName = namespace + metricName;
    }
    return Collector.sanitizeMetricName(metricName);
  }

  @VisibleForTesting
  static Type getType(MetricDescriptor.Type type) {
    if (type == MetricDescriptor.Type.CUMULATIVE_INT64
        || type == MetricDescriptor.Type.CUMULATIVE_DOUBLE) {
      return Type.COUNTER;
    } else if (type == MetricDescriptor.Type.GAUGE_INT64
        || type == MetricDescriptor.Type.GAUGE_DOUBLE) {
      return Type.GAUGE;
    } else if (type == MetricDescriptor.Type.CUMULATIVE_DISTRIBUTION
        || type == MetricDescriptor.Type.GAUGE_DISTRIBUTION) {
      return Type.HISTOGRAM;
    } else if (type == MetricDescriptor.Type.SUMMARY) {
      return Type.SUMMARY;
    }
    return Type.UNTYPED;
  }

  // Converts a point value in Metric to a list of Prometheus Samples.
  @VisibleForTesting
  static List<Sample> getSamples(
      final String name,
      final List<String> labelNames,
      List<LabelValue> labelValuesList,
      Value value) {
    Preconditions.checkArgument(
        labelNames.size() == labelValuesList.size(), "Keys and Values don't have same size.");
    final List<Sample> samples = Lists.newArrayList();

    final List<String> labelValues = new ArrayList<String>(labelValuesList.size());
    for (LabelValue labelValue : labelValuesList) {
      String val = labelValue == null ? "" : labelValue.getValue();
      labelValues.add(val == null ? "" : val);
    }

    return value.match(
        new Function<Double, List<Sample>>() {
          @Override
          public List<Sample> apply(Double arg) {
            samples.add(new Sample(name, labelNames, labelValues, arg));
            return samples;
          }
        },
        new Function<Long, List<Sample>>() {
          @Override
          public List<Sample> apply(Long arg) {
            samples.add(new Sample(name, labelNames, labelValues, arg));
            return samples;
          }
        },
        new Function<Distribution, List<Sample>>() {
          @Override
          public List<Sample> apply(final Distribution arg) {
            BucketOptions bucketOptions = arg.getBucketOptions();
            List<Double> boundaries = new ArrayList<>();

            if (bucketOptions != null) {
              boundaries =
                  bucketOptions.match(
                      new Function<ExplicitOptions, List<Double>>() {
                        @Override
                        public List<Double> apply(ExplicitOptions arg) {
                          return arg.getBucketBoundaries();
                        }
                      },
                      Functions.<List<Double>>throwIllegalArgumentException());
            }

            List<String> labelNamesWithLe = new ArrayList<String>(labelNames);
            labelNamesWithLe.add(LABEL_NAME_BUCKET_BOUND);
            long cumulativeCount = 0;

            for (int i = 0; i < arg.getBuckets().size(); i++) {
              List<String> labelValuesWithLe = new ArrayList<String>(labelValues);
              // The label value of "le" is the upper inclusive bound.
              // For the last bucket, it should be "+Inf".
              String bucketBoundary =
                  doubleToGoString(
                      i < boundaries.size() ? boundaries.get(i) : Double.POSITIVE_INFINITY);
              labelValuesWithLe.add(bucketBoundary);
              cumulativeCount += arg.getBuckets().get(i).getCount();
              samples.add(
                  new MetricFamilySamples.Sample(
                      name + SAMPLE_SUFFIX_BUCKET,
                      labelNamesWithLe,
                      labelValuesWithLe,
                      cumulativeCount));
            }

            samples.add(
                new MetricFamilySamples.Sample(
                    name + SAMPLE_SUFFIX_COUNT, labelNames, labelValues, arg.getCount()));
            samples.add(
                new MetricFamilySamples.Sample(
                    name + SAMPLE_SUFFIX_SUM, labelNames, labelValues, arg.getSum()));
            return samples;
          }
        },
        new Function<Summary, List<Sample>>() {
          @Override
          public List<Sample> apply(Summary arg) {
            Long count = arg.getCount();
            if (count != null) {
              samples.add(
                  new MetricFamilySamples.Sample(
                      name + SAMPLE_SUFFIX_COUNT, labelNames, labelValues, count));
            }
            Double sum = arg.getSum();
            if (sum != null) {
              samples.add(
                  new MetricFamilySamples.Sample(
                      name + SAMPLE_SUFFIX_SUM, labelNames, labelValues, sum));
            }

            List<ValueAtPercentile> valueAtPercentiles = arg.getSnapshot().getValueAtPercentiles();
            List<String> labelNamesWithQuantile = new ArrayList<String>(labelNames);
            labelNamesWithQuantile.add(LABEL_NAME_QUANTILE);
            for (ValueAtPercentile valueAtPercentile : valueAtPercentiles) {
              List<String> labelValuesWithQuantile = new ArrayList<String>(labelValues);
              labelValuesWithQuantile.add(
                  doubleToGoString(valueAtPercentile.getPercentile() / 100));
              samples.add(
                  new MetricFamilySamples.Sample(
                      name,
                      labelNamesWithQuantile,
                      labelValuesWithQuantile,
                      valueAtPercentile.getValue()));
            }
            return samples;
          }
        },
        Functions.<List<Sample>>throwIllegalArgumentException());
  }

  // Converts the list of label keys to a list of string label names. Also sanitizes the label keys.
  @VisibleForTesting
  static List<String> convertToLabelNames(List<LabelKey> labelKeys) {
    final List<String> labelNames = new ArrayList<String>(labelKeys.size());
    for (LabelKey labelKey : labelKeys) {
      labelNames.add(Collector.sanitizeMetricName(labelKey.getKey()));
    }
    return labelNames;
  }

  // Returns true if there is an "le" label name in histogram label names, returns false otherwise.
  // Similar check to
  // https://github.com/prometheus/client_java/blob/af39ca948ca446757f14d8da618a72d18a46ef3d/simpleclient/src/main/java/io/prometheus/client/Histogram.java#L88
  static boolean containsDisallowedLeLabelForHistogram(List<String> labelNames, Type type) {
    if (!Type.HISTOGRAM.equals(type)) {
      return false;
    }
    for (String label : labelNames) {
      if (LABEL_NAME_BUCKET_BOUND.equals(label)) {
        return true;
      }
    }
    return false;
  }

  // Returns true if there is an "quantile" label name in summary label names, returns false
  // otherwise. Similar check to
  // https://github.com/prometheus/client_java/blob/af39ca948ca446757f14d8da618a72d18a46ef3d/simpleclient/src/main/java/io/prometheus/client/Summary.java#L132
  static boolean containsDisallowedQuantileLabelForSummary(List<String> labelNames, Type type) {
    if (!Type.SUMMARY.equals(type)) {
      return false;
    }
    for (String label : labelNames) {
      if (LABEL_NAME_QUANTILE.equals(label)) {
        return true;
      }
    }
    return false;
  }

  private PrometheusExportUtils() {}
}
