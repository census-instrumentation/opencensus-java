/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.contrib.grpc.metrics;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Maps;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for {@link RpcViews}. */
@RunWith(JUnit4.class)
public class RpcViewsTest {

  @Test
  public void registerCumulative() {
    FakeViewManager fakeViewManager = new FakeViewManager();
    RpcViews.registerCumulativeViews(fakeViewManager);
    for (View view : RpcViews.RPC_CUMULATIVE_VIEWS_SET) {
      assertThat(fakeViewManager.isRegistered(view)).isTrue();
    }
    for (View view : RpcViews.RPC_INTERVAL_VIEWS_SET) {
      assertThat(fakeViewManager.isRegistered(view)).isFalse();
    }
  }

  @Test
  public void registerInterval() {
    FakeViewManager fakeViewManager = new FakeViewManager();
    RpcViews.registerIntervalViews(fakeViewManager);
    for (View view : RpcViews.RPC_CUMULATIVE_VIEWS_SET) {
      assertThat(fakeViewManager.isRegistered(view)).isFalse();
    }
    for (View view : RpcViews.RPC_INTERVAL_VIEWS_SET) {
      assertThat(fakeViewManager.isRegistered(view)).isTrue();
    }
  }

  @Test
  public void registerAll() {
    FakeViewManager fakeViewManager = new FakeViewManager();
    RpcViews.registerAllViews(fakeViewManager);
    for (View view : RpcViews.RPC_CUMULATIVE_VIEWS_SET) {
      assertThat(fakeViewManager.isRegistered(view)).isTrue();
    }
    for (View view : RpcViews.RPC_INTERVAL_VIEWS_SET) {
      assertThat(fakeViewManager.isRegistered(view)).isTrue();
    }
  }

  private static final class FakeViewManager extends ViewManager {
    private final Map<View.Name, View> registeredViews = Maps.newHashMap();

    private FakeViewManager() {}

    @Override
    public void registerView(View view) {
      registeredViews.put(view.getName(), view);
    }

    private boolean isRegistered(View view) {
      return registeredViews.containsKey(view.getName());
    }

    @Nullable
    @Override
    public ViewData getView(View.Name view) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<View> getAllExportedViews() {
      throw new UnsupportedOperationException();
    }
  }
}
