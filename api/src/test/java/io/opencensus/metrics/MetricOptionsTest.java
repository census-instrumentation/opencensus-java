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

package io.opencensus.metrics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricOptions}. */
@RunWith(JUnit4.class)
public class MetricOptionsTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "test_name";
  private static final String DESCRIPTION = "test_description";
  private static final String UNIT = "1";
  private static final LabelKey LABEL_KEY = LabelKey.create("test_key", "test key description");
  private static final List<LabelKey> LABEL_KEYS = Collections.singletonList(LABEL_KEY);
  private static final LabelValue LABEL_VALUE = LabelValue.create("test_value");
  private static final List<LabelValue> LABEL_VALUES = Collections.singletonList(LABEL_VALUE);

  @Test
  public void nullDescription() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    MetricOptions.builder().setDescription(null).build();
  }

  @Test
  public void nullUnit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    MetricOptions.builder().setUnit(null).build();
  }

  @Test
  public void nullLabelKeys() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKeys");
    MetricOptions.builder().setLabelKeys(null).build();
  }

  @Test
  public void labelKeys_WithNullElement() {
    List<LabelKey> labelKeys = Collections.singletonList(null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelKeys elements");
    MetricOptions.builder().setLabelKeys(labelKeys).build();
  }

  @Test
  public void nullConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    MetricOptions.builder().setConstantLabels(null).build();
  }

  @Test
  public void constantLabels_WithNullKey() {
    Map<LabelKey, LabelValue> constantLabels = Collections.singletonMap(null, LABEL_VALUE);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels elements");
    MetricOptions.builder().setConstantLabels(constantLabels).build();
  }

  @Test
  public void constantLabels_WithNullValue() {
    Map<LabelKey, LabelValue> constantLabels = Collections.singletonMap(LABEL_KEY, null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels elements");
    MetricOptions.builder().setConstantLabels(constantLabels).build();
  }
}
