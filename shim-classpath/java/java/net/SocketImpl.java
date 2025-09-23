/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public abstract class SocketImpl implements SocketOptions {
  protected InetAddress address;
  protected FileDescriptor fd;
  protected int localport;
  protected int port;

  public SocketImpl() { }

  protected abstract void create(boolean stream) throws IOException;

  protected abstract void connect(String host, int port) throws IOException;

  protected abstract void connect(InetAddress address, int port) throws IOException;

  protected abstract void connect(SocketAddress address, int timeout) throws IOException;

  protected abstract void bind(InetAddress host, int port) throws IOException;

  protected abstract void listen(int backlog) throws IOException;

  protected abstract void accept(SocketImpl s) throws IOException;

  protected abstract InputStream getInputStream() throws IOException;

  protected abstract OutputStream getOutputStream() throws IOException;

  protected abstract int available() throws IOException;

  protected abstract void close() throws IOException;

  protected void shutdownInput() throws IOException {
    throw new IOException("Method not implemented!");
  }

  protected void shutdownOutput() throws IOException {
    throw new IOException("Method not implemented!");
  }

  protected FileDescriptor getFileDescriptor() {
    return fd;
  }

  protected InetAddress getInetAddress() {
    return address;
  }

  protected int getPort() {
    return port;
  }

  protected boolean supportsUrgentData() {
    return false;
  }

  protected abstract void sendUrgentData(int data) throws IOException;

  protected int getLocalPort() {
    return localport;
  }

  protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
    throw new UnsupportedOperationException("'" + name + "' not supported");
  }

  protected <T> T getOption(SocketOption<T> name) throws IOException {
    throw new UnsupportedOperationException("'" + name + "' not supported");
  }

  protected Set<SocketOption<?>> supportedOptions() {
    return java.util.Collections.<SocketOption<?>>emptySet();
  }

  protected void setPerformancePreferences(int connectionTime, int latency, int bandwidth) { }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[addr=" + getInetAddress() + ",port=" + getPort() + ",localport="
        + getLocalPort() + "]";
  }
}
