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

import io.opencensus.common.Scope;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;

// Invokes the given GameOfLifeClient for all of the Game-of-Life specs with the appropriate
// client tag in scope.
final class GameOfLifeApplication {

  private static final Tagger tagger = Tags.getTagger();

  static final TagKey CLIENT_TAG_KEY = TagKey.create("client");
  static final TagKey METHOD = TagKey.create("method");
  static final TagValue METHOD_NAME =
      TagValue.create(CommandProcessorGrpc.getExecuteMethod().getFullMethodName());
  static final TagKey CALLER = TagKey.create("caller");
  static final TagKey ORIGINATOR = TagKey.create("originator");
  static final TagValue USERNAME = TagValue.create(System.getProperty("user.name"));

  static final GolSpec[] gols =
      new GolSpec[] {
        new GolSpec(
            ""
                + "00000000"
                + "00111000"
                + "00000000"
                + "00000000"
                + "00010000"
                + "00010000"
                + "00010000"
                + "00000000"),
        new GolSpec(
            ""
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
        new GolSpec(
            ""
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
                + "00000000000000000000000000000000")
      };

  final GameOfLifeClient client;
  final int gensPerGol;
  final int numGols;

  GameOfLifeApplication(GameOfLifeClient client, int gensPerGol, int numGols) {
    this.client = client;
    this.gensPerGol = gensPerGol;
    this.numGols = numGols;
  }

  void execute() {
    for (GolSpec gol : gols) {
      String request = GolUtils.getRequest(gol.currentGen, gensPerGol);
      TagContext ctx =
          tagger
              .currentBuilder()
              .put(CLIENT_TAG_KEY, GolUtils.getTagValue(gol.dim, gensPerGol, "client"))
              .put(METHOD, METHOD_NAME)
              .put(CALLER, USERNAME)
              .put(ORIGINATOR, USERNAME)
              .build();

      try (Scope scope = tagger.withTagContext(ctx)) {
        for (int i = 0; i < numGols; ++i) {
          String result = client.executeCommand(request);
          String[] results = result.split("; ");
          if (results.length < 1 || results[0].isEmpty()) {
            break;
          }
          gol.currentGen = results[0];
          gol.gens += gensPerGol;
        }
      }
    }
  }

  /** Holds the specification of an RPC for the game of life server. */
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
}
