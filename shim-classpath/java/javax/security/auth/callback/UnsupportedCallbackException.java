/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.callback;

public class UnsupportedCallbackException extends Exception {
  private static final long serialVersionUID = -6873556327655666839L;

  private Callback callback;

  public UnsupportedCallbackException(Callback callback) {
    super();
    this.callback = callback;
  }

  public UnsupportedCallbackException(Callback callback, String msg) {
    super(msg);
    this.callback = callback;
  }

  public Callback getCallback() {
    return callback;
  }
}
