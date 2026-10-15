/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.InvalidMarkException;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class BuffersAndChannelsModern {
  public static void main(String[] args) throws Exception {
    testBufferMarkAndReset();
    testByteBufferModernOps();
    testCharBufferModernOps();
    testCharsetDecodersAndEncoders();
    testChannelsReadersAndWriters();
    testHttpUrlAndProxy();
    System.out.println("BuffersAndChannelsModern OK");
  }

  private static void testBufferMarkAndReset() {
    ByteBuffer bb = ByteBuffer.allocate(16);
    bb.put((byte) 10).put((byte) 20).put((byte) 30);
    bb.mark();
    bb.put((byte) 40).put((byte) 50);
    System.out.println("pos before reset=" + bb.position());
    bb.reset();
    System.out.println("pos after reset=" + bb.position());
    System.out.println("val after reset=" + bb.get());

    boolean markDiscarded = false;
    bb.position(1);
    try {
      bb.reset();
    } catch (InvalidMarkException ex) {
      markDiscarded = true;
    }
    System.out.println("mark discarded on position decrease=" + markDiscarded);
  }

  private static void testByteBufferModernOps() {
    ByteBuffer b1 = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 5 });
    ByteBuffer b2 = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 5 });
    ByteBuffer b3 = ByteBuffer.wrap(new byte[] { 1, 2, 9, 4, 5 });
    ByteBuffer b4 = ByteBuffer.wrap(new byte[] { 1, 2, 3 });

    System.out.println("mismatch equal=" + b1.mismatch(b2));
    System.out.println("mismatch diff=" + b1.mismatch(b3));
    System.out.println("mismatch prefix=" + b1.mismatch(b4));

    ByteBuffer bb = ByteBuffer.allocate(16);
    byte[] src = new byte[] { 11, 22, 33, 44 };
    bb.put(4, src, 1, 3);
    System.out.println("absolute bulk put/get=" + bb.get(4) + "," + bb.get(5) + "," + bb.get(6));

    byte[] dst = new byte[3];
    bb.get(4, dst);
    System.out.println("bulk get dst=" + dst[0] + "," + dst[1] + "," + dst[2]);

    ByteBuffer slice = bb.slice(4, 3);
    System.out.println("slice capacity=" + slice.capacity() + " limit=" + slice.limit() + " pos=" + slice.position());
    System.out.println("slice content=" + slice.get(0) + "," + slice.get(1) + "," + slice.get(2));

    bb.putChar(8, 'Z');
    System.out.println("putChar/getChar=" + bb.getChar(8));

    ByteBuffer ro = bb.asReadOnlyBuffer();
    System.out.println("ro isReadOnly=" + ro.isReadOnly());
    ByteBuffer roSlice = ro.slice(4, 2);
    System.out.println("roSlice isReadOnly=" + roSlice.isReadOnly());
  }

  private static void testCharBufferModernOps() {
    CharBuffer cb = CharBuffer.allocate(32);
    cb.append("Hello");
    cb.append(' ');
    cb.append("World, from Shim!", 0, 5);
    cb.flip();

    System.out.println("charBuffer length=" + cb.length());
    System.out.println("charAt(0)=" + cb.charAt(0));
    System.out.println("charAt(6)=" + cb.charAt(6));
    System.out.println("subSequence=" + cb.subSequence(0, 5));
    System.out.println("toString=" + cb.toString());

    CharBuffer c1 = CharBuffer.wrap("AlphaBeta");
    CharBuffer c2 = CharBuffer.wrap("AlphaGamma");
    System.out.println("charBuffer mismatch=" + c1.mismatch(c2));
  }

  private static void testCharsetDecodersAndEncoders() throws Exception {
    Charset utf8 = StandardCharsets.UTF_8;
    CharsetEncoder enc = utf8.newEncoder();
    CharsetDecoder dec = utf8.newDecoder();

    System.out.println("utf8 canEncode ASCII=" + enc.canEncode("Basalt Project"));
    System.out.println("utf8 canEncode BMP=" + enc.canEncode("\u00e9\u00e8\u00e0"));

    ByteBuffer encoded = enc.encode(CharBuffer.wrap("Hello Shim JVM"));
    CharBuffer decoded = dec.decode(encoded);
    System.out.println("decoded string=" + decoded.toString());

    Charset iso = Charset.forName("ISO-8859-1");
    CharsetEncoder isoEnc = iso.newEncoder();
    System.out.println("iso canEncode euro=" + isoEnc.canEncode('\u20ac'));
    System.out.println("iso canEncode latin=" + isoEnc.canEncode('\u00f1'));

    dec.onMalformedInput(CodingErrorAction.REPLACE);
    dec.replaceWith("?");
    ByteBuffer malformed = ByteBuffer.wrap(new byte[] { (byte) 0xFF, (byte) 0x41 });
    CharBuffer replaced = dec.decode(malformed);
    System.out.println("replaced malformed=" + replaced.toString());
  }

  private static void testChannelsReadersAndWriters() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    WritableByteChannel wbc = Channels.newChannel(baos);
    Writer writer = Channels.newWriter(wbc, StandardCharsets.UTF_8);
    writer.write("Channel writer test output: 12345");
    writer.flush();

    byte[] bytes = baos.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    ReadableByteChannel rbc = Channels.newChannel(bais);
    Reader reader = Channels.newReader(rbc, StandardCharsets.UTF_8);
    char[] buf = new char[64];
    int read = reader.read(buf);
    System.out.println("read through channel reader=" + new String(buf, 0, read));
  }

  private static void testHttpUrlAndProxy() throws Exception {
    URL url = new URL("https://basaltmc.org:8443/api/v1/status?verbose=true");
    System.out.println("url protocol=" + url.getProtocol());
    System.out.println("url host=" + url.getHost());
    System.out.println("url port=" + url.getPort());
    System.out.println("url default port=" + url.getDefaultPort());
    System.out.println("url authority=" + url.getAuthority());
    System.out.println("url path=" + url.getPath());
    System.out.println("url query=" + url.getQuery());
    System.out.println("url toURI=" + url.toURI().toString());

    System.out.println("HTTP_OK=" + HttpURLConnection.HTTP_OK);
    System.out.println("HTTP_NOT_FOUND=" + HttpURLConnection.HTTP_NOT_FOUND);
    System.out.println("HTTP_INTERNAL_ERROR=" + HttpURLConnection.HTTP_INTERNAL_ERROR);

    Proxy noProxy = Proxy.NO_PROXY;
    System.out.println("noProxy type=" + noProxy.type());
    Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
    System.out.println("httpProxy type=" + httpProxy.type());
    System.out.println("httpProxy equals itself=" + httpProxy.equals(httpProxy));

    URLConnection conn = url.openConnection();
    conn.setConnectTimeout(5000);
    conn.setReadTimeout(10000);
    conn.setRequestProperty("User-Agent", "BasaltShim/1.0");
    conn.addRequestProperty("Accept", "application/json");
    System.out.println("connectTimeout=" + conn.getConnectTimeout());
    System.out.println("readTimeout=" + conn.getReadTimeout());
    System.out.println("requestProperty User-Agent=" + conn.getRequestProperty("User-Agent"));
    System.out.println("guessContentType json=" + URLConnection.guessContentTypeFromName("data.json"));
  }
}
