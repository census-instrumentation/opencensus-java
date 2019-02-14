/*
 * Copyright 2016-17, OpenCensus Authors
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

package io.opencensus.implcore.stats;

import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.MeasureMap;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.unsafe.ContextUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Implementation of {@link MeasureMap}. */
final class MeasureMapImpl extends MeasureMap {
  private static final Logger logger = Logger.getLogger(MeasureMapImpl.class.getName());

  private final StatsManager statsManager;
  private final MeasureMapInternal.Builder builder = MeasureMapInternal.builder();
  private volatile boolean hasUnsupportedValues;

  static MeasureMapImpl create(StatsManager statsManager) {
    return new MeasureMapImpl(statsManager);
  }

  private MeasureMapImpl(StatsManager statsManager) {
    this.statsManager = statsManager;
  }

  @Override
  public MeasureMapImpl put(MeasureDouble measure, double value) {
    if (value < 0) {
      hasUnsupportedValues = true;
    }
    builder.put(measure, value);
    return this;
  }

  @Override
  public MeasureMapImpl put(MeasureLong measure, long value) {
    if (value < 0) {
      hasUnsupportedValues = true;
    }
    builder.put(measure, value);
    return this;
  }

  @Override
  public MeasureMap putAttachment(String key, String value) {
    builder.putAttachment(key, value);
    return this;
  }

  @Override
  public void record() {
    // Use the context key directly, to avoid depending on the tags implementation.
    record(ContextUtils.TAG_CONTEXT_KEY.get());
  }

  @Override
  public void record(TagContext tags) {
    recordInternal(RecordUtils.getTagMap(tags));
  }

  @Override
  public void recordWithTags(List<Tag> tags) {
    Map<TagKey, TagValue> allTags =
        new HashMap<TagKey, TagValue>(RecordUtils.getTagMap(ContextUtils.TAG_CONTEXT_KEY.get()));
    for (Tag tag : tags) {
      allTags.put(tag.getKey(), tag.getValue());
    }
    recordInternal(allTags);
  }

  private void recordInternal(Map<TagKey, TagValue> tags) {
    if (hasUnsupportedValues) {
      // drop all the recorded values
      logger.log(Level.WARNING, "Dropping values, value to record must be non-negative.");
      return;
    }
    statsManager.record(tags, builder.build());
  }
}
