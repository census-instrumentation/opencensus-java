package io.opencensus.trace.propagation;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.trace.SpanContext;
import java.util.Collections;
import java.util.List;

/**
 * Injects and extracts {@link SpanContext trace identifiers} as text into carriers that travel
 * in-band across process boundaries. Identifiers are often encoded as messaging or RPC request
 * headers.
 *
 * <h3>Propagation example: Http</h3>
 *
 * <p>When using http, the carrier of propagated data on both the client (injector) and server
 * (extractor) side is usually an http request. Propagation is usually implemented via library-
 * specific request interceptors, where the client-side injects span identifiers and the server-side
 * extracts them.
 */
public abstract class TextFormat {

  static final NoopTextFormat NOOP_TEXT_FORMAT = new NoopTextFormat();

  /**
   * The propagation fields defined. If your carrier is reused, you should delete the fields here
   * before calling {@link #putContext(SpanContext, Object, Setter)}.
   *
   * <p>For example, if the carrier is a single-use or immutable request object, you don't need to
   * clear fields as they couldn't have been set before. If it is a mutable, retryable object,
   * successive calls should clear these fields first.
   */
  // The use cases of this are:
  // * allow pre-allocation of fields, especially in systems like gRPC Metadata
  // * allow a single-pass over an iterator (ex OpenTracing has no getter in TextMap)
  public abstract List<String> fields();

  /**
   * Used to send the trace context downstream. For example, as http headers.
   *
   * For example, to put the context on an {@link java.net.HttpURLConnection}, you would do this:
   * <pre>{@code
   * HttpURLConnection connection = (HttpURLConnection) new URL("http://myserver").openConnection();
   * textFormat.putContext(spanContext, connection, URLConnection::setRequestProperty);
   * }</pre>
   *
   * @param spanContext possibly unsampled.
   * @param carrier holds propagation fields. For example, an outgoing message or http request.
   * @param setter invoked for each propagation key to add or remove.
   */
  public abstract <C> void putContext(SpanContext spanContext, C carrier, Setter<C> setter);

  /**
   * Replaces a propagated field with the given value. Saved as a constant to avoid runtime
   * allocations.
   *
   * For example, a setter for an {@link java.net.HttpURLConnection} would be the method reference
   * {@link java.net.HttpURLConnection#addRequestProperty(String, String)}
   *
   * @param <C> carrier of propagation fields, such as an http request
   */
  public interface Setter<C> {

    void put(C carrier, String field, String value);
  }

  /**
   * Returns the no-op implementation of the {@code TextFormat}.
   *
   * @return the no-op implementation of the {@code TextFormat}.
   */
  static TextFormat getNoopTextFormat() {
    return NOOP_TEXT_FORMAT;
  }

  private static final class NoopTextFormat extends TextFormat {

    private NoopTextFormat() {
    }

    @Override
    public List<String> fields() {
      return Collections.emptyList();
    }

    @Override
    public <C> void putContext(SpanContext spanContext, C carrier, Setter<C> setter) {
      checkNotNull(spanContext, "spanContext");
    }
  }
}
