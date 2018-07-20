/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.trace.Tracestate.Entry;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Tracestate}. */
@RunWith(JUnit4.class)
public class TracestateTest {
  private static final String FIRST_KEY = "key_1";
  private static final String SECOND_KEY = "key_2";
  private static final String FIRST_VALUE = "value.1";
  private static final String SECOND_VALUE = "value.2";

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private final Tracestate firstTracestate =
      Tracestate.EMPTY.toBuilder().addOrUpdate(FIRST_KEY, FIRST_VALUE).build();
  private final Tracestate secondTracestate =
      Tracestate.EMPTY.toBuilder().addOrUpdate(SECOND_KEY, SECOND_VALUE).build();
  private final Tracestate multiValueTracestate =
      Tracestate.EMPTY
          .toBuilder()
          .addOrUpdate(FIRST_KEY, FIRST_VALUE)
          .addOrUpdate(SECOND_KEY, SECOND_VALUE)
          .build();

  @Test
  public void get() {
    assertThat(firstTracestate.get(FIRST_KEY)).isEqualTo(FIRST_VALUE);
    assertThat(secondTracestate.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(multiValueTracestate.get(FIRST_KEY)).isEqualTo(FIRST_VALUE);
    assertThat(multiValueTracestate.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
  }

  @Test
  public void getEntries() {
    assertThat(firstTracestate.getEntries()).containsExactly(Entry.create(FIRST_KEY, FIRST_VALUE));
    assertThat(secondTracestate.getEntries())
        .containsExactly(Entry.create(SECOND_KEY, SECOND_VALUE));
    assertThat(multiValueTracestate.getEntries())
        .containsExactly(
            Entry.create(FIRST_KEY, FIRST_VALUE), Entry.create(SECOND_KEY, SECOND_VALUE));
  }

  @Test
  public void disallowsNullKey() {
    thrown.expect(NullPointerException.class);
    Tracestate.EMPTY.toBuilder().addOrUpdate(null, FIRST_VALUE).build();
  }

  @Test
  public void invalidFirstKeyCharacter() {
    thrown.expect(IllegalArgumentException.class);
    Tracestate.EMPTY.toBuilder().addOrUpdate("1_key", FIRST_VALUE).build();
  }

  @Test
  public void invalidKeyCharacters() {
    thrown.expect(IllegalArgumentException.class);
    Tracestate.EMPTY.toBuilder().addOrUpdate("kEy_1", FIRST_VALUE).build();
  }

  @Test
  public void invalidKeySize() {
    char[] chars = new char[257];
    Arrays.fill(chars, 'a');
    String longKey = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    Tracestate.EMPTY.toBuilder().addOrUpdate(longKey, FIRST_VALUE).build();
  }

  @Test
  public void allAllowedKeyCharacters() {
    StringBuilder stringBuilder = new StringBuilder();
    for (char c = 'a'; c <= 'z'; c++) {
      stringBuilder.append(c);
    }
    for (char c = '0'; c <= '9'; c++) {
      stringBuilder.append(c);
    }
    stringBuilder.append('_');
    stringBuilder.append('-');
    stringBuilder.append('*');
    stringBuilder.append('/');
    String allowedKey = stringBuilder.toString();
    assertThat(
            Tracestate.EMPTY
                .toBuilder()
                .addOrUpdate(allowedKey, FIRST_VALUE)
                .build()
                .get(allowedKey))
        .isEqualTo(FIRST_VALUE);
  }

  @Test
  public void disallowsNullValue() {
    thrown.expect(NullPointerException.class);
    Tracestate.EMPTY.toBuilder().addOrUpdate(FIRST_KEY, null).build();
  }

  @Test
  public void valueCannotContainEqual() {
    thrown.expect(IllegalArgumentException.class);
    Tracestate.EMPTY.toBuilder().addOrUpdate(FIRST_KEY, "my_vakue=5").build();
  }

  @Test
  public void valueCannotContainComma() {
    thrown.expect(IllegalArgumentException.class);
    Tracestate.EMPTY.toBuilder().addOrUpdate(FIRST_KEY, "first,second").build();
  }

  @Test
  public void invalidValueSize() {
    char[] chars = new char[257];
    Arrays.fill(chars, 'a');
    String longValue = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    Tracestate.EMPTY.toBuilder().addOrUpdate(FIRST_KEY, longValue).build();
  }

  @Test
  public void allAllowedValueCharacters() {
    StringBuilder stringBuilder = new StringBuilder();
    for (char c = ' ' /* '\u0020' */; c <= '~' /* '\u007E' */; c++) {
      if (c == ',' || c == '=') {
        continue;
      }
      stringBuilder.append(c);
    }
    String allowedValue = stringBuilder.toString();
    assertThat(
            Tracestate.EMPTY
                .toBuilder()
                .addOrUpdate(FIRST_KEY, allowedValue)
                .build()
                .get(FIRST_KEY))
        .isEqualTo(allowedValue);
  }

  @Test
  public void addEntry() {
    assertThat(firstTracestate.toBuilder().addOrUpdate(SECOND_KEY, SECOND_VALUE).build())
        .isEqualTo(multiValueTracestate);
  }

  @Test
  public void updateEntry() {
    assertThat(
            firstTracestate.toBuilder().addOrUpdate(FIRST_KEY, SECOND_VALUE).build().get(FIRST_KEY))
        .isEqualTo(SECOND_VALUE);
    Tracestate updatedMultiValueTracestate =
        multiValueTracestate.toBuilder().addOrUpdate(FIRST_KEY, SECOND_VALUE).build();
    assertThat(updatedMultiValueTracestate.get(FIRST_KEY)).isEqualTo(SECOND_VALUE);
    assertThat(updatedMultiValueTracestate.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
  }

  @Test
  public void addAndUpdateEntry() {
    assertThat(
            firstTracestate
                .toBuilder()
                .addOrUpdate(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .addOrUpdate(SECOND_KEY, FIRST_VALUE) // add a new entry
                .build()
                .getEntries())
        .containsExactly(
            Entry.create(FIRST_KEY, SECOND_VALUE), Entry.create(SECOND_KEY, FIRST_VALUE));
  }

  @Test
  public void addSameKey() {
    assertThat(
            Tracestate.EMPTY
                .toBuilder()
                .addOrUpdate(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .addOrUpdate(FIRST_KEY, FIRST_VALUE) // add a new entry
                .build()
                .getEntries())
        .containsExactly(Entry.create(FIRST_KEY, FIRST_VALUE));
  }

  @Test
  public void remove() {
    assertThat(multiValueTracestate.toBuilder().remove(SECOND_KEY).build())
        .isEqualTo(firstTracestate);
  }

  @Test
  public void addAndRemoveEntry() {
    assertThat(
            Tracestate.EMPTY
                .toBuilder()
                .addOrUpdate(FIRST_KEY, SECOND_VALUE) // update the existing entry
                .remove(FIRST_KEY) // add a new entry
                .build())
        .isEqualTo(Tracestate.EMPTY);
  }

  @Test
  public void remove_NullNotAllowed() {
    thrown.expect(NullPointerException.class);
    multiValueTracestate.toBuilder().remove(null).build();
  }

  @Test
  public void tracestate_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(Tracestate.EMPTY, Tracestate.EMPTY);
    tester.addEqualityGroup(
        firstTracestate, Tracestate.EMPTY.toBuilder().addOrUpdate(FIRST_KEY, FIRST_VALUE).build());
    tester.addEqualityGroup(
        secondTracestate,
        Tracestate.EMPTY.toBuilder().addOrUpdate(SECOND_KEY, SECOND_VALUE).build());
    tester.testEquals();
  }

  @Test
  public void tracestate_ToString() {
    assertThat(Tracestate.EMPTY.toString()).isEqualTo("Tracestate{entries=[]}");
  }
}
