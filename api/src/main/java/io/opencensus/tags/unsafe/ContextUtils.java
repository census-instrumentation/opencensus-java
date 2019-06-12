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

package io.opencensus.tags.unsafe;

import io.grpc.Context;
import io.opencensus.internal.Utils;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import java.util.Collections;
import java.util.Iterator;
import javax.annotation.concurrent.Immutable;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * Utility methods for accessing the {@link TagContext} contained in the {@link io.grpc.Context}.
 *
 * <p>Most code should interact with the current context via the public APIs in {@link
 * io.opencensus.tags.TagContext} and avoid accessing {@link #TAG_CONTEXT_KEY} directly.
 *
 * @since 0.8
 */
public final class ContextUtils {
  private static final TagContext EMPTY_TAG_CONTEXT = new EmptyTagContext();

  private ContextUtils() {}

  /**
   * The {@link io.grpc.Context.Key} used to interact with the {@code TagContext} contained in the
   * {@link io.grpc.Context}.
   */
  private static final Context.Key</*@Nullable*/ TagContext> TAG_CONTEXT_KEY =
      Context.keyWithDefault("opencensus-tag-context-key", EMPTY_TAG_CONTEXT);

  /**
   * Creates a new {@code Context} with the given value set.
   *
   * @param context the parent {@code Context}.
   * @param tagContext the value to be set.
   * @return a new context with the given value set.
   * @since 0.21
   */
  public static Context withValue(
      Context context, @javax.annotation.Nullable TagContext tagContext) {
    return Utils.checkNotNull(context, "context").withValue(TAG_CONTEXT_KEY, tagContext);
  }

  /**
   * Returns the value from the specified {@code Context}.
   *
   * @param context the specified {@code Context}.
   * @return the value from the specified {@code Context}.
   * @since 0.21
   */
  public static TagContext getValue(Context context) {
    @javax.annotation.Nullable TagContext tags = TAG_CONTEXT_KEY.get(context);
    return tags == null ? EMPTY_TAG_CONTEXT : tags;
  }

  @Immutable
  private static final class EmptyTagContext extends TagContext {

    @Override
    protected Iterator<Tag> getIterator() {
      return Collections.<Tag>emptySet().iterator();
    }
  }
}
