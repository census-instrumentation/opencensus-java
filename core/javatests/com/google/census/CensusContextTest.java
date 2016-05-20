/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.census;

import static com.google.common.truth.Truth.assertThat;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.ByteBuffer;

/**
 * Tests for {@link CensusContext}.
 */
@RunWith(JUnit4.class)
public class CensusContextTest {
  @Test
  public void testDefault() {
    assertThat(DEFAULT.context).isEqualTo(CensusContext.contextFactory.getDefault());
  }

  @Test
  public void testGetCurrent() {
    assertThat(CensusContext.getCurrent()).isEqualTo(DEFAULT);
  }

  @Test
  public void testDeserializeEmpty() {
    assertThat(CensusContext.deserialize(ByteBuffer.wrap(new byte[0]))).isEqualTo(DEFAULT);
  }

  @Test
  public void testDeserializeBadData() {
    assertThat(CensusContext.deserialize(ByteBuffer.wrap("\2as\3df\2".getBytes(UTF_8))))
        .isEqualTo(DEFAULT);
  }

  @Test
  public void testSerializeDeserialize() {
    testSerialization(TagMap.of());
    testSerialization(TAG_MAP1);
    testSerialization(TagMap.of(K1, "v1", K2, "v2", K3, "v3"));
    testSerialization(TagMap.of(K1, ""));
    testSerialization(TagMap.of(K_EMPTY, "v1"));
    testSerialization(TagMap.of(K_EMPTY, ""));
  }

  @Test
  public void testWith() {
    CensusContext context1 = DEFAULT.with(TAG_MAP1);
    assertEquivalent(TagMap.of(K1, "v1"), context1);

    CensusContext context2 = context1.with(TagMap.of(K1, "v10", K2, "v2"));
    assertEquivalent(TagMap.of(K1, "v10", K2, "v2"), context2);

    CensusContext context3 = context2.with(TagMap.of(K1, "v100", K2, "v20", K3, "v3"));
    assertEquivalent(TagMap.of(K1, "v100", K2, "v20", K3, "v3"), context3);

    CensusContext context4 = context3.with(TagMap.of(K3, "v30", K4, "v4"));
    assertEquivalent(
        TagMap.builder().put(K1, "v100").put(K2, "v20").put(K3, "v30").put(K4, "v4").build(),
        context4);
  }

  @Test
  public void testRecordEachMetric() {
    CensusContext context = DEFAULT.with(TAG_MAP1);
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
    CensusContext context = DEFAULT.with(TAG_MAP1);
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
  public void testGetAndSetCurrent() {
    assertThat(DEFAULT).isEqualTo(CensusContext.getCurrent());

    CensusContext context = DEFAULT.with(TAG_MAP1);
    context.setCurrent();
    assertThat(context).isEqualTo(CensusContext.getCurrent());

    DEFAULT.setCurrent();
    assertThat(DEFAULT).isEqualTo(CensusContext.getCurrent());
  }

  @Test
  public void testTransferCurrentThreadUsage() {
    DEFAULT.transferCurrentThreadUsage();
  }

  @Test
  public void testOpStart() {
    DEFAULT.context.opStart();
  }

  @Test
  public void testOpEnd() {
    DEFAULT.context.opEnd();
  }

  @Test
  public void testPrint() {
    DEFAULT.context.print("some interesting event");
  }

  // Tests for overrides.
  @Test
  public void testEquals() {
    assertThat(DEFAULT).isEqualTo(DEFAULT);
    assertThat(DEFAULT.with(TAG_MAP1)).isEqualTo(DEFAULT.with(TAG_MAP1));
    assertThat(DEFAULT.with(TagMap.of(K1, "v1", K2, "v2")))
        .isEqualTo(DEFAULT.with(TagMap.of(K1, "v1", K2, "v2")));
    assertThat(DEFAULT.with(TagMap.of(K1, "v1", K2, "v2")))
        .isEqualTo(DEFAULT.with(TagMap.of(K2, "v2", K1, "v1")));

    assertThat(DEFAULT).isNotEqualTo(DEFAULT.with(TAG_MAP1));
    assertThat(DEFAULT.with(TAG_MAP1)).isNotEqualTo(DEFAULT);
    assertThat(DEFAULT.with(TAG_MAP1)).isNotEqualTo(DEFAULT.with(TagMap.of(K10, "v1")));
    assertThat(DEFAULT.with(TAG_MAP1)).isNotEqualTo(DEFAULT.with(TagMap.of(K1, "v10")));
    assertThat(DEFAULT.with(TAG_MAP1)).isNotEqualTo(DEFAULT.with(TagMap.of(K1, "v1", K2, "v2")));
    assertThat(DEFAULT.with(TAG_MAP1)).isNotEqualTo("foo");
  }

  @Test
  public void testHashCode() {
    assertThat(DEFAULT.with(TAG_MAP1).hashCode()).isEqualTo(DEFAULT.with(TAG_MAP1).hashCode());
    assertThat(DEFAULT.with(TAG_MAP1).hashCode()).isNotEqualTo(DEFAULT.hashCode());
    assertThat(DEFAULT.with(TagMap.of(K10, "v1")).hashCode())
        .isNotEqualTo(DEFAULT.with(TAG_MAP1).hashCode());
    assertThat(DEFAULT.with(TagMap.of(K1, "v10")).hashCode())
        .isNotEqualTo(DEFAULT.with(TAG_MAP1).hashCode());
  }

  @Test
  public void testToString() {
    assertThat(DEFAULT.with(TAG_MAP1).toString()).isEqualTo(DEFAULT.with(TAG_MAP1).toString());
    assertThat(DEFAULT.with(TagMap.of(K10, "v1")).toString())
        .isNotEqualTo(DEFAULT.with(TAG_MAP1).toString());
    assertThat(DEFAULT.with(TagMap.of(K1, "v10")).toString())
        .isNotEqualTo(DEFAULT.with(TAG_MAP1).toString());
  }

  private static final CensusContext DEFAULT = CensusContext.DEFAULT;
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

  private static final TagMap TAG_MAP1 = TagMap.of(K1, "v1");

  private static void assertEquivalent(TagMap tags, CensusContext actual) {
    assertThat(DEFAULT.with(tags)).isEqualTo(actual);
  }

  private static void testSerialization(TagMap tags) {
    CensusContext expected = DEFAULT.with(tags);
    CensusContext actual = CensusContext.deserialize(expected.serialize());
    assertThat(actual).isEqualTo(expected);
  }
}
