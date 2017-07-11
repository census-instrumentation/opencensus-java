/*
 * Copyright 2017, Google Inc.
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

package io.opencensus.tags;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.tags.TagKey.TagKeyBoolean;
import io.opencensus.tags.TagKey.TagKeyLong;
import io.opencensus.tags.TagKey.TagKeyString;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagKey}. */
@RunWith(JUnit4.class)
public final class TagKeyTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testGetName() {
    assertThat(TagKeyString.create("foo").getName()).isEqualTo("foo");
  }

  @Test
  public void createString_AllowTagKeyNameWithMaxLength() {
    char[] key = new char[TagKey.MAX_LENGTH];
    Arrays.fill(key, 'k');
    TagKeyString.create(new String(key));
  }

  @Test
  public void createString_DisallowTagKeyNameOverMaxLength() {
    char[] key = new char[TagKey.MAX_LENGTH + 1];
    Arrays.fill(key, 'k');
    thrown.expect(IllegalArgumentException.class);
    TagKeyString.create(new String(key));
  }

  @Test
  public void createString_DisallowUnprintableChars() {
    thrown.expect(IllegalArgumentException.class);
    TagKeyString.create("\2ab\3cd");
  }

  @Test
  public void testMatchStringKey() {
    assertThat(
            TagKeyString.create("key")
                .match(
                    new Function<TagKeyString, String>() {
                      @Override
                      public String apply(TagKeyString tag) {
                        return tag.getName();
                      }
                    },
                    new Function<TagKeyLong, String>() {
                      @Override
                      public String apply(TagKeyLong tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagKeyBoolean, String>() {
                      @Override
                      public String apply(TagKeyBoolean tag) {
                        throw new AssertionError();
                      }
                    },
                    Functions.<String>throwIllegalArgumentException()))
        .isEqualTo("key");
  }

  @Test
  public void testMatchLongKey() {
    assertThat(
            TagKeyLong.create("key")
                .match(
                    new Function<TagKeyString, String>() {
                      @Override
                      public String apply(TagKeyString tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagKeyLong, String>() {
                      @Override
                      public String apply(TagKeyLong tag) {
                        return tag.getName();
                      }
                    },
                    new Function<TagKeyBoolean, String>() {
                      @Override
                      public String apply(TagKeyBoolean tag) {
                        throw new AssertionError();
                      }
                    },
                    Functions.<String>throwIllegalArgumentException()))
        .isEqualTo("key");
  }

  @Test
  public void testMatchBooleanKey() {
    assertThat(
            TagKeyBoolean.create("key")
                .match(
                    new Function<TagKeyString, String>() {
                      @Override
                      public String apply(TagKeyString tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagKeyLong, String>() {
                      @Override
                      public String apply(TagKeyLong tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagKeyBoolean, String>() {
                      @Override
                      public String apply(TagKeyBoolean tag) {
                        return tag.getName();
                      }
                    },
                    Functions.<String>throwIllegalArgumentException()))
        .isEqualTo("key");
  }

  @Test
  public void testTagKeyEquals() {
    new EqualsTester()
        .addEqualityGroup(TagKeyString.create("foo"), TagKeyString.create("foo"))
        .addEqualityGroup(TagKeyLong.create("foo"))
        .addEqualityGroup(TagKeyBoolean.create("foo"))
        .addEqualityGroup(TagKeyString.create("bar"))
        .testEquals();
  }
}
