/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

public class WebSocketClientModern {
  static final BlockingQueue<String> events = new LinkedBlockingQueue<>();

  static String hex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (byte x : b) {
      sb.append(String.format("%02x", x));
    }
    return sb.toString();
  }

  static void writeFrame(OutputStream out, int opcode, boolean fin, byte[] data) throws IOException {
    ByteArrayOutputStream f = new ByteArrayOutputStream();
    f.write((fin ? 0x80 : 0) | opcode);
    if (data.length < 126) {
      f.write(data.length);
    } else {
      f.write(126);
      f.write(data.length >> 8);
      f.write(data.length);
    }
    f.write(data);
    out.write(f.toByteArray());
    out.flush();
  }

  static void serve(ServerSocket server) {
    try (Socket s = server.accept()) {
      InputStream in = s.getInputStream();
      OutputStream out = s.getOutputStream();
      BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
      String line = r.readLine();
      events.add("request-line " + line);
      String key = null;
      String proto = null;
      Map<String, String> seen = new TreeMap<>();
      while ((line = r.readLine()) != null && !line.isEmpty()) {
        int c = line.indexOf(':');
        String name = line.substring(0, c).toLowerCase();
        String value = line.substring(c + 1).trim();
        if (name.equals("sec-websocket-key")) {
          key = value;
        } else if (name.equals("sec-websocket-protocol")) {
          proto = value;
        } else if (name.equals("x-test") || name.equals("upgrade") || name.equals("sec-websocket-version")) {
          seen.put(name, value);
        }
      }
      events.add("headers " + seen + " protocols=" + proto);
      String accept = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1")
          .digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.ISO_8859_1)));
      out.write(("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: "
          + accept + "\r\n" + (proto != null ? "Sec-WebSocket-Protocol: " + proto.split(",")[0].trim() + "\r\n" : "")
          + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
      out.flush();
      writeFrame(out, 1, true, "hello".getBytes(StandardCharsets.UTF_8));
      writeFrame(out, 1, false, "frag-".getBytes(StandardCharsets.UTF_8));
      writeFrame(out, 0, true, "ment".getBytes(StandardCharsets.UTF_8));
      writeFrame(out, 2, true, new byte[] {1, 2, 3});
      writeFrame(out, 9, true, "p".getBytes(StandardCharsets.UTF_8));
      DataInputStream din = new DataInputStream(in);
      boolean closing = false;
      while (true) {
        int b0 = din.read();
        if (b0 < 0) {
          break;
        }
        int opcode = b0 & 0x0f;
        int b1 = din.read();
        int len = b1 & 0x7f;
        if (len == 126) {
          len = (din.read() << 8) | din.read();
        }
        byte[] mask = new byte[4];
        din.readFully(mask);
        byte[] payload = new byte[len];
        din.readFully(payload);
        for (int i = 0; i < len; ++i) {
          payload[i] ^= mask[i & 3];
        }
        events.add("server got op=" + opcode + " fin=" + ((b0 & 0x80) != 0) + " " + (opcode == 8 ? hex(payload) : opcode == 2 ? hex(payload) : new String(payload, StandardCharsets.UTF_8)));
        if (opcode == 1 && new String(payload, StandardCharsets.UTF_8).equals("echo-me")) {
          writeFrame(out, 1, true, "echo-me".getBytes(StandardCharsets.UTF_8));
        } else if (opcode == 1 && new String(payload, StandardCharsets.UTF_8).equals("bye")) {
          byte[] cr = "going away".getBytes(StandardCharsets.UTF_8);
          byte[] body = new byte[cr.length + 2];
          body[0] = 0x03;
          body[1] = (byte) 0xe9;
          System.arraycopy(cr, 0, body, 2, cr.length);
          writeFrame(out, 8, true, body);
        } else if (opcode == 8) {
          closing = true;
          break;
        }
      }
    } catch (Exception e) {
      events.add("server error " + e);
    }
  }

  static void drain(String label) throws Exception {
    Thread.sleep(300);
    List<String> list = new ArrayList<>();
    events.drainTo(list);
    Collections.sort(list);
    for (String e : list) {
      System.out.println(label + " " + e);
    }
  }

  public static void main(String[] args) throws Exception {
    ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
    int port = server.getLocalPort();
    Thread st = new Thread(() -> serve(server));
    st.start();
    HttpClient client = HttpClient.newHttpClient();
    final CompletableFuture<Void> closed = new CompletableFuture<>();
    WebSocket.Listener listener = new WebSocket.Listener() {
      public void onOpen(WebSocket ws) {
        events.add("client open sub=" + ws.getSubprotocol());
        ws.request(1);
      }

      public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        events.add("client text " + data + " last=" + last);
        ws.request(1);
        return null;
      }

      public CompletionStage<?> onBinary(WebSocket ws, ByteBuffer data, boolean last) {
        byte[] b = new byte[data.remaining()];
        data.get(b);
        events.add("client binary " + hex(b) + " last=" + last);
        ws.request(1);
        return null;
      }

      public CompletionStage<?> onPing(WebSocket ws, ByteBuffer m) {
        byte[] b = new byte[m.remaining()];
        m.get(b);
        events.add("client ping " + hex(b));
        ws.request(1);
        return null;
      }

      public CompletionStage<?> onClose(WebSocket ws, int code, String reason) {
        events.add("client close " + code + " " + reason);
        closed.complete(null);
        return null;
      }

      public void onError(WebSocket ws, Throwable t) {
        events.add("client error " + t.getClass().getSimpleName());
        closed.complete(null);
      }
    };
    WebSocket ws = client.newWebSocketBuilder().header("X-Test", "yes").subprotocols("chat", "other")
        .buildAsync(URI.create("ws://127.0.0.1:" + port + "/path?q=1"), listener).get(10, TimeUnit.SECONDS);
    Thread.sleep(400);
    drain("A");
    ws.request(10);
    drain("B");
    ws.sendText("echo-me", true).get();
    ws.sendText("part1-", false).get();
    ws.sendText("part2", true).get();
    ws.sendBinary(ByteBuffer.wrap(new byte[] {9, 8, 7}), true).get();
    ws.sendPing(ByteBuffer.wrap(new byte[] {5})).get();
    drain("C");
    ws.request(10);
    ws.sendText("bye", true).get();
    closed.get(10, TimeUnit.SECONDS);
    drain("D");
    System.out.println("outputClosed=" + ws.isOutputClosed() + " inputClosed=" + ws.isInputClosed());
    String[] headerNames = {"Upgrade", "Connection", "Sec-WebSocket-Key", "Sec-WebSocket-Protocol", "Host",
        "Content-Length", "Expect", "Transfer-Encoding", "X-Ok", "bad name", ""};
    for (String h : headerNames) {
      try {
        client.newWebSocketBuilder().header(h, "v");
        System.out.println("header " + h + " ok");
      } catch (Exception e) {
        System.out.println("header " + h + " " + e.getClass().getSimpleName());
      }
    }
    for (String[] h : new String[][] {{"Upgrade", "v"}, {"bad name", "v"}, {"X-Ok", "line\nbreak"}}) {
      try {
        client.newWebSocketBuilder().header(h[0], h[1]).buildAsync(URI.create("ws://127.0.0.1:1/"), listener).get(10, TimeUnit.SECONDS);
        System.out.println("connect header " + h[0] + " connected");
      } catch (ExecutionException e) {
        System.out.println("connect header " + h[0] + " " + e.getCause().getClass().getSimpleName());
      } catch (Exception e) {
        System.out.println("connect header " + h[0] + " sync " + e.getClass().getSimpleName());
      }
    }
    for (String[] sp : new String[][] {{"a b"}, {""}, {"ok", "ok"}, {"a,b"}}) {
      try {
        client.newWebSocketBuilder().subprotocols(sp[0], Arrays.copyOfRange(sp, 1, sp.length)).buildAsync(URI.create("ws://127.0.0.1:1/"), listener).get(10, TimeUnit.SECONDS);
        System.out.println("subprotocols " + Arrays.toString(sp) + " connected");
      } catch (ExecutionException e) {
        System.out.println("subprotocols " + Arrays.toString(sp) + " " + e.getCause().getClass().getSimpleName());
      } catch (Exception e) {
        System.out.println("subprotocols " + Arrays.toString(sp) + " sync " + e.getClass().getSimpleName());
      }
    }
    String[] uris = {"http://127.0.0.1:1/", "ws://127.0.0.1:1/#frag", "ws:///nohost", "foo:bar", "wss://127.0.0.1:1/"};
    for (String u : uris) {
      try {
        CompletableFuture<WebSocket> f = client.newWebSocketBuilder().buildAsync(URI.create(u), listener);
        try {
          f.get(10, TimeUnit.SECONDS);
          System.out.println("uri " + u + " connected");
        } catch (ExecutionException e) {
          System.out.println("uri " + u + " future " + e.getCause().getClass().getSimpleName());
        }
      } catch (Exception e) {
        System.out.println("uri " + u + " sync " + e.getClass().getSimpleName());
      }
    }
    int[] codes = {1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1011, 1012, 999, 2999, 3000, 4999, 5000, 0, -1};
    StringBuilder sb = new StringBuilder();
    for (int code : codes) {
      try {
        String r = ws.sendClose(code, "r").handle((v, t) -> t == null ? "ok" : t.getClass().getSimpleName()).get();
        sb.append(code).append('=').append(r).append(' ');
      } catch (Exception e) {
        sb.append(code).append('=').append(e.getClass().getSimpleName()).append(' ');
      }
    }
    System.out.println(sb);
    for (long n : new long[] {0, -1}) {
      try {
        ws.request(n);
        System.out.println("request " + n + " ok");
      } catch (Exception e) {
        System.out.println("request " + n + " " + e.getClass().getSimpleName());
      }
    }
    try {
      ws.sendPing(ByteBuffer.allocate(126)).get();
      System.out.println("ping126 ok");
    } catch (Exception e) {
      System.out.println("ping126 " + e.getClass().getSimpleName() + " " + (e.getCause() == null ? "" : e.getCause().getClass().getSimpleName()));
    }
    try {
      ws.sendText("x", true).get();
      System.out.println("send after close ok");
    } catch (Exception e) {
      System.out.println("send after close " + (e.getCause() == null ? e.getClass() : e.getCause().getClass().getSimpleName()));
    }
    try {
      client.newWebSocketBuilder().subprotocols("a b");
    } catch (Exception e) {
      System.out.println("subprotocol " + e.getClass().getSimpleName());
    }
    try {
      client.newWebSocketBuilder().connectTimeout(java.time.Duration.ZERO);
    } catch (Exception e) {
      System.out.println("timeout " + e.getClass().getSimpleName());
    }
    System.exit(0);
  }
}
