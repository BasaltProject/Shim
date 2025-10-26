/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.io.ObjectStreamException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.management.ObjectName;

public final class SimpleType<T> extends OpenType<T> {
  private static final long serialVersionUID = 2215577471957694503L;

  public static final SimpleType<Void> VOID = make(Void.class);
  public static final SimpleType<Boolean> BOOLEAN = make(Boolean.class);
  public static final SimpleType<Character> CHARACTER = make(Character.class);
  public static final SimpleType<Byte> BYTE = make(Byte.class);
  public static final SimpleType<Short> SHORT = make(Short.class);
  public static final SimpleType<Integer> INTEGER = make(Integer.class);
  public static final SimpleType<Long> LONG = make(Long.class);
  public static final SimpleType<Float> FLOAT = make(Float.class);
  public static final SimpleType<Double> DOUBLE = make(Double.class);
  public static final SimpleType<String> STRING = make(String.class);
  public static final SimpleType<BigDecimal> BIGDECIMAL = make(BigDecimal.class);
  public static final SimpleType<BigInteger> BIGINTEGER = make(BigInteger.class);
  public static final SimpleType<Date> DATE = make(Date.class);
  public static final SimpleType<ObjectName> OBJECTNAME = make(ObjectName.class);

  private transient Integer myHashCode;
  private transient String myToString;

  private static <T> SimpleType<T> make(Class<T> c) {
    try {
      return new SimpleType<T>(c.getName());
    } catch (OpenDataException e) {
      throw new Error(e);
    }
  }

  private SimpleType(String className) throws OpenDataException {
    super(className, className, className);
  }

  @Override
  public boolean isValue(Object obj) {
    if (obj == null) {
      return false;
    }
    return getClassName().equals(obj.getClass().getName());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SimpleType)) {
      return false;
    }
    return getClassName().equals(((SimpleType<?>) obj).getClassName());
  }

  @Override
  public int hashCode() {
    if (myHashCode == null) {
      myHashCode = Integer.valueOf(getClassName().hashCode());
    }
    return myHashCode.intValue();
  }

  @Override
  public String toString() {
    if (myToString == null) {
      myToString = getClass().getName() + "(name=" + getTypeName() + ")";
    }
    return myToString;
  }

  public Object readResolve() throws ObjectStreamException {
    SimpleType<?>[] all = {VOID, BOOLEAN, CHARACTER, BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, STRING, BIGDECIMAL,
        BIGINTEGER, DATE, OBJECTNAME};
    for (SimpleType<?> t : all) {
      if (t.equals(this)) {
        return t;
      }
    }
    throw new java.io.InvalidObjectException("Invalid SimpleType: " + getClassName());
  }
}
