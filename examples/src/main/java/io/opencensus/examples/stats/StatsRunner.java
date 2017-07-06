/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.examples.stats;

import io.opencensus.common.NonThrowingCloseable;
import io.opencensus.stats.Measure.DoubleMeasure;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsContext;
import io.opencensus.stats.StatsContextFactory;
import io.opencensus.stats.TagKey;
import io.opencensus.stats.TagValue;

/**
 * Simple program that uses Stats contexts.
 */
public class StatsRunner {

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagKey K3 = TagKey.create("k3");
  private static final TagKey K4 = TagKey.create("k4");

  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V3 = TagValue.create("v3");
  private static final TagValue V4 = TagValue.create("v4");

  private static final String UNIT = "1";
  private static final DoubleMeasure M1 =
      DoubleMeasure.create("m1", "1st test metric", UNIT);
  private static final DoubleMeasure M2 =
      DoubleMeasure.create("m2", "2nd test metric", UNIT);

  private static final StatsContextFactory factory = Stats.getStatsContextFactory();
  private static final StatsContext DEFAULT = factory.getDefault();

  /**
   * Main method.
   *
   * @param args the main arguments.
   */
  public static void main(String[] args) {
    System.out.println("Hello Stats World");
    System.out.println("Default Tags: " + DEFAULT);
    System.out.println("Current Tags: " + factory.getCurrentStatsContext());
    StatsContext tags1 = DEFAULT.with(K1, V1, K2, V2);
    try (NonThrowingCloseable scopedStatsCtx1 = factory.withStatsContext(tags1)) {
      System.out.println("  Current Tags: " + factory.getCurrentStatsContext());
      System.out.println(
          "  Current == Default + tags1: " + factory.getCurrentStatsContext().equals(tags1));
      StatsContext tags2 = tags1.with(K3, V3, K4, V4);
      try (NonThrowingCloseable scopedStatsCtx2 = factory.withStatsContext(tags2)) {
        System.out.println("    Current Tags: " + factory.getCurrentStatsContext());
        System.out.println(
            "    Current == Default + tags1 + tags2: "
                + factory.getCurrentStatsContext().equals(tags2));
        factory.getCurrentStatsContext().record(
            MeasureMap.builder().set(M1, 0.2).set(M2, 0.4).build());
      }
    }
    System.out.println("Current == Default: " + factory.getCurrentStatsContext().equals(DEFAULT));
  }
}
