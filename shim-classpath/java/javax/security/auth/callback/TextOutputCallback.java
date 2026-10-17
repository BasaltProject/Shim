/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.callback;

public class TextOutputCallback implements Callback, java.io.Serializable {
  private static final long serialVersionUID = 1689502495511663102L;

  public static final int INFORMATION = 0;
  public static final int WARNING = 1;
  public static final int ERROR = 2;

  private final int messageType;
  private final String message;

  public TextOutputCallback(int messageType, String message) {
    if ((messageType != INFORMATION && messageType != WARNING && messageType != ERROR) || message == null
        || message.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.messageType = messageType;
    this.message = message;
  }

  public int getMessageType() {
    return messageType;
  }

  public String getMessage() {
    return message;
  }
}
