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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.Measure;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.concurrent.GuardedBy;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** HTML page formatter for all exported {@link View}s. */
final class StatszZPageHandler extends ZPageHandler {

  private static final Object monitor = new Object();

  private final ViewManager viewManager;

  // measures, cachedViews and root are created when StatszZPageHandler is initialized, and will
  // be updated every time when there's a new View from viewManager.getAllExportedViews().
  // viewManager.getAllExportedViews() will be called every time when the StatsZ page is
  // re-rendered, like refreshing or navigating to other paths.

  @GuardedBy("monitor")
  private final Map<String, Measure> measures = Maps.newTreeMap();

  @GuardedBy("monitor")
  private final Set<View> cachedViews = Sets.newHashSet();

  @GuardedBy("monitor")
  private final TreeNode root = new TreeNode();

  @VisibleForTesting static final String QUERY_PATH = "path";
  private static final String STATSZ_URL = "/statsz";
  private static final String TITLE_COLOR = "#eeeeff";
  private static final String TABLE_BACKGROUND_COLOR = "#fff5ee";
  private static final String TABLE_HEADER_COLOR = "#eee5de";
  private static final String ALIGN_CENTER = "align=\"center\"";
  private static final String TABLE_BORDER = "border=\"1\"";
  private static final String CLASS_LARGER_TR = "directory-tr";
  private static final String TABLE_HEADER_VIEW = "View Name";
  private static final String TABLE_HEADER_DESCRIPTION = "Description";
  private static final String TABLE_HEADER_MEASURE = "Measure";
  private static final String TABLE_HEADER_AGGREGATION = "Aggregation Type";
  private static final String TABLE_HEADER_START = "Start Time";
  private static final String TABLE_HEADER_END = "End Time";
  private static final String TABLE_HEADER_UNIT = "Unit";
  private static final String TABLE_HEADER_MEASURE_TYPE = "Type";
  private static final String TABLE_HEADER_SUM = "Sum";
  private static final String TABLE_HEADER_COUNT = "Count";
  private static final String TABLE_HEADER_MEAN = "Mean";
  private static final String TABLE_HEADER_MAX = "Max";
  private static final String TABLE_HEADER_MIN = "Min";
  private static final String TABLE_HEADER_DEV = "Sum of Squared Deviations";
  private static final String TABLE_HEADER_HISTOGRAM = "Histogram";
  private static final String TABLE_HEADER_RANGE = "Range";
  private static final String TABLE_HEADER_BUCKET_SIZE = "Bucket Size";
  private static final long MILLIS_PER_SECOND = 1000;
  private static final long NANOS_PER_MILLISECOND = 1000 * 1000;
  private static final Splitter PATH_SPLITTER = Splitter.on('/');

  @Override
  public String getUrlPath() {
    return STATSZ_URL;
  }

  @Override
  public void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    PrintWriter out =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8)));
    out.write("<!DOCTYPE html>\n");
    out.write("<html lang=\"en\"><head>\n");
    out.write("<meta charset=\"utf-8\">\n");
    out.write("<title>StatsZ</title>\n");
    out.write("<link rel=\"shortcut icon\" href=\"//www.opencensus.io/favicon.ico\"/>\n");
    Formatter formatter = new Formatter(out, Locale.US);
    emitStyles(out, formatter);
    out.write("</head>\n");
    out.write("<body>\n");
    try {
      emitHtmlBody(queryMap, out, formatter);
    } catch (Throwable t) {
      out.write("Errors while generate the HTML page " + t);
    }
    out.write("</body>\n");
    out.write("</html>\n");
    out.close();
  }

  private static void emitStyles(PrintWriter out, Formatter formatter) {
    out.write("<style>");
    formatter.format("h2{background-color: %s;}", TITLE_COLOR);
    formatter.format("table{background-color: %s;}", TABLE_BACKGROUND_COLOR);
    formatter.format("thead{background-color: %s;}", TABLE_HEADER_COLOR);
    formatter.format(".%s{font-size:150%%}", CLASS_LARGER_TR);
    out.write("</style>");
  }

  private void emitHtmlBody(Map<String, String> queryMap, PrintWriter out, Formatter formatter) {
    synchronized (monitor) {
      groupViewsByDirectoriesAndGetMeasures(
          viewManager.getAllExportedViews(), root, measures, cachedViews);
      out.write("<h1><a href='?'>StatsZ</a></h1>");
      out.write("<p></p>");
      String path = queryMap.get(QUERY_PATH);
      TreeNode current = findNode(path);
      emitDirectoryTable(current, path, out, formatter);
      if (current != null && current.view != null) {
        ViewData viewData = viewManager.getView(current.view.getName());
        emitViewData(viewData, current.view.getName(), out, formatter);
      }
      emitMeasureTable(measures, out, formatter);
    }
  }

  // Parses view names, creates a tree that represents the directory structure and put each view
  // under appropriate directory. Also gets measures from the given views.
  // Directories are the namespaces in view name, separated by '/'.
  private static void groupViewsByDirectoriesAndGetMeasures(
      Set<View> views, TreeNode root, Map<String, Measure> measures, Set<View> cachedViews) {
    for (View view : views) {
      if (cachedViews.contains(view)) {
        continue;
      }
      cachedViews.add(view);

      List<String> dirs = PATH_SPLITTER.splitToList(view.getName().asString());
      TreeNode node = root;
      for (int i = 0; i < dirs.size(); i++) {
        if (node == null) {
          break; // Should never happen. Work around the nullness checker.
        }
        String dir = dirs.get(i);
        if ("".equals(dir) && i == 0) {
          continue; // In case view name starts with a '/'.
        }
        node.views++;
        if (i != dirs.size() - 1) { // Non-leaf node (directory node)
          node.children.putIfAbsent(dir, new TreeNode());
          node = node.children.get(dir);
        } else { // Leaf node (view node)
          node.children.putIfAbsent(dir, new TreeNode(view));
        }
      }

      Measure measure = view.getMeasure();
      measures.putIfAbsent(measure.getName(), measure);
    }
  }

  @GuardedBy("monitor")
  private void emitDirectoryTable(
      /*@Nullable*/ TreeNode currentNode,
      /*@Nullable*/ String path,
      PrintWriter out,
      Formatter formatter) {
    out.write("<h2>Views</h2>");
    if (currentNode == null) {
      formatter.format(
          "<p><font size=+2>Directory not found: %s. Return to root.</font></p>", path);
      currentNode = root;
    }
    if (currentNode == root || path == null) {
      path = "";
    }
    emitDirectoryHeader(path, out, formatter);
    out.write("<table frame=box cellspacing=0 cellpadding=2>");
    for (Entry<String, TreeNode> entry : currentNode.children.entrySet()) {
      TreeNode child = entry.getValue();
      String relativePath = entry.getKey();
      if (child.view == null) { // Directory node, emit a row for directory.
        formatter.format(
            "<tr class=\"%s\"><td>Directory: <a href='?%s=%s'>%s</a> (%d %s)</td></tr>",
            CLASS_LARGER_TR,
            QUERY_PATH,
            path + '/' + relativePath,
            relativePath,
            child.views,
            child.views > 1 ? "views" : "view");
      } else { // View node, emit a row for view.
        String viewName = child.view.getName().asString();
        formatter.format(
            "<tr class=\"%s\"><td>View: <a href='?%s=%s'>%s</a></td></tr>",
            CLASS_LARGER_TR, QUERY_PATH, path + '/' + relativePath, viewName);
      }
    }
    out.write("</table>");
    out.write("<p></p>");
  }

  // Searches the TreeNode whose absolute path matches the given path, started from root.
  // Returns null if such a TreeNode doesn't exist.
  @GuardedBy("monitor")
  private /*@Nullable*/ TreeNode findNode(/*@Nullable*/ String path) {
    if (Strings.isNullOrEmpty(path) || "/".equals(path)) { // Go back to the root directory.
      return root;
    } else {
      List<String> dirs = PATH_SPLITTER.splitToList(path);
      TreeNode node = root;
      for (int i = 0; i < dirs.size(); i++) {
        String dir = dirs.get(i);
        if ("".equals(dir) && i == 0) {
          continue; // Skip the first "", the path of root node.
        }
        if (!node.children.containsKey(dir)) {
          return null;
        } else {
          node = node.children.get(dir);
        }
      }
      return node;
    }
  }

  private static void emitDirectoryHeader(String path, PrintWriter out, Formatter formatter) {
    List<String> dirs = PATH_SPLITTER.splitToList(path);
    StringBuilder currentPath = new StringBuilder("");
    out.write("<h3>Current Path: ");
    for (int i = 0; i < dirs.size(); i++) {
      String dir = dirs.get(i);
      currentPath.append(dir);
      // create links to navigate back to parent directories.
      formatter.format("<a href='?%s=%s'>%s</a>", QUERY_PATH, currentPath.toString(), dir + '/');
      currentPath.append('/');
    }
    out.write("</h2>");
  }

  private static void emitViewData(
      /*@Nullable*/ ViewData viewData, View.Name viewName, PrintWriter out, Formatter formatter) {
    if (viewData == null) {
      formatter.format(
          "<p class=\"%s\">No Stats found for View %s.</p>", CLASS_LARGER_TR, viewName.asString());
      return;
    }
    View view = viewData.getView();
    emitViewInfo(view, viewData.getWindowData(), out, formatter);
    formatter.format(
        "<p class=\"%s\">Stats for View %s</p>", CLASS_LARGER_TR, view.getName().asString());
    out.write("<p></p>");
    formatter.format("<table %s frame=box cellspacing=0 cellpadding=2>", TABLE_BORDER);
    emitViewDataTableHeader(view, out, formatter);
    for (Entry<List</*@Nullable*/ TagValue>, AggregationData> entry :
        viewData.getAggregationMap().entrySet()) {
      emitViewDataRow(view, entry, out, formatter);
    }
    out.write("</table>");
    out.write("<p></p>");
  }

  private static void emitViewInfo(
      View view, AggregationWindowData windowData, PrintWriter out, Formatter formatter) {
    formatter.format("<table width=100%% %s>", TABLE_BORDER);
    emitViewInfoHeader(out, formatter);

    out.write("<tbody>");
    out.write("<tr>"); // One row that represents the selected view.
    formatter.format("<td>%s</td>", view.getName().asString());
    formatter.format("<td>%s</td>", view.getDescription());
    formatter.format("<td>%s</td>", view.getMeasure().getName());
    String aggregationType =
        view.getAggregation()
            .match(
                Functions.returnConstant("Sum"),
                Functions.returnConstant("Count"),
                Functions.returnConstant("Mean"),
                Functions.returnConstant("Distribution"),
                Functions.<String>throwAssertionError());
    formatter.format("<td>%s</td>", aggregationType);
    windowData.match(
        new Function<CumulativeData, Void>() {
          @Override
          public Void apply(CumulativeData arg) {
            formatter.format("<td>%s</td>", toDate(arg.getStart()));
            formatter.format("<td>%s</td>", toDate(arg.getEnd()));
            return null;
          }
        },
        Functions.</*@Nullable*/ Void>throwAssertionError(), // No interval views will be displayed.
        Functions.</*@Nullable*/ Void>throwAssertionError());
    out.write("</tr>");
    out.write("</tbody>");
    out.write("</table>");
    out.write("<p></p>");
  }

  private static Date toDate(Timestamp timestamp) {
    return Date.from(
        Instant.ofEpochMilli(
            timestamp.getSeconds() * MILLIS_PER_SECOND
                + timestamp.getNanos() / NANOS_PER_MILLISECOND));
  }

  private static void emitViewInfoHeader(PrintWriter out, Formatter formatter) {
    out.write("<thead>");
    out.write("<tr>");
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_VIEW);
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_DESCRIPTION);
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_MEASURE);
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_AGGREGATION);
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_START);
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_END);
    out.write("</tr>");
    out.write("</thead>");
  }

  private static void emitViewDataTableHeader(View view, PrintWriter out, Formatter formatter) {
    out.write("<thead>");
    out.write("<tr>");
    for (TagKey tagKey : view.getColumns()) {
      formatter.format("<th class=\"l1\">TagKey: %s (string)</th>", tagKey.getName());
    }
    String unit = view.getMeasure().getUnit();
    view.getAggregation()
        .match(
            new Function<Sum, Void>() {
              @Override
              public Void apply(Sum arg) {
                formatter.format("<th class=\"l1\">%s, %s</th>", TABLE_HEADER_SUM, unit);
                return null;
              }
            },
            new Function<Count, Void>() {
              @Override
              public Void apply(Count arg) {
                formatter.format("<th class=\"l1\">%s</th>", TABLE_HEADER_COUNT);
                return null;
              }
            },
            new Function<Mean, Void>() {
              @Override
              public Void apply(Mean arg) {
                formatter.format("<th class=\"l1\">%s, %s</th>", TABLE_HEADER_MEAN, unit);
                formatter.format("<th class=\"l1\">%s</th>", TABLE_HEADER_COUNT);
                return null;
              }
            },
            new Function<Distribution, Void>() {
              @Override
              public Void apply(Distribution arg) {
                formatter.format("<th class=\"l1\">%s, %s</th>", TABLE_HEADER_MEAN, unit);
                formatter.format("<th class=\"l1\">%s</th>", TABLE_HEADER_COUNT);
                formatter.format("<th class=\"l1\">%s, %s</th>", TABLE_HEADER_MAX, unit);
                formatter.format("<th class=\"l1\">%s, %s</th>", TABLE_HEADER_MIN, unit);
                formatter.format("<th class=\"l1\">%s</th>", TABLE_HEADER_DEV);
                formatter.format("<th class=\"l1\">%s</th>", TABLE_HEADER_HISTOGRAM);
                return null;
              }
            },
            Functions.</*@Nullable*/ Void>throwAssertionError());
    out.write("</tr>");
    out.write("</thead>");
  }

  private static void emitViewDataRow(
      View view,
      Entry<List</*@Nullable*/ TagValue>, AggregationData> entry,
      PrintWriter out,
      Formatter formatter) {
    out.write("<tr>");
    for (/*@Nullable*/ TagValue tagValue : entry.getKey()) {
      String tagValueStr = tagValue == null ? "" : tagValue.asString();
      formatter.format("<td %s>%s</td>", ALIGN_CENTER, tagValueStr);
    }
    entry
        .getValue()
        .match(
            new Function<SumDataDouble, Void>() {
              @Override
              public Void apply(SumDataDouble arg) {
                formatter.format("<td %s>%.3f</td>", ALIGN_CENTER, arg.getSum());
                return null;
              }
            },
            new Function<SumDataLong, Void>() {
              @Override
              public Void apply(SumDataLong arg) {
                formatter.format("<td %s>%d</td>", ALIGN_CENTER, arg.getSum());
                return null;
              }
            },
            new Function<CountData, Void>() {
              @Override
              public Void apply(CountData arg) {
                formatter.format("<td %s>%d</td>", ALIGN_CENTER, arg.getCount());
                return null;
              }
            },
            new Function<MeanData, Void>() {
              @Override
              public Void apply(MeanData arg) {
                formatter.format("<td %s>%.3f</td>", ALIGN_CENTER, arg.getMean());
                formatter.format("<td %s>%d</td>", ALIGN_CENTER, arg.getCount());
                return null;
              }
            },
            new Function<DistributionData, Void>() {
              @Override
              public Void apply(DistributionData arg) {
                checkArgument(
                    view.getAggregation() instanceof Distribution, "Distribution expected.");
                formatter.format("<td %s>%.3f</td>", ALIGN_CENTER, arg.getMean());
                formatter.format("<td %s>%d</td>", ALIGN_CENTER, arg.getCount());
                formatter.format("<td %s>%.3f</td>", ALIGN_CENTER, arg.getMax());
                formatter.format("<td %s>%.3f</td>", ALIGN_CENTER, arg.getMin());
                formatter.format("<td %s>%.3f</td>", ALIGN_CENTER, arg.getSumOfSquaredDeviations());
                emitHistogramBuckets(
                    ((Distribution) view.getAggregation()).getBucketBoundaries().getBoundaries(),
                    arg.getBucketCounts(),
                    out,
                    formatter);
                return null;
              }
            },
            Functions.</*@Nullable*/ Void>throwAssertionError());
    out.write("</tr>");
  }

  private static void emitHistogramBuckets(
      List<Double> bucketBoundaries,
      List<Long> bucketCounts,
      PrintWriter out,
      Formatter formatter) {
    checkArgument(
        bucketBoundaries.size() == bucketCounts.size() - 1,
        "Bucket boundaries and counts don't match");
    out.write("<td>");
    out.write("<table>");
    formatter.format(
        "<thead><tr><th>%s</th><th>%s</th></tr></thead>",
        TABLE_HEADER_RANGE, TABLE_HEADER_BUCKET_SIZE);
    out.write("<tbody>");
    for (int i = 0; i < bucketCounts.size(); i++) {
      double low = i == 0 ? Double.NEGATIVE_INFINITY : bucketBoundaries.get(i - 1);
      double high =
          i == bucketCounts.size() - 1 ? Double.POSITIVE_INFINITY : bucketBoundaries.get(i);
      out.write("<tr>");
      formatter.format("<td>[%.3f...%.3f)</td>", low, high);
      formatter.format("<td>%d</td>", bucketCounts.get(i));
      out.write("</tr>");
    }
    out.write("</tbody>");
    out.write("</table>");
    out.write("</td>");
  }

  private static void emitMeasureTable(
      Map<String, Measure> measures, PrintWriter out, Formatter formatter) {
    out.write("<h2>Measures</h2>");
    out.write("<p></p>");
    formatter.format("<table %s frame=box cellspacing=0 cellpadding=2>", TABLE_BORDER);
    emitMeasureTableHeader(out, formatter);
    out.write("<tbody>");
    for (Entry<String, Measure> entry : measures.entrySet()) {
      emitMeasureTableRow(entry.getValue(), out, formatter);
    }
    out.write("</tbody>");
    out.write("</table>");
    out.write("<p></p>");
  }

  private static void emitMeasureTableHeader(PrintWriter out, Formatter formatter) {
    out.write("<thead>");
    out.write("<tr>");
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_MEASURE);
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_DESCRIPTION);
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_UNIT);
    formatter.format("<th colspan=1 class=\"l1\">%s</th>", TABLE_HEADER_MEASURE_TYPE);
    out.write("</tr>");
    out.write("</thead>");
  }

  private static void emitMeasureTableRow(Measure measure, PrintWriter out, Formatter formatter) {
    out.write("<tr>");
    formatter.format("<td><b>%s</b></td>", measure.getName());
    formatter.format("<td align=\"left\">%s&nbsp;</td>", measure.getDescription());
    formatter.format("<td align=\"left\">%s&nbsp;</td>", measure.getUnit());
    String measureType =
        measure.match(
            Functions.returnConstant("Double"),
            Functions.returnConstant("Long"),
            Functions.throwAssertionError());
    formatter.format("<td align=\"left\">%s&nbsp;</td>", measureType);
    out.write("</tr>");
  }

  static StatszZPageHandler create(ViewManager viewManager) {
    return new StatszZPageHandler(viewManager);
  }

  private StatszZPageHandler(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  /*
   * TreeNode for storing the structure of views and directories that they're in. Think of this as
   * file descriptors for view: non-leaf nodes are directories which may contain views or other
   * directories, and leaf nodes are the ones with actual information on views. Each non-leaf node
   * also has the number of views under its directory.
   */
  private static class TreeNode {
    // Only leaf nodes have views.
    @javax.annotation.Nullable final View view;

    // A mapping from relative path to children TreeNodes. Sorted by the relative path.
    SortedMap<String, TreeNode> children = Maps.newTreeMap();

    // The number of views that a directory contains. 0 for leaf node.
    int views = 0;

    TreeNode() {
      this.view = null;
    }

    TreeNode(View view) {
      this.view = checkNotNull(view, "view");
    }
  }
}
