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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opencensus.implcore.stats.MutableAggregation.MutableDistribution;
import io.opencensus.implcore.tags.TagValueWithMetadata;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.LastValue;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagMetadata.TagTtl;
import io.opencensus.tags.TagValue;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link RecordUtils}. */
@RunWith(JUnit4.class)
public class RecordUtilsTest {

  private static final double EPSILON = 1e-7;
  private static final MeasureDouble MEASURE_DOUBLE =
      MeasureDouble.create("measure1", "description", "1");
  private static final MeasureLong MEASURE_LONG =
      MeasureLong.create("measure2", "description", "1");
  private static final TagMetadata METADATA_UNLIMITED_PROPAGATION =
      TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION);
  private static final TagKey ORIGINATOR = TagKey.create("originator");
  private static final TagKey CALLER = TagKey.create("caller");
  private static final TagKey METHOD = TagKey.create("method");
  private static final TagValue CALLER_V = TagValue.create("some caller");
  private static final TagValue METHOD_V = TagValue.create("some method");
  private static final TagValue METHOD_V_2 = TagValue.create("some other method");
  private static final TagValue METHOD_V_3 = TagValue.create("the third method");
  private static final TagValue STATUS_V = TagValue.create("ok");
  private static final TagValue STATUS_V_2 = TagValue.create("error");
  private static final TagValueWithMetadata CALLER_V_WITH_MD =
      TagValueWithMetadata.create(CALLER_V, METADATA_UNLIMITED_PROPAGATION);
  private static final TagValueWithMetadata METHOD_V_WITH_MD =
      TagValueWithMetadata.create(METHOD_V, METADATA_UNLIMITED_PROPAGATION);
  private static final TagValueWithMetadata METHOD_V_2_WITH_MD =
      TagValueWithMetadata.create(METHOD_V_2, METADATA_UNLIMITED_PROPAGATION);
  private static final TagValueWithMetadata METHOD_V_3_WITH_MD =
      TagValueWithMetadata.create(METHOD_V_3, METADATA_UNLIMITED_PROPAGATION);
  private static final TagValueWithMetadata STATUS_V_WITH_MD =
      TagValueWithMetadata.create(STATUS_V, METADATA_UNLIMITED_PROPAGATION);
  private static final TagValueWithMetadata STATUS_V_2_WITH_MD =
      TagValueWithMetadata.create(STATUS_V_2, METADATA_UNLIMITED_PROPAGATION);

  @Test
  public void testConstants() {
    assertThat(RecordUtils.UNKNOWN_TAG_VALUE).isNull();
  }

  @Test
  public void testGetTagValues() {
    List<TagKey> columns = Arrays.asList(CALLER, METHOD, ORIGINATOR);
    Map<TagKey, TagValueWithMetadata> tags =
        ImmutableMap.of(CALLER, CALLER_V_WITH_MD, METHOD, METHOD_V_WITH_MD);

    assertThat(RecordUtils.getTagValues(tags, columns))
        .containsExactly(CALLER_V, METHOD_V, RecordUtils.UNKNOWN_TAG_VALUE)
        .inOrder();
  }

  @Test
  public void testGetTagValues_MapDeprecatedRpcTag() {
    List<TagKey> columns = Arrays.asList(RecordUtils.RPC_STATUS, RecordUtils.RPC_METHOD);
    Map<TagKey, TagValueWithMetadata> tags =
        ImmutableMap.of(
            RecordUtils.GRPC_CLIENT_METHOD, METHOD_V_WITH_MD,
            RecordUtils.GRPC_CLIENT_STATUS, STATUS_V_WITH_MD);

    assertThat(RecordUtils.getTagValues(tags, columns))
        .containsExactly(STATUS_V, METHOD_V)
        .inOrder();
  }

  @Test
  public void testGetTagValues_MapDeprecatedRpcTag_WithServerTag() {
    List<TagKey> columns = Arrays.asList(RecordUtils.RPC_STATUS, RecordUtils.RPC_METHOD);
    Map<TagKey, TagValueWithMetadata> tags =
        ImmutableMap.of(
            RecordUtils.GRPC_SERVER_METHOD, METHOD_V_WITH_MD,
            RecordUtils.GRPC_SERVER_STATUS, STATUS_V_WITH_MD);

    assertThat(RecordUtils.getTagValues(tags, columns))
        .containsExactly(STATUS_V, METHOD_V)
        .inOrder();
  }

  @Test
  public void testGetTagValues_MapDeprecatedRpcTag_PreferClientTag() {
    List<TagKey> columns = Arrays.asList(RecordUtils.RPC_STATUS, RecordUtils.RPC_METHOD);
    Map<TagKey, TagValueWithMetadata> tags =
        ImmutableMap.of(
            RecordUtils.GRPC_SERVER_METHOD, METHOD_V_WITH_MD,
            RecordUtils.GRPC_SERVER_STATUS, STATUS_V_WITH_MD,
            RecordUtils.GRPC_CLIENT_METHOD, METHOD_V_2_WITH_MD,
            RecordUtils.GRPC_CLIENT_STATUS, STATUS_V_2_WITH_MD);

    // When both client and server new tags are present, client values take precedence.
    assertThat(RecordUtils.getTagValues(tags, columns))
        .containsExactly(STATUS_V_2, METHOD_V_2)
        .inOrder();
  }

  @Test
  public void testGetTagValues_WithOldMethodTag() {
    List<TagKey> columns = Arrays.asList(RecordUtils.RPC_METHOD);
    Map<TagKey, TagValueWithMetadata> tags =
        ImmutableMap.of(
            RecordUtils.GRPC_SERVER_METHOD, METHOD_V_WITH_MD,
            RecordUtils.GRPC_CLIENT_METHOD, METHOD_V_2_WITH_MD,
            RecordUtils.RPC_METHOD, METHOD_V_3_WITH_MD);

    // When the old "method" tag is set, it always takes precedence.
    assertThat(RecordUtils.getTagValues(tags, columns)).containsExactly(METHOD_V_3).inOrder();
  }

  @Test
  public void testGetTagValues_WithNewTags() {
    List<TagKey> columns =
        Arrays.asList(RecordUtils.GRPC_CLIENT_METHOD, RecordUtils.GRPC_SERVER_METHOD);
    Map<TagKey, TagValueWithMetadata> tags =
        ImmutableMap.of(
            RecordUtils.GRPC_SERVER_METHOD, METHOD_V_WITH_MD,
            RecordUtils.GRPC_CLIENT_METHOD, METHOD_V_2_WITH_MD);

    assertThat(RecordUtils.getTagValues(tags, columns))
        .containsExactly(METHOD_V_2, METHOD_V)
        .inOrder();
  }

  @Test
  public void createMutableAggregation() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(-1.0, 0.0, 1.0));

    assertThat(
            RecordUtils.createMutableAggregation(Sum.create(), MEASURE_DOUBLE).toAggregationData())
        .isEqualTo(SumDataDouble.create(0));
    assertThat(RecordUtils.createMutableAggregation(Sum.create(), MEASURE_LONG).toAggregationData())
        .isEqualTo(SumDataLong.create(0));
    assertThat(
            RecordUtils.createMutableAggregation(Count.create(), MEASURE_DOUBLE)
                .toAggregationData())
        .isEqualTo(CountData.create(0));
    assertThat(
            RecordUtils.createMutableAggregation(Count.create(), MEASURE_LONG).toAggregationData())
        .isEqualTo(CountData.create(0));
    assertThat(
            RecordUtils.createMutableAggregation(Mean.create(), MEASURE_DOUBLE).toAggregationData())
        .isEqualTo(MeanData.create(0, 0));
    assertThat(
            RecordUtils.createMutableAggregation(Mean.create(), MEASURE_LONG).toAggregationData())
        .isEqualTo(MeanData.create(0, 0));
    assertThat(
            RecordUtils.createMutableAggregation(LastValue.create(), MEASURE_DOUBLE)
                .toAggregationData())
        .isEqualTo(LastValueDataDouble.create(Double.NaN));
    assertThat(
            RecordUtils.createMutableAggregation(LastValue.create(), MEASURE_LONG)
                .toAggregationData())
        .isEqualTo(LastValueDataLong.create(0));

    MutableDistribution mutableDistribution =
        (MutableDistribution)
            RecordUtils.createMutableAggregation(
                Distribution.create(bucketBoundaries), MEASURE_DOUBLE);
    assertThat(mutableDistribution.getSumOfSquaredDeviations()).isWithin(EPSILON).of(0);
    assertThat(mutableDistribution.getBucketCounts()).isEqualTo(new long[2]);
  }
}
