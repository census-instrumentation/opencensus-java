/*
 * Copyright 2020, OpenCensus Authors
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

package io.opencensus.implcore.trace;

import io.opencensus.common.Clock;
import io.opencensus.implcore.internal.TimestampConverter;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.config.TraceParams;
import java.util.logging.Level;
import javax.annotation.Nullable;

public final class RecordEventsSpanImplWithFinalizer extends RecordEventsSpanImpl {

  protected RecordEventsSpanImplWithFinalizer(
      SpanContext context,
      String name,
      @Nullable Kind kind,
      @Nullable SpanId parentSpanId,
      @Nullable Boolean hasRemoteParent,
      TraceParams traceParams,
      StartEndHandler startEndHandler,
      @Nullable TimestampConverter timestampConverter,
      Clock clock) {
    super(
        context,
        name,
        kind,
        parentSpanId,
        hasRemoteParent,
        traceParams,
        startEndHandler,
        timestampConverter,
        clock);
  }

  @SuppressWarnings("NoFinalizer")
  @Override
  protected void finalize() throws Throwable {
    synchronized (this) {
      if (!hasBeenEnded) {
        logger.log(Level.SEVERE, "Span " + name + " is GC'ed without being ended.");
      }
    }
    super.finalize();
  }
}
