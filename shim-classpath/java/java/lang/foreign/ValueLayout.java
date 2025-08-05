/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public interface ValueLayout extends MemoryLayout {
  AddressLayout ADDRESS = AddressLayoutImpl.DEFAULT;

  ValueLayout.OfByte JAVA_BYTE = ValueLayoutImpl.OfByteImpl.DEFAULT;
  ValueLayout.OfBoolean JAVA_BOOLEAN = ValueLayoutImpl.OfBooleanImpl.DEFAULT;
  ValueLayout.OfChar JAVA_CHAR = ValueLayoutImpl.OfCharImpl.DEFAULT;
  ValueLayout.OfShort JAVA_SHORT = ValueLayoutImpl.OfShortImpl.DEFAULT;
  ValueLayout.OfInt JAVA_INT = ValueLayoutImpl.OfIntImpl.DEFAULT;
  ValueLayout.OfLong JAVA_LONG = ValueLayoutImpl.OfLongImpl.DEFAULT;
  ValueLayout.OfFloat JAVA_FLOAT = ValueLayoutImpl.OfFloatImpl.DEFAULT;
  ValueLayout.OfDouble JAVA_DOUBLE = ValueLayoutImpl.OfDoubleImpl.DEFAULT;

  AddressLayout ADDRESS_UNALIGNED = AddressLayoutImpl.UNALIGNED;
  ValueLayout.OfChar JAVA_CHAR_UNALIGNED = ValueLayoutImpl.OfCharImpl.UNALIGNED;
  ValueLayout.OfShort JAVA_SHORT_UNALIGNED = ValueLayoutImpl.OfShortImpl.UNALIGNED;
  ValueLayout.OfInt JAVA_INT_UNALIGNED = ValueLayoutImpl.OfIntImpl.UNALIGNED;
  ValueLayout.OfLong JAVA_LONG_UNALIGNED = ValueLayoutImpl.OfLongImpl.UNALIGNED;
  ValueLayout.OfFloat JAVA_FLOAT_UNALIGNED = ValueLayoutImpl.OfFloatImpl.UNALIGNED;
  ValueLayout.OfDouble JAVA_DOUBLE_UNALIGNED = ValueLayoutImpl.OfDoubleImpl.UNALIGNED;

  ByteOrder order();

  ValueLayout withOrder(ByteOrder order);

  @Override
  ValueLayout withoutName();

  Class<?> carrier();

  @Override
  ValueLayout withName(String name);

  @Override
  ValueLayout withByteAlignment(long byteAlignment);

  VarHandle varHandle();

  interface OfByte extends ValueLayout {
    @Override
    ValueLayout.OfByte withName(String name);

    @Override
    ValueLayout.OfByte withoutName();

    @Override
    ValueLayout.OfByte withByteAlignment(long byteAlignment);

    @Override
    ValueLayout.OfByte withOrder(ByteOrder order);
  }

  interface OfBoolean extends ValueLayout {
    @Override
    ValueLayout.OfBoolean withName(String name);

    @Override
    ValueLayout.OfBoolean withoutName();

    @Override
    ValueLayout.OfBoolean withByteAlignment(long byteAlignment);

    @Override
    ValueLayout.OfBoolean withOrder(ByteOrder order);
  }

  interface OfChar extends ValueLayout {
    @Override
    ValueLayout.OfChar withName(String name);

    @Override
    ValueLayout.OfChar withoutName();

    @Override
    ValueLayout.OfChar withByteAlignment(long byteAlignment);

    @Override
    ValueLayout.OfChar withOrder(ByteOrder order);
  }

  interface OfShort extends ValueLayout {
    @Override
    ValueLayout.OfShort withName(String name);

    @Override
    ValueLayout.OfShort withoutName();

    @Override
    ValueLayout.OfShort withByteAlignment(long byteAlignment);

    @Override
    ValueLayout.OfShort withOrder(ByteOrder order);
  }

  interface OfInt extends ValueLayout {
    @Override
    ValueLayout.OfInt withName(String name);

    @Override
    ValueLayout.OfInt withoutName();

    @Override
    ValueLayout.OfInt withByteAlignment(long byteAlignment);

    @Override
    ValueLayout.OfInt withOrder(ByteOrder order);
  }

  interface OfLong extends ValueLayout {
    @Override
    ValueLayout.OfLong withName(String name);

    @Override
    ValueLayout.OfLong withoutName();

    @Override
    ValueLayout.OfLong withByteAlignment(long byteAlignment);

    @Override
    ValueLayout.OfLong withOrder(ByteOrder order);
  }

  interface OfFloat extends ValueLayout {
    @Override
    ValueLayout.OfFloat withName(String name);

    @Override
    ValueLayout.OfFloat withoutName();

    @Override
    ValueLayout.OfFloat withByteAlignment(long byteAlignment);

    @Override
    ValueLayout.OfFloat withOrder(ByteOrder order);
  }

  interface OfDouble extends ValueLayout {
    @Override
    ValueLayout.OfDouble withName(String name);

    @Override
    ValueLayout.OfDouble withoutName();

    @Override
    ValueLayout.OfDouble withByteAlignment(long byteAlignment);

    @Override
    ValueLayout.OfDouble withOrder(ByteOrder order);
  }
}
