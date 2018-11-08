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

package io.opencensus.implcore.resource;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.resource.Resource;
import io.opencensus.resource.ResourceDetector;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ResourceDetectorImpl}. */
@RunWith(JUnit4.class)
public class ResourceDetectorImplTest {
  private final ResourceDetector resourceDetector = new ResourceDetectorImpl();
  private Resource resource1;
  private Resource resource2;

  @Before
  public void setUp() {
    Map<String, String> labelMap1 = new HashMap<String, String>();
    labelMap1.put("a", "1");
    labelMap1.put("b", "2");
    Map<String, String> labelMap2 = new HashMap<String, String>();
    labelMap2.put("a", "1");
    labelMap2.put("b", "3");
    labelMap2.put("c", "4");
    resource1 = Resource.create("t1", labelMap1);
    resource2 = Resource.create("t2", labelMap2);
  }

  @Test
  public void testMultiDetector() {
    Map<String, String> expectedLabelMap = new HashMap<String, String>();
    expectedLabelMap.put("a", "1");
    expectedLabelMap.put("b", "2");
    expectedLabelMap.put("c", "4");

    Resource resource = resourceDetector.multiDetector(Arrays.asList(resource1, resource2));
    assertThat(resource.getType()).isEqualTo("t1");
    assertThat(resource.getLabels()).isEqualTo(expectedLabelMap);
  }

  @Test
  public void testMultiDetector_SingletonList() {
    Map<String, String> expectedLabelMap = new HashMap<String, String>();
    expectedLabelMap.put("a", "1");
    expectedLabelMap.put("b", "2");

    Resource resource = resourceDetector.multiDetector(Collections.singletonList(resource1));
    assertThat(resource.getType()).isEqualTo("t1");
    assertThat(resource.getLabels()).isEqualTo(expectedLabelMap);
  }

  @Test
  public void testMultiDetector_Resource1_Null() {
    Map<String, String> expectedLabelMap = new HashMap<String, String>();
    expectedLabelMap.put("a", "1");
    expectedLabelMap.put("b", "3");
    expectedLabelMap.put("c", "4");

    Resource resource = resourceDetector.multiDetector(Arrays.asList(null, resource2));
    assertThat(resource.getType()).isEqualTo("t2");
    assertThat(resource.getLabels()).isEqualTo(expectedLabelMap);
  }

  @Test
  public void testMultiDetector_Resource2_Null() {
    Map<String, String> expectedLabelMap = new HashMap<String, String>();
    expectedLabelMap.put("a", "1");
    expectedLabelMap.put("b", "2");

    Resource resource = resourceDetector.multiDetector(Arrays.asList(resource1, null));
    assertThat(resource.getType()).isEqualTo("t1");
    assertThat(resource.getLabels()).isEqualTo(expectedLabelMap);
  }
}
