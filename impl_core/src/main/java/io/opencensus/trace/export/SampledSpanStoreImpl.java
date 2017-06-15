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

package io.opencensus.trace.export;

import com.google.common.collect.EvictingQueue;
import io.opencensus.trace.SpanImpl;
import io.opencensus.trace.base.Status;
import io.opencensus.trace.base.Status.CanonicalCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Implementation of the {@link SampledSpanStore}. */
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
  private final Map<String, PerSpanNameSamples> samples;

  private static final class Bucket {

    private final EvictingQueue<SpanImpl> queue;
    private long nextSampleTime;

    private Bucket(int numSamples) {
      queue = EvictingQueue.create(numSamples);
    }

    private void considerForSampling(SpanImpl span) {
      long spanEndTime = span.getEndNanoTime();
      if (spanEndTime > nextSampleTime) {
        queue.add(span);
        nextSampleTime = spanEndTime + TIME_BETWEEN_SAMPLES;
      }
    }

    private void getSamples(int maxSpansToReturn, List<SpanImpl> output) {
      for (SpanImpl span : queue) {
        if (output.size() >= maxSpansToReturn) {
          break;
        }
        output.add(span);
      }
    }

    private void getSampledFilteredByLatency(
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
      return queue.size();
    }
  }

  /**
   * Keeps samples for a given span name. Samples for all the latency bucketes and one special
   * bucket for errors.
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
      return latencyBuckets[NUM_LATENCY_BUCKETS];
    }

    private Bucket getErrorBucket(CanonicalCode code) {
      return errorBuckets[code.value() - 1];
    }

    private void considerForSampling(SpanImpl span) {
      Status status = span.getStatus();
      Bucket bucket =
          status.isOk()
              ? getLatencyBucket(span.getLatencyNs())
              : getErrorBucket(status.getCanonicalCode());
      bucket.considerForSampling(span);
    }

    private Map<LatencyBucketBoundaries, Integer> getNumberOfLatencySampledSpans() {
      Map<LatencyBucketBoundaries, Integer> latencyBucketSummaries =
          new HashMap<LatencyBucketBoundaries, Integer>(NUM_LATENCY_BUCKETS);
      for (int i = 0; i < NUM_LATENCY_BUCKETS; i++) {
        latencyBucketSummaries.put(
            LatencyBucketBoundaries.values()[i], latencyBuckets[i].getNumSamples());
      }
      return latencyBucketSummaries;
    }

    private Map<CanonicalCode, Integer> getNumberOfErrorSampledSpans() {
      Map<CanonicalCode, Integer> errorBucketSummaries =
          new HashMap<CanonicalCode, Integer>(NUM_ERROR_BUCKETS);
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
          latencyBuckets[i].getSampledFilteredByLatency(
              latencyLowerNs, latencyUpperNs, maxSpansToReturn, output);
        }
      }
      return output;
    }
  }

  /** Constructs a new {@code SampledSpanStoreImpl}. */
  public SampledSpanStoreImpl() {
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
                it.getValue().getNumberOfLatencySampledSpans(),
                it.getValue().getNumberOfErrorSampledSpans()));
      }
    }
    return Summary.create(ret);
  }

  /**
   * Considers to save the given spans to the stored samples. This must be called at the end of the
   * each Span with the option RECORD_EVENTS.
   *
   * @param span the span to be consider for storing into the store buckets.
   */
  public void considerForSampling(SpanImpl span) {
    synchronized (samples) {
      PerSpanNameSamples perSpanNameSamples = samples.get(span.getName());
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
      for (String spanName : spanNames) {
        samples.remove(spanName);
      }
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
