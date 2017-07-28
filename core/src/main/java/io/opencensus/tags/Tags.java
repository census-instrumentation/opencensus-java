/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.tags;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.internal.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class for accessing the default {@link TagsComponent}. */
public final class Tags {
  private static final Logger logger = Logger.getLogger(Tags.class.getName());

  private static final TagsComponent tagsComponent =
      loadTagsComponent(Provider.getCorrectClassLoader(TagsComponent.class));

  private Tags() {}

  /**
   * Returns the default {@code TagContextFactory}.
   *
   * @return the default {@code TagContextFactory}.
   */
  public static TagContextFactory getTagContextFactory() {
    return tagsComponent.getTagContextFactory();
  }

  // Any provider that may be used for TagsComponent can be added here.
  @VisibleForTesting
  static TagsComponent loadTagsComponent(ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName("io.opencensus.impl.tags.TagsComponentImpl", true, classLoader),
          TagsComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load full implementation for TagsComponent, now trying to load lite "
              + "implementation.",
          e);
    }
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName("io.opencensus.impl.tags.TagsComponentImplLite", true, classLoader),
          TagsComponent.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load lite implementation for TagsComponent, now using "
              + "default implementation for TagsComponent.",
          e);
    }
    return TagsComponent.getNoopTagsComponent();
  }
}
