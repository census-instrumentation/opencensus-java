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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opencensus.common.NonThrowingCloseable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link CurrentTagsUtils}.
 */
@RunWith(JUnit4.class)
public class CurrentTagsUtilsTest {

  @Mock
  private StatsContext statsContext;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetCurrentStatsContext_WhenNoContext() {
    assertThat(CurrentTagsUtils.getCurrentStatsContext()).isNull();
  }

  @Test
  public void testWithStatsContext() {
    assertThat(CurrentTagsUtils.getCurrentStatsContext()).isNull();
    NonThrowingCloseable scopedStatsCtx = CurrentTagsUtils.withStatsContext(statsContext);
    try {
      assertThat(CurrentTagsUtils.getCurrentStatsContext()).isSameAs(statsContext);
    } finally {
      scopedStatsCtx.close();
    }
    assertThat(CurrentTagsUtils.getCurrentStatsContext()).isNull();
  }

  @Test
  public void testWithStatsContextUsingWrap() {
    Runnable runnable;
    NonThrowingCloseable scopedStatsCtx = CurrentTagsUtils.withStatsContext(statsContext);
    try {
      assertThat(CurrentTagsUtils.getCurrentStatsContext()).isSameAs(statsContext);
      runnable = Context.current().wrap(
          new Runnable() {
            @Override
            public void run() {
              assertThat(CurrentTagsUtils.getCurrentStatsContext()).isSameAs(statsContext);
            }
          });
    } finally {
      scopedStatsCtx.close();
    }
    assertThat(CurrentTagsUtils.getCurrentStatsContext()).isNull();
    // When we run the runnable we will have the statsContext in the current Context.
    runnable.run();
  }
}
