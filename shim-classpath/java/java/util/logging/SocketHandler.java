/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SocketHandler extends StreamHandler {
  private Socket sock;
  private String host;
  private int port;

  public SocketHandler() throws IOException {
    super(Level.ALL, new XMLFormatter(), null);
    LogManager manager = LogManager.getLogManager();
    String cname = getClass().getName();
    port = manager.getIntProperty(cname + ".port", 0);
    host = manager.getStringProperty(cname + ".host", null);
    try {
      connect();
    } catch (IOException ix) {
      System.err.println("SocketHandler: connect failed to " + host + ":" + port);
      throw ix;
    }
  }

  public SocketHandler(String host, int port) throws IOException {
    super(Level.ALL, new XMLFormatter(), null);
    this.port = port;
    this.host = host;
    connect();
  }

  private void connect() throws IOException {
    if (port == 0) {
      throw new IllegalArgumentException("Bad port: " + port);
    }
    if (host == null) {
      throw new IllegalArgumentException("Null host name: " + host);
    }
    sock = new Socket(host, port);
    OutputStream out = sock.getOutputStream();
    BufferedOutputStream bout = new BufferedOutputStream(out);
    setOutputStream(bout);
  }

  @Override
  public synchronized void close() throws SecurityException {
    super.close();
    if (sock != null) {
      try {
        sock.close();
      } catch (IOException ix) {
      }
    }
    sock = null;
  }

  @Override
  public synchronized void publish(LogRecord record) {
    if (!isLoggable(record)) {
      return;
    }
    super.publish(record);
    flush();
  }
}
