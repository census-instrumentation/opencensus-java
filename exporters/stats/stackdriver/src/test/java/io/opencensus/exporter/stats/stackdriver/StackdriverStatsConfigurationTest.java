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
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.DEFAULT_CONSTANT_LABELS;
import static io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration.DEFAULT_INTERVAL;

import com.google.api.MonitoredResource;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import io.opencensus.common.Duration;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackdriverStatsConfiguration}. */
@RunWith(JUnit4.class)
public class StackdriverStatsConfigurationTest {

  private static final Credentials FAKE_CREDENTIALS =
      GoogleCredentials.newBuilder().setAccessToken(new AccessToken("fake", new Date(100))).build();
  private static final String PROJECT_ID = "project";
  private static final Duration DURATION = Duration.create(10, 0);
  private static final MonitoredResource RESOURCE =
      MonitoredResource.newBuilder()
          .setType("gce-instance")
          .putLabels("instance-id", "instance")
          .build();
  private static final String CUSTOM_PREFIX = "myorg";

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testConstants() {
    assertThat(DEFAULT_INTERVAL).isEqualTo(Duration.create(60, 0));
  }

  @Test
  public void testBuild() {
    StackdriverStatsConfiguration configuration =
        StackdriverStatsConfiguration.builder()
            .setCredentials(FAKE_CREDENTIALS)
            .setProjectId(PROJECT_ID)
            .setExportInterval(DURATION)
            .setMonitoredResource(RESOURCE)
            .setMetricNamePrefix(CUSTOM_PREFIX)
            .setConstantLabels(Collections.<LabelKey, LabelValue>emptyMap())
            .build();
    assertThat(configuration.getCredentials()).isEqualTo(FAKE_CREDENTIALS);
    assertThat(configuration.getProjectId()).isEqualTo(PROJECT_ID);
    assertThat(configuration.getExportInterval()).isEqualTo(DURATION);
    assertThat(configuration.getMonitoredResource()).isEqualTo(RESOURCE);
    assertThat(configuration.getMetricNamePrefix()).isEqualTo(CUSTOM_PREFIX);
    assertThat(configuration.getConstantLabels()).isEmpty();
  }

  @Test
  public void testBuild_Default() {
    StackdriverStatsConfiguration configuration;
    try {
      configuration = StackdriverStatsConfiguration.builder().build();
    } catch (Exception e) {
      // Some test hosts may not have cloud project ID set up.
      configuration = StackdriverStatsConfiguration.builder().setProjectId("test").build();
    }
    assertThat(configuration.getCredentials()).isNull();
    assertThat(configuration.getProjectId()).isNotNull();
    assertThat(configuration.getExportInterval()).isEqualTo(DEFAULT_INTERVAL);
    assertThat(configuration.getMonitoredResource()).isNotNull();
    assertThat(configuration.getMetricNamePrefix()).isNull();
    assertThat(configuration.getConstantLabels()).isEqualTo(DEFAULT_CONSTANT_LABELS);
  }

  @Test
  public void disallowNullProjectId() {
    StackdriverStatsConfiguration.Builder builder = StackdriverStatsConfiguration.builder();
    thrown.expect(NullPointerException.class);
    builder.setProjectId(null);
  }

  @Test
  public void disallowEmptyProjectId() {
    StackdriverStatsConfiguration.Builder builder = StackdriverStatsConfiguration.builder();
    builder.setProjectId("");
    thrown.expect(IllegalArgumentException.class);
    builder.build();
  }

  @Test
  public void allowToUseDefaultProjectId() {
    String defaultProjectId = ServiceOptions.getDefaultProjectId();
    if (defaultProjectId != null) {
      StackdriverStatsConfiguration configuration = StackdriverStatsConfiguration.builder().build();
      assertThat(configuration.getProjectId()).isEqualTo(defaultProjectId);
    }
  }

  @Test
  public void disallowNullResource() {
    StackdriverStatsConfiguration.Builder builder =
        StackdriverStatsConfiguration.builder().setProjectId(PROJECT_ID);
    thrown.expect(NullPointerException.class);
    builder.setMonitoredResource(null);
  }

  @Test
  public void disallowNullExportInterval() {
    StackdriverStatsConfiguration.Builder builder =
        StackdriverStatsConfiguration.builder().setProjectId(PROJECT_ID);
    thrown.expect(NullPointerException.class);
    builder.setExportInterval(null);
  }

  @Test
  public void disallowNullConstantLabels() {
    StackdriverStatsConfiguration.Builder builder =
        StackdriverStatsConfiguration.builder().setProjectId(PROJECT_ID);
    thrown.expect(NullPointerException.class);
    builder.setConstantLabels(null);
  }

  @Test
  public void disallowNullConstantLabelKey() {
    StackdriverStatsConfiguration.Builder builder =
        StackdriverStatsConfiguration.builder().setProjectId(PROJECT_ID);
    Map<LabelKey, LabelValue> labels = Collections.singletonMap(null, LabelValue.create("val"));
    builder.setConstantLabels(labels);
    thrown.expect(NullPointerException.class);
    builder.build();
  }

  @Test
  public void disallowNullConstantLabelValue() {
    StackdriverStatsConfiguration.Builder builder =
        StackdriverStatsConfiguration.builder().setProjectId(PROJECT_ID);
    Map<LabelKey, LabelValue> labels =
        Collections.singletonMap(LabelKey.create("key", "desc"), null);
    builder.setConstantLabels(labels);
    thrown.expect(NullPointerException.class);
    builder.build();
  }

  @Test
  public void allowNullCredentials() {
    StackdriverStatsConfiguration configuration =
        StackdriverStatsConfiguration.builder()
            .setProjectId(PROJECT_ID)
            .setCredentials(null)
            .build();
    assertThat(configuration.getCredentials()).isNull();
  }

  @Test
  public void allowNullMetricPrefix() {
    StackdriverStatsConfiguration configuration =
        StackdriverStatsConfiguration.builder()
            .setProjectId(PROJECT_ID)
            .setMetricNamePrefix(null)
            .build();
    assertThat(configuration.getMetricNamePrefix()).isNull();
  }
}
