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

import io.opencensus.contrib.monitoredresource.util.MonitoredResource.AwsEc2InstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGceInstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGkeContainerMonitoredResource;
import javax.annotation.Nullable;

/**
 * Utilities for for auto detecting monitored resource based on the environment where the
 * application is running.
 *
 * @since 0.13
 */
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
    if (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
      return GcpGkeContainerMonitoredResource.create();
    }
    if (GcpMetadataConfig.getInstanceId() != null) {
      return GcpGceInstanceMonitoredResource.create();
    }
    if (AwsIdentityDocUtils.isRunningOnAwsEc2()) {
      return AwsEc2InstanceMonitoredResource.create();
    }
    return null;
  }

  private MonitoredResourceUtils() {}
}
