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

package io.opencensus.exporter.trace.stackdriver;

import static com.google.common.truth.Truth.assertThat;

import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import io.opencensus.trace.AttributeValue;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackdriverTraceConfiguration}. */
@RunWith(JUnit4.class)
public class StackdriverTraceConfigurationTest {

  private static final Credentials FAKE_CREDENTIALS =
      GoogleCredentials.newBuilder().setAccessToken(new AccessToken("fake", new Date(100))).build();
  private static final String PROJECT_ID = "project";

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultConfiguration() {
    StackdriverTraceConfiguration configuration;
    try {
      configuration = StackdriverTraceConfiguration.builder().build();
    } catch (Exception e) {
      // Some test hosts may not have cloud project ID set up.
      configuration = StackdriverTraceConfiguration.builder().setProjectId("test").build();
    }
    assertThat(configuration.getCredentials()).isNull();
    assertThat(configuration.getProjectId()).isNotNull();
    assertThat(configuration.getTraceServiceStub()).isNull();
    assertThat(configuration.getFixedAttributes()).isEmpty();
  }

  @Test
  public void updateAll() {
    Map<String, AttributeValue> attributes =
        Collections.singletonMap("key", AttributeValue.stringAttributeValue("val"));
    StackdriverTraceConfiguration configuration =
        StackdriverTraceConfiguration.builder()
            .setCredentials(FAKE_CREDENTIALS)
            .setProjectId(PROJECT_ID)
            .setFixedAttributes(attributes)
            .build();
    assertThat(configuration.getCredentials()).isEqualTo(FAKE_CREDENTIALS);
    assertThat(configuration.getProjectId()).isEqualTo(PROJECT_ID);
    assertThat(configuration.getFixedAttributes()).isEqualTo(attributes);
  }

  @Test
  public void disallowNullProjectId() {
    StackdriverTraceConfiguration.Builder builder = StackdriverTraceConfiguration.builder();
    thrown.expect(NullPointerException.class);
    builder.setProjectId(null);
  }

  @Test
  public void disallowEmptyProjectId() {
    StackdriverTraceConfiguration.Builder builder = StackdriverTraceConfiguration.builder();
    builder.setProjectId("");
    thrown.expect(IllegalArgumentException.class);
    builder.build();
  }

  @Test
  public void allowToUseDefaultProjectId() {
    String defaultProjectId = ServiceOptions.getDefaultProjectId();
    if (defaultProjectId != null) {
      StackdriverTraceConfiguration configuration = StackdriverTraceConfiguration.builder().build();
      assertThat(configuration.getProjectId()).isEqualTo(defaultProjectId);
    }
  }

  @Test
  public void disallowNullFixedAttributes() {
    StackdriverTraceConfiguration.Builder builder = StackdriverTraceConfiguration.builder();
    thrown.expect(NullPointerException.class);
    builder.setFixedAttributes(null);
  }

  @Test
  public void disallowNullFixedAttributeKey() {
    StackdriverTraceConfiguration.Builder builder = StackdriverTraceConfiguration.builder();
    Map<String, AttributeValue> attributes =
        Collections.singletonMap(null, AttributeValue.stringAttributeValue("val"));
    builder.setFixedAttributes(attributes);
    thrown.expect(NullPointerException.class);
    builder.build();
  }

  @Test
  public void disallowNullFixedAttributeValue() {
    StackdriverTraceConfiguration.Builder builder = StackdriverTraceConfiguration.builder();
    Map<String, AttributeValue> attributes = Collections.singletonMap("key", null);
    builder.setFixedAttributes(attributes);
    thrown.expect(NullPointerException.class);
    builder.build();
  }
}
