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

package io.opencensus.contrib.resource.util;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.resource.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CloudResource}. */
@RunWith(JUnit4.class)
public class CloudResourceTest {
  private static final String PROVIDER = "provider";
  private static final String ACCOUNT_ID = "account_id";
  private static final String REGION = "region";
  private static final String ZONE = "zone";

  @Test
  public void create_ContainerResourceTest() {
    Resource resource = CloudResource.create(PROVIDER, ACCOUNT_ID, REGION, ZONE);
    assertThat(resource.getType()).isEqualTo(CloudResource.TYPE);
    assertThat(resource.getLabels())
        .containsExactly(
            CloudResource.PROVIDER_KEY,
            PROVIDER,
            CloudResource.ACCOUNT_ID_KEY,
            ACCOUNT_ID,
            CloudResource.REGION_KEY,
            REGION,
            CloudResource.ZONE_KEY,
            ZONE);
  }
}
