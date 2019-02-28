/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.stats;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Function;
import io.opencensus.stats.AggregationData.DistributionData.Exemplar;
import javax.annotation.concurrent.Immutable;

/**
 * The value of {@link Exemplar} attachment.
 *
 * @since 0.20
 */
public abstract class AttachmentValue {

  /**
   * Applies the given match function to the underlying data type.
   *
   * @since 0.20
   */
  public abstract <T> T match(
      Function<? super AttachmentValueString, T> p0,
      Function<? super AttachmentValue, T> defaultFunction);

  /**
   * String {@link AttachmentValue}.
   *
   * @since 0.20
   */
  @AutoValue
  @Immutable
  public abstract static class AttachmentValueString extends AttachmentValue {

    AttachmentValueString() {}

    /**
     * Returns the string attachment value.
     *
     * @return the string attachment value.
     * @since 0.20
     */
    public abstract String getValue();

    /**
     * Creates an {@link AttachmentValueString}.
     *
     * @param value the string value.
     * @return an {@code AttachmentValueString}.
     * @since 0.20
     */
    public static AttachmentValueString create(String value) {
      return new AutoValue_AttachmentValue_AttachmentValueString(value);
    }

    @Override
    public final <T> T match(
        Function<? super AttachmentValueString, T> p0,
        Function<? super AttachmentValue, T> defaultFunction) {
      return p0.apply(this);
    }
  }
}
