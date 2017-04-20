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

import java.util.*;

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
  private static final TagKey tagKey = RpcConstants.RPC_CLIENT_METHOD;
  private static final TagValue tagValue1 = TagValue.create("some client method");
  private static final TagValue tagValue2 = TagValue.create("some other client method");
  private static final StatsContextImpl oneTag =
          new StatsContextImpl(ImmutableMap.of(tagKey.asString(), tagValue1.asString()));
  private static final StatsContextImpl anotherTag =
          new StatsContextImpl(ImmutableMap.of(tagKey.asString(), tagValue2.asString()));
  private static final StatsContextImpl wrongTag =
          new StatsContextImpl(ImmutableMap.of("Wrong Tag Key", tagValue1.asString()));


  private final StatsManagerImpl statsManager = new StatsManagerImpl(new SimpleEventQueue());

  @Test
  public void testRegisterAndGetView() throws Exception {
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    View actual = statsManager.getView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(actual.getViewDescriptor()).isEqualTo(
        RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
  }

  @Test
  public void testRegisterUnsupportedViewDescriptor() throws Exception {
    thrown.expect(UnsupportedOperationException.class);
    statsManager.registerView(RpcConstants.RPC_CLIENT_REQUEST_COUNT_VIEW);
  }

  @Test
  public void testRegisterViewDescriptorTwice(){
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    View actual = statsManager.getView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(actual.getViewDescriptor()).isEqualTo(
        RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
  }

  @Test
  public void testGetNonexistentView() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    statsManager.getView(RpcConstants.RPC_CLIENT_REQUEST_COUNT_VIEW);
  }

  @Test
  public void testRecord() {
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    for (double val : Arrays.<Double>asList(10.0, 20.0, 30.0, 40.0)) {
      statsManager.record(oneTag, MeasurementMap.of(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, val));
    }

    DistributionView view =
        (DistributionView) statsManager.getView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(view.getViewDescriptor()).isEqualTo(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    // TODO(songya): update to make assertions on the exact time..
    assertThat(view.getEnd().getSeconds()).isAtLeast(view.getStart().getSeconds());
    List<DistributionAggregation> distributionAggregations = view.getDistributionAggregations();
    assertThat(distributionAggregations).hasSize(1);
    DistributionAggregation distributionAggregation = distributionAggregations.get(0);
    verifyDistributionAggregation(distributionAggregation,
        RpcConstants.RPC_MILLIS_BUCKET_BOUNDARIES.size() + 1, 4,100.0,25.0,10.0,40.0,1);
    // Refer to RpcConstants.RPC_MILLIS_BUCKET_BOUNDARIES for bucket boundaries.
    for (int i = 0; i < distributionAggregation.getBucketCounts().size(); ++i) {
      if (i == 9 || i == 12 || i == 14 || i == 15) {
        assertThat(distributionAggregation.getBucketCounts().get(i)).isEqualTo(1);
      } else {
        assertThat(distributionAggregation.getBucketCounts().get(i)).isEqualTo(0);
      }
    }
    List<Tag> tags = distributionAggregation.getTags();
    assertThat(tags.get(0).getKey()).isEqualTo(tagKey);
    assertThat(tags.get(0).getValue()).isEqualTo(tagValue1);
  }

  @Test
  public void testRecordMultipleTagValues() {
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    statsManager.record(oneTag, MeasurementMap.of(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 10.0));
    statsManager.record(anotherTag, MeasurementMap.of(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 30.0));
    statsManager.record(anotherTag, MeasurementMap.of(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 50.0));

    DistributionView view =
        (DistributionView) statsManager.getView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    List<DistributionAggregation> distributionAggregations = view.getDistributionAggregations();
    assertThat(distributionAggregations).hasSize(2);
    DistributionAggregation distributionAggregation1 = distributionAggregations.get(0);
    DistributionAggregation distributionAggregation2 = distributionAggregations.get(1);

    verifyDistributionAggregation(distributionAggregation1,
        RpcConstants.RPC_MILLIS_BUCKET_BOUNDARIES.size() + 1, 1,10.0,10.0,10.0,10.0,1);
    verifyDistributionAggregation(distributionAggregation2,
        RpcConstants.RPC_MILLIS_BUCKET_BOUNDARIES.size() + 1, 2,80.0,40.0,30.0,50.0,1);
    assertThat(distributionAggregation1.getTags().get(0).getKey()).isEqualTo(tagKey);
    assertThat(distributionAggregation1.getTags().get(0).getValue()).isEqualTo(tagValue1);
    assertThat(distributionAggregation2.getTags().get(0).getKey()).isEqualTo(tagKey);
    assertThat(distributionAggregation2.getTags().get(0).getValue()).isEqualTo(tagValue2);
  }

  private static void verifyDistributionAggregation(
      DistributionAggregation distributionAggregation, int bucketCountsSize, int count, double sum,
      double mean, double min, double max, int tagsSize) {
    assertThat(distributionAggregation.getBucketCounts().size()).isEqualTo(bucketCountsSize);
    assertThat(distributionAggregation.getCount()).isEqualTo(count);
    assertThat(distributionAggregation.getSum()).isWithin(TOLERANCE).of(sum);
    assertThat(distributionAggregation.getMean()).isWithin(TOLERANCE).of(mean);
    assertThat(distributionAggregation.getRange().getMin()).isWithin(TOLERANCE).of(min);
    assertThat(distributionAggregation.getRange().getMax()).isWithin(TOLERANCE).of(max);
    assertThat(distributionAggregation.getTags().size()).isEqualTo(tagsSize);
  }

  @Test
  public void testRecordWithoutRegisteringView() {
    thrown.expect(IllegalArgumentException.class);
    statsManager.record(oneTag, MeasurementMap.of(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 10));
  }

  // TODO(songya): update this test once we determine how to handle tags that aren't an exact match for the view.
  @Test
  public void testRecordWithEmptyStatsContext() {
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    // DEFAULT doesn't have tags. Should have TagKey "method" as defined in RpcConstants.
    statsManager.record(StatsContextFactoryImpl.DEFAULT,
        MeasurementMap.of(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 10.0));
    DistributionView view =
        (DistributionView) statsManager.getView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(view.getDistributionAggregations()).hasSize(0);
  }

  @Test
  public void testRecordNonExistentMeasurementDescriptor() {
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    statsManager.record(oneTag, MeasurementMap.of(RpcConstants.RPC_SERVER_ERROR_COUNT, 10.0));
    DistributionView view =
        (DistributionView) statsManager.getView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    assertThat(view.getDistributionAggregations()).hasSize(0);
  }

  // TODO(songya): update this test once we determine how to handle tags that aren't an exact match for the view.
  @Test
  public void testRecordNonExistentTag() {
    statsManager.registerView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    statsManager.record(wrongTag, MeasurementMap.of(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 10.0));
    DistributionView view =
        (DistributionView) statsManager.getView(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW);
    // Won't record stats if there are non existent tags.
    assertThat(view.getDistributionAggregations()).hasSize(0);
  }
}
