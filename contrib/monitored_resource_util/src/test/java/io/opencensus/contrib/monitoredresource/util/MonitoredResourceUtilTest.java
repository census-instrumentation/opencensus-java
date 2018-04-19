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

package io.opencensus.contrib.monitoredresource.util;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MonitoredResourceUtil}. */
@RunWith(JUnit4.class)
public class MonitoredResourceUtilTest {

  @Test
  public void testGetDefaultResource() {
    MonitoredResource resource = MonitoredResourceUtil.getDefaultResource();
    if (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
      assertThat(resource.getResourceType()).isEqualTo(ResourceType.GkeContainer);
    } else if (GcpMetadataConfig.getInstanceId() != null) {
      assertThat(resource.getResourceType()).isEqualTo(ResourceType.GceInstance);
    } else if (AwsIdentityDocUtils.isRunningOnAwsEc2()) {
      assertThat(resource.getResourceType()).isEqualTo(ResourceType.AwsEc2Instance);
    } else {
      assertThat(resource.getResourceType()).isEqualTo(ResourceType.Global);
    }
  }
}
