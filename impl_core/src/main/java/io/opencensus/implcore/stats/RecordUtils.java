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
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableDistribution;
import io.opencensus.implcore.stats.MutableAggregation.MutableLastValueDouble;
import io.opencensus.implcore.stats.MutableAggregation.MutableLastValueLong;
import io.opencensus.implcore.stats.MutableAggregation.MutableMean;
import io.opencensus.implcore.stats.MutableAggregation.MutableSumDouble;
import io.opencensus.implcore.stats.MutableAggregation.MutableSumLong;
import io.opencensus.implcore.tags.TagMapImpl;
import io.opencensus.implcore.tags.TagValueWithMetadata;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.LastValue;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
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

  // TODO(songy23): remove the mapping once we completely remove the deprecated RPC constants.
  @VisibleForTesting static final TagKey RPC_STATUS = TagKey.create("canonical_status");
  @VisibleForTesting static final TagKey RPC_METHOD = TagKey.create("method");
  @VisibleForTesting static final TagKey GRPC_CLIENT_STATUS = TagKey.create("grpc_client_status");
  @VisibleForTesting static final TagKey GRPC_CLIENT_METHOD = TagKey.create("grpc_client_method");
  private static final TagKey GRPC_SERVER_STATUS = TagKey.create("grpc_server_status");
  private static final TagKey GRPC_SERVER_METHOD = TagKey.create("grpc_server_method");
  private static final Multimap<TagKey, TagKey> RPC_TAG_MAPPINGS =
      ImmutableMultimap.<TagKey, TagKey>builder()
          .put(RPC_STATUS, GRPC_CLIENT_STATUS)
          .put(RPC_STATUS, GRPC_SERVER_STATUS)
          .put(RPC_METHOD, GRPC_CLIENT_METHOD)
          .put(RPC_METHOD, GRPC_SERVER_METHOD)
          .build();

  static Map<TagKey, TagValueWithMetadata> getTagMap(TagContext ctx) {
    if (ctx instanceof TagMapImpl) {
      return ((TagMapImpl) ctx).getTags();
    }
    Map<TagKey, TagValueWithMetadata> tags = Maps.newHashMap();
    for (Iterator<Tag> i = InternalUtils.getTags(ctx); i.hasNext(); ) {
      Tag tag = i.next();
      tags.put(tag.getKey(), TagValueWithMetadata.create(tag.getValue(), tag.getTagMetadata()));
    }
    return tags;
  }

  @VisibleForTesting
  static List</*@Nullable*/ TagValue> getTagValues(
      Map<? extends TagKey, TagValueWithMetadata> tags, List<? extends TagKey> columns) {
    List</*@Nullable*/ TagValue> tagValues = new ArrayList</*@Nullable*/ TagValue>(columns.size());
    // Record all the measures in a "Greedy" way.
    // Every view aggregates every measure. This is similar to doing a GROUPBY view’s keys.
    for (int i = 0; i < columns.size(); ++i) {
      TagKey tagKey = columns.get(i);
      if (!tags.containsKey(tagKey)) {
        @javax.annotation.Nullable TagValue tagValue = UNKNOWN_TAG_VALUE;
        if (RPC_TAG_MAPPINGS.containsKey(tagKey)) {
          tagValue = getTagValueForDeprecatedRpcTag(tags, tagKey);
        }
        tagValues.add(tagValue);
      } else {
        tagValues.add(tags.get(tagKey).getTagValue());
      }
    }
    return tagValues;
  }

  // TODO(songy23): remove the mapping once we completely remove the deprecated RPC constants.
  private static TagValue getTagValueForDeprecatedRpcTag(
      Map<? extends TagKey, TagValueWithMetadata> tags, TagKey oldKey) {
    for (TagKey newKey : RPC_TAG_MAPPINGS.get(oldKey)) {
      if (tags.containsKey(newKey)) {
        return tags.get(newKey).getTagValue();
      }
    }
    return UNKNOWN_TAG_VALUE;
  }

  /**
   * Create an empty {@link MutableAggregation} based on the given {@link Aggregation}.
   *
   * @param aggregation {@code Aggregation}.
   * @return an empty {@code MutableAggregation}.
   */
  @VisibleForTesting
  static MutableAggregation createMutableAggregation(
      Aggregation aggregation, final Measure measure) {
    return aggregation.match(
        new Function<Sum, MutableAggregation>() {
          @Override
          public MutableAggregation apply(Sum arg) {
            return measure.match(
                CreateMutableSumDouble.INSTANCE,
                CreateMutableSumLong.INSTANCE,
                Functions.<MutableAggregation>throwAssertionError());
          }
        },
        CreateMutableCount.INSTANCE,
        CreateMutableDistribution.INSTANCE,
        new Function<LastValue, MutableAggregation>() {
          @Override
          public MutableAggregation apply(LastValue arg) {
            return measure.match(
                CreateMutableLastValueDouble.INSTANCE,
                CreateMutableLastValueLong.INSTANCE,
                Functions.<MutableAggregation>throwAssertionError());
          }
        },
        AggregationDefaultFunction.INSTANCE);
  }

  // Covert a mapping from TagValues to MutableAggregation, to a mapping from TagValues to
  // AggregationData.
  static <T> Map<T, AggregationData> createAggregationMap(
      Map<T, MutableAggregation> tagValueAggregationMap, Measure measure) {
    Map<T, AggregationData> map = Maps.newHashMap();
    for (Entry<T, MutableAggregation> entry : tagValueAggregationMap.entrySet()) {
      map.put(entry.getKey(), entry.getValue().toAggregationData());
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

  private static final class CreateMutableSumDouble
      implements Function<MeasureDouble, MutableAggregation> {
    @Override
    public MutableAggregation apply(MeasureDouble arg) {
      return MutableSumDouble.create();
    }

    private static final CreateMutableSumDouble INSTANCE = new CreateMutableSumDouble();
  }

  private static final class CreateMutableSumLong
      implements Function<MeasureLong, MutableAggregation> {
    @Override
    public MutableAggregation apply(MeasureLong arg) {
      return MutableSumLong.create();
    }

    private static final CreateMutableSumLong INSTANCE = new CreateMutableSumLong();
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

  private static final class CreateMutableLastValueDouble
      implements Function<MeasureDouble, MutableAggregation> {
    @Override
    public MutableAggregation apply(MeasureDouble arg) {
      return MutableLastValueDouble.create();
    }

    private static final CreateMutableLastValueDouble INSTANCE = new CreateMutableLastValueDouble();
  }

  private static final class CreateMutableLastValueLong
      implements Function<MeasureLong, MutableAggregation> {
    @Override
    public MutableAggregation apply(MeasureLong arg) {
      return MutableLastValueLong.create();
    }

    private static final CreateMutableLastValueLong INSTANCE = new CreateMutableLastValueLong();
  }

  private RecordUtils() {}
}
