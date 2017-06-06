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

package io.opencensus.trace;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import io.opencensus.trace.Status.CanonicalCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The main exporting API for the trace library.
 *
 * <p>Implementation MUST ensure that all functions are thread safe.
 */
@ThreadSafe
public abstract class TraceExporter {

  private static final NoopTraceExporter noopTraceExporter = new NoopTraceExporter();

  /**
   * Returns the no-op implementation of the {@code TraceExporter}.
   *
   * @return the no-op implementation of the {@code TraceExporter}.
   */
  static TraceExporter getNoopTraceExporter() {
    return noopTraceExporter;
  }

  /**
   * Registers a new service handler that is used by the library to export {@code SpanData} for
   * sampled spans (see {@link TraceOptions#isSampled()}).
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * public static void main(String[] args) {
   *   Tracing.getTraceExporter().registerServiceHandler(
   *       "com.google.stackdriver.tracing", new StackdriverTracingServiceHandler());
   *   // ...
   * }
   * }</pre>
   *
   * @param name the name of the service handler. Must be unique for each service.
   * @param serviceHandler the service handler that is called for each ended sampled span.
   */
  public abstract void registerServiceHandler(String name, ServiceHandler serviceHandler);

  /**
   * Unregisters the service handler with the provided name.
   *
   * @param name the name of the service handler that will be unregistered.
   */
  public abstract void unregisterServiceHandler(String name);

  /**
   * Returns the {@code InProcessDebuggingHandler} that can be used to get useful debugging
   * information such as (active spans, latency based sampled spans, error based sampled spans).
   *
   * @return the {@code InProcessDebuggingHandler} or {@code null} if in-process debugging is not
   *     supported.
   */
  @Nullable
  public abstract InProcessDebuggingHandler getInProcessDebuggingHandler();

  /**
   * This class allows users to access in-process debugging information such as (getting access to
   * all active spans, support latency based sampled spans and error based sampled spans).
   *
   * <p>The active spans tracking is available for all the spans with the option {@link
   * Span.Options#RECORD_EVENTS}. This functionality allows users to debug stuck operations or long
   * living operations.
   *
   * <p>For all completed spans with the option {@link Span.Options#RECORD_EVENTS} the library can
   * store samples based on latency for succeeded operations or based on error code for failed
   * operations. To activate this, users MUST manually configure all the span names for which
   * samples will be collected (see {@link #registerSpanNamesForCollection(Collection)}).
   */
  public abstract static class InProcessDebuggingHandler {

    InProcessDebuggingHandler() {}

    /**
     * Returns the summary of all available in-process debugging data such as number of active
     * spans, number of sampled spans in the latency based samples or error based samples.
     *
     * <p>Latency based sampled summary buckets and error based sampled summary buckets are
     * available only for span names registered using {@link
     * #registerSpanNamesForCollection(Collection)}.
     *
     * @return the summary of all available in-process debugging data.
     */
    public abstract Summary getSummary();

    /**
     * Returns a list of active spans that match the {@code filter}.
     *
     * <p>Active spans are available for all the span names.
     *
     * @param filter used to filter the returned spans.
     * @return a list of active spans that match the {@code filter}.
     */
    public abstract Collection<SpanData> getActiveSpans(ActiveSpansFilter filter);

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
    public abstract Collection<SpanData> getLatencyBasedSampledSpans(
        LatencyBasedSampledSpansFilter filter);

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
    public abstract Collection<SpanData> getErrorBasedSampledSpans(
        ErrorBasedSampledSpansFilter filter);

    /**
     * Appends a list of span names for which the library will collect latency based sampled spans
     * and error based sampled spans.
     *
     * <p>If called multiple times the library keeps the list of unique span names from all the
     * calls.
     *
     * @param spanNames list of span names for which the library will collect samples.
     */
    public abstract void registerSpanNamesForCollection(Collection<String> spanNames);

    /**
     * Removes a list of span names for which the library will collect latency based sampled spans
     * and error based sampled spans.
     *
     * <p>The library keeps the list of unique registered span names for which samples will be
     * called. This method allows users to remove span names from that list.
     *
     * @param spanNames list of span names for which the library will no longer collect samples.
     */
    public abstract void unregisterSpanNamesForCollection(Collection<String> spanNames);

    /** The summary of all in-process debugging information. */
    @AutoValue
    @Immutable
    public abstract static class Summary {

      Summary() {}

      /**
       * Returns a new instance of {@code Summary}.
       *
       * @param perSpanNameSummary a map with summary for each different span name.
       * @return a new instance of {@code Summary}.
       * @throws NullPointerException if {@code perSpanNameSummary} is {@code null}.
       */
      public static Summary create(Map<String, PerSpanNameSummary> perSpanNameSummary) {
        return new AutoValue_TraceExporter_InProcessDebuggingHandler_Summary(
            Collections.unmodifiableMap(
                new HashMap<String, PerSpanNameSummary>(
                    checkNotNull(perSpanNameSummary, "perSpanNameSummary"))));
      }

      /**
       * Returns a map with summary of available data for each different span name.
       *
       * @return a map with all the span names and the summary.
       */
      public abstract Map<String, PerSpanNameSummary> getPerSpanNameSummary();

      /** Summary of all available data for a span name. */
      @AutoValue
      @Immutable
      public abstract static class PerSpanNameSummary {

        PerSpanNameSummary() {}

        /**
         * Returns a new instance of {@code PerSpanNameSummary}.
         *
         * @param numActiveSpans the number of sampled spans.
         * @param latencyBucketSummaries the summary for the latency buckets.
         * @param errorBucketSummaries the summary for the error buckets.
         * @return a new instance of {@code PerSpanNameSummary}.
         * @throws NullPointerException if {@code latencyBucketSummaries} or {@code
         *     errorBucketSummaries} are {@code null}.
         * @throws IllegalArgumentException if {@code numActiveSpans} is negative.
         */
        public static PerSpanNameSummary create(
            int numActiveSpans,
            List<LatencyBucketSummary> latencyBucketSummaries,
            List<ErrorBucketSummary> errorBucketSummaries) {
          checkArgument(numActiveSpans >= 0, "Negative numActiveSpans.");
          return new AutoValue_TraceExporter_InProcessDebuggingHandler_Summary_PerSpanNameSummary(
              numActiveSpans,
              Collections.unmodifiableList(
                  new ArrayList<LatencyBucketSummary>(
                      checkNotNull(latencyBucketSummaries, "latencyBucketSummaries"))),
              Collections.unmodifiableList(
                  new ArrayList<ErrorBucketSummary>(
                      checkNotNull(errorBucketSummaries, "errorBucketSummaries"))));
        }

        /**
         * Returns the number of active spans.
         *
         * @return the number of active spans.
         */
        public abstract int getNumActiveSpans();

        /**
         * Returns the list of all latency based sampled buckets summary.
         *
         * <p>The list is sorted based on the lower latency boundary, and the upper bound of one
         * match the lower bound of the next. Every bucket contains samples with latency within the
         * interval [lowerBoundary, upperBoundary).
         *
         * @return the list of all latency based sampled buckets summary.
         */
        public abstract List<LatencyBucketSummary> getLatencyBucketSummaries();

        /**
         * Returns the list of all error based sampled buckets summary.
         *
         * <p>The list is sorted based on the {@link CanonicalCode#value()} and contains an entry
         * for each of the values other than {@link CanonicalCode#OK}.
         *
         * @return the list of all error based sampled buckets summary.
         */
        public abstract List<ErrorBucketSummary> getErrorBucketSummaries();

        /**
         * Summary of a latency based sampled spans bucket. Contains {@code Span} samples with
         * latency between [latencyLowerNs, latencyUpperNs).
         */
        @AutoValue
        @Immutable
        public abstract static class LatencyBucketSummary {

          LatencyBucketSummary() {}

          /**
           * Returns a new instance of {@code LatencyBucketSummary}. The latency of the samples is
           * in the interval [latencyLowerNs, latencyUpperNs).
           *
           * @param numSamples the number of sampled spans.
           * @param latencyLowerNs the latency lower bound.
           * @param latencyUpperNs the latency upper bound.
           * @return a new instance of {@code LatencyBucketSummary}.
           * @throws IllegalArgumentException if {@code numSamples} or {@code latencyLowerNs} or
           *     {@code latencyUpperNs} are negative.
           */
          public static LatencyBucketSummary create(
              int numSamples, long latencyLowerNs, long latencyUpperNs) {
            checkArgument(numSamples >= 0, "Negative numSamples.");
            checkArgument(latencyLowerNs >= 0, "Negative latencyLowerNs");
            checkArgument(latencyUpperNs >= 0, "Negative latencyUpperNs");
            //CHECKSTYLE:OFF: Long class name.
            return new AutoValue_TraceExporter_InProcessDebuggingHandler_Summary_PerSpanNameSummary_LatencyBucketSummary(
                numSamples, latencyLowerNs, latencyUpperNs);
            //CHECKSTYLE:ON: Long class name.
          }

          /**
           * Returns the number of sampled spans in this bucket.
           *
           * @return the number of sampled spans in this bucket.
           */
          public abstract int getNumSamples();

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
        }

        /** Summary of an error based sampled spans bucket. */
        @AutoValue
        @Immutable
        public abstract static class ErrorBucketSummary {

          ErrorBucketSummary() {}

          /**
           * Returns a new instance of {@code ErrorBucketSummary}.
           *
           * @param numSamples the number of sampled spans.
           * @param canonicalCode the error code of the bucket.
           * @return a new instance of {@code ErrorBucketSummary}.
           * @throws NullPointerException if {@code canonicalCode} is {@code null}.
           * @throws IllegalArgumentException if {@code canonicalCode} is {@link CanonicalCode#OK}
           *     or {@code numSamples} is negative.
           */
          public static ErrorBucketSummary create(int numSamples, CanonicalCode canonicalCode) {
            checkArgument(numSamples >= 0, "Negative numSamples.");
            checkArgument(canonicalCode != CanonicalCode.OK, "Invalid canonical code.");
            //CHECKSTYLE:OFF: Long class name.
            return new AutoValue_TraceExporter_InProcessDebuggingHandler_Summary_PerSpanNameSummary_ErrorBucketSummary(
                numSamples, canonicalCode);
            //CHECKSTYLE:ON: Long class name.
          }

          /**
           * Returns the number of sampled spans in this bucket.
           *
           * @return the number of sampled spans in this bucket.
           */
          public abstract int getNumSamples();

          /**
           * Returns the {@code CanonicalCode} for this bucket. Always different than {@link
           * CanonicalCode#OK}.
           *
           * @return the {@code CanonicalCode} for this bucket.
           */
          public abstract CanonicalCode getCanonicalCode();
        }
      }
    }

    /**
     * Filter for active spans. Used to filter results returned by the {@link
     * #getActiveSpans(ActiveSpansFilter)} request.
     */
    @AutoValue
    @Immutable
    public abstract static class ActiveSpansFilter {

      ActiveSpansFilter() {}

      /**
       * Returns a new instance of {@code ActiveSpansFilter}.
       *
       * <p>Filters all the spans based on {@code spanName} and returns a maximum of {@code
       * maxSpansToReturn}.
       *
       * @param spanName the name of the span.
       * @param maxSpansToReturn the maximum number of results to be returned. {@code 0} means all.
       * @return a new instance of {@code ActiveSpansFilter}.
       * @throws NullPointerException if {@code spanName} is {@code null}.
       * @throws IllegalArgumentException if {@code maxSpansToReturn} is negative.
       */
      public static ActiveSpansFilter create(String spanName, int maxSpansToReturn) {
        checkArgument(maxSpansToReturn >= 0, "Negative maxSpansToReturn.");
        return new AutoValue_TraceExporter_InProcessDebuggingHandler_ActiveSpansFilter(
            spanName, maxSpansToReturn);
      }

      /**
       * Returns the span name.
       *
       * @return the span name.
       */
      public abstract String getSpanName();

      /**
       * Returns the maximum number of spans to be returned. {@code 0} means all.
       *
       * @return the maximum number of spans to be returned.
       */
      public abstract int getMaxSpansToReturn();
    }

    /**
     * Filter for latency based sampled spans. Used to filter results returned by the {@link
     * #getLatencyBasedSampledSpans(LatencyBasedSampledSpansFilter)} request.
     */
    @AutoValue
    @Immutable
    public abstract static class LatencyBasedSampledSpansFilter {

      LatencyBasedSampledSpansFilter() {}

      /**
       * Returns a new instance of {@code LatencyBasedSampledSpansFilter}.
       *
       * <p>Filters all the spans based on {@code spanName} and latency in the interval
       * [latencyLowerNs, latencyUpperNs) and returns a maximum of {@code maxSpansToReturn}.
       *
       * @param spanName the name of the span.
       * @param latencyLowerNs the latency lower bound.
       * @param latencyUpperNs the latency upper bound.
       * @param maxSpansToReturn the maximum number of results to be returned. {@code 0} means all.
       * @return a new instance of {@code LatencyBasedSampledSpansFilter}.
       * @throws NullPointerException if {@code spanName} is {@code null}.
       * @throws IllegalArgumentException if {@code maxSpansToReturn} or {@code latencyLowerNs} or
       *     {@code latencyUpperNs} are negative.
       */
      public static LatencyBasedSampledSpansFilter create(
          String spanName, long latencyLowerNs, long latencyUpperNs, int maxSpansToReturn) {
        checkArgument(maxSpansToReturn >= 0, "Negative maxSpansToReturn.");
        checkArgument(latencyLowerNs >= 0, "Negative latencyLowerNs");
        checkArgument(latencyUpperNs >= 0, "Negative latencyUpperNs");
        return new AutoValue_TraceExporter_InProcessDebuggingHandler_LatencyBasedSampledSpansFilter(
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

    /** Filter for error based sampled spans. */
    @AutoValue
    @Immutable
    public abstract static class ErrorBasedSampledSpansFilter {

      ErrorBasedSampledSpansFilter() {}

      /**
       * Returns a new instance of {@code ErrorBasedSampledSpansFilter}.
       *
       * <p>Filters all the spans based on {@code spanName} and {@code canonicalCode} and returns a
       * maximum of {@code maxSpansToReturn}.
       *
       * @param spanName the name of the span.
       * @param canonicalCode the error code of the span.
       * @param maxSpansToReturn the maximum number of results to be returned. {@code 0} means all.
       * @return a new instance of {@code ErrorBasedSampledSpansFilter}.
       * @throws NullPointerException if {@code spanName} or {@code canonicalCode} are {@code null}.
       * @throws IllegalArgumentException if {@code canonicalCode} is {@link CanonicalCode#OK} or
       *     {@code maxSpansToReturn} is negative.
       */
      public static ErrorBasedSampledSpansFilter create(
          String spanName, CanonicalCode canonicalCode, int maxSpansToReturn) {
        checkArgument(canonicalCode != CanonicalCode.OK, "Invalid canonical code.");
        checkArgument(maxSpansToReturn >= 0, "Negative maxSpansToReturn.");
        return new AutoValue_TraceExporter_InProcessDebuggingHandler_ErrorBasedSampledSpansFilter(
            spanName, canonicalCode, maxSpansToReturn);
      }

      /**
       * Returns the span name used by this filter.
       *
       * @return the span name used by this filter.
       */
      public abstract String getSpanName();

      /**
       * Returns the canonical code used by this filter. Always different than {@link
       * CanonicalCode#OK}.
       *
       * @return the canonical code used by this filter.
       */
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

  /**
   * An abstract class that allows different tracing services to export recorded data for sampled
   * spans in their own format.
   *
   * <p>To export data this MUST be register to to the TraceExporter using {@link
   * #registerServiceHandler(String, ServiceHandler)}.
   */
  public abstract static class ServiceHandler {

    /**
     * Exports a list of sampled (see {@link TraceOptions#isSampled()}) {@link Span}s using the
     * immutable representation {@link SpanData}.
     *
     * <p>This may be called from a different thread than the one that called {@link Span#end()}.
     *
     * <p>Implementation SHOULD not block the calling thread. It should execute the export on a
     * different thread if possible.
     *
     * @param spanDataList a list of {@code SpanData} objects to be exported.
     */
    public abstract void export(Collection<SpanData> spanDataList);
  }

  /**
   * Implementation of the {@link ServiceHandler} which logs all the exported {@link SpanData}.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * public static void main(String[] args) {
   *   Tracing.getTraceExporter().registerServiceHandler(
   *       "io.opencensus.LoggingServiceHandler", LoggingServiceHandler.getInstance());
   *   // ...
   * }
   * }</pre>
   */
  @ThreadSafe
  public static final class LoggingServiceHandler extends ServiceHandler {

    private static final Logger logger = Logger.getLogger(LoggingServiceHandler.class.getName());
    private static final String SERVICE_NAME = "io.opencensus.trace.LoggingServiceHandler";
    private static final LoggingServiceHandler INSTANCE = new LoggingServiceHandler();

    private LoggingServiceHandler() {}

    /**
     * Registers the {@code LoggingServiceHandler} to the {@code TraceExporter}.
     *
     * @param traceExporter the instance of the {@code TraceExporter} where this service is
     *     registered.
     */
    public static void registerService(TraceExporter traceExporter) {
      traceExporter.registerServiceHandler(SERVICE_NAME, INSTANCE);
    }

    /**
     * Unregisters the {@code LoggingServiceHandler} from the {@code TraceExporter}.
     *
     * @param traceExporter the instance of the {@code TraceExporter} from where this service is
     *     unregistered.
     */
    public static void unregisterService(TraceExporter traceExporter) {
      traceExporter.unregisterServiceHandler(SERVICE_NAME);
    }

    @Override
    public void export(Collection<SpanData> spanDataList) {
      for (SpanData spanData : spanDataList) {
        logger.log(Level.INFO, spanData.toString());
      }
    }
  }

  private static final class NoopTraceExporter extends TraceExporter {

    @Override
    public void registerServiceHandler(String name, @Nullable ServiceHandler serviceHandler) {}

    @Override
    public void unregisterServiceHandler(String name) {}

    @Nullable
    @Override
    public InProcessDebuggingHandler getInProcessDebuggingHandler() {
      return null;
    }
  }
}
