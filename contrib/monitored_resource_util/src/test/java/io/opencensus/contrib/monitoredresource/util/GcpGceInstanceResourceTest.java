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

package io.opencensus.contrib.monitoredresource.util;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.resource.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link GcpGceInstanceResource}. */
@RunWith(JUnit4.class)
public class GcpGceInstanceResourceTest {
  private static final String GCP_PROJECT_ID = "gcp-project";
  private static final String GCP_INSTANCE_ID = "instance";
  private static final String GCP_ZONE = "us-east1";

  @Test
  public void create_GcpGceInstanceResource() {
    Resource resource = GcpGceInstanceResource.create(GCP_PROJECT_ID, GCP_ZONE, GCP_INSTANCE_ID);
    assertThat(resource.getType()).isEqualTo(GcpGceInstanceResource.TYPE);
    assertThat(resource.getLabels())
        .containsExactly(
            GcpGceInstanceResource.PROJECT_ID_KEY,
            GCP_PROJECT_ID,
            GcpGceInstanceResource.ZONE_KEY,
            GCP_ZONE,
            GcpGceInstanceResource.INSTANCE_ID_KEY,
            GCP_INSTANCE_ID);
  }
}
