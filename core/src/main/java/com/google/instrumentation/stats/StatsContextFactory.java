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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * Factory class for {@link StatsContext}.
 */
public abstract class StatsContextFactory {
  /**
   * Creates a {@link StatsContext} from the given on-the-wire encoded representation.
   *
   * <p>Should be the inverse of {@link StatsContext#serialize()}. The
   * serialized representation should be based on the {@link StatsContext} binary representation.
   *
   * @param input on-the-wire representation of a {@link StatsContext}
   * @return a {@link StatsContext} deserialized from {@code input}
   */
  public abstract StatsContext deserialize(byte[] input) throws IOException, ParseException;

  /**
   * Old version of {@link StatsContext} deserialization.
   */
  @Deprecated
  public abstract StatsContext deserialize(InputStream input) throws IOException;

  /**
   * Returns the default {@link StatsContext}.
   */
  public abstract StatsContext getDefault();
}
