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

import io.opencensus.contrib.resource.util.AwsEc2InstanceResource;
import io.opencensus.contrib.resource.util.GcpGceInstanceResource;
import io.opencensus.contrib.resource.util.K8sContainerResource;

/**
 * Constants for collecting resource information.
 *
 * @since 0.18
 * @deprecated use constant values from resource helper {@link AwsEc2InstanceResource}, {@link
 *     GcpGceInstanceResource} and {@link K8sContainerResource}.
 */
@Deprecated
public final class ResourceKeyConstants {

  /**
   * AWS key that represents a type of the resource.
   *
   * @since 0.18
   */
  public static final String AWS_EC2_INSTANCE_TYPE = AwsEc2InstanceResource.TYPE;

  /**
   * AWS key that represents a region for the VM.
   *
   * @since 0.18
   */
  public static final String AWS_REGION_KEY = AwsEc2InstanceResource.REGION_KEY;

  /**
   * AWS key that represents the AWS account number for the VM.
   *
   * @since 0.18
   */
  public static final String AWS_ACCOUNT_KEY = AwsEc2InstanceResource.ACCOUNT_ID_KEY;

  /**
   * AWS key that represents the VM instance identifier assigned by AWS.
   *
   * @since 0.18
   */
  public static final String AWS_INSTANCE_ID_KEY = AwsEc2InstanceResource.INSTANCE_ID_KEY;

  /**
   * GCP GCE key that represents a type of the resource.
   *
   * @since 0.18
   */
  public static final String GCP_GCE_INSTANCE_TYPE = GcpGceInstanceResource.TYPE;

  /**
   * GCP GCE key that represents the GCP account number for the instance.
   *
   * @since 0.18
   */
  public static final String GCP_ACCOUNT_ID_KEY = GcpGceInstanceResource.PROJECT_ID_KEY;

  /**
   * GCP GCE key that represents the numeric VM instance identifier assigned by GCE.
   *
   * @since 0.18
   */
  public static final String GCP_INSTANCE_ID_KEY = GcpGceInstanceResource.INSTANCE_ID_KEY;

  /**
   * GCP GCE key that represents the GCE zone in which the VM is running.
   *
   * @since 0.18
   */
  public static final String GCP_ZONE_KEY = GcpGceInstanceResource.ZONE_KEY;

  /**
   * Kubernetes resources key that represents a type of the resource.
   *
   * @since 0.18
   */
  public static final String K8S_CONTAINER_TYPE = K8sContainerResource.TYPE;

  /**
   * Kubernetes resources key that represents the name for the cluster the container is running in.
   *
   * @since 0.18
   */
  public static final String K8S_CLUSTER_NAME_KEY = K8sContainerResource.CLUSTER_NAME_KEY;

  /**
   * Kubernetes resources key that represents the name of the container.
   *
   * @since 0.18
   */
  public static final String K8S_CONTAINER_NAME_KEY = K8sContainerResource.CONTAINER_NAME_KEY;

  /**
   * Kubernetes resources key that represents the identifier for the GCE instance the container is
   * running in.
   *
   * @since 0.18
   */
  public static final String K8S_NAMESPACE_NAME_KEY = K8sContainerResource.NAMESPACE_NAME_KEY;

  /**
   * Kubernetes resources key that represents the identifier for the pod the container is running
   * in.
   *
   * @since 0.18
   */
  public static final String K8S_POD_NAME_KEY = K8sContainerResource.POD_NAME_KEY;

  private ResourceKeyConstants() {}
}
