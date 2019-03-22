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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import io.opencensus.implcore.internal.CurrentState;
import io.opencensus.implcore.internal.CurrentState.State;
import io.opencensus.implcore.tags.TagMapImpl;
import io.opencensus.implcore.tags.TagValueWithMetadata;
import io.opencensus.tags.InternalUtils;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagMetadata;
import io.opencensus.tags.TagMetadata.TagTtl;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.propagation.TagContextDeserializationException;
import io.opencensus.tags.propagation.TagContextSerializationException;
import io.opencensus.tags.propagation.TagContextTextFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
*/

/**
 * Implementation of the W3C correlation context propagation protocol. See <a
 * href=https://github.com/w3c/correlation-context>w3c/correlation-context</a>.
 */
final class CorrelationContextFormat extends TagContextTextFormat {

  @VisibleForTesting static final String CORRELATION_CONTEXT = "Correlation-Context";
  private static final List<String> FIELDS = Collections.singletonList(CORRELATION_CONTEXT);

  @VisibleForTesting static final int MAX_NUMBER_OF_TAGS = 180;
  private static final int TAG_SERIALIZED_SIZE_LIMIT = 4096;
  private static final int TAGCONTEXT_SERIALIZED_SIZE_LIMIT = 8192;
  private static final char TAG_KEY_VALUE_DELIMITER = '=';
  private static final char TAG_DELIMITER = ',';
  private static final Splitter TAG_KEY_VALUE_DELIMITER_SPLITTER =
      Splitter.on(TAG_KEY_VALUE_DELIMITER);
  private static final Splitter TAG_DELIMITER_SPLITTER = Splitter.on(TAG_DELIMITER);

  // private static final char TAG_PROPERTIES_LEFT_PAREN = '[';
  // private static final char TAG_PROPERTIES_RIGHT_PAREN = ']';
  // private static final char TAG_PROPERTIES_DELIMITER = ';';
  // private static final char TAG_PROPERTIES_KEY_VALUE_DELIMITER = '=';

  @VisibleForTesting
  static final TagMetadata METADATA_UNLIMITED_PROPAGATION =
      TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION);

  private final CurrentState state;

  CorrelationContextFormat(CurrentState state) {
    this.state = state;
  }

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C /*>>> extends @NonNull Object*/> void inject(
      TagContext tagContext, C carrier, Setter<C> setter) throws TagContextSerializationException {
    checkNotNull(tagContext, "tagContext");
    checkNotNull(carrier, "carrier");
    checkNotNull(setter, "setter");
    if (State.DISABLED.equals(state.getInternal())) {
      return;
    }

    try {
      StringBuilder stringBuilder = new StringBuilder(TAGCONTEXT_SERIALIZED_SIZE_LIMIT);
      int totalChars = 0; // Here chars are equivalent to bytes, since we're using ascii chars.
      int totalTags = 0;
      for (Iterator<Tag> i = InternalUtils.getTags(tagContext); i.hasNext(); ) {
        Tag tag = i.next();
        if (TagTtl.NO_PROPAGATION.equals(tag.getTagMetadata().getTagTtl())) {
          continue;
        }
        if (stringBuilder.length() > 0) {
          stringBuilder.append(TAG_DELIMITER);
        }
        totalTags++;
        totalChars += encodeTag(tag, stringBuilder);
      }
      checkArgument(
          totalTags <= MAX_NUMBER_OF_TAGS,
          "Number of tags in the TagContext exceeds limit " + MAX_NUMBER_OF_TAGS);
      // Note per W3C spec, only the length of tag key and value counts towards the total length.
      // Length of properties (a.k.a TagMetadata) does not count.
      checkArgument(
          totalChars <= TAGCONTEXT_SERIALIZED_SIZE_LIMIT,
          "Size of TagContext exceeds the maximum serialized size "
              + TAGCONTEXT_SERIALIZED_SIZE_LIMIT);
      setter.put(carrier, CORRELATION_CONTEXT, stringBuilder.toString());
    } catch (IllegalArgumentException e) {
      throw new TagContextSerializationException("Failed to serialize TagContext", e);
    }
  }

  // Encodes the tag to the given string builder, and returns the length of encoded key-value pair.
  private static int encodeTag(Tag tag, StringBuilder stringBuilder) {
    String key = tag.getKey().getName();
    String value = tag.getValue().asString();
    int charsOfTag = key.length() + value.length();
    // This should never happen with our current constraints (<= 255 chars) on tags.
    checkArgument(
        charsOfTag <= TAG_SERIALIZED_SIZE_LIMIT,
        "Serialized size of tag " + tag + " exceeds limit " + TAG_SERIALIZED_SIZE_LIMIT);

    // TODO(songy23): do we want to encode TagMetadata?
    stringBuilder.append(key).append(TAG_KEY_VALUE_DELIMITER).append(value);
    return charsOfTag;
  }

  @Override
  public <C /*>>> extends @NonNull Object*/> TagContext extract(C carrier, Getter<C> getter)
      throws TagContextDeserializationException {
    checkNotNull(carrier, "carrier");
    checkNotNull(getter, "getter");
    if (State.DISABLED.equals(state.getInternal())) {
      return TagMapImpl.EMPTY;
    }

    @Nullable String correlationContext = getter.get(carrier, CORRELATION_CONTEXT);
    if (correlationContext == null) {
      throw new TagContextDeserializationException(CORRELATION_CONTEXT + " not present.");
    }
    try {
      if (correlationContext.isEmpty()) {
        return TagMapImpl.EMPTY;
      }
      Map<TagKey, TagValueWithMetadata> tags = new HashMap<>();
      List<String> stringTags = TAG_DELIMITER_SPLITTER.splitToList(correlationContext);
      for (String stringTag : stringTags) {
        List<String> keyValuePair = TAG_KEY_VALUE_DELIMITER_SPLITTER.splitToList(stringTag);
        checkArgument(keyValuePair.size() == 2, "Malformed tag " + stringTag);
        TagKey key = TagKey.create(keyValuePair.get(0));
        TagValueWithMetadata valueWithMetadata =
            TagValueWithMetadata.create(
                TagValue.create(keyValuePair.get(1)), METADATA_UNLIMITED_PROPAGATION);
        tags.put(key, valueWithMetadata);
      }
      return new TagMapImpl(tags);
    } catch (IllegalArgumentException e) {
      throw new TagContextDeserializationException("Invalid TagContext: " + correlationContext, e);
    }
  }
}
