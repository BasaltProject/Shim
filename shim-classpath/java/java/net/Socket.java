/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;

public class Socket implements Closeable {
  private final NetHandle net = new NetHandle(false);
  private boolean bound;
  private boolean connected;
  private boolean shutIn;
  private boolean shutOut;
  private InetAddress remoteAddress;
  private int remotePort;
  private InetAddress localAddress;
  private int localPort = -1;
  private int timeout;
  private InputStream in;
  private OutputStream out;
  private boolean oldImpl;

  static void init() throws IOException {
    NetNative.init();
  }

  public Socket() { }

  public Socket(Proxy proxy) {
    if (proxy == null) {
      throw new IllegalArgumentException("Invalid Proxy");
    }
    if (proxy.type() != Proxy.Type.DIRECT && proxy.type() != Proxy.Type.SOCKS && proxy.type() != Proxy.Type.HTTP) {
      throw new IllegalArgumentException("Invalid Proxy");
    }
    if (proxy.type() != Proxy.Type.DIRECT && !(proxy.address() instanceof InetSocketAddress)) {
      throw new IllegalArgumentException("Invalid Proxy");
    }
  }

  protected Socket(SocketImpl impl) throws SocketException {
    oldImpl = impl != null;
  }

  @Deprecated
  public Socket(String host, int port, boolean stream) throws IOException {
    this(host, port);
    if (!stream) {
      throw new IOException("Datagram sockets are not supported");
    }
  }

  @Deprecated
  public Socket(InetAddress address, int port, boolean stream) throws IOException {
    this(address, port);
    if (!stream) {
      throw new IOException("Datagram sockets are not supported");
    }
  }

  public Socket(String host, int port) throws UnknownHostException, IOException {
    this(host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(InetAddress.getByName(null), port),
        (SocketAddress) null);
  }

  public Socket(InetAddress address, int port) throws IOException {
    this(address != null ? new InetSocketAddress(address, port) : null, (SocketAddress) null);
  }

  public Socket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
    this(host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(InetAddress.getByName(null), port),
        new InetSocketAddress(localAddr, localPort));
  }

  public Socket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
    this(address != null ? new InetSocketAddress(address, port) : null, new InetSocketAddress(localAddr, localPort));
  }

  private Socket(SocketAddress address, SocketAddress localAddr) throws IOException {
    if (address == null) {
      throw new NullPointerException();
    }
    try {
      if (localAddr != null) {
        bind(localAddr);
      }
      connect(address);
    } catch (IOException | IllegalArgumentException | SecurityException e) {
      try {
        close();
      } catch (IOException ce) {
        e.addSuppressed(ce);
      }
      throw e;
    }
  }

  Socket(long handle, byte[] peer, int peerPort) throws IOException {
    net.adopt(handle);
    remoteAddress = toAddress(peer);
    remotePort = peerPort;
    bound = true;
    connected = true;
    refreshLocal();
  }

  static InetAddress toAddress(byte[] raw) {
    if (raw == null) {
      return null;
    }
    boolean zero = true;
    for (byte b : raw) {
      zero &= b == 0;
    }
    if (zero) {
      return InetAddress.anyLocalAddress();
    }
    try {
      return InetAddress.getByAddress(raw);
    } catch (UnknownHostException e) {
      return null;
    }
  }

  private void refreshLocal() {
    try {
      long h = net.begin();
      try {
        int[] port = new int[1];
        byte[] raw = NetNative.sockName(h, false, port);
        if (raw != null) {
          localAddress = toAddress(raw);
          localPort = port[0];
        }
      } finally {
        net.end();
      }
    } catch (IOException ignore) { }
  }

  public void connect(SocketAddress endpoint) throws IOException {
    connect(endpoint, 0);
  }

  public void connect(SocketAddress endpoint, int timeout) throws IOException {
    if (endpoint == null) {
      throw new IllegalArgumentException("connect: The address can't be null");
    }
    if (timeout < 0) {
      throw new IllegalArgumentException("connect: timeout can't be negative");
    }
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (isConnected()) {
      throw new SocketException("Already connected");
    }
    if (!(endpoint instanceof InetSocketAddress)) {
      throw new IllegalArgumentException("Unsupported address type");
    }
    InetSocketAddress epoint = (InetSocketAddress) endpoint;
    if (epoint.isUnresolved()) {
      throw new UnknownHostException(epoint.getHostName());
    }
    InetAddress addr = epoint.getAddress();
    if (addr.isAnyLocalAddress()) {
      addr = addr instanceof Inet6Address ? InetAddress.getByAddress(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 1})
          : InetAddress.getLoopbackAddress();
    }
    long h = net.begin();
    try {
      NetNative.connect(h, addr.getAddress(), epoint.getPort(), timeout);
    } finally {
      net.end();
    }
    synchronized (this) {
      connected = true;
      bound = true;
      remoteAddress = epoint.getAddress().isAnyLocalAddress() ? addr : epoint.getAddress();
      remotePort = epoint.getPort();
    }
    refreshLocal();
  }

  public void bind(SocketAddress bindpoint) throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (isBound()) {
      throw new SocketException("Already bound");
    }
    if (bindpoint != null && !(bindpoint instanceof InetSocketAddress)) {
      throw new IllegalArgumentException("Unsupported address type");
    }
    InetSocketAddress epoint = (InetSocketAddress) bindpoint;
    if (epoint != null && epoint.isUnresolved()) {
      throw new SocketException("Unresolved address");
    }
    byte[] raw = epoint == null ? null : epoint.getAddress().getAddress();
    int port = epoint == null ? 0 : epoint.getPort();
    long h = net.begin();
    try {
      NetNative.bind(h, raw, port);
    } finally {
      net.end();
    }
    bound = true;
    refreshLocal();
  }

  public InetAddress getInetAddress() {
    return isConnected() ? remoteAddress : null;
  }

  public InetAddress getLocalAddress() {
    if (isClosed()) {
      return InetAddress.anyLocalAddress();
    }
    if (!isBound()) {
      return InetAddress.anyLocalAddress();
    }
    return localAddress != null ? localAddress : InetAddress.anyLocalAddress();
  }

  public int getPort() {
    return isConnected() ? remotePort : 0;
  }

  public int getLocalPort() {
    if (!isBound()) {
      return -1;
    }
    return localPort;
  }

  public SocketAddress getRemoteSocketAddress() {
    if (!isConnected()) {
      return null;
    }
    return new InetSocketAddress(getInetAddress(), getPort());
  }

  public SocketAddress getLocalSocketAddress() {
    if (!isBound()) {
      return null;
    }
    return new InetSocketAddress(getLocalAddress(), getLocalPort());
  }

  public SocketChannel getChannel() {
    return null;
  }

  private final class SocketInput extends InputStream {
    private final byte[] one = new byte[1];

    @Override
    public int read() throws IOException {
      int n = read(one, 0, 1);
      return n < 0 ? -1 : (one[0] & 0xff);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      java.util.Objects.checkFromIndexSize(off, len, b.length);
      if (isClosed()) {
        throw new SocketException("Socket closed");
      }
      if (shutIn) {
        return -1;
      }
      if (len == 0) {
        return 0;
      }
      long h = net.begin();
      try {
        int n = NetNative.read(h, b, off, len, timeout);
        if (isClosed()) {
          throw new SocketException("Socket closed");
        }
        return n;
      } catch (SocketException e) {
        if (isClosed()) {
          throw new SocketException("Socket closed");
        }
        throw e;
      } finally {
        net.end();
      }
    }

    @Override
    public int available() throws IOException {
      if (isClosed()) {
        throw new SocketException("Socket closed");
      }
      if (shutIn) {
        return 0;
      }
      long h = net.begin();
      try {
        return NetNative.available(h);
      } finally {
        net.end();
      }
    }

    @Override
    public void close() throws IOException {
      Socket.this.close();
    }
  }

  private final class SocketOutput extends OutputStream {
    @Override
    public void write(int b) throws IOException {
      write(new byte[] {(byte) b}, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      java.util.Objects.checkFromIndexSize(off, len, b.length);
      if (isClosed()) {
        throw new SocketException("Socket closed");
      }
      if (shutOut) {
        throw new SocketException("Socket output is shutdown");
      }
      if (len == 0) {
        return;
      }
      long h = net.begin();
      try {
        NetNative.write(h, b, off, len);
      } finally {
        net.end();
      }
    }

    @Override
    public void close() throws IOException {
      Socket.this.close();
    }
  }

  public InputStream getInputStream() throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (!isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    if (isInputShutdown()) {
      throw new SocketException("Socket input is shutdown");
    }
    synchronized (this) {
      if (in == null) {
        in = new SocketInput();
      }
      return in;
    }
  }

  public OutputStream getOutputStream() throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (!isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    if (isOutputShutdown()) {
      throw new SocketException("Socket output is shutdown");
    }
    synchronized (this) {
      if (out == null) {
        out = new SocketOutput();
      }
      return out;
    }
  }

  private long handleForOption() throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    try {
      return net.begin();
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    }
  }

  private void setIntOption(int option, int value) throws SocketException {
    long h = handleForOption();
    try {
      NetNative.setOption(h, option, value);
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    } finally {
      net.end();
    }
  }

  private int getIntOption(int option) throws SocketException {
    long h = handleForOption();
    try {
      return NetNative.getOption(h, option);
    } catch (SocketException e) {
      throw e;
    } catch (IOException e) {
      throw new SocketException(e.getMessage());
    } finally {
      net.end();
    }
  }

  public void setTcpNoDelay(boolean on) throws SocketException {
    setIntOption(NetNative.OPT_TCP_NODELAY, on ? 1 : 0);
  }

  public boolean getTcpNoDelay() throws SocketException {
    return getIntOption(NetNative.OPT_TCP_NODELAY) != 0;
  }

  public void setSoLinger(boolean on, int linger) throws SocketException {
    if (!on) {
      setIntOption(NetNative.OPT_LINGER, -1);
    } else {
      if (linger < 0) {
        throw new IllegalArgumentException("invalid value for SO_LINGER");
      }
      if (linger > 65535) {
        linger = 65535;
      }
      setIntOption(NetNative.OPT_LINGER, linger);
    }
  }

  public int getSoLinger() throws SocketException {
    return getIntOption(NetNative.OPT_LINGER);
  }

  public void sendUrgentData(int data) throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (!isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    long h = net.begin();
    try {
      NetNative.sendUrgent(h, data);
    } finally {
      net.end();
    }
  }

  public void setOOBInline(boolean on) throws SocketException {
    setIntOption(NetNative.OPT_OOBINLINE, on ? 1 : 0);
  }

  public boolean getOOBInline() throws SocketException {
    return getIntOption(NetNative.OPT_OOBINLINE) != 0;
  }

  public synchronized void setSoTimeout(int timeout) throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (timeout < 0) {
      throw new IllegalArgumentException("timeout can't be negative");
    }
    this.timeout = timeout;
  }

  public synchronized int getSoTimeout() throws SocketException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    return timeout;
  }

  public synchronized void setSendBufferSize(int size) throws SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("negative send size");
    }
    setIntOption(NetNative.OPT_SNDBUF, size);
  }

  public synchronized int getSendBufferSize() throws SocketException {
    return getIntOption(NetNative.OPT_SNDBUF);
  }

  public synchronized void setReceiveBufferSize(int size) throws SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("invalid receive size");
    }
    setIntOption(NetNative.OPT_RCVBUF, size);
  }

  public synchronized int getReceiveBufferSize() throws SocketException {
    return getIntOption(NetNative.OPT_RCVBUF);
  }

  public void setKeepAlive(boolean on) throws SocketException {
    setIntOption(NetNative.OPT_KEEPALIVE, on ? 1 : 0);
  }

  public boolean getKeepAlive() throws SocketException {
    return getIntOption(NetNative.OPT_KEEPALIVE) != 0;
  }

  public void setTrafficClass(int tc) throws SocketException {
    if (tc < 0 || tc > 255) {
      throw new IllegalArgumentException("tc is not in range 0 -- 255");
    }
    setIntOption(NetNative.OPT_TOS, tc);
  }

  public int getTrafficClass() throws SocketException {
    return getIntOption(NetNative.OPT_TOS);
  }

  public void setReuseAddress(boolean on) throws SocketException {
    setIntOption(NetNative.OPT_REUSEADDR, on ? 1 : 0);
  }

  public boolean getReuseAddress() throws SocketException {
    return getIntOption(NetNative.OPT_REUSEADDR) != 0;
  }

  @Override
  public void close() throws IOException {
    net.close();
  }

  public void shutdownInput() throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (!isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    if (isInputShutdown()) {
      throw new SocketException("Socket input is already shutdown");
    }
    long h = net.begin();
    try {
      NetNative.shutdown(h, 0);
    } finally {
      net.end();
    }
    shutIn = true;
  }

  public void shutdownOutput() throws IOException {
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (!isConnected()) {
      throw new SocketException("Socket is not connected");
    }
    if (isOutputShutdown()) {
      throw new SocketException("Socket output is already shutdown");
    }
    long h = net.begin();
    try {
      NetNative.shutdown(h, 1);
    } finally {
      net.end();
    }
    shutOut = true;
  }

  @Override
  public String toString() {
    if (isConnected()) {
      return "Socket[addr=" + remoteAddress + ",port=" + remotePort + ",localport=" + getLocalPort() + "]";
    }
    return "Socket[unconnected]";
  }

  public boolean isConnected() {
    return connected;
  }

  public boolean isBound() {
    return bound;
  }

  public boolean isClosed() {
    return net.isClosed();
  }

  public boolean isInputShutdown() {
    return shutIn;
  }

  public boolean isOutputShutdown() {
    return shutOut;
  }

  public static synchronized void setSocketImplFactory(SocketImplFactory fac) throws IOException {
    throw new SocketException("factory already defined");
  }

  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) { }

  public <T> Socket setOption(SocketOption<T> name, T value) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (name == StandardSocketOptions.SO_KEEPALIVE) {
      setKeepAlive((Boolean) value);
    } else if (name == StandardSocketOptions.SO_SNDBUF) {
      setSendBufferSize((Integer) value);
    } else if (name == StandardSocketOptions.SO_RCVBUF) {
      setReceiveBufferSize((Integer) value);
    } else if (name == StandardSocketOptions.SO_REUSEADDR) {
      setReuseAddress((Boolean) value);
    } else if (name == StandardSocketOptions.SO_REUSEPORT) {
      setIntOption(NetNative.OPT_REUSEPORT, ((Boolean) value) ? 1 : 0);
    } else if (name == StandardSocketOptions.SO_LINGER) {
      int v = (Integer) value;
      setSoLinger(v >= 0, Math.max(0, v));
    } else if (name == StandardSocketOptions.IP_TOS) {
      setTrafficClass((Integer) value);
    } else if (name == StandardSocketOptions.TCP_NODELAY) {
      setTcpNoDelay((Boolean) value);
    } else {
      throw new UnsupportedOperationException("'" + name + "' not supported");
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getOption(SocketOption<T> name) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (isClosed()) {
      throw new SocketException("Socket is closed");
    }
    if (name == StandardSocketOptions.SO_KEEPALIVE) {
      return (T) Boolean.valueOf(getKeepAlive());
    } else if (name == StandardSocketOptions.SO_SNDBUF) {
      return (T) Integer.valueOf(getSendBufferSize());
    } else if (name == StandardSocketOptions.SO_RCVBUF) {
      return (T) Integer.valueOf(getReceiveBufferSize());
    } else if (name == StandardSocketOptions.SO_REUSEADDR) {
      return (T) Boolean.valueOf(getReuseAddress());
    } else if (name == StandardSocketOptions.SO_REUSEPORT) {
      return (T) Boolean.valueOf(getIntOption(NetNative.OPT_REUSEPORT) != 0);
    } else if (name == StandardSocketOptions.SO_LINGER) {
      return (T) Integer.valueOf(getSoLinger());
    } else if (name == StandardSocketOptions.IP_TOS) {
      return (T) Integer.valueOf(getTrafficClass());
    } else if (name == StandardSocketOptions.TCP_NODELAY) {
      return (T) Boolean.valueOf(getTcpNoDelay());
    }
    throw new UnsupportedOperationException("'" + name + "' not supported");
  }

  public Set<SocketOption<?>> supportedOptions() {
    Set<SocketOption<?>> options = new HashSet<SocketOption<?>>();
    options.add(StandardSocketOptions.SO_KEEPALIVE);
    options.add(StandardSocketOptions.SO_SNDBUF);
    options.add(StandardSocketOptions.SO_RCVBUF);
    options.add(StandardSocketOptions.SO_REUSEADDR);
    options.add(StandardSocketOptions.SO_REUSEPORT);
    options.add(StandardSocketOptions.SO_LINGER);
    options.add(StandardSocketOptions.IP_TOS);
    options.add(StandardSocketOptions.TCP_NODELAY);
    return Collections.unmodifiableSet(options);
  }
}
