/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.serial;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public class SerialJavaObject implements Serializable, Cloneable {
  private static final long serialVersionUID = -1465795139032831023L;

  private Object obj;
  private transient Field[] fields;

  public SerialJavaObject(Object obj) throws SerialException {
    Class<?> c = obj.getClass();
    Field[] f = c.getFields();
    if (hasStaticFields(f)) {
      throw new SerialException("Located static fields in object");
    }
    this.obj = obj;
    this.fields = f;
  }

  private static boolean hasStaticFields(Field[] fields) {
    for (Field field : fields) {
      if (field.getModifiers() == Modifier.STATIC) {
        return true;
      }
    }
    return false;
  }

  public Object getObject() throws SerialException {
    return this.obj;
  }

  public Field[] getFields() throws SerialException {
    if (fields != null) {
      Class<?> c = this.obj.getClass();
      return c.getFields();
    } else {
      throw new SerialException("SerialJavaObject does not contain a serialized object instance");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof SerialJavaObject) {
      SerialJavaObject sjo = (SerialJavaObject) o;
      return obj.equals(sjo.obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 + obj.hashCode();
  }

  @Override
  public Object clone() {
    try {
      SerialJavaObject sjo = (SerialJavaObject) super.clone();
      sjo.fields = Arrays.copyOf(fields, fields.length);
      return sjo;
    } catch (CloneNotSupportedException ex) {
      throw new InternalError();
    }
  }
}
