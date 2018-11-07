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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import java.util.Collection;
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
    RpcViews.registerAllCumulativeViews(fakeViewManager);
    assertThat(fakeViewManager.getRegisteredViews())
        .containsExactlyElementsIn(RpcViews.RPC_CUMULATIVE_VIEWS_SET);
  }

  @Test
  public void registerInterval() {
    FakeViewManager fakeViewManager = new FakeViewManager();
    RpcViews.registerAllIntervalViews(fakeViewManager);
    assertThat(fakeViewManager.getRegisteredViews())
        .containsExactlyElementsIn(RpcViews.RPC_INTERVAL_VIEWS_SET);
  }

  @Test
  public void registerAll() {
    FakeViewManager fakeViewManager = new FakeViewManager();
    RpcViews.registerAllViews(fakeViewManager);
    assertThat(fakeViewManager.getRegisteredViews())
        .containsExactlyElementsIn(
            ImmutableSet.builder()
                .addAll(RpcViews.RPC_CUMULATIVE_VIEWS_SET)
                .addAll(RpcViews.RPC_INTERVAL_VIEWS_SET)
                .build());
  }

  @Test
  public void registerAllGrpcViews() {
    FakeViewManager fakeViewManager = new FakeViewManager();
    RpcViews.registerAllGrpcViews(fakeViewManager);
    assertThat(fakeViewManager.getRegisteredViews())
        .containsExactlyElementsIn(
            ImmutableSet.builder()
                .addAll(RpcViews.GRPC_CLIENT_VIEWS_SET)
                .addAll(RpcViews.GRPC_SERVER_VIEWS_SET)
                .build());
  }

  @Test
  public void registerClientGrpcViews() {
    FakeViewManager fakeViewManager = new FakeViewManager();
    RpcViews.registerClientGrpcViews(fakeViewManager);
    assertThat(fakeViewManager.getRegisteredViews())
        .containsExactlyElementsIn(RpcViews.GRPC_CLIENT_VIEWS_SET);
  }

  @Test
  public void registerServerGrpcViews() {
    FakeViewManager fakeViewManager = new FakeViewManager();
    RpcViews.registerServerGrpcViews(fakeViewManager);
    assertThat(fakeViewManager.getRegisteredViews())
        .containsExactlyElementsIn(RpcViews.GRPC_SERVER_VIEWS_SET);
  }

  @Test
  public void registerRealTimeMetricsViews() {
    FakeViewManager fakeViewManager = new FakeViewManager();
    RpcViews.registerRealTimeMetricsViews(fakeViewManager);
    assertThat(fakeViewManager.getRegisteredViews())
        .containsExactlyElementsIn(RpcViews.RPC_REAL_TIME_METRICS_VIEWS_SET);
  }

  // TODO(bdrutu): Test with reflection that all defined gRPC views are registered.

  private static final class FakeViewManager extends ViewManager {
    private final Map<View.Name, View> registeredViews = Maps.newHashMap();

    private FakeViewManager() {}

    @Override
    public void registerView(View view) {
      registeredViews.put(view.getName(), view);
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

    private Collection<View> getRegisteredViews() {
      return registeredViews.values();
    }
  }
}
