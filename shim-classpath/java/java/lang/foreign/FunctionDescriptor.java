/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;

public interface FunctionDescriptor {
  Optional<MemoryLayout> returnLayout();

  List<MemoryLayout> argumentLayouts();

  FunctionDescriptor appendArgumentLayouts(MemoryLayout... addedLayouts);

  FunctionDescriptor insertArgumentLayouts(int index, MemoryLayout... addedLayouts);

  FunctionDescriptor changeReturnLayout(MemoryLayout newReturnLayout);

  FunctionDescriptor dropReturnLayout();

  MethodType toMethodType();

  static FunctionDescriptor of(MemoryLayout returnLayout, MemoryLayout... argumentLayouts) {
    return FunctionDescriptorImpl.of(returnLayout, argumentLayouts);
  }

  static FunctionDescriptor ofVoid(MemoryLayout... argumentLayouts) {
    return FunctionDescriptorImpl.ofVoid(argumentLayouts);
  }
}
