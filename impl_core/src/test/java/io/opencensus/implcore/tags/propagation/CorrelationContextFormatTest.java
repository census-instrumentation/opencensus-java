/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.implcore.tags.propagation;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.implcore.tags.propagation.CorrelationContextFormat.CORRELATION_CONTEXT;
import static io.opencensus.implcore.tags.propagation.CorrelationContextFormat.METADATA_UNLIMITED_PROPAGATION;

import io.opencensus.implcore.tags.TagsComponentImplBase;
import io.opencensus.implcore.tags.TagsTestUtil;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagMetadata.TagTtl;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.TaggingState;
import io.opencensus.tags.TagsComponent;
import io.opencensus.tags.propagation.TagContextDeserializationException;
import io.opencensus.tags.propagation.TagContextSerializationException;
import io.opencensus.tags.propagation.TagContextTextFormat;
import io.opencensus.tags.propagation.TagContextTextFormat.Getter;
import io.opencensus.tags.propagation.TagContextTextFormat.Setter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link CorrelationContextFormat}. */
@RunWith(JUnit4.class)
public class CorrelationContextFormatTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  private final TagsComponent tagsComponent = new TagsComponentImplBase();
  private final TagContextTextFormat textFormat =
      tagsComponent.getTagPropagationComponent().getCorrelationContextFormat();

  private static final TagMetadata METADATA_NO_PROPAGATION =
      TagMetadata.create(TagTtl.NO_PROPAGATION);

  private static final Setter<Map<String, String>> setter =
      new Setter<Map<String, String>>() {
        @Override
        public void put(Map<String, String> carrier, String key, String value) {
          carrier.put(key, value);
        }
      };

  private static final Getter<Map<String, String>> getter =
      new Getter<Map<String, String>>() {
        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final Tag T1 = Tag.create(K1, V1, METADATA_UNLIMITED_PROPAGATION);
  private static final Tag T2 = Tag.create(K2, V2, METADATA_UNLIMITED_PROPAGATION);

  private static final Random random = new Random();

  @Test
  public void fieldsList() {
    assertThat(textFormat.fields()).containsExactly(CORRELATION_CONTEXT);
  }

  @Test
  public void headerNames() {
    assertThat(CORRELATION_CONTEXT).isEqualTo("Correlation-Context");
  }

  @Test
  public void inject() throws TagContextSerializationException {
    Map<String, String> carrier = new HashMap<String, String>();
    textFormat.inject(makeTagContext(T1, T2), carrier, setter);
    assertThat(carrier).containsExactly(CORRELATION_CONTEXT, "k1=v1,k2=v2");
  }

  @Test
  public void inject_Empty() throws TagContextSerializationException {
    Map<String, String> carrier = new HashMap<String, String>();
    textFormat.inject(makeTagContext(), carrier, setter);
    assertThat(carrier).containsExactly(CORRELATION_CONTEXT, "");
  }

  @Test
  public void inject_SkipNonPropagatingTag() throws TagContextSerializationException {
    Map<String, String> carrier = new HashMap<String, String>();
    Tag tag = Tag.create(K1, V1, METADATA_NO_PROPAGATION);
    textFormat.inject(makeTagContext(tag), carrier, setter);
    assertThat(carrier).containsExactly(CORRELATION_CONTEXT, "");
  }

  @Test
  public void inject_MixedPropagatingAndNonPropagatingTags()
      throws TagContextSerializationException {
    Map<String, String> carrier = new HashMap<String, String>();
    Tag tag = Tag.create(K1, V1, METADATA_NO_PROPAGATION);
    textFormat.inject(makeTagContext(T1, tag, T2), carrier, setter);
    assertThat(carrier).containsExactly(CORRELATION_CONTEXT, "k1=v1,k2=v2");
  }

  @Test
  @SuppressWarnings("deprecation")
  public void inject_TaggingDisabled() throws TagContextSerializationException {
    Map<String, String> carrier = new HashMap<String, String>();
    tagsComponent.setState(TaggingState.DISABLED);
    textFormat.inject(makeTagContext(T1, T2), carrier, setter);
    assertThat(carrier).isEmpty();
    tagsComponent.setState(TaggingState.ENABLED);
  }

  @Test
  public void inject_TooManyTags() throws TagContextSerializationException {
    Tag[] tags = new Tag[CorrelationContextFormat.MAX_NUMBER_OF_TAGS + 1];
    for (int i = 0; i < tags.length; i++) {
      tags[i] =
          Tag.create(
              TagKey.create("k" + i), TagValue.create("v" + i), METADATA_UNLIMITED_PROPAGATION);
    }
    TagContext tagContext = makeTagContext(tags);
    Map<String, String> carrier = new HashMap<String, String>();
    thrown.expect(TagContextSerializationException.class);
    textFormat.inject(tagContext, carrier, setter);
  }

  @Test
  public void inject_SizeTooLarge() throws TagContextSerializationException {
    Tag[] tags = new Tag[40];
    for (int i = 0; i < tags.length; i++) {
      tags[i] =
          Tag.create(
              TagKey.create(generateRandom(240)),
              TagValue.create(generateRandom(240)),
              METADATA_UNLIMITED_PROPAGATION);
    }
    TagContext tagContext = makeTagContext(tags);
    Map<String, String> carrier = new HashMap<String, String>();
    thrown.expect(TagContextSerializationException.class);
    textFormat.inject(tagContext, carrier, setter);
  }

  @Test
  public void extract() throws TagContextDeserializationException {
    Map<String, String> carrier = Collections.singletonMap(CORRELATION_CONTEXT, "k1=v1,k2=v2");
    TagContext tagContext = textFormat.extract(carrier, getter);
    assertThat(TagsTestUtil.tagContextToList(tagContext)).containsExactly(T1, T2);
  }

  @Test
  public void extract_Empty() throws TagContextDeserializationException {
    Map<String, String> carrier = Collections.singletonMap(CORRELATION_CONTEXT, "");
    TagContext tagContext = textFormat.extract(carrier, getter);
    assertThat(TagsTestUtil.tagContextToList(tagContext)).isEmpty();
  }

  @Test
  public void extract_WithUnknownProperties() throws TagContextDeserializationException {
    Map<String, String> carrier =
        Collections.singletonMap(CORRELATION_CONTEXT, "k1=v1;property1=p1;property2=p2,k2=v2");
    Tag expected = Tag.create(K1, TagValue.create("v1"), METADATA_UNLIMITED_PROPAGATION);
    TagContext tagContext = textFormat.extract(carrier, getter);
    assertThat(TagsTestUtil.tagContextToList(tagContext)).containsExactly(expected, T2);
  }

  @Test
  public void extract_TrimSpaces() throws TagContextDeserializationException {
    Map<String, String> carrier = Collections.singletonMap(CORRELATION_CONTEXT, "k1= v1, k2=v2 ");
    Tag expected1 = Tag.create(K1, V1, METADATA_UNLIMITED_PROPAGATION);
    Tag expected2 = Tag.create(K2, V2, METADATA_UNLIMITED_PROPAGATION);
    TagContext tagContext = textFormat.extract(carrier, getter);
    assertThat(TagsTestUtil.tagContextToList(tagContext)).containsExactly(expected1, expected2);
  }

  @Test
  public void extract_OverrideTagWithSpaces() throws TagContextDeserializationException {
    Map<String, String> carrier = Collections.singletonMap(CORRELATION_CONTEXT, "k1= v1, k1=v2 ");
    Tag expected = Tag.create(K1, V2, METADATA_UNLIMITED_PROPAGATION);
    TagContext tagContext = textFormat.extract(carrier, getter);
    assertThat(TagsTestUtil.tagContextToList(tagContext)).containsExactly(expected);
  }

  @Test
  public void extract_NoCorrelationContextHeader() throws TagContextDeserializationException {
    Map<String, String> carrier = Collections.singletonMap("unknown-header", "value");
    thrown.expect(TagContextDeserializationException.class);
    textFormat.extract(carrier, getter);
  }

  @Test
  public void extract_MalformedTag() throws TagContextDeserializationException {
    Map<String, String> carrier = Collections.singletonMap(CORRELATION_CONTEXT, "k1,v1,k2=v2");
    thrown.expect(TagContextDeserializationException.class);
    textFormat.extract(carrier, getter);
  }

  @Test
  public void extract_MalformedTagKey() throws TagContextDeserializationException {
    Map<String, String> carrier = Collections.singletonMap(CORRELATION_CONTEXT, "k1=v1,ké=v2");
    thrown.expect(TagContextDeserializationException.class);
    textFormat.extract(carrier, getter);
  }

  @Test
  public void extract_MalformedTagValue() throws TagContextDeserializationException {
    Map<String, String> carrier = Collections.singletonMap(CORRELATION_CONTEXT, "k1=v1,k2=vé");
    thrown.expect(TagContextDeserializationException.class);
    textFormat.extract(carrier, getter);
  }

  @Test
  public void extract_TagKeyTooLong() throws TagContextDeserializationException {
    String longKey = generateRandom(300);
    Map<String, String> carrier = Collections.singletonMap(CORRELATION_CONTEXT, longKey + "=v1");
    thrown.expect(TagContextDeserializationException.class);
    textFormat.extract(carrier, getter);
  }

  @Test
  public void extract_TagValueTooLong() throws TagContextDeserializationException {
    String longValue = generateRandom(300);
    Map<String, String> carrier = Collections.singletonMap(CORRELATION_CONTEXT, "k1=" + longValue);
    thrown.expect(TagContextDeserializationException.class);
    textFormat.extract(carrier, getter);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void extract_TaggingDisabled()
      throws TagContextDeserializationException, TagContextSerializationException {
    Map<String, String> carrier = new HashMap<String, String>();
    textFormat.inject(makeTagContext(T1), carrier, setter);
    tagsComponent.setState(TaggingState.DISABLED);
    assertThat(TagsTestUtil.tagContextToList(textFormat.extract(carrier, getter))).isEmpty();
    tagsComponent.setState(TaggingState.ENABLED);
  }

  @Test
  public void roundTrip()
      throws TagContextSerializationException, TagContextDeserializationException {
    Tag[] tags = new Tag[40];
    for (int i = 0; i < tags.length; i++) {
      tags[i] =
          Tag.create(
              TagKey.create(generateRandom(10)),
              TagValue.create(generateRandom(10)),
              METADATA_UNLIMITED_PROPAGATION);
    }
    TagContext tagContext = makeTagContext(tags);
    Map<String, String> carrier = new HashMap<String, String>();
    textFormat.inject(tagContext, carrier, setter);
    TagContext actual = textFormat.extract(carrier, getter);
    assertThat(TagsTestUtil.tagContextToList(actual))
        .containsExactlyElementsIn(TagsTestUtil.tagContextToList(tagContext));
  }

  private static TagContext makeTagContext(final Tag... tags) {
    return new TagContext() {
      @Override
      public Iterator<Tag> getIterator() {
        return Arrays.<Tag>asList(tags).iterator();
      }
    };
  }

  private static String generateRandom(int length) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; i++) {
      builder.append(random.nextInt(10));
    }
    return builder.toString();
  }
}
