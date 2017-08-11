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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opencensus.common.Scope;
import io.opencensus.internal.SimpleEventQueue;
import io.opencensus.internal.VarInt;
import io.opencensus.testing.common.TestClock;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link StatsContextFactory}.
 */
@RunWith(JUnit4.class)
public class StatsContextFactoryTest {

  private static final String KEY = "Key";
  private static final String VALUE_STRING = "String";
  private static final int VALUE_INT = 10;

  private final StatsComponentImplBase statsComponent =
      new StatsComponentImplBase(new SimpleEventQueue(), TestClock.create());
  private final StatsContextFactory factory = statsComponent.getStatsContextFactory();
  private final StatsRecorderImpl statsRecorder = statsComponent.getStatsRecorder();
  private final HashMap<TagKey, TagValue> sampleTags = new HashMap<TagKey, TagValue>();
  private final StatsContext defaultCtx = factory.getDefault();
  private final StatsContext statsContext = factory.getDefault()
      .with(TagKey.create(KEY), TagValue.create(VALUE_STRING));

  public StatsContextFactoryTest() {
    sampleTags.put(
        TagKey.create(KEY + StatsSerializer.VALUE_TYPE_STRING), TagValue.create(VALUE_STRING));
  }

  @Test
  public void testVersionAndValueTypeConstants() {
    // Refer to the JavaDoc on StatsSerializer for the definitions on these constants.
    assertThat(StatsSerializer.VERSION_ID).isEqualTo(0);
    assertThat(StatsSerializer.VALUE_TYPE_STRING).isEqualTo(0);
    assertThat(StatsSerializer.VALUE_TYPE_INTEGER).isEqualTo(1);
    assertThat(StatsSerializer.VALUE_TYPE_TRUE).isEqualTo(2);
    assertThat(StatsSerializer.VALUE_TYPE_FALSE).isEqualTo(3);
  }

  @Test
  public void testDeserializeNoTags() throws Exception {
    StatsContext expected = factory.getDefault();
    StatsContext actual = testDeserialize(
        new ByteArrayInputStream(
            new byte[]{StatsSerializer.VERSION_ID}));  // One byte that represents Version ID.
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = IOException.class)
  public void testDeserializeEmptyByteArrayThrowException() throws Exception {
    testDeserialize(new ByteArrayInputStream(new byte[0]));
  }

  @Test
  public void testDeserializeValueTypeString() throws Exception {
    StatsContext expected = new StatsContextImpl(statsRecorder, sampleTags);
    StatsContext actual = testDeserialize(
        constructSingleTypeTagInputStream(StatsSerializer.VALUE_TYPE_STRING));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testDeserializeMultipleString() throws Exception {
    sampleTags.put(TagKey.create("Key2"), TagValue.create("String2"));

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(StatsSerializer.VERSION_ID);
    encodeSingleTypeTagToOutputStream(StatsSerializer.VALUE_TYPE_STRING, byteArrayOutputStream);
    byteArrayOutputStream.write(StatsSerializer.VALUE_TYPE_STRING);
    encodeString("Key2", byteArrayOutputStream);
    encodeString("String2", byteArrayOutputStream);
    StatsContext actual = testDeserialize(
        new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

    StatsContext expected = new StatsContextImpl(statsRecorder, sampleTags);
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = IOException.class)
  public void testDeserializeValueTypeInteger() throws Exception {
    // TODO(songya): test should pass after we add support for type integer
    testDeserialize(constructSingleTypeTagInputStream(StatsSerializer.VALUE_TYPE_INTEGER));
  }

  @Test(expected = IOException.class)
  public void testDeserializeValueTypeTrue() throws Exception {
    // TODO(songya): test should pass after we add support for type boolean
    testDeserialize(constructSingleTypeTagInputStream(StatsSerializer.VALUE_TYPE_TRUE));
  }

  @Test(expected = IOException.class)
  public void testDeserializeValueTypeFalse() throws Exception {
    // TODO(songya): test should pass after we add support for type boolean
    testDeserialize(constructSingleTypeTagInputStream(StatsSerializer.VALUE_TYPE_FALSE));
  }

  @Test(expected = IOException.class)
  public void testDeserializeMultipleValueType() throws Exception {
    // TODO(songya): test should pass after we add support for type integer and boolean
    testDeserialize(constructMultiTypeTagInputStream());
  }

  @Test(expected = IOException.class)
  public void testDeserializeWrongFormat() throws Exception {
    // encoded tags should follow the format <version_id>(<tag_field_id><tag_encoding>)*
    testDeserialize(new ByteArrayInputStream(new byte[3]));
  }

  @Test(expected = IOException.class)
  public void testDeserializeWrongVersionId() throws Exception {
    testDeserialize(new ByteArrayInputStream(new byte[]{(byte) (StatsSerializer.VERSION_ID + 1)}));
  }

  @Test
  public void testGetDefaultForCurrentStatsContextWhenNotSet() {
    assertThat(factory.getCurrentStatsContext()).isEqualTo(defaultCtx);
  }

  @Test
  public void testGetCurrentStatsContext() {
    assertThat(factory.getCurrentStatsContext()).isEqualTo(defaultCtx);
    Context origContext = Context.current().withValue(
        CurrentStatsContextUtils.STATS_CONTEXT_KEY, statsContext)
        .attach();
    // Make sure context is detached even if test fails.
    try {
      assertThat(factory.getCurrentStatsContext()).isSameAs(statsContext);
    } finally {
      Context.current().detach(origContext);
    }
    assertThat(factory.getCurrentStatsContext()).isEqualTo(defaultCtx);
  }

  @Test(expected = NullPointerException.class)
  public void testWithNullStatsContext() {
    factory.withStatsContext(null);
  }

  @Test
  public void testWithStatsContext() {
    assertThat(factory.getCurrentStatsContext()).isEqualTo(defaultCtx);
    Scope scopedStatsCtx = factory.withStatsContext(statsContext);
    try {
      assertThat(factory.getCurrentStatsContext()).isEqualTo(statsContext);
    } finally {
      scopedStatsCtx.close();
    }
    assertThat(factory.getCurrentStatsContext()).isEqualTo(defaultCtx);
  }

  @Test
  public void testWithStatsContextUsingWrap() {
    Runnable runnable;
    Scope scopedStatsCtx = factory.withStatsContext(statsContext);
    try {
      assertThat(factory.getCurrentStatsContext()).isSameAs(statsContext);
      runnable = Context.current().wrap(
          new Runnable() {
            @Override
            public void run() {
              assertThat(factory.getCurrentStatsContext()).isSameAs(statsContext);
            }
          });
    } finally {
      scopedStatsCtx.close();
    }
    assertThat(factory.getCurrentStatsContext()).isEqualTo(defaultCtx);
    // When we run the runnable we will have the statsContext in the current Context.
    runnable.run();
  }

  private StatsContext testDeserialize(InputStream inputStream)
      throws IOException {
    return factory.deserialize(inputStream);
  }

  /*
   * Construct an InputStream with the given type of tag.
   * The input format is:
   *   <version_id><encoded_tags>, and <encoded_tags> == (<tag_field_id><tag_encoding>)*
   * TODO(songya): after supporting serialize integer and boolean,
   * remove this method and use StatsContext.serialize() instead.
   * Currently StatsContext.serialize() can only serialize strings.
   */
  private static InputStream constructSingleTypeTagInputStream(int valueType) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(StatsSerializer.VERSION_ID);
    encodeSingleTypeTagToOutputStream(valueType, byteArrayOutputStream);
    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
  }

  // Construct an InputStream with all 4 types of tags.
  private static InputStream constructMultiTypeTagInputStream() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(StatsSerializer.VERSION_ID);
    encodeSingleTypeTagToOutputStream(StatsSerializer.VALUE_TYPE_STRING, byteArrayOutputStream);
    encodeSingleTypeTagToOutputStream(StatsSerializer.VALUE_TYPE_INTEGER, byteArrayOutputStream);
    encodeSingleTypeTagToOutputStream(StatsSerializer.VALUE_TYPE_TRUE, byteArrayOutputStream);
    encodeSingleTypeTagToOutputStream(StatsSerializer.VALUE_TYPE_FALSE, byteArrayOutputStream);
    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
  }

  private static void encodeSingleTypeTagToOutputStream(
      int valueType, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
    byteArrayOutputStream.write(valueType);

    // encode <tag_key_len><tag_key>, tag key is a string "KEY" appended by the field id here.
    encodeString(KEY + valueType, byteArrayOutputStream);
    switch (valueType) {
      case StatsSerializer.VALUE_TYPE_STRING:
        // String encoded format: <tag_key_len><tag_key><tag_val_len><tag_val>.
        encodeString(VALUE_STRING, byteArrayOutputStream);
        break;
      case StatsSerializer.VALUE_TYPE_INTEGER:
        // Integer encoded format: <tag_key_len><tag_key><int_tag_val>.
        encodeInteger(VALUE_INT, byteArrayOutputStream);
        break;
      case StatsSerializer.VALUE_TYPE_TRUE:
      case StatsSerializer.VALUE_TYPE_FALSE:
        // Boolean encoded format: <tag_key_len><tag_key>. No tag_value is needed
        break;
      default:
        return;
    }
  }

  //     <tag_encoding> (tag_field_id == 0) ==
  //       <tag_key_len><tag_key><tag_val_len><tag_val>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //         <tag_val_len> == varint encoded integer
  //         <tag_val> == tag_val_len bytes comprising UTF-8 string
  private static void encodeString(String input, ByteArrayOutputStream byteArrayOutputStream)
      throws IOException {
    VarInt.putVarInt(input.length(), byteArrayOutputStream);
    byteArrayOutputStream.write(input.getBytes("UTF-8"));
  }

  //     <tag_encoding> (tag_field_id == 1) ==
  //       <tag_key_len><tag_key><int_tag_val>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //         <int_tag_value> == 8 bytes, little-endian integer
  private static void encodeInteger(int input, ByteArrayOutputStream byteArrayOutputStream) {
    byteArrayOutputStream.write((byte) input);
  }
}
