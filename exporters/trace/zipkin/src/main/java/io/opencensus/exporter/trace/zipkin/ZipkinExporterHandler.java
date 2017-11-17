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

package io.opencensus.exporter.trace.zipkin;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.io.BaseEncoding;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Scope;
import io.opencensus.common.Timestamp;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.NetworkEvent;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanData.TimedEvent;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.samplers.Samplers;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;

final class ZipkinExporterHandler extends SpanExporter.Handler {
  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySpampler = Samplers.probabilitySampler(0.0001);
  static final Logger logger = Logger.getLogger(ZipkinExporterHandler.class.getName());

  private static final String STATUS_CODE = "census.status_code";
  private static final String STATUS_DESCRIPTION = "census.status_description";
  private static final Function<Object, String> RETURN_STRING =
      new Function<Object, String>() {
        @Override
        public String apply(Object input) {
          return input.toString();
        }
      };
  private final SpanBytesEncoder encoder;
  private final Sender sender;
  private final Endpoint localEndpoint;

  ZipkinExporterHandler(SpanBytesEncoder encoder, Sender sender, String serviceName) {
    this.encoder = encoder;
    this.sender = sender;
    this.localEndpoint = produceLocalEndpoint(serviceName);
  }

  /** Logic borrowed from brave.internal.Platform.produceLocalEndpoint */
  static Endpoint produceLocalEndpoint(String serviceName) {
    Endpoint.Builder builder = Endpoint.newBuilder().serviceName(serviceName);
    try {
      Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
      if (nics == null) {
        return builder.build();
      }
      while (nics.hasMoreElements()) {
        NetworkInterface nic = nics.nextElement();
        Enumeration<InetAddress> addresses = nic.getInetAddresses();
        while (addresses.hasMoreElements()) {
          InetAddress address = addresses.nextElement();
          if (address.isSiteLocalAddress()) {
            builder.ip(address);
            break;
          }
        }
      }
    } catch (Exception e) {
      // don't crash the caller if there was a problem reading nics.
      if (logger.isLoggable(Level.FINE)) {
        logger.log(Level.FINE, "error reading nics", e);
      }
    }
    return builder.build();
  }

  static Span generateSpan(SpanData spanData, Endpoint localEndpoint) {
    SpanContext context = spanData.getContext();
    long startTimestamp = toEpochMicros(spanData.getStartTimestamp());
    long endTimestamp = toEpochMicros(spanData.getEndTimestamp());
    Span.Builder spanBuilder =
        Span.newBuilder()
            .traceId(encodeTraceId(context.getTraceId()))
            .id(encodeSpanId(context.getSpanId()))
            .kind(toSpanKind(spanData))
            .name(spanData.getName())
            .timestamp(toEpochMicros(spanData.getStartTimestamp()))
            .duration(endTimestamp - startTimestamp)
            .localEndpoint(localEndpoint);

    if (spanData.getParentSpanId() != null && spanData.getParentSpanId().isValid()) {
      spanBuilder.parentId(encodeSpanId(spanData.getParentSpanId()));
    }

    for (Map.Entry<String, AttributeValue> label :
        spanData.getAttributes().getAttributeMap().entrySet()) {
      spanBuilder.putTag(label.getKey(), attributeValueToString(label.getValue()));
    }
    spanBuilder.putTag(STATUS_CODE, spanData.getStatus().getCanonicalCode().toString());
    if (spanData.getStatus().getDescription() != null) {
      spanBuilder.putTag(STATUS_DESCRIPTION, spanData.getStatus().getDescription());
    }

    for (TimedEvent<Annotation> annotation : spanData.getAnnotations().getEvents()) {
      spanBuilder.addAnnotation(
          toEpochMicros(annotation.getTimestamp()), annotation.getEvent().getDescription());
    }

    for (TimedEvent<NetworkEvent> networkEvent : spanData.getNetworkEvents().getEvents()) {
      spanBuilder.addAnnotation(
          toEpochMicros(networkEvent.getTimestamp()), networkEvent.getEvent().getType().name());
    }

    return spanBuilder.build();
  }

  private static String encodeTraceId(TraceId traceId) {
    return BaseEncoding.base16().lowerCase().encode(traceId.getBytes());
  }

  private static String encodeSpanId(SpanId spanId) {
    return BaseEncoding.base16().lowerCase().encode(spanId.getBytes());
  }

  @Nullable
  private static Span.Kind toSpanKind(SpanData spanData) {
    if (Boolean.TRUE.equals(spanData.getHasRemoteParent())) {
      return Span.Kind.SERVER;
    }

    // This is a hack because the v2 API does not have SpanKind. When switch to v2 this will be
    // fixed.
    if (spanData.getName().startsWith("Sent.")) {
      return Span.Kind.CLIENT;
    }

    return null;
  }

  private static long toEpochMicros(Timestamp timestamp) {
    return SECONDS.toMicros(timestamp.getSeconds()) + NANOSECONDS.toMicros(timestamp.getNanos());
  }

  private static String attributeValueToString(AttributeValue attributeValue) {
    return attributeValue.match(
        RETURN_STRING, RETURN_STRING, RETURN_STRING, Functions.<String>returnNull());
  }

  @Override
  public void export(Collection<SpanData> spanDataList) {
    // Start a new span with explicit 1/10000 sampling probability to avoid the case when user
    // sets the default sampler to always sample and we get the gRPC span of the stackdriver
    // export call always sampled and go to an infinite loop.
    Scope scope =
        tracer
            .spanBuilder("ExportStackdriverTraces")
            .setSampler(probabilitySpampler)
            .startScopedSpan();
    try {
      List<byte[]> encodedSpans = new ArrayList<byte[]>(spanDataList.size());
      for (SpanData spanData : spanDataList) {
        encodedSpans.add(encoder.encode(generateSpan(spanData, localEndpoint)));
      }
      try {
        sender.sendSpans(encodedSpans).execute();
      } catch (IOException e) {
        tracer.getCurrentSpan().setStatus(Status.UNKNOWN.withDescription(e.getMessage()));
        throw new RuntimeException(e); // TODO: should we instead do drop metrics?
      }
    } finally {
      scope.close();
    }
  }
}
