/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

public interface RemoteCall {
  void done() throws java.io.IOException;
  void executeCall() throws java.lang.Exception;
  java.io.ObjectInput getInputStream() throws java.io.IOException;
  java.io.ObjectOutput getOutputStream() throws java.io.IOException;
  java.io.ObjectOutput getResultStream(boolean flag) throws java.io.IOException, java.io.StreamCorruptedException;
  void releaseInputStream() throws java.io.IOException;
  void releaseOutputStream() throws java.io.IOException;
}
