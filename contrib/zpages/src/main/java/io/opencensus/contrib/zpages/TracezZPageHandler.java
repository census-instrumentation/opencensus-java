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

package io.opencensus.contrib.zpages;

import static com.google.common.html.HtmlEscapers.htmlEscaper;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.Status.CanonicalCode;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.RunningSpanStore;
import io.opencensus.trace.export.SampledSpanStore;
import io.opencensus.trace.export.SampledSpanStore.ErrorFilter;
import io.opencensus.trace.export.SampledSpanStore.LatencyBucketBoundaries;
import io.opencensus.trace.export.SampledSpanStore.LatencyFilter;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanData.TimedEvents;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

// TODO(hailongwen): remove the usage of `NetworkEvent` in the future.
/**
 * HTML page formatter for tracing debug. The page displays information about all active spans and
 * all sampled spans based on latency and errors.
 *
 * <p>It prints a summary table which contains one row for each span name and data about number of
 * active and sampled spans.
 */
final class TracezZPageHandler extends ZPageHandler {
  private enum RequestType {
    RUNNING(0),
    FINISHED(1),
    FAILED(2),
    UNKNOWN(-1);

    private final int value;

    RequestType(int value) {
      this.value = value;
    }

    static RequestType fromString(String str) {
      int value = Integer.parseInt(str);
      switch (value) {
        case 0:
          return RUNNING;
        case 1:
          return FINISHED;
        case 2:
          return FAILED;
        default:
          return UNKNOWN;
      }
    }

    int getValue() {
      return value;
    }
  }

  private static final String TRACEZ_URL = "/tracez";
  private static final Tracer tracer = Tracing.getTracer();
  // Color to use for zebra-striping.
  private static final String ZEBRA_STRIPE_COLOR = "#F2F2F2";
  // Color for sampled traceIds.
  private static final String SAMPLED_TRACE_ID_COLOR = "#C1272D";
  // Color for not sampled traceIds
  private static final String NOT_SAMPLED_TRACE_ID_COLOR = "black";
  // The header for span name.
  private static final String HEADER_SPAN_NAME = "zspanname";
  // The header for type (running = 0, latency = 1, error = 2) to display.
  private static final String HEADER_SAMPLES_TYPE = "ztype";
  // The header for sub-type:
  // * for latency based samples [0, 8] representing the latency buckets, where 0 is the first one;
  // * for error based samples [0, 15], 0 - means all, otherwise the error code;
  private static final String HEADER_SAMPLES_SUB_TYPE = "zsubtype";
  // Map from LatencyBucketBoundaries to the human string displayed on the UI for each bucket.
  private static final Map<LatencyBucketBoundaries, String> LATENCY_BUCKET_BOUNDARIES_STRING_MAP =
      buildLatencyBucketBoundariesStringMap();
  @javax.annotation.Nullable private final RunningSpanStore runningSpanStore;
  @javax.annotation.Nullable private final SampledSpanStore sampledSpanStore;

  private TracezZPageHandler(
      @javax.annotation.Nullable RunningSpanStore runningSpanStore,
      @javax.annotation.Nullable SampledSpanStore sampledSpanStore) {
    this.runningSpanStore = runningSpanStore;
    this.sampledSpanStore = sampledSpanStore;
  }

  /**
   * Constructs a new {@code TracezZPageHandler}.
   *
   * @param runningSpanStore the instance of the {@code RunningSpanStore} to be used.
   * @param sampledSpanStore the instance of the {@code SampledSpanStore} to be used.
   * @return a new {@code TracezZPageHandler}.
   */
  static TracezZPageHandler create(
      @javax.annotation.Nullable RunningSpanStore runningSpanStore,
      @javax.annotation.Nullable SampledSpanStore sampledSpanStore) {
    return new TracezZPageHandler(runningSpanStore, sampledSpanStore);
  }

  @Override
  public String getUrlPath() {
    return TRACEZ_URL;
  }

  public static void emitStyle(PrintWriter out) {
    out.write("<style>\n");
    out.write(
        "body {font-family:'Roboto',sans-serif;font-size:14px;" + "background-color:#F2F4EC;}\n");
    out.write("h1{color:#3D3D3D;text-align:center; margin-bottom:20px;}\n");
    out.write("p{padding:0 0.5em;color: #3D3D3D}\n");
    out.write(
        "p.header{font-family:'Open Sans',sans-serif;top:0;left:0;width:100%;"
            + "height:60px;vertical-align:middle;color:#C1272D;font-size:22pt;}\n");
    out.write(".header span{color:#3D3D3D;}\n");
    out.write("img.oc{ vertical-align:middle;}\n");
    out.write(
        "table{color:#FFF;background-color:#FFF;overflow:hidden;"
            + "width:100%;margin-bottom:30px;}\n");
    out.write("th{line-height:3.0;padding:0 0.5em;}\n");
    out.write("tr.border td{border-bottom:1px solid #3D3D3D;}\n");
    out.write("tr.bgcolor_red{background-color:#A94442;}\n");
    out.write("td.column_head{text-align:center;color:#FFF;line-height:3.0;}\n");
    out.write("td{color:#3D3D3D;line-height:2.0;padding:0 0.5em;}\n");
    out.write("a{color:#A94442;}\n");
    out.write("td.border-right{border-right:1px solid #FFF;}\n");
    out.write("td.border-left{border-left:1px solid #FFF;}\n");
    out.write("td.border-left-blk{border-left:1px solid #000}\n");
    out.write("td.border-right-blk{border-right:1px solid #000}\n");
    out.write("</style>\n");
  }

  @Override
  public void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    PrintWriter out =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8)));
    out.write("<!DOCTYPE html>\n");
    out.write("<html lang=\"en\"><head>\n");
    out.write("<meta charset=\"utf-8\">\n");
    out.write("<title>TraceZ</title>\n");
    out.write("<link rel=\"shortcut icon\" href=\"//www.opencensus.io/favicon.ico\"/>\n");
    out.write(
        "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300\""
            + "rel=\"stylesheet\">\n");
    out.write(
        "<link href=\"https://fonts.googleapis.com/css?family=Roboto\"" + "rel=\"stylesheet\">\n");
    emitStyle(out);
    out.write("</head>\n");
    out.write("<body>\n");
    out.write(
        "<p class=\"header\">"
            + "<img class=\"oc\" src=\"https://opencensus.io/img/logo-sm.svg\" />"
            + "Open<span>Census</span></p>");
    out.write("<h1>TraceZ Summary</h1>\n");

    try {
      emitHtmlBody(queryMap, out);
    } catch (Throwable t) {
      out.write("Errors while generate the HTML page " + t);
    }
    out.write("</body>\n");
    out.write("</html>\n");
    out.close();
  }

  private void emitHtmlBody(Map<String, String> queryMap, PrintWriter out)
      throws UnsupportedEncodingException {
    if (runningSpanStore == null || sampledSpanStore == null) {
      out.write("OpenCensus implementation not available.");
      return;
    }
    Formatter formatter = new Formatter(out, Locale.US);
    emitSummaryTable(out, formatter);
    String spanName = queryMap.get(HEADER_SPAN_NAME);
    if (spanName != null) {
      tracer
          .getCurrentSpan()
          .addAnnotation(
              "Render spans.",
              ImmutableMap.<String, AttributeValue>builder()
                  .put("SpanName", AttributeValue.stringAttributeValue(spanName))
                  .build());
      String typeStr = queryMap.get(HEADER_SAMPLES_TYPE);
      if (typeStr != null) {
        List<SpanData> spans = null;
        RequestType type = RequestType.fromString(typeStr);
        if (type == RequestType.UNKNOWN) {
          return;
        }
        if (type == RequestType.RUNNING) {
          // Display running.
          spans =
              new ArrayList<>(
                  runningSpanStore.getRunningSpans(RunningSpanStore.Filter.create(spanName, 0)));
          // Sort active spans incremental.
          Collections.sort(spans, new SpanDataComparator(true));
        } else {
          String subtypeStr = queryMap.get(HEADER_SAMPLES_SUB_TYPE);
          if (subtypeStr != null) {
            int subtype = Integer.parseInt(subtypeStr);
            if (type == RequestType.FAILED) {
              if (subtype < 0 || subtype >= CanonicalCode.values().length) {
                return;
              }
              // Display errors. subtype 0 means all.
              CanonicalCode code = subtype == 0 ? null : CanonicalCode.values()[subtype];
              spans =
                  new ArrayList<>(
                      sampledSpanStore.getErrorSampledSpans(ErrorFilter.create(spanName, code, 0)));
            } else {
              if (subtype < 0 || subtype >= LatencyBucketBoundaries.values().length) {
                return;
              }
              // Display latency.
              LatencyBucketBoundaries latencyBucketBoundaries =
                  LatencyBucketBoundaries.values()[subtype];
              spans =
                  new ArrayList<>(
                      sampledSpanStore.getLatencySampledSpans(
                          LatencyFilter.create(
                              spanName,
                              latencyBucketBoundaries.getLatencyLowerNs(),
                              latencyBucketBoundaries.getLatencyUpperNs(),
                              0)));
              // Sort sampled spans decremental.
              Collections.sort(spans, new SpanDataComparator(false));
            }
          }
        }
        emitSpanNameAndCountPages(formatter, spanName, spans == null ? 0 : spans.size(), type);

        if (spans != null) {
          emitSpans(out, formatter, spans);
          emitLegend(out);
        }
      }
    }
  }

  private static void emitSpanNameAndCountPages(
      Formatter formatter, String spanName, int returnedNum, RequestType type) {
    formatter.format("<p><b>Span Name: %s </b></p>%n", htmlEscaper().escape(spanName));
    formatter.format(
        "%s Requests %d</b></p>%n",
        type == RequestType.RUNNING
            ? "Running"
            : type == RequestType.FINISHED ? "Finished" : "Failed",
        returnedNum);
  }

  /** Emits the list of SampledRequets with a header. */
  private static void emitSpans(PrintWriter out, Formatter formatter, Collection<SpanData> spans) {
    out.write("<pre>\n");
    formatter.format("%-23s %18s%n", "When", "Elapsed(s)");
    out.write("-------------------------------------------\n");
    for (SpanData span : spans) {
      tracer
          .getCurrentSpan()
          .addAnnotation(
              "Render span.",
              ImmutableMap.<String, AttributeValue>builder()
                  .put(
                      "SpanId",
                      AttributeValue.stringAttributeValue(
                          BaseEncoding.base16()
                              .lowerCase()
                              .encode(span.getContext().getSpanId().getBytes())))
                  .build());

      emitSingleSpan(out, formatter, span);
    }
    out.write("</pre>\n");
  }

  // Emits the internal html for a single {@link SpanData}.
  @SuppressWarnings("deprecation")
  private static void emitSingleSpan(PrintWriter out, Formatter formatter, SpanData span) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(span.getStartTimestamp().getSeconds()));
    long microsField = TimeUnit.NANOSECONDS.toMicros(span.getStartTimestamp().getNanos());
    String elapsedSecondsStr =
        span.getEndTimestamp() != null
            ? String.format(
                "%13.6f",
                durationToNanos(span.getEndTimestamp().subtractTimestamp(span.getStartTimestamp()))
                    * 1.0e-9)
            : String.format("%13s", " ");

    SpanContext spanContext = span.getContext();
    formatter.format(
        "<b>%04d/%02d/%02d-%02d:%02d:%02d.%06d %s     TraceId: <b style=\"color:%s;\">%s</b> "
            + "SpanId: %s ParentSpanId: %s</b>%n",
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND),
        microsField,
        elapsedSecondsStr,
        spanContext.getTraceOptions().isSampled()
            ? SAMPLED_TRACE_ID_COLOR
            : NOT_SAMPLED_TRACE_ID_COLOR,
        BaseEncoding.base16().lowerCase().encode(spanContext.getTraceId().getBytes()),
        BaseEncoding.base16().lowerCase().encode(spanContext.getSpanId().getBytes()),
        BaseEncoding.base16()
            .lowerCase()
            .encode(
                span.getParentSpanId() == null
                    ? SpanId.INVALID.getBytes()
                    : span.getParentSpanId().getBytes()));

    int lastEntryDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

    Timestamp lastTimestampNanos = span.getStartTimestamp();
    TimedEvents<Annotation> annotations = span.getAnnotations();
    TimedEvents<io.opencensus.trace.NetworkEvent> networkEvents = span.getNetworkEvents();
    List<TimedEvent<?>> timedEvents = new ArrayList<TimedEvent<?>>(annotations.getEvents());
    timedEvents.addAll(networkEvents.getEvents());
    Collections.sort(timedEvents, new TimedEventComparator());
    for (TimedEvent<?> event : timedEvents) {
      // Special printing so that durations smaller than one second
      // are left padded with blanks instead of '0' characters.
      // E.g.,
      //        Number                  Printout
      //        ---------------------------------
      //        0.000534                  .   534
      //        1.000534                 1.000534
      long deltaMicros =
          TimeUnit.NANOSECONDS.toMicros(
              durationToNanos(event.getTimestamp().subtractTimestamp(lastTimestampNanos)));
      String deltaString;
      if (deltaMicros >= 1000000) {
        deltaString = String.format("%.6f", (deltaMicros / 1000000.0));
      } else {
        deltaString = String.format(".%6d", deltaMicros);
      }

      calendar.setTimeInMillis(
          TimeUnit.SECONDS.toMillis(event.getTimestamp().getSeconds())
              + TimeUnit.NANOSECONDS.toMillis(event.getTimestamp().getNanos()));
      microsField = TimeUnit.NANOSECONDS.toMicros(event.getTimestamp().getNanos());

      int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
      if (dayOfYear == lastEntryDayOfYear) {
        formatter.format("%11s", "");
      } else {
        formatter.format(
            "%04d/%02d/%02d-",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH));
        lastEntryDayOfYear = dayOfYear;
      }

      formatter.format(
          "%02d:%02d:%02d.%06d %13s ... %s%n",
          calendar.get(Calendar.HOUR_OF_DAY),
          calendar.get(Calendar.MINUTE),
          calendar.get(Calendar.SECOND),
          microsField,
          deltaString,
          htmlEscaper()
              .escape(
                  event.getEvent() instanceof Annotation
                      ? renderAnnotation((Annotation) event.getEvent())
                      : renderNetworkEvents(
                          (io.opencensus.trace.NetworkEvent) castNonNull(event.getEvent()))));
      lastTimestampNanos = event.getTimestamp();
    }
    Status status = span.getStatus();
    if (status != null) {
      formatter.format("%44s %s%n", "", htmlEscaper().escape(renderStatus(status)));
    }
    formatter.format(
        "%44s %s%n",
        "", htmlEscaper().escape(renderAttributes(span.getAttributes().getAttributeMap())));
  }

  // TODO(sebright): Remove this method.
  @SuppressWarnings("nullness")
  private static <T> T castNonNull(@javax.annotation.Nullable T arg) {
    return arg;
  }

  // Emits the summary table with links to all samples.
  private void emitSummaryTable(PrintWriter out, Formatter formatter)
      throws UnsupportedEncodingException {
    if (runningSpanStore == null || sampledSpanStore == null) {
      return;
    }
    RunningSpanStore.Summary runningSpanStoreSummary = runningSpanStore.getSummary();
    SampledSpanStore.Summary sampledSpanStoreSummary = sampledSpanStore.getSummary();

    out.write("<table style='border-spacing: 0;\n");
    out.write("border-left:1px solid #3D3D3D;border-right:1px solid #3D3D3D;'>\n");
    emitSummaryTableHeader(out, formatter);

    Set<String> spanNames = new TreeSet<>(runningSpanStoreSummary.getPerSpanNameSummary().keySet());
    spanNames.addAll(sampledSpanStoreSummary.getPerSpanNameSummary().keySet());
    boolean zebraColor = true;
    for (String spanName : spanNames) {
      out.write("<tr class=\"border\">\n");
      if (!zebraColor) {
        out.write("<tr class=\"border\">\n");
      } else {
        formatter.format("<tr class=\"border\" style=\"background: %s\">%n", ZEBRA_STRIPE_COLOR);
      }
      zebraColor = !zebraColor;
      formatter.format("<td>%s</td>%n", htmlEscaper().escape(spanName));

      // Running
      out.write("<td class=\"border-right-blk\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
      RunningSpanStore.PerSpanNameSummary runningSpanStorePerSpanNameSummary =
          runningSpanStoreSummary.getPerSpanNameSummary().get(spanName);

      // subtype ignored for running requests.
      emitSingleCell(
          out,
          formatter,
          spanName,
          runningSpanStorePerSpanNameSummary == null
              ? 0
              : runningSpanStorePerSpanNameSummary.getNumRunningSpans(),
          RequestType.RUNNING,
          0);

      SampledSpanStore.PerSpanNameSummary sampledSpanStorePerSpanNameSummary =
          sampledSpanStoreSummary.getPerSpanNameSummary().get(spanName);

      // Latency based samples
      out.write("<td class=\"border-left-blk\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
      Map<LatencyBucketBoundaries, Integer> latencyBucketsSummaries =
          sampledSpanStorePerSpanNameSummary != null
              ? sampledSpanStorePerSpanNameSummary.getNumbersOfLatencySampledSpans()
              : null;
      int subtype = 0;
      for (LatencyBucketBoundaries latencyBucketsBoundaries : LatencyBucketBoundaries.values()) {
        if (latencyBucketsSummaries != null) {
          int numSamples =
              latencyBucketsSummaries.containsKey(latencyBucketsBoundaries)
                  ? latencyBucketsSummaries.get(latencyBucketsBoundaries)
                  : 0;
          emitSingleCell(out, formatter, spanName, numSamples, RequestType.FINISHED, subtype++);
        } else {
          // numSamples < -1 means "Not Available".
          emitSingleCell(out, formatter, spanName, -1, RequestType.FINISHED, subtype++);
        }
      }

      // Error based samples.
      out.write("<td class=\"border-right-blk\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
      if (sampledSpanStorePerSpanNameSummary != null) {
        Map<CanonicalCode, Integer> errorBucketsSummaries =
            sampledSpanStorePerSpanNameSummary.getNumbersOfErrorSampledSpans();
        int numErrorSamples = 0;
        for (Map.Entry<CanonicalCode, Integer> it : errorBucketsSummaries.entrySet()) {
          numErrorSamples += it.getValue();
        }
        // subtype 0 means all;
        emitSingleCell(out, formatter, spanName, numErrorSamples, RequestType.FAILED, 0);
      } else {
        // numSamples < -1 means "Not Available".
        emitSingleCell(out, formatter, spanName, -1, RequestType.FAILED, 0);
      }

      out.write("</tr>\n");
    }
    out.write("</table>");
  }

  private static void emitSummaryTableHeader(PrintWriter out, Formatter formatter) {
    // First line.
    out.write("<tr class=\"bgcolor_red\">\n");
    out.write("<td colspan=1 class=\"column_head\"><b>Span Name</b></td>\n");
    out.write("<td class=\"border-right\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
    out.write("<td colspan=1 class=\"column_head\"><b>Running</b></td>\n");
    out.write("<td class=\"border-left\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
    out.write("<td colspan=9 class=\"column_head\"><b>Latency Samples</b></td>\n");
    out.write("<td class=\"border-right\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
    out.write("<td colspan=1 class=\"column_head\"><b>Error Samples</b></td>\n");
    out.write("</tr>\n");
    // Second line.
    out.write("<tr class=\"bgcolor_red\">\n");
    out.write("<td colspan=1></td>\n");
    out.write("<td class=\"border-right\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
    out.write("<td colspan=1></td>\n");
    out.write("<td class=\"border-left\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
    for (LatencyBucketBoundaries latencyBucketsBoundaries : LatencyBucketBoundaries.values()) {
      formatter.format(
          "<td colspan=1 align=\"center\" style=\"color:#FFF\"><b>[%s]</b></td>%n",
          LATENCY_BUCKET_BOUNDARIES_STRING_MAP.get(latencyBucketsBoundaries));
    }
    out.write("<td class=\"border-right\">&nbsp;&nbsp;&nbsp;&nbsp;</td>");
    out.write("<td colspan=1></td>\n");
    out.write("</tr>\n");
  }

  // If numSamples is greater than 0 then emit a link to see span data, if the numSamples is
  // negative then print "N/A", otherwise print the text "0".
  private static void emitSingleCell(
      PrintWriter out,
      Formatter formatter,
      String spanName,
      int numSamples,
      RequestType type,
      int subtype)
      throws UnsupportedEncodingException {
    if (numSamples > 0) {
      formatter.format(
          "<td align=\"center\"><a href='?%s=%s&%s=%d&%s=%d'>%d</a></td>%n",
          HEADER_SPAN_NAME,
          URLEncoder.encode(spanName, "UTF-8"),
          HEADER_SAMPLES_TYPE,
          type.getValue(),
          HEADER_SAMPLES_SUB_TYPE,
          subtype,
          numSamples);
    } else if (numSamples < 0) {
      out.write("<td align=\"center\">N/A</td>\n");
    } else {
      out.write("<td align=\"center\">0</td>\n");
    }
  }

  private static void emitLegend(PrintWriter out) {
    out.write("<br>\n");
    out.printf(
        "<p><b style=\"color:%s;\">TraceId</b> means sampled request. "
            + "<b style=\"color:%s;\">TraceId</b> means not sampled request.</p>%n",
        SAMPLED_TRACE_ID_COLOR, NOT_SAMPLED_TRACE_ID_COLOR);
  }

  private static Map<LatencyBucketBoundaries, String> buildLatencyBucketBoundariesStringMap() {
    Map<LatencyBucketBoundaries, String> ret = new HashMap<>();
    for (LatencyBucketBoundaries latencyBucketBoundaries : LatencyBucketBoundaries.values()) {
      ret.put(latencyBucketBoundaries, latencyBucketBoundariesToString(latencyBucketBoundaries));
    }
    return Collections.unmodifiableMap(ret);
  }

  private static long durationToNanos(Duration duration) {
    return TimeUnit.SECONDS.toNanos(duration.getSeconds()) + duration.getNanos();
  }

  private static String latencyBucketBoundariesToString(
      LatencyBucketBoundaries latencyBucketBoundaries) {
    switch (latencyBucketBoundaries) {
      case ZERO_MICROSx10:
        return ">0us";
      case MICROSx10_MICROSx100:
        return ">10us";
      case MICROSx100_MILLIx1:
        return ">100us";
      case MILLIx1_MILLIx10:
        return ">1ms";
      case MILLIx10_MILLIx100:
        return ">10ms";
      case MILLIx100_SECONDx1:
        return ">100ms";
      case SECONDx1_SECONDx10:
        return ">1s";
      case SECONDx10_SECONDx100:
        return ">10s";
      case SECONDx100_MAX:
        return ">100s";
    }
    throw new IllegalArgumentException("No value string available for: " + latencyBucketBoundaries);
  }

  @SuppressWarnings("deprecation")
  private static String renderNetworkEvents(io.opencensus.trace.NetworkEvent networkEvent) {
    StringBuilder stringBuilder = new StringBuilder();
    if (networkEvent.getType() == io.opencensus.trace.NetworkEvent.Type.RECV) {
      stringBuilder.append("Received message");
    } else if (networkEvent.getType() == io.opencensus.trace.NetworkEvent.Type.SENT) {
      stringBuilder.append("Sent message");
    } else {
      stringBuilder.append("Unknown");
    }
    stringBuilder.append(" id=");
    stringBuilder.append(networkEvent.getMessageId());
    stringBuilder.append(" uncompressed_size=");
    stringBuilder.append(networkEvent.getUncompressedMessageSize());
    stringBuilder.append(" compressed_size=");
    stringBuilder.append(networkEvent.getCompressedMessageSize());
    return stringBuilder.toString();
  }

  private static String renderAnnotation(Annotation annotation) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(annotation.getDescription());
    if (!annotation.getAttributes().isEmpty()) {
      stringBuilder.append(" ");
      stringBuilder.append(renderAttributes(annotation.getAttributes()));
    }
    return stringBuilder.toString();
  }

  private static String renderStatus(Status status) {
    return status.toString();
  }

  private static String renderAttributes(Map<String, AttributeValue> attributes) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Attributes:{");
    boolean first = true;
    for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
      if (first) {
        first = false;
        stringBuilder.append(entry.getKey());
        stringBuilder.append("=");
        stringBuilder.append(attributeValueToString(entry.getValue()));
      } else {
        stringBuilder.append(", ");
        stringBuilder.append(entry.getKey());
        stringBuilder.append("=");
        stringBuilder.append(attributeValueToString(entry.getValue()));
      }
    }
    stringBuilder.append("}");
    return stringBuilder.toString();
  }

  @javax.annotation.Nullable
  private static String attributeValueToString(AttributeValue attributeValue) {
    return attributeValue.match(
        new Function<String, /*@Nullable*/ String>() {
          @Override
          public String apply(String stringValue) {
            return stringValue;
          }
        },
        new Function<Boolean, /*@Nullable*/ String>() {
          @Override
          public String apply(Boolean booleanValue) {
            return booleanValue.toString();
          }
        },
        new Function<Long, /*@Nullable*/ String>() {
          @Override
          public String apply(Long longValue) {
            return longValue.toString();
          }
        },
        Functions.</*@Nullable*/ String>returnNull());
  }

  private static final class TimedEventComparator
      implements Comparator<TimedEvent<?>>, Serializable {
    private static final long serialVersionUID = 0;

    @Override
    public int compare(TimedEvent<?> o1, TimedEvent<?> o2) {
      return o1.getTimestamp().compareTo(o2.getTimestamp());
    }
  }

  private static final class SpanDataComparator implements Comparator<SpanData>, Serializable {
    private static final long serialVersionUID = 0;
    private final boolean incremental;

    /**
     * Returns a new {@code SpanDataComparator}.
     *
     * @param incremental {@code true} if sorted incremental.
     */
    private SpanDataComparator(boolean incremental) {
      this.incremental = incremental;
    }

    @Override
    public int compare(SpanData o1, SpanData o2) {
      return incremental
          ? o1.getStartTimestamp().compareTo(o2.getStartTimestamp())
          : o2.getStartTimestamp().compareTo(o1.getStartTimestamp());
    }
  }
}
