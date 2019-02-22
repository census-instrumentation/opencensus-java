/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.tags;

import com.google.auto.value.AutoValue;
import io.opencensus.tags.TagMetadata.TagTtl;
import javax.annotation.concurrent.Immutable;

/**
 * {@link TagKey} paired with a {@link TagValue}.
 *
 * @since 0.8
 */
@Immutable
@AutoValue
public abstract class Tag {

  private static final TagMetadata METADATA_UNLIMITED_PROPAGATION =
      TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION);

  Tag() {}

  /**
   * Creates a {@code Tag} from the given key and value.
   *
   * <p>This is equivalent to calling {@code create(key, value,
   * TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION))}.
   *
   * @param key the tag key.
   * @param value the tag value.
   * @return a {@code Tag} with the given key and value.
   * @since 0.8
   * @deprecated in favor of {@link #create(TagKey, TagValue, TagMetadata)}.
   */
  @Deprecated
  public static Tag create(TagKey key, TagValue value) {
    return create(key, value, METADATA_UNLIMITED_PROPAGATION);
  }

  /**
   * Creates a {@code Tag} from the given key, value and metadata.
   *
   * @param key the tag key.
   * @param value the tag value.
   * @param tagMetadata the tag metadata.
   * @return a {@code Tag}.
   * @since 0.20
   */
  public static Tag create(TagKey key, TagValue value, TagMetadata tagMetadata) {
    return new AutoValue_Tag(key, value, tagMetadata);
  }

  /**
   * Returns the tag's key.
   *
   * @return the tag's key.
   * @since 0.8
   */
  public abstract TagKey getKey();

  /**
   * Returns the tag's value.
   *
   * @return the tag's value.
   * @since 0.8
   */
  public abstract TagValue getValue();

  /**
   * Returns the {@link TagMetadata} associated with this {@link Tag}.
   *
   * @return the {@code TagMetadata}.
   * @since 0.20
   */
  public abstract TagMetadata getTagMetadata();
}
