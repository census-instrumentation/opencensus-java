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
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.stats.View.DistributionView;
import io.opencensus.stats.View.IntervalView;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link View} */
@RunWith(JUnit4.class)
public final class ViewTest {
  @Test
  public void testDistributionView() {
    DistributionAggregationDescriptor dAggrDescriptor = DistributionAggregationDescriptor.create();
    final View view = DistributionView.create(
        name, description, measure, dAggrDescriptor, keys);

    assertThat(view.getViewName()).isEqualTo(name);
    assertThat(view.getName()).isEqualTo(name.asString());
    assertThat(view.getDescription()).isEqualTo(description);
    assertThat(view.getMeasure().getName()).isEqualTo(measure.getName());
    assertThat(view.getDimensions()).hasSize(2);
    assertThat(view.getDimensions().get(0).toString()).isEqualTo("foo");
    assertThat(view.getDimensions().get(1).toString()).isEqualTo("bar");
    assertTrue(view.match(
        new Function<DistributionView, Boolean> () {
          @Override public Boolean apply(DistributionView dView) {
            return dView == view;
          }
        },
        new Function<IntervalView, Boolean> () {
          @Override public Boolean apply(IntervalView iView) {
            return false;
          }
        }));
  }

  @Test
  public void testIntervalView() {
    IntervalAggregationDescriptor iAggrDescriptor = IntervalAggregationDescriptor.create(
        Arrays.asList(Duration.fromMillis(1), Duration.fromMillis(22), Duration.fromMillis(333)));
    final View view = IntervalView.create(
        name, description, measure, iAggrDescriptor, keys);

    assertThat(view.getViewName()).isEqualTo(name);
    assertThat(view.getName()).isEqualTo(name.asString());
    assertThat(view.getDescription()).isEqualTo(description);
    assertThat(view.getMeasure().getName())
        .isEqualTo(measure.getName());
    assertThat(view.getDimensions()).hasSize(2);
    assertThat(view.getDimensions().get(0).toString()).isEqualTo("foo");
    assertThat(view.getDimensions().get(1).toString()).isEqualTo("bar");
    assertTrue(view.match(
        new Function<DistributionView, Boolean> () {
          @Override public Boolean apply(DistributionView dView) {
            return false;
          }
        },
        new Function<IntervalView, Boolean> () {
          @Override public Boolean apply(IntervalView iView) {
            return iView == view;
          }
        }));
  }

  @Test
  public void testViewEquals() {
    DistributionAggregationDescriptor dAggrDescriptor = DistributionAggregationDescriptor.create();
    IntervalAggregationDescriptor iAggrDescriptor = IntervalAggregationDescriptor.create(
        Arrays.asList(Duration.fromMillis(1), Duration.fromMillis(22), Duration.fromMillis(333)));
    new EqualsTester()
        .addEqualityGroup(
            DistributionView.create(
                name, description, measure, dAggrDescriptor, keys),
            DistributionView.create(
                name, description, measure, dAggrDescriptor, keys))
        .addEqualityGroup(
            DistributionView.create(
                name, description + 2, measure, dAggrDescriptor, keys))
        .addEqualityGroup(
            IntervalView.create(
                name, description, measure, iAggrDescriptor, keys),
            IntervalView.create(
                name, description, measure, iAggrDescriptor, keys))
        .addEqualityGroup(
            IntervalView.create(
                name, description + 2, measure, iAggrDescriptor, keys))
        .testEquals();
  }

  @Test(expected = NullPointerException.class)
  public void preventNullDistributionViewName() {
    DistributionView.create(
        (View.Name) null,
        description,
        measure,
        DistributionAggregationDescriptor.create(),
        keys);
  }

  @Test(expected = NullPointerException.class)
  public void preventNullDistributionViewStringName() {
    DistributionView.create(
        (String) null,
        description,
        measure,
        DistributionAggregationDescriptor.create(),
        keys);
  }

  @Test(expected = NullPointerException.class)
  public void preventNullIntervalViewName() {
    IntervalView.create(
        (View.Name) null,
        description,
        measure,
        IntervalAggregationDescriptor.create(Arrays.asList(Duration.fromMillis(1))),
        keys);
  }

  @Test(expected = NullPointerException.class)
  public void preventNullIntervalViewStringName() {
    IntervalView.create(
        (String) null,
        description,
        measure,
        IntervalAggregationDescriptor.create(Arrays.asList(Duration.fromMillis(1))),
        keys);
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
  private final List<TagKey> keys = Arrays.asList(TagKey.create("foo"), TagKey.create("bar"));
}
