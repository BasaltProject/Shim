/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

final class CollSer implements Serializable {
  private static final long serialVersionUID = 6309168927139932177L;

  static final int IMM_LIST = 0x1;
  static final int IMM_SET = 0x2;
  static final int IMM_MAP = 0x3;
  static final int IMM_LIST_NULLS = 0x4;

  private final int tag;
  private transient Object[] array;

  CollSer(int t, Object... a) {
    tag = t;
    array = a;
  }

  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    int len = ois.readInt();
    if (len < 0) {
      throw new InvalidObjectException("negative length " + len);
    }
    Object[] a = new Object[len];
    for (int i = 0; i < len; i++) {
      a[i] = ois.readObject();
    }
    array = a;
  }

  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    oos.writeInt(array.length);
    for (int i = 0; i < array.length; i++) {
      oos.writeObject(array[i]);
    }
  }

  private Object readResolve() throws ObjectStreamException {
    try {
      if (array == null) {
        throw new InvalidObjectException("null array");
      }
      switch (tag & 0xff) {
        case IMM_LIST:
          return List.of(array);
        case IMM_LIST_NULLS:
          return Collections.unmodifiableList(new ArrayList<Object>(Arrays.asList(array)));
        case IMM_SET:
          return Set.of(array);
        case IMM_MAP:
          if (array.length == 0) {
            return ImmutableCollections.EMPTY_MAP;
          }
          return new ImmutableCollections.MapN<Object, Object>(array);
        default:
          throw new InvalidObjectException(String.format("invalid flags 0x%x", tag));
      }
    } catch (NullPointerException | IllegalArgumentException ex) {
      InvalidObjectException ioe = new InvalidObjectException("invalid object");
      ioe.initCause(ex);
      throw ioe;
    }
  }
}
