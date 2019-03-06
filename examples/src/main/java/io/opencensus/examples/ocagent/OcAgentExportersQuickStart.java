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

package io.opencensus.examples.ocagent;

import io.opencensus.common.Duration;
import io.opencensus.common.Scope;
import io.opencensus.exporter.metrics.ocagent.OcAgentMetricsExporter;
import io.opencensus.exporter.metrics.ocagent.OcAgentMetricsExporterConfiguration;
import io.opencensus.exporter.trace.ocagent.OcAgentTraceExporter;
import io.opencensus.exporter.trace.ocagent.OcAgentTraceExporterConfiguration;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongGauge;
import io.opencensus.metrics.LongGauge.LongPoint;
import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.Metrics;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/** Sample application that shows how to export traces and metrics to OC-Agent. */
public class OcAgentExportersQuickStart {

  private static final Logger logger = Logger.getLogger(OcAgentExportersQuickStart.class.getName());
  private static final Random random = new Random();

  private static final Tracer tracer = Tracing.getTracer();
  private static final Tagger tagger = Tags.getTagger();
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();
  private static final ViewManager viewManager = Stats.getViewManager();
  private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();

  private static final String SERVICE_NAME = "ocagent-java-exporter-quickstart";
  private static final String DEFAULT_ENDPOINT = "localhost:55678";
  private static final Duration RETRY_INTERVAL = Duration.create(10, 0);
  private static final Duration EXPORT_INTERVAL = Duration.create(5, 0);

  // The latency in milliseconds
  private static final MeasureDouble M_LATENCY_MS =
      MeasureDouble.create("repl/latency", "The latency in milliseconds per REPL loop", "ms");

  // Counts the number of lines read.
  private static final MeasureLong M_LINES_IN =
      MeasureLong.create("repl/lines_in", "The number of lines read in", "1");

  // Counts the number of non EOF(end-of-file) errors.
  private static final MeasureLong M_ERRORS =
      MeasureLong.create("repl/errors", "The number of errors encountered", "1");

  // Counts/groups the lengths of lines read in.
  private static final MeasureLong M_LINE_LENGTHS =
      MeasureLong.create("repl/line_lengths", "The distribution of line lengths", "By");

  // The tag "method"
  private static final TagKey KEY_METHOD = TagKey.create("method");

  // Defining the distribution aggregations
  private static final Aggregation LATENCY_DISTRIBUTION =
      Distribution.create(
          BucketBoundaries.create(
              Arrays.asList(
                  // [>=0ms, >=25ms, >=50ms, >=75ms, >=100ms, >=200ms, >=400ms, >=600ms, >=800ms,
                  // >=1s, >=2s, >=4s, >=6s]
                  0.0,
                  25.0,
                  50.0,
                  75.0,
                  100.0,
                  200.0,
                  400.0,
                  600.0,
                  800.0,
                  1000.0,
                  2000.0,
                  4000.0,
                  6000.0)));

  private static final Aggregation LENGTH_DISTRIBUTION =
      Distribution.create(
          BucketBoundaries.create(
              Arrays.asList(
                  // [>=0B, >=5B, >=10B, >=20B, >=40B, >=60B, >=80B, >=100B, >=200B, >=400B,
                  // >=600B,
                  // >=800B, >=1000B]
                  0.0,
                  5.0,
                  10.0,
                  20.0,
                  40.0,
                  60.0,
                  80.0,
                  100.0,
                  200.0,
                  400.0,
                  600.0,
                  800.0,
                  1000.0)));

  // Define the count aggregation
  private static final Aggregation COUNT = Aggregation.Count.create();

  // Empty column
  private static final List<TagKey> NO_KEYS = Collections.emptyList();

  // Define the views
  private static final List<View> VIEWS =
      Arrays.asList(
          View.create(
              Name.create("ocjavametrics/latency"),
              "The distribution of latencies",
              M_LATENCY_MS,
              LATENCY_DISTRIBUTION,
              Collections.singletonList(KEY_METHOD)),
          View.create(
              Name.create("ocjavametrics/lines_in"),
              "The number of lines read in from standard input",
              M_LINES_IN,
              COUNT,
              NO_KEYS),
          View.create(
              Name.create("ocjavametrics/errors"),
              "The number of errors encountered",
              M_ERRORS,
              COUNT,
              Collections.singletonList(KEY_METHOD)),
          View.create(
              Name.create("ocjavametrics/line_lengths"),
              "The distribution of line lengths",
              M_LINE_LENGTHS,
              LENGTH_DISTRIBUTION,
              NO_KEYS));

  /** Main launcher of the example. */
  public static void main(String[] args) throws InterruptedException {
    configureAlwaysSample(); // Always sample for demo purpose. DO NOT use in production.
    registerAllViews();
    LongGauge gauge = registerGauge();

    String endPoint = getStringOrDefaultFromArgs(args, 0, DEFAULT_ENDPOINT);
    registerAgentExporters(endPoint);

    try (Scope scope = tracer.spanBuilder("root").startScopedSpan()) {
      int iteration = 1;
      while (true) {
        doWork(iteration, random.nextInt(10), gauge);
        iteration++;
        Thread.sleep(5000);
      }
    } catch (InterruptedException e) {
      logger.info("Thread interrupted, exiting in 5 seconds.");
      Thread.sleep(5000); // Wait 5s so that last batch will be exported.
    }
  }

  private static void configureAlwaysSample() {
    TraceConfig traceConfig = Tracing.getTraceConfig();
    TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
    traceConfig.updateActiveTraceParams(
        activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());
  }

  private static void registerAgentExporters(String endPoint) {
    OcAgentTraceExporter.createAndRegister(
        OcAgentTraceExporterConfiguration.builder()
            .setEndPoint(endPoint)
            .setServiceName(SERVICE_NAME)
            .setUseInsecure(true)
            .setEnableConfig(false)
            .build());

    OcAgentMetricsExporter.createAndRegister(
        OcAgentMetricsExporterConfiguration.builder()
            .setEndPoint(endPoint)
            .setServiceName(SERVICE_NAME)
            .setUseInsecure(true)
            .setRetryInterval(RETRY_INTERVAL)
            .setExportInterval(EXPORT_INTERVAL)
            .build());
  }

  private static void registerAllViews() {
    for (View view : VIEWS) {
      viewManager.registerView(view);
    }
  }

  private static LongGauge registerGauge() {
    return metricRegistry.addLongGauge(
        "pending_jobs",
        "Pending jobs of current iteration",
        "1",
        Collections.singletonList(LabelKey.create("Name", "desc")));
  }

  private static void doWork(int iteration, int jobs, LongGauge gauge) {
    String childSpanName = "iteration-" + iteration;
    LabelValue value = LabelValue.create(childSpanName);
    LongPoint point = gauge.getOrCreateTimeSeries(Collections.singletonList(value));
    try (Scope scope = tracer.spanBuilder(childSpanName).startScopedSpan()) {
      for (int i = 0; i < jobs; i++) {
        String grandChildSpanName = childSpanName + "-job-" + i;
        try (Scope childScope = tracer.spanBuilder(grandChildSpanName).startScopedSpan()) {
          point.set(jobs - i);
          String line = generateRandom(random.nextInt(128));
          processLine(line);
          recordStat(M_LINES_IN, 1L);
          recordStat(M_LINE_LENGTHS, (long) line.length());
        } catch (Exception e) {
          tracer.getCurrentSpan().setStatus(Status.INTERNAL.withDescription(e.toString()));
        }
      }
    }
  }

  private static String generateRandom(int size) {
    byte[] array = new byte[size];
    random.nextBytes(array);
    return new String(array, Charset.forName("UTF-8"));
  }

  private static String processLine(String line) {
    long startTimeNs = System.nanoTime();

    try {
      Thread.sleep(10L);
      return line.toUpperCase(Locale.US);
    } catch (Exception e) {
      recordTaggedStat(KEY_METHOD, "processLine", M_ERRORS, 1L);
      return "";
    } finally {
      long totalTimeNs = System.nanoTime() - startTimeNs;
      double timespentMs = totalTimeNs / 1e6;
      recordTaggedStat(KEY_METHOD, "processLine", M_LATENCY_MS, timespentMs);
    }
  }

  private static void recordStat(MeasureLong ml, Long n) {
    TagContext empty = tagger.emptyBuilder().build();
    statsRecorder.newMeasureMap().put(ml, n).record(empty);
  }

  private static void recordTaggedStat(TagKey key, String value, MeasureLong ml, long n) {
    TagContext context = tagger.emptyBuilder().put(key, TagValue.create(value)).build();
    statsRecorder.newMeasureMap().put(ml, n).record(context);
  }

  private static void recordTaggedStat(TagKey key, String value, MeasureDouble md, double d) {
    TagContext context = tagger.emptyBuilder().put(key, TagValue.create(value)).build();
    statsRecorder.newMeasureMap().put(md, d).record(context);
  }

  private static String getStringOrDefaultFromArgs(String[] args, int index, String defaultString) {
    String s = defaultString;
    if (index < args.length) {
      s = args[index];
    }
    return s;
  }
}
