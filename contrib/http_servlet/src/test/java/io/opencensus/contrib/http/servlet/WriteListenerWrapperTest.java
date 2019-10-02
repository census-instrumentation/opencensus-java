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

package io.opencensus.contrib.http.servlet;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.grpc.Context.Key;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.WriteListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link WriteListenerWrapper}. */
@RunWith(JUnit4.class)
public class WriteListenerWrapperTest {

  @Test
  public void testWriteOnPossibleWithContext() throws IOException, ServletException {

    Key<String> key = Context.<String>key("test-key");
    Context curr = Context.current();
    assertThat(curr).isNotNull();
    assertThat(curr.withValue(key, "parent").attach()).isNotNull();

    final Context parentContext = Context.current();
    assertThat(parentContext).isNotNull();

    WriteListenerWrapper writeListener =
        new WriteListenerWrapper(
            new WriteListener() {
              @Override
              public void onWritePossible() throws IOException {
                Context curr = Context.current();
                assertThat(curr).isNotNull();
                assertThat(curr).isEqualTo(parentContext);
              }

              @Override
              public void onError(Throwable t) {}
            });

    assertThat(parentContext.withValue(key, "child").attach()).isNotNull();
    writeListener.onWritePossible();
  }
}
