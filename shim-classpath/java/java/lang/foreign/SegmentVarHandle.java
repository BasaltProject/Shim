/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;

final class SegmentVarHandle extends VarHandle {
  private final ValueLayout layout;
  private final long fixedOffset;
  private final long elementSize;

  SegmentVarHandle(ValueLayout layout, long fixedOffset) {
    super(layout.carrier(), Arrays.<Class<?>>asList(MemorySegment.class, long.class));
    this.layout = layout;
    this.fixedOffset = fixedOffset;
    this.elementSize = 0L;
  }

  SegmentVarHandle(ValueLayout layout, long fixedOffset, long elementSize) {
    super(layout.carrier(), Arrays.<Class<?>>asList(MemorySegment.class, long.class, long.class));
    this.layout = layout;
    this.fixedOffset = fixedOffset;
    this.elementSize = elementSize;
  }

  private long computeOffset(Object[] args) {
    long base = ((Number) args[1]).longValue();
    if (elementSize == 0L) {
      return base + fixedOffset;
    } else {
      long index = ((Number) args[2]).longValue();
      return base + fixedOffset + Math.multiplyExact(index, elementSize);
    }
  }

  private int valueIndex() {
    return elementSize == 0L ? 2 : 3;
  }

  @Override
  public Object get(Object... args) {
    MemorySegment seg = (MemorySegment) args[0];
    long offset = computeOffset(args);
    if (layout instanceof ValueLayout.OfByte) {
      return seg.get((ValueLayout.OfByte) layout, offset);
    } else if (layout instanceof ValueLayout.OfBoolean) {
      return seg.get((ValueLayout.OfBoolean) layout, offset);
    } else if (layout instanceof ValueLayout.OfChar) {
      return seg.get((ValueLayout.OfChar) layout, offset);
    } else if (layout instanceof ValueLayout.OfShort) {
      return seg.get((ValueLayout.OfShort) layout, offset);
    } else if (layout instanceof ValueLayout.OfInt) {
      return seg.get((ValueLayout.OfInt) layout, offset);
    } else if (layout instanceof ValueLayout.OfFloat) {
      return seg.get((ValueLayout.OfFloat) layout, offset);
    } else if (layout instanceof ValueLayout.OfLong) {
      return seg.get((ValueLayout.OfLong) layout, offset);
    } else if (layout instanceof ValueLayout.OfDouble) {
      return seg.get((ValueLayout.OfDouble) layout, offset);
    } else if (layout instanceof AddressLayout) {
      return seg.get((AddressLayout) layout, offset);
    }
    throw new UnsupportedOperationException("Unsupported layout: " + layout);
  }

  @Override
  public void set(Object... args) {
    MemorySegment seg = (MemorySegment) args[0];
    long offset = computeOffset(args);
    Object val = args[valueIndex()];
    if (layout instanceof ValueLayout.OfByte) {
      seg.set((ValueLayout.OfByte) layout, offset, ((Number) val).byteValue());
    } else if (layout instanceof ValueLayout.OfBoolean) {
      seg.set((ValueLayout.OfBoolean) layout, offset, (Boolean) val);
    } else if (layout instanceof ValueLayout.OfChar) {
      seg.set((ValueLayout.OfChar) layout, offset, (Character) val);
    } else if (layout instanceof ValueLayout.OfShort) {
      seg.set((ValueLayout.OfShort) layout, offset, ((Number) val).shortValue());
    } else if (layout instanceof ValueLayout.OfInt) {
      seg.set((ValueLayout.OfInt) layout, offset, ((Number) val).intValue());
    } else if (layout instanceof ValueLayout.OfFloat) {
      seg.set((ValueLayout.OfFloat) layout, offset, ((Number) val).floatValue());
    } else if (layout instanceof ValueLayout.OfLong) {
      seg.set((ValueLayout.OfLong) layout, offset, ((Number) val).longValue());
    } else if (layout instanceof ValueLayout.OfDouble) {
      seg.set((ValueLayout.OfDouble) layout, offset, ((Number) val).doubleValue());
    } else if (layout instanceof AddressLayout) {
      seg.set((AddressLayout) layout, offset, (MemorySegment) val);
    } else {
      throw new UnsupportedOperationException("Unsupported layout: " + layout);
    }
  }

  @Override
  public boolean compareAndSet(Object... args) {
    int vIdx = valueIndex();
    Object expected = args[vIdx];
    Object update = args[vIdx + 1];
    synchronized (this) {
      Object current = get(args);
      if (Objects.equals(current, expected)) {
        Object[] setArgs = Arrays.copyOf(args, vIdx + 1);
        setArgs[vIdx] = update;
        set(setArgs);
        return true;
      }
      return false;
    }
  }

  @Override
  public Object getAndSet(Object... args) {
    int vIdx = valueIndex();
    Object update = args[vIdx];
    synchronized (this) {
      Object current = get(args);
      Object[] setArgs = Arrays.copyOf(args, vIdx + 1);
      setArgs[vIdx] = update;
      set(setArgs);
      return current;
    }
  }

  @Override
  public Object getAndAdd(Object... args) {
    int vIdx = valueIndex();
    Number delta = (Number) args[vIdx];
    synchronized (this) {
      Object current = get(args);
      if (current instanceof Integer) {
        int next = ((Integer) current) + delta.intValue();
        Object[] setArgs = Arrays.copyOf(args, vIdx + 1);
        setArgs[vIdx] = next;
        set(setArgs);
        return current;
      } else if (current instanceof Long) {
        long next = ((Long) current) + delta.longValue();
        Object[] setArgs = Arrays.copyOf(args, vIdx + 1);
        setArgs[vIdx] = next;
        set(setArgs);
        return current;
      } else {
        throw new UnsupportedOperationException("getAndAdd not supported on " + current.getClass());
      }
    }
  }
}
