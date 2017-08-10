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

package io.opencensus.tags.unsafe;

import io.grpc.Context;
import io.opencensus.tags.TagContext;

/**
 * Utility methods for accessing the {@link TagContext} contained in the {@link io.grpc.Context}.
 *
 * <p>Most code should interact with the current context via the public APIs in {@link
 * io.opencensus.tags.TagContext} and avoid accessing {@link #TAG_CONTEXT_KEY} directly.
 */
public final class ContextUtils {
  private ContextUtils() {}

  /**
   * The {@link io.grpc.Context.Key} used to interact with the {@code TagContext} contained in the
   * {@link io.grpc.Context}.
   */
  public static final Context.Key<TagContext> TAG_CONTEXT_KEY =
      Context.key("opencensus-tag-context-key");
}
