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

/**
 * Enters the scope of code where the given {@link TagContext} is in the current Context, and
 * returns an object that represents that scope. The scope is exited when the returned object is
 * closed.
 */
public class WithTagContext implements NonThrowingCloseable {
  private final Context origContext;

  private WithTagContext(TagContext tagContext) {
    origContext =
        Context.current().withValue(TagContextExplicit.STATS_CONTEXT_KEY, tagContext).attach();
  }

  // TODO(dpo): Consider for later.
  // /** Shorthand for builder().set(k1, v1).build() */
  // public final TagContext set(TagKey k1, String v1) {
  //   return builder().set(k1, v1).build();
  // }

  // /** Shorthand for builder().set(k1, v1).set(k2, v2).build() */
  // public final TagContext set(TagKey k1, String v1, TagKey k2, String v2) {
  //   return builder().set(k1, v1).set(k2, v2).build();
  // }

  // /** Shorthand for builder().set(k1, v1).set(k2, v2).set(k3, v3).build() */
  // public final TagContext set(
  //     TagKey k1, String v1, TagKey k2, String v2, TagKey k3, String v3) {
  //   return builder().set(k1, v1).set(k2, v2).set(k3, v3).build();
  // }

  public static final Builder builder() {
    // TODO(dpo): make sure null case is handled with default.
    return new Builder(TagContextExplicit.getCurrentTagContext());
  }

  @Override
  public void close() {
    Context.current().detach(origContext);
  }

  public static final class Builder {
    private TagContext.Builder bld;

    // TODO(dpo): provide support.
    // public Builder insert(TagKey.StringTagKey key, String value) {
    //   bld.insert(key, value);
    //   return this;
    // }


    public Builder set(TagKey.StringTagKey key, String value) {
      bld.set(key, value);
      return this;
    }

    public Builder clear(TagKey.StringTagKey key) {
      bld.clear(key);
      return this;
    }

    public WithTagContext build() {
      return new WithTagContext(bld.build());
    }

    private Builder(TagContext ctxt) {
      // toBuidler()?
      bld = ctxt.toBuilder();
    }
  }
}
