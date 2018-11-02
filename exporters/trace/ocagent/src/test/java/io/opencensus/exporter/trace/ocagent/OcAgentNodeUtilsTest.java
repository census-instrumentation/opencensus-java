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

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.exporter.trace.ocagent.OcAgentNodeUtils.OC_AGENT_EXPORTER_VERSION;
import static io.opencensus.exporter.trace.ocagent.OcAgentNodeUtils.RESOURCE_LABEL_ATTRIBUTE_KEY;
import static io.opencensus.exporter.trace.ocagent.OcAgentNodeUtils.RESOURCE_TYPE_ATTRIBUTE_KEY;

import io.opencensus.common.Timestamp;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.AwsEc2InstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGceInstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGkeContainerMonitoredResource;
import io.opencensus.proto.agent.common.v1.LibraryInfo;
import io.opencensus.proto.agent.common.v1.LibraryInfo.Language;
import io.opencensus.proto.agent.common.v1.ProcessIdentifier;
import io.opencensus.proto.agent.common.v1.ServiceInfo;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link OcAgentNodeUtils}. */
@RunWith(JUnit4.class)
public class OcAgentNodeUtilsTest {

  private static final AwsEc2InstanceMonitoredResource AWS_RESOURCE =
      AwsEc2InstanceMonitoredResource.create("account1", "instance1", "us-east-2");
  private static final GcpGceInstanceMonitoredResource GCE_RESOURCE =
      GcpGceInstanceMonitoredResource.create("account2", "instance2", "us-west2");
  private static final GcpGkeContainerMonitoredResource GKE_RESOURCE =
      GcpGkeContainerMonitoredResource.create(
          "account3", "cluster", "container", "", "instance3", "", "us-west4");

  @Test
  public void testConstants() {
    assertThat(OC_AGENT_EXPORTER_VERSION).isEqualTo("0.17.1-SNAPSHOT");
    assertThat(RESOURCE_TYPE_ATTRIBUTE_KEY).isEqualTo("OPENCENSUS_SOURCE_TYPE");
    assertThat(RESOURCE_LABEL_ATTRIBUTE_KEY).isEqualTo("OPENCENSUS_SOURCE_LABELS");
  }

  @Test
  public void getProcessIdentifier() {
    String jvmName = "54321@my.org";
    Timestamp timestamp = Timestamp.create(10, 20);
    ProcessIdentifier processIdentifier = OcAgentNodeUtils.getProcessIdentifier(jvmName, timestamp);
    assertThat(processIdentifier.getHostName()).isEqualTo("my.org");
    assertThat(processIdentifier.getPid()).isEqualTo(54321);
    assertThat(processIdentifier.getStartTimestamp())
        .isEqualTo(com.google.protobuf.Timestamp.newBuilder().setSeconds(10).setNanos(20).build());
  }

  @Test
  public void getLibraryInfo() {
    String currentOcJavaVersion = "0.16.0";
    LibraryInfo libraryInfo = OcAgentNodeUtils.getLibraryInfo(currentOcJavaVersion);
    assertThat(libraryInfo.getLanguage()).isEqualTo(Language.JAVA);
    assertThat(libraryInfo.getCoreLibraryVersion()).isEqualTo(currentOcJavaVersion);
    assertThat(libraryInfo.getExporterVersion()).isEqualTo(OC_AGENT_EXPORTER_VERSION);
  }

  @Test
  public void getServiceInfo() {
    String serviceName = "my-service";
    ServiceInfo serviceInfo = OcAgentNodeUtils.getServiceInfo(serviceName);
    assertThat(serviceInfo.getName()).isEqualTo(serviceName);
  }

  @Test
  public void getAttributeMap_Null() {
    Map<String, String> attributeMap = OcAgentNodeUtils.getAttributeMap(null);
    assertThat(attributeMap).isEmpty();
  }

  @Test
  public void getAttributeMap_AwsEc2Resource() {
    Map<String, String> attributeMap = OcAgentNodeUtils.getAttributeMap(AWS_RESOURCE);
    assertThat(attributeMap)
        .containsExactly(
            RESOURCE_TYPE_ATTRIBUTE_KEY,
            "AWS_EC2_INSTANCE",
            RESOURCE_LABEL_ATTRIBUTE_KEY,
            "aws_account=account1,instance_id=instance1,region=us-east-2");
  }

  @Test
  public void getAttributeMap_GceResource() {
    Map<String, String> attributeMap = OcAgentNodeUtils.getAttributeMap(GCE_RESOURCE);
    assertThat(attributeMap)
        .containsExactly(
            RESOURCE_TYPE_ATTRIBUTE_KEY,
            "GCP_GCE_INSTANCE",
            RESOURCE_LABEL_ATTRIBUTE_KEY,
            "gcp_account=account2,instance_id=instance2,zone=us-west2");
  }

  @Test
  public void getAttributeMap_GkeResource() {
    Map<String, String> attributeMap = OcAgentNodeUtils.getAttributeMap(GKE_RESOURCE);
    assertThat(attributeMap)
        .containsExactly(
            RESOURCE_TYPE_ATTRIBUTE_KEY,
            "GCP_GKE_CONTAINER",
            RESOURCE_LABEL_ATTRIBUTE_KEY,
            "gcp_account=account3,instance_id=instance3,location=us-west4,"
                + "cluster_name=cluster,container_name=container");
  }
}
