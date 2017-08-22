/*
 * Copyright 2016, Google Inc.
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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Duration;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.View.Window.Cumulative;
import io.opencensus.stats.View.Window.Interval;
import io.opencensus.tags.TagKey.TagKeyString;
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
        name, description, measure, aggregations, keys, Cumulative.create());
    assertThat(view.getName()).isEqualTo(name);
    assertThat(view.getDescription()).isEqualTo(description);
    assertThat(view.getMeasure().getName()).isEqualTo(measure.getName());
    assertThat(view.getAggregations()).isEqualTo(aggregations);
    assertThat(view.getColumns()).hasSize(2);
    assertThat(view.getColumns()).containsExactly(FOO, BAR).inOrder();
    assertThat(view.getWindow()).isEqualTo(Cumulative.create());
  }

  @Test
  public void testIntervalView() {
    final View view = View.create(
        name, description, measure, aggregations, keys, Interval.create(minute));
    assertThat(view.getName()).isEqualTo(name);
    assertThat(view.getDescription()).isEqualTo(description);
    assertThat(view.getMeasure().getName())
        .isEqualTo(measure.getName());
    assertThat(view.getAggregations()).isEqualTo(aggregations);
    assertThat(view.getColumns()).hasSize(2);
    assertThat(view.getColumns()).containsExactly(FOO, BAR).inOrder();
    assertThat(view.getWindow()).isEqualTo(Interval.create(minute));
  }

  @Test
  public void testViewEquals() {
    new EqualsTester()
        .addEqualityGroup(
            View.create(
                name, description, measure, aggregations, keys, Cumulative.create()),
            View.create(
                name, description, measure, aggregations, keys, Cumulative.create()))
        .addEqualityGroup(
            View.create(
                name, description + 2, measure, aggregations, keys, Cumulative.create()))
        .addEqualityGroup(
            View.create(
                name, description, measure, aggregations, keys, Interval.create(minute)),
            View.create(
                name, description, measure, aggregations, keys, Interval.create(minute)))
        .addEqualityGroup(
            View.create(
                name, description, measure, aggregations, keys, Interval.create(twoMinutes)))
        .testEquals();
  }

  @Test(expected = IllegalArgumentException.class)
  public void preventDuplicateAggregations() {
    View.create(name, description, measure,
        Arrays.asList(Sum.create(), Sum.create(), Count.create()), keys, Cumulative.create());
  }

  @Test(expected = IllegalArgumentException.class)
  public void preventDuplicateColumns() {
    View.create(
        name,
        description,
        measure,
        aggregations,
        Arrays.asList(TagKeyString.create("duplicate"), TagKeyString.create("duplicate")),
        Cumulative.create());
  }

  @Test(expected = NullPointerException.class)
  public void preventNullViewName() {
    View.create(null, description, measure, aggregations, keys, Interval.create(minute));
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

  @Test
  public void testViewNameEquals() {
    new EqualsTester()
        .addEqualityGroup(
            View.Name.create("view-1"), View.Name.create("view-1"))
        .addEqualityGroup(View.Name.create("view-2"))
        .testEquals();
  }

  private final View.Name name = View.Name.create("test-view-name");
  private final String description = "test-view-name description";
  private final Measure measure = Measure.MeasureDouble.create(
      "measure", "measure description", "1");
  private static final TagKeyString FOO = TagKeyString.create("foo");
  private static final TagKeyString BAR = TagKeyString.create("bar");
  private final List<TagKeyString> keys = Arrays.asList(FOO, BAR);
  private final List<Aggregation> aggregations = Arrays.asList(Sum.create(), Count.create());
  private final Duration minute = Duration.create(60, 0);
  private final Duration twoMinutes = Duration.create(120, 0);
}
