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

package com.google.instrumentation.examples.stats;

import com.google.instrumentation.stats.MeasurementDescriptor;
import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import com.google.instrumentation.stats.MeasurementMap;
import com.google.instrumentation.stats.Stats;
import com.google.instrumentation.stats.StatsContext;
import com.google.instrumentation.stats.TagKey;
import com.google.instrumentation.stats.TagValue;
import io.grpc.Context;
import java.util.Arrays;

/** Simple program that uses Stats contexts. */
public class StatsRunner {
  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagKey K3 = TagKey.create("k3");
  private static final TagKey K4 = TagKey.create("k4");

  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V3 = TagValue.create("v3");
  private static final TagValue V4 = TagValue.create("v4");

  private static final MeasurementUnit simpleMeasurementUnit =
      MeasurementUnit.create(1, Arrays.asList(BasicUnit.SCALAR));
  private static final MeasurementDescriptor M1 =
      MeasurementDescriptor.create("m1", "1st test metric", simpleMeasurementUnit);
  private static final MeasurementDescriptor M2 =
      MeasurementDescriptor.create("m2", "2nd test metric", simpleMeasurementUnit);

  /** Main method. */
  public static void main(String[] args) {
    System.out.println("Hello Stats World");
    System.out.println("Default Tags: " + DEFAULT);
    System.out.println("Current Tags: " + getCurrentStatsContext());
    Context context1 = withCurrent(DEFAULT.with(K1, V1, K2, V2));
    Context original = context1.attach();
    try {
      System.out.println("  Current Tags: " + getCurrentStatsContext());
      System.out.println(
          "  Current == Default + tags1: "
              + getCurrentStatsContext().equals(getStatsContext(context1)));
      Context context2 = withCurrent(getCurrentStatsContext().with(K3, V3, K4, V4));
      context2.attach();
      try {
        System.out.println("    Current Tags: " + getCurrentStatsContext());
        System.out.println(
            "    Current == Default + tags1 + tags2: "
                + getCurrentStatsContext().equals(getStatsContext(context2)));
        getCurrentStatsContext().record(MeasurementMap.of(M1, 0.2, M2, 0.4));
      } finally {
        context2.detach(context1);
      }
    } finally {
      context1.detach(original);
    }
    System.out.println("Current == Default: " + getCurrentStatsContext().equals(DEFAULT));
  }

  private static final StatsContext DEFAULT = Stats.getStatsContextFactory().getDefault();

  private static final Context.Key<StatsContext> STATS_CONTEXT_KEY =
      Context.keyWithDefault("StatsContextKey", DEFAULT);

  private static final StatsContext getCurrentStatsContext() {
    return getStatsContext(Context.current());
  }

  private static final StatsContext getStatsContext(Context context) {
    return STATS_CONTEXT_KEY.get(context);
  }

  private static final Context withCurrent(StatsContext context) {
    return Context.current().withValue(STATS_CONTEXT_KEY, context);
  }
}
