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

package io.opencensus.temporary.metrics;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.temporary.metrics.MetricDescriptor.Type;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricDescriptor}. */
@RunWith(JUnit4.class)
public class MetricDescriptorTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME_1 = "metric1";
  private static final String METRIC_NAME_2 = "metric2";
  private static final String DESCRIPTION = "Metric description.";
  private static final String UNIT = "kb/s";
  private static final LabelKey KEY_1 = LabelKey.create("key1", "some key");
  private static final LabelKey KEY_2 = LabelKey.create("key2", "some other key");

  @Test
  public void testGet() {
    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(
            METRIC_NAME_1, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, Arrays.asList(KEY_1, KEY_2));
    assertThat(metricDescriptor.getName()).isEqualTo(METRIC_NAME_1);
    assertThat(metricDescriptor.getDescription()).isEqualTo(DESCRIPTION);
    assertThat(metricDescriptor.getUnit()).isEqualTo(UNIT);
    assertThat(metricDescriptor.getType()).isEqualTo(Type.GAUGE_DOUBLE);
    assertThat(metricDescriptor.getLabelKeys()).containsExactly(KEY_1, KEY_2).inOrder();
  }

  @Test
  public void preventNullLabelKeyList() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(CoreMatchers.equalTo("labelKeys"));
    MetricDescriptor.create(METRIC_NAME_1, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, null);
  }

  @Test
  public void preventNullLabelKey() {
    List<LabelKey> keys = Arrays.asList(KEY_1, null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(CoreMatchers.equalTo("labelKey"));
    MetricDescriptor.create(METRIC_NAME_1, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, keys);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            MetricDescriptor.create(
                METRIC_NAME_1, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, Arrays.asList(KEY_1, KEY_2)),
            MetricDescriptor.create(
                METRIC_NAME_1, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, Arrays.asList(KEY_1, KEY_2)))
        .addEqualityGroup(
            MetricDescriptor.create(
                METRIC_NAME_2, DESCRIPTION, UNIT, Type.GAUGE_DOUBLE, Arrays.asList(KEY_1, KEY_2)))
        .addEqualityGroup(
            MetricDescriptor.create(
                METRIC_NAME_2, DESCRIPTION, UNIT, Type.GAUGE_INT64, Arrays.asList(KEY_1, KEY_2)))
        .addEqualityGroup(
            MetricDescriptor.create(
                METRIC_NAME_1,
                DESCRIPTION,
                UNIT,
                Type.CUMULATIVE_DISTRIBUTION,
                Arrays.asList(KEY_1, KEY_2)))
        .addEqualityGroup(
            MetricDescriptor.create(
                METRIC_NAME_1,
                DESCRIPTION,
                UNIT,
                Type.CUMULATIVE_DISTRIBUTION,
                Arrays.asList(KEY_1)))
        .testEquals();
  }
}
