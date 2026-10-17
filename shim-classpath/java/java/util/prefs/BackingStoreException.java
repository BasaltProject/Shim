/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.prefs;

public class BackingStoreException extends Exception {
  private static final long serialVersionUID = 859796500401108469L;

  public BackingStoreException(String s) {
    super(s);
  }

  public BackingStoreException(Throwable cause) {
    super(cause);
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.NotSerializableException {
    throw new java.io.NotSerializableException("BackingStoreException");
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.NotSerializableException {
    throw new java.io.NotSerializableException("BackingStoreException");
  }
}
