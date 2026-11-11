/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

final class DowncallHandle extends MethodHandle {
  static final int kFpRegs = 8;
  static final int kMaxStack = 32;
  static final int kRetInt = 16;
  static final int kRetFp = 18;
  static final int kStackBase = 24;
  static final int kSlots = kStackBase + kMaxStack;

  private final MemorySegment address;
  private final List<MemoryLayout> args;
  private final MemoryLayout ret;
  private final int intRegs;
  private final boolean x86;
  private final boolean captureErrno;

  DowncallHandle(MemorySegment address, FunctionDescriptor fd, Linker.Option... options) {
    super(type(address, fd, captures(options)));
    long regs = NativeLinkerImpl.supported0();
    if (regs == 0L) {
      throw new UnsupportedOperationException("Native downcalls are not supported on this platform");
    }
    this.intRegs = (int) regs;
    this.x86 = regs == 6L;
    this.address = address;
    this.captureErrno = captures(options);
    this.args = fd.argumentLayouts();
    this.ret = fd.returnLayout().orElse(null);
    for (MemoryLayout l : args) {
      check(l);
    }
    if (ret != null) {
      check(ret);
    }
  }

  private void check(MemoryLayout l) {
    if (l instanceof ValueLayout) {
      return;
    }
    if (l instanceof GroupLayout && x86) {
      return;
    }
    throw new UnsupportedOperationException("Unsupported layout on this platform: " + l);
  }

  private static boolean captures(Linker.Option[] options) {
    for (Linker.Option o : options) {
      if (o instanceof NativeLinkerImpl.CaptureCallStateOption
          && ((NativeLinkerImpl.CaptureCallStateOption) o).capturedState.length > 0) {
        return true;
      }
    }
    return false;
  }

  private static MethodType type(MemorySegment address, FunctionDescriptor fd, boolean capture) {
    MethodType t = fd.toMethodType();
    if (capture) {
      t = t.insertParameterTypes(0, MemorySegment.class);
    }
    if (fd.returnLayout().isPresent() && fd.returnLayout().get() instanceof GroupLayout) {
      t = t.insertParameterTypes(0, SegmentAllocator.class);
    }
    return address == null ? t.insertParameterTypes(0, MemorySegment.class) : t;
  }

  static void classify(MemoryLayout l, long base, int[] classes) {
    if (l instanceof ValueLayout) {
      boolean fp = l instanceof ValueLayout.OfFloat || l instanceof ValueLayout.OfDouble;
      int idx = (int) (base >> 3);
      if (idx < classes.length) {
        classes[idx] = fp ? (classes[idx] == 0 ? 2 : classes[idx]) : 1;
      }
    } else if (l instanceof SequenceLayout) {
      SequenceLayout s = (SequenceLayout) l;
      for (long i = 0; i < s.elementCount(); i++) {
        classify(s.elementLayout(), base + i * s.elementLayout().byteSize(), classes);
      }
    } else if (l instanceof UnionLayout) {
      for (MemoryLayout m : ((GroupLayout) l).memberLayouts()) {
        classify(m, base, classes);
      }
    } else if (l instanceof GroupLayout) {
      long off = base;
      for (MemoryLayout m : ((GroupLayout) l).memberLayouts()) {
        classify(m, off, classes);
        off += m.byteSize();
      }
    }
  }

  private static int[] classesOf(MemoryLayout l) {
    int[] classes = new int[(int) ((l.byteSize() + 7) >> 3)];
    classify(l, 0, classes);
    for (int i = 0; i < classes.length; i++) {
      if (classes[i] == 0) {
        classes[i] = 1;
      }
    }
    return classes;
  }

  private static long eightbyte(MemorySegment seg, long size, int k) {
    long v = 0;
    for (int b = 0; b < 8; b++) {
      long idx = k * 8L + b;
      if (idx < size) {
        v |= (seg.get(ValueLayout.JAVA_BYTE, idx) & 0xffL) << (8 * b);
      }
    }
    return v;
  }

  @Override
  public Object invokeWithArguments(Object... arguments) throws Throwable {
    sun.misc.Unsafe u = sun.misc.Unsafe.getUnsafe();
    int offset = 0;
    long fn;
    if (address == null) {
      fn = ((MemorySegment) arguments[0]).address();
      offset = 1;
    } else {
      fn = address.address();
    }
    SegmentAllocator allocator = null;
    boolean structRet = ret instanceof GroupLayout;
    if (structRet) {
      allocator = (SegmentAllocator) arguments[offset++];
    }
    MemorySegment captureSeg = null;
    if (captureErrno) {
      captureSeg = (MemorySegment) arguments[offset++];
    }
    if (fn == 0L) {
      throw new IllegalArgumentException("Symbol is NULL: " + MemorySegment.ofAddress(0L));
    }
    if (arguments.length - offset != args.size()) {
      throw new java.lang.invoke.WrongMethodTypeException("expected " + args.size() + " arguments but got "
          + (arguments.length - offset));
    }
    long buf = u.allocateMemory(kSlots * 8L);
    try {
      for (int i = 0; i < kSlots; i++) {
        u.putLong(buf + i * 8L, 0L);
      }
      int ints = 0;
      int fps = 0;
      int stack = 0;
      MemorySegment retSeg = null;
      int[] retClasses = null;
      int kind = 0;
      if (structRet) {
        long size = ret.byteSize();
        retSeg = allocator.allocate(ret);
        if (size > 16) {
          u.putLong(buf + ints++ * 8L, retSeg.address());
        } else {
          retClasses = classesOf(ret);
          if (retClasses.length == 1) {
            kind = retClasses[0] == 2 ? 1 : 0;
          } else {
            kind = retClasses[0] == 1 ? (retClasses[1] == 1 ? 2 : 4) : (retClasses[1] == 1 ? 5 : 3);
          }
        }
      } else if (ret instanceof ValueLayout.OfFloat || ret instanceof ValueLayout.OfDouble) {
        kind = 1;
      }
      for (int i = 0; i < args.size(); i++) {
        MemoryLayout l = args.get(i);
        Object v = arguments[i + offset];
        if (l instanceof ValueLayout.OfDouble) {
          long bits = Double.doubleToRawLongBits(((Number) v).doubleValue());
          if (fps < kFpRegs) {
            u.putLong(buf + (8 + fps++) * 8L, bits);
          } else {
            stack = pushStack(u, buf, stack, bits);
          }
        } else if (l instanceof ValueLayout.OfFloat) {
          long bits = Float.floatToRawIntBits(((Number) v).floatValue()) & 0xffffffffL;
          if (fps < kFpRegs) {
            u.putLong(buf + (8 + fps++) * 8L, bits);
          } else {
            stack = pushStack(u, buf, stack, bits);
          }
        } else if (l instanceof ValueLayout) {
          long bits = integer(l, v);
          if (ints < intRegs) {
            u.putLong(buf + ints++ * 8L, bits);
          } else {
            stack = pushStack(u, buf, stack, bits);
          }
        } else {
          MemorySegment seg = (MemorySegment) v;
          long size = l.byteSize();
          if (seg.byteSize() < size) {
            throw new IndexOutOfBoundsException("Segment too small for " + l);
          }
          int n = (int) ((size + 7) >> 3);
          int[] classes = size > 16 ? null : classesOf(l);
          int needInt = 0;
          int needFp = 0;
          if (classes != null) {
            for (int c : classes) {
              if (c == 1) {
                needInt++;
              } else {
                needFp++;
              }
            }
          }
          if (classes != null && ints + needInt <= intRegs && fps + needFp <= kFpRegs) {
            for (int k = 0; k < n; k++) {
              long bits = eightbyte(seg, size, k);
              if (classes[k] == 1) {
                u.putLong(buf + ints++ * 8L, bits);
              } else {
                u.putLong(buf + (8 + fps++) * 8L, bits);
              }
            }
          } else {
            for (int k = 0; k < n; k++) {
              stack = pushStack(u, buf, stack, eightbyte(seg, size, k));
            }
          }
        }
      }
      NativeLinkerImpl.call0(fn, buf, kind);
      if (captureSeg != null) {
        captureSeg.set(ValueLayout.JAVA_INT, 0L, (int) u.getLong(buf + 20 * 8L));
      }
      if (structRet) {
        if (ret.byteSize() <= 16) {
          int intIdx = 0;
          int fpIdx = 0;
          long size = ret.byteSize();
          for (int k = 0; k < retClasses.length; k++) {
            long bits = retClasses[k] == 1 ? u.getLong(buf + (kRetInt + intIdx++) * 8L)
                                           : u.getLong(buf + (kRetFp + fpIdx++) * 8L);
            for (int b = 0; b < 8 && k * 8L + b < size; b++) {
              retSeg.set(ValueLayout.JAVA_BYTE, k * 8L + b, (byte) (bits >>> (8 * b)));
            }
          }
        }
        return retSeg;
      }
      return result(u, buf, kind == 1);
    } finally {
      u.freeMemory(buf);
    }
  }

  private static int pushStack(sun.misc.Unsafe u, long buf, int stack, long bits) {
    if (stack >= kMaxStack) {
      throw new UnsupportedOperationException("Too many stack arguments for a native downcall");
    }
    u.putLong(buf + (kStackBase + stack) * 8L, bits);
    return stack + 1;
  }

  private static long integer(MemoryLayout l, Object v) {
    if (l instanceof AddressLayout) {
      MemorySegment s = (MemorySegment) v;
      if (!s.isNative()) {
        throw new IllegalArgumentException("Heap segment not allowed: " + s);
      }
      return s.address();
    }
    if (v instanceof Boolean) {
      return (Boolean) v ? 1L : 0L;
    }
    if (v instanceof Character) {
      return (Character) v;
    }
    return ((Number) v).longValue();
  }

  private Object result(sun.misc.Unsafe u, long buf, boolean fpRet) {
    if (ret == null) {
      return null;
    }
    long raw = u.getLong(buf + kRetInt * 8L);
    if (ret instanceof ValueLayout.OfDouble) {
      return u.getDouble(buf + kRetFp * 8L);
    } else if (ret instanceof ValueLayout.OfFloat) {
      return Float.intBitsToFloat((int) u.getLong(buf + kRetFp * 8L));
    } else if (ret instanceof ValueLayout.OfByte) {
      return (byte) raw;
    } else if (ret instanceof ValueLayout.OfBoolean) {
      return (raw & 0xff) != 0;
    } else if (ret instanceof ValueLayout.OfChar) {
      return (char) raw;
    } else if (ret instanceof ValueLayout.OfShort) {
      return (short) raw;
    } else if (ret instanceof ValueLayout.OfInt) {
      return (int) raw;
    } else if (ret instanceof ValueLayout.OfLong) {
      return raw;
    }
    MemorySegment s = MemorySegment.ofAddress(raw);
    AddressLayout al = (AddressLayout) ret;
    if (al.targetLayout().isPresent() && raw != 0L) {
      return s.reinterpret(al.targetLayout().get().byteSize());
    }
    return s;
  }
}
