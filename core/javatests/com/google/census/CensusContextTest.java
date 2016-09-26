/*
 * Copyright 2016, Google Inc.
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

package com.google.census;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link CensusContext}.
 */
@RunWith(JUnit4.class)
public class CensusContextTest {
  private static final CensusContext DEFAULT = Census.getDefault();

  private static final MetricName[] CensusMetricNames = {
    RpcConstants.RPC_CLIENT_BYTES_RECEIVED, RpcConstants.RPC_CLIENT_BYTES_SENT,
    RpcConstants.RPC_CLIENT_LATENCY, RpcConstants.RPC_SERVER_BYTES_RECEIVED,
    RpcConstants.RPC_SERVER_BYTES_SENT, RpcConstants.RPC_SERVER_LATENCY
  };

  private static final TagKey K_EMPTY = new TagKey("");
  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");
  private static final TagKey K3 = new TagKey("k3");
  private static final TagKey K4 = new TagKey("k4");
  private static final TagKey K10 = new TagKey("k10");

  private static final TagValue V_EMPTY = new TagValue("");
  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");
  private static final TagValue V3 = new TagValue("v3");
  private static final TagValue V4 = new TagValue("v4");
  private static final TagValue V10 = new TagValue("v10");
  private static final TagValue V20 = new TagValue("v20");
  private static final TagValue V30 = new TagValue("v30");
  private static final TagValue V100 = new TagValue("v100");

  @Test
  public void testWith() {
    assertThat(DEFAULT.builder().set(K1, V1).build()).isEqualTo(DEFAULT.with(K1, V1));

    assertThat(DEFAULT.builder().set(K1, V1).set(K2, V2).build())
        .isEqualTo(DEFAULT.with(K1, V1, K2, V2));

    assertThat(DEFAULT.builder().set(K1, V1).set(K2, V2).set(K3, V3).build())
        .isEqualTo(DEFAULT.with(K1, V1, K2, V2, K3, V3));
  }

  @Test
  public void testWithComposed() {
    CensusContext context1 = DEFAULT.with(K1, V1);
    assertThat(DEFAULT.builder().set(K1, V1).build()).isEqualTo(context1);

    CensusContext context2 = context1.with(K1, V10, K2, V2);
    assertThat(DEFAULT.with(K1, V10, K2, V2)).isEqualTo(context2);

    CensusContext context3 = context2.with(K1, V100, K2, V20, K3, V3);
    assertThat(DEFAULT.with(K1, V100, K2, V20, K3, V3)).isEqualTo(context3);

    CensusContext context4 = context3.with(K3, V30, K4, V4);
    assertThat(DEFAULT.builder().set(K1, V100).set(K2, V20).set(K3, V30).set(K4, V4).build())
        .isEqualTo(context4);
  }


  @Test
  public void testRecordEachMetric() {
    CensusContext context = DEFAULT.with(K1, V1);
    double value = 44.0;
    for (MetricName metric : CensusMetricNames) {
      MetricMap metrics = MetricMap.of(metric, value);
      context.record(metrics);
      //verify(context.context).record(metrics);
      value++;
    }
  }

  @Test
  public void testRecordAllMetrics() {
    CensusContext context = DEFAULT.with(K1, V1);
    double value = 44.0;
    MetricMap.Builder builder = MetricMap.builder();
    for (MetricName metric : CensusMetricNames) {
      MetricMap metrics = builder.put(metric, value).build();
      context.record(metrics);
      //verify(context.context).record(metrics);
      value++;
    }
  }

  @Test
  public void testSerialize() {
    testSerialization(DEFAULT.builder().build());
    testSerialization(DEFAULT.with(K1, V1));
    testSerialization(DEFAULT.with(K1, V1, K2, V2, K3, V3));
    testSerialization(DEFAULT.with(K1, V_EMPTY));
    testSerialization(DEFAULT.with(K_EMPTY, V1));
    testSerialization(DEFAULT.with(K_EMPTY, V_EMPTY));
  }

  @Test
  public void testSetCurrent() {
    assertThat(DEFAULT).isEqualTo(Census.getCurrent());

    CensusContext context = DEFAULT.with(K1, V1);
    context.setCurrent();
    assertThat(context).isEqualTo(Census.getCurrent());

    DEFAULT.setCurrent();
    assertThat(DEFAULT).isEqualTo(Census.getCurrent());
  }

  @Test
  public void testMultipleThreadsWithContext() throws Exception {
    CensusContext currentContext = Census.getCurrent();
    CensusContext c1 = Census.getDefault().with(K1, V1);
    CensusContext c2 = Census.getDefault().with(K2, V2);
    ExecutorService executor = Executors.newFixedThreadPool(1);
    // Attach c1 to the executor thread.
    Future<?> future = executor.submit(withContext(Census.getDefault(), c1));
    future.get();
    // Verify that the context for the current thread was not updated.
    assertThat(Census.getCurrent()).isEqualTo(currentContext);
    // Attach c2 to the executor thread.
    future = executor.submit(withContext(c1, c2));
    future.get();
  }

  // Tests for Object overrides.

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(DEFAULT, DEFAULT)
        .addEqualityGroup(DEFAULT.with(K1, V1), DEFAULT.with(K1, V1))
        .addEqualityGroup(
            DEFAULT.with(K1, V1, K2, V2),
            DEFAULT.with(K1, V1, K2, V2),
            DEFAULT.with(K2, V2, K1, V1))
        .addEqualityGroup(DEFAULT.with(K10, V1))
        .addEqualityGroup(DEFAULT.with(K1, V10))
        .addEqualityGroup("foo")
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(DEFAULT.with(K1, V1).toString()).isEqualTo(DEFAULT.with(K1, V1).toString());
    assertThat(DEFAULT.with(K10, V1).toString()).isNotEqualTo(DEFAULT.with(K1, V1).toString());
    assertThat(DEFAULT.with(K1, V10).toString()).isNotEqualTo(DEFAULT.with(K1, V1).toString());
  }

  private static void testSerialization(CensusContext expected) {
    CensusContext actual = Census.deserialize(expected.serialize());
    assertThat(actual).isEqualTo(expected);
  }

  private static final Runnable withContext(final CensusContext prev, final CensusContext next) {
    return new Runnable() {
      @Override
      public void run() {
        assertThat(Census.getCurrent()).isEqualTo(prev);
        next.setCurrent();
        assertThat(Census.getCurrent()).isEqualTo(next);
      }
    };
  }
}
