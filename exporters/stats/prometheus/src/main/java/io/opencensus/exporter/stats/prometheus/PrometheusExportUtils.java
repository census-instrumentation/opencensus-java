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
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * Util methods to convert OpenCensus Stats data models to Prometheus data models.
 *
 * <p>Each OpenCensus {@link View} will be converted to a Prometheus {@link MetricFamilySamples}
 * with no {@link Sample}s, and is used for registering Prometheus {@code Metric}s. Only {@code
 * Cumulative} views are supported. All views are under namespace "opencensus".
 *
 * <p>{@link Aggregation} will be converted to a corresponding Prometheus {@link Type}. {@link Sum}
 * will be {@link Type#UNTYPED}, {@link Count} will be {@link Type#COUNTER}, {@link
 * Aggregation.Mean} will be {@link Type#SUMMARY}, {@link Aggregation.LastValue} will be {@link
 * Type#GAUGE} and {@link Distribution} will be {@link Type#HISTOGRAM}. Please note we cannot set
 * bucket boundaries for custom {@link Type#HISTOGRAM}.
 *
 * <p>Each OpenCensus {@link ViewData} will be converted to a Prometheus {@link
 * MetricFamilySamples}, and each {@code Row} of the {@link ViewData} will be converted to
 * Prometheus {@link Sample}s.
 *
 * <p>{@link SumDataDouble}, {@link SumDataLong}, {@link LastValueDataDouble}, {@link
 * LastValueDataLong} and {@link CountData} will be converted to a single {@link Sample}. {@link
 * AggregationData.MeanData} will be converted to two {@link Sample}s sum and count. {@link
 * DistributionData} will be converted to a list of {@link Sample}s that have the sum, count and
 * histogram buckets.
 *
 * <p>{@link TagKey} and {@link TagValue} will be converted to Prometheus {@code LabelName} and
 * {@code LabelValue}. {@code Null} {@link TagValue} will be converted to an empty string.
 *
 * <p>Please note that Prometheus Metric and Label name can only have alphanumeric characters and
 * underscore. All other characters will be sanitized by underscores.
 */
@SuppressWarnings("deprecation")
final class PrometheusExportUtils {

  @VisibleForTesting static final String OPENCENSUS_NAMESPACE = "opencensus";
  @VisibleForTesting static final String OPENCENSUS_HELP_MSG = "Opencensus Prometheus metrics: ";
  @VisibleForTesting static final String SAMPLE_SUFFIX_BUCKET = "_bucket";
  @VisibleForTesting static final String SAMPLE_SUFFIX_COUNT = "_count";
  @VisibleForTesting static final String SAMPLE_SUFFIX_SUM = "_sum";
  @VisibleForTesting static final String LABEL_NAME_BUCKET_BOUND = "le";

  // Converts a ViewData to a Prometheus MetricFamilySamples.
  static MetricFamilySamples createMetricFamilySamples(ViewData viewData) {
    View view = viewData.getView();
    String name =
        Collector.sanitizeMetricName(OPENCENSUS_NAMESPACE + '_' + view.getName().asString());
    Type type = getType(view.getAggregation(), view.getWindow());
    List<Sample> samples = Lists.newArrayList();
    for (Entry<List</*@Nullable*/ TagValue>, AggregationData> entry :
        viewData.getAggregationMap().entrySet()) {
      samples.addAll(
          getSamples(
              name, view.getColumns(), entry.getKey(), entry.getValue(), view.getAggregation()));
    }
    return new MetricFamilySamples(
        name, type, OPENCENSUS_HELP_MSG + view.getDescription(), samples);
  }

  // Converts a View to a Prometheus MetricFamilySamples.
  // Used only for Prometheus metric registry, should not contain any actual samples.
  static MetricFamilySamples createDescribableMetricFamilySamples(View view) {
    String name =
        Collector.sanitizeMetricName(OPENCENSUS_NAMESPACE + '_' + view.getName().asString());
    Type type = getType(view.getAggregation(), view.getWindow());
    return new MetricFamilySamples(
        name, type, OPENCENSUS_HELP_MSG + view.getDescription(), Collections.<Sample>emptyList());
  }

  @VisibleForTesting
  static Type getType(Aggregation aggregation, View.AggregationWindow window) {
    if (!(window instanceof View.AggregationWindow.Cumulative)) {
      return Type.UNTYPED;
    }
    return aggregation.match(
        Functions.returnConstant(Type.UNTYPED), // SUM
        Functions.returnConstant(Type.COUNTER), // COUNT
        Functions.returnConstant(Type.HISTOGRAM), // DISTRIBUTION
        Functions.returnConstant(Type.GAUGE), // LAST VALUE
        new Function<Aggregation, Type>() {
          @Override
          public Type apply(Aggregation arg) {
            if (arg instanceof Aggregation.Mean) {
              return Type.SUMMARY;
            }
            return Type.UNTYPED;
          }
        });
  }

  @VisibleForTesting
  static List<Sample> getSamples(
      final String name,
      List<TagKey> tagKeys,
      List</*@Nullable*/ TagValue> tagValues,
      AggregationData aggregationData,
      final Aggregation aggregation) {
    Preconditions.checkArgument(
        tagKeys.size() == tagValues.size(), "Tag keys and tag values have different sizes.");
    final List<Sample> samples = Lists.newArrayList();
    final List<String> labelNames = new ArrayList<String>(tagKeys.size());
    final List<String> labelValues = new ArrayList<String>(tagValues.size());
    for (TagKey tagKey : tagKeys) {
      labelNames.add(Collector.sanitizeMetricName(tagKey.getName()));
    }
    for (TagValue tagValue : tagValues) {
      String labelValue = tagValue == null ? "" : tagValue.asString();
      labelValues.add(labelValue);
    }

    aggregationData.match(
        new Function<SumDataDouble, Void>() {
          @Override
          public Void apply(SumDataDouble arg) {
            samples.add(new Sample(name, labelNames, labelValues, arg.getSum()));
            return null;
          }
        },
        new Function<SumDataLong, Void>() {
          @Override
          public Void apply(SumDataLong arg) {
            samples.add(new Sample(name, labelNames, labelValues, arg.getSum()));
            return null;
          }
        },
        new Function<CountData, Void>() {
          @Override
          public Void apply(CountData arg) {
            samples.add(new Sample(name, labelNames, labelValues, arg.getCount()));
            return null;
          }
        },
        new Function<DistributionData, Void>() {
          @Override
          public Void apply(DistributionData arg) {
            // For histogram buckets, manually add the bucket boundaries as "le" labels.
            // See io.prometheus.client.Histogram.java#L329.
            checkLeLabelInLabelNames(labelNames);
            @SuppressWarnings("unchecked")
            Distribution distribution = (Distribution) aggregation;
            List<Double> boundaries = distribution.getBucketBoundaries().getBoundaries();
            List<String> labelNamesWithLe = new ArrayList<String>(labelNames);
            labelNamesWithLe.add(LABEL_NAME_BUCKET_BOUND);
            for (int i = 0; i < arg.getBucketCounts().size(); i++) {
              List<String> labelValuesWithLe = new ArrayList<String>(labelValues);
              // The label value of "le" is the upper inclusive bound.
              // For the last bucket, it should be "+Inf".
              String bucketBoundary =
                  doubleToGoString(
                      i < boundaries.size() ? boundaries.get(i) : Double.POSITIVE_INFINITY);
              labelValuesWithLe.add(bucketBoundary);
              samples.add(
                  new MetricFamilySamples.Sample(
                      name + SAMPLE_SUFFIX_BUCKET,
                      labelNamesWithLe,
                      labelValuesWithLe,
                      arg.getBucketCounts().get(i)));
            }

            samples.add(
                new MetricFamilySamples.Sample(
                    name + SAMPLE_SUFFIX_COUNT, labelNames, labelValues, arg.getCount()));
            samples.add(
                new MetricFamilySamples.Sample(
                    name + SAMPLE_SUFFIX_SUM,
                    labelNames,
                    labelValues,
                    arg.getCount() * arg.getMean()));
            return null;
          }
        },
        new Function<LastValueDataDouble, Void>() {
          @Override
          public Void apply(LastValueDataDouble arg) {
            samples.add(new Sample(name, labelNames, labelValues, arg.getLastValue()));
            return null;
          }
        },
        new Function<LastValueDataLong, Void>() {
          @Override
          public Void apply(LastValueDataLong arg) {
            samples.add(new Sample(name, labelNames, labelValues, arg.getLastValue()));
            return null;
          }
        },
        new Function<AggregationData, Void>() {
          @Override
          public Void apply(AggregationData arg) {
            // TODO(songya): remove this once Mean aggregation is completely removed. Before that
            // we need to continue supporting Mean, since it could still be used by users and some
            // deprecated RPC views.
            if (arg instanceof AggregationData.MeanData) {
              AggregationData.MeanData meanData = (AggregationData.MeanData) arg;
              samples.add(
                  new MetricFamilySamples.Sample(
                      name + SAMPLE_SUFFIX_COUNT, labelNames, labelValues, meanData.getCount()));
              samples.add(
                  new MetricFamilySamples.Sample(
                      name + SAMPLE_SUFFIX_SUM,
                      labelNames,
                      labelValues,
                      meanData.getCount() * meanData.getMean()));
              return null;
            }
            throw new IllegalArgumentException("Unknown Aggregation.");
          }
        });

    return samples;
  }

  // Similar check to io.prometheus.client.Histogram.java#L86.
  private static void checkLeLabelInLabelNames(List<String> labelNames) {
    for (String label : labelNames) {
      if (LABEL_NAME_BUCKET_BOUND.equals(label)) {
        throw new IllegalStateException(
            "Prometheus Histogram cannot have a label named 'le', "
                + "because it is a reserved label for bucket boundaries. "
                + "Please remove this tag key from your view.");
      }
    }
  }

  private PrometheusExportUtils() {}
}
