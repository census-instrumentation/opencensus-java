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

package io.opencensus.implcore.tags.propagation;

import io.opencensus.implcore.tags.CurrentTaggingState;
import io.opencensus.implcore.tags.TagContextImpl;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TaggingState;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagContextDeserializationException;

final class TagContextBinarySerializerImpl extends TagContextBinarySerializer {
  private static final byte[] EMPTY_BYTE_ARRAY = {};

  private final CurrentTaggingState state;

  TagContextBinarySerializerImpl(CurrentTaggingState state) {
    this.state = state;
  }

  @Override
  public byte[] toByteArray(TagContext tags) {
    return state.getInternal() == TaggingState.DISABLED
        ? EMPTY_BYTE_ARRAY
        : SerializationUtils.serializeBinary(tags);
  }

  @Override
  public TagContext fromByteArray(byte[] bytes) throws TagContextDeserializationException {
    return state.getInternal() == TaggingState.DISABLED
        ? TagContextImpl.EMPTY
        : SerializationUtils.deserializeBinary(bytes);
  }
}
