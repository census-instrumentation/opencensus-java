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

package io.opencensus.exporter.stats.stackdriver;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.MonitoredResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackdriverStatsMonitoredResource}. */
@RunWith(JUnit4.class)
public class StackdriverStatsMonitoredResourceTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getDefault() {
    assertThat(StackdriverStatsMonitoredResource.getMonitoredResource())
        .isEqualTo(MonitoredResource.newBuilder().setType("global").build());
  }

  @Test
  public void setAndGet() {
    MonitoredResource resource =
        MonitoredResource.newBuilder()
            .setType("global")
            .putLabels("instance-id", "some-instance")
            .build();
    StackdriverStatsMonitoredResource.setMonitoredResource(resource);
    try {
      assertThat(StackdriverStatsMonitoredResource.getMonitoredResource()).isEqualTo(resource);
    } finally {
      StackdriverStatsMonitoredResource.setMonitoredResource(
          MonitoredResource.newBuilder().setType("global").build());
    }
  }

  @Test
  public void preventSettingNull() {
    thrown.expect(NullPointerException.class);
    StackdriverStatsMonitoredResource.setMonitoredResource(null);
  }
}
