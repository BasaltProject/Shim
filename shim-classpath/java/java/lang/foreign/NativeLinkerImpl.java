/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.MethodHandle;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class NativeLinkerImpl implements Linker {
  static final NativeLinkerImpl INSTANCE = new NativeLinkerImpl();

  static final StructLayout CAPTURE_STATE_LAYOUT = MemoryLayout.structLayout(
      ValueLayout.JAVA_INT.withName("error"),
      MemoryLayout.paddingLayout(4L)
  );

  static native long dlopen0(long path);

  static native long dlsym0(long handle, long name);

  static native long supported0();

  static native void call0(long function, long buffer, long kind);

  static native long stubAlloc0();

  static native long entry0();

  static void upcall(long id, long frame) {
    UpcallSupport.upcall(id, frame);
  }

  static Optional<MemorySegment> lookup(long handle, String name) {
    Objects.requireNonNull(name, "name");
    sun.misc.Unsafe u = sun.misc.Unsafe.getUnsafe();
    long cs = cstring(name);
    try {
      long addr = dlsym0(handle, cs);
      return addr == 0L ? Optional.empty() : Optional.of(MemorySegment.ofAddress(addr));
    } finally {
      u.freeMemory(cs);
    }
  }

  static long cstring(String s) {
    byte[] b = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    sun.misc.Unsafe u = sun.misc.Unsafe.getUnsafe();
    long p = u.allocateMemory(b.length + 1L);
    for (int i = 0; i < b.length; i++) {
      u.putByte(p + i, b[i]);
    }
    u.putByte(p + b.length, (byte) 0);
    return p;
  }

  static final SymbolLookup LOADER_LOOKUP = new SymbolLookup() {
    @Override
    public Optional<MemorySegment> find(String name) {
      return lookup(0L, name);
    }
  };

  private static final SymbolLookup DEFAULT_LOOKUP = new SymbolLookup() {
    @Override
    public Optional<MemorySegment> find(String name) {
      return lookup(0L, name);
    }
  };

  private final Map<String, MemoryLayout> canonicalLayouts;

  private NativeLinkerImpl() {
    Map<String, MemoryLayout> map = new HashMap<String, MemoryLayout>();
    map.put("bool", ValueLayout.JAVA_BOOLEAN);
    map.put("char", ValueLayout.JAVA_BYTE);
    map.put("short", ValueLayout.JAVA_SHORT);
    map.put("int", ValueLayout.JAVA_INT);
    map.put("long", ValueLayout.JAVA_LONG);
    map.put("long long", ValueLayout.JAVA_LONG);
    map.put("float", ValueLayout.JAVA_FLOAT);
    map.put("double", ValueLayout.JAVA_DOUBLE);
    map.put("void*", ValueLayout.ADDRESS);
    map.put("size_t", ValueLayout.JAVA_LONG);
    map.put("int8_t", ValueLayout.JAVA_BYTE);
    map.put("int16_t", ValueLayout.JAVA_SHORT);
    map.put("int32_t", ValueLayout.JAVA_INT);
    map.put("int64_t", ValueLayout.JAVA_LONG);
    map.put("wchar_t", ValueLayout.JAVA_INT);
    map.put("jboolean", ValueLayout.JAVA_BOOLEAN);
    map.put("jbyte", ValueLayout.JAVA_BYTE);
    map.put("jchar", ValueLayout.JAVA_CHAR);
    map.put("jshort", ValueLayout.JAVA_SHORT);
    map.put("jint", ValueLayout.JAVA_INT);
    map.put("jlong", ValueLayout.JAVA_LONG);
    map.put("jfloat", ValueLayout.JAVA_FLOAT);
    map.put("jdouble", ValueLayout.JAVA_DOUBLE);
    this.canonicalLayouts = Collections.unmodifiableMap(map);
  }

  @Override
  public MethodHandle downcallHandle(MemorySegment address, FunctionDescriptor function, Linker.Option... options) {
    Objects.requireNonNull(address, "address");
    Objects.requireNonNull(function, "function");
    return new DowncallHandle(address, function, options);
  }

  @Override
  public MethodHandle downcallHandle(FunctionDescriptor function, Linker.Option... options) {
    Objects.requireNonNull(function, "function");
    return new DowncallHandle(null, function, options);
  }

  @Override
  public MemorySegment upcallStub(MethodHandle target, FunctionDescriptor function, Arena arena, Linker.Option... options) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(function, "function");
    Objects.requireNonNull(arena, "arena");
    return UpcallSupport.create(target, function);
  }

  @Override
  public SymbolLookup defaultLookup() {
    return DEFAULT_LOOKUP;
  }

  @Override
  public Map<String, MemoryLayout> canonicalLayouts() {
    return canonicalLayouts;
  }

  static SymbolLookup libraryLookup(String name, Arena arena) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(arena, "arena");
    long cs = cstring(name);
    long handle;
    try {
      handle = dlopen0(cs);
    } finally {
      sun.misc.Unsafe.getUnsafe().freeMemory(cs);
    }
    if (handle == 0L) {
      throw new IllegalArgumentException("Cannot open library: " + name);
    }
    final long h = handle;
    return new SymbolLookup() {
      @Override
      public Optional<MemorySegment> find(String sym) {
        return lookup(h, sym);
      }
    };
  }

  static final class FirstVariadicArgOption implements Linker.Option {
    final int index;

    FirstVariadicArgOption(int index) {
      this.index = index;
    }
  }

  static final class CaptureCallStateOption implements Linker.Option {
    final String[] capturedState;

    CaptureCallStateOption(String[] capturedState) {
      this.capturedState = capturedState;
    }
  }

  static final class CriticalOption implements Linker.Option {
    final boolean allowHeapAccess;

    CriticalOption(boolean allowHeapAccess) {
      this.allowHeapAccess = allowHeapAccess;
    }
  }
}
