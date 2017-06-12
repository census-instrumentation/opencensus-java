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

package io.opencensus.trace.base;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.Span;
import io.opencensus.trace.config.TraceConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that enables overriding the default values used when starting a {@link Span}. Allows
 * overriding the {@link Sampler sampler}, the parent links, and option to record all the events
 * even if the {@code Span} is not sampled.
 */
@AutoValue
@Immutable
public abstract class StartSpanOptions {
  private static final List<Span> EMPTY_PARENT_LINKS_LIST = Collections.emptyList();

  /** The default {@code StartSpanOptions}. */
  @VisibleForTesting public static final StartSpanOptions DEFAULT = builder().build();

  /**
   * Returns the {@link Sampler} to be used, or {@code null} if default.
   *
   * @return the {@code Sampler} to be used, or {@code null} if default.
   */
  @Nullable
  public abstract Sampler getSampler();

  /**
   * Returns the parent links to be set for the {@link Span}.
   *
   * @return the parent links to be set for the {@code Span}.
   */
  public abstract List<Span> getParentLinks();

  /**
   * Returns the record events option, or {@code null} if default.
   *
   * <p>See {@link Span.Options#RECORD_EVENTS} for more details.
   *
   * @return the record events option, or {@code null} if default.
   */
  @Nullable
  public abstract Boolean getRecordEvents();

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   */
  public static Builder builder() {
    return new AutoValue_StartSpanOptions.Builder().setParentLinks(EMPTY_PARENT_LINKS_LIST);
  }

  /** Builder class for {@link StartSpanOptions}. */
  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * Sets the {@link Sampler} to be used. If {@code null} the default {@code Sampler} from the
     * {@link TraceConfig#getActiveTraceParams()} will be used.
     *
     * @param sampler the {@link Sampler} to be used.
     * @return this.
     */
    public abstract Builder setSampler(@Nullable Sampler sampler);

    /**
     * Sets the parent links to be set for the {@link Span}.
     *
     * @param parentLinks the parent links to be set for the {@link Span}.
     * @return this.
     * @throws NullPointerException if {@code parentLinks} is {@code null}.
     */
    public abstract Builder setParentLinks(List<Span> parentLinks);

    /**
     * Sets the record events option. If {@code null} the default value from the {@link
     * TraceConfig#getActiveTraceParams()} will be used.
     *
     * <p>See {@link Span.Options#RECORD_EVENTS} for more details.
     *
     * @param recordEvents the record events option.
     * @return this.
     */
    public abstract Builder setRecordEvents(@Nullable Boolean recordEvents);

    abstract List<Span> getParentLinks(); // not public

    abstract StartSpanOptions autoBuild(); // not public

    /**
     * Builds and returns a {@code StartSpanOptions} with the desired settings.
     *
     * @return a {@code StartSpanOptions} with the desired settings.
     */
    public StartSpanOptions build() {
      setParentLinks(Collections.unmodifiableList(new ArrayList<Span>(getParentLinks())));
      return autoBuild();
    }
  }

  StartSpanOptions() {}
}
