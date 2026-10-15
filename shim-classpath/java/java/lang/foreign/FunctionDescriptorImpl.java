/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.foreign;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

final class FunctionDescriptorImpl implements FunctionDescriptor {
  private final MemoryLayout returnLayout;
  private final List<MemoryLayout> argumentLayouts;

  FunctionDescriptorImpl(MemoryLayout returnLayout, List<MemoryLayout> argumentLayouts) {
    this.returnLayout = returnLayout;
    this.argumentLayouts = Collections.unmodifiableList(Objects.requireNonNull(argumentLayouts, "argumentLayouts"));
  }

  static FunctionDescriptor of(MemoryLayout returnLayout, MemoryLayout... argumentLayouts) {
    Objects.requireNonNull(returnLayout, "returnLayout");
    Objects.requireNonNull(argumentLayouts, "argumentLayouts");
    List<MemoryLayout> args = new ArrayList<MemoryLayout>(argumentLayouts.length);
    for (MemoryLayout m : argumentLayouts) {
      args.add(Objects.requireNonNull(m, "argumentLayout"));
    }
    return new FunctionDescriptorImpl(returnLayout, args);
  }

  static FunctionDescriptor ofVoid(MemoryLayout... argumentLayouts) {
    Objects.requireNonNull(argumentLayouts, "argumentLayouts");
    List<MemoryLayout> args = new ArrayList<MemoryLayout>(argumentLayouts.length);
    for (MemoryLayout m : argumentLayouts) {
      args.add(Objects.requireNonNull(m, "argumentLayout"));
    }
    return new FunctionDescriptorImpl(null, args);
  }

  @Override
  public Optional<MemoryLayout> returnLayout() {
    return Optional.ofNullable(returnLayout);
  }

  @Override
  public List<MemoryLayout> argumentLayouts() {
    return argumentLayouts;
  }

  @Override
  public FunctionDescriptor appendArgumentLayouts(MemoryLayout... addedLayouts) {
    Objects.requireNonNull(addedLayouts, "addedLayouts");
    List<MemoryLayout> args = new ArrayList<MemoryLayout>(argumentLayouts);
    for (MemoryLayout m : addedLayouts) {
      args.add(Objects.requireNonNull(m, "addedLayout"));
    }
    return new FunctionDescriptorImpl(returnLayout, args);
  }

  @Override
  public FunctionDescriptor insertArgumentLayouts(int index, MemoryLayout... addedLayouts) {
    Objects.requireNonNull(addedLayouts, "addedLayouts");
    if (index < 0 || index > argumentLayouts.size()) {
      throw new IndexOutOfBoundsException("Index: " + index + ", size: " + argumentLayouts.size());
    }
    List<MemoryLayout> args = new ArrayList<MemoryLayout>(argumentLayouts);
    for (int i = 0; i < addedLayouts.length; i++) {
      args.add(index + i, Objects.requireNonNull(addedLayouts[i], "addedLayout"));
    }
    return new FunctionDescriptorImpl(returnLayout, args);
  }

  @Override
  public FunctionDescriptor changeReturnLayout(MemoryLayout newReturnLayout) {
    Objects.requireNonNull(newReturnLayout, "newReturnLayout");
    return new FunctionDescriptorImpl(newReturnLayout, argumentLayouts);
  }

  @Override
  public FunctionDescriptor dropReturnLayout() {
    return new FunctionDescriptorImpl(null, argumentLayouts);
  }

  private static Class<?> carrierOf(MemoryLayout layout) {
    if (layout instanceof ValueLayout) {
      return ((ValueLayout) layout).carrier();
    }
    return MemorySegment.class;
  }

  @Override
  public MethodType toMethodType() {
    Class<?> rtype = returnLayout != null ? carrierOf(returnLayout) : void.class;
    Class<?>[] ptypes = new Class<?>[argumentLayouts.size()];
    for (int i = 0; i < ptypes.length; i++) {
      ptypes[i] = carrierOf(argumentLayouts.get(i));
    }
    return MethodType.methodType(rtype, ptypes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("(");
    for (MemoryLayout arg : argumentLayouts) {
      sb.append(arg);
    }
    sb.append(")");
    if (returnLayout != null) {
      sb.append(returnLayout);
    } else {
      sb.append("v");
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof FunctionDescriptor)) return false;
    FunctionDescriptor other = (FunctionDescriptor) obj;
    return returnLayout().equals(other.returnLayout())
        && argumentLayouts.equals(other.argumentLayouts());
  }

  @Override
  public int hashCode() {
    return Objects.hash(returnLayout, argumentLayouts);
  }
}
