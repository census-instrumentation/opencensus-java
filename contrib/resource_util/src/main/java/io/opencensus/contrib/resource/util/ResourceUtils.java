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

package io.opencensus.contrib.resource.util;

import io.opencensus.resource.Resource;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Utilities for for auto detecting monitored resource based on the environment where the
 * application is running.
 *
 * @since 0.13
 */
public final class ResourceUtils {

  /**
   * Returns a {@code Resource}. Detector sequentially runs resource detection from environment
   * variables, K8S, GCE and AWS.
   *
   * @return a {@code Resource}.
   * @since 0.18
   */
  @Nullable
  public static Resource detectResource() {
    List<Resource> resourceList = new ArrayList<Resource>();
    resourceList.add(Resource.createFromEnvironmentVariables());
    if (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
      resourceList.add(K8sContainerResource.detect());
    }
    // This can be true even if this is k8s container in case of GKE and we want to merge these
    // resources.
    if (GcpMetadataConfig.getInstanceId() != null) {
      resourceList.add(GcpGceInstanceResource.detect());
    }
    if (AwsIdentityDocUtils.isRunningOnAwsEc2()) {
      resourceList.add(AwsEc2InstanceResource.detect());
    }
    return Resource.mergeResources(resourceList);
  }

  private ResourceUtils() {}
}
