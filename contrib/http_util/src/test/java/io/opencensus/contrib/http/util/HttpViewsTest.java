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

import static org.mockito.Mockito.verify;

import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Test for {@link HttpViews}. */
@RunWith(JUnit4.class)
public class HttpViewsTest {

  @Mock ViewManager viewManager;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void registerClientViews() {
    HttpViews.registerAllClientViews(viewManager);
    for (View view : HttpViews.HTTP_CLIENT_VIEWS_SET) {
      verify(viewManager).registerView(view);
    }
  }

  @Test
  public void registerServerViews() {
    HttpViews.registerAllServerViews(viewManager);
    for (View view : HttpViews.HTTP_SERVER_VIEWS_SET) {
      verify(viewManager).registerView(view);
    }
  }

  @Test
  public void registerAll() {
    HttpViews.registerAllViews(viewManager);
    for (View view : HttpViews.HTTP_CLIENT_VIEWS_SET) {
      verify(viewManager).registerView(view);
    }
    for (View view : HttpViews.HTTP_SERVER_VIEWS_SET) {
      verify(viewManager).registerView(view);
    }
  }
}
