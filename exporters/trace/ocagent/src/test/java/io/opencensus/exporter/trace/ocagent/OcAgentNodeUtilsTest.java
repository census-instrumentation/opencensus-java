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

import io.opencensus.common.Timestamp;
import io.opencensus.proto.agent.common.v1.LibraryInfo;
import io.opencensus.proto.agent.common.v1.LibraryInfo.Language;
import io.opencensus.proto.agent.common.v1.ProcessIdentifier;
import io.opencensus.proto.agent.common.v1.ServiceInfo;
import io.opencensus.proto.resource.v1.Resource;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link OcAgentNodeUtils}. */
@RunWith(JUnit4.class)
public class OcAgentNodeUtilsTest {

  private static final io.opencensus.resource.Resource CUSTOM_RESOURCE =
      io.opencensus.resource.Resource.create(
          "some environment", Collections.singletonMap("k1", "v1"));

  @Test
  public void testConstants() {
    assertThat(OC_AGENT_EXPORTER_VERSION).isEqualTo("0.18.0");
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
  public void toResourceProto_Null() {
    Resource resourceProto = OcAgentNodeUtils.toResourceProto(null);
    assertThat(resourceProto).isNull();
  }

  @Test
  public void toResourceProto() {
    Resource resourceProto = OcAgentNodeUtils.toResourceProto(CUSTOM_RESOURCE);
    assertThat(resourceProto.getType()).isEqualTo("some environment");
    assertThat(resourceProto.getLabelsMap()).containsExactly("k1", "v1");
  }
}
