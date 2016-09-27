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

package com.google.census.examples;

import com.google.census.Census;
import com.google.census.CensusContext;
import com.google.census.MetricMap;
import com.google.census.MetricName;
import com.google.census.TagKey;
import com.google.census.TagValue;

public class CensusRunner {
  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");
  private static final TagKey K3 = new TagKey("k3");
  private static final TagKey K4 = new TagKey("k4");

  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");
  private static final TagValue V3 = new TagValue("v3");
  private static final TagValue V4 = new TagValue("v4");

  private static final MetricName M1 = new MetricName("m1");
  private static final MetricName M2 = new MetricName("m2");

  public static void main(String args[]) {
    System.out.println("Hello Census World");
    System.out.println("Default Tags: " + Census.getCensusContextFactory().getDefault());
    CensusContext context1 = Census.getCensusContextFactory().getDefault().with(K1, V1, K2, V2);
    System.out.println("Context1 Tags: " + context1);
    CensusContext context2 = context1.with(K3, V3, K4, V4);
    System.out.println("Context2 Tags: " + context2);
    context2.record(MetricMap.of(M1, 0.2, M2, 0.4));
  }
}
