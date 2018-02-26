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

package io.opencensus.example.gameoflife;

import static io.opencensus.example.gameoflife.GameOfLifeApplication.CLIENT_TAG_KEY;

import com.google.common.base.Charsets;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.opencensus.example.gameoflife.GameOfLifeApplication.GolSpec;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Clientz handler. Displays the game board for game of life.
 */
// Cannot extend ZPageHandler since it's package private.
final class ClientzHandler implements HttpHandler {
  private final String defaultHost;
  private final int defaultPort;
  private final int gensPerGol;

  ClientzHandler(String host, int defaultPort, int gensPerGol) {
    this.defaultHost = host;
    this.defaultPort = defaultPort;
    this.gensPerGol = gensPerGol;
  }

  @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    try {
      httpExchange.sendResponseHeaders(200, 0);
      emitHtml(uriQueryToMap(httpExchange.getRequestURI()), httpExchange.getResponseBody());
    } finally {
      httpExchange.close();
    }
  }

  private void emitHtml(Map<String, String> queryMap, OutputStream outputStream) {
    String host = getParamValueOrDefault(queryMap, "server", defaultHost);
    int port = getIntOrDefault(queryMap.get("port"), defaultPort);
    int numGols = getIntOrDefault(queryMap.get("rpcs"), 0);
    GameOfLifeClient client = new GameOfLifeClient(host, port);
    try {
      new GameOfLifeApplication(client, gensPerGol, numGols).execute();
      // display result
      PrintWriter printWriter =
          new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, Charsets.UTF_8)));
      try {
        writePage(printWriter, host, port, numGols);
      } finally {
        printWriter.close();
      }
    } finally {
      client.shutdown();
    }
  }

  private static Map<String, String> uriQueryToMap(URI uri) {
    String query = uri.getQuery();
    if (query == null) {
      return Collections.emptyMap();
    }
    Map<String, String> result = new HashMap<String, String>();
    for (String param : query.split("&")) {
      String[] pair = param.split("=");
      if (pair.length > 1) {
        result.put(pair[0], pair[1]);
      } else {
        result.put(pair[0], "");
      }
    }
    return result;
  }

  private void writePage(PrintWriter pw, String server, int port, int rpcs) {
    pw.println("<html>");
    pw.println("<head>");
    pw.println("<title>Clientz</title>");
    pw.println("<style>");
    pw.println("label { display: inline-block; width: 5em; }");
    pw.println("td { font-weight:normal; text-align:center; color:#38761d; }");
    pw.println("th { font-weight:normal; text-align:center; color:#23238e; }");
    pw.println("</style>");
    pw.println("</head>");
    pw.println("<body bgcolor=#ffffff>");
    pw.println("<h1>Client</h1>");
    pw.println("<form method=\"get\" action=\"clientz\">");

    pw.println("<fieldset>");
    pw.println("<legend>Client</legend>");

    pw.println("<p>");
    pw.println("<label for=\"server\">Server:</label>");
    pw.println("<input type=\"text\" name=\"server\" id=\"server\"value=\"" +  server + "\"/>");

    pw.println("<p>");
    pw.println("<label for=\"port\">Port:</label>");
    pw.println("<input type=\"text\" name=\"port\" id=\"port\"value=\"" +  port + "\"/>");

    pw.println("<p>");
    pw.println("<label for=\"rpcs\">RPCs:</label>");
    pw.println("<select name=\"rpcs\">");
    pw.println("<option " + (rpcs == 4 ? "selected " : "") + "value=4>4</option>");
    pw.println("<option " + (rpcs == 16 ? "selected " : "") + "value=16>16</option>");
    pw.println("<option " + (rpcs == 128 ? "selected " : "") + "value=128>128</option>");
    pw.println("<option " + (rpcs == 256 ? "selected " : "") + "value=256>256</option>");
    pw.println("</select>");
    pw.println("</fieldset>");

    pw.println("<p><input type=\"submit\"/></p>");

    for (GolSpec gol : GameOfLifeApplication.gols) {
      pw.println("<p>");
      pw.println("<fieldset>");
      pw.println("<legend>RPC " + getTitle(gol.dim, gensPerGol) + "</legend>");
      pw.println("<ul>");
      pw.println("<li><strong>Tag:</strong> {"
          + CLIENT_TAG_KEY.getName()
          + " : "
          + GolUtils.getTagValue(gol.dim, gensPerGol, "client").asString()
          + '}');
      pw.println("<li><strong>Dimensions:</strong> " + gol.dim + "x" + gol.dim);
      pw.println("<li><strong>Generations per RPC:</strong> " + gensPerGol);
      pw.println("<li><strong>Result:</strong> ");
      pw.println(formatResultAsTable(gol.initGen, gol.currentGen, gol.dim, gol.gens));
      pw.println("</ul>");
      pw.println("</fieldset>");
    }
    pw.println("</form>");
    pw.println("</body>");
    pw.println("</html>");
  }

  private static String getTitle(int dim, int gensPerGol) {
    return dim + "x" + dim + " Board/" + gensPerGol + " Generations Per RPC";
  }

  private static String formatResultAsTable(String initGen, String currGen, int dim, long gens) {
    StringBuilder retval = new StringBuilder();
    retval.append("<table>");
    int initIndex = 0;
    int currIndex = 0;
    retval.append("<tr>");
    retval.append("<td style=\"font-weight:bold;\" colspan=\"")
        .append(dim + 1).append("\">InitGen</td>");
    retval.append("<th style=\"font-weight:bold;\" colspan=\"")
        .append(dim).append("\">Gen ").append(gens).append("</th>");
    retval.append("</tr>");
    for (int row = 0; row < dim; ++row) {
      retval.append("<tr>");
      for (int col = 0; col < dim; ++col) {
        retval.append("<td>");
        if (initGen.charAt(initIndex) == '1') {
          retval.append("*");
        } else {
          retval.append(".");
        }
        retval.append("</td>");
        ++initIndex;
      }
      retval.append("<td> &nbsp </td>");
      for (int col = 0; col < dim; ++col) {
        retval.append("<th>");
        if (currGen.charAt(currIndex) == '1') {
          retval.append("*");
        } else {
          retval.append(".");
        }
        retval.append("</th>");
        ++currIndex;
      }
      retval.append("</tr>");
    }
    retval.append("</table>");
    return retval.toString();
  }

  private static String getParamValueOrDefault(
      Map<String, String> queryMap, String name, String defaultValue) {
    return queryMap.containsKey(name) ? queryMap.get(name) : defaultValue;
  }

  private static int getIntOrDefault(String s, int dflt) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException exn) {
      return dflt;
    }
  }
}
