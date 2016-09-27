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

package com.google.census;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link CensusGrpcContext}.
 */
@RunWith(JUnit4.class)
public class CensusGrpcContextTest {
  private static final CensusContext DEFAULT = Census.getCensusContextFactory().getDefault();
  private static final CensusGrpcContext CENSUS_GRPC_CONTEXT = CensusGrpcContext.getInstance();

  @Test
  public void withCensusContext() {
    Context original = Context.current();
    assertThat(CENSUS_GRPC_CONTEXT.get(original)).isEqualTo(DEFAULT);
    CensusContext censusContext = DEFAULT.with(new TagKey("k1"), new TagValue("v1"));
    Context context = CENSUS_GRPC_CONTEXT.withCensusContext(original, censusContext);
    assertThat(CENSUS_GRPC_CONTEXT.get(context)).isEqualTo(censusContext);
  }

  @Test
  public void attachCensusContext() {
    assertThat(CENSUS_GRPC_CONTEXT.get(Context.current())).isEqualTo(DEFAULT);
    testAttachCensusContext(DEFAULT.with(K1, V1)).run();
    assertThat(CENSUS_GRPC_CONTEXT.get(Context.current())).isEqualTo(DEFAULT);
  }

  @Test
  public void attachCensusContextInMultipleThreads() throws Exception {
     assertThat(CENSUS_GRPC_CONTEXT.get(Context.current())).isEqualTo(DEFAULT);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    Future<?> future1 = executor.submit(testAttachCensusContext(DEFAULT.with(K1, V1)));
    Future<?> future2 = executor.submit(testAttachCensusContext(DEFAULT.with(K2, V2)));
    future1.get();
    future2.get();
    assertThat(CENSUS_GRPC_CONTEXT.get(Context.current())).isEqualTo(DEFAULT);
  }

  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");

  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");

  private static final Runnable testAttachCensusContext(final CensusContext censusContext) {
    return new Runnable() {
      @Override
      public void run() {
        assertThat(CENSUS_GRPC_CONTEXT.get(Context.current())).isEqualTo(DEFAULT);
        Context current = CENSUS_GRPC_CONTEXT.withCensusContext(Context.current(), censusContext);
        Context original = current.attach();
        assertThat(CENSUS_GRPC_CONTEXT.get(Context.current())).isEqualTo(censusContext);
        current.detach(original);
        assertThat(CENSUS_GRPC_CONTEXT.get(Context.current())).isEqualTo(DEFAULT);
      }
    };
  }
}
