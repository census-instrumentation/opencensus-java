/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.implcore.trace.export;

import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.trace.export.RunningSpanStore;
import io.opencensus.trace.export.SpanData;
import java.util.Collection;
import java.util.Collections;

/** Abstract implementation of the {@link RunningSpanStore}. */
public abstract class RunningSpanStoreImpl extends RunningSpanStore {

  private static final RunningSpanStoreImpl NOOP_RUNNING_SPAN_STORE_IMPL =
      new NoopRunningSpanStoreImpl();

  /** Returns the no-op implementation of the {@link RunningSpanStoreImpl}. */
  static RunningSpanStoreImpl getNoopRunningSpanStoreImpl() {
    return NOOP_RUNNING_SPAN_STORE_IMPL;
  }

  /**
   * Adds the {@code Span} into the running spans list when the {@code Span} starts.
   *
   * @param span the {@code Span} that started.
   */
  public abstract void onStart(RecordEventsSpanImpl span);

  /**
   * Removes the {@code Span} from the running spans list when the {@code Span} ends.
   *
   * @param span the {@code Span} that ended.
   */
  public abstract void onEnd(RecordEventsSpanImpl span);

  private static final class NoopRunningSpanStoreImpl extends RunningSpanStoreImpl {

    private static final Summary EMPTY_SUMMARY =
        RunningSpanStore.Summary.create(Collections.<String, PerSpanNameSummary>emptyMap());

    @Override
    public void onStart(RecordEventsSpanImpl span) {}

    @Override
    public void onEnd(RecordEventsSpanImpl span) {}

    @Override
    public Summary getSummary() {
      return EMPTY_SUMMARY;
    }

    @Override
    public Collection<SpanData> getRunningSpans(Filter filter) {
      return Collections.<SpanData>emptyList();
    }
  }
}
