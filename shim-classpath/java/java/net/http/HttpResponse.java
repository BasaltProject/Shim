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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.net.ssl.SSLSession;

public interface HttpResponse<T> {

  int statusCode();

  HttpRequest request();

  Optional<HttpResponse<T>> previousResponse();

  HttpHeaders headers();

  T body();

  Optional<SSLSession> sslSession();

  default Optional<String> connectionLabel() {
    return Optional.empty();
  }

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

    public static <T> PushPromiseHandler<T> of(Function<HttpRequest, BodyHandler<T>> pushPromiseHandler,
        ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<T>>> pushPromisesMap) {
      return (initiatingRequest, pushPromiseRequest, acceptor) -> {
        CompletableFuture<HttpResponse<T>> cf = acceptor.apply(pushPromiseHandler.apply(pushPromiseRequest));
        pushPromisesMap.put(pushPromiseRequest, cf);
      };
    }
  }

  public static class BodyHandlers {
    private BodyHandlers() {}

    public static <T> BodyHandler<T> fromSubscriber(Flow.Subscriber<? super List<ByteBuffer>> subscriber,
        Function<? super Flow.Subscriber<? super List<ByteBuffer>>, ? extends T> finisher) {
      return fromSubscriberImpl(subscriber, finisher);
    }

    @SuppressWarnings("unchecked")
    private static <T, S extends Flow.Subscriber<? super List<ByteBuffer>>> BodyHandler<T> fromSubscriberImpl(
        Flow.Subscriber<? super List<ByteBuffer>> subscriber,
        Function<? super Flow.Subscriber<? super List<ByteBuffer>>, ? extends T> finisher) {
      Objects.requireNonNull(subscriber, "subscriber");
      Objects.requireNonNull(finisher, "finisher");
      return responseInfo -> BodySubscribers.fromSubscriber(subscriber, finisher);
    }

    public static BodyHandler<Void> fromSubscriber(Flow.Subscriber<? super List<ByteBuffer>> subscriber) {
      Objects.requireNonNull(subscriber, "subscriber");
      return responseInfo -> BodySubscribers.fromSubscriber(subscriber);
    }

    public static BodyHandler<Void> fromLineSubscriber(Flow.Subscriber<? super String> subscriber) {
      Objects.requireNonNull(subscriber, "subscriber");
      return responseInfo -> BodySubscribers.fromLineSubscriber(subscriber, s -> null,
          charsetFrom(responseInfo.headers()), null);
    }

    public static <S extends Flow.Subscriber<? super String>, T> BodyHandler<T> fromLineSubscriber(S subscriber,
        Function<? super S, ? extends T> finisher, String lineSeparator) {
      Objects.requireNonNull(subscriber, "subscriber");
      Objects.requireNonNull(finisher, "finisher");
      if (lineSeparator != null && lineSeparator.isEmpty()) {
        throw new IllegalArgumentException("empty line separator");
      }
      return responseInfo -> BodySubscribers.fromLineSubscriber(subscriber, finisher,
          charsetFrom(responseInfo.headers()), lineSeparator);
    }

    public static BodyHandler<Void> discarding() {
      return responseInfo -> BodySubscribers.discarding();
    }

    public static <U> BodyHandler<U> replacing(U value) {
      return responseInfo -> BodySubscribers.replacing(value);
    }

    public static BodyHandler<String> ofString() {
      return responseInfo -> BodySubscribers.ofString(charsetFrom(responseInfo.headers()));
    }

    public static BodyHandler<String> ofString(Charset charset) {
      Objects.requireNonNull(charset, "charset");
      return responseInfo -> BodySubscribers.ofString(charset);
    }

    public static BodyHandler<Path> ofFile(Path file, OpenOption... openOptions) {
      Objects.requireNonNull(file);
      List<OpenOption> opts = List.of(openOptions);
      if (opts.contains(StandardOpenOption.DELETE_ON_CLOSE) || opts.contains(StandardOpenOption.READ)) {
        throw new IllegalArgumentException("invalid openOptions: " + opts);
      }
      return responseInfo -> BodySubscribers.ofFile(file, openOptions);
    }

    public static BodyHandler<Path> ofFile(Path file) {
      return ofFile(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public static BodyHandler<Path> ofFileDownload(Path directory, OpenOption... openOptions) {
      Objects.requireNonNull(directory);
      List<OpenOption> opts = List.of(openOptions);
      if (!Files.exists(directory)) {
        throw new IllegalArgumentException("non-existent directory: " + directory);
      }
      if (!Files.isDirectory(directory)) {
        throw new IllegalArgumentException("not a directory: " + directory);
      }
      if (!Files.isWritable(directory)) {
        throw new IllegalArgumentException("non-writable directory: " + directory);
      }
      return responseInfo -> {
        String disposition = responseInfo.headers().firstValue("Content-Disposition")
            .orElseThrow(() -> new UncheckedIOException(new IOException("No Content-Disposition header")));
        String name = null;
        for (String part : disposition.split(";")) {
          String t = part.trim();
          if (t.regionMatches(true, 0, "filename=", 0, 9)) {
            name = t.substring(9).trim();
            if (name.length() >= 2 && name.startsWith("\"") && name.endsWith("\"")) {
              name = name.substring(1, name.length() - 1);
            }
          }
        }
        if (name == null || name.isEmpty()) {
          throw new UncheckedIOException(new IOException("Bad Content-Disposition filename parameter"));
        }
        Path target = directory.resolve(name).normalize();
        if (!target.startsWith(directory.normalize())) {
          throw new UncheckedIOException(new IOException("Resulting file, " + target + ", outside of given directory"));
        }
        return BodySubscribers.ofFile(target, openOptions.length == 0
            ? new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.WRITE} : openOptions);
      };
    }

    public static BodyHandler<InputStream> ofInputStream() {
      return responseInfo -> BodySubscribers.ofInputStream();
    }

    public static BodyHandler<Stream<String>> ofLines() {
      return responseInfo -> BodySubscribers.ofLines(charsetFrom(responseInfo.headers()));
    }

    public static BodyHandler<Void> ofByteArrayConsumer(Consumer<Optional<byte[]>> consumer) {
      Objects.requireNonNull(consumer);
      return responseInfo -> BodySubscribers.ofByteArrayConsumer(consumer);
    }

    public static BodyHandler<byte[]> ofByteArray() {
      return responseInfo -> BodySubscribers.ofByteArray();
    }

    public static BodyHandler<Flow.Publisher<List<ByteBuffer>>> ofPublisher() {
      return responseInfo -> BodySubscribers.ofPublisher();
    }

    public static <T> BodyHandler<T> buffering(BodyHandler<T> downstreamHandler, int bufferSize) {
      Objects.requireNonNull(downstreamHandler);
      if (bufferSize <= 0) {
        throw new IllegalArgumentException("must be greater than 0");
      }
      return responseInfo -> BodySubscribers.buffering(downstreamHandler.apply(responseInfo), bufferSize);
    }

    public static <T> BodyHandler<T> limiting(BodyHandler<T> downstreamHandler, long capacity) {
      Objects.requireNonNull(downstreamHandler);
      if (capacity < 0) {
        throw new IllegalArgumentException("capacity must not be negative: " + capacity);
      }
      return responseInfo -> BodySubscribers.limiting(downstreamHandler.apply(responseInfo), capacity);
    }

    static Charset charsetFrom(HttpHeaders headers) {
      String type = headers.firstValue("Content-type").orElse("text/html; charset=utf-8");
      int i = type.indexOf(';');
      if (i >= 0) {
        for (String param : type.substring(i + 1).split(";")) {
          String t = param.trim();
          if (t.regionMatches(true, 0, "charset=", 0, 8)) {
            String cs = t.substring(8).trim();
            if (cs.length() >= 2 && cs.startsWith("\"") && cs.endsWith("\"")) {
              cs = cs.substring(1, cs.length() - 1);
            }
            try {
              return Charset.forName(cs);
            } catch (IllegalArgumentException e) {
              return StandardCharsets.UTF_8;
            }
          }
        }
      }
      return StandardCharsets.UTF_8;
    }
  }

  public static class BodySubscribers {
    private BodySubscribers() {}

    public static BodySubscriber<Void> fromSubscriber(Flow.Subscriber<? super List<ByteBuffer>> subscriber) {
      Objects.requireNonNull(subscriber, "subscriber");
      return new RelaySubscriber<Void>(subscriber, s -> null);
    }

    public static <S extends Flow.Subscriber<? super List<ByteBuffer>>, T> BodySubscriber<T> fromSubscriber(
        S subscriber, Function<? super S, ? extends T> finisher) {
      Objects.requireNonNull(subscriber, "subscriber");
      Objects.requireNonNull(finisher, "finisher");
      return new RelaySubscriber<T>(subscriber, s -> finisher.apply(subscriber));
    }

    public static BodySubscriber<Void> fromLineSubscriber(Flow.Subscriber<? super String> subscriber) {
      Objects.requireNonNull(subscriber, "subscriber");
      return fromLineSubscriber(subscriber, s -> null, StandardCharsets.UTF_8, null);
    }

    public static <S extends Flow.Subscriber<? super String>, T> BodySubscriber<T> fromLineSubscriber(S subscriber,
        Function<? super S, ? extends T> finisher, Charset charset, String lineSeparator) {
      Objects.requireNonNull(subscriber, "subscriber");
      Objects.requireNonNull(finisher, "finisher");
      Objects.requireNonNull(charset, "charset");
      if (lineSeparator != null && lineSeparator.isEmpty()) {
        throw new IllegalArgumentException("empty line separator");
      }
      return new LineSubscriber<S, T>(subscriber, finisher, charset, lineSeparator);
    }

    public static BodySubscriber<String> ofString(Charset charset) {
      Objects.requireNonNull(charset, "charset");
      return mapping(ofByteArray(), bytes -> new String(bytes, charset));
    }

    public static BodySubscriber<byte[]> ofByteArray() {
      return new ByteArraySubscriber();
    }

    public static BodySubscriber<Path> ofFile(Path file, OpenOption... openOptions) {
      Objects.requireNonNull(file);
      List<OpenOption> opts = List.of(openOptions);
      if (opts.contains(StandardOpenOption.DELETE_ON_CLOSE) || opts.contains(StandardOpenOption.READ)) {
        throw new IllegalArgumentException("invalid openOptions: " + opts);
      }
      return new FileSubscriber(file, openOptions);
    }

    public static BodySubscriber<Path> ofFile(Path file) {
      return ofFile(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public static BodySubscriber<Void> ofByteArrayConsumer(Consumer<Optional<byte[]>> consumer) {
      Objects.requireNonNull(consumer);
      return new ConsumerSubscriber(consumer);
    }

    public static BodySubscriber<InputStream> ofInputStream() {
      return mapping(ofByteArray(), ByteArrayInputStream::new);
    }

    public static BodySubscriber<Stream<String>> ofLines(Charset charset) {
      Objects.requireNonNull(charset, "charset");
      return mapping(ofInputStream(), in -> new BufferedReader(new InputStreamReader(in, charset)).lines());
    }

    public static BodySubscriber<Flow.Publisher<List<ByteBuffer>>> ofPublisher() {
      return new PublisherSubscriber();
    }

    public static <U> BodySubscriber<U> replacing(U value) {
      return mapping(discardingBytes(), v -> value);
    }

    public static BodySubscriber<Void> discarding() {
      return replacing(null);
    }

    private static BodySubscriber<byte[]> discardingBytes() {
      return new ByteArraySubscriber(true);
    }

    public static <T> BodySubscriber<T> buffering(BodySubscriber<T> downstream, int bufferSize) {
      Objects.requireNonNull(downstream);
      if (bufferSize <= 0) {
        throw new IllegalArgumentException("must be greater than 0");
      }
      return new BufferingSubscriber<T>(downstream, bufferSize);
    }

    public static <T> BodySubscriber<T> limiting(BodySubscriber<T> downstream, long capacity) {
      Objects.requireNonNull(downstream);
      if (capacity < 0) {
        throw new IllegalArgumentException("capacity must not be negative: " + capacity);
      }
      return new LimitingSubscriber<T>(downstream, capacity);
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

    private static final class RelaySubscriber<T> implements BodySubscriber<T> {
      private final Flow.Subscriber<? super List<ByteBuffer>> downstream;
      private final Function<Object, ? extends T> finisher;
      private final CompletableFuture<T> result = new CompletableFuture<T>();

      RelaySubscriber(Flow.Subscriber<? super List<ByteBuffer>> downstream, Function<Object, ? extends T> finisher) {
        this.downstream = downstream;
        this.finisher = finisher;
      }

      @Override
      public CompletionStage<T> getBody() {
        return result;
      }

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        downstream.onSubscribe(subscription);
      }

      @Override
      public void onNext(List<ByteBuffer> item) {
        downstream.onNext(item);
      }

      @Override
      public void onError(Throwable throwable) {
        try {
          downstream.onError(throwable);
        } finally {
          result.completeExceptionally(throwable);
        }
      }

      @Override
      public void onComplete() {
        try {
          downstream.onComplete();
          result.complete(finisher.apply(downstream));
        } catch (Throwable t) {
          result.completeExceptionally(t);
        }
      }
    }

    private static final class LineSubscriber<S extends Flow.Subscriber<? super String>, T>
        implements BodySubscriber<T> {
      private final S downstream;
      private final Function<? super S, ? extends T> finisher;
      private final Charset charset;
      private final String separator;
      private final CompletableFuture<T> result = new CompletableFuture<T>();
      private final java.util.ArrayDeque<String> lines = new java.util.ArrayDeque<String>();
      private final StringBuilder partial = new StringBuilder();
      private final java.io.ByteArrayOutputStream pending = new java.io.ByteArrayOutputStream();
      private long demand;
      private boolean completed;
      private boolean delivered;
      private boolean cancelled;
      private Flow.Subscription upstream;
      private boolean lastWasCr;

      LineSubscriber(S downstream, Function<? super S, ? extends T> finisher, Charset charset, String separator) {
        this.downstream = downstream;
        this.finisher = finisher;
        this.charset = charset;
        this.separator = separator;
      }

      @Override
      public CompletionStage<T> getBody() {
        return result;
      }

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        this.upstream = subscription;
        downstream.onSubscribe(new Flow.Subscription() {
          @Override
          public void request(long n) {
            if (n <= 0) {
              downstream.onError(new IllegalArgumentException("non-positive subscription request"));
              return;
            }
            synchronized (LineSubscriber.this) {
              demand = demand + n < 0 ? Long.MAX_VALUE : demand + n;
            }
            drain();
          }

          @Override
          public void cancel() {
            cancelled = true;
            subscription.cancel();
          }
        });
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(List<ByteBuffer> item) {
        for (ByteBuffer b : item) {
          byte[] chunk = new byte[b.remaining()];
          b.get(chunk);
          pending.write(chunk, 0, chunk.length);
        }
        String decoded = decodeAvailable();
        split(decoded);
        drain();
      }

      private String decodeAvailable() {
        byte[] bytes = pending.toByteArray();
        java.nio.charset.CharsetDecoder dec = charset.newDecoder()
            .onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE)
            .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPLACE);
        ByteBuffer in = ByteBuffer.wrap(bytes);
        java.nio.CharBuffer out = java.nio.CharBuffer.allocate(bytes.length * 2 + 4);
        dec.decode(in, out, false);
        pending.reset();
        pending.write(bytes, in.position(), bytes.length - in.position());
        out.flip();
        return out.toString();
      }

      private void split(String text) {
        if (separator != null) {
          partial.append(text);
          int idx;
          while ((idx = partial.indexOf(separator)) >= 0) {
            lines.add(partial.substring(0, idx));
            partial.delete(0, idx + separator.length());
          }
          return;
        }
        for (int i = 0; i < text.length(); i++) {
          char c = text.charAt(i);
          if (lastWasCr) {
            lastWasCr = false;
            if (c == '\n') {
              continue;
            }
          }
          if (c == '\r') {
            lines.add(partial.toString());
            partial.setLength(0);
            lastWasCr = true;
          } else if (c == '\n') {
            lines.add(partial.toString());
            partial.setLength(0);
          } else {
            partial.append(c);
          }
        }
      }

      private void drain() {
        while (!cancelled) {
          String line;
          synchronized (this) {
            if (demand == 0 || lines.isEmpty()) {
              break;
            }
            line = lines.poll();
            if (demand != Long.MAX_VALUE) {
              demand--;
            }
          }
          downstream.onNext(line);
        }
        if (completed && lines.isEmpty() && !delivered && !cancelled) {
          delivered = true;
          downstream.onComplete();
        }
      }

      @Override
      public void onError(Throwable throwable) {
        downstream.onError(throwable);
        result.completeExceptionally(throwable);
      }

      @Override
      public void onComplete() {
        if (pending.size() > 0) {
          split(new String(pending.toByteArray(), charset));
          pending.reset();
        }
        if (partial.length() > 0) {
          lines.add(partial.toString());
          partial.setLength(0);
        }
        completed = true;
        result.complete(finisher.apply(downstream));
        drain();
      }
    }

    private static final class FileSubscriber implements BodySubscriber<Path> {
      private final Path file;
      private final java.nio.channels.FileChannel channel;
      private final CompletableFuture<Path> result = new CompletableFuture<Path>();

      FileSubscriber(Path file, OpenOption[] options) {
        this.file = file;
        java.nio.channels.FileChannel ch;
        try {
          java.util.Set<OpenOption> set = new java.util.HashSet<OpenOption>(java.util.Arrays.asList(options));
          set.add(StandardOpenOption.WRITE);
          ch = java.nio.channels.FileChannel.open(file, set);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
        this.channel = ch;
      }

      @Override
      public CompletionStage<Path> getBody() {
        return result;
      }

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(List<ByteBuffer> item) {
        try {
          for (ByteBuffer b : item) {
            while (b.hasRemaining()) {
              channel.write(b);
            }
          }
        } catch (IOException e) {
          onError(e);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        try {
          channel.close();
        } catch (IOException ignored) {
        }
        result.completeExceptionally(throwable);
      }

      @Override
      public void onComplete() {
        try {
          channel.close();
          result.complete(file);
        } catch (IOException e) {
          result.completeExceptionally(e);
        }
      }
    }

    private static final class ConsumerSubscriber implements BodySubscriber<Void> {
      private final Consumer<Optional<byte[]>> consumer;
      private final CompletableFuture<Void> result = new CompletableFuture<Void>();

      ConsumerSubscriber(Consumer<Optional<byte[]>> consumer) {
        this.consumer = consumer;
      }

      @Override
      public CompletionStage<Void> getBody() {
        return result;
      }

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(List<ByteBuffer> items) {
        for (ByteBuffer b : items) {
          byte[] chunk = new byte[b.remaining()];
          b.get(chunk);
          consumer.accept(Optional.of(chunk));
        }
      }

      @Override
      public void onError(Throwable throwable) {
        result.completeExceptionally(throwable);
      }

      @Override
      public void onComplete() {
        consumer.accept(Optional.empty());
        result.complete(null);
      }
    }

    private static final class PublisherSubscriber implements BodySubscriber<Flow.Publisher<List<ByteBuffer>>> {
      private final CompletableFuture<Flow.Publisher<List<ByteBuffer>>> result =
          new CompletableFuture<Flow.Publisher<List<ByteBuffer>>>();
      private final java.util.ArrayDeque<List<ByteBuffer>> queue = new java.util.ArrayDeque<List<ByteBuffer>>();
      private Flow.Subscriber<? super List<ByteBuffer>> subscriber;
      private Flow.Subscription upstream;
      private long demand;
      private boolean done;
      private Throwable error;
      private boolean finished;

      @Override
      public CompletionStage<Flow.Publisher<List<ByteBuffer>>> getBody() {
        return result;
      }

      @Override
      public synchronized void onSubscribe(Flow.Subscription subscription) {
        this.upstream = subscription;
        result.complete(sub -> {
          Objects.requireNonNull(sub);
          synchronized (PublisherSubscriber.this) {
            if (subscriber != null) {
              sub.onSubscribe(new Flow.Subscription() {
                public void request(long n) {
                }

                public void cancel() {
                }
              });
              sub.onError(new IllegalStateException("subscription already taken"));
              return;
            }
            subscriber = sub;
          }
          sub.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(long n) {
              synchronized (PublisherSubscriber.this) {
                demand = demand + n < 0 ? Long.MAX_VALUE : demand + n;
              }
              upstream.request(n);
              drain();
            }

            @Override
            public void cancel() {
              upstream.cancel();
            }
          });
        });
      }

      private void drain() {
        while (true) {
          List<ByteBuffer> item;
          synchronized (this) {
            if (subscriber == null || demand == 0 || queue.isEmpty()) {
              break;
            }
            item = queue.poll();
            if (demand != Long.MAX_VALUE) {
              demand--;
            }
          }
          subscriber.onNext(item);
        }
        synchronized (this) {
          if (subscriber != null && queue.isEmpty() && !finished) {
            if (error != null) {
              finished = true;
              subscriber.onError(error);
            } else if (done) {
              finished = true;
              subscriber.onComplete();
            }
          }
        }
      }

      @Override
      public void onNext(List<ByteBuffer> item) {
        synchronized (this) {
          queue.add(item);
        }
        drain();
      }

      @Override
      public void onError(Throwable throwable) {
        synchronized (this) {
          error = throwable;
        }
        drain();
      }

      @Override
      public void onComplete() {
        synchronized (this) {
          done = true;
        }
        drain();
      }
    }

    private static final class BufferingSubscriber<T> implements BodySubscriber<T> {
      private final BodySubscriber<T> downstream;
      private final int bufferSize;
      private final java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();

      BufferingSubscriber(BodySubscriber<T> downstream, int bufferSize) {
        this.downstream = downstream;
        this.bufferSize = bufferSize;
      }

      @Override
      public CompletionStage<T> getBody() {
        return downstream.getBody();
      }

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        downstream.onSubscribe(subscription);
      }

      @Override
      public void onNext(List<ByteBuffer> item) {
        for (ByteBuffer b : item) {
          byte[] chunk = new byte[b.remaining()];
          b.get(chunk);
          buffer.write(chunk, 0, chunk.length);
        }
        while (buffer.size() >= bufferSize) {
          byte[] all = buffer.toByteArray();
          downstream.onNext(List.of(ByteBuffer.wrap(all, 0, bufferSize).slice()));
          buffer.reset();
          buffer.write(all, bufferSize, all.length - bufferSize);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        downstream.onError(throwable);
      }

      @Override
      public void onComplete() {
        if (buffer.size() > 0) {
          downstream.onNext(List.of(ByteBuffer.wrap(buffer.toByteArray())));
          buffer.reset();
        }
        downstream.onComplete();
      }
    }

    private static final class LimitingSubscriber<T> implements BodySubscriber<T> {
      private final BodySubscriber<T> downstream;
      private final long capacity;
      private long received;
      private boolean failed;
      private Flow.Subscription subscription;

      LimitingSubscriber(BodySubscriber<T> downstream, long capacity) {
        this.downstream = downstream;
        this.capacity = capacity;
      }

      @Override
      public CompletionStage<T> getBody() {
        return downstream.getBody();
      }

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        downstream.onSubscribe(subscription);
      }

      @Override
      public void onNext(List<ByteBuffer> item) {
        if (failed) {
          return;
        }
        long size = 0;
        for (ByteBuffer b : item) {
          size += b.remaining();
        }
        if (received + size > capacity) {
          failed = true;
          subscription.cancel();
          downstream.onError(new IOException("body exceeds capacity: " + capacity));
          return;
        }
        received += size;
        downstream.onNext(item);
      }

      @Override
      public void onError(Throwable throwable) {
        if (!failed) {
          downstream.onError(throwable);
        }
      }

      @Override
      public void onComplete() {
        if (!failed) {
          downstream.onComplete();
        }
      }
    }

    private static class ByteArraySubscriber implements BodySubscriber<byte[]> {
      private final CompletableFuture<byte[]> future = new CompletableFuture<byte[]>();
      private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      private final boolean discard;

      ByteArraySubscriber() {
        this(false);
      }

      ByteArraySubscriber(boolean discard) {
        this.discard = discard;
      }

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
              if (discard) {
                b.position(b.limit());
              } else if (b.hasArray()) {
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
