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
import io.opencensus.tags.TagKey.TagType;
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
    assertThat(TagKey.createStringKey("foo").getName()).isEqualTo("foo");
  }

  @Test
  public void testGetTagType() {
    assertThat(TagKey.createStringKey("key").getTagType()).isEqualTo(TagType.TAG_STRING);
    assertThat(TagKey.createLongKey("key").getTagType()).isEqualTo(TagType.TAG_LONG);
    assertThat(TagKey.createBooleanKey("key").getTagType()).isEqualTo(TagType.TAG_BOOLEAN);
  }

  @Test
  public void createString_AllowTagKeyNameWithMaxLength() {
    char[] key = new char[TagKey.MAX_LENGTH];
    Arrays.fill(key, 'k');
    TagKey.createStringKey(new String(key));
  }

  @Test
  public void createString_DisallowTagKeyNameOverMaxLength() {
    char[] key = new char[TagKey.MAX_LENGTH + 1];
    Arrays.fill(key, 'k');
    thrown.expect(IllegalArgumentException.class);
    TagKey.createStringKey(new String(key));
  }

  @Test
  public void createString_DisallowUnprintableChars() {
    thrown.expect(IllegalArgumentException.class);
    TagKey.createStringKey("\2ab\3cd");
  }

  @Test
  public void testTagKeyEquals() {
    new EqualsTester()
        .addEqualityGroup(TagKey.createStringKey("foo"), TagKey.createStringKey("foo"))
        .addEqualityGroup(TagKey.createLongKey("foo"))
        .addEqualityGroup(TagKey.createBooleanKey("foo"))
        .addEqualityGroup(TagKey.createStringKey("bar"))
        .testEquals();
  }
}
