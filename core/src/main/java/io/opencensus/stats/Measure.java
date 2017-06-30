/*
 * Copyright 2016, Google Inc.
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

package io.opencensus.stats;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Function;
import io.opencensus.internal.StringUtil;
import javax.annotation.concurrent.Immutable;

/**
 * Measure.
 */
@Immutable
public abstract class Measure {

  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(Function<DoubleMeasure, T> p0, Function<LongMeasure, T> p1);

  /**
   * Name of measure, as a {@code String}.
   */
  public abstract String getName();

  /**
   * Detailed description of the measure, used in documentation.
   */
  public abstract String getDescription();

  /**
   * The units in which {@link Measure} values are measured.
   *
   * <p>The grammar for a unit is as follows:
   *     Expression = Component { "." Component } { "/" Component } ;
   *     Component = [ PREFIX ] UNIT [ Annotation ] | Annotation | "1" ;
   *     Annotation = "{" NAME "}" ;
   * For example, string “MBy{transmitted}/ms” stands for megabytes per milliseconds, and the
   * annotation transmitted inside {} is just a comment of the unit.
   */
  public abstract String getUnit();

  @Immutable
  @AutoValue
  public abstract static class DoubleMeasure extends Measure {
    DoubleMeasure() {}

    /**
     * Constructs a new {@link DoubleMeasure}.
     */
    public static DoubleMeasure create(String name, String description, String unit) {
      // TODO(dpo): ensure that measure names are unique, and consider if there should be any
      // restricitons on name (e.g. size, characters).
      return new AutoValue_Measure_DoubleMeasure(name, description, unit);
    }

    @Override
    public <T> T match(Function<DoubleMeasure, T> p0, Function<LongMeasure, T> p1) {
      return p0.apply(this);
    }

    @Override
    public abstract String getName();

    @Override
    public abstract String getDescription();

    @Override
    public abstract String getUnit();
  }

  @Immutable
  @AutoValue
  // TODO: determine whether we want to support LongMeasure in V0.1
  public abstract static class LongMeasure extends Measure {
    LongMeasure() {}

    /**
     * Constructs a new {@link LongMeasure}.
     */
    public static LongMeasure create(String name, String description, String unit) {
      // TODO(dpo): ensure that measure names are unique, and consider if there should be any
      // restricitons on name (e.g. size, characters).
      return new AutoValue_Measure_LongMeasure(name, description, unit);
    }

    @Override
    public <T> T match(Function<DoubleMeasure, T> p0, Function<LongMeasure, T> p1) {
      return p1.apply(this);
    }

    @Override
    public abstract String getName();

    @Override
    public abstract String getDescription();

    @Override
    public abstract String getUnit();
  }
}
