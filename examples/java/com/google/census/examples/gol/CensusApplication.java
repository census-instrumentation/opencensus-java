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

import com.google.census.CensusScope;
import com.google.census.TagKey;
import com.google.census.TagMap;

// Invokes the given CensusClient for all of the Game-of-Life specs with the appropriate
// Census client tag in scope.
class CensusApplication {
  static final TagKey CLIENT_KEY = new TagKey("census_gol_client");

  final CensusClient client;
  final int gensPerRpc;
  final int numRpcs;

  CensusApplication(CensusClient client, int gensPerRpc, int numRpcs) {
    this.client = client;
    this.gensPerRpc = gensPerRpc;
    this.numRpcs = numRpcs;
  }

  void execute() {
    for (GolSpec gol : gols) {
      TagMap tags = TagMap.of(CLIENT_KEY, getTagValue(gol.dim, gensPerRpc));
      String request = getRequest(gol.currentGen, gensPerRpc);
      try (CensusScope scope = new CensusScope(tags)) {
        for (int i = 0; i < numRpcs; ++i) {
          String result = client.executeCommand(request);
          String[] results = result.split("; ");
          if (results.length < 1) {
            break;
          }
          gol.currentGen = results[0];
          gol.gens += gensPerRpc;
        }
      }
    }
  }

  // Generates tag value as <dimension>x<dimension>-<generations>.
  static String getTagValue(int dim, int gensPerRpc) {
    return dim + "x" + dim + "-" + gensPerRpc;
  }

  // Encodes a request for the given generation and number of generations to calculate.
  static String getRequest(String gen, int gensPerRpc) {
    return "gol " + gensPerRpc + " " + gen;
  }

  // Holds the specification of an RPC for the game of life server.
  static class GolSpec {
    final int dim;
    final String initGen;
    String currentGen;
    long gens;

    GolSpec(String initGen) {
      this.initGen = initGen;
      this.dim = (int) Math.sqrt(initGen.length());
      this.currentGen = initGen;
      this.gens = 0;
    }
  }

  static final GolSpec[] gols = new GolSpec[] {

    new GolSpec(""
        + "00000000"
        + "00111000"
        + "00000000"
        + "00000000"
        + "00010000"
        + "00010000"
        + "00010000"
        + "00000000"),

    new GolSpec(""
        + "0000000000000000"
        + "0011100000111000"
        + "0000000000000000"
        + "0000000000000000"
        + "0001000000010000"
        + "0001000000010000"
        + "0001000000010000"
        + "0000000000000000"
        + "0000000000000000"
        + "0011100000111000"
        + "0000000000000000"
        + "0000000000000000"
        + "0001000000010000"
        + "0001000000010000"
        + "0001000000010000"
        + "0000000000000000"),

    new GolSpec(""
        + "00000000000000000000000000000000"
        + "00111000001110000011100000111000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00010000000100000001000000010000"
        + "00010000000100000001000000010000"
        + "00010000000100000001000000010000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00111000001110000011100000111000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00010000000100000001000000010000"
        + "00010000000100000001000000010000"
        + "00010000000100000001000000010000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00111000001110000011100000111000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00010000000100000001000000010000"
        + "00010000000100000001000000010000"
        + "00010000000100000001000000010000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00111000001110000011100000111000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00010000000100000001000000010000"
        + "00010000000100000001000000010000"
        + "00010000000100000001000000010000"
        + "00000000000000000000000000000000"),
  };
}
