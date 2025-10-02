/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import shim.VMClass;

public class ObjectInputStream extends InputStream implements ObjectInput, ObjectStreamConstants {
  private static final Object kUnshared = new Object();

  private static final HashMap<String, Class<?>> kPrimitives = new HashMap<String, Class<?>>();

  static {
    kPrimitives.put("boolean", Boolean.TYPE);
    kPrimitives.put("byte", Byte.TYPE);
    kPrimitives.put("char", Character.TYPE);
    kPrimitives.put("short", Short.TYPE);
    kPrimitives.put("int", Integer.TYPE);
    kPrimitives.put("long", Long.TYPE);
    kPrimitives.put("float", Float.TYPE);
    kPrimitives.put("double", Double.TYPE);
    kPrimitives.put("void", Void.TYPE);
  }

  private final InputStream raw;
  private int peeked = -1;
  private long streamBytes;
  private boolean blockMode;
  private int blkRemain;
  private final ArrayList<Object> handles = new ArrayList<Object>();
  private int depth;
  private final boolean enableOverride;
  private boolean enableResolve;
  private boolean defaultDataEnd;
  private Object curObj;
  private ObjectStreamClass curDesc;
  private List<Validation> validations;
  private ObjectInputFilter serialFilter;
  private final byte[] scratch = new byte[8];

  private static final class Validation {
    final ObjectInputValidation obj;
    final int prio;

    Validation(ObjectInputValidation obj, int prio) {
      this.obj = obj;
      this.prio = prio;
    }
  }

  public ObjectInputStream(InputStream in) throws IOException {
    if (in == null) {
      throw new NullPointerException();
    }
    this.raw = in;
    this.enableOverride = false;
    ObjectInputFilter global = ObjectInputFilter.Config.getSerialFilter();
    serialFilter = ObjectInputFilter.Config.getSerialFilterFactory().apply(null, global);
    readStreamHeader();
    blockMode = true;
  }

  protected ObjectInputStream() throws IOException, SecurityException {
    this.raw = null;
    this.enableOverride = true;
  }

  public final ObjectInputFilter getObjectInputFilter() {
    return serialFilter;
  }

  public final void setObjectInputFilter(ObjectInputFilter filter) {
    if (handles.size() > 0) {
      throw new IllegalStateException("filter can not be set after an object has been read");
    }
    ObjectInputFilter next = ObjectInputFilter.Config.getSerialFilterFactory().apply(serialFilter, filter);
    if (next == null && filter != null) {
      throw new NullPointerException("filter factory returned null when filter is non-null");
    }
    serialFilter = next;
  }

  public final Object readObject() throws IOException, ClassNotFoundException {
    if (enableOverride) {
      return readObjectOverride();
    }
    depth++;
    try {
      Object obj = readObject0(false);
      if (depth == 1) {
        runValidations();
      }
      return obj;
    } finally {
      depth--;
      if (depth == 0) {
        clearIfOutermost();
      }
    }
  }

  protected Object readObjectOverride() throws IOException, ClassNotFoundException {
    return null;
  }

  public Object readUnshared() throws IOException, ClassNotFoundException {
    depth++;
    try {
      Object obj = readObject0(true);
      if (depth == 1) {
        runValidations();
      }
      return obj;
    } finally {
      depth--;
    }
  }

  private void clearIfOutermost() {
    validations = null;
  }

  private void runValidations() throws InvalidObjectException {
    List<Validation> list = validations;
    validations = null;
    if (list == null) {
      return;
    }
    for (int i = 1; i < list.size(); i++) {
      Validation v = list.get(i);
      int j = i - 1;
      while (j >= 0 && list.get(j).prio < v.prio) {
        list.set(j + 1, list.get(j));
        j--;
      }
      list.set(j + 1, v);
    }
    for (Validation v : list) {
      v.obj.validateObject();
    }
  }

  public void defaultReadObject() throws IOException, ClassNotFoundException {
    Object obj = curObj;
    ObjectStreamClass desc = curDesc;
    if (obj == null || desc == null) {
      throw new NotActiveException("not in call to readObject");
    }
    boolean old = setBlockMode(false);
    try {
      defaultReadFields(obj, desc);
    } finally {
      setBlockMode(true);
    }
    if (!desc.hasWriteObjectData()) {
      defaultDataEnd = true;
    }
  }

  public ObjectInputStream.GetField readFields() throws IOException, ClassNotFoundException {
    Object obj = curObj;
    ObjectStreamClass desc = curDesc;
    if (obj == null || desc == null) {
      throw new NotActiveException("not in call to readObject");
    }
    setBlockMode(false);
    GetFieldImpl getField = new GetFieldImpl(desc);
    try {
      getField.readFields();
    } finally {
      setBlockMode(true);
    }
    if (!desc.hasWriteObjectData()) {
      defaultDataEnd = true;
    }
    return getField;
  }

  public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException,
      InvalidObjectException {
    if (depth == 0) {
      throw new NotActiveException("stream inactive");
    }
    if (obj == null) {
      throw new InvalidObjectException("null callback");
    }
    if (validations == null) {
      validations = new ArrayList<Validation>();
    }
    validations.add(new Validation(obj, prio));
  }

  protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    String name = desc.getName();
    try {
      return Class.forName(name, false, loader());
    } catch (ClassNotFoundException ex) {
      Class<?> cl = kPrimitives.get(name);
      if (cl != null) {
        return cl;
      }
      throw ex;
    }
  }

  private static ClassLoader loader() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return cl != null ? cl : ClassLoader.getSystemClassLoader();
  }

  protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
    ClassLoader ldr = loader();
    Class<?>[] cinterfaces = new Class<?>[interfaces.length];
    for (int i = 0; i < interfaces.length; i++) {
      cinterfaces[i] = Class.forName(interfaces[i], false, ldr);
    }
    try {
      return Proxy.getProxyClass(ldr, cinterfaces);
    } catch (IllegalArgumentException e) {
      throw new ClassNotFoundException(null, e);
    }
  }

  protected Object resolveObject(Object obj) throws IOException {
    return obj;
  }

  protected boolean enableResolveObject(boolean enable) throws SecurityException {
    boolean old = enableResolve;
    enableResolve = enable;
    return old;
  }

  protected void readStreamHeader() throws IOException, StreamCorruptedException {
    short s0 = (short) rawShort();
    short s1 = (short) rawShort();
    if (s0 != STREAM_MAGIC || s1 != STREAM_VERSION) {
      throw new StreamCorruptedException(String.format("invalid stream header: %04X%04X", s0, s1));
    }
  }

  protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
    return ObjectStreamClass.readNonProxy(this);
  }

  /* ---- raw byte layer ---- */

  private int rawPeek() throws IOException {
    if (peeked < 0) {
      peeked = raw.read();
      if (peeked >= 0) {
        streamBytes++;
      }
    }
    return peeked;
  }

  private int rawByte() throws IOException {
    int b = rawPeek();
    peeked = -1;
    return b;
  }

  private int rawRead(byte[] b, int off, int len) throws IOException {
    if (len == 0) {
      return 0;
    }
    if (peeked >= 0) {
      b[off] = (byte) peeked;
      peeked = -1;
      return 1;
    }
    int n = raw.read(b, off, len);
    if (n > 0) {
      streamBytes += n;
    }
    return n;
  }

  private int rawShort() throws IOException {
    int a = rawByte();
    int b = rawByte();
    if ((a | b) < 0) {
      throw new EOFException();
    }
    return (a << 8) | b;
  }

  private int rawAvailable() throws IOException {
    return raw.available() + (peeked >= 0 ? 1 : 0);
  }

  /* ---- block data layer ---- */

  private boolean setBlockMode(boolean mode) throws IOException {
    boolean old = blockMode;
    if (old != mode) {
      blkRemain = 0;
      blockMode = mode;
    }
    return old;
  }

  private int currentBlockRemaining() throws IOException {
    if (!blockMode) {
      return 0;
    }
    if (blkRemain == 0) {
      refill();
    }
    return blkRemain > 0 ? blkRemain : 0;
  }

  private void refill() throws IOException {
    int tc = rawPeek();
    if (tc == TC_BLOCKDATA) {
      rawByte();
      int len = rawByte();
      if (len < 0) {
        throw new StreamCorruptedException("unexpected EOF in block header");
      }
      blkRemain = len == 0 ? 0 : len;
      if (len == 0) {
        refill();
      }
    } else if (tc == TC_BLOCKDATALONG) {
      rawByte();
      int len = rawInt();
      if (len < 0) {
        throw new StreamCorruptedException("illegal block data header length: " + len);
      }
      blkRemain = len;
      if (len == 0) {
        refill();
      }
    } else {
      blkRemain = -1;
    }
  }

  private int rawInt() throws IOException {
    int a = rawByte();
    int b = rawByte();
    int c = rawByte();
    int d = rawByte();
    if ((a | b | c | d) < 0) {
      throw new EOFException();
    }
    return (a << 24) | (b << 16) | (c << 8) | d;
  }

  private int readData(byte[] b, int off, int len) throws IOException {
    if (len == 0) {
      return 0;
    }
    if (!blockMode) {
      return rawRead(b, off, len);
    }
    if (blkRemain == 0) {
      refill();
    }
    if (blkRemain < 0) {
      return -1;
    }
    int n = rawRead(b, off, Math.min(len, blkRemain));
    if (n < 0) {
      throw new StreamCorruptedException("unexpected EOF in block data");
    }
    blkRemain -= n;
    return n;
  }

  private void skipBlockData() throws IOException {
    if (!blockMode) {
      throw new IllegalStateException("not in block data mode");
    }
    byte[] tmp = new byte[256];
    while (readData(tmp, 0, tmp.length) >= 0) {
    }
  }

  private void fill(byte[] b, int off, int len) throws IOException {
    while (len > 0) {
      int n = readData(b, off, len);
      if (n < 0) {
        throw new EOFException();
      }
      off += n;
      len -= n;
    }
  }

  /* ---- DataInput / InputStream ---- */

  @Override
  public int read() throws IOException {
    if (!blockMode) {
      return rawByte();
    }
    byte[] b = scratch;
    return readData(b, 0, 1) < 0 ? -1 : (b[0] & 0xFF);
  }

  @Override
  public int read(byte[] buf, int off, int len) throws IOException {
    if (buf == null) {
      throw new NullPointerException();
    }
    java.util.Objects.checkFromIndexSize(off, len, buf.length);
    return readData(buf, off, len);
  }

  @Override
  public int available() throws IOException {
    if (blockMode) {
      int rem = currentBlockRemaining();
      return Math.min(rem, rawAvailable());
    }
    return rawAvailable();
  }

  @Override
  public void close() throws IOException {
    raw.close();
  }

  @Override
  public long skip(long n) throws IOException {
    return super.skip(n);
  }

  public void readFully(byte[] buf) throws IOException {
    fill(buf, 0, buf.length);
  }

  public void readFully(byte[] buf, int off, int len) throws IOException {
    java.util.Objects.checkFromIndexSize(off, len, buf.length);
    fill(buf, off, len);
  }

  public int skipBytes(int len) throws IOException {
    int total = 0;
    byte[] tmp = new byte[Math.min(Math.max(len, 1), 512)];
    while (total < len) {
      int n = readData(tmp, 0, Math.min(tmp.length, len - total));
      if (n < 0) {
        break;
      }
      total += n;
    }
    return total;
  }

  public boolean readBoolean() throws IOException {
    fill(scratch, 0, 1);
    return scratch[0] != 0;
  }

  public byte readByte() throws IOException {
    fill(scratch, 0, 1);
    return scratch[0];
  }

  public int readUnsignedByte() throws IOException {
    fill(scratch, 0, 1);
    return scratch[0] & 0xFF;
  }

  public char readChar() throws IOException {
    fill(scratch, 0, 2);
    return (char) (((scratch[0] & 0xFF) << 8) | (scratch[1] & 0xFF));
  }

  public short readShort() throws IOException {
    return (short) readChar();
  }

  public int readUnsignedShort() throws IOException {
    return readChar();
  }

  public int readInt() throws IOException {
    fill(scratch, 0, 4);
    return getInt(scratch, 0);
  }

  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  public long readLong() throws IOException {
    fill(scratch, 0, 8);
    return getLong(scratch, 0);
  }

  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  @Deprecated
  public String readLine() throws IOException {
    StringBuilder sb = new StringBuilder();
    boolean any = false;
    for (;;) {
      int c = read();
      if (c < 0) {
        return any ? sb.toString() : null;
      }
      any = true;
      if (c == '\n') {
        return sb.toString();
      }
      if (c == '\r') {
        return sb.toString();
      }
      sb.append((char) c);
    }
  }

  public String readUTF() throws IOException {
    int len = readUnsignedShort();
    return readUTFBody(len);
  }

  private String readUTFBody(long len) throws IOException {
    byte[] buf = new byte[(int) len];
    fill(buf, 0, buf.length);
    return decodeUTF(buf);
  }

  private static String decodeUTF(byte[] b) throws UTFDataFormatException {
    char[] out = new char[b.length];
    int n = 0;
    int i = 0;
    while (i < b.length) {
      int c = b[i] & 0xFF;
      switch (c >> 4) {
        case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
          i++;
          out[n++] = (char) c;
          break;
        case 12: case 13: {
          if (i + 1 >= b.length) {
            throw new UTFDataFormatException("malformed input: partial character at end");
          }
          int c2 = b[i + 1];
          if ((c2 & 0xC0) != 0x80) {
            throw new UTFDataFormatException("malformed input around byte " + (i + 1));
          }
          out[n++] = (char) (((c & 0x1F) << 6) | (c2 & 0x3F));
          i += 2;
          break;
        }
        case 14: {
          if (i + 2 >= b.length) {
            throw new UTFDataFormatException("malformed input: partial character at end");
          }
          int c2 = b[i + 1];
          int c3 = b[i + 2];
          if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) {
            throw new UTFDataFormatException("malformed input around byte " + (i + 1));
          }
          out[n++] = (char) (((c & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
          i += 3;
          break;
        }
        default:
          throw new UTFDataFormatException("malformed input around byte " + i);
      }
    }
    return new String(out, 0, n);
  }

  static int getInt(byte[] b, int off) {
    return ((b[off] & 0xFF) << 24) | ((b[off + 1] & 0xFF) << 16) | ((b[off + 2] & 0xFF) << 8) | (b[off + 3] & 0xFF);
  }

  static long getLong(byte[] b, int off) {
    return ((long) getInt(b, off) << 32) | (getInt(b, off + 4) & 0xFFFFFFFFL);
  }

  static short getShort(byte[] b, int off) {
    return (short) (((b[off] & 0xFF) << 8) | (b[off + 1] & 0xFF));
  }

  /* ---- handles ---- */

  private int assign(Object obj) {
    handles.add(obj);
    return handles.size() - 1;
  }

  private void setHandle(int h, Object obj) {
    if (handles.get(h) != kUnshared) {
      handles.set(h, obj);
    }
  }

  private Object readHandle() throws IOException {
    int h = rawInt() - baseWireHandle;
    if (h < 0 || h >= handles.size()) {
      throw new StreamCorruptedException(String.format("invalid handle value: %08X", h + baseWireHandle));
    }
    Object o = handles.get(h);
    if (o == kUnshared) {
      throw new InvalidObjectException("cannot read back reference to unshared object");
    }
    return o;
  }

  /* ---- filter ---- */

  private void filterCheck(final Class<?> clazz, final long arrayLength) throws InvalidClassException {
    ObjectInputFilter f = serialFilter;
    if (f == null) {
      return;
    }
    final long refs = handles.size();
    final long d = depth;
    final long bytes = streamBytes;
    ObjectInputFilter.Status status = f.checkInput(new ObjectInputFilter.FilterInfo() {
      public Class<?> serialClass() {
        return clazz;
      }

      public long arrayLength() {
        return arrayLength;
      }

      public long depth() {
        return d;
      }

      public long references() {
        return refs;
      }

      public long streamBytes() {
        return bytes;
      }
    });
    if (status == null || status == ObjectInputFilter.Status.REJECTED) {
      throw new InvalidClassException("filter status: " + status);
    }
  }

  /* ---- object graph ---- */

  private Object readObject0(boolean unshared) throws IOException, ClassNotFoundException {
    boolean oldMode = blockMode;
    if (oldMode) {
      int remain = currentBlockRemaining();
      if (remain > 0) {
        throw new OptionalDataException(remain);
      } else if (defaultDataEnd) {
        throw new OptionalDataException(true);
      }
      setBlockMode(false);
    }
    int tc;
    while ((tc = rawPeek()) == TC_RESET) {
      rawByte();
      handles.clear();
    }
    try {
      switch (tc) {
        case TC_NULL:
          rawByte();
          return null;
        case TC_REFERENCE:
          rawByte();
          return checkResolve(readHandle());
        case TC_CLASS:
          return checkResolve(readClass(unshared));
        case TC_CLASSDESC:
        case TC_PROXYCLASSDESC:
          return checkResolve(readClassDesc(unshared));
        case TC_STRING:
        case TC_LONGSTRING:
          return checkResolve(readString(unshared));
        case TC_ARRAY:
          return checkResolve(readArray(unshared));
        case TC_ENUM:
          return checkResolve(readEnum(unshared));
        case TC_OBJECT:
          return checkResolve(readOrdinaryObject(unshared));
        case TC_EXCEPTION: {
          rawByte();
          handles.clear();
          IOException ex = readFatalException();
          handles.clear();
          throw new WriteAbortedException("writing aborted", ex);
        }
        case TC_BLOCKDATA:
        case TC_BLOCKDATALONG:
          if (oldMode) {
            throw new StreamCorruptedException("unexpected block data");
          }
          throw new OptionalDataException(0);
        case TC_ENDBLOCKDATA:
          if (oldMode) {
            throw new OptionalDataException(true);
          }
          throw new StreamCorruptedException("unexpected end of block data");
        case -1:
          throw new EOFException();
        default:
          throw new StreamCorruptedException(String.format("invalid type code: %02X", tc));
      }
    } finally {
      if (oldMode) {
        setBlockMode(true);
      }
    }
  }

  private IOException readFatalException() throws IOException {
    try {
      Object o = readObject0(false);
      return (IOException) o;
    } catch (ClassNotFoundException e) {
      throw new StreamCorruptedException("invalid exception object");
    }
  }

  private Object checkResolve(Object obj) throws IOException {
    if (!enableResolve || obj == null) {
      return obj;
    }
    Object rep = resolveObject(obj);
    if (rep != obj) {
      if (rep != null && rep.getClass().isArray()) {
        filterCheck(rep.getClass(), Array.getLength(rep));
      } else if (rep != null) {
        filterCheck(rep.getClass(), -1);
      }
      for (int i = handles.size() - 1; i >= 0; i--) {
        if (handles.get(i) == obj) {
          handles.set(i, rep);
          break;
        }
      }
    }
    return rep;
  }

  private Class<?> readClass(boolean unshared) throws IOException, ClassNotFoundException {
    rawByte();
    ObjectStreamClass desc = readClassDesc(false);
    Class<?> cl = desc.forClass();
    assign(unshared ? kUnshared : cl);
    if (cl == null) {
      throw new ClassNotFoundException(desc.getName());
    }
    return cl;
  }

  private ObjectStreamClass readClassDesc(boolean unshared) throws IOException, ClassNotFoundException {
    int tc = rawPeek();
    switch (tc) {
      case TC_NULL:
        rawByte();
        return null;
      case TC_REFERENCE:
        rawByte();
        Object o = readHandle();
        if (!(o instanceof ObjectStreamClass)) {
          throw new StreamCorruptedException("invalid class descriptor reference");
        }
        return (ObjectStreamClass) o;
      case TC_PROXYCLASSDESC:
        return readProxyDesc(unshared);
      case TC_CLASSDESC:
        return readNonProxyDesc(unshared);
      case -1:
        throw new EOFException();
      default:
        throw new StreamCorruptedException(String.format("invalid type code: %02X", tc));
    }
  }

  private ObjectStreamClass readProxyDesc(boolean unshared) throws IOException, ClassNotFoundException {
    rawByte();
    ObjectStreamClass desc = new ObjectStreamClass();
    assign(unshared ? kUnshared : desc);
    int numIfaces = rawInt();
    String[] ifaces = new String[numIfaces];
    boolean old = setBlockMode(false);
    for (int i = 0; i < numIfaces; i++) {
      ifaces[i] = readUTF();
    }
    Class<?> cl = null;
    ClassNotFoundException resolveEx = null;
    setBlockMode(true);
    try {
      cl = resolveProxyClass(ifaces);
      if (cl == null) {
        resolveEx = new ClassNotFoundException("null class");
      } else if (!Proxy.isProxyClass(cl)) {
        throw new InvalidClassException("Not a proxy");
      } else {
        filterCheck(cl, -1);
      }
    } catch (ClassNotFoundException ex) {
      resolveEx = ex;
    }
    skipCustomData();
    desc.initProxy(cl, resolveEx, readClassDesc(false));
    return desc;
  }

  private ObjectStreamClass readNonProxyDesc(boolean unshared) throws IOException, ClassNotFoundException {
    rawByte();
    ObjectStreamClass desc = new ObjectStreamClass();
    assign(unshared ? kUnshared : desc);
    ObjectStreamClass readDesc;
    try {
      readDesc = readClassDescriptor();
    } catch (ClassNotFoundException ex) {
      throw (IOException) new InvalidClassException("failed to read class descriptor").initCause(ex);
    }
    Class<?> cl = null;
    ClassNotFoundException resolveEx = null;
    setBlockMode(true);
    try {
      cl = resolveClass(readDesc);
      if (cl == null) {
        resolveEx = new ClassNotFoundException("null class");
      } else {
        filterCheck(cl, -1);
      }
    } catch (ClassNotFoundException ex) {
      resolveEx = ex;
    }
    skipCustomData();
    desc.initNonProxy(readDesc, cl, resolveEx, readClassDesc(false));
    return desc;
  }

  String readTypeString() throws IOException {
    int tc = rawPeek();
    switch (tc) {
      case TC_NULL:
        rawByte();
        return null;
      case TC_REFERENCE:
        rawByte();
        return (String) readHandle();
      case TC_STRING:
      case TC_LONGSTRING:
        return readString(false);
      default:
        throw new StreamCorruptedException(String.format("invalid type code: %02X", tc));
    }
  }

  private String readString(boolean unshared) throws IOException {
    int tc = rawByte();
    String str;
    switch (tc) {
      case TC_STRING:
        str = readUTFBody(rawShort());
        break;
      case TC_LONGSTRING: {
        long len = ((long) rawInt() << 32) | (rawInt() & 0xFFFFFFFFL);
        if (len < 0 || len > Integer.MAX_VALUE) {
          throw new StreamCorruptedException("illegal string length: " + len);
        }
        str = readUTFBody(len);
        break;
      }
      default:
        throw new StreamCorruptedException(String.format("invalid type code: %02X", tc));
    }
    assign(unshared ? kUnshared : str);
    return str;
  }

  private Object readArray(boolean unshared) throws IOException, ClassNotFoundException {
    rawByte();
    ObjectStreamClass desc = readClassDesc(false);
    int len = rawInt();
    if (len < 0) {
      throw new StreamCorruptedException("Array length is negative: " + len);
    }
    Class<?> cl = desc.forClass();
    if (cl == null) {
      throw new ClassNotFoundException(desc.getName());
    }
    filterCheck(cl, len);
    Class<?> ccl = cl.getComponentType();
    Object array = Array.newInstance(ccl, len);
    int h = assign(unshared ? kUnshared : array);
    if (ccl.isPrimitive()) {
      if (ccl == Integer.TYPE) {
        int[] ia = (int[]) array;
        for (int i = 0; i < len; i++) {
          ia[i] = rawInt();
        }
      } else if (ccl == Byte.TYPE) {
        byte[] ba = (byte[]) array;
        int off = 0;
        while (off < len) {
          int n = rawRead(ba, off, len - off);
          if (n < 0) {
            throw new EOFException();
          }
          off += n;
        }
      } else if (ccl == Long.TYPE) {
        long[] la = (long[]) array;
        for (int i = 0; i < len; i++) {
          la[i] = ((long) rawInt() << 32) | (rawInt() & 0xFFFFFFFFL);
        }
      } else if (ccl == Float.TYPE) {
        float[] fa = (float[]) array;
        for (int i = 0; i < len; i++) {
          fa[i] = Float.intBitsToFloat(rawInt());
        }
      } else if (ccl == Double.TYPE) {
        double[] da = (double[]) array;
        for (int i = 0; i < len; i++) {
          da[i] = Double.longBitsToDouble(((long) rawInt() << 32) | (rawInt() & 0xFFFFFFFFL));
        }
      } else if (ccl == Short.TYPE) {
        short[] sa = (short[]) array;
        for (int i = 0; i < len; i++) {
          sa[i] = (short) rawShort();
        }
      } else if (ccl == Character.TYPE) {
        char[] ca = (char[]) array;
        for (int i = 0; i < len; i++) {
          ca[i] = (char) rawShort();
        }
      } else if (ccl == Boolean.TYPE) {
        boolean[] za = (boolean[]) array;
        for (int i = 0; i < len; i++) {
          int b = rawByte();
          if (b < 0) {
            throw new EOFException();
          }
          za[i] = b != 0;
        }
      } else {
        throw new InternalError();
      }
    } else {
      Object[] oa = (Object[]) array;
      for (int i = 0; i < len; i++) {
        Object elem = readObject0(false);
        try {
          oa[i] = elem;
        } catch (ArrayStoreException e) {
          throw (IOException) new InvalidClassException("array element type mismatch").initCause(e);
        }
      }
    }
    return handles.get(h) == kUnshared ? array : handles.get(h);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object readEnum(boolean unshared) throws IOException, ClassNotFoundException {
    rawByte();
    ObjectStreamClass desc = readClassDesc(false);
    if (!desc.isEnum()) {
      throw new InvalidClassException("non-enum class: " + desc);
    }
    int h = assign(unshared ? kUnshared : null);
    String name = readString(false);
    Class<?> cl = desc.forClass();
    if (cl == null) {
      throw new ClassNotFoundException(desc.getName());
    }
    Enum<?> result;
    try {
      result = Enum.valueOf((Class) cl, name);
    } catch (IllegalArgumentException ex) {
      throw (IOException) new InvalidObjectException("enum constant " + name + " does not exist in " + cl)
          .initCause(ex);
    }
    setHandle(h, result);
    return result;
  }

  private Object readOrdinaryObject(boolean unshared) throws IOException, ClassNotFoundException {
    rawByte();
    ObjectStreamClass desc = readClassDesc(false);
    desc.checkDeserialize();
    Class<?> cl = desc.forClass();
    if (cl == String.class || cl == Class.class || cl == ObjectStreamClass.class) {
      throw new InvalidClassException("invalid class descriptor");
    }
    if (cl == null) {
      throw new ClassNotFoundException(desc.getName());
    }
    if (desc.isRecord()) {
      return readRecord(desc, unshared);
    }
    Object obj;
    try {
      obj = desc.isInstantiable() ? desc.allocate() : null;
    } catch (Exception ex) {
      throw (IOException) new InvalidClassException(desc.forClass().getName(), "unable to create instance")
          .initCause(ex);
    }
    int h = assign(unshared ? kUnshared : obj);
    if (desc.isExternalizable()) {
      readExternalData((Externalizable) obj, desc);
    } else {
      readSerialData(obj, desc);
    }
    if (obj != null && desc.hasReadResolveMethod()) {
      Object rep = desc.invokeReadResolve(obj);
      if (unshared && rep != null && rep.getClass().isArray()) {
        rep = cloneArray(rep);
      }
      if (rep != obj) {
        if (rep != null) {
          if (rep.getClass().isArray()) {
            filterCheck(rep.getClass(), Array.getLength(rep));
          } else {
            filterCheck(rep.getClass(), -1);
          }
        }
        obj = rep;
        setHandle(h, obj);
      }
    }
    return obj;
  }

  private static Object cloneArray(Object a) {
    int n = Array.getLength(a);
    Object c = Array.newInstance(a.getClass().getComponentType(), n);
    System.arraycopy(a, 0, c, 0, n);
    return c;
  }

  private Object readRecord(ObjectStreamClass desc, boolean unshared) throws IOException, ClassNotFoundException {
    int h = assign(unshared ? kUnshared : null);
    ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
    Object result = null;
    for (ObjectStreamClass.ClassDataSlot slot : slots) {
      if (!slot.hasData) {
        continue;
      }
      ObjectStreamClass sd = slot.desc;
      boolean old = setBlockMode(false);
      FieldValues fv = new FieldValues(sd);
      fv.read();
      setBlockMode(old);
      if (sd.forClass() == desc.forClass()) {
        java.lang.reflect.RecordComponent[] comps = desc.forClass().getRecordComponents();
        Object[] args = new Object[comps.length];
        for (int i = 0; i < comps.length; i++) {
          args[i] = fv.valueFor(comps[i].getName(), comps[i].getType());
        }
        try {
          result = desc.newRecord(args);
        } catch (java.lang.reflect.InvocationTargetException e) {
          Throwable t = e.getCause();
          if (t instanceof RuntimeException || t instanceof Error) {
            throw new InvalidObjectException(t.getMessage() == null ? t.toString() : t.getMessage());
          }
          throw (IOException) new InvalidObjectException("ReflectiveOperationException during deserialization")
              .initCause(e);
        } catch (Exception e) {
          throw (IOException) new InvalidObjectException("ReflectiveOperationException during deserialization")
              .initCause(e);
        }
      }
    }
    setHandle(h, result);
    if (result != null && desc.hasReadResolveMethod()) {
      Object rep = desc.invokeReadResolve(result);
      if (rep != result) {
        result = rep;
        setHandle(h, result);
      }
    }
    return result;
  }

  private void readExternalData(Externalizable obj, ObjectStreamClass desc) throws IOException,
      ClassNotFoundException {
    Object oldObj = curObj;
    ObjectStreamClass oldDesc = curDesc;
    curObj = null;
    curDesc = null;
    boolean blocked = desc.hasBlockExternalData();
    setBlockMode(blocked);
    try {
      if (obj != null) {
        obj.readExternal(this);
      }
    } finally {
      curObj = oldObj;
      curDesc = oldDesc;
    }
    if (blocked) {
      skipCustomData();
    }
    setBlockMode(false);
  }

  private void skipCustomData() throws IOException, ClassNotFoundException {
    for (;;) {
      if (blockMode) {
        skipBlockData();
        setBlockMode(false);
      }
      int tc = rawPeek();
      switch (tc) {
        case TC_BLOCKDATA:
        case TC_BLOCKDATALONG:
          setBlockMode(true);
          break;
        case TC_ENDBLOCKDATA:
          rawByte();
          return;
        case -1:
          throw new EOFException();
        default:
          readObject0(false);
          break;
      }
    }
  }

  private void readSerialData(Object obj, ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
    for (int i = 0; i < slots.length; i++) {
      ObjectStreamClass slotDesc = slots[i].desc;
      if (slots[i].hasData) {
        if (obj != null && slotDesc.hasReadObjectMethod()) {
          Object oldObj = curObj;
          ObjectStreamClass oldDesc = curDesc;
          boolean oldDefault = defaultDataEnd;
          curObj = obj;
          curDesc = slotDesc;
          defaultDataEnd = false;
          setBlockMode(true);
          try {
            slotDesc.invokeReadObject(obj, this);
          } finally {
            curObj = oldObj;
            curDesc = oldDesc;
            defaultDataEnd = oldDefault;
          }
        } else {
          setBlockMode(false);
          defaultReadFields(obj != null && slotDesc.forClass() != null ? obj : null, slotDesc);
        }
        if (slotDesc.hasWriteObjectData()) {
          skipCustomData();
        } else {
          setBlockMode(false);
        }
      } else if (obj != null && slotDesc.hasReadObjectNoDataMethod()) {
        slotDesc.invokeReadObjectNoData(obj);
      }
    }
  }

  private void defaultReadFields(Object obj, ObjectStreamClass desc) throws IOException, ClassNotFoundException {
    Class<?> cl = desc.forClass();
    if (cl != null && obj != null && !cl.isInstance(obj)) {
      throw new ClassCastException();
    }
    byte[] prim = new byte[desc.getPrimDataSize()];
    int got = 0;
    while (got < prim.length) {
      int n = rawRead(prim, got, prim.length - got);
      if (n < 0) {
        throw new EOFException();
      }
      got += n;
    }
    ObjectStreamField[] fields = desc.getFields(false);
    int numPrim = fields.length - desc.getNumObjFields();
    if (obj != null) {
      for (int i = 0; i < numPrim; i++) {
        setPrimitive(obj, fields[i], prim);
      }
    }
    for (int i = numPrim; i < fields.length; i++) {
      Object value = readObject0(fields[i].isUnshared());
      Field f = fields[i].getField();
      if (obj != null && f != null) {
        try {
          f.set(obj, value);
        } catch (IllegalArgumentException e) {
          throw (IOException) new ClassCastException(
              "cannot assign instance of " + (value == null ? "null" : value.getClass().getName()) + " to field "
              + f.getDeclaringClass().getName() + "." + f.getName() + " of type " + f.getType().getName()
              + " in instance of " + obj.getClass().getName()).initCause(e);
        } catch (IllegalAccessException e) {
          throw new InternalError(e);
        }
      }
    }
  }

  private static void setPrimitive(Object obj, ObjectStreamField sf, byte[] buf) {
    Field f = sf.getField();
    if (f == null) {
      return;
    }
    int off = sf.getOffset();
    try {
      switch (sf.getTypeCode()) {
        case 'Z': f.setBoolean(obj, buf[off] != 0); break;
        case 'B': f.setByte(obj, buf[off]); break;
        case 'C': f.setChar(obj, (char) getShort(buf, off)); break;
        case 'S': f.setShort(obj, getShort(buf, off)); break;
        case 'I': f.setInt(obj, getInt(buf, off)); break;
        case 'F': f.setFloat(obj, Float.intBitsToFloat(getInt(buf, off))); break;
        case 'J': f.setLong(obj, getLong(buf, off)); break;
        case 'D': f.setDouble(obj, Double.longBitsToDouble(getLong(buf, off))); break;
        default: throw new InternalError();
      }
    } catch (IllegalAccessException e) {
      throw new InternalError(e);
    }
  }

  static native Object makeInstance(VMClass c);

  public abstract static class GetField {
    public GetField() { }

    public abstract ObjectStreamClass getObjectStreamClass();

    public abstract boolean defaulted(String name) throws IOException;

    public abstract boolean get(String name, boolean val) throws IOException;

    public abstract byte get(String name, byte val) throws IOException;

    public abstract char get(String name, char val) throws IOException;

    public abstract short get(String name, short val) throws IOException;

    public abstract int get(String name, int val) throws IOException;

    public abstract long get(String name, long val) throws IOException;

    public abstract float get(String name, float val) throws IOException;

    public abstract double get(String name, double val) throws IOException;

    public abstract Object get(String name, Object val) throws IOException;
  }

  private class FieldValues {
    final ObjectStreamClass desc;
    byte[] primVals;
    Object[] objVals;

    FieldValues(ObjectStreamClass desc) {
      this.desc = desc;
      primVals = new byte[desc.getPrimDataSize()];
      objVals = new Object[desc.getNumObjFields()];
    }

    void read() throws IOException, ClassNotFoundException {
      int got = 0;
      while (got < primVals.length) {
        int n = rawRead(primVals, got, primVals.length - got);
        if (n < 0) {
          throw new EOFException();
        }
        got += n;
      }
      ObjectStreamField[] fields = desc.getFields(false);
      int numPrim = fields.length - objVals.length;
      for (int i = 0; i < objVals.length; i++) {
        objVals[i] = readObject0(fields[numPrim + i].isUnshared());
      }
    }

    Object valueFor(String name, Class<?> type) {
      ObjectStreamField f = null;
      for (ObjectStreamField c : desc.getFields(false)) {
        if (c.getName().equals(name)) {
          f = c;
          break;
        }
      }
      if (f == null) {
        return defaultFor(type);
      }
      int off = f.getOffset();
      switch (f.getTypeCode()) {
        case 'Z': return Boolean.valueOf(primVals[off] != 0);
        case 'B': return Byte.valueOf(primVals[off]);
        case 'C': return Character.valueOf((char) getShort(primVals, off));
        case 'S': return Short.valueOf(getShort(primVals, off));
        case 'I': return Integer.valueOf(getInt(primVals, off));
        case 'F': return Float.valueOf(Float.intBitsToFloat(getInt(primVals, off)));
        case 'J': return Long.valueOf(getLong(primVals, off));
        case 'D': return Double.valueOf(Double.longBitsToDouble(getLong(primVals, off)));
        default: return objVals[off];
      }
    }
  }

  private static Object defaultFor(Class<?> type) {
    if (!type.isPrimitive()) {
      return null;
    }
    if (type == Boolean.TYPE) {
      return Boolean.FALSE;
    } else if (type == Byte.TYPE) {
      return Byte.valueOf((byte) 0);
    } else if (type == Character.TYPE) {
      return Character.valueOf((char) 0);
    } else if (type == Short.TYPE) {
      return Short.valueOf((short) 0);
    } else if (type == Integer.TYPE) {
      return Integer.valueOf(0);
    } else if (type == Long.TYPE) {
      return Long.valueOf(0L);
    } else if (type == Float.TYPE) {
      return Float.valueOf(0f);
    }
    return Double.valueOf(0d);
  }

  private final class GetFieldImpl extends GetField {
    private final FieldValues values;
    private final ObjectStreamClass desc;

    GetFieldImpl(ObjectStreamClass desc) {
      this.desc = desc;
      this.values = new FieldValues(desc);
    }

    void readFields() throws IOException, ClassNotFoundException {
      values.read();
    }

    public ObjectStreamClass getObjectStreamClass() {
      return desc.getLocalDesc();
    }

    public boolean defaulted(String name) {
      return getFieldOffset(name, null) < 0;
    }

    public boolean get(String name, boolean val) {
      int off = getFieldOffset(name, Boolean.TYPE);
      return off >= 0 ? values.primVals[off] != 0 : val;
    }

    public byte get(String name, byte val) {
      int off = getFieldOffset(name, Byte.TYPE);
      return off >= 0 ? values.primVals[off] : val;
    }

    public char get(String name, char val) {
      int off = getFieldOffset(name, Character.TYPE);
      return off >= 0 ? (char) getShort(values.primVals, off) : val;
    }

    public short get(String name, short val) {
      int off = getFieldOffset(name, Short.TYPE);
      return off >= 0 ? getShort(values.primVals, off) : val;
    }

    public int get(String name, int val) {
      int off = getFieldOffset(name, Integer.TYPE);
      return off >= 0 ? getInt(values.primVals, off) : val;
    }

    public float get(String name, float val) {
      int off = getFieldOffset(name, Float.TYPE);
      return off >= 0 ? Float.intBitsToFloat(getInt(values.primVals, off)) : val;
    }

    public long get(String name, long val) {
      int off = getFieldOffset(name, Long.TYPE);
      return off >= 0 ? getLong(values.primVals, off) : val;
    }

    public double get(String name, double val) {
      int off = getFieldOffset(name, Double.TYPE);
      return off >= 0 ? Double.longBitsToDouble(getLong(values.primVals, off)) : val;
    }

    public Object get(String name, Object val) throws IOException {
      int off = getFieldOffset(name, Object.class);
      return off >= 0 ? values.objVals[off] : val;
    }

    private int getFieldOffset(String name, Class<?> type) {
      ObjectStreamField field = desc.getField(name, type);
      if (field != null) {
        return field.getOffset();
      }
      ObjectStreamClass local = desc.getLocalDesc();
      if (local != null && local.getField(name, type) != null) {
        return -1;
      }
      throw new IllegalArgumentException("no such field " + name + " with type " + type);
    }
  }
}
