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

package io.opencensus.exporter.trace.ocagent;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/** Fake implementations of {@link io.grpc.stub.ClientCalls}. Used in unit tests. */
final class FakeClientCalls {

  private FakeClientCalls() {}

  // No-op implementation of ClientCall. ClientCall cannot be mocked with annotation:
  // @DoNotMock("Use InProcessServerBuilder and make a test server instead").
  static final class FakeClientCall extends ClientCall<Object, Object> {
    private final List<Object> messages = new ArrayList<Object>();

    @Override
    public void start(Listener<Object> responseListener, Metadata headers) {}

    @Override
    public void request(int numMessages) {}

    @Override
    public void cancel(@Nullable String message, @Nullable Throwable cause) {}

    @Override
    public void halfClose() {}

    @Override
    public void sendMessage(Object message) {
      messages.add(message);
    }

    // Returns the stored messages.
    List<Object> getMessages() {
      return messages;
    }
  }

  // Implementation of ClientCall that throws an exception.
  private abstract static class FakeErrorClientCall extends ClientCall<Object, Object> {
    private final RuntimeException exception;

    FakeErrorClientCall(RuntimeException exception) {
      this.exception = exception;
    }

    @Override
    public void request(int numMessages) {}

    @Override
    public void cancel(@Nullable String message, @Nullable Throwable cause) {}

    @Override
    public void halfClose() {}
  }

  // Implementation of ClientCall that throws an exception when starting.
  static final class FakeErrorStartClientCall extends FakeErrorClientCall {
    FakeErrorStartClientCall(RuntimeException exception) {
      super(exception);
    }

    @Override
    public void start(Listener<Object> responseListener, Metadata headers) {
      throw super.exception;
    }

    @Override
    public void sendMessage(Object message) {}
  }

  // Implementation of ClientCall that throws an exception when sending messages.
  static final class FakeErrorSendClientCall extends FakeErrorClientCall {
    FakeErrorSendClientCall(RuntimeException exception) {
      super(exception);
    }

    @Override
    public void start(Listener<Object> responseListener, Metadata headers) {}

    @Override
    public void sendMessage(Object message) {
      throw super.exception;
    }
  }
}
