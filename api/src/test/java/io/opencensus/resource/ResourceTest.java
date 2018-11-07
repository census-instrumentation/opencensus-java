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

package io.opencensus.resource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Resource}. */
@RunWith(JUnit4.class)
public class ResourceTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testMaxLength() {
    assertThat(Resource.MAX_LENGTH).isEqualTo(255);
  }

  @Test
  public void testParseResourceType() {
    String rawEnvType = "k8s.io/container";
    String envType = Resource.parseResourceType(rawEnvType);
    assertThat(envType).isNotNull();
    assertEquals(rawEnvType, envType);
  }

  @Test
  public void testParseResourceType_Null() {
    String envType = Resource.parseResourceType(null);
    assertThat(envType).isNull();
  }

  @Test
  public void testParseResourceType_DisallowUnprintableChars() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Type should be a ASCII string with a length greater than 0 and not exceed "
            + "255 characters.");
    Resource.parseResourceType("\2ab\3cd");
  }

  @Test
  public void testParseResourceType_DisallowTypeNameOverMaxLength() {
    char[] chars = new char[Resource.MAX_LENGTH + 1];
    Arrays.fill(chars, 'k');
    String type = new String(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Type should be a ASCII string with a length greater than 0 and not exceed "
            + "255 characters.");
    Resource.parseResourceType(type);
  }

  @Test
  public void testParseResourceLabels() {
    Map<String, String> expectedLabelsMap = new HashMap<String, String>();
    expectedLabelsMap.put("k8s.io/pod/name", "pod-xyz-123");
    expectedLabelsMap.put("k8s.io/container/name", "c1");
    expectedLabelsMap.put("k8s.io/namespace/name", "default");

    String rawEnvLabels =
        "k8s.io/pod/name=\"pod-xyz-123\",k8s.io/container/name=\"c1\","
            + "k8s.io/namespace/name=\"default\"";
    Map<String, String> labelsMap = Resource.parseResourceLabels(rawEnvLabels);
    assertEquals(expectedLabelsMap, labelsMap);
    assertEquals(3, labelsMap.size());
  }

  @Test
  public void testParseResourceLabels_WithSpaces() {
    Map<String, String> expectedLabelsMap = new HashMap<String, String>();
    expectedLabelsMap.put("example.org/test-1", "test $ \\\"");
    expectedLabelsMap.put("Abc", "Def");

    String rawEnvLabels = "example.org/test-1=\"test $ \\\"\" ,  Abc=\"Def\"";
    Map<String, String> labelsMap = Resource.parseResourceLabels(rawEnvLabels);
    assertEquals(expectedLabelsMap, labelsMap);
    assertEquals(2, labelsMap.size());
  }

  @Test
  public void testParseResourceLabels_SingleKey() {
    Map<String, String> expectedLabelsMap = new HashMap<String, String>();
    expectedLabelsMap.put("single", "key");

    String rawEnvLabels = "single=\"key\"";
    Map<String, String> labelsMap = Resource.parseResourceLabels(rawEnvLabels);
    assertEquals(1, labelsMap.size());
    assertEquals(expectedLabelsMap, labelsMap);
  }

  @Test
  public void testParseResourceLabels_Null() {
    Map<String, String> labelsMap = Resource.parseResourceLabels(null);
    assertThat(labelsMap).isNotNull();
    assertThat(labelsMap).isEmpty();
  }

  @Test
  public void testParseResourceLabels_DisallowUnprintableChars() {
    String rawEnvLabels = "example.org/test-1=\2ab\3cd";
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Label value should be a ASCII string with a length not exceed 255 characters.");
    Resource.parseResourceLabels(rawEnvLabels);
  }

  @Test
  public void testParseResourceLabels_DisallowLabelKeyOverMaxLength() {
    char[] chars = new char[Resource.MAX_LENGTH + 1];
    Arrays.fill(chars, 'k');
    String rawEnvLabels = new String(chars) + "=test-1";
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Label key should be a ASCII string with a length greater than 0 and not exceed "
            + "255 characters.");
    Resource.parseResourceLabels(rawEnvLabels);
  }

  @Test
  public void testParseResourceLabels_DisallowLabelValueOverMaxLength() {
    char[] chars = new char[Resource.MAX_LENGTH + 1];
    Arrays.fill(chars, 'k');
    String rawEnvLabels = "example.org/test-1=" + new String(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        "Label value should be a ASCII string with a length not exceed 255 characters.");
    Resource.parseResourceLabels(rawEnvLabels);
  }

  @Test
  public void create() {
    Map<String, String> labelMap = new HashMap<String, String>();
    labelMap.put("a", "1");
    labelMap.put("b", "2");
    Resource resource = Resource.create("t1", labelMap);
    assertThat(resource.getType()).isNotNull();
    assertThat(resource.getType()).isEqualTo("t1");
    assertThat(resource.getLabels()).isNotNull();
    assertThat(resource.getLabels().size()).isEqualTo(2);
    assertThat(resource.getLabels()).isEqualTo(labelMap);

    Resource resource1 = Resource.create(null, Collections.<String, String>emptyMap());
    assertThat(resource1.getType()).isNull();
    assertThat(resource1.getLabels()).isNotNull();
    assertThat(resource1.getLabels()).isEmpty();
  }

  @Test
  public void testResourceEquals() {
    Map<String, String> labelMap1 = new HashMap<String, String>();
    labelMap1.put("a", "1");
    labelMap1.put("b", "2");
    Map<String, String> labelMap2 = new HashMap<String, String>();
    labelMap2.put("a", "1");
    labelMap2.put("b", "3");
    labelMap2.put("c", "4");
    new EqualsTester()
        .addEqualityGroup(Resource.create("t1", labelMap1), Resource.create("t1", labelMap1))
        .addEqualityGroup(Resource.create("t2", labelMap2))
        .testEquals();
  }
}
