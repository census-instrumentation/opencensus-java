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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Splitter;
import io.opencensus.resource.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for Kubernetes deployment service {@code Resource}.
 *
 * @since 0.20
 */
public class K8sResource {
  /**
   * The type of this {@code Resource}.
   *
   * @since 0.20
   */
  public static final String TYPE = "k8s";

  /**
   * Key for the name of the cluster.
   *
   * @since 0.20
   */
  public static final String CLUSTER_NAME_KEY = "k8s.cluster.name";

  /**
   * Key for the name of the namespace.
   *
   * @since 0.20
   */
  public static final String NAMESPACE_NAME_KEY = "k8s.namespace.name";

  /**
   * Key for the name of the pod.
   *
   * @since 0.20
   */
  public static final String POD_NAME_KEY = "k8s.pod.name";

  /**
   * Key for the name of the deployment.
   *
   * @since 0.24
   */
  public static final String DEPLOYMENT_NAME_KEY = "k8s.deployment.name";

  private static final Splitter splitter = Splitter.on('-');

  /**
   * Returns a {@link Resource} that describes Kubernetes deployment service.
   *
   * @param clusterName the k8s cluster name.
   * @param namespace the k8s namespace.
   * @param podName the k8s pod name.
   * @return a {@link Resource} that describes a k8s container.
   * @since 0.20
   * @deprecated in favor of {@link #create(String, String, String, String)}.
   */
  @Deprecated
  public static Resource create(String clusterName, String namespace, String podName) {
    return create(clusterName, namespace, podName, "");
  }

  /**
   * Returns a {@link Resource} that describes Kubernetes deployment service.
   *
   * @param clusterName the k8s cluster name.
   * @param namespace the k8s namespace.
   * @param podName the k8s pod name.
   * @param deploymentName the k8s deployment name.
   * @return a {@link Resource} that describes a k8s container.
   * @since 0.24
   */
  public static Resource create(
      String clusterName, String namespace, String podName, String deploymentName) {
    Map<String, String> labels = new LinkedHashMap<String, String>();
    labels.put(CLUSTER_NAME_KEY, checkNotNull(clusterName, "clusterName"));
    labels.put(NAMESPACE_NAME_KEY, checkNotNull(namespace, "namespace"));
    labels.put(POD_NAME_KEY, checkNotNull(podName, "podName"));
    labels.put(DEPLOYMENT_NAME_KEY, checkNotNull(deploymentName, "deploymentName"));
    return Resource.create(TYPE, labels);
  }

  static Resource detect() {
    String podName = firstNonNull(System.getenv("HOSTNAME"), "");
    String deploymentName = "";
    // Extract deployment name from the pod name. Pod name is created using
    // format: [deployment-name]-[Random-String-For-ReplicaSet]-[Random-String-For-Pod]
    List<String> parts = splitter.splitToList(podName);
    if (parts.size() == 3) {
      deploymentName = parts.get(0);
    }
    return create(
        GcpMetadataConfig.getClusterName(),
        firstNonNull(System.getenv("NAMESPACE"), ""),
        podName,
        deploymentName);
  }

  private K8sResource() {}
}
