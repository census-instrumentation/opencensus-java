/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.tags;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.tags.Tag.TagBoolean;
import io.opencensus.tags.Tag.TagLong;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Tag}. */
@RunWith(JUnit4.class)
public final class TagTest {

  @Test
  public void testGetStringKey() {
    assertThat(
            TagString.create(TagKeyString.create("k"), TagValueString.create("v")).getKey())
        .isEqualTo(TagKeyString.create("k"));
  }

  @Test
  public void testGetLongKey() {
    assertThat(TagLong.create(TagKeyLong.create("k"), 2L).getKey())
        .isEqualTo(TagKeyLong.create("k"));
  }

  @Test
  public void testGetBooleanKey() {
    assertThat(TagBoolean.create(TagKeyBoolean.create("k"), false).getKey())
        .isEqualTo(TagKeyBoolean.create("k"));
  }

  @Test
  public void testMatchStringTag() {
    assertThat(
            TagString.create(TagKeyString.create("k1"), TagValueString.create("value"))
                .match(
                    new Function<TagString, String>() {
                      @Override
                      public String apply(TagString tag) {
                        return tag.getValue().asString();
                      }
                    },
                    new Function<TagLong, String>() {
                      @Override
                      public String apply(TagLong tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagBoolean, String>() {
                      @Override
                      public String apply(TagBoolean tag) {
                        throw new AssertionError();
                      }
                    },
                    Functions.<String>throwIllegalArgumentException()))
        .isEqualTo("value");
  }

  @Test
  public void testMatchLong() {
    assertThat(
            TagLong.create(TagKeyLong.create("k2"), 3L)
                .match(
                    new Function<TagString, Long>() {
                      @Override
                      public Long apply(TagString tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagLong, Long>() {
                      @Override
                      public Long apply(TagLong tag) {
                        return tag.getValue();
                      }
                    },
                    new Function<TagBoolean, Long>() {
                      @Override
                      public Long apply(TagBoolean tag) {
                        throw new AssertionError();
                      }
                    },
                    Functions.<Long>throwIllegalArgumentException()))
        .isEqualTo(3L);
  }

  @Test
  public void testMatchBoolean() {
    assertThat(
            TagBoolean.create(TagKeyBoolean.create("k3"), false)
                .match(
                    new Function<TagString, Boolean>() {
                      @Override
                      public Boolean apply(TagString tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagLong, Boolean>() {
                      @Override
                      public Boolean apply(TagLong tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagBoolean, Boolean>() {
                      @Override
                      public Boolean apply(TagBoolean tag) {
                        return tag.getValue();
                      }
                    },
                    Functions.<Boolean>throwIllegalArgumentException()))
        .isEqualTo(false);
  }

  @Test
  public void testTagEquals() {
    new EqualsTester()
        .addEqualityGroup(
            TagString.create(TagKeyString.create("Key1"), TagValueString.create("foo")),
            TagString.create(TagKeyString.create("Key1"), TagValueString.create("foo")))
        .addEqualityGroup(
            TagLong.create(TagKeyLong.create("Key1"), 100L),
            TagLong.create(TagKeyLong.create("Key1"), 100L))
        .addEqualityGroup(
            TagBoolean.create(TagKeyBoolean.create("Key1"), true),
            TagBoolean.create(TagKeyBoolean.create("Key1"), true))
        .addEqualityGroup(TagBoolean.create(TagKeyBoolean.create("Key2"), true))
        .addEqualityGroup(TagBoolean.create(TagKeyBoolean.create("Key1"), false))
        .testEquals();
  }
}
