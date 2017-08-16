/*
 * Copyright 2017, Google Inc.
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

package io.opencensus.tags;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.concurrent.Immutable;

/** Object for serializing and deserializing {@link TagContext}s with the binary format. */
public abstract class TagContextBinarySerializer {
  private static final TagContextBinarySerializer NOOP_TAG_CONTEXT_BINARY_SERIALIZER =
      new NoopTagContextBinarySerializer();

  /**
   * Serializes the {@code TagContext} into the on-the-wire representation.
   *
   * <p>This method should be the inverse of {@link #deserialize}.
   *
   * @param tags the {@code TagContext} to serialize.
   * @param output the {@link OutputStream} to write the serialized tags to.
   * @throws IOException if there is an {@code IOException} while writing to {@code output}.
   */
  public abstract void serialize(TagContext tags, OutputStream output) throws IOException;

  /**
   * Creates a {@code TagContext} from the given on-the-wire encoded representation.
   *
   * <p>This method should be the inverse of {@link #serialize}.
   *
   * @param input on-the-wire representation of a {@code TagContext}.
   * @return a {@code TagContext} deserialized from {@code input}.
   * @throws IOException if there is a parse error or an {@code IOException} while reading from
   *     {@code input}.
   */
  public abstract TagContext deserialize(InputStream input) throws IOException;

  /**
   * Returns a {@code TagContextBinarySerializer} that serializes all {@code TagContext}s to zero
   * bytes and deserializes all inputs to empty {@code TagContext}s.
   */
  static TagContextBinarySerializer getNoopTagContextBinarySerializer() {
    return NOOP_TAG_CONTEXT_BINARY_SERIALIZER;
  }

  @Immutable
  private static final class NoopTagContextBinarySerializer extends TagContextBinarySerializer {

    @Override
    public void serialize(TagContext tags, OutputStream output) throws IOException {}

    @Override
    public TagContext deserialize(InputStream input) throws IOException {
      return TagContext.getNoopTagContext();
    }
  }
}
