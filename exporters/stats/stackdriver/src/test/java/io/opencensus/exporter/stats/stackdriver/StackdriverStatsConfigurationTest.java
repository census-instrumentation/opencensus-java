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
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import io.opencensus.common.Duration;
import java.util.Date;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackdriverStatsConfiguration}. */
@RunWith(JUnit4.class)
public class StackdriverStatsConfigurationTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final Credentials FAKE_CREDENTIALS =
      GoogleCredentials.newBuilder().setAccessToken(new AccessToken("fake", new Date(100))).build();
  private static final String PROJECT_ID = "project";
  private static final Duration DURATION = Duration.create(10, 0);
  private static final MonitoredResource RESOURCE =
      MonitoredResource.newBuilder()
          .setType("gce-instance")
          .putLabels("instance-id", "instance")
          .build();

  @Test
  public void testBuild() {
    StackdriverStatsConfiguration configuration =
        StackdriverStatsConfiguration.builder()
            .setCredentials(FAKE_CREDENTIALS)
            .setProjectId(PROJECT_ID)
            .setExportInterval(DURATION)
            .setMonitoredResource(RESOURCE)
            .build();
    assertThat(configuration.getCredentials()).isEqualTo(FAKE_CREDENTIALS);
    assertThat(configuration.getProjectId()).isEqualTo(PROJECT_ID);
    assertThat(configuration.getExportInterval()).isEqualTo(DURATION);
    assertThat(configuration.getMonitoredResource()).isEqualTo(RESOURCE);
  }

  @Test
  public void testBuild_Default() {
    StackdriverStatsConfiguration configuration = StackdriverStatsConfiguration.builder().build();
    assertThat(configuration.getCredentials()).isNull();
    assertThat(configuration.getProjectId()).isNull();
    assertThat(configuration.getExportInterval()).isNull();
    assertThat(configuration.getMonitoredResource()).isNull();
  }
}
