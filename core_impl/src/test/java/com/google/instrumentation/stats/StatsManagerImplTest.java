/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.instrumentation.common.SimpleEventQueue;
import com.google.instrumentation.stats.View.DistributionView;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link StatsManagerImpl}.
 */
@RunWith(JUnit4.class)
public class StatsManagerImplTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static final double TOLERANCE = 1e-5;
  private static final TagKey tagKey = RpcMeasurementConstants.RPC_CLIENT_METHOD;
  private static final TagKey wrongTagKey = TagKey.create("Wrong Tag Key");
  private static final TagKey wrongTagKey2 = TagKey.create("Another wrong Tag Key");
  private static final TagValue tagValue1 = TagValue.create("some client method");
  private static final TagValue tagValue2 = TagValue.create("some other client method");
  private static final StatsContextImpl oneTag =
          new StatsContextImpl(ImmutableMap.of(tagKey, tagValue1));
  private static final StatsContextImpl anotherTag =
          new StatsContextImpl(ImmutableMap.of(tagKey, tagValue2));
  private static final StatsContextImpl wrongTag =
          new StatsContextImpl(ImmutableMap.of(wrongTagKey, tagValue1));
  private static final StatsContextImpl wrongTag2 =
      new StatsContextImpl(ImmutableMap.of(wrongTagKey, tagValue1, wrongTagKey2, tagValue2));


  private final StatsManagerImpl statsManager = new StatsManagerImpl(new SimpleEventQueue());

  @Test
  public void testRegisterAndGetView() throws Exception {
    statsManager.registerView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    View actual = statsManager.getView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(actual.getViewDescriptor()).isEqualTo(
        RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
  }

  @Test
  public void testRegisterUnsupportedViewDescriptor() throws Exception {
    thrown.expect(UnsupportedOperationException.class);
    statsManager.registerView(RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_VIEW);
  }

  @Test
  public void testRegisterViewDescriptorTwice() {
    statsManager.registerView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    statsManager.registerView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    View actual = statsManager.getView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(actual.getViewDescriptor()).isEqualTo(
        RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
  }

  @Test
  public void testGetNonexistentView() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    statsManager.getView(RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_VIEW);
  }

  @Test
  public void testRecord() {
    statsManager.registerView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    for (double val : Arrays.<Double>asList(10.0, 20.0, 30.0, 40.0)) {
      statsManager.record(
          oneTag, MeasurementMap.of(RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, val));
    }

    DistributionView view =
        (DistributionView) statsManager.getView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(view.getViewDescriptor()).isEqualTo(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    // TODO(songya): update to make assertions on the exact time based on fake clock.
    assertThat(view.getEnd().getSeconds()).isAtLeast(view.getStart().getSeconds());
    List<DistributionAggregation> distributionAggregations = view.getDistributionAggregations();
    assertThat(distributionAggregations).hasSize(1);
    DistributionAggregation distributionAggregation = distributionAggregations.get(0);
    verifyDistributionAggregation(distributionAggregation, 4, 100.0, 25.0, 10.0, 40.0, 1);
    // Refer to RpcViewConstants.RPC_MILLIS_BUCKET_BOUNDARIES for bucket boundaries.
    verifyBucketCounts(distributionAggregation.getBucketCounts(), 9, 12, 14, 15);

    List<Tag> tags = distributionAggregation.getTags();
    assertThat(tags.get(0).getKey()).isEqualTo(tagKey);
    assertThat(tags.get(0).getValue()).isEqualTo(tagValue1);
  }

  @Test
  public void testRecordMultipleTagValues() {
    statsManager.registerView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    statsManager.record(oneTag, MeasurementMap.of(
        RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 10.0));
    statsManager.record(anotherTag, MeasurementMap.of(
        RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 30.0));
    statsManager.record(anotherTag, MeasurementMap.of(
        RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 50.0));

    DistributionView view =
        (DistributionView) statsManager.getView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    List<DistributionAggregation> distributionAggregations = view.getDistributionAggregations();
    assertThat(distributionAggregations).hasSize(2);
    // Sort distributionAggregations by count.
    Collections.sort(distributionAggregations, new Comparator<DistributionAggregation>() {
      @Override
      public int compare(DistributionAggregation o1, DistributionAggregation o2) {
        return Long.valueOf(o1.getCount()).compareTo(o2.getCount());
      }
    });

    DistributionAggregation distributionAggregation1 = distributionAggregations.get(0);
    DistributionAggregation distributionAggregation2 = distributionAggregations.get(1);

    verifyDistributionAggregation(distributionAggregation1, 1, 10.0, 10.0, 10.0, 10.0, 1);
    verifyDistributionAggregation(distributionAggregation2, 2, 80.0, 40.0, 30.0, 50.0, 1);
    verifyBucketCounts(distributionAggregation1.getBucketCounts(), 9);
    verifyBucketCounts(distributionAggregation2.getBucketCounts(), 14, 16);
    assertThat(distributionAggregation1.getTags().get(0).getKey()).isEqualTo(tagKey);
    assertThat(distributionAggregation1.getTags().get(0).getValue()).isEqualTo(tagValue1);
    assertThat(distributionAggregation2.getTags().get(0).getKey()).isEqualTo(tagKey);
    assertThat(distributionAggregation2.getTags().get(0).getValue()).isEqualTo(tagValue2);
  }

  private static void verifyDistributionAggregation(
      DistributionAggregation distributionAggregation,
      int count, double sum, double mean, double min, double max, int tagsSize) {
    assertThat(distributionAggregation.getCount()).isEqualTo(count);
    assertThat(distributionAggregation.getSum()).isWithin(TOLERANCE).of(sum);
    assertThat(distributionAggregation.getMean()).isWithin(TOLERANCE).of(mean);
    assertThat(distributionAggregation.getRange().getMin()).isWithin(TOLERANCE).of(min);
    assertThat(distributionAggregation.getRange().getMax()).isWithin(TOLERANCE).of(max);
    assertThat(distributionAggregation.getTags().size()).isEqualTo(tagsSize);
  }

  private static final void verifyBucketCounts(List<Long> bucketCounts, int... nonZeroBuckets) {
    // nonZeroBuckets must be ordered.
    Arrays.sort(nonZeroBuckets);
    int j = 0;
    for (int i = 0; i < bucketCounts.size(); ++i) {
      if (j < nonZeroBuckets.length && i == nonZeroBuckets[j]) {
        assertThat(bucketCounts.get(i)).isNotEqualTo(0);
        ++j;
      } else {
        assertThat(bucketCounts.get(i)).isEqualTo(0);
      }
    }
  }

  @Test
  public void testRecordWithoutRegisteringView() {
    thrown.expect(IllegalArgumentException.class);
    statsManager.record(oneTag, MeasurementMap.of(
        RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 10));
  }

  @Test
  public void testRecordWithEmptyStatsContext() {
    statsManager.registerView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    // DEFAULT doesn't have tags. Should have TagKey "method" as defined in RpcViewConstants.
    statsManager.record(StatsContextFactoryImpl.DEFAULT, MeasurementMap.of(
        RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 10.0));
    DistributionView view =
        (DistributionView) statsManager.getView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(view.getDistributionAggregations()).hasSize(1);
    DistributionAggregation distributionAggregation = view.getDistributionAggregations().get(0);
    List<Tag> tags = distributionAggregation.getTags();
    assertThat(tags.get(0).getKey()).isEqualTo(tagKey);
    // Tag is missing for associated measurementValues, should use default tag value
    // "unknown/not set".
    assertThat(tags.get(0).getValue()).isEqualTo(MutableView.UNKNOWN_TAG_VALUE);
    // Should record stats with default Tag: "method" : "unknown/not set".
    verifyDistributionAggregation(distributionAggregation, 1, 10.0, 10.0, 10.0, 10.0, 1);
  }

  @Test
  public void testRecordNonExistentMeasurementDescriptor() {
    statsManager.registerView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    statsManager.record(oneTag, MeasurementMap.of(
        RpcMeasurementConstants.RPC_SERVER_ERROR_COUNT, 10.0));
    DistributionView view =
        (DistributionView) statsManager.getView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(view.getDistributionAggregations()).hasSize(0);
  }


  @Test
  public void testRecordTagDoesNotMatchView() {
    statsManager.registerView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    statsManager.record(wrongTag, MeasurementMap.of(
        RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 10.0));
    statsManager.record(wrongTag2, MeasurementMap.of(
        RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 50.0));
    DistributionView view =
        (DistributionView) statsManager.getView(RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);

    assertThat(view.getDistributionAggregations()).hasSize(1);
    DistributionAggregation distributionAggregation = view.getDistributionAggregations().get(0);
    List<Tag> tags = distributionAggregation.getTags();
    // Won't record the unregistered tag key, will use default tag instead:
    // "method" : "unknown/not set".
    assertThat(tags.get(0).getKey()).isEqualTo(tagKey);
    assertThat(tags.get(0).getValue()).isEqualTo(MutableView.UNKNOWN_TAG_VALUE);
    // Should record stats with default Tag: "method" : "unknown/not set"
    verifyDistributionAggregation(distributionAggregation, 2, 60.0, 30.0, 10.0, 50.0, 1);
  }
}
