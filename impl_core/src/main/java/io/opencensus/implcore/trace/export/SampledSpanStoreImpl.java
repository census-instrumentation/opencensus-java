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

package io.opencensus.implcore.trace.export;

import com.google.common.collect.EvictingQueue;
import io.opencensus.implcore.trace.SpanImpl;
import io.opencensus.trace.Status;
import io.opencensus.trace.Status.CanonicalCode;
import io.opencensus.trace.export.SampledSpanStore;
import io.opencensus.trace.export.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation of the {@link SampledSpanStore}. */
@ThreadSafe
public final class SampledSpanStoreImpl extends SampledSpanStore {
  private static final int NUM_SAMPLES_PER_LATENCY_BUCKET = 10;
  private static final int NUM_SAMPLES_PER_ERROR_BUCKET = 5;
  private static final long TIME_BETWEEN_SAMPLES = TimeUnit.SECONDS.toNanos(1);
  private static final int NUM_LATENCY_BUCKETS = LatencyBucketBoundaries.values().length;
  // The total number of canonical codes - 1 (the OK code).
  private static final int NUM_ERROR_BUCKETS = CanonicalCode.values().length - 1;
  private static final int MAX_PER_SPAN_NAME_SAMPLES =
      NUM_SAMPLES_PER_LATENCY_BUCKET * NUM_LATENCY_BUCKETS
          + NUM_SAMPLES_PER_ERROR_BUCKET * NUM_ERROR_BUCKETS;

  @GuardedBy("samples")
  private final Map<String, PerSpanNameSamples> samples;

  private static final class Bucket {

    private final EvictingQueue<SpanImpl> sampledSpansQueue;
    private final EvictingQueue<SpanImpl> notSampledSpansQueue;
    private long lastSampledNanoTime;
    private long lastNotSampledNanoTime;

    private Bucket(int numSamples) {
      sampledSpansQueue = EvictingQueue.create(numSamples);
      notSampledSpansQueue = EvictingQueue.create(numSamples);
    }

    private void considerForSampling(SpanImpl span) {
      long spanEndNanoTime = span.getEndNanoTime();
      if (span.getContext().getTraceOptions().isSampled()) {
        // Need to compare by doing the subtraction all the time because in case of an overflow,
        // this may never sample again (at least for the next ~200 years). No real chance to
        // overflow two times because that means the process runs for ~200 years.
        if (spanEndNanoTime - lastSampledNanoTime > TIME_BETWEEN_SAMPLES) {
          sampledSpansQueue.add(span);
          lastSampledNanoTime = spanEndNanoTime;
        }
      } else {
        // Need to compare by doing the subtraction all the time because in case of an overflow,
        // this may never sample again (at least for the next ~200 years). No real chance to
        // overflow two times because that means the process runs for ~200 years.
        if (spanEndNanoTime - lastNotSampledNanoTime > TIME_BETWEEN_SAMPLES) {
          notSampledSpansQueue.add(span);
          lastNotSampledNanoTime = spanEndNanoTime;
        }
      }
    }

    private void getSamples(int maxSpansToReturn, List<SpanImpl> output) {
      getSamples(sampledSpansQueue, maxSpansToReturn, output);
      getSamples(notSampledSpansQueue, maxSpansToReturn, output);
    }

    private static void getSamples(EvictingQueue<SpanImpl> queue, int maxSpansToReturn,
        List<SpanImpl> output) {
      for (SpanImpl span : queue) {
        if (output.size() >= maxSpansToReturn) {
          break;
        }
        output.add(span);
      }
    }

    private void getSamplesFilteredByLatency(
        long latencyLowerNs, long latencyUpperNs, int maxSpansToReturn, List<SpanImpl> output) {
      getSamplesFilteredByLatency(sampledSpansQueue, latencyLowerNs, latencyUpperNs,
          maxSpansToReturn, output);
      getSamplesFilteredByLatency(notSampledSpansQueue, latencyLowerNs, latencyUpperNs,
          maxSpansToReturn, output);
    }

    private static void getSamplesFilteredByLatency(EvictingQueue<SpanImpl> queue,
        long latencyLowerNs, long latencyUpperNs, int maxSpansToReturn, List<SpanImpl> output) {
      for (SpanImpl span : queue) {
        if (output.size() >= maxSpansToReturn) {
          break;
        }
        long spanLatencyNs = span.getLatencyNs();
        if (spanLatencyNs >= latencyLowerNs && spanLatencyNs < latencyUpperNs) {
          output.add(span);
        }
      }
    }

    private int getNumSamples() {
      return sampledSpansQueue.size() + notSampledSpansQueue.size();
    }
  }

  /**
   * Keeps samples for a given span name. Samples for all the latency buckets and for all canonical
   * codes other than OK.
   */
  private static final class PerSpanNameSamples {

    private final Bucket[] latencyBuckets;
    private final Bucket[] errorBuckets;

    private PerSpanNameSamples() {
      latencyBuckets = new Bucket[NUM_LATENCY_BUCKETS];
      for (int i = 0; i < NUM_LATENCY_BUCKETS; i++) {
        latencyBuckets[i] = new Bucket(NUM_SAMPLES_PER_LATENCY_BUCKET);
      }
      errorBuckets = new Bucket[NUM_ERROR_BUCKETS];
      for (int i = 0; i < NUM_ERROR_BUCKETS; i++) {
        errorBuckets[i] = new Bucket(NUM_SAMPLES_PER_ERROR_BUCKET);
      }
    }

    private Bucket getLatencyBucket(long latencyNs) {
      for (int i = 0; i < NUM_LATENCY_BUCKETS; i++) {
        LatencyBucketBoundaries boundaries = LatencyBucketBoundaries.values()[i];
        if (latencyNs >= boundaries.getLatencyLowerNs()
            && latencyNs < boundaries.getLatencyUpperNs()) {
          return latencyBuckets[i];
        }
      }
      // latencyNs is negative or Long.MAX_VALUE, so this Span can be ignored. This cannot happen
      // in real production because System#nanoTime is monotonic.
      return null;
    }

    private Bucket getErrorBucket(CanonicalCode code) {
      return errorBuckets[code.value() - 1];
    }

    private void considerForSampling(SpanImpl span) {
      Status status = span.getStatus();
      // Null status means running Span, this should not happen in production, but the library
      // should not crash because of this.
      if (status != null) {
        Bucket bucket =
            status.isOk()
                ? getLatencyBucket(span.getLatencyNs())
                : getErrorBucket(status.getCanonicalCode());
        // If unable to find the bucket, ignore this Span.
        if (bucket != null) {
          bucket.considerForSampling(span);
        }
      }
    }

    private Map<LatencyBucketBoundaries, Integer> getNumbersOfLatencySampledSpans() {
      Map<LatencyBucketBoundaries, Integer> latencyBucketSummaries =
          new EnumMap<LatencyBucketBoundaries, Integer>(LatencyBucketBoundaries.class);
      for (int i = 0; i < NUM_LATENCY_BUCKETS; i++) {
        latencyBucketSummaries.put(
            LatencyBucketBoundaries.values()[i], latencyBuckets[i].getNumSamples());
      }
      return latencyBucketSummaries;
    }

    private Map<CanonicalCode, Integer> getNumbersOfErrorSampledSpans() {
      Map<CanonicalCode, Integer> errorBucketSummaries =
          new EnumMap<CanonicalCode, Integer>(CanonicalCode.class);
      for (int i = 0; i < NUM_ERROR_BUCKETS; i++) {
        errorBucketSummaries.put(CanonicalCode.values()[i + 1], errorBuckets[i].getNumSamples());
      }
      return errorBucketSummaries;
    }

    private List<SpanImpl> getErrorSamples(CanonicalCode code, int maxSpansToReturn) {
      ArrayList<SpanImpl> output = new ArrayList<SpanImpl>(maxSpansToReturn);
      if (code != null) {
        getErrorBucket(code).getSamples(maxSpansToReturn, output);
      } else {
        for (int i = 0; i < NUM_ERROR_BUCKETS; i++) {
          errorBuckets[i].getSamples(maxSpansToReturn, output);
        }
      }
      return output;
    }

    private List<SpanImpl> getLatencySamples(
        long latencyLowerNs, long latencyUpperNs, int maxSpansToReturn) {
      ArrayList<SpanImpl> output = new ArrayList<SpanImpl>(maxSpansToReturn);
      for (int i = 0; i < NUM_LATENCY_BUCKETS; i++) {
        LatencyBucketBoundaries boundaries = LatencyBucketBoundaries.values()[i];
        if (latencyUpperNs >= boundaries.getLatencyLowerNs()
            && latencyLowerNs < boundaries.getLatencyUpperNs()) {
          latencyBuckets[i].getSamplesFilteredByLatency(
              latencyLowerNs, latencyUpperNs, maxSpansToReturn, output);
        }
      }
      return output;
    }
  }

  /** Constructs a new {@code SampledSpanStoreImpl}. */
  SampledSpanStoreImpl() {
    samples = new HashMap<String, PerSpanNameSamples>();
  }

  @Override
  public Summary getSummary() {
    Map<String, PerSpanNameSummary> ret = new HashMap<String, PerSpanNameSummary>();
    synchronized (samples) {
      for (Map.Entry<String, PerSpanNameSamples> it : samples.entrySet()) {
        ret.put(
            it.getKey(),
            PerSpanNameSummary.create(
                it.getValue().getNumbersOfLatencySampledSpans(),
                it.getValue().getNumbersOfErrorSampledSpans()));
      }
    }
    return Summary.create(ret);
  }

  /**
   * Considers to save the given spans to the stored samples. This must be called at the end of each
   * Span with the option RECORD_EVENTS.
   *
   * @param span the span to be consider for storing into the store buckets.
   */
  public void considerForSampling(SpanImpl span) {
    synchronized (samples) {
      String spanName = span.getName();
      if (span.getRegisterNameForSampledSpanStore() && !samples.containsKey(spanName)) {
        samples.put(spanName, new PerSpanNameSamples());
      }
      PerSpanNameSamples perSpanNameSamples = samples.get(spanName);
      if (perSpanNameSamples != null) {
        perSpanNameSamples.considerForSampling(span);
      }
    }
  }

  @Override
  public void registerSpanNamesForCollection(Collection<String> spanNames) {
    synchronized (samples) {
      for (String spanName : spanNames) {
        if (!samples.containsKey(spanName)) {
          samples.put(spanName, new PerSpanNameSamples());
        }
      }
    }
  }

  @Override
  public void unregisterSpanNamesForCollection(Collection<String> spanNames) {
    synchronized (samples) {
      samples.keySet().removeAll(spanNames);
    }
  }

  @Override
  public Set<String> getRegisteredSpanNamesForCollection() {
    synchronized (samples) {
      return Collections.unmodifiableSet(new HashSet<String>(samples.keySet()));
    }
  }

  @Override
  public Collection<SpanData> getErrorSampledSpans(ErrorFilter filter) {
    int numSpansToReturn =
        filter.getMaxSpansToReturn() == 0
            ? MAX_PER_SPAN_NAME_SAMPLES
            : filter.getMaxSpansToReturn();
    List<SpanImpl> spans = Collections.emptyList();
    // Try to not keep the lock to much, do the SpanImpl -> SpanData conversion outside the lock.
    synchronized (samples) {
      PerSpanNameSamples perSpanNameSamples = samples.get(filter.getSpanName());
      if (perSpanNameSamples != null) {
        spans = perSpanNameSamples.getErrorSamples(filter.getCanonicalCode(), numSpansToReturn);
      }
    }
    List<SpanData> ret = new ArrayList<SpanData>(spans.size());
    for (SpanImpl span : spans) {
      ret.add(span.toSpanData());
    }
    return Collections.unmodifiableList(ret);
  }

  @Override
  public Collection<SpanData> getLatencySampledSpans(LatencyFilter filter) {
    int numSpansToReturn =
        filter.getMaxSpansToReturn() == 0
            ? MAX_PER_SPAN_NAME_SAMPLES
            : filter.getMaxSpansToReturn();
    List<SpanImpl> spans = Collections.emptyList();
    // Try to not keep the lock to much, do the SpanImpl -> SpanData conversion outside the lock.
    synchronized (samples) {
      PerSpanNameSamples perSpanNameSamples = samples.get(filter.getSpanName());
      if (perSpanNameSamples != null) {
        spans =
            perSpanNameSamples.getLatencySamples(
                filter.getLatencyLowerNs(), filter.getLatencyUpperNs(), numSpansToReturn);
      }
    }
    List<SpanData> ret = new ArrayList<SpanData>(spans.size());
    for (SpanImpl span : spans) {
      ret.add(span.toSpanData());
    }
    return Collections.unmodifiableList(ret);
  }
}
