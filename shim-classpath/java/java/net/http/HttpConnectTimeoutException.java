/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.http;

public class HttpConnectTimeoutException extends HttpTimeoutException {
  public HttpConnectTimeoutException(String message) {
    super(message);
  }
}
