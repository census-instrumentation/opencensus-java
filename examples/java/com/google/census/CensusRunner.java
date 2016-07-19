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

package com.google.census;

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
    System.out.println("Default Tags: " + Census.getDefault());
    System.out.println("Current Tags: " + Census.getCurrent());
    try (CensusScope scope1 = CensusScope.of(K1, V1, K2, V2)) {
        CensusContext context1 = Census.getDefault().with(K1, V1, K2, V2);
        System.out.println("  Current Tags: " + Census.getCurrent());
        System.out.println("  Current == Default + tags1: " + Census.getCurrent().equals(context1));
        TagMap tags2 = TagMap.of();
        try (CensusScope scope2 = CensusScope.of(K3, V3, K4, V4)) {
            CensusContext context2 = context1.with(K3, V3, K4, V4);
            System.out.println("    Current Tags: " + Census.getCurrent());
            System.out.println("    Current == Default + tags1 + tags2: "
                + Census.getCurrent().equals(context2));
            Census.getCurrent().record(MetricMap.of(M1, 0.2, M2, 0.4));
        }
    }
    System.out.println("Current == Default: " +
        Census.getCurrent().equals(Census.getDefault()));
  }
}
