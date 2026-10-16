/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

public interface CompletionHandler<V, A> {
  void completed(V result, A attachment);

  void failed(Throwable exc, A attachment);
}
