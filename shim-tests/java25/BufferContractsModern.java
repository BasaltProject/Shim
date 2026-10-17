/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.nio.*;
import java.util.*;

public class BufferContractsModern {
  interface Body {
    Object get() throws Exception;
  }

  static void probe(String name, Body body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage());
    }
    System.out.println(name + "=" + out.replaceAll("@[0-9a-f]+", "@H"));
  }

  static String info(Buffer b) {
    return b.position() + "|" + b.limit() + "|" + b.capacity() + "|" + b.isDirect() + "|" + b.isReadOnly() + "|" + b.hasArray() + "|" + (b.hasArray() ? b.arrayOffset() : -1) + "|" + b.remaining() + "|" + b.hasRemaining();
  }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.US);
    probe("native order", () -> ByteOrder.nativeOrder() + " " + ByteOrder.BIG_ENDIAN + " " + ByteOrder.LITTLE_ENDIAN);
    probe("byte alloc", () -> info(ByteBuffer.allocate(10)) + " " + ByteBuffer.allocate(10).order() + " " + info(ByteBuffer.allocateDirect(10)) + " " + ByteBuffer.allocateDirect(10).order());
    probe("byte alloc neg", () -> ByteBuffer.allocate(-1));
    probe("byte allocDirect neg", () -> ByteBuffer.allocateDirect(-1));
    probe("byte wrap", () -> info(ByteBuffer.wrap(new byte[10])) + " " + info(ByteBuffer.wrap(new byte[10], 2, 5)) + " " + ByteBuffer.wrap(new byte[10], 2, 5).arrayOffset());
    probe("byte wrap bad", () -> ByteBuffer.wrap(new byte[10], 8, 5));
    probe("byte wrap neg", () -> ByteBuffer.wrap(new byte[10], -1, 5));
    probe("byte slice", () -> { ByteBuffer b = ByteBuffer.allocate(10); b.position(3).limit(8); ByteBuffer s = b.slice(); b.put(3, (byte) 9); return info(s) + " " + s.get(0) + " " + s.order() + " " + info(b.slice(1, 2)) + " " + b.slice(1, 2).arrayOffset(); });
    probe("byte slice bad", () -> { ByteBuffer b = ByteBuffer.allocate(10); b.limit(5); return b.slice(3, 4); });
    probe("byte slice order", () -> { ByteBuffer b = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN); return b.slice().order() + " " + b.duplicate().order() + " " + b.asReadOnlyBuffer().order() + " " + b.asIntBuffer().order() + " " + ByteBuffer.allocate(4).asIntBuffer().order(); });
    probe("byte duplicate", () -> { ByteBuffer b = ByteBuffer.allocate(10); b.position(2).limit(7); b.mark(); b.position(4); ByteBuffer d = b.duplicate(); d.position(6); d.reset(); return info(d) + " " + d.position() + " " + info(b); });
    probe("byte readonly", () -> { ByteBuffer b = ByteBuffer.allocate(10).asReadOnlyBuffer(); StringBuilder sb = new StringBuilder(info(b)); try { b.put((byte) 1); } catch (ReadOnlyBufferException e) { sb.append(" ro1"); } try { b.array(); } catch (ReadOnlyBufferException e) { sb.append(" ro2"); } try { b.compact(); } catch (ReadOnlyBufferException e) { sb.append(" ro3"); } try { b.putInt(1); } catch (ReadOnlyBufferException e) { sb.append(" ro4"); } try { b.put(new byte[1]); } catch (ReadOnlyBufferException e) { sb.append(" ro5"); } try { b.arrayOffset(); } catch (ReadOnlyBufferException e) { sb.append(" ro6"); } return sb + " " + b.hasArray() + " " + b.asReadOnlyBuffer().isReadOnly() + " " + b.slice().isReadOnly() + " " + b.duplicate().isReadOnly(); });
    probe("byte get put abs", () -> { ByteBuffer b = ByteBuffer.allocate(4); b.put(0, (byte) 1).put(3, (byte) 4); return Arrays.toString(b.array()) + " " + b.get(3) + " " + info(b); });
    probe("byte get oob", () -> ByteBuffer.allocate(4).get(4));
    probe("byte get oob neg", () -> ByteBuffer.allocate(4).get(-1));
    probe("byte put oob", () -> ByteBuffer.allocate(4).put(4, (byte) 1));
    probe("byte relative overflow", () -> { ByteBuffer b = ByteBuffer.allocate(1); b.put((byte) 1); return b.put((byte) 2); });
    probe("byte relative underflow", () -> { ByteBuffer b = ByteBuffer.allocate(1); b.get(); return b.get(); });
    probe("byte bulk", () -> { ByteBuffer b = ByteBuffer.allocate(8); b.put(new byte[] {1, 2, 3}).put(new byte[] {9, 8, 7, 6}, 1, 2); byte[] out = new byte[3]; b.flip().get(out); return Arrays.toString(b.array()) + " " + Arrays.toString(out) + " " + info(b); });
    probe("byte bulk overflow", () -> ByteBuffer.allocate(2).put(new byte[3]));
    probe("byte bulk underflow", () -> ByteBuffer.allocate(2).get(new byte[3]));
    probe("byte bulk bad range", () -> ByteBuffer.allocate(5).put(new byte[3], 2, 2));
    probe("byte bulk get bad range", () -> ByteBuffer.allocate(5).get(new byte[3], -1, 2));
    probe("byte put buffer", () -> { ByteBuffer a = ByteBuffer.allocate(4); a.put(new byte[] {1, 2, 3}).flip(); ByteBuffer b = ByteBuffer.allocate(6); b.put(a); return info(a) + " " + info(b) + " " + Arrays.toString(b.array()); });
    probe("byte put self", () -> { ByteBuffer a = ByteBuffer.allocate(4); return a.put(a); });
    probe("byte put buffer overflow", () -> ByteBuffer.allocate(1).put(ByteBuffer.allocate(2)));
    probe("byte put abs buffer", () -> { ByteBuffer a = ByteBuffer.wrap(new byte[] {1, 2, 3, 4}); ByteBuffer b = ByteBuffer.allocate(6); b.put(2, a, 1, 3); return Arrays.toString(b.array()) + " " + info(a); });
    probe("byte put abs buffer bad", () -> ByteBuffer.allocate(6).put(5, ByteBuffer.allocate(4), 0, 3));
    probe("byte put abs self overlap", () -> { ByteBuffer a = ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5, 6}); a.put(2, a, 0, 4); return Arrays.toString(a.array()); });
    probe("byte get abs array", () -> { ByteBuffer a = ByteBuffer.wrap(new byte[] {1, 2, 3, 4}); byte[] out = new byte[3]; a.get(1, out); byte[] out2 = new byte[5]; a.get(0, out2, 2, 3); return Arrays.toString(out) + Arrays.toString(out2) + info(a); });
    probe("byte put abs array", () -> { ByteBuffer a = ByteBuffer.allocate(4); a.put(1, new byte[] {7, 8}); a.put(0, new byte[] {1, 2, 3}, 2, 1); return Arrays.toString(a.array()) + info(a); });
    probe("byte flip clear rewind", () -> { ByteBuffer b = ByteBuffer.allocate(8); b.put((byte) 1).put((byte) 2); b.flip(); String f = info(b); b.get(); b.rewind(); String r = info(b); b.clear(); return f + " " + r + " " + info(b); });
    probe("byte mark reset", () -> { ByteBuffer b = ByteBuffer.allocate(8); b.position(2).mark().position(5).reset(); int p = b.position(); b.position(1); try { b.reset(); return p + " no"; } catch (InvalidMarkException e) { return p + " ime"; } });
    probe("byte reset nomark", () -> ByteBuffer.allocate(4).reset());
    probe("byte position bad", () -> ByteBuffer.allocate(4).position(5));
    probe("byte position neg", () -> ByteBuffer.allocate(4).position(-1));
    probe("byte limit bad", () -> ByteBuffer.allocate(4).limit(5));
    probe("byte limit shrink", () -> { ByteBuffer b = ByteBuffer.allocate(8); b.position(6).mark(); b.limit(3); return info(b) + " " + b.position(); });
    probe("byte compact", () -> { ByteBuffer b = ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5}); b.position(2).limit(4); b.compact(); return Arrays.toString(b.array()) + info(b); });
    probe("byte compact direct", () -> { ByteBuffer b = ByteBuffer.allocateDirect(5); b.put(new byte[] {1, 2, 3, 4, 5}); b.position(2).limit(4); b.compact(); byte[] out = new byte[5]; b.clear(); b.get(out); return Arrays.toString(out); });
    probe("byte typed", () -> { ByteBuffer b = ByteBuffer.allocate(40); b.putShort((short) 0x0102).putChar('x').putInt(0x03040506).putLong(0x0708090a0b0c0d0eL).putFloat(1.5f).putDouble(2.25); b.flip(); return b.getShort() + " " + b.getChar() + " " + Integer.toHexString(b.getInt()) + " " + Long.toHexString(b.getLong()) + " " + b.getFloat() + " " + b.getDouble() + " " + info(b); });
    probe("byte typed little", () -> { ByteBuffer b = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN); b.putInt(0x01020304).putShort((short) 0x0506); return Arrays.toString(Arrays.copyOf(b.array(), 6)) + " " + b.order(); });
    probe("byte typed direct order", () -> { ByteBuffer b = ByteBuffer.allocateDirect(8); b.putInt(0x01020304); b.order(ByteOrder.LITTLE_ENDIAN); b.putInt(0x01020304); b.flip(); return b.get(0) + " " + b.get(3) + " " + b.get(4) + " " + b.get(7); });
    probe("byte typed abs", () -> { ByteBuffer b = ByteBuffer.allocate(16); b.putInt(2, 0x01020304).putLong(6, 5L); return b.getInt(2) + " " + b.getLong(6) + " " + b.getShort(2) + " " + b.getChar(4) + " " + info(b); });
    probe("byte typed abs oob", () -> ByteBuffer.allocate(4).getInt(1));
    probe("byte typed overflow", () -> ByteBuffer.allocate(3).putInt(1));
    probe("byte typed underflow", () -> ByteBuffer.allocate(3).getInt());
    probe("byte typed float nan", () -> { ByteBuffer b = ByteBuffer.allocate(8); b.putFloat(Float.NaN).putDouble(Double.NaN); b.flip(); return Integer.toHexString(b.getInt(0)) + " " + Long.toHexString(b.getLong(4)); });
    probe("byte views", () -> { ByteBuffer b = ByteBuffer.allocate(32); b.position(1); ShortBuffer s = b.asShortBuffer(); CharBuffer c = b.asCharBuffer(); IntBuffer i = b.asIntBuffer(); LongBuffer l = b.asLongBuffer(); FloatBuffer f = b.asFloatBuffer(); DoubleBuffer d = b.asDoubleBuffer(); return info(s) + " " + info(c) + " " + info(i) + " " + info(l) + " " + info(f) + " " + info(d) + " " + s.order() + c.order() + i.order() + l.order() + f.order() + d.order(); });
    probe("byte views write", () -> { ByteBuffer b = ByteBuffer.allocate(16); IntBuffer i = b.asIntBuffer(); i.put(0x01020304).put(5); b.order(ByteOrder.LITTLE_ENDIAN); IntBuffer j = b.asIntBuffer(); j.put(1, 0x0a0b0c0d); return Arrays.toString(b.array()) + info(i) + info(j); });
    probe("byte views readonly", () -> { ByteBuffer b = ByteBuffer.allocate(16).asReadOnlyBuffer(); IntBuffer i = b.asIntBuffer(); try { i.put(1); return "put"; } catch (ReadOnlyBufferException e) { return info(i) + " ro"; } });
    probe("byte views direct", () -> { ByteBuffer b = ByteBuffer.allocateDirect(16); LongBuffer l = b.asLongBuffer(); l.put(0x0102030405060708L); return b.get(0) + " " + b.get(7) + " " + info(l) + " " + l.get(0); });
    probe("view slice", () -> { ByteBuffer b = ByteBuffer.allocate(32); IntBuffer i = b.asIntBuffer(); i.position(2); IntBuffer s = i.slice(); s.put(0, 7); return info(s) + " " + i.get(2) + " " + s.order() + " " + info(i.duplicate()) + " " + info(i.asReadOnlyBuffer()); });
    probe("view compact", () -> { ByteBuffer b = ByteBuffer.allocate(16); IntBuffer i = b.asIntBuffer(); i.put(new int[] {1, 2, 3, 4}); i.flip(); i.get(); i.compact(); return Arrays.toString(b.array()) + info(i); });
    probe("view equals", () -> { ByteBuffer a = ByteBuffer.allocate(8); a.putInt(0, 5); ByteBuffer b = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN); b.putInt(0, 5); return a.asIntBuffer().equals(b.asIntBuffer()) + " " + a.asIntBuffer().compareTo(b.asIntBuffer()) + " " + a.asIntBuffer().mismatch(b.asIntBuffer()); });
    probe("int alloc", () -> info(IntBuffer.allocate(4)) + " " + IntBuffer.allocate(4).order() + " " + info(IntBuffer.wrap(new int[6], 1, 3)) + " " + IntBuffer.wrap(new int[6], 1, 3).arrayOffset());
    probe("char alloc", () -> info(CharBuffer.allocate(4)) + " " + CharBuffer.allocate(4).order() + " " + CharBuffer.wrap("hello").order() + " " + info(CharBuffer.wrap("hello")));
    probe("short long float double order", () -> ShortBuffer.allocate(1).order() + " " + LongBuffer.allocate(1).order() + " " + FloatBuffer.allocate(1).order() + " " + DoubleBuffer.allocate(1).order() + " " + CharBuffer.allocate(1).order());
    probe("int ops", () -> { IntBuffer b = IntBuffer.allocate(6); b.put(1).put(new int[] {2, 3}).put(1, 9); b.flip(); int[] out = new int[3]; b.get(out); b.rewind(); return Arrays.toString(b.array()) + Arrays.toString(out) + info(b) + " " + b.toString() + " " + b.hashCode(); });
    probe("int equals compare", () -> { IntBuffer a = IntBuffer.wrap(new int[] {1, 2, 3}); IntBuffer b = IntBuffer.wrap(new int[] {0, 1, 2, 3}, 1, 3); IntBuffer c = IntBuffer.wrap(new int[] {1, 2, 4}); return a.equals(b) + " " + a.equals(c) + " " + a.compareTo(b) + " " + a.compareTo(c) + " " + c.compareTo(a) + " " + a.mismatch(c) + " " + a.mismatch(b) + " " + a.mismatch(IntBuffer.allocate(0)) + " " + a.mismatch(IntBuffer.wrap(new int[] {1, 2, 3, 4})) + " " + (a.hashCode() == b.hashCode()) + " " + a.equals("x") + " " + a.equals(null); });
    probe("float equals nan", () -> { FloatBuffer a = FloatBuffer.wrap(new float[] {Float.NaN, 0.0f}); FloatBuffer b = FloatBuffer.wrap(new float[] {Float.NaN, -0.0f}); return a.equals(b) + " " + a.compareTo(b) + " " + a.mismatch(b) + " " + FloatBuffer.wrap(new float[] {0.0f}).equals(FloatBuffer.wrap(new float[] {-0.0f})); });
    probe("double equals nan", () -> { DoubleBuffer a = DoubleBuffer.wrap(new double[] {Double.NaN, 0.0}); DoubleBuffer b = DoubleBuffer.wrap(new double[] {Double.NaN, -0.0}); return a.equals(b) + " " + a.compareTo(b) + " " + a.mismatch(b); });
    probe("byte equals compare", () -> { ByteBuffer a = ByteBuffer.wrap(new byte[] {1, 2, -3}); ByteBuffer b = ByteBuffer.wrap(new byte[] {1, 2, 3}); return a.equals(b) + " " + a.compareTo(b) + " " + b.compareTo(a) + " " + a.mismatch(b) + " " + a.hashCode() + " " + b.hashCode() + " " + ByteBuffer.allocate(0).hashCode(); });
    probe("byte compare direct heap", () -> { ByteBuffer a = ByteBuffer.allocateDirect(3); a.put(new byte[] {1, 2, 3}).flip(); ByteBuffer b = ByteBuffer.wrap(new byte[] {1, 2, 3}); return a.equals(b) + " " + a.compareTo(b) + " " + a.hashCode() + " " + a.mismatch(b); });
    probe("toString", () -> ByteBuffer.allocate(5).toString() + " " + ByteBuffer.allocateDirect(5).toString() + " " + ByteBuffer.allocate(5).asReadOnlyBuffer() + " " + IntBuffer.allocate(2) + " " + CharBuffer.wrap("abc") + " " + ByteBuffer.allocate(8).asIntBuffer() + " " + ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer() + " " + ByteBuffer.allocate(8).asReadOnlyBuffer().asShortBuffer());
    probe("char buffer text", () -> { CharBuffer c = CharBuffer.wrap("hello world"); c.position(6); return c + " " + c.length() + " " + c.charAt(1) + " " + c.subSequence(1, 3) + " " + info(c) + " " + info(c.subSequence(0, 2)) + " " + c.chars().sum() + " " + c.isReadOnly() + " " + c.hasArray(); });
    probe("char buffer text bad", () -> CharBuffer.wrap("abc").subSequence(0, 5));
    probe("char buffer text bad2", () -> { CharBuffer c = CharBuffer.wrap("abc"); c.position(1); return c.charAt(5); });
    probe("char buffer put str", () -> { CharBuffer c = CharBuffer.allocate(8); c.put("abc").put("xyzw", 1, 3).append("Q").append("RSTU", 1, 2).append('Z'); return new String(c.array()) + info(c); });
    probe("char buffer put str overflow", () -> CharBuffer.allocate(2).put("abc"));
    probe("char buffer put str ro", () -> CharBuffer.wrap("abc").put("x"));
    probe("char buffer get str", () -> { CharBuffer c = CharBuffer.wrap("hello"); char[] out = new char[3]; c.get(out); return new String(out) + info(c); });
    probe("char buffer wrap seq", () -> { CharBuffer c = CharBuffer.wrap(new StringBuilder("abcdef"), 1, 4); return c + " " + info(c) + " " + c.slice() + " " + c.duplicate() + " " + c.asReadOnlyBuffer(); });
    probe("char buffer read", () -> { CharBuffer c = CharBuffer.allocate(4); int n = CharBuffer.wrap("ab").read(c); c.flip(); return n + " " + c + " " + info(c); });
    probe("char buffer read eof", () -> { CharBuffer s = CharBuffer.wrap("x"); s.get(); return s.read(CharBuffer.allocate(2)); });
    probe("char buffer read ro", () -> CharBuffer.wrap("ab").read(CharBuffer.wrap("cd")));
    probe("char buffer getChars", () -> { CharBuffer c = CharBuffer.wrap("hello world"); c.position(2); char[] out = new char[6]; c.getChars(1, 4, out, 2); return new String(out).replace('\0', '.') + info(c); });
    probe("char buffer getChars bad", () -> { CharBuffer.wrap("abc").getChars(2, 1, new char[5], 0); return "ok"; });
    probe("char buffer getChars bad dst", () -> { CharBuffer.wrap("abc").getChars(0, 3, new char[2], 0); return "ok"; });
    probe("charsequence getChars", () -> { StringBuilder sb = new StringBuilder("xyz"); char[] out = new char[4]; ((CharSequence) sb).getChars(0, 3, out, 1); return new String(out).replace('\0', '.'); });
    probe("buffer abs bulk int", () -> { IntBuffer b = IntBuffer.allocate(6); b.put(1, new int[] {1, 2, 3}); b.put(0, new int[] {9, 8, 7, 6}, 3, 1); int[] out = new int[4]; b.get(1, out, 1, 3); int[] out2 = new int[2]; b.get(4, out2); return Arrays.toString(b.array()) + Arrays.toString(out) + Arrays.toString(out2) + info(b); });
    probe("buffer abs bulk bad", () -> IntBuffer.allocate(4).put(3, new int[2]));
    probe("buffer abs bulk ro", () -> IntBuffer.allocate(4).asReadOnlyBuffer().put(0, new int[1]));
    probe("buffer abs buffer int", () -> { IntBuffer a = IntBuffer.wrap(new int[] {1, 2, 3, 4}); IntBuffer b = IntBuffer.allocate(6); b.put(1, a, 1, 3); return Arrays.toString(b.array()); });
    probe("buffer abs bulk long double", () -> { LongBuffer l = LongBuffer.allocate(3); l.put(0, new long[] {1, 2, 3}); DoubleBuffer d = DoubleBuffer.allocate(3); d.put(1, new double[] {1.5}); ShortBuffer s = ShortBuffer.allocate(2); s.put(0, new short[] {4, 5}); FloatBuffer f = FloatBuffer.allocate(2); f.put(0, new float[] {1f, 2f}); CharBuffer c = CharBuffer.allocate(2); c.put(0, new char[] {'a', 'b'}); return Arrays.toString(l.array()) + Arrays.toString(d.array()) + Arrays.toString(s.array()) + Arrays.toString(f.array()) + Arrays.toString(c.array()); });
    probe("aligned slice", () -> { ByteBuffer b = ByteBuffer.allocateDirect(64); b.position(3).limit(50); ByteBuffer s = b.alignedSlice(8); return b.alignmentOffset(0, 8) + " " + b.alignmentOffset(3, 4) + " " + s.capacity() % 8 + " " + (s.capacity() > 0) + " " + b.alignedSlice(1).capacity(); });
    probe("alignment bad", () -> ByteBuffer.allocate(8).alignmentOffset(0, 3));
    probe("alignment heap big unit", () -> ByteBuffer.allocate(8).alignmentOffset(0, 16));
    probe("alignment neg", () -> ByteBuffer.allocate(8).alignmentOffset(-1, 4));
    probe("heap alignment small", () -> ByteBuffer.allocate(64).alignmentOffset(0, 4) + " " + ByteBuffer.allocate(64).alignmentOffset(1, 4) + " " + ByteBuffer.wrap(new byte[64], 3, 20).alignmentOffset(0, 8));
    probe("array access", () -> { byte[] arr = new byte[5]; ByteBuffer b = ByteBuffer.wrap(arr, 1, 3); return (b.array() == arr) + " " + b.arrayOffset() + " " + ByteBuffer.allocateDirect(4).hasArray(); });
    probe("array on direct", () -> ByteBuffer.allocateDirect(4).array());
    probe("array offset on direct", () -> ByteBuffer.allocateDirect(4).arrayOffset());
    probe("order change", () -> { ByteBuffer b = ByteBuffer.allocate(4); ByteBuffer r = b.order(ByteOrder.LITTLE_ENDIAN); return (r == b) + " " + b.order() + " " + b.order(ByteOrder.BIG_ENDIAN).order(); });
    probe("order null", () -> ByteBuffer.allocate(4).order(null));
    probe("buffer wrap null", () -> ByteBuffer.wrap(null));
    probe("put null", () -> ByteBuffer.allocate(4).put((byte[]) null));
    probe("put buffer null", () -> ByteBuffer.allocate(4).put((ByteBuffer) null));
    probe("short alloc ops", () -> { ShortBuffer s = ShortBuffer.allocate(3); s.put((short) 1).put((short) -2); s.flip(); return s.get() + " " + s.get() + " " + info(s) + " " + s; });
    probe("long ops", () -> { LongBuffer l = LongBuffer.wrap(new long[] {1, 2, 3}); l.position(1); return l.get() + " " + info(l.slice()) + " " + l.compareTo(LongBuffer.wrap(new long[] {2, 3})) + " " + l.hashCode(); });
    probe("double ops", () -> { DoubleBuffer d = DoubleBuffer.wrap(new double[] {1.5, 2.5}); return d.get() + " " + d.hashCode() + " " + d; });
    probe("float ops", () -> { FloatBuffer d = FloatBuffer.wrap(new float[] {1.5f, 2.5f}); return d.get() + " " + d.hashCode() + " " + d; });
    probe("clear flip chain", () -> { ByteBuffer b = ByteBuffer.allocate(4); Buffer x = b.clear(); Buffer y = b.flip(); Buffer z = b.rewind(); Buffer w = b.mark(); return (x == b) + " " + (y == b) + " " + (z == b) + " " + (w == b); });
    probe("covariant returns", () -> ((Object) ByteBuffer.allocate(4).position(1)).getClass().getSimpleName() + " " + ((Object) IntBuffer.allocate(4).limit(2)).getClass().getSimpleName() + " " + ((Object) CharBuffer.allocate(4).flip()).getClass().getSimpleName());
    probe("mapped slice types", () -> { java.lang.reflect.Method[] ms = java.nio.MappedByteBuffer.class.getDeclaredMethods(); List<String> names = new ArrayList<>(); for (java.lang.reflect.Method m : ms) if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) names.add(m.getName() + ":" + m.getReturnType().getSimpleName()); Collections.sort(names); return names; });
    probe("bulk transfer large", () -> { ByteBuffer a = ByteBuffer.allocate(100000); for (int i = 0; i < a.capacity(); i++) a.put(i, (byte) i); ByteBuffer d = ByteBuffer.allocateDirect(100000); d.put(a.duplicate()); d.flip(); ByteBuffer c = ByteBuffer.allocate(100000); c.put(d); c.flip(); return a.equals(c) + " " + c.hashCode() + " " + d.limit(); });
    probe("direct typed views roundtrip", () -> { ByteBuffer d = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()); d.asDoubleBuffer().put(1.5).put(2.5); d.asFloatBuffer().put(8, 3.5f); return d.getDouble(0) + " " + d.getDouble(8) + " " + d.getFloat(32); });
  }
}
