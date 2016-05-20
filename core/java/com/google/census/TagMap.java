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

import com.google.common.collect.UnmodifiableIterator;

import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * A map from Census tag keys to tag values.
 */
public class TagMap implements Iterable<Entry<String, String>> {
  /**
   * Returns the empty {@link TagMap}.
   */
  public static TagMap of() {
    return new Builder().build();
  }

  /**
   * Returns a {@link TagMap} consisting of the given key/value mapping.
   */
  public static TagMap of(TagKey key1, String value1) {
    return new Builder().put(key1, value1).build();
  }

  /**
   * Returns a {@link TagMap} consisting of the given key/value mappings.
   */
  public static TagMap of(TagKey key1, String value1, TagKey key2, String value2) {
    return new Builder().put(key1, value1).put(key2, value2).build();
  }

  /**
   * Returns a {@link TagMap} consisting of the given key/value mappings.
   */
  public static TagMap of(
      TagKey key1, String value1, TagKey key2, String value2, TagKey key3, String value3) {
    return new Builder().put(key1, value1).put(key2, value2).put(key3, value3).build();
  }

  /**
   * Returns a {@link Builder} for the {@link TagMap} class.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the number of tags in this {@link TagMap}.
   */
  public int size() {
    return tags.size();
  }

  /**
   * Returns an {@link UnmodifiableIterator} over the key/value mappings in this {@link TagMap}.
   */
  @Override
  public UnmodifiableIterator<Entry<String, String>> iterator() {
    return new Iterator();
  }

  final ArrayList<Tag> tags;

  private TagMap(ArrayList<Tag> tags) {
    this.tags = tags;
  }

  /** Builder class for {@link TagMap}. */
  public static class Builder {
    /**
     * Associates the given tag key with the given tag value. Subsequent updates to the same key
     * are ignored.
     *
     * @param key the key
     * @param value the value to be associated with {@code key}
     * @return {@code this}
     */
    public Builder put(TagKey key, TagValue value) {
      tags.add(new Tag(key.toString(), value.toString()));
      return this;
    }

    /**
     * Associates the given tag key with the given tag value. Subsequent updates to the same key
     * are ignored.
     *
     * @param key the key
     * @param value the value to be associated with {@code key}
     * @return {@code this}
     */
    public Builder put(TagKey key, String value) {
      return put(key, new TagValue(value));
    }

    /**
     * Builds a {@link TagMap} from the specified keys and values.
     *
     * @return {@link TagMap} comprised of the given keys and values.
     */
    public TagMap build() {
      // Note: this makes adding tags quadratic but is fastest for the sizes of TagMaps that we
      // should see. We may want to go to a strategy of sort/eliminate for larger TagMaps.
      for (int i = 0; i < tags.size(); i++) {
        String current = tags.get(i).getKey();
        for (int j = i + 1; j < tags.size(); j++) {
          if (current.equals(tags.get(j).getKey())) {
            tags.remove(j);
            j--;
          }
        }
      }
      return new TagMap(tags);
    }

    private final ArrayList<Tag> tags = new ArrayList<>();

    private Builder() {
    }
  }

  // Provides an UnmodifiableIterator over this instance's Tags.
  private class Iterator extends UnmodifiableIterator<Entry<String, String>> {
    @Override
    public boolean hasNext() {
      return position < length;
    }

    @Override
    public Entry<String, String> next() {
      return tags.get(position++);
    }

    private final int length = tags.size();
    private int position = 0;
  }
}
