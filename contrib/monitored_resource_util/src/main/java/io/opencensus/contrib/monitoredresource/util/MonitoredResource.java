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

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link MonitoredResource} represents an auto-detected monitored resource used by application for
 * exporting stats. It has a {@code ResourceType} associated with a mapping from resource labels to
 * values.
 *
 * @since 0.13
 */
@Immutable
public abstract class MonitoredResource {

  MonitoredResource() {}

  /**
   * Returns the {@link ResourceType} of this {@link MonitoredResource}.
   *
   * @return the {@code ResourceType}.
   * @since 0.13
   */
  public abstract ResourceType getResourceType();

  /*
   * Returns the first of two given parameters that is not null, if either is, or otherwise
   * throws a NullPointerException.
   */
  private static <T> T firstNonNull(@Nullable T first, @Nullable T second) {
    if (first != null) {
      return first;
    }
    if (second != null) {
      return second;
    }
    throw new NullPointerException("Both parameters are null");
  }

  // TODO(songya): consider using a tagged union match() approach (that will introduce
  // dependency on opencensus-api).

  /**
   * {@link MonitoredResource} for AWS EC2 instance.
   *
   * @since 0.13
   */
  @Immutable
  @AutoValue
  public abstract static class AwsEc2MonitoredResource extends MonitoredResource {

    private static final String AWS_ACCOUNT =
        firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("accountId"), "");
    private static final String AWS_INSTANCE_ID =
        firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("instanceId"), "");
    private static final String AWS_REGION =
        firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("region"), "");

    @Override
    public ResourceType getResourceType() {
      return ResourceType.AwsEc2Instance;
    }

    /**
     * Returns the AWS account ID.
     *
     * @return the AWS account ID.
     * @since 0.13
     */
    public abstract String getAccount();

    /**
     * Returns the AWS EC2 instance ID.
     *
     * @return the AWS EC2 instance ID.
     * @since 0.13
     */
    public abstract String getInstanceId();

    /**
     * Returns the AWS region.
     *
     * @return the AWS region.
     * @since 0.13
     */
    public abstract String getRegion();

    static AwsEc2MonitoredResource create() {
      return new AutoValue_MonitoredResource_AwsEc2MonitoredResource(
          AWS_ACCOUNT, AWS_INSTANCE_ID, AWS_REGION);
    }
  }

  /**
   * {@link MonitoredResource} for GCP GCE instance.
   *
   * @since 0.13
   */
  @Immutable
  @AutoValue
  public abstract static class GcpGceInstanceMonitoredResource extends MonitoredResource {

    private static final String GCP_INSTANCE_ID =
        firstNonNull(GcpMetadataConfig.getInstanceId(), "");
    private static final String GCP_ZONE = firstNonNull(GcpMetadataConfig.getZone(), "");

    @Override
    public ResourceType getResourceType() {
      return ResourceType.GcpGceInstance;
    }

    /**
     * Returns the GCP GCE instance ID.
     *
     * @return the GCP GCE instance ID.
     * @since 0.13
     */
    public abstract String getInstanceId();

    /**
     * Returns the GCP zone.
     *
     * @return the GCP zone.
     * @since 0.13
     */
    public abstract String getZone();

    static GcpGceInstanceMonitoredResource create() {
      return new AutoValue_MonitoredResource_GcpGceInstanceMonitoredResource(
          GCP_INSTANCE_ID, GCP_ZONE);
    }
  }

  /**
   * {@link MonitoredResource} for GCP GKE container.
   *
   * @since 0.13
   */
  @Immutable
  @AutoValue
  public abstract static class GcpGkeContainerMonitoredResource extends MonitoredResource {

    private static final String GCP_CLUSTER_NAME =
        firstNonNull(GcpMetadataConfig.getClusterName(), "");
    private static final String GCP_CONTAINER_NAME =
        firstNonNull(System.getenv("CONTAINER_NAME"), "");
    private static final String GCP_NAMESPACE_ID = firstNonNull(System.getenv("NAMESPACE"), "");
    private static final String GCP_INSTANCE_ID =
        firstNonNull(GcpMetadataConfig.getInstanceId(), "");
    private static final String GCP_POD_ID = firstNonNull(System.getenv("HOSTNAME"), "");
    private static final String GCP_ZONE = firstNonNull(GcpMetadataConfig.getZone(), "");

    @Override
    public ResourceType getResourceType() {
      return ResourceType.GcpGkeContainer;
    }

    /**
     * Returns the GCP GKE cluster name.
     *
     * @return the GCP GKE cluster name.
     * @since 0.13
     */
    public abstract String getClusterName();

    /**
     * Returns the GCP GKE container name.
     *
     * @return the GCP GKE container name.
     * @since 0.13
     */
    public abstract String getContainerName();

    /**
     * Returns the GCP GKE namespace ID.
     *
     * @return the GCP GKE namespace ID.
     * @since 0.13
     */
    public abstract String getNamespaceId();

    /**
     * Returns the GCP GKE instance ID.
     *
     * @return the GCP GKE instance ID.
     * @since 0.13
     */
    public abstract String getInstanceId();

    /**
     * Returns the GCP GKE Pod ID.
     *
     * @return the GCP GKE Pod ID.
     * @since 0.13
     */
    public abstract String getPodId();

    /**
     * Returns the GCP zone.
     *
     * @return the GCP zone.
     * @since 0.13
     */
    public abstract String getZone();

    static GcpGkeContainerMonitoredResource create() {
      return new AutoValue_MonitoredResource_GcpGkeContainerMonitoredResource(
          GCP_CLUSTER_NAME,
          GCP_CONTAINER_NAME,
          GCP_NAMESPACE_ID,
          GCP_INSTANCE_ID,
          GCP_POD_ID,
          GCP_ZONE);
    }
  }
}
