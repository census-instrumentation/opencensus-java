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

import io.opencensus.tags.TagValueString;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagValueString}. */
@RunWith(JUnit4.class)
public class TagValueStringTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

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
}
