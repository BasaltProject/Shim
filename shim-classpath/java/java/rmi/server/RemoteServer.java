/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

import java.io.OutputStream;
import java.io.PrintStream;
import sun.rmi.transport.TCPTransport;

public abstract class RemoteServer extends RemoteObject {
  private static final long serialVersionUID = -4100238210092549637L;

  private static volatile PrintStream logStream;

  protected RemoteServer() {
    super();
  }

  protected RemoteServer(RemoteRef ref) {
    super(ref);
  }

  public static String getClientHost() throws ServerNotActiveException {
    String h = TCPTransport.currentClientHost();
    if (h == null) {
      throw new ServerNotActiveException("not in a remote call");
    }
    return h;
  }

  public static void setLog(OutputStream out) {
    logStream = (out == null) ? null : ((out instanceof PrintStream) ? (PrintStream) out : new PrintStream(out));
    LogStream.setDefaultStream(logStream);
  }

  public static PrintStream getLog() {
    return logStream;
  }
}
