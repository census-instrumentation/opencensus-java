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

package io.opencensus.contrib.zpages;

import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_ERROR_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_ERROR_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_ERROR_COUNT_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_FINISHED_COUNT_CUMULATIVE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_FINISHED_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_FINISHED_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_REQUEST_BYTES_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_REQUEST_COUNT_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_RESPONSE_BYTES_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_RESPONSE_BYTES_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_RESPONSE_BYTES_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_RESPONSE_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_RESPONSE_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_RESPONSE_COUNT_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_STARTED_COUNT_CUMULATIVE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_STARTED_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_STARTED_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_ERROR_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_ERROR_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_ERROR_COUNT_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_FINISHED_COUNT_CUMULATIVE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_FINISHED_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_FINISHED_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_REQUEST_BYTES_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_REQUEST_BYTES_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_REQUEST_BYTES_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_REQUEST_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_REQUEST_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_REQUEST_COUNT_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_RESPONSE_BYTES_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_RESPONSE_BYTES_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_RESPONSE_BYTES_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_RESPONSE_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_RESPONSE_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_RESPONSE_COUNT_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_SERVER_ELAPSED_TIME_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_SERVER_LATENCY_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_SERVER_LATENCY_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_SERVER_LATENCY_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_STARTED_COUNT_CUMULATIVE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_STARTED_COUNT_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_STARTED_COUNT_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW;
import static io.opencensus.contrib.grpc.metrics.RpcViewConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_VIEW;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.opencensus.common.Duration;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagValue;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

/** HTML page formatter for gRPC cumulative and interval stats. */
final class RpczZPageHandler extends ZPageHandler {

  private final ViewManager viewManager;

  private static final String RPCZ_URL = "/rpcz";
  private static final String SENT = "Sent";
  private static final String RECEIVED = "Received";
  private static final String TITLE_COLOR = "\"#eeeeff\"";
  private static final String TABLE_HEADER_COLOR = "\"#fff5ee\"";
  private static final String TABLE_ROW_COLOR = "\"#eee5de\"";
  private static final double SECONDS_PER_MINUTE = 60.0;
  private static final double SECONDS_PER_HOUR = 3600.0;
  private static final double NANOS_PER_SECOND = 1e9;
  private static final double BYTES_PER_KB = 1024;
  private static final ImmutableList<String> RPC_STATS_TYPES =
      ImmutableList.of(
          "Count",
          "Avg latency (ms)",
          // TODO(songya): add a column for latency percentiles.
          "Rate (rpc/s)",
          "Input (kb/s)",
          "Output (kb/s)",
          "Errors");

  private static final ImmutableList<View> CLIENT_RPC_CUMULATIVE_VIEWS =
      ImmutableList.of(
          RPC_CLIENT_ERROR_COUNT_VIEW,
          RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW,
          RPC_CLIENT_REQUEST_BYTES_VIEW,
          RPC_CLIENT_RESPONSE_BYTES_VIEW,
          RPC_CLIENT_STARTED_COUNT_CUMULATIVE_VIEW,
          // The last 5 views are not used yet.
          RPC_CLIENT_REQUEST_COUNT_VIEW,
          RPC_CLIENT_RESPONSE_COUNT_VIEW,
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_VIEW,
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_VIEW,
          RPC_CLIENT_FINISHED_COUNT_CUMULATIVE_VIEW);

  private static final ImmutableList<View> SERVER_RPC_CUMULATIVE_VIEWS =
      ImmutableList.of(
          RPC_SERVER_ERROR_COUNT_VIEW,
          RPC_SERVER_SERVER_LATENCY_VIEW,
          RPC_SERVER_REQUEST_BYTES_VIEW,
          RPC_SERVER_RESPONSE_BYTES_VIEW,
          RPC_SERVER_STARTED_COUNT_CUMULATIVE_VIEW,
          // The last 5 views are not used yet.
          RPC_SERVER_REQUEST_COUNT_VIEW,
          RPC_SERVER_RESPONSE_COUNT_VIEW,
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_VIEW,
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_VIEW,
          RPC_SERVER_FINISHED_COUNT_CUMULATIVE_VIEW);

  // Interval views may be removed in the future.
  private static final ImmutableList<View> CLIENT_RPC_MINUTE_VIEWS =
      ImmutableList.of(
          RPC_CLIENT_ERROR_COUNT_MINUTE_VIEW,
          RPC_CLIENT_ROUNDTRIP_LATENCY_MINUTE_VIEW,
          RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW,
          RPC_CLIENT_RESPONSE_BYTES_MINUTE_VIEW,
          RPC_CLIENT_STARTED_COUNT_MINUTE_VIEW,
          // The last 5 views are not used yet.
          RPC_CLIENT_REQUEST_COUNT_MINUTE_VIEW,
          RPC_CLIENT_RESPONSE_COUNT_MINUTE_VIEW,
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW,
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW,
          RPC_CLIENT_FINISHED_COUNT_MINUTE_VIEW);

  // Interval views may be removed in the future.
  private static final ImmutableList<View> SERVER_RPC_MINUTE_VIEWS =
      ImmutableList.of(
          RPC_SERVER_ERROR_COUNT_MINUTE_VIEW,
          RPC_SERVER_SERVER_LATENCY_MINUTE_VIEW,
          RPC_SERVER_REQUEST_BYTES_MINUTE_VIEW,
          RPC_SERVER_RESPONSE_BYTES_MINUTE_VIEW,
          RPC_SERVER_STARTED_COUNT_MINUTE_VIEW,
          // The last 5 views are not used yet.
          RPC_SERVER_REQUEST_COUNT_MINUTE_VIEW,
          RPC_SERVER_RESPONSE_COUNT_MINUTE_VIEW,
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_MINUTE_VIEW,
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_MINUTE_VIEW,
          RPC_SERVER_FINISHED_COUNT_MINUTE_VIEW);

  // Interval views may be removed in the future.
  private static final ImmutableList<View> CLIENT_RPC_HOUR_VIEWS =
      ImmutableList.of(
          RPC_CLIENT_ERROR_COUNT_HOUR_VIEW,
          RPC_CLIENT_ROUNDTRIP_LATENCY_HOUR_VIEW,
          RPC_CLIENT_REQUEST_BYTES_HOUR_VIEW,
          RPC_CLIENT_RESPONSE_BYTES_HOUR_VIEW,
          RPC_CLIENT_STARTED_COUNT_HOUR_VIEW,
          // The last 5 views are not used yet.
          RPC_CLIENT_REQUEST_COUNT_HOUR_VIEW,
          RPC_CLIENT_RESPONSE_COUNT_HOUR_VIEW,
          RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW,
          RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW,
          RPC_CLIENT_FINISHED_COUNT_HOUR_VIEW);

  // Interval views may be removed in the future.
  private static final ImmutableList<View> SERVER_RPC_HOUR_VIEWS =
      ImmutableList.of(
          RPC_SERVER_ERROR_COUNT_HOUR_VIEW,
          RPC_SERVER_SERVER_LATENCY_HOUR_VIEW,
          RPC_SERVER_SERVER_ELAPSED_TIME_HOUR_VIEW,
          RPC_SERVER_REQUEST_BYTES_HOUR_VIEW,
          RPC_SERVER_RESPONSE_BYTES_HOUR_VIEW,
          RPC_SERVER_STARTED_COUNT_HOUR_VIEW,
          // The last 5 views are not used yet.
          RPC_SERVER_REQUEST_COUNT_HOUR_VIEW,
          RPC_SERVER_RESPONSE_COUNT_HOUR_VIEW,
          RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES_HOUR_VIEW,
          RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES_HOUR_VIEW,
          RPC_SERVER_FINISHED_COUNT_HOUR_VIEW);

  @Override
  public String getUrlPath() {
    return RPCZ_URL;
  }

  @Override
  public void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    PrintWriter out =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8)));
    out.write("<!DOCTYPE html>\n");
    out.write("<html lang=\"en\"><head>\n");
    out.write("<meta charset=\"utf-8\">\n");
    out.write("<title>RpcZ</title>\n");
    out.write("<link rel=\"shortcut icon\" href=\"//www.opencensus.io/favicon.ico\"/>\n");
    out.write("</head>\n");
    out.write("<body>\n");
    try {
      emitHtmlBody(out);
    } catch (Throwable t) {
      out.write("Errors while generate the HTML page " + t);
    }
    out.write("</body>\n");
    out.write("</html>\n");
    out.close();
  }

  private void emitHtmlBody(PrintWriter out) {
    Formatter formatter = new Formatter(out, Locale.US);
    out.write("<h1>RPC Stats</h1>");
    out.write("<p></p>");
    emitSummaryTable(out, formatter, /* isReceived= */ false);
    emitSummaryTable(out, formatter, /* isReceived= */ true);
  }

  private void emitSummaryTable(PrintWriter out, Formatter formatter, boolean isReceived) {
    formatter.format(
        "<p><table bgcolor=%s width=100%%><tr align=center><td><font size=+2>"
            + "%s</font></td></tr></table></p>",
        TITLE_COLOR, (isReceived ? RECEIVED : SENT));
    formatter.format(
        "<table bgcolor=%s frame=box cellspacing=0 cellpadding=2>", TABLE_HEADER_COLOR);
    emitSummaryTableHeader(out, formatter);
    Map<String, StatsSnapshot> snapshots = getStatsSnapshots(isReceived);
    for (Entry<String, StatsSnapshot> entry : snapshots.entrySet()) {
      emitSummaryTableRows(out, formatter, entry.getValue(), entry.getKey());
    }
    out.write("</table>");
  }

  private static void emitSummaryTableHeader(PrintWriter out, Formatter formatter) {
    // First line.
    formatter.format("<tr bgcolor=%s>", TABLE_ROW_COLOR);
    out.write("<th></th><td></td>");
    for (String rpcStatsType : RPC_STATS_TYPES) {
      formatter.format("<th class=\"l1\" colspan=3>%s</th><td></td>", rpcStatsType);
    }
    out.write("</tr>");

    // Second line.
    formatter.format("<tr bgcolor=%s>", TABLE_ROW_COLOR);
    out.write("<th align=left>Method</th><td>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
    for (int i = 0; i < RPC_STATS_TYPES.size(); i++) {
      out.write("<th align=right>Min.</th><th align=right>Hr.</th><th align=right>Tot.</th>");
      if (i != RPC_STATS_TYPES.size() - 1) { // Add spaces between each column.
        out.write("<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
      }
    }
    out.write("</tr>");

    // Empty line.
    out.write("<tr><td colspan=33><font size=-2>&nbsp;</font></td></tr>");
  }

  private static void emitSummaryTableRows(
      PrintWriter out, Formatter formatter, StatsSnapshot snapshot, String method) {
    out.write("<tr>");
    formatter.format("<td><b>%s</b></td>", method);
    out.write("<td></td>");
    formatter.format("<td align=\"right\">%d</td>", snapshot.countLastMinute);
    formatter.format("<td align=\"right\">%d</td>", snapshot.countLastHour);
    formatter.format("<td align=\"right\">%d</td><td></td>", snapshot.countTotal);
    formatter.format("<td align=\"right\">%.3f</td>", snapshot.avgLatencyLastMinute);
    formatter.format("<td align=\"right\">%.3f</td>", snapshot.avgLatencyLastHour);
    formatter.format("<td align=\"right\">%.3f</td><td></td>", snapshot.avgLatencyTotal);
    formatter.format("<td align=\"right\">%.3f</td>", snapshot.rpcRateLastMinute);
    formatter.format("<td align=\"right\">%.3f</td>", snapshot.rpcRateLastHour);
    formatter.format("<td align=\"right\">%.3f</td><td></td>", snapshot.rpcRateTotal);
    formatter.format("<td align=\"right\">%.3f</td>", snapshot.inputRateLastMinute);
    formatter.format("<td align=\"right\">%.3f</td>", snapshot.inputRateLastHour);
    formatter.format("<td align=\"right\">%.3f</td><td></td>", snapshot.inputRateTotal);
    formatter.format("<td align=\"right\">%.3f</td>", snapshot.outputRateLastMinute);
    formatter.format("<td align=\"right\">%.3f</td>", snapshot.outputRateLastHour);
    formatter.format("<td align=\"right\">%.3f</td><td></td>", snapshot.outputRateTotal);
    formatter.format("<td align=\"right\">%d</td>", snapshot.errorsLastMinute);
    formatter.format("<td align=\"right\">%d</td>", snapshot.errorsLastHour);
    formatter.format("<td align=\"right\">%d</td><td></td>", snapshot.errorsTotal);
    out.write("</tr>");
  }

  // Gets stats snapshot for each method.
  private Map<String, StatsSnapshot> getStatsSnapshots(boolean isReceived) {
    SortedMap<String, StatsSnapshot> map = Maps.newTreeMap(); // Sorted by method name.
    if (isReceived) {
      getStatsSnapshots(map, SERVER_RPC_CUMULATIVE_VIEWS);
      getStatsSnapshots(map, SERVER_RPC_MINUTE_VIEWS);
      getStatsSnapshots(map, SERVER_RPC_HOUR_VIEWS);
    } else {
      getStatsSnapshots(map, CLIENT_RPC_CUMULATIVE_VIEWS);
      getStatsSnapshots(map, CLIENT_RPC_MINUTE_VIEWS);
      getStatsSnapshots(map, CLIENT_RPC_HOUR_VIEWS);
    }
    return map;
  }

  private void getStatsSnapshots(Map<String, StatsSnapshot> map, List<View> views) {
    for (View view : views) {
      ViewData viewData = viewManager.getView(view.getName());
      if (viewData == null) {
        continue;
      }
      for (Entry<List<TagValue>, AggregationData> entry : viewData.getAggregationMap().entrySet()) {
        String method;
        List<TagValue> tagValues = entry.getKey();
        if (tagValues.size() == 1) {
          method = tagValues.get(0).asString();
        } else { // Error count views have two tag key: status and method.
          method = tagValues.get(1).asString();
        }
        StatsSnapshot snapshot = map.get(method);
        if (snapshot == null) {
          snapshot = new StatsSnapshot();
          map.put(method, snapshot);
        }

        getStats(snapshot, entry.getValue(), view, viewData.getWindowData());
      }
    }
  }

  // Gets RPC stats by its view definition, and set it to stats snapshot.
  private static void getStats(
      StatsSnapshot snapshot, AggregationData data, View view, AggregationWindowData windowData) {
    if (view == RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW || view == RPC_SERVER_SERVER_LATENCY_VIEW) {
      snapshot.avgLatencyTotal = ((DistributionData) data).getMean();
    } else if (view == RPC_CLIENT_ROUNDTRIP_LATENCY_MINUTE_VIEW
        || view == RPC_SERVER_SERVER_LATENCY_MINUTE_VIEW) {
      snapshot.avgLatencyLastMinute = ((MeanData) data).getMean();
    } else if (view == RPC_CLIENT_ROUNDTRIP_LATENCY_HOUR_VIEW
        || view == RPC_SERVER_SERVER_LATENCY_HOUR_VIEW) {
      snapshot.avgLatencyLastHour = ((MeanData) data).getMean();
    } else if (view == RPC_CLIENT_ERROR_COUNT_VIEW || view == RPC_SERVER_ERROR_COUNT_VIEW) {
      snapshot.errorsTotal = ((MeanData) data).getCount();
    } else if (view == RPC_CLIENT_ERROR_COUNT_MINUTE_VIEW
        || view == RPC_SERVER_ERROR_COUNT_MINUTE_VIEW) {
      snapshot.errorsLastMinute = ((MeanData) data).getCount();
    } else if (view == RPC_CLIENT_ERROR_COUNT_HOUR_VIEW
        || view == RPC_SERVER_ERROR_COUNT_HOUR_VIEW) {
      snapshot.errorsLastHour = ((MeanData) data).getCount();
    } else if (view == RPC_CLIENT_REQUEST_BYTES_VIEW || view == RPC_SERVER_REQUEST_BYTES_VIEW) {
      DistributionData distributionData = (DistributionData) data;
      snapshot.inputRateTotal =
          distributionData.getCount()
              * distributionData.getMean()
              / BYTES_PER_KB
              / getDurationInSecs((CumulativeData) windowData);
    } else if (view == RPC_CLIENT_REQUEST_BYTES_MINUTE_VIEW
        || view == RPC_SERVER_REQUEST_BYTES_MINUTE_VIEW) {
      MeanData meanData = (MeanData) data;
      snapshot.inputRateLastMinute =
          meanData.getMean() * meanData.getCount() / BYTES_PER_KB / SECONDS_PER_MINUTE;
    } else if (view == RPC_CLIENT_REQUEST_BYTES_HOUR_VIEW
        || view == RPC_SERVER_REQUEST_BYTES_HOUR_VIEW) {
      MeanData meanData = (MeanData) data;
      snapshot.inputRateLastHour =
          meanData.getMean() * meanData.getCount() / BYTES_PER_KB / SECONDS_PER_HOUR;
    } else if (view == RPC_CLIENT_RESPONSE_BYTES_VIEW || view == RPC_SERVER_RESPONSE_BYTES_VIEW) {
      DistributionData distributionData = (DistributionData) data;
      snapshot.outputRateTotal =
          distributionData.getCount()
              * distributionData.getMean()
              / BYTES_PER_KB
              / getDurationInSecs((CumulativeData) windowData);
    } else if (view == RPC_CLIENT_RESPONSE_BYTES_MINUTE_VIEW
        || view == RPC_SERVER_RESPONSE_BYTES_MINUTE_VIEW) {
      MeanData meanData = (MeanData) data;
      snapshot.outputRateLastMinute =
          meanData.getMean() * meanData.getCount() / BYTES_PER_KB / SECONDS_PER_MINUTE;
    } else if (view == RPC_CLIENT_RESPONSE_BYTES_HOUR_VIEW
        || view == RPC_SERVER_RESPONSE_BYTES_HOUR_VIEW) {
      MeanData meanData = (MeanData) data;
      snapshot.outputRateLastHour =
          meanData.getMean() * meanData.getCount() / BYTES_PER_KB / SECONDS_PER_HOUR;
    } else if (view == RPC_CLIENT_STARTED_COUNT_MINUTE_VIEW
        || view == RPC_SERVER_STARTED_COUNT_MINUTE_VIEW) {
      snapshot.countLastMinute = ((CountData) data).getCount();
      snapshot.rpcRateLastMinute = snapshot.countLastMinute / SECONDS_PER_MINUTE;
    } else if (view == RPC_CLIENT_STARTED_COUNT_HOUR_VIEW
        || view == RPC_SERVER_STARTED_COUNT_HOUR_VIEW) {
      snapshot.countLastHour = ((CountData) data).getCount();
      snapshot.rpcRateLastHour = snapshot.countLastHour / SECONDS_PER_HOUR;
    } else if (view == RPC_CLIENT_STARTED_COUNT_CUMULATIVE_VIEW
        || view == RPC_SERVER_STARTED_COUNT_CUMULATIVE_VIEW) {
      snapshot.countTotal = ((CountData) data).getCount();
      snapshot.rpcRateTotal = snapshot.countTotal / getDurationInSecs((CumulativeData) windowData);
    } // TODO(songya): compute and store latency percentiles.
  }

  // Calculates the duration of the given CumulativeData in seconds.
  private static double getDurationInSecs(CumulativeData cumulativeData) {
    return toDoubleSeconds(cumulativeData.getEnd().subtractTimestamp(cumulativeData.getStart()));
  }

  // Converts a Duration to seconds. Converts the nanoseconds of the given duration to decimals of
  // second, and adds it to the second of duration.
  // For example, Duration.create(/* seconds */ 5, /* nanos */ 5 * 1e8) will be converted to 5.5
  // seconds.
  private static double toDoubleSeconds(Duration duration) {
    return duration.getNanos() / NANOS_PER_SECOND + duration.getSeconds();
  }

  static RpczZPageHandler create(ViewManager viewManager) {
    return new RpczZPageHandler(viewManager);
  }

  private RpczZPageHandler(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  private static class StatsSnapshot {
    long countLastMinute;
    long countLastHour;
    long countTotal;
    double rpcRateLastMinute;
    double rpcRateLastHour;
    double rpcRateTotal;
    double avgLatencyLastMinute;
    double avgLatencyLastHour;
    double avgLatencyTotal;
    double inputRateLastMinute;
    double inputRateLastHour;
    double inputRateTotal;
    double outputRateLastMinute;
    double outputRateLastHour;
    double outputRateTotal;
    long errorsLastMinute;
    long errorsLastHour;
    long errorsTotal;
  }
}
