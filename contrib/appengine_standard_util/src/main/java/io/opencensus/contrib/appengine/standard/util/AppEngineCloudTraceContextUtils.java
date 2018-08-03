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

package io.opencensus.contrib.appengine.standard.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.apphosting.api.CloudTraceContext;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import java.nio.ByteBuffer;

/**
 * Utility class to convert between {@link io.opencensus.trace.SpanContext} and {@link
 * CloudTraceContext}.
 *
 * @since 0.14
 */
public final class AppEngineCloudTraceContextUtils {
  private static final byte[] INVALID_TRACE_ID =
      TraceIdProto.newBuilder().setHi(0).setLo(0).build().toByteArray();
  private static final long INVALID_SPAN_ID = 0L;
  private static final long INVALID_TRACE_MASK = 0L;
  private static final Tracestate TRACESTATE_DEFAULT = Tracestate.builder().build();

  @VisibleForTesting
  static final CloudTraceContext INVALID_CLOUD_TRACE_CONTEXT =
      new CloudTraceContext(INVALID_TRACE_ID, INVALID_SPAN_ID, INVALID_TRACE_MASK);

  /**
   * Converts AppEngine {@code CloudTraceContext} to {@code SpanContext}.
   *
   * @param cloudTraceContext the AppEngine {@code CloudTraceContext}.
   * @return the converted {@code SpanContext}.
   * @since 0.14
   */
  public static SpanContext fromCloudTraceContext(CloudTraceContext cloudTraceContext) {
    checkNotNull(cloudTraceContext, "cloudTraceContext");

    try {
      // Extract the trace ID from the binary protobuf CloudTraceContext#traceId.
      TraceIdProto traceIdProto = TraceIdProto.parseFrom(cloudTraceContext.getTraceId());
      ByteBuffer traceIdBuf = ByteBuffer.allocate(TraceId.SIZE);
      traceIdBuf.putLong(traceIdProto.getHi());
      traceIdBuf.putLong(traceIdProto.getLo());
      ByteBuffer spanIdBuf = ByteBuffer.allocate(SpanId.SIZE);
      spanIdBuf.putLong(cloudTraceContext.getSpanId());

      return SpanContext.create(
          TraceId.fromBytes(traceIdBuf.array()),
          SpanId.fromBytes(spanIdBuf.array()),
          TraceOptions.builder().setIsSampled(cloudTraceContext.isTraceEnabled()).build(),
          TRACESTATE_DEFAULT);
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts {@code SpanContext} to AppEngine {@code CloudTraceContext}.
   *
   * @param spanContext the {@code SpanContext}.
   * @return the converted AppEngine {@code CloudTraceContext}.
   * @since 0.14
   */
  public static CloudTraceContext toCloudTraceContext(SpanContext spanContext) {
    checkNotNull(spanContext, "spanContext");

    ByteBuffer traceIdBuf = ByteBuffer.wrap(spanContext.getTraceId().getBytes());
    TraceIdProto traceIdProto =
        TraceIdProto.newBuilder().setHi(traceIdBuf.getLong()).setLo(traceIdBuf.getLong()).build();
    ByteBuffer spanIdBuf = ByteBuffer.wrap(spanContext.getSpanId().getBytes());

    return new CloudTraceContext(
        traceIdProto.toByteArray(),
        spanIdBuf.getLong(),
        spanContext.getTraceOptions().isSampled() ? 1L : 0L);
  }

  private AppEngineCloudTraceContextUtils() {}
}
