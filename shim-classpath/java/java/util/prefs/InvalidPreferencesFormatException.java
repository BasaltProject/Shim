/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.prefs;

public class InvalidPreferencesFormatException extends Exception {
  private static final long serialVersionUID = -791715184232119669L;

  public InvalidPreferencesFormatException(Throwable cause) {
    super(cause);
  }

  public InvalidPreferencesFormatException(String message) {
    super(message);
  }

  public InvalidPreferencesFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.NotSerializableException {
    throw new java.io.NotSerializableException("InvalidPreferencesFormatException");
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.NotSerializableException {
    throw new java.io.NotSerializableException("InvalidPreferencesFormatException");
  }
}
