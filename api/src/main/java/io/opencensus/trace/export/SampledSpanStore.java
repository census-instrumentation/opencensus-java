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

package io.opencensus.trace.export;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Status.CanonicalCode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * This class allows users to access in-process information such as latency based sampled spans and
 * error based sampled spans.
 *
 * <p>For all completed spans with the option {@link Span.Options#RECORD_EVENTS} the library can
 * store samples based on latency for succeeded operations or based on error code for failed
 * operations. To activate this, users MUST manually configure all the span names for which samples
 * will be collected (see {@link #registerSpanNamesForCollection(Collection)}).
 */
@ThreadSafe
public abstract class SampledSpanStore {

  protected SampledSpanStore() {}

  /**
   * Returns the summary of all available data, such as number of sampled spans in the latency based
   * samples or error based samples.
   *
   * <p>Data available only for span names registered using {@link
   * #registerSpanNamesForCollection(Collection)}.
   *
   * @return the summary of all available data.
   */
  public abstract Summary getSummary();

  /**
   * Returns a list of succeeded spans (spans with {@link Status} equal to {@link Status#OK}) that
   * match the {@code filter}.
   *
   * <p>Latency based sampled spans are available only for span names registered using {@link
   * #registerSpanNamesForCollection(Collection)}.
   *
   * @param filter used to filter the returned sampled spans.
   * @return a list of succeeded spans that match the {@code filter}.
   */
  public abstract Collection<SpanData> getLatencySampledSpans(LatencyFilter filter);

  /**
   * Returns a list of failed spans (spans with {@link Status} other than {@link Status#OK}) that
   * match the {@code filter}.
   *
   * <p>Error based sampled spans are available only for span names registered using {@link
   * #registerSpanNamesForCollection(Collection)}.
   *
   * @param filter used to filter the returned sampled spans.
   * @return a list of failed spans that match the {@code filter}.
   */
  public abstract Collection<SpanData> getErrorSampledSpans(ErrorFilter filter);

  /**
   * Appends a list of span names for which the library will collect latency based sampled spans and
   * error based sampled spans.
   *
   * <p>If called multiple times the library keeps the list of unique span names from all the calls.
   *
   * @param spanNames list of span names for which the library will collect samples.
   */
  public abstract void registerSpanNamesForCollection(Collection<String> spanNames);

  /**
   * Removes a list of span names for which the library will collect latency based sampled spans and
   * error based sampled spans.
   *
   * <p>The library keeps the list of unique registered span names for which samples will be called.
   * This method allows users to remove span names from that list.
   *
   * @param spanNames list of span names for which the library will no longer collect samples.
   */
  public abstract void unregisterSpanNamesForCollection(Collection<String> spanNames);

  /**
   * Returns the list of unique span names registered to the library. For this set of span names the
   * library will collect latency based sampled spans and error based sampled spans.
   *
   * @return the list of unique span names registered to the library.
   */
  public abstract Set<String> listRegisteredSpanNamesForCollection();

  /** The summary of all available data. */
  @AutoValue
  @Immutable
  public abstract static class Summary {

    Summary() {}

    /**
     * Returns a new instance of {@code Summary}.
     *
     * @param perSpanNameSummary a map with summary for each span name.
     * @return a new instance of {@code Summary}.
     * @throws NullPointerException if {@code perSpanNameSummary} is {@code null}.
     */
    public static Summary create(Map<String, PerSpanNameSummary> perSpanNameSummary) {
      return new AutoValue_SampledSpanStore_Summary(
          Collections.unmodifiableMap(
              new HashMap<String, PerSpanNameSummary>(
                  checkNotNull(perSpanNameSummary, "perSpanNameSummary"))));
    }

    /**
     * Returns a map with summary of available data for each span name.
     *
     * @return a map with all the span names and the summary.
     */
    public abstract Map<String, PerSpanNameSummary> getPerSpanNameSummary();
  }

  /** Summary of all available data for a span name. */
  @AutoValue
  @Immutable
  public abstract static class PerSpanNameSummary {

    PerSpanNameSummary() {}

    /**
     * Returns a new instance of {@code PerSpanNameSummary}.
     *
     * @param numbersOfLatencySampledSpans the summary for the latency buckets.
     * @param numbersOfErrorSampledSpans the summary for the error buckets.
     * @return a new instance of {@code PerSpanNameSummary}.
     * @throws NullPointerException if {@code numbersOfLatencySampledSpans} or {@code
     *     numbersOfErrorSampledSpans} are {@code null}.
     */
    public static PerSpanNameSummary create(
        Map<LatencyBucketBoundaries, Integer> numbersOfLatencySampledSpans,
        Map<CanonicalCode, Integer> numbersOfErrorSampledSpans) {
      return new AutoValue_SampledSpanStore_PerSpanNameSummary(
          Collections.unmodifiableMap(
              new HashMap<LatencyBucketBoundaries, Integer>(
                  checkNotNull(numbersOfLatencySampledSpans, "numbersOfLatencySampledSpans"))),
          Collections.unmodifiableMap(
              new HashMap<CanonicalCode, Integer>(
                  checkNotNull(numbersOfErrorSampledSpans, "numbersOfErrorSampledSpans"))));
    }

    /**
     * Returns the number of sampled spans in all the latency buckets.
     *
     * <p>Data available only for span names registered using {@link
     * #registerSpanNamesForCollection(Collection)}.
     *
     * @return the number of sampled spans in all the latency buckets.
     */
    public abstract Map<LatencyBucketBoundaries, Integer> getNumbersOfLatencySampledSpans();

    /**
     * Returns the number of sampled spans in all the error buckets.
     *
     * <p>Data available only for span names registered using {@link
     * #registerSpanNamesForCollection(Collection)}.
     *
     * @return the number of sampled spans in all the error buckets.
     */
    public abstract Map<CanonicalCode, Integer> getNumbersOfErrorSampledSpans();
  }

  /**
   * The latency buckets boundaries. Samples based on latency for successful spans (the status of
   * the span has a canonical code equal to {@link CanonicalCode#OK}) are collected in one of these
   * latency buckets.
   */
  public enum LatencyBucketBoundaries {
    // Stores finished successful requests of duration within the interval [0, 10us)
    ZERO_MICROSx10(0, TimeUnit.MICROSECONDS.toNanos(10)),
    // Stores finished successful requests of duration within the interval [10us, 100us)
    MICROSx10_MICROSx100(TimeUnit.MICROSECONDS.toNanos(10), TimeUnit.MICROSECONDS.toNanos(100)),
    // Stores finished successful requests of duration within the interval [100us, 1ms)
    MICROSx100_MILLIx1(TimeUnit.MICROSECONDS.toNanos(100), TimeUnit.MILLISECONDS.toNanos(1)),
    // Stores finished successful requests of duration within the interval [1ms, 10ms)
    MILLIx1_MILLIx10(TimeUnit.MILLISECONDS.toNanos(1), TimeUnit.MILLISECONDS.toNanos(10)),
    // Stores finished successful requests of duration within the interval [10ms, 100ms)
    MILLIx10_MILLIx100(TimeUnit.MILLISECONDS.toNanos(10), TimeUnit.MILLISECONDS.toNanos(100)),
    // Stores finished successful requests of duration within the interval [100ms, 1sec)
    MILLIx100_SECONDx1(TimeUnit.MILLISECONDS.toNanos(100), TimeUnit.SECONDS.toNanos(1)),
    // Stores finished successful requests of duration within the interval [1sec, 10sec)
    SECONDx1_SECONDx10(TimeUnit.SECONDS.toNanos(1), TimeUnit.SECONDS.toNanos(10)),
    // Stores finished successful requests of duration within the interval [10sec, 100sec)
    SECONDx10_SECONDx100(TimeUnit.SECONDS.toNanos(10), TimeUnit.SECONDS.toNanos(100)),
    // Stores finished successful requests of duration >= 100sec
    SECONDx100_MAX(TimeUnit.SECONDS.toNanos(100), Long.MAX_VALUE);

    /**
     * Constructs a {@code LatencyBucketBoundaries} with the given boundaries and label.
     *
     * @param latencyLowerNs the latency lower bound of the bucket.
     * @param latencyUpperNs the latency upper bound of the bucket.
     */
    LatencyBucketBoundaries(long latencyLowerNs, long latencyUpperNs) {
      this.latencyLowerNs = latencyLowerNs;
      this.latencyUpperNs = latencyUpperNs;
    }

    /**
     * Returns the latency lower bound of the bucket.
     *
     * @return the latency lower bound of the bucket.
     */
    public long getLatencyLowerNs() {
      return latencyLowerNs;
    }

    /**
     * Returns the latency upper bound of the bucket.
     *
     * @return the latency upper bound of the bucket.
     */
    public long getLatencyUpperNs() {
      return latencyUpperNs;
    }

    private final long latencyLowerNs;
    private final long latencyUpperNs;
  }

  /**
   * Filter for latency based sampled spans. Used to filter results returned by the {@link
   * #getLatencySampledSpans(LatencyFilter)} request.
   */
  @AutoValue
  @Immutable
  public abstract static class LatencyFilter {

    LatencyFilter() {}

    /**
     * Returns a new instance of {@code LatencyFilter}.
     *
     * <p>Filters all the spans based on {@code spanName} and latency in the interval
     * [latencyLowerNs, latencyUpperNs) and returns a maximum of {@code maxSpansToReturn}.
     *
     * @param spanName the name of the span.
     * @param latencyLowerNs the latency lower bound.
     * @param latencyUpperNs the latency upper bound.
     * @param maxSpansToReturn the maximum number of results to be returned. {@code 0} means all.
     * @return a new instance of {@code LatencyFilter}.
     * @throws NullPointerException if {@code spanName} is {@code null}.
     * @throws IllegalArgumentException if {@code maxSpansToReturn} or {@code latencyLowerNs} or
     *     {@code latencyUpperNs} are negative.
     */
    public static LatencyFilter create(
        String spanName, long latencyLowerNs, long latencyUpperNs, int maxSpansToReturn) {
      checkArgument(maxSpansToReturn >= 0, "Negative maxSpansToReturn.");
      checkArgument(latencyLowerNs >= 0, "Negative latencyLowerNs");
      checkArgument(latencyUpperNs >= 0, "Negative latencyUpperNs");
      return new AutoValue_SampledSpanStore_LatencyFilter(
          spanName, latencyLowerNs, latencyUpperNs, maxSpansToReturn);
    }

    /**
     * Returns the span name used by this filter.
     *
     * @return the span name used by this filter.
     */
    public abstract String getSpanName();

    /**
     * Returns the latency lower bound of this bucket (inclusive).
     *
     * @return the latency lower bound of this bucket.
     */
    public abstract long getLatencyLowerNs();

    /**
     * Returns the latency upper bound of this bucket (exclusive).
     *
     * @return the latency upper bound of this bucket.
     */
    public abstract long getLatencyUpperNs();

    /**
     * Returns the maximum number of spans to be returned. {@code 0} means all.
     *
     * @return the maximum number of spans to be returned.
     */
    public abstract int getMaxSpansToReturn();
  }

  /**
   * Filter for error based sampled spans. Used to filter results returned by the {@link
   * #getErrorSampledSpans(ErrorFilter)} request.
   */
  @AutoValue
  @Immutable
  public abstract static class ErrorFilter {

    ErrorFilter() {}

    /**
     * Returns a new instance of {@code ErrorFilter}.
     *
     * <p>Filters all the spans based on {@code spanName} and {@code canonicalCode} and returns a
     * maximum of {@code maxSpansToReturn}.
     *
     * @param spanName the name of the span.
     * @param canonicalCode the error code of the span. {@code null} can be used to query all error
     *     codes.
     * @param maxSpansToReturn the maximum number of results to be returned. {@code 0} means all.
     * @return a new instance of {@code ErrorFilter}.
     * @throws NullPointerException if {@code spanName} is {@code null}.
     * @throws IllegalArgumentException if {@code canonicalCode} is {@link CanonicalCode#OK} or
     *     {@code maxSpansToReturn} is negative.
     */
    public static ErrorFilter create(
        String spanName, @Nullable CanonicalCode canonicalCode, int maxSpansToReturn) {
      if (canonicalCode != null) {
        checkArgument(canonicalCode != CanonicalCode.OK, "Invalid canonical code.");
      }
      checkArgument(maxSpansToReturn >= 0, "Negative maxSpansToReturn.");
      return new AutoValue_SampledSpanStore_ErrorFilter(spanName, canonicalCode, maxSpansToReturn);
    }

    /**
     * Returns the span name used by this filter.
     *
     * @return the span name used by this filter.
     */
    public abstract String getSpanName();

    /**
     * Returns the canonical code used by this filter. Always different than {@link
     * CanonicalCode#OK}. If {@code null} then all errors match.
     *
     * @return the canonical code used by this filter.
     */
    @Nullable
    public abstract CanonicalCode getCanonicalCode();

    /**
     * Returns the maximum number of spans to be returned. Used to enforce the number of returned
     * {@code SpanData}. {@code 0} means all.
     *
     * @return the maximum number of spans to be returned.
     */
    public abstract int getMaxSpansToReturn();
  }
}
