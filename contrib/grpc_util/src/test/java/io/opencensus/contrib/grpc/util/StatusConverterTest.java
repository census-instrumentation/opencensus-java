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

package io.opencensus.contrib.grpc.util;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StatusConverter}. */
@RunWith(JUnit4.class)
public class StatusConverterTest {

  @Test
  public void convertFromGrpcCode() {
    for (io.grpc.Status.Code grpcCanonicalCode : io.grpc.Status.Code.values()) {
      io.opencensus.trace.Status.CanonicalCode opencensusCanonicalCode =
          StatusConverter.fromGrpcCode(grpcCanonicalCode);
      assertThat(opencensusCanonicalCode.toString()).isEqualTo(grpcCanonicalCode.toString());
    }
  }

  @Test
  public void convertFromGrpcStatus() {
    // Without description
    for (io.grpc.Status.Code grpcCanonicalCode : io.grpc.Status.Code.values()) {
      io.grpc.Status grpcStatus = io.grpc.Status.fromCode(grpcCanonicalCode);
      io.opencensus.trace.Status opencensusStatus = StatusConverter.fromGrpcStatus(grpcStatus);
      assertThat(opencensusStatus.getCanonicalCode().toString())
          .isEqualTo(grpcStatus.getCode().toString());
      assertThat(opencensusStatus.getDescription()).isNull();
    }

    // With description
    for (io.grpc.Status.Code grpcCanonicalCode : io.grpc.Status.Code.values()) {
      io.grpc.Status grpcStatus =
          io.grpc.Status.fromCode(grpcCanonicalCode).withDescription("This is my description");
      io.opencensus.trace.Status opencensusStatus = StatusConverter.fromGrpcStatus(grpcStatus);
      assertThat(opencensusStatus.getCanonicalCode().toString())
          .isEqualTo(grpcStatus.getCode().toString());
      assertThat(opencensusStatus.getDescription()).isEqualTo(grpcStatus.getDescription());
    }
  }

  @Test
  public void convertToGrpcCode() {
    for (io.opencensus.trace.Status.CanonicalCode opencensusCanonicalCode :
        io.opencensus.trace.Status.CanonicalCode.values()) {
      io.grpc.Status.Code grpcCanonicalCode = StatusConverter.toGrpcCode(opencensusCanonicalCode);
      assertThat(grpcCanonicalCode.toString()).isEqualTo(opencensusCanonicalCode.toString());
    }
  }

  @Test
  public void convertToGrpcStatus() {
    // Without description
    for (io.opencensus.trace.Status.CanonicalCode opencensusCanonicalCode :
        io.opencensus.trace.Status.CanonicalCode.values()) {
      io.opencensus.trace.Status opencensusStatus = opencensusCanonicalCode.toStatus();
      io.grpc.Status grpcStatus = StatusConverter.toGrpcStatus(opencensusStatus);
      assertThat(grpcStatus.getCode().toString())
          .isEqualTo(opencensusStatus.getCanonicalCode().toString());
      assertThat(grpcStatus.getDescription()).isNull();
    }

    // With description
    for (io.opencensus.trace.Status.CanonicalCode opencensusCanonicalCode :
        io.opencensus.trace.Status.CanonicalCode.values()) {
      io.opencensus.trace.Status opencensusStatus =
          opencensusCanonicalCode.toStatus().withDescription("This is my description");
      io.grpc.Status grpcStatus = StatusConverter.toGrpcStatus(opencensusStatus);
      assertThat(grpcStatus.getCode().toString())
          .isEqualTo(opencensusStatus.getCanonicalCode().toString());
      assertThat(grpcStatus.getDescription()).isEqualTo(opencensusStatus.getDescription());
    }
  }
}
