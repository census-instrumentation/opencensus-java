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

import io.opencensus.resource.Resource;
import io.opencensus.resource.ResourceDetector;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Implementation of {@link ResourceDetector}. */
public final class ResourceDetectorImpl implements ResourceDetector {

  @Override
  /*@Nullable*/
  public Resource multiDetector(List</*@Nullable*/ Resource> resources) {
    Resource currentResource = null;
    for (Resource resource : resources) {
      currentResource = merge(currentResource, resource);
    }
    return currentResource;
  }

  /**
   * Returns a new, merged {@link Resource} by merging two resources. In case of a collision, first
   * resource takes precedence.
   */
  /*@Nullable*/
  private static Resource merge(
      /*@Nullable*/ Resource resource, /*@Nullable*/ Resource otherResource) {
    if (otherResource == null) {
      return resource;
    }
    if (resource == null) {
      return otherResource;
    }

    String mergedType = resource.getType() != null ? resource.getType() : otherResource.getType();
    Map<String, String> mergedLabelMap =
        new LinkedHashMap<String, String>(otherResource.getLabels());

    // Labels from resource overwrite labels from otherResource.
    for (Entry<String, String> entry : resource.getLabels().entrySet()) {
      mergedLabelMap.put(entry.getKey(), entry.getValue());
    }
    return Resource.create(mergedType, mergedLabelMap);
  }
}
