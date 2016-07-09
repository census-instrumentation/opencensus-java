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

import static com.google.common.truth.Truth.assertThat;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.ByteBuffer;

/**
 * Tests for {@link CensusContextFactory}.
 */
@RunWith(JUnit4.class)
public class CensusContextFactoryTest {
  @Test
  public void testDeserialize() {
    String s = new String(DEFAULT.serialize().array(), UTF_8);
    TagMap.Builder builder = TagMap.builder();
    String key = "k";
    String val = "v";
    for (int ix = 0; ix < 5; ix++) {
      builder.put(new TagKey(key), val);
      s += "\2" + key + "\3" + val;
      assertThat(DEFAULT.with(builder.build()))
          .isEqualTo(CensusContextFactory.deserialize(encode(s)));
      key += key;
      val += val;
    }
  }

  @Test
  public void testDeserializeEdgeCases() {
    assertThat(CensusContextFactory.deserialize(encode(""))).isEqualTo(DEFAULT);
    assertThat(CensusContextFactory.deserialize(encode("\2\3")))
        .isEqualTo(DEFAULT.with(TagMap.of(new TagKey(""), "")));
  }

  @Test
  public void testDeserializeMalformedContext() {
    assertThat(CensusContextFactory.deserialize(encode("g"))).isNull();
    assertThat(CensusContextFactory.deserialize(encode("garbagedata"))).isNull();
    assertThat(CensusContextFactory.deserialize(encode("\2key"))).isNull();
    assertThat(CensusContextFactory.deserialize(encode("\3val"))).isNull();
    assertThat(CensusContextFactory.deserialize(encode("\2key\2\3val"))).isNull();
    assertThat(CensusContextFactory.deserialize(encode("\2key\3val\3"))).isNull();
    assertThat(CensusContextFactory.deserialize(encode("\2key\3val\2key2"))).isNull();
  }

  private static final CensusContext DEFAULT = CensusContextFactory.getDefault();

  private static ByteBuffer encode(String s) {
    return ByteBuffer.wrap(s.getBytes(UTF_8));
  }
}
