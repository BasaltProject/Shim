/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface WebSocket {

  public static final int NORMAL_CLOSURE = 1000;

  public interface Builder {
    Builder header(String name, String value);
    Builder connectTimeout(Duration timeout);
    Builder subprotocols(String mostPreferred, String... lesserPreferred);
    CompletableFuture<WebSocket> buildAsync(URI uri, Listener listener);
  }

  public interface Listener {
    default void onOpen(WebSocket webSocket) {
      webSocket.request(1);
    }

    default CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      webSocket.request(1);
      return null;
    }

    default CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
      webSocket.request(1);
      return null;
    }

    default CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
      webSocket.request(1);
      return null;
    }

    default CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
      webSocket.request(1);
      return null;
    }

    default CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
      return null;
    }

    default void onError(WebSocket webSocket, Throwable error) {}
  }

  CompletableFuture<WebSocket> sendText(CharSequence message, boolean last);
  CompletableFuture<WebSocket> sendBinary(ByteBuffer message, boolean last);
  CompletableFuture<WebSocket> sendPing(ByteBuffer message);
  CompletableFuture<WebSocket> sendPong(ByteBuffer message);
  CompletableFuture<WebSocket> sendClose(int statusCode, String reason);
  void request(long n);
  String getSubprotocol();
  boolean isOutputClosed();
  boolean isInputClosed();
  void abort();
}
