/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Objects;

abstract class ValueLayoutImpl extends AbstractMemoryLayout implements ValueLayout {
  final Class<?> carrier;
  final ByteOrder order;
  final char typeChar;

  ValueLayoutImpl(long byteSize, long byteAlignment, String name, Class<?> carrier,
                  ByteOrder order, char typeChar) {
    super(byteSize, byteAlignment, name);
    this.carrier = Objects.requireNonNull(carrier, "carrier");
    this.order = Objects.requireNonNull(order, "order");
    this.typeChar = typeChar;
  }

  @Override
  public Class<?> carrier() {
    return carrier;
  }

  @Override
  public ByteOrder order() {
    return order;
  }

  @Override
  public VarHandle varHandle() {
    return new SegmentVarHandle(this, 0L);
  }

  @Override
  public String toString() {
    char c = (order == ByteOrder.BIG_ENDIAN) ? Character.toUpperCase(typeChar) : typeChar;
    String base = c + Long.toString(byteSize);
    if (byteAlignment != byteSize) {
      base = byteAlignment + "%" + base;
    }
    return formatName(base, name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ValueLayout)) return false;
    ValueLayout other = (ValueLayout) obj;
    return carrier.equals(other.carrier())
        && byteSize == other.byteSize()
        && byteAlignment == other.byteAlignment()
        && order.equals(other.order())
        && name().equals(other.name());
  }

  @Override
  public int hashCode() {
    return Objects.hash(carrier, byteSize, byteAlignment, order, name);
  }

  static final class OfByteImpl extends ValueLayoutImpl implements ValueLayout.OfByte {
    static final OfByteImpl DEFAULT = new OfByteImpl(1L, 1L, null, ByteOrder.nativeOrder());

    OfByteImpl(long byteSize, long byteAlignment, String name, ByteOrder order) {
      super(byteSize, byteAlignment, name, byte.class, order, 'b');
    }

    @Override
    public ValueLayout.OfByte withName(String name) {
      Objects.requireNonNull(name, "name");
      return new OfByteImpl(byteSize, byteAlignment, name, order);
    }

    @Override
    public ValueLayout.OfByte withoutName() {
      return name == null ? this : new OfByteImpl(byteSize, byteAlignment, null, order);
    }

    @Override
    public ValueLayout.OfByte withByteAlignment(long alignment) {
      if (alignment <= 0 || Long.bitCount(alignment) != 1) {
        throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
      }
      return new OfByteImpl(byteSize, alignment, name, order);
    }

    @Override
    public ValueLayout.OfByte withOrder(ByteOrder newOrder) {
      Objects.requireNonNull(newOrder, "newOrder");
      return newOrder == order ? this : new OfByteImpl(byteSize, byteAlignment, name, newOrder);
    }
  }

  static final class OfBooleanImpl extends ValueLayoutImpl implements ValueLayout.OfBoolean {
    static final OfBooleanImpl DEFAULT = new OfBooleanImpl(1L, 1L, null, ByteOrder.nativeOrder());

    OfBooleanImpl(long byteSize, long byteAlignment, String name, ByteOrder order) {
      super(byteSize, byteAlignment, name, boolean.class, order, 'z');
    }

    @Override
    public ValueLayout.OfBoolean withName(String name) {
      Objects.requireNonNull(name, "name");
      return new OfBooleanImpl(byteSize, byteAlignment, name, order);
    }

    @Override
    public ValueLayout.OfBoolean withoutName() {
      return name == null ? this : new OfBooleanImpl(byteSize, byteAlignment, null, order);
    }

    @Override
    public ValueLayout.OfBoolean withByteAlignment(long alignment) {
      if (alignment <= 0 || Long.bitCount(alignment) != 1) {
        throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
      }
      return new OfBooleanImpl(byteSize, alignment, name, order);
    }

    @Override
    public ValueLayout.OfBoolean withOrder(ByteOrder newOrder) {
      Objects.requireNonNull(newOrder, "newOrder");
      return newOrder == order ? this : new OfBooleanImpl(byteSize, byteAlignment, name, newOrder);
    }
  }

  static final class OfCharImpl extends ValueLayoutImpl implements ValueLayout.OfChar {
    static final OfCharImpl DEFAULT = new OfCharImpl(2L, 2L, null, ByteOrder.nativeOrder());
    static final OfCharImpl UNALIGNED = new OfCharImpl(2L, 1L, null, ByteOrder.nativeOrder());

    OfCharImpl(long byteSize, long byteAlignment, String name, ByteOrder order) {
      super(byteSize, byteAlignment, name, char.class, order, 'c');
    }

    @Override
    public ValueLayout.OfChar withName(String name) {
      Objects.requireNonNull(name, "name");
      return new OfCharImpl(byteSize, byteAlignment, name, order);
    }

    @Override
    public ValueLayout.OfChar withoutName() {
      return name == null ? this : new OfCharImpl(byteSize, byteAlignment, null, order);
    }

    @Override
    public ValueLayout.OfChar withByteAlignment(long alignment) {
      if (alignment <= 0 || Long.bitCount(alignment) != 1) {
        throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
      }
      return new OfCharImpl(byteSize, alignment, name, order);
    }

    @Override
    public ValueLayout.OfChar withOrder(ByteOrder newOrder) {
      Objects.requireNonNull(newOrder, "newOrder");
      return newOrder == order ? this : new OfCharImpl(byteSize, byteAlignment, name, newOrder);
    }
  }

  static final class OfShortImpl extends ValueLayoutImpl implements ValueLayout.OfShort {
    static final OfShortImpl DEFAULT = new OfShortImpl(2L, 2L, null, ByteOrder.nativeOrder());
    static final OfShortImpl UNALIGNED = new OfShortImpl(2L, 1L, null, ByteOrder.nativeOrder());

    OfShortImpl(long byteSize, long byteAlignment, String name, ByteOrder order) {
      super(byteSize, byteAlignment, name, short.class, order, 's');
    }

    @Override
    public ValueLayout.OfShort withName(String name) {
      Objects.requireNonNull(name, "name");
      return new OfShortImpl(byteSize, byteAlignment, name, order);
    }

    @Override
    public ValueLayout.OfShort withoutName() {
      return name == null ? this : new OfShortImpl(byteSize, byteAlignment, null, order);
    }

    @Override
    public ValueLayout.OfShort withByteAlignment(long alignment) {
      if (alignment <= 0 || Long.bitCount(alignment) != 1) {
        throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
      }
      return new OfShortImpl(byteSize, alignment, name, order);
    }

    @Override
    public ValueLayout.OfShort withOrder(ByteOrder newOrder) {
      Objects.requireNonNull(newOrder, "newOrder");
      return newOrder == order ? this : new OfShortImpl(byteSize, byteAlignment, name, newOrder);
    }
  }

  static final class OfIntImpl extends ValueLayoutImpl implements ValueLayout.OfInt {
    static final OfIntImpl DEFAULT = new OfIntImpl(4L, 4L, null, ByteOrder.nativeOrder());
    static final OfIntImpl UNALIGNED = new OfIntImpl(4L, 1L, null, ByteOrder.nativeOrder());

    OfIntImpl(long byteSize, long byteAlignment, String name, ByteOrder order) {
      super(byteSize, byteAlignment, name, int.class, order, 'i');
    }

    @Override
    public ValueLayout.OfInt withName(String name) {
      Objects.requireNonNull(name, "name");
      return new OfIntImpl(byteSize, byteAlignment, name, order);
    }

    @Override
    public ValueLayout.OfInt withoutName() {
      return name == null ? this : new OfIntImpl(byteSize, byteAlignment, null, order);
    }

    @Override
    public ValueLayout.OfInt withByteAlignment(long alignment) {
      if (alignment <= 0 || Long.bitCount(alignment) != 1) {
        throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
      }
      return new OfIntImpl(byteSize, alignment, name, order);
    }

    @Override
    public ValueLayout.OfInt withOrder(ByteOrder newOrder) {
      Objects.requireNonNull(newOrder, "newOrder");
      return newOrder == order ? this : new OfIntImpl(byteSize, byteAlignment, name, newOrder);
    }
  }

  static final class OfLongImpl extends ValueLayoutImpl implements ValueLayout.OfLong {
    static final OfLongImpl DEFAULT = new OfLongImpl(8L, 8L, null, ByteOrder.nativeOrder());
    static final OfLongImpl UNALIGNED = new OfLongImpl(8L, 1L, null, ByteOrder.nativeOrder());

    OfLongImpl(long byteSize, long byteAlignment, String name, ByteOrder order) {
      super(byteSize, byteAlignment, name, long.class, order, 'j');
    }

    @Override
    public ValueLayout.OfLong withName(String name) {
      Objects.requireNonNull(name, "name");
      return new OfLongImpl(byteSize, byteAlignment, name, order);
    }

    @Override
    public ValueLayout.OfLong withoutName() {
      return name == null ? this : new OfLongImpl(byteSize, byteAlignment, null, order);
    }

    @Override
    public ValueLayout.OfLong withByteAlignment(long alignment) {
      if (alignment <= 0 || Long.bitCount(alignment) != 1) {
        throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
      }
      return new OfLongImpl(byteSize, alignment, name, order);
    }

    @Override
    public ValueLayout.OfLong withOrder(ByteOrder newOrder) {
      Objects.requireNonNull(newOrder, "newOrder");
      return newOrder == order ? this : new OfLongImpl(byteSize, byteAlignment, name, newOrder);
    }
  }

  static final class OfFloatImpl extends ValueLayoutImpl implements ValueLayout.OfFloat {
    static final OfFloatImpl DEFAULT = new OfFloatImpl(4L, 4L, null, ByteOrder.nativeOrder());
    static final OfFloatImpl UNALIGNED = new OfFloatImpl(4L, 1L, null, ByteOrder.nativeOrder());

    OfFloatImpl(long byteSize, long byteAlignment, String name, ByteOrder order) {
      super(byteSize, byteAlignment, name, float.class, order, 'f');
    }

    @Override
    public ValueLayout.OfFloat withName(String name) {
      Objects.requireNonNull(name, "name");
      return new OfFloatImpl(byteSize, byteAlignment, name, order);
    }

    @Override
    public ValueLayout.OfFloat withoutName() {
      return name == null ? this : new OfFloatImpl(byteSize, byteAlignment, null, order);
    }

    @Override
    public ValueLayout.OfFloat withByteAlignment(long alignment) {
      if (alignment <= 0 || Long.bitCount(alignment) != 1) {
        throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
      }
      return new OfFloatImpl(byteSize, alignment, name, order);
    }

    @Override
    public ValueLayout.OfFloat withOrder(ByteOrder newOrder) {
      Objects.requireNonNull(newOrder, "newOrder");
      return newOrder == order ? this : new OfFloatImpl(byteSize, byteAlignment, name, newOrder);
    }
  }

  static final class OfDoubleImpl extends ValueLayoutImpl implements ValueLayout.OfDouble {
    static final OfDoubleImpl DEFAULT = new OfDoubleImpl(8L, 8L, null, ByteOrder.nativeOrder());
    static final OfDoubleImpl UNALIGNED = new OfDoubleImpl(8L, 1L, null, ByteOrder.nativeOrder());

    OfDoubleImpl(long byteSize, long byteAlignment, String name, ByteOrder order) {
      super(byteSize, byteAlignment, name, double.class, order, 'd');
    }

    @Override
    public ValueLayout.OfDouble withName(String name) {
      Objects.requireNonNull(name, "name");
      return new OfDoubleImpl(byteSize, byteAlignment, name, order);
    }

    @Override
    public ValueLayout.OfDouble withoutName() {
      return name == null ? this : new OfDoubleImpl(byteSize, byteAlignment, null, order);
    }

    @Override
    public ValueLayout.OfDouble withByteAlignment(long alignment) {
      if (alignment <= 0 || Long.bitCount(alignment) != 1) {
        throw new IllegalArgumentException("Alignment must be a power of 2: " + alignment);
      }
      return new OfDoubleImpl(byteSize, alignment, name, order);
    }

    @Override
    public ValueLayout.OfDouble withOrder(ByteOrder newOrder) {
      Objects.requireNonNull(newOrder, "newOrder");
      return newOrder == order ? this : new OfDoubleImpl(byteSize, byteAlignment, name, newOrder);
    }
  }
}
