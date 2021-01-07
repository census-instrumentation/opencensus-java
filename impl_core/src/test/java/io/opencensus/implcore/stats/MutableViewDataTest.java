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

package io.opencensus.implcore.stats;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import io.opencensus.implcore.internal.CurrentState;
import io.opencensus.implcore.tags.TagMapImpl;
import io.opencensus.metrics.data.AttachmentValue;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.tags.TagKey;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MutableViewData}. */
@RunWith(JUnit4.class)
public class MutableViewDataTest {

  @Test
  public void testConstants() {
    assertThat(MutableViewData.ZERO_TIMESTAMP).isEqualTo(Timestamp.create(0, 0));
  }

  @Test
  public void testTimeRewindsOnCountViewNoThrow() {
    // First we set up some buckets THEN we rewind time for giggles.
    View tester =
        View.create(
            View.Name.create("view"),
            "Description",
            MeasureDouble.create("name", "desc", "us"),
            Count.create(),
            Collections.singletonList(TagKey.create("KEY")));
    Timestamp start = Timestamp.create(10000000, 0);
    Timestamp validPointTime = Timestamp.create(10000010, 0);
    CurrentState.State state = CurrentState.State.ENABLED;
    MutableViewData viewData = MutableViewData.create(tester, start);
    // Create a data points to get thrown away.
    viewData.record(
        TagMapImpl.EMPTY, 1.0, validPointTime, Collections.<String, AttachmentValue>emptyMap());
    // Rewind time and look for explosions.
    Timestamp thePast = Timestamp.create(0, 0);
    ViewData result = viewData.toViewData(thePast, state);
    assertThat(result.getAggregationMap()).isEmpty();
  }

  @Test
  public void testTimeRewindsOnDistributionViewNoThrow() {
    // First we set up some buckets THEN we rewind time for giggles.
    Aggregation latencyDistribution =
        Distribution.create(
            BucketBoundaries.create(Arrays.asList(0.0, 25.0, 100.0, 200.0, 400.0, 800.0, 10000.0)));
    View tester =
        View.create(
            View.Name.create("view"),
            "Description",
            MeasureDouble.create("name", "desc", "us"),
            latencyDistribution,
            Collections.singletonList(TagKey.create("KEY")));
    Timestamp start = Timestamp.create(10000000, 0);
    Timestamp validPointTime = Timestamp.create(10000010, 0);
    CurrentState.State state = CurrentState.State.ENABLED;
    MutableViewData viewData = MutableViewData.create(tester, start);
    // Create a data points to get thrown away.
    viewData.record(
        TagMapImpl.EMPTY, 1.0, validPointTime, Collections.<String, AttachmentValue>emptyMap());
    // Rewind time and look for explosions.
    Timestamp thePast = Timestamp.create(0, 0);
    ViewData result = viewData.toViewData(thePast, state);
    assertThat(result.getAggregationMap()).isEmpty();
  }
}
