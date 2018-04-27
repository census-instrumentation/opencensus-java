/*
 * Copyright 2016-17, OpenCensus Authors
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

package io.opencensus.examples.tags;

import io.opencensus.common.Scope;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;

/** Simple program that uses {@link TagContext}. */
public class TagContextExample {

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagKey K3 = TagKey.create("k3");
  private static final TagKey K4 = TagKey.create("k4");

  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V3 = TagValue.create("v3");
  private static final TagValue V4 = TagValue.create("v4");

  private static final String UNIT = "1";
  private static final MeasureDouble M1 = MeasureDouble.create("m1", "1st test metric", UNIT);
  private static final MeasureDouble M2 = MeasureDouble.create("m2", "2nd test metric", UNIT);

  private static final Tagger tagger = Tags.getTagger();
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

  private TagContextExample() {}

  /**
   * Main method.
   *
   * @param args the main arguments.
   */
  public static void main(String[] args) {
    System.out.println("Hello Stats World");
    System.out.println("Default Tags: " + tagger.empty());
    System.out.println("Current Tags: " + tagger.getCurrentTagContext());
    TagContext tags1 = tagger.emptyBuilder().put(K1, V1).put(K2, V2).build();
    try (Scope scopedTagCtx1 = tagger.withTagContext(tags1)) {
      System.out.println("  Current Tags: " + tagger.getCurrentTagContext());
      System.out.println(
          "  Current == Default + tags1: " + tagger.getCurrentTagContext().equals(tags1));
      TagContext tags2 = tagger.toBuilder(tags1).put(K3, V3).put(K4, V4).build();
      try (Scope scopedTagCtx2 = tagger.withTagContext(tags2)) {
        System.out.println("    Current Tags: " + tagger.getCurrentTagContext());
        System.out.println(
            "    Current == Default + tags1 + tags2: "
                + tagger.getCurrentTagContext().equals(tags2));
        statsRecorder.newMeasureMap().put(M1, 0.2).put(M2, 0.4).record();
      }
    }
    System.out.println(
        "Current == Default: " + tagger.getCurrentTagContext().equals(tagger.empty()));
  }
}
