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

import io.opencensus.contrib.resource.util.CloudResource;
import io.opencensus.contrib.resource.util.ContainerResource;
import io.opencensus.contrib.resource.util.HostResource;
import io.opencensus.contrib.resource.util.ResourceUtils;
import io.opencensus.resource.Resource;
import javax.annotation.Nullable;

/**
 * Utilities for for auto detecting monitored resource based on the environment where the
 * application is running.
 *
 * @since 0.13
 * @deprecated since 0.20, use {@link ResourceUtils} instead.
 */
@Deprecated
public final class MonitoredResourceUtils {

  /**
   * Returns a self-configured monitored resource, or {@code null} if the application is not running
   * on a supported environment.
   *
   * @return a {@code MonitoredResource}.
   * @since 0.13
   */
  @Nullable
  public static MonitoredResource getDefaultResource() {
    Resource resource = ResourceUtils.detectResource();
    if (resource == null) {
      return null;
    }
    String resourceType = resource.getType();
    if (ContainerResource.TYPE.equals(resourceType)) {
      return MonitoredResource.GcpGkeContainerMonitoredResource.create(resource);
    }
    if (HostResource.TYPE.equals(resourceType)) {
      String provider = resource.getLabels().get(CloudResource.PROVIDER_KEY);
      if (CloudResource.PROVIDER_GCP.equals(provider)) {
        return MonitoredResource.GcpGceInstanceMonitoredResource.create(resource);
      }
      if (CloudResource.PROVIDER_AWS.equals(provider)) {
        return MonitoredResource.AwsEc2InstanceMonitoredResource.create(resource);
      }
    }
    return null;
  }

  /**
   * Returns a {@code Resource}. Detector sequentially runs resource detection from environment
   * variables, K8S, GCE and AWS.
   *
   * @return a {@code Resource}.
   * @since 0.18
   */
  @Nullable
  public static Resource detectResource() {
    return ResourceUtils.detectResource();
  }

  private MonitoredResourceUtils() {}
}
