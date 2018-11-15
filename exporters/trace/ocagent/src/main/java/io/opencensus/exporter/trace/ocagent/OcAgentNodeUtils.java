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

package io.opencensus.exporter.trace.ocagent;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.OpenCensusLibraryInformation;
import io.opencensus.common.Timestamp;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.AwsEc2InstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGceInstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGkeContainerMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResourceUtils;
import io.opencensus.proto.agent.common.v1.LibraryInfo;
import io.opencensus.proto.agent.common.v1.LibraryInfo.Language;
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.common.v1.ProcessIdentifier;
import io.opencensus.proto.agent.common.v1.ServiceInfo;
import io.opencensus.proto.resource.v1.Resource;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import javax.annotation.Nullable;

/** Utilities for detecting and creating {@link Node}. */
final class OcAgentNodeUtils {

  // The current version of the OpenCensus OC-Agent Exporter.
  @VisibleForTesting
  static final String OC_AGENT_EXPORTER_VERSION = "0.18.0-SNAPSHOT"; // CURRENT_OPENCENSUS_VERSION

  @Nullable
  private static final MonitoredResource RESOURCE = MonitoredResourceUtils.getDefaultResource();

  // Creates a Node with information from the OpenCensus library and environment variables.
  static Node getNodeInfo(String serviceName) {
    String jvmName = ManagementFactory.getRuntimeMXBean().getName();
    Timestamp censusTimestamp = Timestamp.fromMillis(System.currentTimeMillis());
    return Node.newBuilder()
        .setIdentifier(getProcessIdentifier(jvmName, censusTimestamp))
        .setLibraryInfo(getLibraryInfo(OpenCensusLibraryInformation.VERSION))
        .setServiceInfo(getServiceInfo(serviceName))
        .build();
  }

  // Creates process identifier with the given JVM name and start time.
  @VisibleForTesting
  static ProcessIdentifier getProcessIdentifier(String jvmName, Timestamp censusTimestamp) {
    String hostname;
    int pid;
    // jvmName should be something like '<pid>@<hostname>', at least in Oracle and OpenJdk JVMs
    int delimiterIndex = jvmName.indexOf('@');
    if (delimiterIndex < 1) {
      // Not the expected format, generate a random number.
      try {
        hostname = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        hostname = "localhost";
      }
      // Generate a random number as the PID.
      pid = new SecureRandom().nextInt();
    } else {
      hostname = jvmName.substring(delimiterIndex + 1, jvmName.length());
      try {
        pid = Integer.parseInt(jvmName.substring(0, delimiterIndex));
      } catch (NumberFormatException e) {
        // Generate a random number as the PID if format is unexpected.
        pid = new SecureRandom().nextInt();
      }
    }

    return ProcessIdentifier.newBuilder()
        .setHostName(hostname)
        .setPid(pid)
        .setStartTimestamp(TraceProtoUtils.toTimestampProto(censusTimestamp))
        .build();
  }

  // Creates library info with the given OpenCensus Java version.
  @VisibleForTesting
  static LibraryInfo getLibraryInfo(String currentOcJavaVersion) {
    return LibraryInfo.newBuilder()
        .setLanguage(Language.JAVA)
        .setCoreLibraryVersion(currentOcJavaVersion)
        .setExporterVersion(OC_AGENT_EXPORTER_VERSION)
        .build();
  }

  // Creates service info with the given service name.
  @VisibleForTesting
  static ServiceInfo getServiceInfo(String serviceName) {
    return ServiceInfo.newBuilder().setName(serviceName).build();
  }

  @Nullable
  static Resource getDefaultResourceProto() {
    return toResourceProto(RESOURCE);
  }

  // Converts a MonitoredResource to a Resource proto.
  // TODO: update this method to use the Java Resource API once auto-detection is implemented.
  @Nullable
  @VisibleForTesting
  static Resource toResourceProto(@Nullable MonitoredResource resource) {
    if (resource == null) {
      return null;
    } else {
      Resource.Builder resourceProtoBuilder = Resource.newBuilder();
      resourceProtoBuilder.setType(resource.getResourceType().name());
      putResourceLabels(resource, resourceProtoBuilder);
      return resourceProtoBuilder.build();
    }
  }

  // Puts the attributes of MonitoredResource to ResourceProto.
  private static void putResourceLabels(
      MonitoredResource resource, Resource.Builder resourceProtoBuilder) {
    if (resource instanceof AwsEc2InstanceMonitoredResource) {
      AwsEc2InstanceMonitoredResource awsEc2Resource = (AwsEc2InstanceMonitoredResource) resource;
      putIntoBuilderIfHasValue(resourceProtoBuilder, "aws_account", awsEc2Resource.getAccount());
      putIntoBuilderIfHasValue(resourceProtoBuilder, "instance_id", awsEc2Resource.getInstanceId());
      putIntoBuilderIfHasValue(resourceProtoBuilder, "region", awsEc2Resource.getRegion());
    } else if (resource instanceof GcpGceInstanceMonitoredResource) {
      GcpGceInstanceMonitoredResource gceResource = (GcpGceInstanceMonitoredResource) resource;
      putIntoBuilderIfHasValue(resourceProtoBuilder, "gcp_account", gceResource.getAccount());
      putIntoBuilderIfHasValue(resourceProtoBuilder, "instance_id", gceResource.getInstanceId());
      putIntoBuilderIfHasValue(resourceProtoBuilder, "zone", gceResource.getZone());
    } else if (resource instanceof GcpGkeContainerMonitoredResource) {
      GcpGkeContainerMonitoredResource gkeResource = (GcpGkeContainerMonitoredResource) resource;
      putIntoBuilderIfHasValue(resourceProtoBuilder, "gcp_account", gkeResource.getAccount());
      putIntoBuilderIfHasValue(resourceProtoBuilder, "instance_id", gkeResource.getInstanceId());
      putIntoBuilderIfHasValue(resourceProtoBuilder, "location", gkeResource.getZone());
      putIntoBuilderIfHasValue(
          resourceProtoBuilder, "namespace_name", gkeResource.getNamespaceId());
      putIntoBuilderIfHasValue(resourceProtoBuilder, "cluster_name", gkeResource.getClusterName());
      putIntoBuilderIfHasValue(
          resourceProtoBuilder, "container_name", gkeResource.getContainerName());
      putIntoBuilderIfHasValue(resourceProtoBuilder, "pod_name", gkeResource.getPodId());
    }
  }

  // If the given resourceValue is not empty, put it into the given resourceProtoBuilder.
  // Otherwise skip the value.
  private static void putIntoBuilderIfHasValue(
      Resource.Builder resourceProtoBuilder, String resourceKey, String resourceValue) {
    if (resourceValue.isEmpty()) {
      return;
    }
    resourceProtoBuilder.putLabels(resourceKey, resourceValue);
  }

  private OcAgentNodeUtils() {}
}
