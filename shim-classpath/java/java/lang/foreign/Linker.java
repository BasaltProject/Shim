/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Objects;

public interface Linker {
  static Linker nativeLinker() {
    return NativeLinkerImpl.INSTANCE;
  }

  MethodHandle downcallHandle(MemorySegment address, FunctionDescriptor function, Linker.Option... options);

  MethodHandle downcallHandle(FunctionDescriptor function, Linker.Option... options);

  MemorySegment upcallStub(MethodHandle target, FunctionDescriptor function, Arena arena, Linker.Option... options);

  SymbolLookup defaultLookup();

  Map<String, MemoryLayout> canonicalLayouts();

  interface Option {
    static Option firstVariadicArg(int index) {
      if (index < 0) {
        throw new IllegalArgumentException("Negative index: " + index);
      }
      return new NativeLinkerImpl.FirstVariadicArgOption(index);
    }

    static Option captureCallState(String... capturedState) {
      Objects.requireNonNull(capturedState, "capturedState");
      return new NativeLinkerImpl.CaptureCallStateOption(capturedState);
    }

    static StructLayout captureStateLayout() {
      return NativeLinkerImpl.CAPTURE_STATE_LAYOUT;
    }

    static Option critical(boolean allowHeapAccess) {
      return new NativeLinkerImpl.CriticalOption(allowHeapAccess);
    }
  }
}
