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

import com.google.cloud.MetadataConfig;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import java.util.Map;

/**
 * Utilities for for auto detecting monitored resource based on the environment where the
 * application is running.
 *
 * @since 0.13
 */
public final class MonitoredResourceUtil {

  /* A mapping from ResourceType to its associated LabelKeys. */
  private static final ImmutableMultimap<ResourceType, LabelKey> RESOURCE_TYPE_WITH_LABEL_KEYS =
      ImmutableMultimap.<ResourceType, LabelKey>builder()
          .putAll(
              ResourceType.GkeContainer,
              LabelKey.GcpClusterName,
              LabelKey.GcpContainerName,
              LabelKey.GcpNamespaceId,
              LabelKey.GcpInstanceId,
              LabelKey.GcpGkePodId,
              LabelKey.GcpZone)
          .putAll(ResourceType.GceInstance, LabelKey.GcpInstanceId, LabelKey.GcpZone)
          .putAll(
              ResourceType.AwsEc2Instance,
              LabelKey.AwsAccount,
              LabelKey.AwsInstanceId,
              LabelKey.AwsRegion)
          .build();

  /**
   * Returns a self-configured monitored resource.
   *
   * @return a {@code Resource}.
   * @since 0.13
   */
  public static Resource getDefaultResource() {
    ResourceType detectedResourceType = getAutoDetectedResourceType();
    Map<String, String> labels = Maps.newHashMap();
    for (LabelKey labelKey : RESOURCE_TYPE_WITH_LABEL_KEYS.get(detectedResourceType)) {
      String value = getValue(labelKey);
      if (value == null) {
        value = "";
      }
      // Label values can be null or empty, but each label key must have an associated value.
      labels.put(labelKey.getKey(), value);
    }
    return Resource.create(detectedResourceType.getType(), labels);
  }

  // Gets the value for the given {@link LabelKey}.
  @javax.annotation.Nullable
  private static String getValue(LabelKey labelKey) {
    String value;
    switch (labelKey) {
      case GcpClusterName:
        value = MetadataConfig.getClusterName();
        break;
      case GcpInstanceId:
        value = MetadataConfig.getInstanceId();
        break;
      case GcpInstanceName:
        value = System.getenv("GAE_INSTANCE");
        break;
      case GcpGkePodId:
        value = System.getenv("HOSTNAME");
        break;
      case GcpZone:
        value = MetadataConfig.getZone();
        break;
      case GcpContainerName:
        value = System.getenv("CONTAINER_NAME");
        break;
      case GcpNamespaceId:
        value = System.getenv("NAMESPACE");
        break;
      case AwsAccount:
        value = AwsIdentityDocUtils.getValueFromAwsIdentityDocument("accountId");
        break;
      case AwsInstanceId:
        value = AwsIdentityDocUtils.getValueFromAwsIdentityDocument("instanceId");
        break;
      case AwsRegion:
        value = "aws:" + AwsIdentityDocUtils.getValueFromAwsIdentityDocument("region");
        break;
      default:
        value = null;
        break;
    }
    return value;
  }

  // Detects monitored resource type using environment variables, else return global as default.
  private static ResourceType getAutoDetectedResourceType() {
    if (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
      return ResourceType.GkeContainer;
    }
    if (MetadataConfig.getInstanceId() != null) {
      return ResourceType.GceInstance;
    }
    if (AwsIdentityDocUtils.isRunningOnAwsEc2()) {
      return ResourceType.AwsEc2Instance;
    }
    // default resource type
    return ResourceType.Global;
  }

  private MonitoredResourceUtil() {}
}
