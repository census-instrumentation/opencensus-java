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
import io.opencensus.trace.export.SampledSpanStore;
import io.opencensus.trace.export.SpanData;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/** Abstract implementation of the {@link SampledSpanStore}. */
public abstract class SampledSpanStoreImpl extends SampledSpanStore {
  private static final SampledSpanStoreImpl NOOP_SAMPLED_SPAN_STORE_IMPL =
      new NoopSampledSpanStoreImpl();

  /** Returns the new no-op implmentation of {@link SampledSpanStoreImpl}. */
  public static SampledSpanStoreImpl getNoopSampledSpanStoreImpl() {
    return NOOP_SAMPLED_SPAN_STORE_IMPL;
  }

  /**
   * Considers to save the given spans to the stored samples. This must be called at the end of each
   * Span with the option RECORD_EVENTS.
   *
   * @param span the span to be consider for storing into the store buckets.
   */
  public abstract void considerForSampling(RecordEventsSpanImpl span);

  protected void shutdown() {}

  private static final class NoopSampledSpanStoreImpl extends SampledSpanStoreImpl {
    private static final Summary EMPTY_SUMMARY =
        Summary.create(Collections.<String, PerSpanNameSummary>emptyMap());
    private static final Set<String> EMPTY_REGISTERED_SPAN_NAMES = Collections.<String>emptySet();
    private static final Collection<SpanData> EMPTY_SPANDATA = Collections.<SpanData>emptySet();

    @Override
    public Summary getSummary() {
      return EMPTY_SUMMARY;
    }

    @Override
    public void considerForSampling(RecordEventsSpanImpl span) {}

    @Override
    @SuppressWarnings("deprecation")
    public void registerSpanNamesForCollection(Collection<String> spanNames) {}

    @Override
    @SuppressWarnings("deprecation")
    public void unregisterSpanNamesForCollection(Collection<String> spanNames) {}

    @Override
    public Set<String> getRegisteredSpanNamesForCollection() {
      return EMPTY_REGISTERED_SPAN_NAMES;
    }

    @Override
    public Collection<SpanData> getErrorSampledSpans(ErrorFilter filter) {
      return EMPTY_SPANDATA;
    }

    @Override
    public Collection<SpanData> getLatencySampledSpans(LatencyFilter filter) {
      return EMPTY_SPANDATA;
    }
  }
}
