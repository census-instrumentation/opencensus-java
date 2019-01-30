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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.resource.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for GCP GCE instance {@code Resource}.
 *
 * @since 0.20
 */
public final class GcpGceInstanceResource {
  private static final String PROJECT_ID = firstNonNull(GcpMetadataConfig.getProjectId(), "");
  private static final String INSTANCE_ID = firstNonNull(GcpMetadataConfig.getInstanceId(), "");
  private static final String ZONE = firstNonNull(GcpMetadataConfig.getZone(), "");

  /**
   * GCP GCE key that represents a type of the resource.
   *
   * @since 0.20
   */
  public static final String TYPE = "cloud.google.com/gce/instance";

  /**
   * GCP GCE key that represents the GCP account number for the instance.
   *
   * @since 0.20
   */
  public static final String PROJECT_ID_KEY = "cloud.google.com/gce/project_id";

  /**
   * GCP GCE key that represents the numeric VM instance identifier assigned by GCE.
   *
   * @since 0.20
   */
  public static final String INSTANCE_ID_KEY = "cloud.google.com/gce/instance_id";

  /**
   * GCP GCE key that represents the GCE zone in which the VM is running.
   *
   * @since 0.20
   */
  public static final String ZONE_KEY = "cloud.google.com/gce/zone";

  /**
   * Returns a {@link Resource} that describes a k8s container.
   *
   * @param projectId the GCP project number.
   * @param zone the GCP zone.
   * @param instanceId the GCP GCE instance ID.
   * @return a {@link Resource} that describes a k8s container.
   * @since 0.20
   */
  public static Resource create(String projectId, String zone, String instanceId) {
    Map<String, String> mutableLabels = new LinkedHashMap<String, String>();
    mutableLabels.put(PROJECT_ID_KEY, checkNotNull(projectId, "projectId"));
    mutableLabels.put(ZONE_KEY, checkNotNull(zone, "zone"));
    mutableLabels.put(INSTANCE_ID_KEY, checkNotNull(instanceId, "instanceId"));
    return Resource.create(TYPE, Collections.unmodifiableMap(mutableLabels));
  }

  static Resource detect() {
    return create(PROJECT_ID, ZONE, INSTANCE_ID);
  }

  private GcpGceInstanceResource() {}
}
