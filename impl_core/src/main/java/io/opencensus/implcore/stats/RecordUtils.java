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

package io.opencensus.implcore.stats;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableDistribution;
import io.opencensus.implcore.stats.MutableAggregation.MutableLastValue;
import io.opencensus.implcore.stats.MutableAggregation.MutableMean;
import io.opencensus.implcore.stats.MutableAggregation.MutableSum;
import io.opencensus.implcore.tags.TagContextImpl;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.LastValue;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.DistributionData.Exemplar;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Measurement;
import io.opencensus.stats.Measurement.MeasurementDouble;
import io.opencensus.stats.Measurement.MeasurementLong;
import io.opencensus.tags.InternalUtils;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

@SuppressWarnings("deprecation")
/* Common static utilities for stats recording. */
final class RecordUtils {

  @javax.annotation.Nullable @VisibleForTesting static final TagValue UNKNOWN_TAG_VALUE = null;

  static Map<TagKey, TagValue> getTagMap(TagContext ctx) {
    if (ctx instanceof TagContextImpl) {
      return ((TagContextImpl) ctx).getTags();
    } else {
      Map<TagKey, TagValue> tags = Maps.newHashMap();
      for (Iterator<Tag> i = InternalUtils.getTags(ctx); i.hasNext(); ) {
        Tag tag = i.next();
        tags.put(tag.getKey(), tag.getValue());
      }
      return tags;
    }
  }

  @VisibleForTesting
  static List</*@Nullable*/ TagValue> getTagValues(
      Map<? extends TagKey, ? extends TagValue> tags, List<? extends TagKey> columns) {
    List</*@Nullable*/ TagValue> tagValues = new ArrayList</*@Nullable*/ TagValue>(columns.size());
    // Record all the measures in a "Greedy" way.
    // Every view aggregates every measure. This is similar to doing a GROUPBY view’s keys.
    for (int i = 0; i < columns.size(); ++i) {
      TagKey tagKey = columns.get(i);
      if (!tags.containsKey(tagKey)) {
        // replace not found key values by null.
        tagValues.add(UNKNOWN_TAG_VALUE);
      } else {
        tagValues.add(tags.get(tagKey));
      }
    }
    return tagValues;
  }

  /**
   * Create an empty {@link MutableAggregation} based on the given {@link Aggregation}.
   *
   * @param aggregation {@code Aggregation}.
   * @return an empty {@code MutableAggregation}.
   */
  @VisibleForTesting
  static MutableAggregation createMutableAggregation(Aggregation aggregation) {
    return aggregation.match(
        CreateMutableSum.INSTANCE,
        CreateMutableCount.INSTANCE,
        CreateMutableDistribution.INSTANCE,
        CreateMutableLastValue.INSTANCE,
        AggregationDefaultFunction.INSTANCE);
  }

  /**
   * Create an {@link AggregationData} snapshot based on the given {@link MutableAggregation}.
   *
   * @param aggregation {@code MutableAggregation}
   * @param measure {@code Measure}
   * @return an {@code AggregationData} which is the snapshot of current summary statistics.
   */
  @VisibleForTesting
  static AggregationData createAggregationData(MutableAggregation aggregation, Measure measure) {
    return aggregation.match(
        new CreateSumData(measure),
        CreateCountData.INSTANCE,
        CreateMeanData.INSTANCE,
        CreateDistributionData.INSTANCE,
        new CreateLastValueData(measure));
  }

  // Covert a mapping from TagValues to MutableAggregation, to a mapping from TagValues to
  // AggregationData.
  static <T> Map<T, AggregationData> createAggregationMap(
      Map<T, MutableAggregation> tagValueAggregationMap, Measure measure) {
    Map<T, AggregationData> map = Maps.newHashMap();
    for (Entry<T, MutableAggregation> entry : tagValueAggregationMap.entrySet()) {
      map.put(entry.getKey(), createAggregationData(entry.getValue(), measure));
    }
    return map;
  }

  static double getDoubleValueFromMeasurement(Measurement measurement) {
    return measurement.match(
        GET_VALUE_FROM_MEASUREMENT_DOUBLE,
        GET_VALUE_FROM_MEASUREMENT_LONG,
        Functions.<Double>throwAssertionError());
  }

  // static inner Function classes

  private static final Function<MeasurementDouble, Double> GET_VALUE_FROM_MEASUREMENT_DOUBLE =
      new Function<MeasurementDouble, Double>() {
        @Override
        public Double apply(MeasurementDouble arg) {
          return arg.getValue();
        }
      };

  private static final Function<MeasurementLong, Double> GET_VALUE_FROM_MEASUREMENT_LONG =
      new Function<MeasurementLong, Double>() {
        @Override
        public Double apply(MeasurementLong arg) {
          // TODO: consider checking truncation here.
          return (double) arg.getValue();
        }
      };

  private static final class CreateMutableSum implements Function<Sum, MutableAggregation> {
    @Override
    public MutableAggregation apply(Sum arg) {
      return MutableSum.create();
    }

    private static final CreateMutableSum INSTANCE = new CreateMutableSum();
  }

  private static final class CreateMutableCount implements Function<Count, MutableAggregation> {
    @Override
    public MutableAggregation apply(Count arg) {
      return MutableCount.create();
    }

    private static final CreateMutableCount INSTANCE = new CreateMutableCount();
  }

  // TODO(songya): remove this once Mean aggregation is completely removed. Before that
  // we need to continue supporting Mean, since it could still be used by users and some
  // deprecated RPC views.
  private static final class AggregationDefaultFunction
      implements Function<Aggregation, MutableAggregation> {
    @Override
    public MutableAggregation apply(Aggregation arg) {
      if (arg instanceof Aggregation.Mean) {
        return MutableMean.create();
      }
      throw new IllegalArgumentException("Unknown Aggregation.");
    }

    private static final AggregationDefaultFunction INSTANCE = new AggregationDefaultFunction();
  }

  private static final class CreateMutableDistribution
      implements Function<Distribution, MutableAggregation> {
    @Override
    public MutableAggregation apply(Distribution arg) {
      return MutableDistribution.create(arg.getBucketBoundaries());
    }

    private static final CreateMutableDistribution INSTANCE = new CreateMutableDistribution();
  }

  private static final class CreateMutableLastValue
      implements Function<LastValue, MutableAggregation> {
    @Override
    public MutableAggregation apply(LastValue arg) {
      return MutableLastValue.create();
    }

    private static final CreateMutableLastValue INSTANCE = new CreateMutableLastValue();
  }

  private static final class CreateSumData implements Function<MutableSum, AggregationData> {

    private final Measure measure;

    private CreateSumData(Measure measure) {
      this.measure = measure;
    }

    @Override
    public AggregationData apply(final MutableSum arg) {
      return measure.match(
          Functions.<AggregationData>returnConstant(SumDataDouble.create(arg.getSum())),
          Functions.<AggregationData>returnConstant(SumDataLong.create(Math.round(arg.getSum()))),
          Functions.<AggregationData>throwAssertionError());
    }
  }

  private static final class CreateCountData implements Function<MutableCount, AggregationData> {
    @Override
    public AggregationData apply(MutableCount arg) {
      return CountData.create(arg.getCount());
    }

    private static final CreateCountData INSTANCE = new CreateCountData();
  }

  private static final class CreateMeanData implements Function<MutableMean, AggregationData> {
    @Override
    public AggregationData apply(MutableMean arg) {
      return AggregationData.MeanData.create(arg.getMean(), arg.getCount());
    }

    private static final CreateMeanData INSTANCE = new CreateMeanData();
  }

  private static final class CreateDistributionData
      implements Function<MutableDistribution, AggregationData> {
    @Override
    public AggregationData apply(MutableDistribution arg) {
      List<Long> boxedBucketCounts = new ArrayList<Long>();
      for (long bucketCount : arg.getBucketCounts()) {
        boxedBucketCounts.add(bucketCount);
      }
      List<Exemplar> exemplars = new ArrayList<Exemplar>();
      Exemplar[] exemplarArray = arg.getExemplars();
      if (exemplarArray != null) {
        for (Exemplar exemplar : exemplarArray) {
          if (exemplar != null) {
            exemplars.add(exemplar);
          }
        }
      }
      return DistributionData.create(
          arg.getMean(),
          arg.getCount(),
          arg.getMin(),
          arg.getMax(),
          arg.getSumOfSquaredDeviations(),
          boxedBucketCounts,
          exemplars);
    }

    private static final CreateDistributionData INSTANCE = new CreateDistributionData();
  }

  private static final class CreateLastValueData
      implements Function<MutableLastValue, AggregationData> {

    private final Measure measure;

    private CreateLastValueData(Measure measure) {
      this.measure = measure;
    }

    @Override
    public AggregationData apply(final MutableLastValue arg) {
      return measure.match(
          Functions.<AggregationData>returnConstant(LastValueDataDouble.create(arg.getLastValue())),
          Functions.<AggregationData>returnConstant(
              LastValueDataLong.create(Math.round(arg.getLastValue()))),
          Functions.<AggregationData>throwAssertionError());
    }
  }

  private RecordUtils() {}
}
