/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import shim.Classes;
import shim.SystemClassLoader;
import shim.VMClass;
import shim.VMField;
import shim.VMMethod;

public class ObjectStreamClass implements Serializable {
  public static final ObjectStreamField[] NO_FIELDS = new ObjectStreamField[0];
  private static final long serialVersionUID = -6120832682080437368L;
  private static final ObjectStreamField[] serialPersistentFields = NO_FIELDS;

  private static final Map<Class<?>, ObjectStreamClass> cache = new HashMap<Class<?>, ObjectStreamClass>();

  private Class<?> cl;
  private String name;
  private volatile Long suid;
  private boolean isProxy;
  private boolean isEnum;
  private boolean isRecord;
  private boolean serializable;
  private boolean externalizable;
  private boolean hasWriteObjectData;
  private boolean hasBlockExternalData = true;
  private ObjectStreamField[] fields;
  private int primDataSize;
  private int numObjFields;
  private ObjectStreamClass superDesc;
  private ObjectStreamClass localDesc;
  private InvalidClassException deserializeEx;
  private InvalidClassException serializeEx;
  private Method writeObjectMethod;
  private Method readObjectMethod;
  private Method readObjectNoDataMethod;
  private Method writeReplaceMethod;
  private Method readResolveMethod;
  private Constructor<?> cons;
  private Class<?> firstNonSerializable;

  ObjectStreamClass() { }

  public static ObjectStreamClass lookup(Class<?> cl) {
    return lookup(cl, false);
  }

  public static ObjectStreamClass lookupAny(Class<?> cl) {
    return lookup(cl, true);
  }

  public String getName() {
    return name;
  }

  public long getSerialVersionUID() {
    if (suid == null) {
      if (isRecord) {
        return 0L;
      }
      suid = Long.valueOf(computeDefaultSUID(cl));
    }
    return suid.longValue();
  }

  public Class<?> forClass() {
    return cl;
  }

  public ObjectStreamField[] getFields() {
    return getFields(true);
  }

  public ObjectStreamField getField(String name) {
    return getField(name, null);
  }

  @Override
  public String toString() {
    return name + ": static final long serialVersionUID = " + getSerialVersionUID() + "L;";
  }

  static ObjectStreamClass lookup(Class<?> cl, boolean all) {
    if (!(all || Serializable.class.isAssignableFrom(cl))) {
      return null;
    }
    synchronized (cache) {
      ObjectStreamClass desc = cache.get(cl);
      if (desc == null) {
        desc = new ObjectStreamClass();
        cache.put(cl, desc);
        desc.initLocal(cl);
      }
      return desc;
    }
  }

  private void initLocal(Class<?> cl) {
    this.cl = cl;
    this.name = cl.getName();
    this.isProxy = java.lang.reflect.Proxy.isProxyClass(cl);
    this.isEnum = Enum.class.isAssignableFrom(cl);
    this.serializable = Serializable.class.isAssignableFrom(cl);
    this.externalizable = Externalizable.class.isAssignableFrom(cl);
    this.isRecord = cl.isRecord();
    Class<?> superCl = cl.getSuperclass();
    this.superDesc = (superCl != null) ? lookup(superCl, false) : null;
    this.localDesc = this;
    if (serializable) {
      if (isEnum) {
        suid = Long.valueOf(0L);
        fields = NO_FIELDS;
        return;
      }
      if (cl.isArray()) {
        fields = NO_FIELDS;
        return;
      }
      suid = getDeclaredSUID(cl);
      try {
        fields = getSerialFields(cl);
        computeFieldOffsets();
      } catch (InvalidClassException e) {
        serializeEx = deserializeEx = e;
        fields = NO_FIELDS;
      }
      if (externalizable) {
        try {
          cons = getExternalizableConstructor(cl);
        } catch (Exception e) {
          deserializeEx = new InvalidClassException(cl.getName(), "no valid constructor");
        }
      } else if (isRecord) {
        deserializeEx = null;
      } else {
        firstNonSerializable = firstNonSerializable(cl);
        if (firstNonSerializable == null) {
          deserializeEx = new InvalidClassException(cl.getName(), "no valid constructor");
        } else {
          cons = getSerializableConstructor(firstNonSerializable);
          if (cons == null && deserializeEx == null) {
            deserializeEx = new InvalidClassException(cl.getName(), "no valid constructor");
          }
        }
        writeObjectMethod = getPrivateMethod(cl, "writeObject", new Class<?>[] {ObjectOutputStream.class},
            Void.TYPE);
        readObjectMethod = getPrivateMethod(cl, "readObject", new Class<?>[] {ObjectInputStream.class}, Void.TYPE);
        readObjectNoDataMethod = getPrivateMethod(cl, "readObjectNoData", null, Void.TYPE);
        hasWriteObjectData = (writeObjectMethod != null);
      }
      writeReplaceMethod = getInheritableMethod(cl, "writeReplace", null, Object.class);
      readResolveMethod = getInheritableMethod(cl, "readResolve", null, Object.class);
    } else {
      suid = Long.valueOf(0L);
      fields = NO_FIELDS;
    }
  }

  private static Class<?> firstNonSerializable(Class<?> cl) {
    for (Class<?> c = cl.getSuperclass(); c != null; c = c.getSuperclass()) {
      if (!Serializable.class.isAssignableFrom(c)) {
        return c;
      }
    }
    return null;
  }

  private static Constructor<?> getSerializableConstructor(Class<?> base) {
    try {
      Constructor<?> c = base.getDeclaredConstructor(new Class<?>[0]);
      int mods = c.getModifiers();
      if ((mods & Modifier.PRIVATE) != 0) {
        return null;
      }
      c.setAccessible(true);
      return c;
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static Constructor<?> getExternalizableConstructor(Class<?> cl) throws Exception {
    Constructor<?> c = cl.getDeclaredConstructor(new Class<?>[0]);
    c.setAccessible(true);
    if ((c.getModifiers() & Modifier.PUBLIC) == 0) {
      return null;
    }
    return c;
  }

  private static Long getDeclaredSUID(Class<?> cl) {
    try {
      Field f = cl.getDeclaredField("serialVersionUID");
      int mask = Modifier.STATIC | Modifier.FINAL;
      if ((f.getModifiers() & mask) == mask) {
        f.setAccessible(true);
        return Long.valueOf(f.getLong(null));
      }
    } catch (Exception ex) {
    }
    return null;
  }

  private static ObjectStreamField[] getSerialFields(Class<?> cl) throws InvalidClassException {
    ObjectStreamField[] fields;
    if (Serializable.class.isAssignableFrom(cl) && !Externalizable.class.isAssignableFrom(cl)
        && !Enum.class.isAssignableFrom(cl) && !cl.isInterface()) {
      if (cl.isRecord()) {
        fields = getRecordFields(cl);
      } else if ((fields = getDeclaredSerialFields(cl)) == null) {
        fields = getDefaultSerialFields(cl);
      }
      Arrays.sort(fields);
    } else {
      fields = NO_FIELDS;
    }
    return fields;
  }

  private static ObjectStreamField[] getRecordFields(Class<?> cl) {
    RecordComponent[] comps = cl.getRecordComponents();
    ObjectStreamField[] result = new ObjectStreamField[comps.length];
    for (int i = 0; i < comps.length; i++) {
      try {
        Field f = cl.getDeclaredField(comps[i].getName());
        f.setAccessible(true);
        result[i] = new ObjectStreamField(f, false, true);
      } catch (NoSuchFieldException e) {
        throw new InternalError(e);
      }
    }
    return result;
  }

  private static ObjectStreamField[] getDeclaredSerialFields(Class<?> cl) throws InvalidClassException {
    ObjectStreamField[] serialPersistentFields = null;
    try {
      Field f = cl.getDeclaredField("serialPersistentFields");
      int mask = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
      if ((f.getModifiers() & mask) == mask) {
        f.setAccessible(true);
        serialPersistentFields = (ObjectStreamField[]) f.get(null);
      }
    } catch (Exception ex) {
    }
    if (serialPersistentFields == null) {
      return null;
    } else if (serialPersistentFields.length == 0) {
      return NO_FIELDS;
    }
    ObjectStreamField[] boundFields = new ObjectStreamField[serialPersistentFields.length];
    HashSet<String> fieldNames = new HashSet<String>(serialPersistentFields.length);
    for (int i = 0; i < serialPersistentFields.length; i++) {
      ObjectStreamField spf = serialPersistentFields[i];
      String fname = spf.getName();
      if (fieldNames.contains(fname)) {
        throw new InvalidClassException("multiple serializable fields named " + fname);
      }
      fieldNames.add(fname);
      try {
        Field f = cl.getDeclaredField(fname);
        if ((f.getType() == spf.getType()) && ((f.getModifiers() & Modifier.STATIC) == 0)) {
          f.setAccessible(true);
          boundFields[i] = new ObjectStreamField(f, spf.isUnshared(), true);
        }
      } catch (NoSuchFieldException ex) {
      }
      if (boundFields[i] == null) {
        boundFields[i] = new ObjectStreamField(fname, spf.getType(), spf.isUnshared());
      }
    }
    return boundFields;
  }

  private static ObjectStreamField[] getDefaultSerialFields(Class<?> cl) {
    Field[] clFields = cl.getDeclaredFields();
    ArrayList<ObjectStreamField> list = new ArrayList<ObjectStreamField>();
    int mask = Modifier.STATIC | Modifier.TRANSIENT;
    for (int i = 0; i < clFields.length; i++) {
      if ((clFields[i].getModifiers() & mask) == 0) {
        clFields[i].setAccessible(true);
        list.add(new ObjectStreamField(clFields[i], false, true));
      }
    }
    return list.toArray(new ObjectStreamField[list.size()]);
  }

  private void computeFieldOffsets() throws InvalidClassException {
    primDataSize = 0;
    numObjFields = 0;
    int firstObjIndex = -1;
    for (int i = 0; i < fields.length; i++) {
      ObjectStreamField f = fields[i];
      switch (f.getTypeCode()) {
        case 'Z':
        case 'B':
          f.setOffset(primDataSize++);
          break;
        case 'C':
        case 'S':
          f.setOffset(primDataSize);
          primDataSize += 2;
          break;
        case 'I':
        case 'F':
          f.setOffset(primDataSize);
          primDataSize += 4;
          break;
        case 'J':
        case 'D':
          f.setOffset(primDataSize);
          primDataSize += 8;
          break;
        case '[':
        case 'L':
          f.setOffset(numObjFields++);
          if (firstObjIndex == -1) {
            firstObjIndex = i;
          }
          break;
        default:
          throw new InternalError();
      }
    }
    if (firstObjIndex != -1 && firstObjIndex + numObjFields != fields.length) {
      throw new InvalidClassException(name, "illegal field order");
    }
  }

  private static Method getPrivateMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
    if (argTypes == null) {
      argTypes = new Class<?>[0];
    }
    try {
      Method meth = cl.getDeclaredMethod(name, argTypes);
      meth.setAccessible(true);
      int mods = meth.getModifiers();
      return ((meth.getReturnType() == returnType) && ((mods & Modifier.STATIC) == 0)
          && ((mods & Modifier.PRIVATE) != 0)) ? meth : null;
    } catch (NoSuchMethodException ex) {
      return null;
    }
  }

  private static Method getInheritableMethod(Class<?> cl, String name, Class<?>[] argTypes, Class<?> returnType) {
    if (argTypes == null) {
      argTypes = new Class<?>[0];
    }
    Method meth = null;
    Class<?> defCl = cl;
    while (defCl != null) {
      try {
        meth = defCl.getDeclaredMethod(name, argTypes);
        break;
      } catch (NoSuchMethodException ex) {
        defCl = defCl.getSuperclass();
      }
    }
    if ((meth == null) || (meth.getReturnType() != returnType)) {
      return null;
    }
    meth.setAccessible(true);
    int mods = meth.getModifiers();
    if ((mods & (Modifier.STATIC | Modifier.ABSTRACT)) != 0) {
      return null;
    } else if ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
      return meth;
    } else if ((mods & Modifier.PRIVATE) != 0) {
      return (cl == defCl) ? meth : null;
    } else {
      return packageEquals(cl, defCl) ? meth : null;
    }
  }

  private static boolean packageEquals(Class<?> cl1, Class<?> cl2) {
    return cl1.getClassLoader() == cl2.getClassLoader() && cl1.getPackageName().equals(cl2.getPackageName());
  }

  private static String packageName(Class<?> cl) {
    return cl.getPackageName();
  }

  boolean isProxy() {
    return isProxy;
  }

  boolean isEnum() {
    return isEnum;
  }

  boolean isRecord() {
    return isRecord;
  }

  boolean isExternalizable() {
    return externalizable;
  }

  boolean isSerializable() {
    return serializable;
  }

  boolean hasBlockExternalData() {
    return hasBlockExternalData;
  }

  boolean hasWriteObjectData() {
    return hasWriteObjectData;
  }

  boolean isInstantiable() {
    return cons != null || isRecord;
  }

  boolean hasWriteObjectMethod() {
    return writeObjectMethod != null;
  }

  boolean hasReadObjectMethod() {
    return readObjectMethod != null;
  }

  boolean hasReadObjectNoDataMethod() {
    return readObjectNoDataMethod != null;
  }

  boolean hasWriteReplaceMethod() {
    return writeReplaceMethod != null;
  }

  boolean hasReadResolveMethod() {
    return readResolveMethod != null;
  }

  void checkDeserialize() throws InvalidClassException {
    requireInitialized();
    if (deserializeEx != null) {
      throw deserializeEx;
    }
  }

  void checkSerialize() throws InvalidClassException {
    requireInitialized();
    if (serializeEx != null) {
      throw serializeEx;
    }
  }

  void checkDefaultSerialize() throws InvalidClassException {
    requireInitialized();
  }

  private void requireInitialized() {
    if (cl == null && name == null) {
      throw new InternalError("uninitialized");
    }
  }

  ObjectStreamField[] getFields(boolean copy) {
    return copy ? fields.clone() : fields;
  }

  ObjectStreamField getField(String name, Class<?> type) {
    for (int i = 0; i < fields.length; i++) {
      ObjectStreamField f = fields[i];
      if (f.getName().equals(name)) {
        if (type == null || (type == Object.class && !f.isPrimitive())) {
          return f;
        }
        Class<?> ftype = f.getType();
        if (ftype != null && type.isAssignableFrom(ftype)) {
          return f;
        }
      }
    }
    return null;
  }

  int getPrimDataSize() {
    return primDataSize;
  }

  int getNumObjFields() {
    return numObjFields;
  }

  ObjectStreamClass getSuperDesc() {
    return superDesc;
  }

  ObjectStreamClass getLocalDesc() {
    return localDesc;
  }

  Class<?> getCl() {
    return cl;
  }

  Object allocate() throws InstantiationException {
    Object o = ObjectInputStream.makeInstance(shim.SystemClassLoader.vmClass(cl));
    if (o == null) {
      throw new InstantiationException(cl.getName());
    }
    if (cons != null && firstNonSerializable != Object.class) {
      try {
        constructorInvoke(cons, o);
      } catch (Exception e) {
        InstantiationException ie = new InstantiationException(cl.getName());
        ie.initCause(e);
        throw ie;
      }
    }
    return o;
  }

  private static void constructorInvoke(Constructor<?> c, Object receiver) throws Exception {
    c.method.invoke(receiver);
  }

  Object invokeReadResolve(Object obj) throws IOException, UnsupportedOperationException {
    try {
      return readResolveMethod.invoke(obj, (Object[]) null);
    } catch (java.lang.reflect.InvocationTargetException ex) {
      Throwable th = ex.getCause();
      if (th instanceof ObjectStreamException) {
        throw (ObjectStreamException) th;
      }
      throwMiscException(th);
      throw new InternalError(th);
    } catch (IllegalAccessException ex) {
      throw new InternalError(ex);
    }
  }

  Object invokeWriteReplace(Object obj) throws IOException {
    try {
      return writeReplaceMethod.invoke(obj, (Object[]) null);
    } catch (java.lang.reflect.InvocationTargetException ex) {
      Throwable th = ex.getCause();
      if (th instanceof ObjectStreamException) {
        throw (ObjectStreamException) th;
      }
      throwMiscException(th);
      throw new InternalError(th);
    } catch (IllegalAccessException ex) {
      throw new InternalError(ex);
    }
  }

  void invokeWriteObject(Object obj, ObjectOutputStream out) throws IOException {
    try {
      writeObjectMethod.invoke(obj, new Object[] {out});
    } catch (java.lang.reflect.InvocationTargetException ex) {
      Throwable th = ex.getCause();
      if (th instanceof IOException) {
        throw (IOException) th;
      }
      throwMiscException(th);
    } catch (IllegalAccessException ex) {
      throw new InternalError(ex);
    }
  }

  void invokeReadObject(Object obj, ObjectInputStream in) throws ClassNotFoundException, IOException {
    try {
      readObjectMethod.invoke(obj, new Object[] {in});
    } catch (java.lang.reflect.InvocationTargetException ex) {
      Throwable th = ex.getCause();
      if (th instanceof ClassNotFoundException) {
        throw (ClassNotFoundException) th;
      } else if (th instanceof IOException) {
        throw (IOException) th;
      } else {
        throwMiscException(th);
      }
    } catch (IllegalAccessException ex) {
      throw new InternalError(ex);
    }
  }

  void invokeReadObjectNoData(Object obj) throws IOException {
    try {
      readObjectNoDataMethod.invoke(obj, (Object[]) null);
    } catch (java.lang.reflect.InvocationTargetException ex) {
      Throwable th = ex.getCause();
      if (th instanceof ObjectStreamException) {
        throw (ObjectStreamException) th;
      } else {
        throwMiscException(th);
      }
    } catch (IllegalAccessException ex) {
      throw new InternalError(ex);
    }
  }

  private static void throwMiscException(Throwable th) throws IOException {
    if (th instanceof RuntimeException) {
      throw (RuntimeException) th;
    } else if (th instanceof Error) {
      throw (Error) th;
    } else {
      IOException ex = new IOException("unexpected exception type");
      ex.initCause(th);
      throw ex;
    }
  }

  Externalizable newExternalizable() throws Exception {
    return (Externalizable) cons.newInstance((Object[]) null);
  }

  Object newRecord(Object[] values) throws Exception {
    RecordComponent[] comps = cl.getRecordComponents();
    Class<?>[] types = new Class<?>[comps.length];
    for (int i = 0; i < comps.length; i++) {
      types[i] = comps[i].getType();
    }
    Constructor<?> canonical = cl.getDeclaredConstructor(types);
    canonical.setAccessible(true);
    return canonical.newInstance(values);
  }

  void initProxy(Class<?> cl, ClassNotFoundException resolveEx, ObjectStreamClass superDesc)
      throws InvalidClassException {
    ObjectStreamClass osc = null;
    if (cl != null) {
      osc = lookup(cl, true);
      if (!osc.isProxy) {
        throw new InvalidClassException("cannot bind proxy descriptor to a non-proxy class");
      }
    }
    this.cl = cl;
    this.name = cl == null ? "$Proxy" : cl.getName();
    this.isProxy = true;
    this.serializable = true;
    this.superDesc = superDesc;
    this.fields = NO_FIELDS;
    this.suid = Long.valueOf(0L);
    if (osc != null) {
      localDesc = osc;
      writeReplaceMethod = osc.writeReplaceMethod;
      readResolveMethod = osc.readResolveMethod;
      cons = osc.cons;
      firstNonSerializable = osc.firstNonSerializable;
    }
    if (resolveEx != null) {
      deserializeEx = new InvalidClassException(resolveEx.getMessage());
    }
  }

  static ObjectStreamClass readNonProxy(ObjectInputStream in) throws IOException, ClassNotFoundException {
    ObjectStreamClass d = new ObjectStreamClass();
    d.name = in.readUTF();
    d.suid = Long.valueOf(in.readLong());
    d.isProxy = false;
    byte flags = in.readByte();
    d.hasWriteObjectData = ((flags & ObjectStreamConstants.SC_WRITE_METHOD) != 0);
    d.hasBlockExternalData = ((flags & ObjectStreamConstants.SC_BLOCK_DATA) != 0);
    d.externalizable = ((flags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0);
    boolean sflag = ((flags & ObjectStreamConstants.SC_SERIALIZABLE) != 0);
    if (d.externalizable && sflag) {
      throw new InvalidClassException(d.name, "serializable and externalizable flags conflict");
    }
    d.serializable = d.externalizable || sflag;
    d.isEnum = ((flags & ObjectStreamConstants.SC_ENUM) != 0);
    if (d.isEnum && d.suid.longValue() != 0L) {
      throw new InvalidClassException(d.name, "enum descriptor has non-zero serialVersionUID: " + d.suid);
    }
    int numFields = in.readShort();
    if (d.isEnum && numFields != 0) {
      throw new InvalidClassException(d.name, "enum descriptor has non-zero field count: " + numFields);
    }
    d.fields = (numFields > 0) ? new ObjectStreamField[numFields] : NO_FIELDS;
    for (int i = 0; i < numFields; i++) {
      char tcode = (char) in.readByte();
      String fname = in.readUTF();
      String signature = ((tcode == 'L') || (tcode == '[')) ? in.readTypeString() : String.valueOf(tcode);
      try {
        d.fields[i] = new ObjectStreamField(fname, signature, false);
      } catch (RuntimeException e) {
        throw new InvalidClassException(d.name, "invalid descriptor for field " + fname);
      }
    }
    d.computeFieldOffsets();
    return d;
  }

  void initNonProxy(ObjectStreamClass model, Class<?> cl, ClassNotFoundException resolveEx,
      ObjectStreamClass superDesc) throws InvalidClassException {
    long suid = model.getSerialVersionUID();
    ObjectStreamClass osc = null;
    if (cl != null) {
      osc = lookup(cl, true);
      if (osc.isProxy) {
        throw new InvalidClassException("cannot bind non-proxy descriptor to a proxy class");
      }
      if (model.isEnum != osc.isEnum) {
        throw new InvalidClassException(model.isEnum ? "cannot bind enum descriptor to a non-enum class"
            : "cannot bind non-enum descriptor to an enum class");
      }
      if (model.serializable == osc.serializable && !cl.isArray() && !cl.isRecord()
          && suid != osc.getSerialVersionUID()) {
        throw new InvalidClassException(osc.name, "local class incompatible: stream classdesc serialVersionUID = "
            + suid + ", local class serialVersionUID = " + osc.getSerialVersionUID());
      }
      if (!classNamesEqual(model.name, osc.name)) {
        throw new InvalidClassException(osc.name, "local class name incompatible with stream class name \""
            + model.name + "\"");
      }
      if (!model.isEnum) {
        if ((model.serializable == osc.serializable) && (model.externalizable != osc.externalizable)) {
          throw new InvalidClassException(osc.name, "Serializable incompatible with Externalizable");
        }
        if ((model.serializable != osc.serializable) || (model.externalizable != osc.externalizable)
            || !(model.serializable || model.externalizable)) {
          deserializeEx = new InvalidClassException(osc.name, "class invalid for deserialization");
        }
      }
    }
    this.cl = cl;
    this.name = model.name;
    this.suid = Long.valueOf(suid);
    this.isProxy = false;
    this.isEnum = model.isEnum;
    this.serializable = model.serializable;
    this.externalizable = model.externalizable;
    this.hasBlockExternalData = model.hasBlockExternalData;
    this.hasWriteObjectData = model.hasWriteObjectData;
    this.fields = model.fields;
    this.primDataSize = model.primDataSize;
    this.numObjFields = model.numObjFields;
    if (osc != null) {
      localDesc = osc;
      isRecord = osc.isRecord;
      writeObjectMethod = osc.writeObjectMethod;
      readObjectMethod = osc.readObjectMethod;
      readObjectNoDataMethod = osc.readObjectNoDataMethod;
      writeReplaceMethod = osc.writeReplaceMethod;
      readResolveMethod = osc.readResolveMethod;
      if (deserializeEx == null) {
        deserializeEx = osc.deserializeEx;
      }
      cons = osc.cons;
      firstNonSerializable = osc.firstNonSerializable;
    }
    if (resolveEx != null && deserializeEx == null) {
      deserializeEx = new InvalidClassException(resolveEx.getMessage());
    }
    this.superDesc = superDesc;
    if (cl != null) {
      matchFields();
    }
  }

  private static boolean classNamesEqual(String name1, String name2) {
    name1 = name1.substring(name1.lastIndexOf('.') + 1);
    name2 = name2.substring(name2.lastIndexOf('.') + 1);
    return name1.equals(name2);
  }

  private void matchFields() throws InvalidClassException {
    ObjectStreamField[] localFields = (localDesc != null) ? localDesc.fields : NO_FIELDS;
    ObjectStreamField[] matched = new ObjectStreamField[fields.length];
    for (int i = 0; i < fields.length; i++) {
      ObjectStreamField f = fields[i];
      ObjectStreamField m = null;
      for (int j = 0; j < localFields.length; j++) {
        ObjectStreamField lf = localFields[j];
        if (f.getName().equals(lf.getName())) {
          if ((f.isPrimitive() || lf.isPrimitive()) && f.getTypeCode() != lf.getTypeCode()) {
            throw new InvalidClassException(localDesc.name, "incompatible types for field " + f.getName());
          }
          if (lf.getField() != null) {
            m = new ObjectStreamField(lf.getField(), lf.isUnshared(), false);
          } else {
            m = new ObjectStreamField(lf.getName(), lf.getSignature(), lf.isUnshared());
          }
        }
      }
      if (m == null) {
        m = new ObjectStreamField(f.getName(), f.getSignature(), false);
      }
      m.setOffset(f.getOffset());
      matched[i] = m;
    }
    fields = matched;
  }

  void writeNonProxy(ObjectOutputStream out) throws IOException {
    out.writeUTF(name);
    out.writeLong(getSerialVersionUID());
    byte flags = 0;
    if (externalizable) {
      flags |= ObjectStreamConstants.SC_EXTERNALIZABLE;
      int protocol = out.getProtocolVersion();
      if (protocol != ObjectStreamConstants.PROTOCOL_VERSION_1) {
        flags |= ObjectStreamConstants.SC_BLOCK_DATA;
      }
    } else if (serializable) {
      flags |= ObjectStreamConstants.SC_SERIALIZABLE;
    }
    if (hasWriteObjectData) {
      flags |= ObjectStreamConstants.SC_WRITE_METHOD;
    }
    if (isEnum) {
      flags |= ObjectStreamConstants.SC_ENUM;
    }
    out.writeByte(flags);
    out.writeShort(fields.length);
    for (int i = 0; i < fields.length; i++) {
      ObjectStreamField f = fields[i];
      out.writeByte(f.getTypeCode());
      out.writeUTF(f.getName());
      if (!f.isPrimitive()) {
        out.writeTypeString(f.getTypeString());
      }
    }
  }

  ClassDataSlot[] getClassDataLayout() throws InvalidClassException {
    ArrayList<ClassDataSlot> slots = new ArrayList<ClassDataSlot>();
    Class<?> start = cl;
    Class<?> end = cl;
    while (end != null && Serializable.class.isAssignableFrom(end)) {
      end = end.getSuperclass();
    }
    HashSet<String> oscNames = new HashSet<String>(3);
    for (ObjectStreamClass d = this; d != null; d = d.superDesc) {
      if (oscNames.contains(d.name)) {
        throw new InvalidClassException("Circular reference.");
      }
      oscNames.add(d.name);
      String searchName = (d.cl != null) ? d.cl.getName() : d.name;
      Class<?> match = null;
      for (Class<?> c = start; c != end; c = c == null ? null : c.getSuperclass()) {
        if (c == null) {
          break;
        }
        if (searchName.equals(c.getName())) {
          match = c;
          break;
        }
      }
      if (match != null) {
        for (Class<?> c = start; c != match; c = c.getSuperclass()) {
          slots.add(new ClassDataSlot(ObjectStreamClass.lookup(c, true), false));
        }
        start = match.getSuperclass();
      }
      slots.add(new ClassDataSlot(d.getVariantFor(match), true));
    }
    for (Class<?> c = start; c != end && c != null; c = c.getSuperclass()) {
      slots.add(new ClassDataSlot(ObjectStreamClass.lookup(c, true), false));
    }
    Collections.reverse(slots);
    return slots.toArray(new ClassDataSlot[slots.size()]);
  }

  private ObjectStreamClass getVariantFor(Class<?> cl) throws InvalidClassException {
    if (this.cl == cl) {
      return this;
    }
    ObjectStreamClass desc = new ObjectStreamClass();
    if (isProxy) {
      desc.initProxy(cl, null, superDesc);
    } else {
      desc.initNonProxy(this, cl, null, superDesc);
    }
    return desc;
  }

  static final class ClassDataSlot {
    final ObjectStreamClass desc;
    final boolean hasData;

    ClassDataSlot(ObjectStreamClass desc, boolean hasData) {
      this.desc = desc;
      this.hasData = hasData;
    }
  }

  void setSuperDesc(ObjectStreamClass superDesc) {
    this.superDesc = superDesc;
  }

  void setClassForProxy(Class<?> cl) {
    this.cl = cl;
  }

  void copyFlagsFrom(ObjectStreamClass other) { }

  void checkFinal() { }

  static ObjectStreamClass placeholder(String name) {
    ObjectStreamClass d = new ObjectStreamClass();
    d.name = name;
    d.fields = NO_FIELDS;
    d.suid = Long.valueOf(0L);
    return d;
  }


  private static long computeDefaultSUID(Class<?> cl) {
    if (!Serializable.class.isAssignableFrom(cl) || java.lang.reflect.Proxy.isProxyClass(cl)) {
      return 0L;
    }
    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      DataOutputStream dout = new DataOutputStream(bout);
      dout.writeUTF(cl.getName());
      int classMods = cl.getModifiers() & (Modifier.PUBLIC | Modifier.FINAL | Modifier.INTERFACE | Modifier.ABSTRACT);
      VMClass vm = SystemClassLoader.vmClass(cl);
      List<VMMethod> methods = declaredMethods(vm);
      if ((classMods & Modifier.INTERFACE) != 0) {
        classMods = (methods.size() > 0) ? (classMods | Modifier.ABSTRACT) : (classMods & ~Modifier.ABSTRACT);
      }
      dout.writeInt(classMods);
      if (!cl.isArray()) {
        Class<?>[] interfaces = cl.getInterfaces();
        String[] ifaceNames = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
          ifaceNames[i] = interfaces[i].getName();
        }
        Arrays.sort(ifaceNames);
        for (int i = 0; i < ifaceNames.length; i++) {
          dout.writeUTF(ifaceNames[i]);
        }
      }
      List<VMField> fieldList = new ArrayList<VMField>();
      if (vm.fieldTable != null) {
        for (VMField f : vm.fieldTable) {
          fieldList.add(f);
        }
      }
      Collections.sort(fieldList, new Comparator<VMField>() {
        public int compare(VMField a, VMField b) {
          return Classes.toString(a.name).compareTo(Classes.toString(b.name));
        }
      });
      for (VMField f : fieldList) {
        int mods = f.flags & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC
            | Modifier.FINAL | Modifier.VOLATILE | Modifier.TRANSIENT);
        if (((mods & Modifier.PRIVATE) == 0) || ((mods & (Modifier.STATIC | Modifier.TRANSIENT)) == 0)) {
          dout.writeUTF(Classes.toString(f.name));
          dout.writeInt(mods);
          dout.writeUTF(Classes.toString(f.spec));
        }
      }
      boolean hasClinit = false;
      for (VMMethod m : methods) {
        if (Classes.toString(m.name).equals("<clinit>")) {
          hasClinit = true;
        }
      }
      if (hasClinit) {
        dout.writeUTF("<clinit>");
        dout.writeInt(Modifier.STATIC);
        dout.writeUTF("()V");
      }
      List<VMMethod> ctors = new ArrayList<VMMethod>();
      List<VMMethod> ordinary = new ArrayList<VMMethod>();
      for (VMMethod m : methods) {
        String n = Classes.toString(m.name);
        if (n.equals("<clinit>")) {
          continue;
        }
        if ((m.flags & Modifier.PRIVATE) != 0) {
          continue;
        }
        if (n.equals("<init>")) {
          ctors.add(m);
        } else {
          ordinary.add(m);
        }
      }
      Collections.sort(ctors, new Comparator<VMMethod>() {
        public int compare(VMMethod a, VMMethod b) {
          return Classes.toString(a.spec).compareTo(Classes.toString(b.spec));
        }
      });
      int ctorMask = Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
          | Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.ABSTRACT | Modifier.STRICT;
      for (VMMethod m : ctors) {
        dout.writeUTF("<init>");
        dout.writeInt(m.flags & ctorMask);
        dout.writeUTF(Classes.toString(m.spec).replace('/', '.'));
      }
      Collections.sort(ordinary, new Comparator<VMMethod>() {
        public int compare(VMMethod a, VMMethod b) {
          int c = Classes.toString(a.name).compareTo(Classes.toString(b.name));
          return c != 0 ? c : Classes.toString(a.spec).compareTo(Classes.toString(b.spec));
        }
      });
      for (VMMethod m : ordinary) {
        dout.writeUTF(Classes.toString(m.name));
        dout.writeInt(m.flags & ctorMask);
        dout.writeUTF(Classes.toString(m.spec).replace('/', '.'));
      }
      dout.flush();
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] hashBytes = md.digest(bout.toByteArray());
      long hash = 0;
      for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
        hash = (hash << 8) | (hashBytes[i] & 0xFF);
      }
      return hash;
    } catch (IOException ex) {
      throw new InternalError(ex);
    } catch (java.security.NoSuchAlgorithmException ex) {
      throw new SecurityException(ex.getMessage());
    }
  }

  private static List<VMMethod> declaredMethods(VMClass vm) {
    List<VMMethod> list = new ArrayList<VMMethod>();
    VMMethod[] table = vm.methodTable;
    if (table == null) {
      return list;
    }
    int count = table.length;
    if (vm.addendum != null && vm.addendum.declaredMethodCount >= 0) {
      count = vm.addendum.declaredMethodCount;
    }
    for (int i = 0; i < count; i++) {
      list.add(table[i]);
    }
    return list;
  }
}
