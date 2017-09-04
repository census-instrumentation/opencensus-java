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

/**
 * Utility class to convert between {@link io.opencensus.trace.Status} and {@link io.grpc.Status}
 */
public final class StatusConverter {

  /**
   * Returns a {@link io.opencensus.trace.Status.CanonicalCode} from a {@link io.grpc.Status.Code}.
   *
   * @param grpcCanonicalCode the given gRPC CanonicalCode.
   * @return a {@code io.opencensus.trace.Status.CanonicalCode} from a {@code io.grpc.Status.Code}.
   */
  public static io.opencensus.trace.Status.CanonicalCode fromGrpcCanonicalCode(
      io.grpc.Status.Code grpcCanonicalCode) {
    return opencensusStatusFromGrpcCode(grpcCanonicalCode).getCanonicalCode();
  }

  /**
   * Returns a {@link io.opencensus.trace.Status} from a {@link io.grpc.Status}.
   *
   * @param grpcStatus the given gRPC Status.
   * @return a {@code io.opencensus.trace.Status} from a {@code io.grpc.Status}.
   */
  public static io.opencensus.trace.Status fromGrpcStatus(io.grpc.Status grpcStatus) {
    io.opencensus.trace.Status status = opencensusStatusFromGrpcCode(grpcStatus.getCode());
    if (grpcStatus.getDescription() != null) {
      status = status.withDescription(grpcStatus.getDescription());
    }
    return status;
  }

  /**
   * Returns a {@link io.grpc.Status.Code} from a {@link io.opencensus.trace.Status.CanonicalCode}.
   *
   * @param opencensusCanonicalCode the given OpenCensus CanonicalCode.
   * @return a {@code io.grpc.Status.Code} from a {@code io.opencensus.trace.Status.CanonicalCode}.
   */
  public static io.grpc.Status.Code toGrpcCanonicalCode(io.opencensus.trace.Status.CanonicalCode
      opencensusCanonicalCode) {
    return grpcStatusFromOpencensusCanonicalCode(opencensusCanonicalCode).getCode();
  }

  /**
   * Returns a {@link io.grpc.Status} from a {@link io.opencensus.trace.Status}.
   *
   * @param opencensusStatus the given OpenCensus Status.
   * @return a {@code io.grpc.Status} from a {@code io.opencensus.trace.Status}.
   */
  public static io.grpc.Status toGrpcStatus(io.opencensus.trace.Status opencensusStatus) {
    io.grpc.Status status = grpcStatusFromOpencensusCanonicalCode(opencensusStatus
        .getCanonicalCode());
    if (opencensusStatus.getDescription() != null) {
      status = status.withDescription(opencensusStatus.getDescription());
    }
    return status;
  }

  private static io.opencensus.trace.Status opencensusStatusFromGrpcCode(io.grpc.Status.Code
      grpcCanonicaleCode) {
    switch (grpcCanonicaleCode) {
      case OK:
        return io.opencensus.trace.Status.OK;
      case CANCELLED:
        return io.opencensus.trace.Status.CANCELLED;
      case UNKNOWN:
        return io.opencensus.trace.Status.UNKNOWN;
      case INVALID_ARGUMENT:
        return io.opencensus.trace.Status.INVALID_ARGUMENT;
      case DEADLINE_EXCEEDED:
        return io.opencensus.trace.Status.DEADLINE_EXCEEDED;
      case NOT_FOUND:
        return io.opencensus.trace.Status.NOT_FOUND;
      case ALREADY_EXISTS:
        return io.opencensus.trace.Status.ALREADY_EXISTS;
      case PERMISSION_DENIED:
        return io.opencensus.trace.Status.PERMISSION_DENIED;
      case RESOURCE_EXHAUSTED:
        return io.opencensus.trace.Status.RESOURCE_EXHAUSTED;
      case FAILED_PRECONDITION:
        return io.opencensus.trace.Status.FAILED_PRECONDITION;
      case ABORTED:
        return io.opencensus.trace.Status.ABORTED;
      case OUT_OF_RANGE:
        return io.opencensus.trace.Status.OUT_OF_RANGE;
      case UNIMPLEMENTED:
        return io.opencensus.trace.Status.UNIMPLEMENTED;
      case INTERNAL:
        return io.opencensus.trace.Status.INTERNAL;
      case UNAVAILABLE:
        return io.opencensus.trace.Status.UNAVAILABLE;
      case DATA_LOSS:
        return io.opencensus.trace.Status.DATA_LOSS;
      case UNAUTHENTICATED:
        return io.opencensus.trace.Status.UNAUTHENTICATED;
    }
    throw new AssertionError("Unhandled status code " + grpcCanonicaleCode);
  }

  private static io.grpc.Status grpcStatusFromOpencensusCanonicalCode(io.opencensus.trace.Status
      .CanonicalCode opencensusCanonicalCode) {
    switch (opencensusCanonicalCode) {
      case OK:
        return io.grpc.Status.OK;
      case CANCELLED:
        return io.grpc.Status.CANCELLED;
      case UNKNOWN:
        return io.grpc.Status.UNKNOWN;
      case INVALID_ARGUMENT:
        return io.grpc.Status.INVALID_ARGUMENT;
      case DEADLINE_EXCEEDED:
        return io.grpc.Status.DEADLINE_EXCEEDED;
      case NOT_FOUND:
        return io.grpc.Status.NOT_FOUND;
      case ALREADY_EXISTS:
        return io.grpc.Status.ALREADY_EXISTS;
      case PERMISSION_DENIED:
        return io.grpc.Status.PERMISSION_DENIED;
      case RESOURCE_EXHAUSTED:
        return io.grpc.Status.RESOURCE_EXHAUSTED;
      case FAILED_PRECONDITION:
        return io.grpc.Status.FAILED_PRECONDITION;
      case ABORTED:
        return io.grpc.Status.ABORTED;
      case OUT_OF_RANGE:
        return io.grpc.Status.OUT_OF_RANGE;
      case UNIMPLEMENTED:
        return io.grpc.Status.UNIMPLEMENTED;
      case INTERNAL:
        return io.grpc.Status.INTERNAL;
      case UNAVAILABLE:
        return io.grpc.Status.UNAVAILABLE;
      case DATA_LOSS:
        return io.grpc.Status.DATA_LOSS;
      case UNAUTHENTICATED:
        return io.grpc.Status.UNAUTHENTICATED;
    }
    throw new AssertionError("Unhandled status code " + opencensusCanonicalCode);
  }

  private StatusConverter() {
  }
}
