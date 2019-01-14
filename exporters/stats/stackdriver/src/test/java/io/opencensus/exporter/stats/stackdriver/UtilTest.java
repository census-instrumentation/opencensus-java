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

package io.opencensus.exporter.stats.stackdriver;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Util}. */
@RunWith(JUnit4.class)
public class UtilTest {
  @Test
  public void testConstants() {
    assertThat(CreateTimeSeriesExporter.MAX_BATCH_EXPORT_SIZE).isEqualTo(200);
    assertThat(Util.CUSTOM_METRIC_DOMAIN).isEqualTo("custom.googleapis.com/");
    assertThat(Util.CUSTOM_OPENCENSUS_DOMAIN).isEqualTo("custom.googleapis.com/opencensus/");
    assertThat(Util.DEFAULT_DISPLAY_NAME_PREFIX).isEqualTo("OpenCensus/");
  }

  @Test
  public void getDomain() {
    assertThat(Util.getDomain(null)).isEqualTo("custom.googleapis.com/opencensus/");
    assertThat(Util.getDomain("")).isEqualTo("custom.googleapis.com/opencensus/");
    assertThat(Util.getDomain("custom.googleapis.com/myorg/"))
        .isEqualTo("custom.googleapis.com/myorg/");
    assertThat(Util.getDomain("external.googleapis.com/prometheus/"))
        .isEqualTo("external.googleapis.com/prometheus/");
    assertThat(Util.getDomain("myorg")).isEqualTo("myorg/");
  }

  @Test
  public void getDisplayNamePrefix() {
    assertThat(Util.getDisplayNamePrefix(null)).isEqualTo("OpenCensus/");
    assertThat(Util.getDisplayNamePrefix("")).isEqualTo("");
    assertThat(Util.getDisplayNamePrefix("custom.googleapis.com/myorg/"))
        .isEqualTo("custom.googleapis.com/myorg/");
    assertThat(Util.getDisplayNamePrefix("external.googleapis.com/prometheus/"))
        .isEqualTo("external.googleapis.com/prometheus/");
    assertThat(Util.getDisplayNamePrefix("myorg")).isEqualTo("myorg/");
  }
}
