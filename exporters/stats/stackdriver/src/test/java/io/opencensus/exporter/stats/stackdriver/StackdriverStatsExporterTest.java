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

import com.google.auth.oauth2.GoogleCredentials;
import io.opencensus.common.Duration;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackdriverStatsExporter}. */
@RunWith(JUnit4.class)
public class StackdriverStatsExporterTest {

  private static final String PROJECT_ID = "projectId";
  private static final Duration ONE_SECOND = Duration.create(1, 0);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void createWithNullCredentials() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("credentials");
    StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
        null, PROJECT_ID, ONE_SECOND);
  }

  @Test
  public void createWithNullProjectId() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("projectId");
    StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), null, ONE_SECOND);
  }

  @Test
  public void createWithNullDuration() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("exportInterval");
    StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), PROJECT_ID, null);
  }

  @Test
  public void createWithNegativeDuration() throws IOException {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Duration must be positive");
    StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), PROJECT_ID, Duration.create(-1, 0));
  }

  @Test
  public void createExporterTwice() throws IOException {
    StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), PROJECT_ID, ONE_SECOND);
    try {
      thrown.expect(IllegalStateException.class);
      thrown.expectMessage("Stackdriver stats exporter is already created.");
      StackdriverStatsExporter.createAndRegisterWithCredentialsAndProjectId(
          GoogleCredentials.newBuilder().build(), PROJECT_ID, ONE_SECOND);
    } finally {
      StackdriverStatsExporter.unsafeResetExporter();
    }
  }

  @Test
  public void setNullMonitoredResource() {
    thrown.expect(NullPointerException.class);
    StackdriverStatsExporter.setMonitoredResource(null);
  }
}
