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
  private static final CensusContext DEFAULT = CensusContextFactory.getDefault();

  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");
  private static final TagKey K3 = new TagKey("k3");
  private static final TagKey K4 = new TagKey("k4");

  private static final MetricName M1 = new MetricName("m1");
  private static final MetricName M2 = new MetricName("m2");

  public static void main(String args[]) {
    System.out.println("Hello Census World");
    System.out.println("Default Tags: " + CensusContextFactory.getDefault());
    System.out.println("Current Tags: " + CensusContextFactory.getCurrent());
    TagMap tags1 = TagMap.of(K1, "v1", K2, "v2");
    try (CensusScope scope1 = new CensusScope(tags1)) {
        System.out.println("  Current Tags: " + CensusContextFactory.getCurrent());
        System.out.println("  Current == Default + tags1: "
            + CensusContextFactory.getCurrent().equals(DEFAULT.with(tags1)));
        TagMap tags2 = TagMap.of(K3, "v3", K4, "v4");
        try (CensusScope scope2 = new CensusScope(tags2)) {
            System.out.println("    Current Tags: " + CensusContextFactory.getCurrent());
            System.out.println("    Current == Default + tags1 + tags2: "
                + CensusContextFactory.getCurrent().equals(DEFAULT.with(tags1).with(tags2)));
            CensusContextFactory.getCurrent().record(MetricMap.of(M1, 0.2, M2, 0.4));
        }
    }
    System.out.println("Current == Default: " +
        CensusContextFactory.getCurrent().equals(CensusContextFactory.getDefault()));
  }
}
