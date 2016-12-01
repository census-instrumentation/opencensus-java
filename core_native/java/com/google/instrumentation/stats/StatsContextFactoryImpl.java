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

package com.google.instrumentation.stats;

import java.nio.ByteBuffer;
import java.util.HashMap;
import javax.annotation.Nullable;

/**
 * Native Implementation of {@link StatsContextFactory}.
 */
public final class StatsContextFactoryImpl extends StatsContextFactory {
  static final StatsContextImpl DEFAULT = new StatsContextImpl(new HashMap<String, String>(0));

  public StatsContextFactoryImpl() {}

  /**
   * Deserializes a {@link StatsContextImpl} from a serialized {@code StatsContextProto}.
   *
   * <p>The encoded tags are of the form: {@code <tag prefix> + 'key' + <tag delim> + 'value'}*
   */
  @Override
  @Nullable
  public StatsContextImpl deserialize(ByteBuffer buffer) {
    return StatsSerializer.deserialize(buffer);
  }

  @Override
  public StatsContext getDefault() {
    return DEFAULT;
  }
}
