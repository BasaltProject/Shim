/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;
import java.util.zip.*;

public class DeflateStreamsModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  interface IoBody { Object run() throws Exception; }

  static void io(String name, IoBody body) {
    probe(name, () -> {
      try { return body.run(); } catch (Exception e) { return "!" + e.getClass().getName() + ":" + e.getMessage(); }
    });
  }

  static String hex(byte[] b, int n) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < n; i++) sb.append(String.format("%02x", b[i]));
    return sb.toString();
  }

  static byte[] data() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 300; i++) sb.append("line ").append(i % 17).append(" of the sample text\n");
    return sb.toString().getBytes();
  }

  static byte[] deflateAll(Deflater d, byte[] in) {
    d.setInput(in);
    d.finish();
    byte[] out = new byte[in.length + 64];
    int n = 0;
    while (!d.finished()) n += d.deflate(out, n, out.length - n);
    return Arrays.copyOf(out, n);
  }

  public static void main(String[] args) throws Exception {
    byte[] data = data();
    io("constants", () -> Deflater.DEFLATED + " " + Deflater.NO_COMPRESSION + " " + Deflater.BEST_SPEED + " " + Deflater.BEST_COMPRESSION + " " + Deflater.DEFAULT_COMPRESSION + " " + Deflater.FILTERED + " " + Deflater.HUFFMAN_ONLY + " " + Deflater.DEFAULT_STRATEGY + " " + Deflater.NO_FLUSH + " " + Deflater.SYNC_FLUSH + " " + Deflater.FULL_FLUSH);
    for (int level : new int[] {-1, 0, 1, 6, 9}) {
      io("level-" + level, () -> { Deflater d = new Deflater(level); byte[] c = deflateAll(d, data); String r = c.length + " " + d.getAdler() + " " + d.getBytesRead() + " " + d.getBytesWritten() + " " + d.getTotalIn() + " " + d.getTotalOut(); d.end(); return r; });
    }
    io("strategies", () -> { StringBuilder sb = new StringBuilder(); for (int s : new int[] {Deflater.DEFAULT_STRATEGY, Deflater.FILTERED, Deflater.HUFFMAN_ONLY}) { Deflater d = new Deflater(); d.setStrategy(s); sb.append(deflateAll(d, data).length).append(' '); d.end(); } return sb; });
    io("bad-args", () -> { StringBuilder sb = new StringBuilder(); try { new Deflater(10); } catch (IllegalArgumentException e) { sb.append("lvl "); } try { new Deflater().setStrategy(7); } catch (IllegalArgumentException e) { sb.append("strat "); } try { new Deflater().setLevel(-2); } catch (IllegalArgumentException e) { sb.append("setlvl "); } try { new Deflater().setInput(new byte[2], 1, 5); } catch (IndexOutOfBoundsException e) { sb.append("range "); } return sb; });
    io("nowrap", () -> { Deflater d = new Deflater(6, true); byte[] c = deflateAll(d, data); Inflater i = new Inflater(true); i.setInput(c); byte[] out = new byte[data.length]; int n = i.inflate(out); return c.length + " " + n + " " + Arrays.equals(out, data) + " " + i.finished(); });
    io("roundtrip", () -> { Deflater d = new Deflater(); byte[] c = deflateAll(d, data); Inflater i = new Inflater(); i.setInput(c); byte[] out = new byte[data.length + 10]; int n = i.inflate(out); return n + " " + i.finished() + " " + i.getRemaining() + " " + i.getTotalIn() + " " + i.getTotalOut() + " " + i.getAdler() + " " + i.needsInput(); });
    io("sync-flush", () -> { Deflater d = new Deflater(); d.setInput("hello world".getBytes()); byte[] out = new byte[100]; int n = d.deflate(out, 0, out.length, Deflater.SYNC_FLUSH); return n + " " + hex(out, n) + " " + d.needsInput() + " " + d.finished(); });
    io("full-flush", () -> { Deflater d = new Deflater(); d.setInput("hello world".getBytes()); byte[] out = new byte[100]; int n = d.deflate(out, 0, out.length, Deflater.FULL_FLUSH); return n + " " + hex(out, n); });
    io("bad-flush", () -> { Deflater d = new Deflater(); return d.deflate(new byte[10], 0, 10, 9); });
    io("dictionary", () -> { byte[] dict = "the sample text of line".getBytes(); Deflater d = new Deflater(); d.setDictionary(dict); byte[] c = deflateAll(d, data); Inflater i = new Inflater(); i.setInput(c); byte[] out = new byte[data.length]; int n = i.inflate(out); boolean need = i.needsDictionary(); String r = c.length + " " + n + " " + need + " " + i.getAdler(); i.setDictionary(dict); n = i.inflate(out); return r + " " + n + " " + Arrays.equals(out, data) + " " + i.needsDictionary(); });
    io("dictionary-bytebuffer", () -> { Deflater d = new Deflater(); d.setDictionary(ByteBuffer.wrap("abcabc".getBytes())); return d.getAdler(); });
    io("bytebuffer", () -> { Deflater d = new Deflater(); ByteBuffer in = ByteBuffer.wrap(data); d.setInput(in); d.finish(); ByteBuffer out = ByteBuffer.allocate(data.length + 64); int total = 0; while (!d.finished()) total += d.deflate(out); out.flip(); byte[] c = new byte[out.remaining()]; out.get(c); Inflater i = new Inflater(); ByteBuffer cin = ByteBuffer.wrap(c); i.setInput(cin); ByteBuffer res = ByteBuffer.allocate(data.length); int n = i.inflate(res); return total + " " + in.remaining() + " " + n + " " + cin.remaining() + " " + res.position() + " " + Arrays.equals(Arrays.copyOf(res.array(), n), data); });
    io("small-output", () -> { Deflater d = new Deflater(); d.setInput(data); d.finish(); byte[] out = new byte[7]; int total = 0, calls = 0; ByteArrayOutputStream bo = new ByteArrayOutputStream(); while (!d.finished()) { int n = d.deflate(out); bo.write(out, 0, n); calls++; } Inflater i = new Inflater(); i.setInput(bo.toByteArray()); byte[] res = new byte[data.length]; int off = 0; while (!i.finished()) { int got = i.inflate(res, off, Math.min(5, res.length - off)); if (got == 0 && (i.needsInput() || off == res.length)) break; off += got; } return bo.size() + " " + (calls > 10) + " " + off; });
    io("setlevel-midstream", () -> { Deflater d = new Deflater(0); d.setInput(Arrays.copyOf(data, 600)); byte[] out = new byte[40000]; int n = d.deflate(out, 0, out.length, Deflater.SYNC_FLUSH); d.setLevel(9); d.setInput(Arrays.copyOfRange(data, 600, data.length)); d.finish(); for (int g = 0; !d.finished() && g < 100; g++) n += d.deflate(out, n, out.length - n); Inflater i = new Inflater(); i.setInput(out, 0, n); byte[] res = new byte[data.length]; int m = i.inflate(res); return (m == data.length) + " " + Arrays.equals(res, data); });
    io("reset", () -> { Deflater d = new Deflater(); byte[] a = deflateAll(d, data); d.reset(); byte[] b = deflateAll(d, data); return Arrays.equals(a, b) + " " + d.getBytesRead(); });
    io("closed", () -> { Deflater d = new Deflater(); d.close(); StringBuilder sb = new StringBuilder(); try { d.deflate(new byte[4]); } catch (IllegalStateException e) { sb.append(e.getMessage()).append(' '); } try { d.finished(); } catch (IllegalStateException e) { sb.append(e.getMessage()).append(' '); } try { d.getBytesRead(); } catch (IllegalStateException e) { sb.append("bytes "); } d.end(); Inflater i = new Inflater(); i.close(); try { i.inflate(new byte[4]); } catch (IllegalStateException e) { sb.append(e.getMessage()); } return sb; });
    io("inflate-corrupt", () -> { Inflater i = new Inflater(); i.setInput(new byte[] {1, 2, 3, 4, 5, 6, 7, 8}); try { i.inflate(new byte[10]); return "none"; } catch (DataFormatException e) { return e.getMessage(); } });
    io("inflate-nodata", () -> { Inflater i = new Inflater(); byte[] c = deflateAll(new Deflater(), data); i.setInput(Arrays.copyOf(c, c.length / 2)); byte[] res = new byte[data.length]; int n = i.inflate(res); int m = i.inflate(res); return (n > 0) + " " + m + " " + i.finished() + " " + i.needsInput(); });
    io("deflater-out", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (DeflaterOutputStream o = new DeflaterOutputStream(bo)) { o.write(data); } return bo.size() + " " + hex(bo.toByteArray(), 8); });
    io("deflater-out-sync", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); DeflaterOutputStream o = new DeflaterOutputStream(bo, true); o.write("abc".getBytes()); o.flush(); int mid = bo.size(); o.write("def".getBytes()); o.close(); return mid + " " + bo.size() + " " + hex(bo.toByteArray(), bo.size()); });
    io("deflater-out-nosync", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); DeflaterOutputStream o = new DeflaterOutputStream(bo); o.write("abc".getBytes()); o.flush(); int mid = bo.size(); o.close(); return mid + " " + bo.size(); });
    io("deflater-out-errors", () -> { StringBuilder sb = new StringBuilder(); DeflaterOutputStream o = new DeflaterOutputStream(new ByteArrayOutputStream()); o.finish(); try { o.write(1); } catch (IOException e) { sb.append(e.getMessage()).append(' '); } try { new DeflaterOutputStream(null); } catch (NullPointerException e) { sb.append("npe "); } try { new DeflaterOutputStream(new ByteArrayOutputStream(), new Deflater(), 0); } catch (IllegalArgumentException e) { sb.append(e.getMessage()); } return sb; });
    io("inflater-in", () -> { byte[] c = deflateAll(new Deflater(), data); try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(c))) { byte[] r = in.readAllBytes(); return r.length + " " + Arrays.equals(r, data) + " " + in.available() + " " + in.read(); } });
    io("inflater-in-skip", () -> { byte[] c = deflateAll(new Deflater(), data); try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(c))) { long s = in.skip(100); int b = in.read(); return s + " " + b + " " + in.markSupported() + " " + in.available(); } });
    io("inflater-in-errors", () -> { StringBuilder sb = new StringBuilder(); try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(new byte[] {1, 2, 3, 4}))) { in.read(); } catch (IOException e) { sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(' '); } byte[] c = deflateAll(new Deflater(), data); try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(Arrays.copyOf(c, 20)))) { in.readAllBytes(); } catch (IOException e) { sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(' '); } InflaterInputStream cl = new InflaterInputStream(new ByteArrayInputStream(c)); cl.close(); try { cl.read(); } catch (IOException e) { sb.append(e.getMessage()).append(' '); } try { cl.reset(); } catch (IOException e) { sb.append(e.getMessage()); } try { new InflaterInputStream(new ByteArrayInputStream(c), new Inflater(), 0); } catch (IllegalArgumentException e) { sb.append(' ').append(e.getMessage()); } return sb; });
    io("deflater-in", () -> { try (DeflaterInputStream in = new DeflaterInputStream(new ByteArrayInputStream(data))) { byte[] c = in.readAllBytes(); Inflater i = new Inflater(); i.setInput(c); byte[] r = new byte[data.length]; i.inflate(r); return c.length + " " + Arrays.equals(r, data) + " " + in.available() + " " + hex(c, 6); } });
    io("deflater-in-misc", () -> { StringBuilder sb = new StringBuilder(); DeflaterInputStream in = new DeflaterInputStream(new ByteArrayInputStream(data), new Deflater(1), 16); sb.append(in.skip(10)).append(' ').append(in.read()).append(' ').append(in.markSupported()).append(' ').append(in.available()); in.close(); try { in.read(); } catch (IOException e) { sb.append(' ').append(e.getMessage()); } try { new DeflaterInputStream(new ByteArrayInputStream(data), null); } catch (NullPointerException e) { sb.append(' ').append(e.getMessage()); } try { new DeflaterInputStream(new ByteArrayInputStream(data), new Deflater(), 0); } catch (IllegalArgumentException e) { sb.append(' ').append(e.getMessage()); } return sb; });
    io("inflater-out", () -> { byte[] c = deflateAll(new Deflater(), data); ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (InflaterOutputStream o = new InflaterOutputStream(bo)) { for (int i = 0; i < c.length; i += 7) o.write(c, i, Math.min(7, c.length - i)); } return bo.size() + " " + Arrays.equals(bo.toByteArray(), data); });
    io("inflater-out-errors", () -> { StringBuilder sb = new StringBuilder(); InflaterOutputStream o = new InflaterOutputStream(new ByteArrayOutputStream()); try { o.write(new byte[] {1, 2, 3, 4, 5}); } catch (ZipException e) { sb.append(e.getMessage()).append(' '); } o = new InflaterOutputStream(new ByteArrayOutputStream()); o.close(); try { o.write(1); } catch (IOException e) { sb.append(e.getMessage()).append(' '); } try { new InflaterOutputStream(new ByteArrayOutputStream(), null); } catch (NullPointerException e) { sb.append(e.getMessage()).append(' '); } try { new InflaterOutputStream(null); } catch (NullPointerException e) { sb.append(e.getMessage()); } return sb; });
    io("gzip", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (GZIPOutputStream g = new GZIPOutputStream(bo)) { g.write(data); } try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bo.toByteArray()))) { byte[] r = in.readAllBytes(); return bo.size() + " " + Arrays.equals(r, data) + " " + hex(bo.toByteArray(), 10); } });
    io("zip", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); try (ZipOutputStream z = new ZipOutputStream(bo)) { z.putNextEntry(new ZipEntry("a.txt")); z.write(data); z.closeEntry(); z.putNextEntry(new ZipEntry("b/c.txt")); z.write("tiny".getBytes()); z.closeEntry(); } StringBuilder sb = new StringBuilder(); try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(bo.toByteArray()))) { ZipEntry e; while ((e = in.getNextEntry()) != null) sb.append(e.getName()).append('=').append(in.readAllBytes().length).append(' '); } return sb; });
    io("zip-error", () -> new ZipError("boom") instanceof InternalError ? new ZipError("boom").getMessage() : "no");
  }
}
