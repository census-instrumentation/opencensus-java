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

import io.opencensus.implcore.internal.CurrentState;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagContextTextSerializer;
import io.opencensus.tags.propagation.TagPropagationComponent;

/** Implementation of {@link TagPropagationComponent}. */
public final class TagPropagationComponentImpl extends TagPropagationComponent {
  private final TagContextBinarySerializer tagContextBinarySerializer;

  public TagPropagationComponentImpl(CurrentState state) {
    tagContextBinarySerializer = new TagContextBinarySerializerImpl(state);
  }

  @Override
  public TagContextBinarySerializer getBinarySerializer() {
    return tagContextBinarySerializer;
  }

  @Override
  public TagContextTextSerializer getTextSerializer() {
    throw new UnsupportedOperationException("not implemented");
  }
}
