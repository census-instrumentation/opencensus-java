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
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/** Utilities for detecting and creating {@link Node}. */
final class OcAgentNodeUtils {

  // The current version of the OpenCensus OC-Agent Exporter.
  @VisibleForTesting
  static final String OC_AGENT_EXPORTER_VERSION = "0.17.0-SNAPSHOT"; // CURRENT_OPENCENSUS_VERSION

  @VisibleForTesting static final String RESOURCE_TYPE_ATTRIBUTE_KEY = "OPENCENSUS_SOURCE_TYPE";
  @VisibleForTesting static final String RESOURCE_LABEL_ATTRIBUTE_KEY = "OPENCENSUS_SOURCE_LABELS";

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
        .putAllAttributes(getAttributeMap(RESOURCE))
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

  /*
   * Creates an attribute map with the given MonitoredResource.
   * If the given resource is not null, the attribute map contains exactly two entries:
   *
   * OPENCENSUS_SOURCE_TYPE:
   *   A string that describes the type of the resource prefixed by a domain namespace,
   *   e.g. “kubernetes.io/container”.
   * OPENCENSUS_SOURCE_LABELS:
   *   A comma-separated list of labels describing the source in more detail,
   *   e.g. “key1=val1,key2=val2”. The allowed character set is appropriately constrained.
   */
  // TODO: update the resource attributes once we have an agreement on the resource specs:
  // https://github.com/census-instrumentation/opencensus-specs/pull/162.
  @VisibleForTesting
  static Map<String, String> getAttributeMap(@Nullable MonitoredResource resource) {
    if (resource == null) {
      return Collections.emptyMap();
    } else {
      Map<String, String> resourceAttributes = new HashMap<String, String>();
      resourceAttributes.put(RESOURCE_TYPE_ATTRIBUTE_KEY, resource.getResourceType().name());
      resourceAttributes.put(RESOURCE_LABEL_ATTRIBUTE_KEY, getConcatenatedResourceLabels(resource));
      return resourceAttributes;
    }
  }

  // Encodes the attributes of MonitoredResource into a comma-separated list of labels.
  // For example "aws_account=account1,instance_id=instance1,region=us-east-2".
  private static String getConcatenatedResourceLabels(MonitoredResource resource) {
    StringBuilder resourceLabels = new StringBuilder();
    if (resource instanceof AwsEc2InstanceMonitoredResource) {
      AwsEc2InstanceMonitoredResource awsEc2Resource = (AwsEc2InstanceMonitoredResource) resource;
      putIntoBuilderIfHasValue(resourceLabels, "aws_account", awsEc2Resource.getAccount());
      putIntoBuilderIfHasValue(resourceLabels, "instance_id", awsEc2Resource.getInstanceId());
      putIntoBuilderIfHasValue(resourceLabels, "region", awsEc2Resource.getRegion());
    } else if (resource instanceof GcpGceInstanceMonitoredResource) {
      GcpGceInstanceMonitoredResource gceResource = (GcpGceInstanceMonitoredResource) resource;
      putIntoBuilderIfHasValue(resourceLabels, "gcp_account", gceResource.getAccount());
      putIntoBuilderIfHasValue(resourceLabels, "instance_id", gceResource.getInstanceId());
      putIntoBuilderIfHasValue(resourceLabels, "zone", gceResource.getZone());
    } else if (resource instanceof GcpGkeContainerMonitoredResource) {
      GcpGkeContainerMonitoredResource gkeResource = (GcpGkeContainerMonitoredResource) resource;
      putIntoBuilderIfHasValue(resourceLabels, "gcp_account", gkeResource.getAccount());
      putIntoBuilderIfHasValue(resourceLabels, "instance_id", gkeResource.getInstanceId());
      putIntoBuilderIfHasValue(resourceLabels, "location", gkeResource.getZone());
      putIntoBuilderIfHasValue(resourceLabels, "namespace_name", gkeResource.getNamespaceId());
      putIntoBuilderIfHasValue(resourceLabels, "cluster_name", gkeResource.getClusterName());
      putIntoBuilderIfHasValue(resourceLabels, "container_name", gkeResource.getContainerName());
      putIntoBuilderIfHasValue(resourceLabels, "pod_name", gkeResource.getPodId());
    }
    return resourceLabels.toString();
  }

  // If the given resourceValue is not empty, encodes resourceKey and resourceValue as
  // "resourceKey:resourceValue" and puts it into the given StringBuilder. Otherwise skip the value.
  private static void putIntoBuilderIfHasValue(
      StringBuilder builder, String resourceKey, String resourceValue) {
    if (resourceValue.isEmpty()) {
      return;
    }
    if (!(builder.length() == 0)) {
      // Appends the comma separator to the front, if the StringBuilder already has entries.
      builder.append(',');
    }
    builder.append(resourceKey);
    builder.append('=');
    builder.append(resourceValue);
  }

  private OcAgentNodeUtils() {}
}
