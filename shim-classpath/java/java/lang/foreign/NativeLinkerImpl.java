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

  static final SymbolLookup LOADER_LOOKUP = new SymbolLookup() {
    @Override
    public Optional<MemorySegment> find(String name) {
      Objects.requireNonNull(name, "name");
      return Optional.empty();
    }
  };

  private static final SymbolLookup DEFAULT_LOOKUP = new SymbolLookup() {
    @Override
    public Optional<MemorySegment> find(String name) {
      Objects.requireNonNull(name, "name");
      return Optional.empty();
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
    throw new UnsupportedOperationException("Native downcall handles are not implemented in this environment");
  }

  @Override
  public MethodHandle downcallHandle(FunctionDescriptor function, Linker.Option... options) {
    Objects.requireNonNull(function, "function");
    throw new UnsupportedOperationException("Native downcall handles are not implemented in this environment");
  }

  @Override
  public MemorySegment upcallStub(MethodHandle target, FunctionDescriptor function, Arena arena, Linker.Option... options) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(function, "function");
    Objects.requireNonNull(arena, "arena");
    throw new UnsupportedOperationException("Native upcall stubs are not implemented in this environment");
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
    try {
      System.loadLibrary(name);
    } catch (Throwable ignored) {
    }
    return new SymbolLookup() {
      @Override
      public Optional<MemorySegment> find(String sym) {
        Objects.requireNonNull(sym, "sym");
        return Optional.empty();
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
