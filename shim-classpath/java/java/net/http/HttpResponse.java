/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface HttpResponse<T> {

  int statusCode();

  HttpRequest request();

  Optional<HttpResponse<T>> previousResponse();

  HttpHeaders headers();

  T body();

  Optional<Object> sslSession();

  URI uri();

  HttpClient.Version version();

  public interface ResponseInfo {
    int statusCode();
    HttpHeaders headers();
    HttpClient.Version version();
  }

  @FunctionalInterface
  public interface BodyHandler<T> {
    BodySubscriber<T> apply(ResponseInfo responseInfo);
  }

  public interface BodySubscriber<T> extends Flow.Subscriber<List<ByteBuffer>> {
    CompletionStage<T> getBody();
  }

  @FunctionalInterface
  public interface PushPromiseHandler<T> {
    void applyPushPromise(
        HttpRequest initiatingRequest,
        HttpRequest pushPromiseRequest,
        Function<HttpResponse.BodyHandler<T>, CompletableFuture<HttpResponse<T>>> acceptor);
  }

  public static class BodyHandlers {
    private BodyHandlers() {}

    public static BodyHandler<String> ofString() {
      return ofString(StandardCharsets.UTF_8);
    }

    public static BodyHandler<String> ofString(Charset charset) {
      Objects.requireNonNull(charset, "charset");
      return responseInfo -> BodySubscribers.ofString(charset);
    }

    public static BodyHandler<byte[]> ofByteArray() {
      return responseInfo -> BodySubscribers.ofByteArray();
    }

    public static BodyHandler<Stream<String>> ofLines() {
      return responseInfo -> BodySubscribers.ofLines(StandardCharsets.UTF_8);
    }

    public static BodyHandler<InputStream> ofInputStream() {
      return responseInfo -> BodySubscribers.ofInputStream();
    }

    public static BodyHandler<Void> discarding() {
      return responseInfo -> BodySubscribers.discarding();
    }

    public static <U> BodyHandler<U> replacing(U value) {
      return responseInfo -> BodySubscribers.replacing(value);
    }

    public static <T> BodyHandler<T> fromSubscriber(BodySubscriber<T> subscriber) {
      Objects.requireNonNull(subscriber, "subscriber");
      return responseInfo -> subscriber;
    }

    public static <T, S extends BodySubscriber<T>> BodyHandler<T> fromSubscriber(
        S subscriber, Function<? super S, ? extends T> finisher)
    {
      Objects.requireNonNull(subscriber, "subscriber");
      Objects.requireNonNull(finisher, "finisher");
      return responseInfo -> new BodySubscriber<T>() {
        @Override
        public CompletionStage<T> getBody() {
          return subscriber.getBody().thenApply(ignored -> finisher.apply(subscriber));
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
          subscriber.onSubscribe(subscription);
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
          subscriber.onNext(item);
        }

        @Override
        public void onError(Throwable throwable) {
          subscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
          subscriber.onComplete();
        }
      };
    }
  }

  public static class BodySubscribers {
    private BodySubscribers() {}

    public static BodySubscriber<String> ofString(Charset charset) {
      Objects.requireNonNull(charset, "charset");
      return mapping(ofByteArray(), bytes -> new String(bytes, charset));
    }

    public static BodySubscriber<byte[]> ofByteArray() {
      return new ByteArraySubscriber();
    }

    public static BodySubscriber<Stream<String>> ofLines(Charset charset) {
      Objects.requireNonNull(charset, "charset");
      return mapping(ofInputStream(), in -> new BufferedReader(new InputStreamReader(in, charset)).lines());
    }

    public static BodySubscriber<InputStream> ofInputStream() {
      return mapping(ofByteArray(), ByteArrayInputStream::new);
    }

    public static BodySubscriber<Void> discarding() {
      return mapping(ofByteArray(), bytes -> null);
    }

    public static <U> BodySubscriber<U> replacing(U value) {
      return mapping(ofByteArray(), bytes -> value);
    }

    public static <T, U> BodySubscriber<U> mapping(BodySubscriber<T> upstream, Function<? super T, ? extends U> mapper) {
      Objects.requireNonNull(upstream, "upstream");
      Objects.requireNonNull(mapper, "mapper");
      return new BodySubscriber<U>() {
        @Override
        public CompletionStage<U> getBody() {
          return upstream.getBody().thenApply(mapper);
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
          upstream.onSubscribe(subscription);
        }

        @Override
        public void onNext(List<ByteBuffer> item) {
          upstream.onNext(item);
        }

        @Override
        public void onError(Throwable throwable) {
          upstream.onError(throwable);
        }

        @Override
        public void onComplete() {
          upstream.onComplete();
        }
      };
    }

    private static class ByteArraySubscriber implements BodySubscriber<byte[]> {
      private final CompletableFuture<byte[]> future = new CompletableFuture<byte[]>();
      private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      @Override
      public CompletionStage<byte[]> getBody() {
        return future;
      }

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        if (subscription != null) {
          subscription.request(Long.MAX_VALUE);
        }
      }

      @Override
      public void onNext(List<ByteBuffer> item) {
        if (item != null) {
          for (ByteBuffer b : item) {
            if (b != null && b.hasRemaining()) {
              if (b.hasArray()) {
                baos.write(b.array(), b.arrayOffset() + b.position(), b.remaining());
                b.position(b.limit());
              } else {
                byte[] chunk = new byte[b.remaining()];
                b.get(chunk);
                baos.write(chunk, 0, chunk.length);
              }
            }
          }
        }
      }

      @Override
      public void onError(Throwable throwable) {
        future.completeExceptionally(throwable);
      }

      @Override
      public void onComplete() {
        future.complete(baos.toByteArray());
      }
    }
  }
}
