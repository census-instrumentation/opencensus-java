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

package io.opencensus.examples.quickstart;

import io.opencensus.common.Scope;
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
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
import io.prometheus.client.exporter.HTTPServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Sample application that shows how to record stats and export to Prometheus. */
public final class Repl {

  // The latency in milliseconds
  private static final MeasureDouble M_LATENCY_MS =
      MeasureDouble.create("repl/latency", "The latency in milliseconds per REPL loop", "ms");

  // Counts the number of lines read in from standard input.
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

  private static final Tagger tagger = Tags.getTagger();
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

  /** Main launcher for the Repl example. */
  public static void main(String... args) {
    // Step 1. Enable OpenCensus Metrics.
    try {
      setupOpenCensusAndPrometheusExporter();
    } catch (IOException e) {
      System.err.println("Failed to create and register OpenCensus Prometheus Stats exporter " + e);
      return;
    }

    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

    while (true) {
      try {
        readEvaluateProcessLine(stdin);
      } catch (IOException e) {
        System.err.println("EOF bye " + e);
        return;
      } catch (Exception e) {
        recordTaggedStat(KEY_METHOD, "repl", M_ERRORS, new Long(1));
        return;
      }
    }
  }

  private static void recordStat(MeasureLong ml, Long n) {
    TagContext tctx = tagger.emptyBuilder().build();
    try (Scope ss = tagger.withTagContext(tctx)) {
      statsRecorder.newMeasureMap().put(ml, n).record();
    }
  }

  private static void recordTaggedStat(TagKey key, String value, MeasureLong ml, Long n) {
    TagContext tctx = tagger.emptyBuilder().put(key, TagValue.create(value)).build();
    try (Scope ss = tagger.withTagContext(tctx)) {
      statsRecorder.newMeasureMap().put(ml, n).record();
    }
  }

  private static void recordTaggedStat(TagKey key, String value, MeasureDouble md, Double d) {
    TagContext tctx = tagger.emptyBuilder().put(key, TagValue.create(value)).build();
    try (Scope ss = tagger.withTagContext(tctx)) {
      statsRecorder.newMeasureMap().put(md, d).record();
    }
  }

  private static String processLine(String line) {
    long startTimeNs = System.nanoTime();

    try {
      return line.toUpperCase();
    } catch (Exception e) {
      recordTaggedStat(KEY_METHOD, "processLine", M_ERRORS, new Long(1));
      return "";
    } finally {
      long totalTimeNs = System.nanoTime() - startTimeNs;
      double timespentMs = (new Double(totalTimeNs)) / 1e6;
      recordTaggedStat(KEY_METHOD, "processLine", M_LATENCY_MS, timespentMs);
    }
  }

  private static void readEvaluateProcessLine(BufferedReader in) throws IOException {
    System.out.print("> ");
    System.out.flush();

    String line = in.readLine();
    String processed = processLine(line);
    System.out.println("< " + processed + "\n");
    if (line != null && line.length() > 0) {
      recordStat(M_LINES_IN, new Long(1));
      recordStat(M_LINE_LENGTHS, new Long(line.length()));
    }
  }

  private static void registerAllViews() {
    // Defining the distribution aggregations
    Aggregation latencyDistribution =
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

    Aggregation lengthsDistribution =
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
    Aggregation countAggregation = Aggregation.Count.create();

    // So tagKeys
    List<TagKey> noKeys = new ArrayList<TagKey>();

    // Define the views
    View[] views =
        new View[] {
          View.create(
              Name.create("ocjavametrics/latency"),
              "The distribution of latencies",
              M_LATENCY_MS,
              latencyDistribution,
              Collections.singletonList(KEY_METHOD)),
          View.create(
              Name.create("ocjavametrics/lines_in"),
              "The number of lines read in from standard input",
              M_LINES_IN,
              countAggregation,
              noKeys),
          View.create(
              Name.create("ocjavametrics/errors"),
              "The number of errors encountered",
              M_ERRORS,
              countAggregation,
              Collections.singletonList(KEY_METHOD)),
          View.create(
              Name.create("ocjavametrics/line_lengths"),
              "The distribution of line lengths",
              M_LINE_LENGTHS,
              lengthsDistribution,
              noKeys)
        };

    // Create the view manager
    ViewManager vmgr = Stats.getViewManager();

    // Then finally register the views
    for (View view : views) {
      vmgr.registerView(view);
    }
  }

  private static void setupOpenCensusAndPrometheusExporter() throws IOException {
    // Firstly register the views
    registerAllViews();

    // Create and register the Prometheus exporter
    PrometheusStatsCollector.createAndRegister();

    // Run the server as a daemon on address "localhost:8889"
    HTTPServer server = new HTTPServer("localhost", 8889, true);
  }
}
