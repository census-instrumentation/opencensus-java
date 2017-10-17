/*
 * Copyright 2016-17, OpenCensus Authors
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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import io.opencensus.common.Duration;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.AggregationWindow.Interval;
import io.opencensus.tags.TagKey;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link View}. */
@RunWith(JUnit4.class)
public final class ViewTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testConstants() {
    assertThat(View.NAME_MAX_LENGTH).isEqualTo(256);
  }

  @Test
  public void testDistributionView() {
    final View view = View.create(
        NAME, DESCRIPTION, MEASURE, MEAN, keys, Cumulative.create());
    assertThat(view.getName()).isEqualTo(NAME);
    assertThat(view.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(view.getMeasure().getName()).isEqualTo(MEASURE.getName());
    assertThat(view.getAggregation()).isEqualTo(MEAN);
    assertThat(view.getColumns()).hasSize(2);
    assertThat(view.getColumns()).containsExactly(FOO, BAR).inOrder();
    assertThat(view.getWindow()).isEqualTo(Cumulative.create());
  }

  @Test
  public void testIntervalView() {
    final View view = View.create(
        NAME, DESCRIPTION, MEASURE, MEAN, keys, Interval.create(MINUTE));
    assertThat(view.getName()).isEqualTo(NAME);
    assertThat(view.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(view.getMeasure().getName())
        .isEqualTo(MEASURE.getName());
    assertThat(view.getAggregation()).isEqualTo(MEAN);
    assertThat(view.getColumns()).hasSize(2);
    assertThat(view.getColumns()).containsExactly(FOO, BAR).inOrder();
    assertThat(view.getWindow()).isEqualTo(Interval.create(MINUTE));
  }

  @Test
  public void testViewEquals() {
    new EqualsTester()
        .addEqualityGroup(
            View.create(
                NAME, DESCRIPTION, MEASURE, MEAN, keys, Cumulative.create()),
            View.create(
                NAME, DESCRIPTION, MEASURE, MEAN, keys, Cumulative.create()))
        .addEqualityGroup(
            View.create(
                NAME, DESCRIPTION + 2, MEASURE, MEAN, keys, Cumulative.create()))
        .addEqualityGroup(
            View.create(
                NAME, DESCRIPTION, MEASURE, MEAN, keys, Interval.create(MINUTE)),
            View.create(
                NAME, DESCRIPTION, MEASURE, MEAN, keys, Interval.create(MINUTE)))
        .addEqualityGroup(
            View.create(
                NAME, DESCRIPTION, MEASURE, MEAN, keys, Interval.create(TWO_MINUTES)))
        .testEquals();
  }

  @Test(expected = IllegalArgumentException.class)
  public void preventDuplicateColumns() {
    View.create(
        NAME,
        DESCRIPTION,
        MEASURE,
        MEAN,
        Arrays.asList(TagKey.create("duplicate"), TagKey.create("duplicate")),
        Cumulative.create());
  }

  @Test(expected = NullPointerException.class)
  public void preventNullViewName() {
    View.create(null, DESCRIPTION, MEASURE, MEAN, keys, Interval.create(MINUTE));
  }

  @Test
  public void preventTooLongViewName() {
    char[] chars = new char[View.NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    View.Name.create(longName);
  }

  @Test
  public void preventNonPrintableViewName() {
    thrown.expect(IllegalArgumentException.class);
    View.Name.create("\2");
  }

  @Test
  public void testViewName() {
    assertThat(View.Name.create("my name").asString()).isEqualTo("my name");
  }

  @Test(expected = NullPointerException.class)
  public void preventNullNameString() {
    View.Name.create(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void preventNegativeIntervalDuration() {
    Interval.create(NEG_TEN_SECONDS);
  }

  @Test
  public void testViewNameEquals() {
    new EqualsTester()
        .addEqualityGroup(
            View.Name.create("view-1"), View.Name.create("view-1"))
        .addEqualityGroup(View.Name.create("view-2"))
        .testEquals();
  }

  private static final View.Name NAME = View.Name.create("test-view-name");
  private static final String DESCRIPTION = "test-view-name description";
  private static final Measure MEASURE = Measure.MeasureDouble.create(
      "measure", "measure description", "1");
  private static final TagKey FOO = TagKey.create("foo");
  private static final TagKey BAR = TagKey.create("bar");
  private static final List<TagKey> keys = ImmutableList.of(FOO, BAR);
  private static final Mean MEAN = Mean.create();
  private static final Duration MINUTE = Duration.create(60, 0);
  private static final Duration TWO_MINUTES = Duration.create(120, 0);
  private static final Duration NEG_TEN_SECONDS = Duration.create(-10, 0);
}
