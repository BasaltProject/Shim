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
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class ConstantDescsImpl {
  private ConstantDescsImpl() {}

  static final class PrimitiveDesc implements ClassDesc {
    private final String descriptor;

    PrimitiveDesc(String descriptor) {
      this.descriptor = descriptor;
    }

    @Override
    public String descriptorString() {
      return descriptor;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o instanceof ClassDesc) {
        return descriptor.equals(((ClassDesc) o).descriptorString());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return descriptor.hashCode();
    }

    @Override
    public String toString() {
      return "PrimitiveClassDesc[" + displayName() + "]";
    }
  }

  static final class ArrayDesc implements ClassDesc {
    private final String descriptor;

    ArrayDesc(String descriptor) {
      this.descriptor = descriptor;
    }

    @Override
    public String descriptorString() {
      return descriptor;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o instanceof ClassDesc) {
        return descriptor.equals(((ClassDesc) o).descriptorString());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return descriptor.hashCode();
    }

    @Override
    public String toString() {
      int rank = 0;
      while (rank < descriptor.length() && descriptor.charAt(rank) == '[') {
        rank++;
      }
      ClassDesc elem = ClassDesc.ofDescriptor(descriptor.substring(rank));
      return "ArrayClassDesc[" + elem.displayName() + ", " + rank + "]";
    }
  }

  static final class ClassOrInterfaceDesc implements ClassDesc {
    private final String descriptor;

    ClassOrInterfaceDesc(String descriptor) {
      this.descriptor = descriptor;
    }

    @Override
    public String descriptorString() {
      return descriptor;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o instanceof ClassDesc) {
        return descriptor.equals(((ClassDesc) o).descriptorString());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return descriptor.hashCode();
    }

    @Override
    public String toString() {
      return "ClassOrInterfaceDesc[" + displayName() + "]";
    }
  }

  static final class MethodTypeDescImpl implements MethodTypeDesc {
    private final ClassDesc returnType;
    private final List<ClassDesc> params;

    MethodTypeDescImpl(ClassDesc returnType, List<ClassDesc> params) {
      this.returnType = returnType;
      this.params = Collections.unmodifiableList(new ArrayList<ClassDesc>(params));
    }

    @Override
    public ClassDesc returnType() {
      return returnType;
    }

    @Override
    public int parameterCount() {
      return params.size();
    }

    @Override
    public ClassDesc parameterType(int index) {
      return params.get(index);
    }

    @Override
    public List<ClassDesc> parameterList() {
      return params;
    }

    @Override
    public ClassDesc[] parameterArray() {
      return params.toArray(new ClassDesc[params.size()]);
    }

    @Override
    public MethodTypeDesc changeReturnType(ClassDesc newReturnType) {
      Objects.requireNonNull(newReturnType, "newReturnType");
      return new MethodTypeDescImpl(newReturnType, params);
    }

    @Override
    public MethodTypeDesc changeParameterType(int index, ClassDesc paramType) {
      Objects.requireNonNull(paramType, "paramType");
      if (paramType.descriptorString().equals("V")) {
        throw new IllegalArgumentException("Parameter cannot be void");
      }
      List<ClassDesc> list = new ArrayList<ClassDesc>(params);
      list.set(index, paramType);
      return new MethodTypeDescImpl(returnType, list);
    }

    @Override
    public MethodTypeDesc dropParameterTypes(int start, int end) {
      if (start < 0 || end > params.size() || start > end) {
        throw new IndexOutOfBoundsException("Invalid range: " + start + " to " + end);
      }
      List<ClassDesc> list = new ArrayList<ClassDesc>(params.size() - (end - start));
      for (int i = 0; i < start; ++i) {
        list.add(params.get(i));
      }
      for (int i = end; i < params.size(); ++i) {
        list.add(params.get(i));
      }
      return new MethodTypeDescImpl(returnType, list);
    }

    @Override
    public MethodTypeDesc insertParameterTypes(int pos, ClassDesc... paramTypes) {
      if (pos < 0 || pos > params.size()) {
        throw new IndexOutOfBoundsException("Invalid position: " + pos);
      }
      List<ClassDesc> list = new ArrayList<ClassDesc>(params);
      if (paramTypes != null) {
        for (int i = 0; i < paramTypes.length; ++i) {
          ClassDesc p = paramTypes[i];
          Objects.requireNonNull(p, "paramType");
          if (p.descriptorString().equals("V")) {
            throw new IllegalArgumentException("Parameter cannot be void");
          }
          list.add(pos + i, p);
        }
      }
      return new MethodTypeDescImpl(returnType, list);
    }

    @Override
    public String descriptorString() {
      StringBuilder sb = new StringBuilder("(");
      for (ClassDesc p : params) {
        sb.append(p.descriptorString());
      }
      sb.append(')').append(returnType.descriptorString());
      return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o instanceof MethodTypeDesc) {
        return descriptorString().equals(((MethodTypeDesc) o).descriptorString());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return descriptorString().hashCode();
    }

    @Override
    public String toString() {
      return "MethodTypeDesc[" + displayDescriptor() + "]";
    }
  }

  static final class DirectMethodHandleDescImpl implements DirectMethodHandleDesc {
    private final Kind kind;
    private final ClassDesc owner;
    private final String name;
    private final MethodTypeDesc type;

    DirectMethodHandleDescImpl(Kind kind, ClassDesc owner, String name, MethodTypeDesc type) {
      this.kind = kind;
      this.owner = owner;
      this.name = name;
      this.type = type;
    }

    @Override
    public Kind kind() {
      return kind;
    }

    @Override
    public int refKind() {
      return kind.refKind;
    }

    @Override
    public boolean isOwnerInterface() {
      return kind.isInterface;
    }

    @Override
    public ClassDesc owner() {
      return owner;
    }

    @Override
    public String methodName() {
      return name;
    }

    @Override
    public MethodTypeDesc invocationType() {
      switch (kind) {
        case VIRTUAL:
        case INTERFACE_VIRTUAL:
        case SPECIAL:
        case INTERFACE_SPECIAL:
          if (type.parameterCount() == 0 || !type.parameterType(0).equals(owner)) {
            return type.insertParameterTypes(0, owner);
          }
          return type;
        case CONSTRUCTOR:
          return type;
        default:
          return type;
      }
    }

    @Override
    public MethodHandle resolveConstantDesc(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
      Objects.requireNonNull(lookup, "lookup");
      Class<?> ownerClass = owner.resolveConstantDesc(lookup);
      switch (kind) {
        case STATIC:
        case INTERFACE_STATIC: {
          MethodType mt = type.resolveConstantDesc(lookup);
          return lookup.findStatic(ownerClass, name, mt);
        }
        case VIRTUAL:
        case INTERFACE_VIRTUAL: {
          MethodTypeDesc baseType = type;
          if (baseType.parameterCount() > 0 && baseType.parameterType(0).equals(owner)) {
            baseType = baseType.dropParameterTypes(0, 1);
          }
          MethodType mt = baseType.resolveConstantDesc(lookup);
          return lookup.findVirtual(ownerClass, name, mt);
        }
        case SPECIAL:
        case INTERFACE_SPECIAL: {
          MethodTypeDesc baseType = type;
          if (baseType.parameterCount() > 0 && baseType.parameterType(0).equals(owner)) {
            baseType = baseType.dropParameterTypes(0, 1);
          }
          MethodType mt = baseType.resolveConstantDesc(lookup);
          return lookup.findSpecial(ownerClass, name, mt, ownerClass);
        }
        case CONSTRUCTOR: {
          MethodTypeDesc baseType = type.changeReturnType(ConstantDescs.CD_void);
          MethodType mt = baseType.resolveConstantDesc(lookup);
          return lookup.findConstructor(ownerClass, mt);
        }
        case GETTER: {
          Class<?> fieldType = type.returnType().resolveConstantDesc(lookup);
          return lookup.findGetter(ownerClass, name, fieldType);
        }
        case SETTER: {
          Class<?> fieldType = type.parameterType(1).resolveConstantDesc(lookup);
          return lookup.findSetter(ownerClass, name, fieldType);
        }
        case STATIC_GETTER: {
          Class<?> fieldType = type.returnType().resolveConstantDesc(lookup);
          return lookup.findStaticGetter(ownerClass, name, fieldType);
        }
        case STATIC_SETTER: {
          Class<?> fieldType = type.parameterType(0).resolveConstantDesc(lookup);
          return lookup.findStaticSetter(ownerClass, name, fieldType);
        }
        default:
          throw new IllegalArgumentException("Unsupported kind: " + kind);
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof DirectMethodHandleDesc)) return false;
      DirectMethodHandleDesc that = (DirectMethodHandleDesc) o;
      return kind == that.kind()
          && owner.equals(that.owner())
          && name.equals(that.methodName())
          && invocationType().equals(that.invocationType());
    }

    @Override
    public int hashCode() {
      int res = kind.hashCode();
      res = 31 * res + owner.hashCode();
      res = 31 * res + name.hashCode();
      res = 31 * res + invocationType().hashCode();
      return res;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("MethodHandleDesc[");
      sb.append(kind.name()).append('/').append(owner.displayName()).append("::").append(name);
      MethodTypeDesc it = invocationType();
      sb.append('(');
      List<ClassDesc> params = it.parameterList();
      for (int i = 0; i < params.size(); ++i) {
        if (i > 0) sb.append(',');
        sb.append(params.get(i).displayName());
      }
      sb.append(')');
      sb.append(it.returnType().displayName()).append(']');
      return sb.toString();
    }
  }

  static final class AsTypeMethodHandleDesc implements MethodHandleDesc {
    private final MethodHandleDesc delegate;
    private final MethodTypeDesc type;

    AsTypeMethodHandleDesc(MethodHandleDesc delegate, MethodTypeDesc type) {
      this.delegate = delegate;
      this.type = type;
    }

    @Override
    public MethodTypeDesc invocationType() {
      return type;
    }

    @Override
    public MethodHandle resolveConstantDesc(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
      MethodHandle mh = delegate.resolveConstantDesc(lookup);
      MethodType mt = type.resolveConstantDesc(lookup);
      return mh.asType(mt);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof MethodHandleDesc)) return false;
      MethodHandleDesc that = (MethodHandleDesc) o;
      return invocationType().equals(that.invocationType());
    }

    @Override
    public int hashCode() {
      return type.hashCode();
    }

    @Override
    public String toString() {
      return "MethodHandleDesc[" + delegate + " asType " + type + "]";
    }
  }
}
