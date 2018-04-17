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

/**
 * {@link LabelKey} represents the associated property of a {@link MonitoredResource}.
 *
 * @since 0.13
 */
public enum LabelKey {
  GcpClusterName("cluster_name"),
  GcpContainerName("container_name"),
  GcpNamespaceId("namespace_id"),
  GcpInstanceId("instance_id"),
  GcpGkePodId("pod_id"),
  GcpZone("zone"),
  AwsAccount("aws_account"),
  AwsInstanceId("instance_id"),
  AwsRegion("region");

  private final String key;

  LabelKey(String key) {
    this.key = key;
  }

  /**
   * Returns the key of the {@link LabelKey}.
   *
   * @return the key of the {@link LabelKey}.
   * @since 0.13
   */
  public String getKey() {
    return key;
  }
}
