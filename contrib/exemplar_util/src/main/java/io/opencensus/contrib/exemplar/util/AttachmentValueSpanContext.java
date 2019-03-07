/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.contrib.exemplar.util;

import com.google.auto.value.AutoValue;
import io.opencensus.common.AttachmentValue;
import io.opencensus.trace.SpanContext;
import javax.annotation.concurrent.Immutable;

/**
 * {@link SpanContext} {@link AttachmentValue}.
 *
 * <p>Added in the contrib util package because we want to avoid dependency on Tracing API in Stats
 * APIs.
 *
 * @since 0.20
 */
@Immutable
@AutoValue
public abstract class AttachmentValueSpanContext extends AttachmentValue {

  AttachmentValueSpanContext() {}

  /**
   * Returns the span context attachment value.
   *
   * @return the span context attachment value.
   * @since 0.20
   */
  public abstract SpanContext getSpanContext();

  /**
   * Creates an {@link AttachmentValueSpanContext}.
   *
   * @param spanContext the span context value.
   * @return an {@code AttachmentValueSpanContext}.
   * @since 0.20
   */
  public static AttachmentValueSpanContext create(SpanContext spanContext) {
    return new AutoValue_AttachmentValueSpanContext(spanContext);
  }

  @Override
  public final String getValue() {
    return getSpanContext().toString();
  }
}
