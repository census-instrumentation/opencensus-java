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
import io.opencensus.tags.TagValue.TagValueBoolean;
import io.opencensus.tags.TagValue.TagValueLong;
import io.opencensus.tags.TagValue.TagValueString;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagValue}. */
@RunWith(JUnit4.class)
public final class TagValueTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testMatchStringValue() {
    assertThat(
            TagValueString.create("value")
                .match(
                    new Function<TagValueString, String>() {
                      @Override
                      public String apply(TagValueString tag) {
                        return tag.asString();
                      }
                    },
                    new Function<TagValueLong, String>() {
                      @Override
                      public String apply(TagValueLong tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagValueBoolean, String>() {
                      @Override
                      public String apply(TagValueBoolean tag) {
                        throw new AssertionError();
                      }
                    },
                    Functions.<String>throwIllegalArgumentException()))
        .isEqualTo("value");
  }

  @Test
  public void testMatchLongValue() {
    assertThat(
            TagValueLong.create(1234)
                .match(
                    new Function<TagValueString, Long>() {
                      @Override
                      public Long apply(TagValueString tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagValueLong, Long>() {
                      @Override
                      public Long apply(TagValueLong tag) {
                        return tag.asLong();
                      }
                    },
                    new Function<TagValueBoolean, Long>() {
                      @Override
                      public Long apply(TagValueBoolean tag) {
                        throw new AssertionError();
                      }
                    },
                    Functions.<Long>throwIllegalArgumentException()))
        .isEqualTo(1234);
  }

  @Test
  public void testMatchBooleanValue() {
    assertThat(
            TagValueBoolean.create(false)
                .match(
                    new Function<TagValueString, Boolean>() {
                      @Override
                      public Boolean apply(TagValueString tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagValueLong, Boolean>() {
                      @Override
                      public Boolean apply(TagValueLong tag) {
                        throw new AssertionError();
                      }
                    },
                    new Function<TagValueBoolean, Boolean>() {
                      @Override
                      public Boolean apply(TagValueBoolean tag) {
                        return tag.asBoolean();
                      }
                    },
                    Functions.<Boolean>throwIllegalArgumentException()))
        .isEqualTo(false);
  }

  @Test
  public void allowStringTagValueWithMaxLength() {
    char[] chars = new char[TagValueString.MAX_LENGTH];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    assertThat(TagValueString.create(value).asString()).isEqualTo(value);
  }

  @Test
  public void disallowStringTagValueOverMaxLength() {
    char[] chars = new char[TagValueString.MAX_LENGTH + 1];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    TagValueString.create(value);
  }

  @Test
  public void disallowStringTagValueWithUnprintableChars() {
    String value = "\2ab\3cd";
    thrown.expect(IllegalArgumentException.class);
    TagValueString.create(value);
  }

  @Test
  public void testTagValueEquals() {
    new EqualsTester()
        .addEqualityGroup(TagValueString.create("foo"), TagValueString.create("foo"))
        .addEqualityGroup(TagValueLong.create(2))
        .addEqualityGroup(TagValueBoolean.create(true))
        .addEqualityGroup(TagValueString.create("bar"))
        .testEquals();
  }
}
