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

/** Object for serializing and deserializing {@link TagContext}s with the binary format. */
public abstract class TagContextBinarySerializer {

  /**
   * Serializes the {@code TagContext} into the on-the-wire representation.
   *
   * <p>This method should be the inverse of {@link #fromByteArray}.
   *
   * @param tags the {@code TagContext} to serialize.
   * @return the on-the-wire representation of a {@code TagContext}.
   */
  public abstract byte[] toByteArray(TagContext tags);

  /**
   * Creates a {@code TagContext} from the given on-the-wire encoded representation.
   *
   * <p>This method should be the inverse of {@link #toByteArray}.
   *
   * @param bytes on-the-wire representation of a {@code TagContext}.
   * @return a {@code TagContext} deserialized from {@code bytes}.
   * @throws TagContextParseException if there is a parse error.
   */
  // TODO(sebright): Use a more appropriate exception type, since this method doesn't do IO.
  public abstract TagContext fromByteArray(byte[] bytes) throws TagContextParseException;
}
