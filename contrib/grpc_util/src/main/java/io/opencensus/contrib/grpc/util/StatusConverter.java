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
   * Returns a {@link io.opencensus.trace.Status} from a {@link io.grpc.Status}.
   *
   * @param grpcStatus the given gRPC Status.
   * @return a {@code io.opencensus.trace.Status} from a {@code io.grpc.Status}.
   */
  public static io.opencensus.trace.Status fromGrpcStatus(io.grpc.Status grpcStatus) {
    io.opencensus.trace.Status status;
    switch (grpcStatus.getCode()) {
      case OK:
        status = io.opencensus.trace.Status.OK;
        break;
      case CANCELLED:
        status = io.opencensus.trace.Status.CANCELLED;
        break;
      case UNKNOWN:
        status = io.opencensus.trace.Status.UNKNOWN;
        break;
      case INVALID_ARGUMENT:
        status = io.opencensus.trace.Status.INVALID_ARGUMENT;
        break;
      case DEADLINE_EXCEEDED:
        status = io.opencensus.trace.Status.DEADLINE_EXCEEDED;
        break;
      case NOT_FOUND:
        status = io.opencensus.trace.Status.NOT_FOUND;
        break;
      case ALREADY_EXISTS:
        status = io.opencensus.trace.Status.ALREADY_EXISTS;
        break;
      case PERMISSION_DENIED:
        status = io.opencensus.trace.Status.PERMISSION_DENIED;
        break;
      case RESOURCE_EXHAUSTED:
        status = io.opencensus.trace.Status.RESOURCE_EXHAUSTED;
        break;
      case FAILED_PRECONDITION:
        status = io.opencensus.trace.Status.FAILED_PRECONDITION;
        break;
      case ABORTED:
        status = io.opencensus.trace.Status.ABORTED;
        break;
      case OUT_OF_RANGE:
        status = io.opencensus.trace.Status.OUT_OF_RANGE;
        break;
      case UNIMPLEMENTED:
        status = io.opencensus.trace.Status.UNIMPLEMENTED;
        break;
      case INTERNAL:
        status = io.opencensus.trace.Status.INTERNAL;
        break;
      case UNAVAILABLE:
        status = io.opencensus.trace.Status.UNAVAILABLE;
        break;
      case DATA_LOSS:
        status = io.opencensus.trace.Status.DATA_LOSS;
        break;
      case UNAUTHENTICATED:
        status = io.opencensus.trace.Status.UNAUTHENTICATED;
        break;
      default:
        throw new AssertionError("Unhandled status code " + grpcStatus.getCode());
    }
    if (grpcStatus.getDescription() != null) {
      status = status.withDescription(grpcStatus.getDescription());
    }
    return status;
  }

  /**
   * Returns a {@link io.grpc.Status} from a {@link io.opencensus.trace.Status}.
   *
   * @param opencensusStatus the given Status.
   * @return a {@link io.grpc.Status} from a {@link io.opencensus.trace.Status}.
   */
  public static io.grpc.Status toGrpcStatus(io.opencensus.trace.Status opencensusStatus) {
    io.grpc.Status status;
    switch (opencensusStatus.getCanonicalCode()) {
      case OK:
        status = io.grpc.Status.OK;
        break;
      case CANCELLED:
        status = io.grpc.Status.CANCELLED;
        break;
      case UNKNOWN:
        status = io.grpc.Status.UNKNOWN;
        break;
      case INVALID_ARGUMENT:
        status = io.grpc.Status.INVALID_ARGUMENT;
        break;
      case DEADLINE_EXCEEDED:
        status = io.grpc.Status.DEADLINE_EXCEEDED;
        break;
      case NOT_FOUND:
        status = io.grpc.Status.NOT_FOUND;
        break;
      case ALREADY_EXISTS:
        status = io.grpc.Status.ALREADY_EXISTS;
        break;
      case PERMISSION_DENIED:
        status = io.grpc.Status.PERMISSION_DENIED;
        break;
      case RESOURCE_EXHAUSTED:
        status = io.grpc.Status.RESOURCE_EXHAUSTED;
        break;
      case FAILED_PRECONDITION:
        status = io.grpc.Status.FAILED_PRECONDITION;
        break;
      case ABORTED:
        status = io.grpc.Status.ABORTED;
        break;
      case OUT_OF_RANGE:
        status = io.grpc.Status.OUT_OF_RANGE;
        break;
      case UNIMPLEMENTED:
        status = io.grpc.Status.UNIMPLEMENTED;
        break;
      case INTERNAL:
        status = io.grpc.Status.INTERNAL;
        break;
      case UNAVAILABLE:
        status = io.grpc.Status.UNAVAILABLE;
        break;
      case DATA_LOSS:
        status = io.grpc.Status.DATA_LOSS;
        break;
      case UNAUTHENTICATED:
        status = io.grpc.Status.UNAUTHENTICATED;
        break;
      default:
        throw new AssertionError("Unhandled status code " + opencensusStatus.getCanonicalCode());
    }
    if (opencensusStatus.getDescription() != null) {
      status = status.withDescription(opencensusStatus.getDescription());
    }
    return status;
  }

  private StatusConverter() {}
}
