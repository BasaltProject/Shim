/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

final class UpcallSupport {
  private static final ConcurrentHashMap<Long, UpcallSupport> kStubs = new ConcurrentHashMap<Long, UpcallSupport>();
  private static final AtomicLong kNextId = new AtomicLong(1);

  private final MethodHandle target;
  private final List<MemoryLayout> args;
  private final MemoryLayout ret;

  private UpcallSupport(MethodHandle target, FunctionDescriptor fd) {
    this.target = target;
    this.args = fd.argumentLayouts();
    this.ret = fd.returnLayout().orElse(null);
  }

  static MemorySegment create(MethodHandle target, FunctionDescriptor fd) {
    if (NativeLinkerImpl.supported0() != 6L) {
      throw new UnsupportedOperationException("Native upcall stubs are only supported on x86-64 Linux");
    }
    if (!target.type().equals(fd.toMethodType())) {
      throw new IllegalArgumentException("Wrong method handle type: " + target.type());
    }
    long id = kNextId.getAndIncrement();
    kStubs.put(id, new UpcallSupport(target, fd));
    sun.misc.Unsafe u = sun.misc.Unsafe.getUnsafe();
    long stub = NativeLinkerImpl.stubAlloc0();
    if (stub == 0L) {
      throw new OutOfMemoryError("Cannot allocate an upcall stub");
    }
    long entry = NativeLinkerImpl.entry0();
    long p = stub;
    u.putByte(p++, (byte) 0x49);
    u.putByte(p++, (byte) 0xBA);
    u.putLong(p, id);
    p += 8;
    u.putByte(p++, (byte) 0x49);
    u.putByte(p++, (byte) 0xBB);
    u.putLong(p, entry);
    p += 8;
    u.putByte(p++, (byte) 0x41);
    u.putByte(p++, (byte) 0xFF);
    u.putByte(p++, (byte) 0xE3);
    return MemorySegment.ofAddress(stub);
  }

  static void upcall(long id, long frame) {
    try {
      UpcallSupport s = kStubs.get(id);
      s.run(frame);
    } catch (Throwable t) {
      System.err.println("Unrecoverable uncaught exception encountered. The VM will now exit");
      t.printStackTrace();
      Runtime.getRuntime().halt(1);
    }
  }

  private static long word(long frame, int index) {
    return sun.misc.Unsafe.getUnsafe().getLong(frame + index * 8L);
  }

  private void run(long frame) throws Throwable {
    sun.misc.Unsafe u = sun.misc.Unsafe.getUnsafe();
    long stackPtr = word(frame, 18);
    int ints = 0;
    int fps = 0;
    int stack = 0;
    boolean structRet = ret instanceof GroupLayout;
    long hidden = 0L;
    int[] retClasses = null;
    if (structRet && ret.byteSize() > 16) {
      hidden = word(frame, ints++);
    }
    Object[] values = new Object[args.size()];
    for (int i = 0; i < values.length; i++) {
      MemoryLayout l = args.get(i);
      if (l instanceof ValueLayout.OfDouble || l instanceof ValueLayout.OfFloat) {
        long bits;
        if (fps < 8) {
          bits = word(frame, 6 + fps++);
        } else {
          bits = u.getLong(stackPtr + stack++ * 8L);
        }
        values[i] = l instanceof ValueLayout.OfDouble ? (Object) Double.longBitsToDouble(bits)
                                                      : (Object) Float.intBitsToFloat((int) bits);
      } else if (l instanceof ValueLayout) {
        long bits;
        if (ints < 6) {
          bits = word(frame, ints++);
        } else {
          bits = u.getLong(stackPtr + stack++ * 8L);
        }
        values[i] = scalar((ValueLayout) l, bits);
      } else {
        long size = l.byteSize();
        int n = (int) ((size + 7) >> 3);
        int[] classes = size > 16 ? null : classesOf(l);
        boolean inRegs = false;
        if (classes != null) {
          int needInt = 0;
          int needFp = 0;
          for (int c : classes) {
            if (c == 1) {
              needInt++;
            } else {
              needFp++;
            }
          }
          inRegs = ints + needInt <= 6 && fps + needFp <= 8;
        }
        MemorySegment seg = Arena.ofAuto().allocate(l);
        for (int k = 0; k < n; k++) {
          long bits;
          if (inRegs) {
            bits = classes[k] == 1 ? word(frame, ints++) : word(frame, 6 + fps++);
          } else {
            bits = u.getLong(stackPtr + stack++ * 8L);
          }
          for (int b = 0; b < 8 && k * 8L + b < size; b++) {
            seg.set(ValueLayout.JAVA_BYTE, k * 8L + b, (byte) (bits >>> (8 * b)));
          }
        }
        values[i] = seg;
      }
    }
    Object r = target.invokeWithArguments(values);
    if (ret == null) {
      return;
    }
    if (ret instanceof ValueLayout.OfDouble) {
      u.putLong(frame + 16 * 8L, Double.doubleToRawLongBits(((Number) r).doubleValue()));
    } else if (ret instanceof ValueLayout.OfFloat) {
      u.putLong(frame + 16 * 8L, Float.floatToRawIntBits(((Number) r).floatValue()) & 0xffffffffL);
    } else if (ret instanceof AddressLayout) {
      u.putLong(frame + 14 * 8L, ((MemorySegment) r).address());
    } else if (ret instanceof ValueLayout) {
      long bits;
      if (r instanceof Boolean) {
        bits = (Boolean) r ? 1L : 0L;
      } else if (r instanceof Character) {
        bits = (Character) r;
      } else {
        bits = ((Number) r).longValue();
      }
      u.putLong(frame + 14 * 8L, bits);
    } else {
      MemorySegment seg = (MemorySegment) r;
      long size = ret.byteSize();
      if (size > 16) {
        MemorySegment.copy(seg, 0, MemorySegment.ofAddress(hidden).reinterpret(size), 0, size);
        u.putLong(frame + 14 * 8L, hidden);
      } else {
        retClasses = classesOf(ret);
        int ri = 0;
        int rf = 0;
        for (int k = 0; k < retClasses.length; k++) {
          long bits = 0;
          for (int b = 0; b < 8 && k * 8L + b < size; b++) {
            bits |= (seg.get(ValueLayout.JAVA_BYTE, k * 8L + b) & 0xffL) << (8 * b);
          }
          if (retClasses[k] == 1) {
            u.putLong(frame + (14 + ri++) * 8L, bits);
          } else {
            u.putLong(frame + (16 + rf++) * 8L, bits);
          }
        }
      }
    }
  }

  private static int[] classesOf(MemoryLayout l) {
    int[] classes = new int[(int) ((l.byteSize() + 7) >> 3)];
    DowncallHandle.classify(l, 0, classes);
    for (int i = 0; i < classes.length; i++) {
      if (classes[i] == 0) {
        classes[i] = 1;
      }
    }
    return classes;
  }

  private static Object scalar(ValueLayout l, long bits) {
    if (l instanceof ValueLayout.OfByte) {
      return (byte) bits;
    } else if (l instanceof ValueLayout.OfBoolean) {
      return (bits & 0xff) != 0;
    } else if (l instanceof ValueLayout.OfChar) {
      return (char) bits;
    } else if (l instanceof ValueLayout.OfShort) {
      return (short) bits;
    } else if (l instanceof ValueLayout.OfInt) {
      return (int) bits;
    } else if (l instanceof ValueLayout.OfLong) {
      return bits;
    }
    MemorySegment s = MemorySegment.ofAddress(bits);
    AddressLayout al = (AddressLayout) l;
    if (al.targetLayout().isPresent() && bits != 0L) {
      return s.reinterpret(al.targetLayout().get().byteSize());
    }
    return s;
  }
}
