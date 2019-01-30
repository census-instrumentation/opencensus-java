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
 * Helper class for K8S container {@code Resource}.
 *
 * @since 0.20
 */
public class K8sContainerResource {
  private static final String CLUSTER_NAME = firstNonNull(GcpMetadataConfig.getClusterName(), "");
  private static final String CONTAINER_NAME = firstNonNull(System.getenv("CONTAINER_NAME"), "");
  private static final String NAMESPACE_NAME = firstNonNull(System.getenv("NAMESPACE"), "");
  private static final String POD_NAME = firstNonNull(System.getenv("HOSTNAME"), "");

  /**
   * Kubernetes resources key that represents a type of the resource.
   *
   * @since 0.20
   */
  public static final String TYPE = "k8s.io/container";

  /**
   * Kubernetes resources key that represents the name for the cluster the container is running in.
   *
   * @since 0.20
   */
  public static final String CLUSTER_NAME_KEY = "k8s.io/cluster/name";

  /**
   * Kubernetes resources key that represents the identifier for the GCE instance the container is
   * running in.
   *
   * @since 0.20
   */
  public static final String NAMESPACE_NAME_KEY = "k8s.io/namespace/name";

  /**
   * Kubernetes resources key that represents the identifier for the pod the container is running
   * in.
   *
   * @since 0.20
   */
  public static final String POD_NAME_KEY = "k8s.io/pod/name";

  /**
   * Kubernetes resources key that represents the name of the container.
   *
   * @since 0.20
   */
  public static final String CONTAINER_NAME_KEY = "k8s.io/container/name";

  /**
   * Returns a {@link Resource} that describes a k8s container.
   *
   * @param clusterName the k8s cluster name.
   * @param namespace the k8s namespace.
   * @param podName the k8s pod name.
   * @param containerName the k8s container name.
   * @return a {@link Resource} that describes a k8s container.
   * @since 0.20
   */
  public static Resource create(
      String clusterName, String namespace, String podName, String containerName) {
    Map<String, String> mutableLabels = new LinkedHashMap<String, String>();
    mutableLabels.put(CLUSTER_NAME_KEY, checkNotNull(clusterName, "clusterName"));
    mutableLabels.put(NAMESPACE_NAME_KEY, checkNotNull(namespace, "namespace"));
    mutableLabels.put(POD_NAME_KEY, checkNotNull(podName, "podName"));
    mutableLabels.put(CONTAINER_NAME_KEY, checkNotNull(containerName, "containerName"));
    return Resource.create(TYPE, Collections.unmodifiableMap(mutableLabels));
  }

  static Resource detect() {
    return create(CLUSTER_NAME, NAMESPACE_NAME, POD_NAME, CONTAINER_NAME);
  }

  private K8sContainerResource() {}
}
