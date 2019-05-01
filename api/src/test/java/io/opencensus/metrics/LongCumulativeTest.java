/*
 * Copyright 2019, OpenCensus Authors
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

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongCumulative}. */
@RunWith(JUnit4.class)
public class LongCumulativeTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("key", "key description"));
  private static final List<LabelValue> LABEL_VALUES =
      Collections.singletonList(LabelValue.create("value"));
  private static final List<LabelKey> EMPTY_LABEL_KEYS = new ArrayList<LabelKey>();
  private static final List<LabelValue> EMPTY_LABEL_VALUES = new ArrayList<LabelValue>();

  @Test
  public void noopGetOrCreateTimeSeries_WithNullLabelValues() {
    LongCumulative longCumulative =
        LongCumulative.newNoopLongCumulative(NAME, DESCRIPTION, UNIT, EMPTY_LABEL_KEYS);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    longCumulative.getOrCreateTimeSeries(null);
  }

  @Test
  public void noopGetOrCreateTimeSeries_WithNullElement() {
    List<LabelValue> labelValues = Collections.singletonList(null);
    LongCumulative longCumulative =
        LongCumulative.newNoopLongCumulative(NAME, DESCRIPTION, UNIT, LABEL_KEY);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValue");
    longCumulative.getOrCreateTimeSeries(labelValues);
  }

  @Test
  public void noopGetOrCreateTimeSeries_WithInvalidLabelSize() {
    LongCumulative longCumulative =
        LongCumulative.newNoopLongCumulative(NAME, DESCRIPTION, UNIT, LABEL_KEY);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Label Keys and Label Values don't have same size.");
    longCumulative.getOrCreateTimeSeries(EMPTY_LABEL_VALUES);
  }

  @Test
  public void noopRemoveTimeSeries_WithNullLabelValues() {
    LongCumulative longCumulative =
        LongCumulative.newNoopLongCumulative(NAME, DESCRIPTION, UNIT, LABEL_KEY);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labelValues");
    longCumulative.removeTimeSeries(null);
  }

  @Test
  public void noopSameAs() {
    LongCumulative longCumulative =
        LongCumulative.newNoopLongCumulative(NAME, DESCRIPTION, UNIT, LABEL_KEY);
    assertThat(longCumulative.getDefaultTimeSeries())
        .isSameInstanceAs(longCumulative.getDefaultTimeSeries());
    assertThat(longCumulative.getDefaultTimeSeries())
        .isSameInstanceAs(longCumulative.getOrCreateTimeSeries(LABEL_VALUES));
  }
}
