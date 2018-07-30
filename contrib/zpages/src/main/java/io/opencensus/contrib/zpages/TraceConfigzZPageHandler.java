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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Charsets;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

// TODO(bdrutu): Add tests.
// TODO(hailongwen): Remove the usage of `NetworkEvent` in the future.
/**
 * HTML page formatter for tracing config. The page displays information about the current active
 * tracing configuration and allows users to change it.
 */
final class TraceConfigzZPageHandler extends ZPageHandler {
  private static final String TRACE_CONFIGZ_URL = "/traceconfigz";
  private final TraceConfig traceConfig;

  private static final String CHANGE = "change";
  private static final String PERMANENT_CHANGE = "permanently";
  private static final String RESTORE_DEFAULT_CHANGE = "restore_default";
  private static final String QUERY_COMPONENT_SAMPLING_PROBABILITY = "samplingprobability";
  private static final String QUERY_COMPONENT_MAX_NUMBER_OF_ATTRIBUTES = "maxnumberofattributes";
  private static final String QUERY_COMPONENT_MAX_NUMBER_OF_ANNOTATIONS = "maxnumberofannotations";
  private static final String QUERY_COMPONENT_MAX_NUMBER_OF_NETWORK_EVENTS =
      "maxnumberofnetworkevents";
  private static final String QUERY_COMPONENT_MAX_NUMBER_OF_LINKS = "maxnumberoflinks";

  // TODO(bdrutu): Use post.
  // TODO(bdrutu): Refactor this to not use a big "printf".
  private static final String TRACECONFIGZ_FORM_BODY =
      "<form action=/traceconfigz method=get>%n"
          // Permanently changes table.
          + "<table class=\"small\" rules=\"all\">%n"
          + "<td colspan=\"3\" class=\"col_head\">Permanently change "
          + "<input type=\"hidden\" name=\"%s\" value=\"%s\"></td>%n"
          + "<tr><td>SamplingProbability to</td> "
          + "<td><input type=text size=15 name=%s value=\"\"></td> <td>(%s)</td>%n"
          + "<tr><td>MaxNumberOfAttributes to</td> "
          + "<td><input type=text size=15 name=%s value=\"\"></td> <td>(%d)</td>%n"
          + "<tr><td>MaxNumberOfAnnotations to</td>"
          + "<td><input type=text size=15 name=%s value=\"\"></td> <td>(%d)</td>%n"
          + "<tr><td>MaxNumberOfNetworkEvents to</td> "
          + "<td><input type=text size=15 name=%s value=\"\"></td> <td>(%d)</td>%n"
          + "<tr><td>MaxNumberOfLinks to</td>"
          + "<td><input type=text size=15 name=%s value=\"\"></td> <td>(%d)</td>%n"
          + "</table>%n"
          // Submit button.
          + "<input class=\"button\" type=submit value=Submit>%n"
          + "</form>";

  private static final String RESTORE_DEFAULT_FORM_BODY =
      "<form action=/traceconfigz method=get>%n"
          // Restore to default.
          + "<b class=\"title\">Restore default</b> %n"
          + "<input type=\"hidden\" name=\"%s\" value=\"%s\"></td>%n"
          + "</br>%n"
          // Reset button.
          + "<input class=\"button\" type=submit value=Reset>%n"
          + "</form>";

  static TraceConfigzZPageHandler create(TraceConfig traceConfig) {
    return new TraceConfigzZPageHandler(traceConfig);
  }

  @Override
  public String getUrlPath() {
    return TRACE_CONFIGZ_URL;
  }

  private static void emitStyle(PrintWriter out) {
    out.write("<style>\n");
    out.write(Style.STYLE);
    out.write("</style>\n");
  }

  @Override
  @SuppressWarnings("deprecation")
  public void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    PrintWriter out =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8)));
    out.write("<!DOCTYPE html>\n");
    out.write("<html lang=\"en\"><head>\n");
    out.write("<meta charset=\"utf-8\">\n");
    out.write("<title>TraceConfigZ</title>\n");
    out.write("<link rel=\"shortcut icon\" href=\"https://opencensus.io/images/favicon.ico\"/>\n");
    out.write(
        "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300\""
            + "rel=\"stylesheet\">\n");
    out.write(
        "<link href=\"https://fonts.googleapis.com/css?family=Roboto\"" 
            + "rel=\"stylesheet\">\n");
    emitStyle(out);
    out.write("</head>\n");
    out.write("<body>\n");
    out.write(
        "<p class=\"header\">"
      + "<img class=\"oc\" src=\"https://opencensus.io/img/logo-sm.svg\" />"
      + "Open<span>Census</span></p>");
    out.write("<h1 class=\"left\">Trace Configuration</h1>");
    out.write("<p></p>");
    try {
      // Work that can throw exceptions.
      maybeApplyChanges(queryMap);
    } finally {
      // TODO(bdrutu): Maybe display to the page if an exception happened.
      // Display the page in any case.
      out.printf(
          TRACECONFIGZ_FORM_BODY,
          CHANGE,
          PERMANENT_CHANGE,
          QUERY_COMPONENT_SAMPLING_PROBABILITY,
          "0.0001", // TODO(bdrutu): Get this from the default sampler (if possible).
          QUERY_COMPONENT_MAX_NUMBER_OF_ATTRIBUTES,
          TraceParams.DEFAULT.getMaxNumberOfAttributes(),
          QUERY_COMPONENT_MAX_NUMBER_OF_ANNOTATIONS,
          TraceParams.DEFAULT.getMaxNumberOfAnnotations(),
          QUERY_COMPONENT_MAX_NUMBER_OF_NETWORK_EVENTS,
          TraceParams.DEFAULT.getMaxNumberOfNetworkEvents(),
          QUERY_COMPONENT_MAX_NUMBER_OF_LINKS,
          TraceParams.DEFAULT.getMaxNumberOfLinks());
      out.write("<br>\n");
      out.printf(RESTORE_DEFAULT_FORM_BODY, CHANGE, RESTORE_DEFAULT_CHANGE);
      out.write("<br>\n");
      emitTraceParamsTable(traceConfig.getActiveTraceParams(), out);
      out.write("</body>\n");
      out.write("</html>\n");
      out.close();
    }
  }

  // If this is a supported change (currently only permanent changes are supported) apply it.
  @SuppressWarnings("deprecation")
  private void maybeApplyChanges(Map<String, String> queryMap) {
    String changeStr = queryMap.get(CHANGE);
    if (PERMANENT_CHANGE.equals(changeStr)) {
      TraceParams.Builder traceParamsBuilder = traceConfig.getActiveTraceParams().toBuilder();
      String samplingProbabilityStr = queryMap.get(QUERY_COMPONENT_SAMPLING_PROBABILITY);
      if (!isNullOrEmpty(samplingProbabilityStr)) {
        double samplingProbability = Double.parseDouble(samplingProbabilityStr);
        traceParamsBuilder.setSampler(Samplers.probabilitySampler(samplingProbability));
      }
      String maxNumberOfAttributesStr = queryMap.get(QUERY_COMPONENT_MAX_NUMBER_OF_ATTRIBUTES);
      if (!isNullOrEmpty(maxNumberOfAttributesStr)) {
        int maxNumberOfAttributes = Integer.parseInt(maxNumberOfAttributesStr);
        traceParamsBuilder.setMaxNumberOfAttributes(maxNumberOfAttributes);
      }
      String maxNumberOfAnnotationsStr = queryMap.get(QUERY_COMPONENT_MAX_NUMBER_OF_ANNOTATIONS);
      if (!isNullOrEmpty(maxNumberOfAnnotationsStr)) {
        int maxNumberOfAnnotations = Integer.parseInt(maxNumberOfAnnotationsStr);
        traceParamsBuilder.setMaxNumberOfAnnotations(maxNumberOfAnnotations);
      }
      String maxNumberOfNetworkEventsStr =
          queryMap.get(QUERY_COMPONENT_MAX_NUMBER_OF_NETWORK_EVENTS);
      if (!isNullOrEmpty(maxNumberOfNetworkEventsStr)) {
        int maxNumberOfNetworkEvents = Integer.parseInt(maxNumberOfNetworkEventsStr);
        traceParamsBuilder.setMaxNumberOfNetworkEvents(maxNumberOfNetworkEvents);
      }
      String maxNumverOfLinksStr = queryMap.get(QUERY_COMPONENT_MAX_NUMBER_OF_LINKS);
      if (!isNullOrEmpty(maxNumverOfLinksStr)) {
        int maxNumberOfLinks = Integer.parseInt(maxNumverOfLinksStr);
        traceParamsBuilder.setMaxNumberOfLinks(maxNumberOfLinks);
      }
      traceConfig.updateActiveTraceParams(traceParamsBuilder.build());
    } else if (RESTORE_DEFAULT_CHANGE.equals(changeStr)) {
      traceConfig.updateActiveTraceParams(TraceParams.DEFAULT);
    }
  }

  // Prints a table to a PrintWriter that shows existing trace parameters.
  @SuppressWarnings("deprecation")
  private static void emitTraceParamsTable(TraceParams params, PrintWriter out) {
    out.write(
        "<b class=\"title\">Active tracing parameters:</b><br>\n"
            + "<table class=\"small\" rules=\"all\">\n"
            + "  <tr>\n"
            + "    <td class=\"col_headR\">Name</td>\n"
            + "    <td class=\"col_head\">Value</td>\n"
            + "  </tr>\n");
    out.printf(
        "  <tr>%n    <td>Sampler</td>%n    <td>%s</td>%n  </tr>%n",
        params.getSampler().getDescription());
    out.printf(
        "  <tr>%n    <td>MaxNumberOfAttributes</td>%n    <td>%d</td>%n  </tr>%n",
        params.getMaxNumberOfAttributes());
    out.printf(
        "  <tr>%n    <td>MaxNumberOfAnnotations</td>%n    <td>%d</td>%n  </tr>%n",
        params.getMaxNumberOfAnnotations());
    out.printf(
        "  <tr>%n    <td>MaxNumberOfNetworkEvents</td>%n    <td>%d</td>%n  </tr>%n",
        params.getMaxNumberOfNetworkEvents());
    out.printf(
        "  <tr>%n    <td>MaxNumberOfLinks</td>%n    <td>%d</td>%n  </tr>%n",
        params.getMaxNumberOfLinks());

    out.write("</table>\n");
  }

  private TraceConfigzZPageHandler(TraceConfig traceConfig) {
    this.traceConfig = traceConfig;
  }
}
