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

import io.grpc.Context;
import io.opencensus.common.NonThrowingCloseable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Util methods/functionality to interact with the {@link io.grpc.Context}.
 *
 * <p>Users must interact with the current Context via the public APIs in {@link
 * WithTagContext} and avoid usages of the {@link #STATS_CONTEXT_KEY} directly.
 */
public final class TagContextExplicit {
  /**
   * Returns The {@link TagContext} from the current context.
   *
   * @return The {@code TagContext} from the current context.
   */
  public static TagContext getCurrentTagContext() {
    TagContext context = STATS_CONTEXT_KEY.get(Context.current());
    return context == null ? EMPTY : context;
  }

  /**
   * Creates a {@link TagContext} from the given on-the-wire encoded representation.
   *
   * <p>Should be the inverse of {@link serialize(TagContext, java.io.OutputStream)}. The
   * serialized representation should be based on the {@link TagContext} binary representation.
   *
   * @param input on-the-wire representation of a {@link TagContext}
   * @return a {@link TagContext} deserialized from {@code input}
   */
  // TODO(dpo): import current implementation
  public TagContext deserialize(InputStream input) throws IOException {
    return EMPTY;
  }

  /**
   * Serializes the given {@link TagContext} into the on-the-wire representation.
   *
   * <p>The inverse of {@link Stats#deserialize(java.io.InputStream)}.
   *
   * @param ctxt the {@link TagContext} to serialize
   * @param output the {@link OutputStream} to add the serialized form of the givne {@link TagContext}
   */
  // TODO(dpo): import current implementation
  public void serialize(TagContext ctxt, OutputStream output) throws IOException {
    return;
  }

  /**
   * The {@link io.grpc.Context.Key} used to interact with {@link io.grpc.Context}.
   * DO NOT USE.
   */
  public static final Context.Key<TagContext> STATS_CONTEXT_KEY = Context.key(
      "instrumentation-stats-key");

  private static final TagContext EMPTY =
      new TagContext(new HashMap<TagKey.StringTagKey, String>());

  // Static class.
  private TagContextExplicit() {
  }

  /**
   * Enters the scope of code where the given {@link TagContext} is in the current Context, and
   * returns an object that represents that scope. The scope is exited when the returned object is
   * closed.
   */
  public static final class WithTagContext implements NonThrowingCloseable {
    private final Context origContext;

    private WithTagContext(TagContext tagContext) {
      origContext = Context.current().withValue(STATS_CONTEXT_KEY, tagContext).attach();
    }

    /**
     * Enters the scope of code where the given {@link TagContext} is in the current context, and
     * returns an object that represents that scope. The scope is exited when the returned object
     * is closed.
     *
     * <p>Supports try-with-resource idiom.
     *
     * @param tagContext The {@code TagContext} to be set to the current context.
     * @return An object that defines a scope where the given {@code TagContext} is set to the
     *     current context.
     */
    public static WithTagContext create(TagContext tagContext) {
      return new WithTagContext(tagContext);
    }

    @Override
    public void close() {
      Context.current().detach(origContext);
    }
  }
}
