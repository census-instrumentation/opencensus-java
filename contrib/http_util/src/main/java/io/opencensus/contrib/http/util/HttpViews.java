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

package io.opencensus.contrib.http.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import io.opencensus.stats.Stats;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;

/**
 * A helper class that allows users to register HTTP views easily.
 *
 * @since 0.13
 */
public class HttpViews {

  private HttpViews() {}

  @VisibleForTesting
  static final ImmutableSet<View> HTTP_SERVER_VIEWS_SET =
      ImmutableSet.of(
          HttpViewConstants.HTTP_SERVER_COMPLETED_COUNT_VIEW,
          HttpViewConstants.HTTP_SERVER_SENT_BYTES_VIEW,
          HttpViewConstants.HTTP_SERVER_RECEIVED_BYTES_VIEW,
          HttpViewConstants.HTTP_SERVER_LATENCY_VIEW);

  @VisibleForTesting
  static final ImmutableSet<View> HTTP_CLIENT_VIEWS_SET =
      ImmutableSet.of(
          HttpViewConstants.HTTP_CLIENT_COMPLETED_COUNT_VIEW,
          HttpViewConstants.HTTP_CLIENT_RECEIVED_BYTES_VIEW,
          HttpViewConstants.HTTP_CLIENT_SENT_BYTES_VIEW,
          HttpViewConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY_VIEW);

  /**
   * Register all default client views.
   *
   * <p>It is recommended to call this method before doing any HTTP call to avoid missing stats.
   *
   * @since 0.13
   */
  public static final void registerAllClientViews() {
    registerAllClientViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllClientViews(ViewManager viewManager) {
    for (View view : HTTP_CLIENT_VIEWS_SET) {
      viewManager.registerView(view);
    }
  }

  /**
   * Register all default server views.
   *
   * <p>It is recommended to call this method before doing any HTTP call to avoid missing stats.
   *
   * @since 0.13
   */
  public static final void registerAllServerViews() {
    registerAllServerViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllServerViews(ViewManager viewManager) {
    for (View view : HTTP_SERVER_VIEWS_SET) {
      viewManager.registerView(view);
    }
  }

  /**
   * Register all default views. Equivalent with calling {@link #registerAllClientViews()} and
   * {@link #registerAllServerViews()}.
   *
   * <p>It is recommended to call this method before doing any HTTP call to avoid missing stats.
   *
   * @since 0.13
   */
  public static final void registerAllViews() {
    registerAllViews(Stats.getViewManager());
  }

  @VisibleForTesting
  static void registerAllViews(ViewManager viewManager) {
    registerAllClientViews(viewManager);
    registerAllServerViews(viewManager);
  }
}
