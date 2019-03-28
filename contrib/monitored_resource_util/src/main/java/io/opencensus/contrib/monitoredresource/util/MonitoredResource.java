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

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableMap;
import io.opencensus.resource.Resource;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * {@link MonitoredResource} represents an auto-detected monitored resource used by application for
 * exporting stats. It has a {@code ResourceType} associated with a mapping from resource labels to
 * values.
 *
 * @since 0.13
 * @deprecated use {@link Resource}.
 */
@Immutable
@Deprecated
public abstract class MonitoredResource {

  MonitoredResource() {}

  /**
   * Returns the {@link ResourceType} of this {@link MonitoredResource}.
   *
   * @return the {@code ResourceType}.
   * @since 0.13
   */
  public abstract ResourceType getResourceType();

  // TODO(songya): consider using a tagged union match() approach (that will introduce
  // dependency on opencensus-api).

  /**
   * {@link MonitoredResource} for AWS EC2 instance.
   *
   * @since 0.13
   * @deprecated use {@link Resource}.
   */
  @Immutable
  @Deprecated
  public static final class AwsEc2InstanceMonitoredResource extends MonitoredResource {
    private final Map<String, String> labels;

    @Override
    public ResourceType getResourceType() {
      return ResourceType.AWS_EC2_INSTANCE;
    }

    /**
     * Returns the AWS account ID.
     *
     * @return the AWS account ID.
     * @since 0.13
     */
    public String getAccount() {
      return firstNonNull(labels.get(ResourceKeyConstants.AWS_ACCOUNT_KEY), "");
    }

    /**
     * Returns the AWS EC2 instance ID.
     *
     * @return the AWS EC2 instance ID.
     * @since 0.13
     */
    public String getInstanceId() {
      return firstNonNull(labels.get(ResourceKeyConstants.AWS_INSTANCE_ID_KEY), "");
    }

    /**
     * Returns the AWS region.
     *
     * @return the AWS region.
     * @since 0.13
     */
    public String getRegion() {
      return firstNonNull(labels.get(ResourceKeyConstants.AWS_REGION_KEY), "");
    }

    /**
     * Returns an {@link AwsEc2InstanceMonitoredResource}.
     *
     * @param account the AWS account ID.
     * @param instanceId the AWS EC2 instance ID.
     * @param region the AWS region.
     * @return an {@code AwsEc2InstanceMonitoredResource}.
     * @since 0.15
     */
    public static AwsEc2InstanceMonitoredResource create(
        String account, String instanceId, String region) {
      return new AwsEc2InstanceMonitoredResource(
          ImmutableMap.of(
              ResourceKeyConstants.AWS_ACCOUNT_KEY,
              account,
              ResourceKeyConstants.AWS_REGION_KEY,
              region,
              ResourceKeyConstants.AWS_INSTANCE_ID_KEY,
              instanceId));
    }

    static AwsEc2InstanceMonitoredResource create(Resource resource) {
      return new AwsEc2InstanceMonitoredResource(resource.getLabels());
    }

    private AwsEc2InstanceMonitoredResource(Map<String, String> labels) {
      this.labels = labels;
    }
  }

  /**
   * {@link MonitoredResource} for GCP GCE instance.
   *
   * @since 0.13
   * @deprecated use {@link Resource}.
   */
  @Immutable
  @Deprecated
  public static final class GcpGceInstanceMonitoredResource extends MonitoredResource {
    private final Map<String, String> labels;

    @Override
    public ResourceType getResourceType() {
      return ResourceType.GCP_GCE_INSTANCE;
    }

    /**
     * Returns the GCP account number for the instance.
     *
     * @return the GCP account number for the instance.
     * @since 0.13
     */
    public String getAccount() {
      return firstNonNull(labels.get(ResourceKeyConstants.GCP_ACCOUNT_ID_KEY), "");
    }

    /**
     * Returns the GCP GCE instance ID.
     *
     * @return the GCP GCE instance ID.
     * @since 0.13
     */
    public String getInstanceId() {
      return firstNonNull(labels.get(ResourceKeyConstants.GCP_INSTANCE_ID_KEY), "");
    }

    /**
     * Returns the GCP zone.
     *
     * @return the GCP zone.
     * @since 0.13
     */
    public String getZone() {
      return firstNonNull(labels.get(ResourceKeyConstants.GCP_ZONE_KEY), "");
    }

    /**
     * Returns a {@link GcpGceInstanceMonitoredResource}.
     *
     * @param account the GCP account number.
     * @param instanceId the GCP GCE instance ID.
     * @param zone the GCP zone.
     * @return a {@code GcpGceInstanceMonitoredResource}.
     * @since 0.15
     */
    public static GcpGceInstanceMonitoredResource create(
        String account, String instanceId, String zone) {
      return new GcpGceInstanceMonitoredResource(
          ImmutableMap.of(
              ResourceKeyConstants.GCP_ACCOUNT_ID_KEY,
              account,
              ResourceKeyConstants.GCP_ZONE_KEY,
              zone,
              ResourceKeyConstants.GCP_INSTANCE_ID_KEY,
              instanceId));
    }

    static GcpGceInstanceMonitoredResource create(Resource resource) {
      return new GcpGceInstanceMonitoredResource(resource.getLabels());
    }

    private GcpGceInstanceMonitoredResource(Map<String, String> labels) {
      this.labels = labels;
    }
  }

  /**
   * {@link MonitoredResource} for GCP GKE container.
   *
   * @since 0.13
   * @deprecated use {@link Resource}.
   */
  @Immutable
  @Deprecated
  public static final class GcpGkeContainerMonitoredResource extends MonitoredResource {
    private final Map<String, String> labels;

    @Override
    public ResourceType getResourceType() {
      return ResourceType.GCP_GKE_CONTAINER;
    }

    /**
     * Returns the GCP account number for the instance.
     *
     * @return the GCP account number for the instance.
     * @since 0.13
     */
    public String getAccount() {
      return firstNonNull(labels.get(ResourceKeyConstants.GCP_ACCOUNT_ID_KEY), "");
    }

    /**
     * Returns the GCP GKE cluster name.
     *
     * @return the GCP GKE cluster name.
     * @since 0.13
     */
    public String getClusterName() {
      return firstNonNull(labels.get(ResourceKeyConstants.K8S_CLUSTER_NAME_KEY), "");
    }

    /**
     * Returns the GCP GKE container name.
     *
     * @return the GCP GKE container name.
     * @since 0.13
     */
    public String getContainerName() {
      return firstNonNull(labels.get(ResourceKeyConstants.K8S_CONTAINER_NAME_KEY), "");
    }

    /**
     * Returns the GCP GKE namespace ID.
     *
     * @return the GCP GKE namespace ID.
     * @since 0.13
     */
    public String getNamespaceId() {
      return firstNonNull(labels.get(ResourceKeyConstants.K8S_NAMESPACE_NAME_KEY), "");
    }

    /**
     * Returns the GCP GKE instance ID.
     *
     * @return the GCP GKE instance ID.
     * @since 0.13
     */
    public String getInstanceId() {
      return firstNonNull(labels.get(ResourceKeyConstants.GCP_INSTANCE_ID_KEY), "");
    }

    /**
     * Returns the GCP GKE Pod ID.
     *
     * @return the GCP GKE Pod ID.
     * @since 0.13
     */
    public String getPodId() {
      return firstNonNull(labels.get(ResourceKeyConstants.K8S_POD_NAME_KEY), "");
    }

    /**
     * Returns the GCP zone.
     *
     * @return the GCP zone.
     * @since 0.13
     */
    public String getZone() {
      return firstNonNull(labels.get(ResourceKeyConstants.GCP_ZONE_KEY), "");
    }

    /**
     * Returns a {@link GcpGkeContainerMonitoredResource}.
     *
     * @param account the GCP account number.
     * @param clusterName the GCP GKE cluster name.
     * @param containerName the GCP GKE container name.
     * @param namespaceId the GCP GKE namespace ID.
     * @param instanceId the GCP GKE instance ID.
     * @param podId the GCP GKE Pod ID.
     * @param zone the GCP zone.
     * @return a {@code GcpGkeContainerMonitoredResource}.
     * @since 0.15
     */
    public static GcpGkeContainerMonitoredResource create(
        String account,
        String clusterName,
        String containerName,
        String namespaceId,
        String instanceId,
        String podId,
        String zone) {
      return new GcpGkeContainerMonitoredResource(
          ImmutableMap.<String, String>builder()
              .put(ResourceKeyConstants.GCP_ACCOUNT_ID_KEY, account)
              .put(ResourceKeyConstants.K8S_CLUSTER_NAME_KEY, clusterName)
              .put(ResourceKeyConstants.K8S_CONTAINER_NAME_KEY, containerName)
              .put(ResourceKeyConstants.K8S_NAMESPACE_NAME_KEY, namespaceId)
              .put(ResourceKeyConstants.GCP_INSTANCE_ID_KEY, instanceId)
              .put(ResourceKeyConstants.K8S_POD_NAME_KEY, podId)
              .put(ResourceKeyConstants.GCP_ZONE_KEY, zone)
              .build());
    }

    static GcpGkeContainerMonitoredResource create(Resource resource) {
      return new GcpGkeContainerMonitoredResource(resource.getLabels());
    }

    private GcpGkeContainerMonitoredResource(Map<String, String> labels) {
      this.labels = labels;
    }
  }
}
