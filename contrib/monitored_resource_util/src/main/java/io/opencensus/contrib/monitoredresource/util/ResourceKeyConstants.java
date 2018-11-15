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
 * Constants for collecting resource information.
 *
 * @since 0.18
 */
public final class ResourceKeyConstants {

  /**
   * AWS key that represents a type of the resource.
   *
   * @since 0.18
   */
  public static final String AWS_EC2_INSTANCE_TYPE = "aws_ec2_instance";

  /**
   * AWS key that represents a region for the VM.
   *
   * @since 0.18
   */
  public static final String AWS_REGION_KEY = "region";

  /**
   * AWS key that represents the AWS account number for the VM.
   *
   * @since 0.18
   */
  public static final String AWS_ACCOUNT_KEY = "aws_account";

  /**
   * AWS key that represents the VM instance identifier assigned by AWS.
   *
   * @since 0.18
   */
  public static final String AWS_INSTANCE_ID_KEY = "instance_id";

  /**
   * AWS key that represents a prefix for region value.
   *
   * @since 0.18
   */
  public static final String AWS_REGION_VALUE_PREFIX = "aws:";

  /**
   * GCP GCE key that represents a type of the resource.
   *
   * @since 0.18
   */
  public static final String GCP_GCE_INSTANCE_TYPE = "gce_instance";

  /**
   * GCP GCE key that represents the GCP account number for the instance.
   *
   * @since 0.18
   */
  public static final String GCP_ACCOUNT_ID_KEY = "gcp_account";

  /**
   * GCP GCE key that represents the numeric VM instance identifier assigned by GCE.
   *
   * @since 0.18
   */
  public static final String GCP_INSTANCE_ID_KEY = "instance_id";

  /**
   * GCP GCE key that represents the GCE zone in which the VM is running.
   *
   * @since 0.18
   */
  public static final String GCP_ZONE_KEY = "zone";

  /**
   * GCP GKE key that represents a type of the resource.
   *
   * @since 0.18
   */
  public static final String GCP_GKE_INSTANCE_TYPE = "k8s_container";

  /**
   * GCP GKE key that represents the name for the cluster the container is running in.
   *
   * @since 0.18
   */
  public static final String GCP_GKE_CLUSTER_KEY = "cluster_name";

  /**
   * GCP GKE key that represents the name of the container.
   *
   * @since 0.18
   */
  public static final String GCP_GKE_CONTAINER_KEY = "container_name";

  /**
   * GCP GKE key that represents the identifier for the GCE instance the container is running in.
   *
   * @since 0.18
   */
  public static final String GCP_GKE_NAMESPACE_ID_KEY = "namespace_id";

  /**
   * GCP GKE key that represents the identifier for the pod the container is running in.
   *
   * @since 0.18
   */
  public static final String GCP_GKE_POD_ID_KEY = "pod_id";

  /**
   * GCP GKE key that represents the GCE location in which the VM is running.
   *
   * @since 0.18
   */
  public static final String GCP_GKE_ZONE_KEY = "zone";

  private ResourceKeyConstants() {}
}
