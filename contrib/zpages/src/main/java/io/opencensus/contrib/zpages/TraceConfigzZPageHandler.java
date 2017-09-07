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
/**
 * HTML page formatter for tracing config. The page displays information the current active
 * tracing configuration and allows users to change it.
 */
final class TraceConfigzZPageHandler extends ZPageHandler {
  private static final String TRACE_CONFIGZ_URL = "/traceconfigz";
  private final TraceConfig traceConfig;

  private static final String CHANGE = "change";
  private static final String PERMANENT_CHANGE = "permanently";
  private static final String CHANGE_ON_INPUT_PERMANENT =
      "oninput=\"document.getElementById('" + PERMANENT_CHANGE + "').checked = true\"";
  private static final String QUERY_COMPONENT_SAMPLING_PROBABILITY = "samplingprobability";
  private static final String QUERY_COMPONENT_MAX_NUMBER_OF_ATTRIBUTES = "maxnumberofattributes";
  private static final String QUERY_COMPONENT_MAX_NUMBER_OF_ANNOTATIONS = "maxnumberofannotations";
  private static final String QUERY_COMPONENT_MAX_NUMBER_OF_NETWORK_EVENTS =
      "maxnumberofnetworkevents";
  private static final String QUERY_COMPONENT_MAX_NUMBER_OF_LINKS = "maxnumberoflinks";

  // TODO(bdrutu): Use post.
  // TODO(bdrutu): Refactor this to not use a big "printf".
  private static final String TRACECONFIGZ_FORM_BODY =
      "<form action=/traceconfigz method=get>\n"
          +
          // Permanently change.
          "<p>\n"
          + "<input type=radio name=" + CHANGE + " value=%s id=%s> \n"
          + "Permanently change:"
          + "<br> SamplingProbability to <input type=text size=10 name=%s value=\"%f\" %s>\n"
          + "<br> MaxNumberOfAttributes to <input type=text size=10 name=%s value=\"%d\" %s>\n"
          + "<br> MaxNumberOfAnnotations to <input type=text size=10 name=%s value=\"%d\" %s> \n"
          + "<br> MaxNumberOfNetworkEvents to <input type=text size=10 name=%s value=\"%d\" %s> \n"
          + "<br> MaxNumberOfLinks to <input type=text size=10 name=%s value=\"%d\" %s> \n"
          +
          // Submit button.
          "<p>\n"
          + "<input type=submit value=Start>\n"
          + "</form>";

  static TraceConfigzZPageHandler create(TraceConfig traceConfig) {
    return new TraceConfigzZPageHandler(traceConfig);
  }

  @Override
  public String getUrlPath() {
    return TRACE_CONFIGZ_URL;
  }

  @Override
  public void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    PrintWriter out =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8)));
    out.write("<!DOCTYPE html>\n");
    out.write("<html lang=\"en\"><head>\n");
    out.write("<meta charset=\"utf-8\">\n");
    out.write("<title>TraceConfigZ</title>\n");
    out.write("<link rel=\"shortcut icon\" href=\"//www.opencensus.io/favicon.ico\"/>\n");
    out.write("</head>\n");
    out.write("<body>\n");
    if (!queryMap.isEmpty()) {
      maybeApplyChanges(queryMap);
    }
    TraceParams currentParams = traceConfig.getActiveTraceParams();
    out.printf(
        TRACECONFIGZ_FORM_BODY,
        PERMANENT_CHANGE,
        PERMANENT_CHANGE,
        QUERY_COMPONENT_SAMPLING_PROBABILITY,
        0.0001,
        CHANGE_ON_INPUT_PERMANENT,
        QUERY_COMPONENT_MAX_NUMBER_OF_ATTRIBUTES,
        currentParams.getMaxNumberOfAttributes(),
        CHANGE_ON_INPUT_PERMANENT,
        QUERY_COMPONENT_MAX_NUMBER_OF_ANNOTATIONS,
        currentParams.getMaxNumberOfAnnotations(),
        CHANGE_ON_INPUT_PERMANENT,
        QUERY_COMPONENT_MAX_NUMBER_OF_NETWORK_EVENTS,
        currentParams.getMaxNumberOfNetworkEvents(),
        CHANGE_ON_INPUT_PERMANENT,
        QUERY_COMPONENT_MAX_NUMBER_OF_LINKS,
        currentParams.getMaxNumberOfLinks(),
        CHANGE_ON_INPUT_PERMANENT);
    emitTraceParamsTable(currentParams, out);
    out.write("</body>\n");
    out.write("</html>\n");
    out.close();
  }

  // If this is a supported change (currently only permanent changes are supported) apply it.
  private void maybeApplyChanges(Map<String, String> queryMap) {
    if (queryMap.containsKey(CHANGE) && queryMap.get(CHANGE).equals(PERMANENT_CHANGE)) {
      TraceParams.Builder traceParamsBuilder = traceConfig.getActiveTraceParams().toBuilder();
      if (queryMap.containsKey(QUERY_COMPONENT_SAMPLING_PROBABILITY)) {
        double samplingProbability = Double.parseDouble(queryMap.get
            (QUERY_COMPONENT_SAMPLING_PROBABILITY));
        traceParamsBuilder.setSampler(Samplers.probabilitySampler(samplingProbability));
      }
      if (queryMap.containsKey(QUERY_COMPONENT_MAX_NUMBER_OF_ATTRIBUTES)) {
        int maxNumberOfAttributes = Integer.parseInt(queryMap.get
            (QUERY_COMPONENT_MAX_NUMBER_OF_ATTRIBUTES));
        traceParamsBuilder.setMaxNumberOfAttributes(maxNumberOfAttributes);
      }
      if (queryMap.containsKey(QUERY_COMPONENT_MAX_NUMBER_OF_ANNOTATIONS)) {
        int maxNumberOfAnnotations = Integer.parseInt(queryMap.get
            (QUERY_COMPONENT_MAX_NUMBER_OF_ANNOTATIONS));
        traceParamsBuilder.setMaxNumberOfAnnotations(maxNumberOfAnnotations);
      }
      if (queryMap.containsKey(QUERY_COMPONENT_MAX_NUMBER_OF_NETWORK_EVENTS)) {
        int maxNumberOfNetworkEvents = Integer.parseInt(queryMap.get
            (QUERY_COMPONENT_MAX_NUMBER_OF_NETWORK_EVENTS));
        traceParamsBuilder.setMaxNumberOfNetworkEvents(maxNumberOfNetworkEvents);
      }
      if (queryMap.containsKey(QUERY_COMPONENT_MAX_NUMBER_OF_LINKS)) {
        int maxNumberOfLinks = Integer.parseInt(queryMap.get
            (QUERY_COMPONENT_MAX_NUMBER_OF_LINKS));
        traceParamsBuilder.setMaxNumberOfLinks(maxNumberOfLinks);
      }
      traceConfig.updateActiveTraceParams(traceParamsBuilder.build());
    }
  }

  // Prints a table to a PrintWriter that shows existing trace parameters.
  private static void emitTraceParamsTable(TraceParams params, PrintWriter out) {
    out.write(
        "<p>\n"
            + "<b>Active tracing parameters:</b><br>\n"
            + "<blockquote>\n"
            + "<table rules=\"all\" frame=\"border\">\n"
            + "  <tr style=\"background: #eee\">\n"
            + "    <td>Name</td>\n"
            + "    <td>Value</td>\n"
            + "  </tr>\n");
    out.printf(
        "  <tr>\n    <td>Sampler</td>\n    <td>%s</td>\n  </tr>\n", params.getSampler().toString());
    out.printf(
        "  <tr>\n    <td>MaxNumberOfAttributes</td>\n    <td>%d</td>\n  </tr>\n",
        params.getMaxNumberOfAttributes());
    out.printf(
        "  <tr>\n    <td>MaxNumberOfAnnotations</td>\n    <td>%d</td>\n  </tr>\n",
        params.getMaxNumberOfAnnotations());
    out.printf(
        "  <tr>\n    <td>MaxNumberOfNetworkEvents</td>\n    <td>%d</td>\n  </tr>\n",
        params.getMaxNumberOfNetworkEvents());
    out.printf(
        "  <tr>\n    <td>MaxNumberOfLinks</td>\n    <td>%d</td>\n  </tr>\n",
        params.getMaxNumberOfLinks());

    out.write("</table>\n" + "</blockquote>\n");
  }

  private TraceConfigzZPageHandler(TraceConfig traceConfig) {
    this.traceConfig = traceConfig;
  }
}
