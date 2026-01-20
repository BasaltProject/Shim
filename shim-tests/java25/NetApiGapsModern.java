/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.time.Duration;
import java.util.concurrent.*;

public class NetApiGapsModern {
  static void t(String n, Callable<Object> c) {
    try {
      System.out.println(n + " = " + c.call());
    } catch (Throwable e) {
      System.out.println(n + " ! " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  @SuppressWarnings("deprecation")
  public static void main(String[] a) throws Exception {
    t("default-prop", () -> {
      URLConnection.setDefaultRequestProperty("X-A", "1");
      return URLConnection.getDefaultRequestProperty("X-A");
    });
    t("serial-perm", () -> ObjectStreamConstants.SERIAL_FILTER_PERMISSION.getName());
    t("proto-name", () -> StandardProtocolFamily.UNIX.name() + StandardProtocolFamily.INET6.name());
    t("mc-closed", () -> {
      MulticastSocket m = new MulticastSocket();
      m.close();
      try {
        m.getInterface();
        return "no";
      } catch (SocketException e) {
        return e.getMessage();
      }
    });
    t("mc-null-if", () -> {
      try (MulticastSocket m = new MulticastSocket()) {
        m.setInterface(null);
        return "no";
      } catch (SocketException e) {
        return e.getMessage();
      }
    });
    t("mc-ttl-send", () -> {
      try (MulticastSocket m = new MulticastSocket()) {
        m.setTimeToLive(7);
        m.send(new DatagramPacket(new byte[1], 1, InetAddress.getLoopbackAddress(), 9), (byte) 3);
        return m.getTimeToLive();
      }
    });
    t("async-streams", () -> {
      try (AsynchronousServerSocketChannel s = AsynchronousServerSocketChannel.open()
          .bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0))) {
        AsynchronousSocketChannel c = AsynchronousSocketChannel.open();
        c.connect(s.getLocalAddress()).get();
        AsynchronousSocketChannel p = s.accept().get();
        OutputStream out = Channels.newOutputStream(c);
        out.write("hello".getBytes());
        InputStream in = Channels.newInputStream(p);
        byte[] b = new byte[5];
        int n = 0;
        while (n < 5) {
          n += in.read(b, n, 5 - n);
        }
        c.close();
        int eof = in.read();
        p.close();
        return new String(b) + " " + eof;
      }
    });
  }
}
