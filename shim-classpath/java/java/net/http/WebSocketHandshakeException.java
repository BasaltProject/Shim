/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

import java.io.IOException;

public final class WebSocketHandshakeException extends IOException {
  private final HttpResponse<?> response;

  public WebSocketHandshakeException(HttpResponse<?> response) {
    super("WebSocket handshake failed");
    this.response = response;
  }

  public HttpResponse<?> getResponse() {
    return response;
  }

  @Override
  public WebSocketHandshakeException initCause(Throwable cause) {
    super.initCause(cause);
    return this;
  }
}
