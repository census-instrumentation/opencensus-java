/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.census.examples.gol;

import com.google.monitoring.runtime.MonitoringHook;
import com.google.monitoring.runtime.RequestHandlerAdapter;

import java.io.PrintWriter;
import java.util.Map;

/**
 * Censusz handler. Displays Census internal state.
 *
 * @author dpo@google.com (Dino Oliva)
 */
public class CensusClientz implements MonitoringHook {
  private final String defaultHost;
  private final int defaultPort;
  private final int gensPerRpc;

  public CensusClientz(String host, int defaultPort, int gensPerRpc) {
    this.defaultHost = host;
    this.defaultPort = defaultPort;
    this.gensPerRpc = gensPerRpc;
  }

  @Override
  public String getLinkName() {
    return "censusclientz";
  }

  @Override
  public String getLinkHtml(String linkUrl) {
    return "Show <a href=\"" + linkUrl + "\">censusclientz</a>.";
  }

  @Override
  public void handleRequest(RequestHandlerAdapter handler) throws Exception {
    // set params based on input
    Map<String, String[]> params = handler.getParameterMap();
    String host = getParamValueOrDefault(params, "server", defaultHost);
    int port = getIntOrDefault(getParamValue(params, "port"), defaultPort);
    int numRpcs = getIntOrDefault(getParamValue(params, "rpcs"), 0);
    CensusClient client = new CensusClient(host, port);
    try {
      // execute rpcs
      new CensusApplication(client, gensPerRpc, numRpcs).execute();
      // display result
      handler.setContentType("text/html");
      PrintWriter pw = handler.getPrintWriter();
      writePage(pw, host, port, numRpcs);
      return;
    } finally {
      client.shutdown();
    }
  }

  private void writePage(PrintWriter pw, String server, int port, int rpcs) {
    pw.println("<html>");
    pw.println("<head>");
    pw.println("<title>CensusClientz</title>");
    pw.println("<style>");
    pw.println("label { display: inline-block; width: 5em; }");
    pw.println("td { font-weight:normal; text-align:center; color:#38761d; }");
    pw.println("th { font-weight:normal; text-align:center; color:#23238e; }");
    pw.println("</style>");
    pw.println("</head>");
    pw.println("<body bgcolor=#ffffff>");
    pw.println("<h1>Census Client</h1>");
    pw.println("<form method=\"get\" action=\"censusclientz\">");

    pw.println("<fieldset>");
    pw.println("<legend>Census Client</legend>");

    pw.println("<p>");
    pw.println("<label for=\"server\">Server:</label>");
    pw.println("<input type=\"text\" name=\"server\" id=\"server\"value=\"" +  server + "\"/>");

    pw.println("<p>");
    pw.println("<label for=\"port\">Port:</label>");
    pw.println("<input type=\"text\" name=\"port\" id=\"port\"value=\"" +  port + "\"/>");

    pw.println("<p>");
    pw.println("<label for=\"rpcs\">RPCs:</label>");
    pw.println("<select name=\"rpcs\">");
    pw.println("<option " + (rpcs == 16 ? "selected " : "") + "value=16>16</option>");
    pw.println("<option " + (rpcs == 128 ? "selected " : "") + "value=128>128</option>");
    pw.println("<option " + (rpcs == 256 ? "selected " : "") + "value=256>256</option>");
    pw.println("</select>");
    pw.println("</fieldset>");

    pw.println("<p><input type=\"submit\"/></p>");

    for (CensusApplication.GolSpec gol : CensusApplication.gols) {
      pw.println("<p>");
      pw.println("<fieldset>");
      pw.println("<legend>RPC " + getTitle(gol.dim, gensPerRpc) + "</legend>");
      pw.println("<ul>");
      pw.println("<li><strong>Tag:</strong> "
          + CensusApplication.CLIENT_KEY + ":x"
          + CensusApplication.getTagValue(gol.dim, gensPerRpc));
      pw.println("<li><strong>Dimensions:</strong> " + gol.dim + "x" + gol.dim);
      pw.println("<li><strong>Generations per RPC:</strong> " + gensPerRpc);
      pw.println("<li><strong>Result:</strong> ");
      pw.println(formatResultAsTable(gol.initGen, gol.currentGen, gol.dim, gol.gens));
      pw.println("</ul>");
      pw.println("</fieldset>");
    }
    pw.println("</form>");
    pw.println("</body>");
    pw.println("</html>");
  }

  private static String getTitle(int dim, int gensPerRpc) {
    return dim + "x" + dim + " Board/" + gensPerRpc + " Generations Per RPC";
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
          retval.append("&nbsp");
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
          retval.append("&nbsp");
        }
        retval.append("</th>");
        ++currIndex;
      }
      retval.append("</tr>");
    }
    retval.append("</table>");
    return retval.toString();
  }

  private static String getParamValue(Map<String, String[]> params, String name) {
    String[] values = params.get(name);
    if (values == null || values.length == 0) {
      return null;
    }
    return values[values.length - 1];
  }

  private static String getParamValueOrDefault(
      Map<String, String[]> params, String name, String defaultValue) {
    String val = getParamValue(params, name);
    return (val == null) ? defaultValue : val;
  }

  private static int getIntOrDefault(String s, int dflt) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException exn) {
      return dflt;
    }
  }
}
