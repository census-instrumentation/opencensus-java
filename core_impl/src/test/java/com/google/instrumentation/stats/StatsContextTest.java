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

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;

import com.google.io.base.VarInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link StatsContext}.
 */
@RunWith(JUnit4.class)
public class StatsContextTest {
  private static final StatsContext DEFAULT = Stats.getStatsContextFactory().getDefault();

  private static final MeasurementDescriptor[] StatsMeasurementDescriptors = {
    RpcConstants.RPC_CLIENT_REQUEST_BYTES, RpcConstants.RPC_CLIENT_RESPONSE_BYTES,
    RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, RpcConstants.RPC_SERVER_REQUEST_BYTES,
    RpcConstants.RPC_SERVER_RESPONSE_BYTES, RpcConstants.RPC_SERVER_SERVER_LATENCY
  };

  private static final int VERSION_ID = 0;
  private static final int VALUE_TYPE_STRING = 0;

  private static final TagKey K_EMPTY = TagKey.create("");
  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagKey K3 = TagKey.create("k3");
  private static final TagKey K4 = TagKey.create("k4");
  private static final TagKey K10 = TagKey.create("k10");

  private static final TagValue V_EMPTY = TagValue.create("");
  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V3 = TagValue.create("v3");
  private static final TagValue V4 = TagValue.create("v4");
  private static final TagValue V10 = TagValue.create("v10");
  private static final TagValue V20 = TagValue.create("v20");
  private static final TagValue V30 = TagValue.create("v30");
  private static final TagValue V100 = TagValue.create("v100");

  private static final Tag T1 = Tag.create(K1, V1);
  private static final Tag T2 = Tag.create(K2, V2);
  private static final Tag T3 = Tag.create(K3, V3);
  private static final Tag T4 = Tag.create(K4, V4);

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
    StatsContext context1 = DEFAULT.with(K1, V1);
    assertThat(DEFAULT.builder().set(K1, V1).build()).isEqualTo(context1);

    StatsContext context2 = context1.with(K1, V10, K2, V2);
    assertThat(DEFAULT.with(K1, V10, K2, V2)).isEqualTo(context2);

    StatsContext context3 = context2.with(K1, V100, K2, V20, K3, V3);
    assertThat(DEFAULT.with(K1, V100, K2, V20, K3, V3)).isEqualTo(context3);

    StatsContext context4 = context3.with(K3, V30, K4, V4);
    assertThat(DEFAULT.builder().set(K1, V100).set(K2, V20).set(K3, V30).set(K4, V4).build())
        .isEqualTo(context4);
  }


  @Test
  public void testRecordEachMeasurement() {
    StatsContext context = DEFAULT.with(K1, V1);
    double value = 44.0;
    for (MeasurementDescriptor descriptor : StatsMeasurementDescriptors) {
      MeasurementMap measurements = MeasurementMap.of(descriptor, value);
      context.record(measurements);
      //verify(context.context).record(measurements);
      value++;
    }
  }

  @Test
  public void testRecordAllMeasurements() {
    StatsContext context = DEFAULT.with(K1, V1);
    double value = 44.0;
    MeasurementMap.Builder builder = MeasurementMap.builder();
    for (MeasurementDescriptor descriptor : StatsMeasurementDescriptors) {
      MeasurementMap measurements = builder.put(descriptor, value).build();
      context.record(measurements);
      //verify(context.context).record(measurements);
      value++;
    }
  }

  @Test
  public void testSerializeDefault() throws Exception {
    testSerialize();
  }

  @Test
  public void testSerializeWithOneStringTag() throws Exception {
    testSerialize(T1);
  }

  @Test
  public void testSerializeWithMultiStringTags() throws Exception {
    testSerialize(T1, T2, T3, T4);
  }

  @Test
  public void testRoundtripSerialization() throws Exception {
    testRoundtripSerialization(DEFAULT.builder().build());
    testRoundtripSerialization(DEFAULT.with(K1, V1));
    testRoundtripSerialization(DEFAULT.with(K1, V1, K2, V2, K3, V3));
    testRoundtripSerialization(DEFAULT.with(K1, V_EMPTY));
    testRoundtripSerialization(DEFAULT.with(K_EMPTY, V1));
    testRoundtripSerialization(DEFAULT.with(K_EMPTY, V_EMPTY));
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

  private static void testSerialize(Tag... tags) throws IOException {
    final Map<TagKey, TagValue> tagMap = new HashMap<TagKey, TagValue>();
    for (Tag tag : tags) {
      tagMap.put(tag.getKey(), tag.getValue());
    }

    ByteArrayOutputStream actual = new ByteArrayOutputStream();
    new StatsContextImpl(tagMap).serialize(actual);

    ByteArrayOutputStream expected = new ByteArrayOutputStream();
    expected.write(VERSION_ID);
    for (Entry<TagKey, TagValue> entry : tagMap.entrySet()) {
      expected.write(VALUE_TYPE_STRING);
      encodeString(entry.getKey().asString(), expected);
      encodeString(entry.getValue().asString(), expected);
    }

    assertThat(actual.toByteArray()).isEqualTo(expected.toByteArray());
  }

  private static void testRoundtripSerialization(StatsContext expected) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    expected.serialize(output);
    ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
    StatsContext actual = Stats.getStatsContextFactory().deserialize(input);
    assertThat(actual).isEqualTo(expected);
  }

  private static final void encodeString(String input, ByteArrayOutputStream byteArrayOutputStream)
      throws IOException {
    VarInt.putVarInt(input.length(), byteArrayOutputStream);
    byteArrayOutputStream.write(input.getBytes("UTF-8"));
  }
}
