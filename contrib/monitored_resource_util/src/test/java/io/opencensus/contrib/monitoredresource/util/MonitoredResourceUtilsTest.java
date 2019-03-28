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

import io.opencensus.contrib.resource.util.CloudResource;
import io.opencensus.contrib.resource.util.ContainerResource;
import io.opencensus.contrib.resource.util.HostResource;
import io.opencensus.contrib.resource.util.ResourceUtils;
import io.opencensus.resource.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MonitoredResourceUtils}. */
@RunWith(JUnit4.class)
public class MonitoredResourceUtilsTest {

  @Test
  public void testGetDefaultResource() {
    MonitoredResource monitoredResource = MonitoredResourceUtils.getDefaultResource();
    Resource resource = ResourceUtils.detectResource();
    String resourceType = resource == null ? null : resource.getType();
    if (resourceType == null) {
      assertThat(monitoredResource).isNull();
      return;
    }
    assertThat(monitoredResource).isNotNull();
    if (resourceType.equals(ContainerResource.TYPE)) {
      assertThat(monitoredResource.getResourceType()).isEqualTo(ResourceType.GCP_GKE_CONTAINER);
    } else if (resourceType.equals(HostResource.TYPE)
        && CloudResource.PROVIDER_GCP.equals(
            resource.getLabels().get(CloudResource.PROVIDER_KEY))) {
      assertThat(monitoredResource.getResourceType()).isEqualTo(ResourceType.GCP_GCE_INSTANCE);
    } else if (resourceType.equals(HostResource.TYPE)
        && CloudResource.PROVIDER_GCP.equals(
            resource.getLabels().get(CloudResource.PROVIDER_KEY))) {
      assertThat(monitoredResource.getResourceType()).isEqualTo(ResourceType.AWS_EC2_INSTANCE);
    } else {
      assertThat(monitoredResource).isNull();
    }
  }
}
