/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.Supplier;

public abstract class HttpRequest {

  protected HttpRequest() {}

  public static Builder newBuilder() {
    return new BuilderImpl();
  }

  public static Builder newBuilder(HttpRequest request, java.util.function.BiPredicate<String, String> filter) {
    Objects.requireNonNull(request);
    Objects.requireNonNull(filter);
    Builder b = newBuilder().uri(request.uri()).expectContinue(request.expectContinue());
    request.timeout().ifPresent(b::timeout);
    request.version().ifPresent(b::version);
    request.headers().map().forEach((name, values) -> {
      for (String v : values) {
        if (filter.test(name, v)) {
          b.header(name, v);
        }
      }
    });
    b.method(request.method(), request.bodyPublisher().orElse(null));
    return b;
  }

  public static Builder newBuilder(URI uri) {
    return new BuilderImpl(uri);
  }

  public abstract Optional<BodyPublisher> bodyPublisher();

  public abstract String method();

  public abstract Optional<Duration> timeout();

  public abstract boolean expectContinue();

  public abstract URI uri();

  public abstract Optional<HttpClient.Version> version();

  public abstract HttpHeaders headers();

  public interface Builder {
    Builder uri(URI uri);
    Builder expectContinue(boolean enable);
    Builder version(HttpClient.Version version);
    Builder timeout(Duration duration);
    Builder setHeader(String name, String value);
    Builder header(String name, String value);
    Builder headers(String... headers);
    Builder GET();
    Builder POST(BodyPublisher bodyPublisher);
    Builder PUT(BodyPublisher bodyPublisher);
    Builder DELETE();

    default Builder HEAD() {
      return method("HEAD", BodyPublishers.noBody());
    }

    Builder method(String method, BodyPublisher bodyPublisher);
    Builder copy();
    HttpRequest build();
  }

  public interface BodyPublisher extends Flow.Publisher<ByteBuffer> {
    long contentLength();
  }

  public static class BodyPublishers {
    private BodyPublishers() {}

    public static BodyPublisher ofByteArray(byte[] buf) {
      Objects.requireNonNull(buf, "buf");
      return ofByteArray(buf, 0, buf.length);
    }

    public static BodyPublisher ofByteArray(byte[] buf, int offset, int length) {
      Objects.checkFromIndexSize(offset, length, buf.length);
      byte[] copy = new byte[length];
      System.arraycopy(buf, offset, copy, 0, length);
      return new ByteArrayPublisher(copy);
    }

    public static BodyPublisher ofString(String body) {
      return ofString(body, StandardCharsets.UTF_8);
    }

    public static BodyPublisher ofString(String s, Charset charset) {
      Objects.requireNonNull(s, "s");
      Objects.requireNonNull(charset, "charset");
      return new ByteArrayPublisher(s.getBytes(charset));
    }

    public static BodyPublisher ofInputStream(Supplier<? extends InputStream> streamSupplier) {
      Objects.requireNonNull(streamSupplier, "streamSupplier");
      return new InputStreamPublisher(streamSupplier);
    }

    public static BodyPublisher fromPublisher(Flow.Publisher<? extends ByteBuffer> publisher) {
      Objects.requireNonNull(publisher, "publisher");
      return new StreamPublisher(publisher, -1);
    }

    public static BodyPublisher fromPublisher(Flow.Publisher<? extends ByteBuffer> publisher, long contentLength) {
      Objects.requireNonNull(publisher, "publisher");
      if (contentLength < 1) {
        throw new IllegalArgumentException("non-positive contentLength: " + contentLength);
      }
      return new StreamPublisher(publisher, contentLength);
    }

    public static BodyPublisher ofFile(java.nio.file.Path path) throws java.io.FileNotFoundException {
      Objects.requireNonNull(path, "path");
      if (!java.nio.file.Files.isRegularFile(path)) {
        throw new java.io.FileNotFoundException(path + " not found");
      }
      try {
        return new ByteArrayPublisher(java.nio.file.Files.readAllBytes(path));
      } catch (java.io.IOException e) {
        throw new java.io.UncheckedIOException(e);
      }
    }

    public static BodyPublisher ofByteArrays(Iterable<byte[]> iter) {
      Objects.requireNonNull(iter, "iter");
      java.io.ByteArrayOutputStream all = new java.io.ByteArrayOutputStream();
      for (byte[] b : iter) {
        all.write(b, 0, b.length);
      }
      return new StreamPublisher(ofByteArray(all.toByteArray()), -1);
    }

    private static class StreamPublisher implements BodyPublisher {
      private final Flow.Publisher<? extends ByteBuffer> publisher;
      private final long length;

      StreamPublisher(Flow.Publisher<? extends ByteBuffer> publisher, long length) {
        this.publisher = publisher;
        this.length = length;
      }

      @Override
      public long contentLength() {
        return length;
      }

      @Override
      @SuppressWarnings("unchecked")
      public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        ((Flow.Publisher<ByteBuffer>) publisher).subscribe(subscriber);
      }
    }

    public static BodyPublisher noBody() {
      return new NoBodyPublisher();
    }

    public static BodyPublisher concat(BodyPublisher... publishers) {
      Objects.requireNonNull(publishers, "publishers");
      long total = 0;
      List<byte[]> all = new ArrayList<byte[]>();
      for (BodyPublisher p : publishers) {
        if (p instanceof ByteArrayPublisher) {
          byte[] b = ((ByteArrayPublisher) p).bytes;
          all.add(b);
          total += b.length;
        } else if (p.contentLength() == 0) {
          continue;
        } else {
          throw new UnsupportedOperationException("Publisher type not supported for concat");
        }
      }
      byte[] combined = new byte[(int) total];
      int pos = 0;
      for (byte[] b : all) {
        System.arraycopy(b, 0, combined, pos, b.length);
        pos += b.length;
      }
      return new ByteArrayPublisher(combined);
    }

    private static class ByteArrayPublisher implements BodyPublisher {
      final byte[] bytes;

      ByteArrayPublisher(byte[] bytes) {
        this.bytes = bytes;
      }

      @Override
      public long contentLength() {
        return bytes.length;
      }

      @Override
      public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        Objects.requireNonNull(subscriber);
        subscriber.onSubscribe(new Flow.Subscription() {
          private boolean sent = false;

          @Override
          public void request(long n) {
            if (n > 0 && !sent) {
              sent = true;
              subscriber.onNext(ByteBuffer.wrap(bytes));
              subscriber.onComplete();
            }
          }

          @Override
          public void cancel() {
            sent = true;
          }
        });
      }
    }

    private static class NoBodyPublisher implements BodyPublisher {
      @Override
      public long contentLength() {
        return 0;
      }

      @Override
      public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        Objects.requireNonNull(subscriber);
        subscriber.onSubscribe(new Flow.Subscription() {
          @Override
          public void request(long n) {
            subscriber.onComplete();
          }

          @Override
          public void cancel() {}
        });
      }
    }

    private static class InputStreamPublisher implements BodyPublisher {
      private final Supplier<? extends InputStream> supplier;

      InputStreamPublisher(Supplier<? extends InputStream> supplier) {
        this.supplier = supplier;
      }

      @Override
      public long contentLength() {
        return -1;
      }

      @Override
      public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        Objects.requireNonNull(subscriber);
        try (InputStream in = supplier.get()) {
          subscriber.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(long n) {
              try {
                byte[] buf = new byte[8192];
                int read;
                while ((read = in.read(buf)) != -1) {
                  subscriber.onNext(ByteBuffer.wrap(buf, 0, read));
                }
                subscriber.onComplete();
              } catch (Throwable t) {
                subscriber.onError(t);
              }
            }

            @Override
            public void cancel() {}
          });
        } catch (Throwable t) {
          subscriber.onError(t);
        }
      }
    }
  }

  private static class BuilderImpl implements Builder {
    private URI uri;
    private String method = "GET";
    private BodyPublisher publisher;
    private Duration timeout;
    private HttpClient.Version version;
    private boolean expectContinue;
    private final Map<String, List<String>> headers = new java.util.TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);

    BuilderImpl() {}

    BuilderImpl(URI uri) {
      this.uri = Objects.requireNonNull(uri, "uri");
    }

    @Override
    public Builder uri(URI uri) {
      this.uri = Objects.requireNonNull(uri, "uri");
      return this;
    }

    @Override
    public Builder HEAD() {
      this.method = "HEAD";
      this.publisher = null;
      return this;
    }

    @Override
    public Builder expectContinue(boolean enable) {
      this.expectContinue = enable;
      return this;
    }

    @Override
    public Builder version(HttpClient.Version version) {
      this.version = version;
      return this;
    }

    @Override
    public Builder timeout(Duration duration) {
      this.timeout = duration;
      return this;
    }

    @Override
    public Builder setHeader(String name, String value) {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(value, "value");
      List<String> list = new ArrayList<String>();
      list.add(value);
      headers.put(name, list);
      return this;
    }

    @Override
    public Builder header(String name, String value) {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(value, "value");
      List<String> list = headers.get(name);
      if (list == null) {
        list = new ArrayList<String>();
        headers.put(name, list);
      }
      list.add(value);
      return this;
    }

    @Override
    public Builder headers(String... headers) {
      if (headers == null || headers.length % 2 != 0) {
        throw new IllegalArgumentException("Headers array length must be even");
      }
      for (int i = 0; i < headers.length; i += 2) {
        header(headers[i], headers[i + 1]);
      }
      return this;
    }

    @Override
    public Builder GET() {
      return method("GET", null);
    }

    @Override
    public Builder POST(BodyPublisher bodyPublisher) {
      return method("POST", Objects.requireNonNull(bodyPublisher, "bodyPublisher"));
    }

    @Override
    public Builder PUT(BodyPublisher bodyPublisher) {
      return method("PUT", Objects.requireNonNull(bodyPublisher, "bodyPublisher"));
    }

    @Override
    public Builder DELETE() {
      return method("DELETE", null);
    }

    @Override
    public Builder method(String method, BodyPublisher bodyPublisher) {
      this.method = Objects.requireNonNull(method, "method");
      this.publisher = bodyPublisher;
      return this;
    }

    @Override
    public Builder copy() {
      BuilderImpl b = new BuilderImpl();
      b.uri = this.uri;
      b.method = this.method;
      b.publisher = this.publisher;
      b.timeout = this.timeout;
      b.version = this.version;
      b.expectContinue = this.expectContinue;
      for (Map.Entry<String, List<String>> e : this.headers.entrySet()) {
        b.headers.put(e.getKey(), new ArrayList<String>(e.getValue()));
      }
      return b;
    }

    @Override
    public HttpRequest build() {
      if (uri == null) {
        throw new IllegalStateException("URI must be set");
      }
      return new HttpRequestImpl(this);
    }
  }

  private static class HttpRequestImpl extends HttpRequest {
    private final URI uri;
    private final String method;
    private final Optional<BodyPublisher> publisher;
    private final Optional<Duration> timeout;
    private final Optional<HttpClient.Version> version;
    private final boolean expectContinue;
    private final HttpHeaders headers;

    HttpRequestImpl(BuilderImpl b) {
      this.uri = b.uri;
      this.method = b.method;
      this.publisher = Optional.ofNullable(b.publisher);
      this.timeout = Optional.ofNullable(b.timeout);
      this.version = Optional.ofNullable(b.version);
      this.expectContinue = b.expectContinue;
      this.headers = HttpHeaders.of(b.headers, (k, v) -> true);
    }

    @Override
    public Optional<BodyPublisher> bodyPublisher() {
      return publisher;
    }

    @Override
    public String method() {
      return method;
    }

    @Override
    public Optional<Duration> timeout() {
      return timeout;
    }

    @Override
    public boolean expectContinue() {
      return expectContinue;
    }

    @Override
    public URI uri() {
      return uri;
    }

    @Override
    public Optional<HttpClient.Version> version() {
      return version;
    }

    @Override
    public HttpHeaders headers() {
      return headers;
    }
  }
}
