/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DynamicConstantDesc<T> implements ConstantDesc {
  private final DirectMethodHandleDesc bootstrapMethod;
  private final String constantName;
  private final ClassDesc constantType;
  private final ConstantDesc[] bootstrapArgs;

  protected DynamicConstantDesc(DirectMethodHandleDesc bootstrapMethod,
                                String constantName,
                                ClassDesc constantType,
                                ConstantDesc[] bootstrapArgs) {
    this.bootstrapMethod = Objects.requireNonNull(bootstrapMethod, "bootstrapMethod");
    this.constantName = Objects.requireNonNull(constantName, "constantName");
    this.constantType = Objects.requireNonNull(constantType, "constantType");
    this.bootstrapArgs = bootstrapArgs != null ? bootstrapArgs.clone() : new ConstantDesc[0];
  }

  public static <T> DynamicConstantDesc<T> of(DirectMethodHandleDesc bootstrapMethod, ConstantDesc... bootstrapArgs) {
    return ofNamed(bootstrapMethod, ConstantDescs.DEFAULT_NAME, bootstrapMethod.invocationType().returnType(), bootstrapArgs);
  }

  public static <T> DynamicConstantDesc<T> of(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
    return ofNamed(bootstrapMethod, constantName, constantType, bootstrapArgs);
  }

  public static <T> DynamicConstantDesc<T> ofNamed(DirectMethodHandleDesc bootstrapMethod, String constantName, ClassDesc constantType, ConstantDesc... bootstrapArgs) {
    return new DynamicConstantDesc<T>(bootstrapMethod, constantName, constantType, bootstrapArgs);
  }

  @SuppressWarnings("unchecked")
  public static <T> ConstantDesc ofCanonical(DirectMethodHandleDesc bootstrapMethod, String constantName,
      ClassDesc constantType, ConstantDesc[] bootstrapArgs) {
    Objects.requireNonNull(bootstrapMethod, "bootstrapMethod");
    Objects.requireNonNull(constantName, "constantName");
    Objects.requireNonNull(constantType, "constantType");
    Objects.requireNonNull(bootstrapArgs, "bootstrapArgs");
    if (bootstrapMethod.equals(ConstantDescs.BSM_NULL_CONSTANT) && bootstrapArgs.length == 0) {
      return ConstantDescs.NULL;
    }
    if (bootstrapMethod.equals(ConstantDescs.BSM_PRIMITIVE_CLASS) && bootstrapArgs.length == 0
        && constantType.equals(ConstantDescs.CD_Class)) {
      return ClassDesc.ofDescriptor(constantName);
    }
    if (bootstrapMethod.equals(ConstantDescs.BSM_ENUM_CONSTANT) && bootstrapArgs.length == 0) {
      return java.lang.Enum.EnumDesc.of(constantType, constantName);
    }
    if (bootstrapMethod.equals(ConstantDescs.BSM_VARHANDLE_STATIC_FIELD) && bootstrapArgs.length == 2
        && constantType.equals(ConstantDescs.CD_VarHandle)) {
      return java.lang.invoke.VarHandle.VarHandleDesc.ofStaticField((ClassDesc) bootstrapArgs[0], constantName,
          (ClassDesc) bootstrapArgs[1]);
    }
    if (bootstrapMethod.equals(ConstantDescs.BSM_VARHANDLE_FIELD) && bootstrapArgs.length == 2
        && constantType.equals(ConstantDescs.CD_VarHandle)) {
      return java.lang.invoke.VarHandle.VarHandleDesc.ofField((ClassDesc) bootstrapArgs[0], constantName,
          (ClassDesc) bootstrapArgs[1]);
    }
    if (bootstrapMethod.equals(ConstantDescs.BSM_VARHANDLE_ARRAY) && bootstrapArgs.length == 1
        && constantType.equals(ConstantDescs.CD_VarHandle)) {
      return java.lang.invoke.VarHandle.VarHandleDesc.ofArray((ClassDesc) bootstrapArgs[0]);
    }
    return ofNamed(bootstrapMethod, constantName, constantType, bootstrapArgs);
  }

  public DirectMethodHandleDesc bootstrapMethod() {
    return bootstrapMethod;
  }

  public String constantName() {
    return constantName;
  }

  public ClassDesc constantType() {
    return constantType;
  }

  public ConstantDesc[] bootstrapArgs() {
    return bootstrapArgs.clone();
  }

  public List<ConstantDesc> bootstrapArgsList() {
    return Collections.unmodifiableList(Arrays.asList(bootstrapArgs));
  }

  @Override
  @SuppressWarnings("unchecked")
  public T resolveConstantDesc(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
    Objects.requireNonNull(lookup, "lookup");
    MethodHandle bsm = bootstrapMethod.resolveConstantDesc(lookup);
    Class<?> type = constantType.resolveConstantDesc(lookup);

    List<Object> args = new ArrayList<Object>(3 + bootstrapArgs.length);
    args.add(lookup);
    args.add(constantName);
    args.add(type);
    for (ConstantDesc argDesc : bootstrapArgs) {
      if (argDesc == null) {
        args.add(null);
      } else {
        args.add(argDesc.resolveConstantDesc(lookup));
      }
    }
    try {
      return (T) bsm.invokeWithArguments(args);
    } catch (ReflectiveOperationException e) {
      throw e;
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new ReflectiveOperationException(t.getMessage(), t);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DynamicConstantDesc)) return false;
    DynamicConstantDesc<?> that = (DynamicConstantDesc<?>) o;
    return bootstrapMethod.equals(that.bootstrapMethod)
        && constantName.equals(that.constantName)
        && constantType.equals(that.constantType)
        && Arrays.equals(bootstrapArgs, that.bootstrapArgs);
  }

  @Override
  public int hashCode() {
    int res = bootstrapMethod.hashCode();
    res = 31 * res + constantName.hashCode();
    res = 31 * res + constantType.hashCode();
    res = 31 * res + Arrays.hashCode(bootstrapArgs);
    return res;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DynamicConstantDesc[");
    sb.append(bootstrapMethod.owner().displayName()).append("::").append(bootstrapMethod.methodName()).append('(');
    boolean hasName = !constantName.equals(ConstantDescs.DEFAULT_NAME);
    if (hasName) {
      sb.append(constantName).append('/');
    }
    for (int i = 0; i < bootstrapArgs.length; ++i) {
      if (i > 0) sb.append(',');
      sb.append(bootstrapArgs[i]);
    }
    sb.append(')').append(constantType.displayName()).append(']');
    return sb.toString();
  }
}
