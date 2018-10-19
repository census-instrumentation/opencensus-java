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

package io.opencensus.metrics.export;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Summary}. */
@RunWith(JUnit4.class)
public class SummaryTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private static final double TOLERANCE = 1e-6;

  @Test
  public void createAndGet_ValueAtPercentile() {
    ValueAtPercentile valueAtPercentile = ValueAtPercentile.create(99.5, 10.2);
    assertThat(valueAtPercentile.getPercentile()).isWithin(TOLERANCE).of(99.5);
    assertThat(valueAtPercentile.getValue()).isWithin(TOLERANCE).of(10.2);
  }

  @Test
  public void createValueAtPercentile_InvalidValueAtPercentileInterval() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("percentile must be in the interval (0.0, 100.0]");
    ValueAtPercentile.create(100.1, 10.2);
  }

  @Test
  public void createValueAtPercentile_NegativeValue() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("value must be non-negative");
    ValueAtPercentile.create(99.5, -10.2);
  }

  @Test
  public void createAndGet_Snapshot() {
    Snapshot snapshot =
        Snapshot.create(
            10L, 87.07, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)));
    assertThat(snapshot.getCount()).isEqualTo(10);
    assertThat(snapshot.getSum()).isWithin(TOLERANCE).of(87.07);
    assertThat(snapshot.getValueAtPercentiles())
        .containsExactly(ValueAtPercentile.create(99.5, 10.2));
  }

  @Test
  public void createAndGet_Snapshot_WithNullCountAndSum() {
    Snapshot snapshot =
        Snapshot.create(
            null, null, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)));
    assertThat(snapshot.getCount()).isNull();
    assertThat(snapshot.getSum()).isNull();
    assertThat(snapshot.getValueAtPercentiles())
        .containsExactly(ValueAtPercentile.create(99.5, 10.2));
  }

  @Test
  public void createSnapshot_NegativeCount() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("count must be non-negative");
    Snapshot.create(-10L, 87.07, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)));
  }

  @Test
  public void createSnapshot_NegativeSum() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum must be non-negative");
    Snapshot.create(10L, -87.07, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)));
  }

  @Test
  public void createSnapshot_ZeroCountAndNonZeroSum() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum must be 0 if count is 0");
    Snapshot.create(0L, 87.07, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)));
  }

  @Test
  public void createSnapshot_NullValueAtPercentilesList() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("valueAtPercentiles");
    Snapshot.create(10L, 87.07, null);
  }

  @Test
  public void createSnapshot_OneNullValueAtPercentile() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("valueAtPercentile");
    Snapshot.create(10L, 87.07, Collections.<ValueAtPercentile>singletonList(null));
  }

  @Test
  public void createAndGet_Summary() {
    Snapshot snapshot =
        Snapshot.create(
            10L, 87.07, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)));
    Summary summary = Summary.create(10L, 6.6, snapshot);
    assertThat(summary.getCount()).isEqualTo(10);
    assertThat(summary.getSum()).isWithin(TOLERANCE).of(6.6);
    assertThat(summary.getSnapshot()).isEqualTo(snapshot);
  }

  @Test
  public void createSummary_NegativeCount() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("count must be non-negative");
    Summary.create(
        -10L, 6.6, Snapshot.create(null, null, Collections.<ValueAtPercentile>emptyList()));
  }

  @Test
  public void createSummary_NegativeSum() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum must be non-negative");
    Summary.create(
        10L, -6.6, Snapshot.create(null, null, Collections.<ValueAtPercentile>emptyList()));
  }

  @Test
  public void createSummary_ZeroCountAndNonZeroSum() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("sum must be 0 if count is 0");
    Summary.create(
        0L, 6.6, Snapshot.create(null, null, Collections.<ValueAtPercentile>emptyList()));
  }

  @Test
  public void createSummary_NullSnapshot() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("snapshot");
    Summary.create(10L, 6.6, null);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Summary.create(
                10L,
                10.0,
                Snapshot.create(
                    10L, 87.07, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)))),
            Summary.create(
                10L,
                10.0,
                Snapshot.create(
                    10L, 87.07, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)))))
        .addEqualityGroup(
            Summary.create(
                7L,
                10.0,
                Snapshot.create(
                    10L, 87.07, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)))))
        .addEqualityGroup(
            Summary.create(
                10L,
                7.0,
                Snapshot.create(
                    10L, 87.07, Collections.singletonList(ValueAtPercentile.create(99.5, 10.2)))))
        .addEqualityGroup(
            Summary.create(
                10L, 10.0, Snapshot.create(null, null, Collections.<ValueAtPercentile>emptyList())))
        .testEquals();
  }
}
