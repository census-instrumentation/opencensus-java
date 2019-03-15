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

package io.opencensus.tags.propagation;

import io.opencensus.tags.TagContext;

/**
 * Object for serializing and deserializing {@link TagContext}s with HTTP text format.
 *
 * <p>OpenCensus uses W3C Correlation Context as the HTTP text format. For more details, see <a
 * href="https://github.com/w3c/correlation-context">correlation-context</a>.
 *
 * @since 0.21
 */
public abstract class TagContextTextSerializer {

  /**
   * Serializes the {@code TagContext} into the plain text representation.
   *
   * <p>This method should be the inverse of {@link #fromText(String)}.
   *
   * @param tags the {@code TagContext} to serialize.
   * @return the plain text representation of a {@code TagContext}.
   * @throws TagContextSerializationException if the result would be larger than the maximum allowed
   *     serialized size.
   * @since 0.21
   */
  public abstract String toText(TagContext tags) throws TagContextSerializationException;

  /**
   * Creates a {@code TagContext} from the given plain text encoded representation.
   *
   * <p>This method should be the inverse of {@link #toText(TagContext)}.
   *
   * @param text plain text representation of a {@code TagContext}.
   * @return a {@code TagContext} deserialized from {@code text}.
   * @throws TagContextDeserializationException if there is a parse error, the input contains
   *     invalid tags, or the input is larger than the maximum allowed serialized size.
   * @since 0.21
   */
  public abstract TagContext fromText(String text) throws TagContextDeserializationException;
}
