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
import java.util.IdentityHashMap;

public class ObjectOutputStream extends OutputStream implements ObjectOutput, ObjectStreamConstants {
  private static final int kMaxBlock = 1024;

  private final OutputStream out;
  private final byte[] block = new byte[kMaxBlock];
  private int blockPos;
  private boolean blockMode = true;
  private final IdentityHashMap<Object, Integer> handles = new IdentityHashMap<Object, Integer>();
  private int nextHandle;
  private final boolean enableOverride;
  private boolean enableReplace;
  private int protocol = PROTOCOL_VERSION_2;
  private int depth;
  private Object curObj;
  private ObjectStreamClass curDesc;
  private PutFieldImpl curPut;
  private final byte[] scratch = new byte[8];

  public ObjectOutputStream(OutputStream out) throws IOException {
    if (out == null) {
      throw new NullPointerException();
    }
    this.out = out;
    this.enableOverride = false;
    writeStreamHeader();
    setBlockMode(true);
  }

  protected ObjectOutputStream() throws IOException, SecurityException {
    this.out = null;
    this.enableOverride = true;
  }

  public void useProtocolVersion(int version) throws IOException {
    if (nextHandle != 0) {
      throw new IllegalStateException("stream non-empty");
    }
    switch (version) {
      case PROTOCOL_VERSION_1:
      case PROTOCOL_VERSION_2:
        protocol = version;
        break;
      default:
        throw new IllegalArgumentException("unknown version: " + version);
    }
  }

  int getProtocolVersion() {
    return protocol;
  }

  public final void writeObject(Object obj) throws IOException {
    if (enableOverride) {
      writeObjectOverride(obj);
      return;
    }
    try {
      writeObject0(obj, false);
    } catch (IOException ex) {
      if (depth == 0) {
        writeFatalException(ex);
      }
      throw ex;
    }
  }

  protected void writeObjectOverride(Object obj) throws IOException { }

  public void writeUnshared(Object obj) throws IOException {
    try {
      writeObject0(obj, true);
    } catch (IOException ex) {
      if (depth == 0) {
        writeFatalException(ex);
      }
      throw ex;
    }
  }

  public void defaultWriteObject() throws IOException {
    Object curObj = this.curObj;
    ObjectStreamClass curDesc = this.curDesc;
    if (curObj == null || curDesc == null) {
      throw new NotActiveException("not in call to writeObject");
    }
    setBlockMode(false);
    try {
      defaultWriteFields(curObj, curDesc);
    } finally {
      setBlockMode(true);
    }
  }

  public ObjectOutputStream.PutField putFields() throws IOException {
    if (curPut == null) {
      Object curObj = this.curObj;
      ObjectStreamClass curDesc = this.curDesc;
      if (curObj == null || curDesc == null) {
        throw new NotActiveException("not in call to writeObject");
      }
      curPut = new PutFieldImpl(curDesc);
    }
    return curPut;
  }

  public void writeFields() throws IOException {
    if (curPut == null) {
      throw new NotActiveException("no current PutField object");
    }
    setBlockMode(false);
    try {
      curPut.writeFields();
    } finally {
      setBlockMode(true);
    }
  }

  public void reset() throws IOException {
    if (depth != 0) {
      throw new IOException("stream active");
    }
    setBlockMode(false);
    writeByteRaw(TC_RESET);
    clear();
    setBlockMode(true);
  }

  protected void annotateClass(Class<?> cl) throws IOException { }

  protected void annotateProxyClass(Class<?> cl) throws IOException { }

  protected Object replaceObject(Object obj) throws IOException {
    return obj;
  }

  protected boolean enableReplaceObject(boolean enable) throws SecurityException {
    boolean old = enableReplace;
    enableReplace = enable;
    return old;
  }

  protected void writeStreamHeader() throws IOException {
    writeShortRaw(STREAM_MAGIC);
    writeShortRaw(STREAM_VERSION);
  }

  protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
    desc.writeNonProxy(this);
  }

  @Override
  public void write(int val) throws IOException {
    putByte(val);
  }

  @Override
  public void write(byte[] buf) throws IOException {
    write(buf, 0, buf.length);
  }

  @Override
  public void write(byte[] buf, int off, int len) throws IOException {
    if (buf == null) {
      throw new NullPointerException();
    }
    java.util.Objects.checkFromIndexSize(off, len, buf.length);
    putBytes(buf, off, len);
  }

  @Override
  public void flush() throws IOException {
    drain();
    out.flush();
  }

  protected void drain() throws IOException {
    if (blockPos > 0 && blockMode) {
      writeBlockHeader(blockPos);
      out.write(block, 0, blockPos);
      blockPos = 0;
    } else if (blockPos > 0) {
      out.write(block, 0, blockPos);
      blockPos = 0;
    }
  }

  @Override
  public void close() throws IOException {
    flush();
    out.close();
  }

  public void writeBoolean(boolean val) throws IOException {
    putByte(val ? 1 : 0);
  }

  public void writeByte(int val) throws IOException {
    putByte(val);
  }

  public void writeShort(int val) throws IOException {
    scratch[0] = (byte) (val >>> 8);
    scratch[1] = (byte) val;
    putBytes(scratch, 0, 2);
  }

  public void writeChar(int val) throws IOException {
    writeShort(val);
  }

  public void writeInt(int val) throws IOException {
    scratch[0] = (byte) (val >>> 24);
    scratch[1] = (byte) (val >>> 16);
    scratch[2] = (byte) (val >>> 8);
    scratch[3] = (byte) val;
    putBytes(scratch, 0, 4);
  }

  public void writeFloat(float val) throws IOException {
    writeInt(Float.floatToIntBits(val));
  }

  public void writeLong(long val) throws IOException {
    for (int i = 0; i < 8; i++) {
      scratch[i] = (byte) (val >>> (56 - 8 * i));
    }
    putBytes(scratch, 0, 8);
  }

  public void writeDouble(double val) throws IOException {
    writeLong(Double.doubleToLongBits(val));
  }

  public void writeBytes(String str) throws IOException {
    int len = str.length();
    byte[] buf = new byte[len];
    for (int i = 0; i < len; i++) {
      buf[i] = (byte) str.charAt(i);
    }
    putBytes(buf, 0, len);
  }

  public void writeChars(String str) throws IOException {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      writeShort(str.charAt(i));
    }
  }

  public void writeUTF(String str) throws IOException {
    long utflen = utfLength(str);
    if (utflen > 0xFFFF) {
      throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");
    }
    writeShort((int) utflen);
    writeUTFBody(str);
  }

  private static long utfLength(String s) {
    long utflen = 0;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c >= 0x0001 && c <= 0x007F) {
        utflen++;
      } else if (c > 0x07FF) {
        utflen += 3;
      } else {
        utflen += 2;
      }
    }
    return utflen;
  }

  private void writeUTFBody(String s) throws IOException {
    byte[] buf = new byte[512];
    int pos = 0;
    for (int i = 0; i < s.length(); i++) {
      if (pos > buf.length - 3) {
        putBytes(buf, 0, pos);
        pos = 0;
      }
      char c = s.charAt(i);
      if (c >= 0x0001 && c <= 0x007F) {
        buf[pos++] = (byte) c;
      } else if (c > 0x07FF) {
        buf[pos++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
        buf[pos++] = (byte) (0x80 | ((c >> 6) & 0x3F));
        buf[pos++] = (byte) (0x80 | (c & 0x3F));
      } else {
        buf[pos++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
        buf[pos++] = (byte) (0x80 | (c & 0x3F));
      }
    }
    putBytes(buf, 0, pos);
  }

  /* ---- block data layer ---- */

  private void putByte(int b) throws IOException {
    if (blockPos >= kMaxBlock) {
      drain();
    }
    block[blockPos++] = (byte) b;
  }

  private void putBytes(byte[] b, int off, int len) throws IOException {
    while (len > 0) {
      if (blockPos >= kMaxBlock) {
        drain();
      }
      int n = Math.min(len, kMaxBlock - blockPos);
      System.arraycopy(b, off, block, blockPos, n);
      blockPos += n;
      off += n;
      len -= n;
    }
  }

  private void writeBlockHeader(int len) throws IOException {
    if (len <= 0xFF) {
      out.write(TC_BLOCKDATA);
      out.write(len);
    } else {
      out.write(TC_BLOCKDATALONG);
      out.write(len >>> 24);
      out.write(len >>> 16);
      out.write(len >>> 8);
      out.write(len);
    }
  }

  private boolean setBlockMode(boolean mode) throws IOException {
    if (blockMode == mode) {
      return mode;
    }
    drain();
    blockMode = mode;
    return !mode;
  }

  private void writeByteRaw(int b) throws IOException {
    drain();
    out.write(b);
  }

  private void writeShortRaw(int v) throws IOException {
    out.write(v >>> 8);
    out.write(v);
  }

  private void writeIntRaw(int v) throws IOException {
    out.write(v >>> 24);
    out.write(v >>> 16);
    out.write(v >>> 8);
    out.write(v);
  }

  private void writeLongRaw(long v) throws IOException {
    writeIntRaw((int) (v >>> 32));
    writeIntRaw((int) v);
  }

  /* ---- object graph ---- */

  private void clear() {
    handles.clear();
    nextHandle = 0;
  }

  private void assignHandle(Object obj, boolean unshared) {
    if (!unshared) {
      handles.put(obj, Integer.valueOf(nextHandle));
    }
    nextHandle++;
  }

  private boolean writeHandle(Object obj) throws IOException {
    Integer h = handles.get(obj);
    if (h == null) {
      return false;
    }
    writeByteRaw(TC_REFERENCE);
    writeIntRaw(baseWireHandle + h.intValue());
    return true;
  }

  private void writeObject0(Object obj, boolean unshared) throws IOException {
    boolean oldMode = setBlockMode(false);
    depth++;
    try {
      Object orig = obj;
      Class<?> cl;
      ObjectStreamClass desc;
      for (;;) {
        if (obj == null) {
          writeByteRaw(TC_NULL);
          return;
        }
        if (!unshared && writeHandle(obj)) {
          return;
        }
        cl = obj.getClass();
        desc = ObjectStreamClass.lookup(cl, true);
        if (desc.hasWriteReplaceMethod()) {
          Object rep = desc.invokeWriteReplace(obj);
          if (rep != null && rep.getClass() == cl) {
            obj = rep;
            break;
          }
          obj = rep;
          if (rep == null) {
            continue;
          }
          continue;
        }
        break;
      }
      if (enableReplace) {
        Object rep = replaceObject(obj);
        if (rep != obj && rep != null) {
          cl = rep.getClass();
          desc = ObjectStreamClass.lookup(cl, true);
        }
        obj = rep;
      }
      if (obj != orig) {
        if (obj == null) {
          writeByteRaw(TC_NULL);
          return;
        }
        if (!unshared && writeHandle(obj)) {
          return;
        }
      }
      if (obj instanceof Class) {
        writeClass((Class<?>) obj, unshared);
      } else if (obj instanceof ObjectStreamClass) {
        writeClassDesc((ObjectStreamClass) obj, unshared);
      } else if (obj instanceof String) {
        writeString((String) obj, unshared);
      } else if (cl.isArray()) {
        writeArray(obj, desc, unshared);
      } else if (obj instanceof Enum) {
        writeEnum((Enum<?>) obj, desc, unshared);
      } else if (obj instanceof Serializable) {
        writeOrdinaryObject(obj, desc, unshared);
      } else {
        throw new NotSerializableException(cl.getName());
      }
    } finally {
      depth--;
      setBlockMode(oldMode);
    }
  }

  private void writeClass(Class<?> cl, boolean unshared) throws IOException {
    writeByteRaw(TC_CLASS);
    writeClassDesc(ObjectStreamClass.lookup(cl, true), false);
    assignHandle(cl, unshared);
  }

  private void writeClassDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
    if (desc == null) {
      writeByteRaw(TC_NULL);
    } else if (!unshared && writeHandle(desc)) {
      return;
    } else if (desc.isProxy()) {
      writeProxyDesc(desc, unshared);
    } else {
      writeNonProxyDesc(desc, unshared);
    }
  }

  private void writeProxyDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
    writeByteRaw(TC_PROXYCLASSDESC);
    assignHandle(desc, false);
    Class<?> cl = desc.forClass();
    Class<?>[] ifaces = cl.getInterfaces();
    writeIntRaw(ifaces.length);
    for (int i = 0; i < ifaces.length; i++) {
      writeUTFRaw(ifaces[i].getName());
    }
    setBlockMode(true);
    annotateProxyClass(cl);
    setBlockMode(false);
    writeByteRaw(TC_ENDBLOCKDATA);
    writeClassDesc(ObjectStreamClass.lookup(cl.getSuperclass(), true), false);
  }

  private void writeUTFRaw(String s) throws IOException {
    boolean old = setBlockMode(false);
    writeUTFDirect(s);
    setBlockMode(old);
  }

  private void writeUTFDirect(String s) throws IOException {
    writeUTF(s);
  }

  private void writeNonProxyDesc(ObjectStreamClass desc, boolean unshared) throws IOException {
    writeByteRaw(TC_CLASSDESC);
    assignHandle(desc, false);
    writeClassDescriptor(desc);
    Class<?> cl = desc.forClass();
    setBlockMode(true);
    annotateClass(cl);
    setBlockMode(false);
    writeByteRaw(TC_ENDBLOCKDATA);
    writeClassDesc(desc.getSuperDesc(), false);
  }

  void writeTypeString(String str) throws IOException {
    if (str == null) {
      writeByteRaw(TC_NULL);
    } else if (writeHandle(str)) {
      return;
    } else {
      writeString(str, false);
    }
  }

  private void writeString(String str, boolean unshared) throws IOException {
    assignHandle(str, unshared);
    long utflen = utfLength(str);
    if (utflen <= 0xFFFF) {
      writeByteRaw(TC_STRING);
      writeUTFNoBlock(str, utflen);
    } else {
      writeByteRaw(TC_LONGSTRING);
      drain();
      writeLongRaw(utflen);
      writeUTFBody(str);
    }
  }

  private void writeUTFNoBlock(String str, long utflen) throws IOException {
    drain();
    writeShortRaw((int) utflen);
    writeUTFBody(str);
  }

  private void writeArray(Object array, ObjectStreamClass desc, boolean unshared) throws IOException {
    writeByteRaw(TC_ARRAY);
    writeClassDesc(desc, false);
    assignHandle(array, unshared);
    Class<?> ccl = desc.forClass().getComponentType();
    int len = Array.getLength(array);
    drain();
    writeIntRaw(len);
    if (ccl.isPrimitive()) {
      if (ccl == Integer.TYPE) {
        int[] ia = (int[]) array;
        for (int i = 0; i < len; i++) {
          writeIntRaw(ia[i]);
        }
      } else if (ccl == Byte.TYPE) {
        byte[] ba = (byte[]) array;
        drain();
        out.write(ba, 0, len);
      } else if (ccl == Long.TYPE) {
        long[] la = (long[]) array;
        for (int i = 0; i < len; i++) {
          writeLongRaw(la[i]);
        }
      } else if (ccl == Float.TYPE) {
        float[] fa = (float[]) array;
        for (int i = 0; i < len; i++) {
          writeIntRaw(Float.floatToIntBits(fa[i]));
        }
      } else if (ccl == Double.TYPE) {
        double[] da = (double[]) array;
        for (int i = 0; i < len; i++) {
          writeLongRaw(Double.doubleToLongBits(da[i]));
        }
      } else if (ccl == Short.TYPE) {
        short[] sa = (short[]) array;
        for (int i = 0; i < len; i++) {
          writeShortRaw(sa[i]);
        }
      } else if (ccl == Character.TYPE) {
        char[] ca = (char[]) array;
        for (int i = 0; i < len; i++) {
          writeShortRaw(ca[i]);
        }
      } else if (ccl == Boolean.TYPE) {
        boolean[] za = (boolean[]) array;
        for (int i = 0; i < len; i++) {
          out.write(za[i] ? 1 : 0);
        }
      } else {
        throw new InternalError();
      }
    } else {
      Object[] objs = (Object[]) array;
      for (int i = 0; i < len; i++) {
        writeObject0(objs[i], false);
      }
    }
  }

  private void writeEnum(Enum<?> en, ObjectStreamClass desc, boolean unshared) throws IOException {
    writeByteRaw(TC_ENUM);
    ObjectStreamClass sdesc = desc.getSuperDesc();
    writeClassDesc((sdesc.forClass() == Enum.class) ? desc : sdesc, false);
    assignHandle(en, unshared);
    writeString(en.name(), false);
  }

  private void writeOrdinaryObject(Object obj, ObjectStreamClass desc, boolean unshared) throws IOException {
    desc.checkSerialize();
    writeByteRaw(TC_OBJECT);
    writeClassDesc(desc, false);
    assignHandle(obj, unshared);
    if (desc.isExternalizable() && !desc.isProxy()) {
      writeExternalData((Externalizable) obj);
    } else {
      writeSerialData(obj, desc);
    }
  }

  private void writeExternalData(Externalizable obj) throws IOException {
    Object oldObj = curObj;
    ObjectStreamClass oldDesc = curDesc;
    PutFieldImpl oldPut = curPut;
    curObj = null;
    curDesc = null;
    curPut = null;
    try {
      if (protocol == PROTOCOL_VERSION_1) {
        setBlockMode(false);
        obj.writeExternal(this);
      } else {
        setBlockMode(true);
        obj.writeExternal(this);
        setBlockMode(false);
        writeByteRaw(TC_ENDBLOCKDATA);
      }
    } finally {
      curObj = oldObj;
      curDesc = oldDesc;
      curPut = oldPut;
    }
  }

  private void writeSerialData(Object obj, ObjectStreamClass desc) throws IOException {
    ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
    for (int i = 0; i < slots.length; i++) {
      ObjectStreamClass slotDesc = slots[i].desc;
      if (slotDesc.hasWriteObjectMethod()) {
        Object oldObj = curObj;
        ObjectStreamClass oldDesc = curDesc;
        PutFieldImpl oldPut = curPut;
        curObj = obj;
        curDesc = slotDesc;
        curPut = null;
        boolean oldMode = setBlockMode(true);
        try {
          slotDesc.invokeWriteObject(obj, this);
        } finally {
          curObj = oldObj;
          curDesc = oldDesc;
          curPut = oldPut;
        }
        setBlockMode(false);
        writeByteRaw(TC_ENDBLOCKDATA);
      } else {
        defaultWriteFields(obj, slotDesc);
      }
    }
  }

  private void defaultWriteFields(Object obj, ObjectStreamClass desc) throws IOException {
    Class<?> cl = desc.forClass();
    if (cl != null && obj != null && !cl.isInstance(obj)) {
      throw new ClassCastException();
    }
    ObjectStreamField[] fields = desc.getFields(false);
    int numPrim = fields.length - desc.getNumObjFields();
    drain();
    byte[] primVals = new byte[desc.getPrimDataSize()];
    for (int i = 0; i < numPrim; i++) {
      getPrimitiveField(obj, fields[i], primVals);
    }
    out.write(primVals, 0, primVals.length);
    for (int i = numPrim; i < fields.length; i++) {
      Object value = getObjectField(obj, fields[i]);
      writeObject0(value, fields[i].isUnshared());
    }
  }

  private static void getPrimitiveField(Object obj, ObjectStreamField f, byte[] buf) throws IOException {
    int off = f.getOffset();
    Field fld = f.getField();
    try {
      switch (f.getTypeCode()) {
        case 'Z':
          buf[off] = (byte) (fld.getBoolean(obj) ? 1 : 0);
          break;
        case 'B':
          buf[off] = fld.getByte(obj);
          break;
        case 'C':
          putShort(buf, off, fld.getChar(obj));
          break;
        case 'S':
          putShort(buf, off, fld.getShort(obj));
          break;
        case 'I':
          putInt(buf, off, fld.getInt(obj));
          break;
        case 'F':
          putInt(buf, off, Float.floatToIntBits(fld.getFloat(obj)));
          break;
        case 'J':
          putLong(buf, off, fld.getLong(obj));
          break;
        case 'D':
          putLong(buf, off, Double.doubleToLongBits(fld.getDouble(obj)));
          break;
        default:
          throw new InternalError();
      }
    } catch (IllegalAccessException e) {
      throw new InternalError(e);
    }
  }

  private static Object getObjectField(Object obj, ObjectStreamField f) throws IOException {
    Field fld = f.getField();
    if (fld == null) {
      return null;
    }
    try {
      return fld.get(obj);
    } catch (IllegalAccessException e) {
      throw new InternalError(e);
    }
  }

  static void putShort(byte[] b, int off, int v) {
    b[off] = (byte) (v >>> 8);
    b[off + 1] = (byte) v;
  }

  static void putInt(byte[] b, int off, int v) {
    b[off] = (byte) (v >>> 24);
    b[off + 1] = (byte) (v >>> 16);
    b[off + 2] = (byte) (v >>> 8);
    b[off + 3] = (byte) v;
  }

  static void putLong(byte[] b, int off, long v) {
    putInt(b, off, (int) (v >>> 32));
    putInt(b, off + 4, (int) v);
  }

  private void writeFatalException(IOException ex) throws IOException {
    clear();
    boolean oldMode = setBlockMode(false);
    try {
      writeByteRaw(TC_EXCEPTION);
      writeObject0(ex, false);
      clear();
    } finally {
      setBlockMode(oldMode);
    }
  }

  public abstract static class PutField {
    public PutField() { }

    public abstract void put(String name, boolean val);

    public abstract void put(String name, byte val);

    public abstract void put(String name, char val);

    public abstract void put(String name, short val);

    public abstract void put(String name, int val);

    public abstract void put(String name, long val);

    public abstract void put(String name, float val);

    public abstract void put(String name, double val);

    public abstract void put(String name, Object val);

    @Deprecated
    public abstract void write(ObjectOutput out) throws IOException;
  }

  private final class PutFieldImpl extends PutField {
    private final ObjectStreamClass desc;
    private final byte[] primVals;
    private final Object[] objVals;

    PutFieldImpl(ObjectStreamClass desc) {
      this.desc = desc;
      primVals = new byte[desc.getPrimDataSize()];
      objVals = new Object[desc.getNumObjFields()];
    }

    public void put(String name, boolean val) {
      primVals[getFieldOffset(name, Boolean.TYPE)] = (byte) (val ? 1 : 0);
    }

    public void put(String name, byte val) {
      primVals[getFieldOffset(name, Byte.TYPE)] = val;
    }

    public void put(String name, char val) {
      putShort(primVals, getFieldOffset(name, Character.TYPE), val);
    }

    public void put(String name, short val) {
      putShort(primVals, getFieldOffset(name, Short.TYPE), val);
    }

    public void put(String name, int val) {
      putInt(primVals, getFieldOffset(name, Integer.TYPE), val);
    }

    public void put(String name, float val) {
      putInt(primVals, getFieldOffset(name, Float.TYPE), Float.floatToIntBits(val));
    }

    public void put(String name, long val) {
      putLong(primVals, getFieldOffset(name, Long.TYPE), val);
    }

    public void put(String name, double val) {
      putLong(primVals, getFieldOffset(name, Double.TYPE), Double.doubleToLongBits(val));
    }

    public void put(String name, Object val) {
      objVals[getFieldOffset(name, Object.class)] = val;
    }

    public void write(ObjectOutput out) throws IOException {
      if (ObjectOutputStream.this != out) {
        throw new IllegalArgumentException("wrong stream");
      }
      out.write(primVals, 0, primVals.length);
      ObjectStreamField[] fields = desc.getFields(false);
      int numPrim = fields.length - objVals.length;
      for (int i = 0; i < objVals.length; i++) {
        if (fields[numPrim + i].isUnshared()) {
          throw new IOException("cannot write unshared object");
        }
        out.writeObject(objVals[i]);
      }
    }

    void writeFields() throws IOException {
      drain();
      ObjectOutputStream.this.out.write(primVals, 0, primVals.length);
      ObjectStreamField[] fields = desc.getFields(false);
      int numPrim = fields.length - objVals.length;
      for (int i = 0; i < objVals.length; i++) {
        writeObject0(objVals[i], fields[numPrim + i].isUnshared());
      }
    }

    private int getFieldOffset(String name, Class<?> type) {
      ObjectStreamField field = desc.getField(name, type);
      if (field == null) {
        throw new IllegalArgumentException("no such field " + name + " with type " + type);
      }
      return field.getOffset();
    }
  }
}
