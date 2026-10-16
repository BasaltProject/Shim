/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2016, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2016, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.invoke;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MethodHandles {
  public static Lookup lookup() {
    return new Lookup(java.lang.reflect.Method.getCaller().class_, 0x5f);
  }

  public static Lookup publicLookup() {
    return new Lookup(shim.Classes.toVMClass(Object.class), 0x20);
  }

  public static VarHandle arrayElementVarHandle(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return VarHandle.makeArrayHandle(arrayClass);
  }

  public static VarHandle byteArrayViewVarHandle(Class<?> viewArrayClass, ByteOrder byteOrder) {
    Objects.requireNonNull(viewArrayClass, "viewArrayClass");
    Objects.requireNonNull(byteOrder, "byteOrder");
    if (!viewArrayClass.isArray() || viewArrayClass == byte[].class || viewArrayClass == boolean[].class
        || !viewArrayClass.getComponentType().isPrimitive()) {
      throw new IllegalArgumentException("not a supported view array class: " + viewArrayClass);
    }
    return VarHandle.makeByteArrayViewHandle(viewArrayClass, byteOrder);
  }

  public static VarHandle byteBufferViewVarHandle(Class<?> viewArrayClass, ByteOrder byteOrder) {
    Objects.requireNonNull(viewArrayClass, "viewArrayClass");
    Objects.requireNonNull(byteOrder, "byteOrder");
    if (!viewArrayClass.isArray() || viewArrayClass == byte[].class || viewArrayClass == boolean[].class
        || !viewArrayClass.getComponentType().isPrimitive()) {
      throw new IllegalArgumentException("not a supported view array class: " + viewArrayClass);
    }
    return VarHandle.makeByteBufferViewHandle(viewArrayClass, byteOrder);
  }

  public static VarHandle filterValue(VarHandle target, MethodHandle filterToTarget, MethodHandle filterFromTarget) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(filterToTarget, "filterToTarget");
    Objects.requireNonNull(filterFromTarget, "filterFromTarget");
    Class<?> targetType = target.varType();
    MethodType toType = filterToTarget.type();
    MethodType fromType = filterFromTarget.type();
    if (toType.parameterCount() != 1 || toType.returnType() != targetType) {
      throw new IllegalArgumentException("filterToTarget must be (V) -> targetType: " + toType);
    }
    if (fromType.parameterCount() != 1 || fromType.parameterType(0) != targetType) {
      throw new IllegalArgumentException("filterFromTarget must be (targetType) -> V: " + fromType);
    }
    Class<?> valueType = toType.parameterType(0);
    if (fromType.returnType() != valueType) {
      throw new IllegalArgumentException("filter return type must match valueType: " + fromType);
    }
    return VarHandle.makeFilterValue(target, filterToTarget, filterFromTarget);
  }

  public static VarHandle filterCoordinates(VarHandle target, int pos, MethodHandle... filters) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(filters, "filters");
    int coordCount = target.coordinateTypes().size();
    if (pos < 0 || pos >= coordCount || pos + filters.length > coordCount) {
      throw new IllegalArgumentException("pos out of range: " + pos);
    }
    for (int i = 0; i < filters.length; i++) {
      MethodHandle f = Objects.requireNonNull(filters[i], "filter");
      Class<?> coord = target.coordinateTypes().get(pos + i);
      if (f.type().parameterCount() != 1 || f.type().returnType() != coord) {
        throw new IllegalArgumentException("filter " + i + " must have signature (C) -> " + coord);
      }
    }
    return VarHandle.makeFilterCoordinates(target, pos, filters);
  }

  public static VarHandle insertCoordinates(VarHandle target, int pos, Object... values) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(values, "values");
    int coordCount = target.coordinateTypes().size();
    if (pos < 0 || pos >= coordCount || pos + values.length > coordCount) {
      throw new IllegalArgumentException("pos out of range: " + pos);
    }
    return VarHandle.makeInsertCoordinates(target, pos, values);
  }

  public static VarHandle dropCoordinates(VarHandle target, int pos, List<Class<?>> valueTypes) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(valueTypes, "valueTypes");
    int coordCount = target.coordinateTypes().size();
    if (pos < 0 || pos > coordCount) {
      throw new IllegalArgumentException("pos out of range: " + pos);
    }
    return VarHandle.makeDropCoordinates(target, pos, valueTypes);
  }

  public static VarHandle dropCoordinates(VarHandle target, int pos, Class<?>... valueTypes) {
    Objects.requireNonNull(valueTypes, "valueTypes");
    return dropCoordinates(target, pos, Arrays.asList(valueTypes));
  }

  public static VarHandle permuteCoordinates(VarHandle target, List<Class<?>> newCoordinates, int... reorder) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(newCoordinates, "newCoordinates");
    Objects.requireNonNull(reorder, "reorder");
    int targetCoords = target.coordinateTypes().size();
    if (reorder.length != targetCoords) {
      throw new IllegalArgumentException("reorder length must equal target coordinates count");
    }
    for (int i = 0; i < reorder.length; i++) {
      int r = reorder[i];
      if (r < 0 || r >= newCoordinates.size()) {
        throw new IllegalArgumentException("reorder index out of range: " + r);
      }
      Class<?> targetCoord = target.coordinateTypes().get(i);
      Class<?> newCoord = newCoordinates.get(r);
      if (!targetCoord.isAssignableFrom(newCoord)) {
        throw new IllegalArgumentException("cannot assign " + newCoord + " to " + targetCoord);
      }
    }
    return VarHandle.makePermuteCoordinates(target, newCoordinates, reorder);
  }

  public static VarHandle collectCoordinates(VarHandle target, int pos, MethodHandle filter) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(filter, "filter");
    int coordCount = target.coordinateTypes().size();
    if (pos < 0 || pos >= coordCount) {
      throw new IllegalArgumentException("pos out of range: " + pos);
    }
    Class<?> targetCoord = target.coordinateTypes().get(pos);
    if (filter.type().returnType() != targetCoord) {
      throw new IllegalArgumentException("filter return type must match target coordinate: " + targetCoord);
    }
    return VarHandle.makeCollectCoordinates(target, pos, filter);
  }

  public static MethodHandle identity(Class<?> type) {
    Objects.requireNonNull(type);
    return new MethodHandle.IdentityMethodHandle(type);
  }

  public static MethodHandle constant(Class<?> type, Object value) {
    Objects.requireNonNull(type);
    return new MethodHandle.ConstantMethodHandle(type, value);
  }

  public static MethodHandle dropArguments(MethodHandle target, int pos, List<Class<?>> valueTypes) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(valueTypes);
    return new MethodHandle.DropArgumentsMethodHandle(target, pos, valueTypes);
  }

  public static MethodHandle dropArguments(MethodHandle target, int pos, Class<?>... valueTypes) {
    return dropArguments(target, pos, Arrays.asList(valueTypes));
  }

  public static MethodHandle filterReturnValue(MethodHandle target, MethodHandle filter) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(filter);
    return new MethodHandle.FilterReturnValueMethodHandle(target, filter);
  }

  public static MethodHandle insertArguments(MethodHandle target, int pos, Object... values) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(values);
    return new MethodHandle.InsertArgumentsMethodHandle(target, pos, values);
  }

  public static MethodHandle filterArguments(MethodHandle target, int pos, MethodHandle... filters) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(filters);
    return new MethodHandle.FilterArgumentsMethodHandle(target, pos, filters);
  }

  public static MethodHandle permuteArguments(MethodHandle target, MethodType newType, int... reorder) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(newType);
    Objects.requireNonNull(reorder);
    return new MethodHandle.PermuteArgumentsMethodHandle(target, newType, reorder);
  }

  public static MethodHandle foldArguments(MethodHandle target, MethodHandle combiner) {
    return foldArguments(target, 0, combiner);
  }

  public static MethodHandle foldArguments(MethodHandle target, int pos, MethodHandle combiner) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(combiner);
    return new MethodHandle.FoldArgumentsMethodHandle(target, pos, combiner);
  }

  public static MethodHandle guardWithTest(MethodHandle test, MethodHandle target, MethodHandle fallback) {
    Objects.requireNonNull(test);
    Objects.requireNonNull(target);
    Objects.requireNonNull(fallback);
    return new MethodHandle.GuardWithTestMethodHandle(test, target, fallback);
  }

  public static MethodHandle catchException(MethodHandle target, Class<? extends Throwable> exType, MethodHandle handler) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(exType);
    Objects.requireNonNull(handler);
    return new MethodHandle.CatchExceptionMethodHandle(target, exType, handler);
  }

  public static MethodHandle throwException(Class<?> returnType, Class<? extends Throwable> exType) {
    Objects.requireNonNull(returnType);
    Objects.requireNonNull(exType);
    return new MethodHandle.ThrowExceptionMethodHandle(returnType, exType);
  }

  public static MethodHandle dropReturn(MethodHandle target) {
    Objects.requireNonNull(target);
    return new MethodHandle.DropReturnMethodHandle(target);
  }

  public static MethodHandle tryFinally(MethodHandle target, MethodHandle cleanup) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(cleanup, "cleanup");
    Class<?> rtype = target.type().returnType();
    if (cleanup.type().returnType() != rtype) {
      throw new IllegalArgumentException("cleanup return type must match target return type: " + cleanup.type().returnType() + " != " + rtype);
    }
    int minCleanupParams = (rtype == void.class) ? 1 : 2;
    if (cleanup.type().parameterCount() < minCleanupParams) {
      throw new IllegalArgumentException("cleanup must accept at least (" + (rtype == void.class ? "Throwable" : "Throwable, " + rtype) + ")");
    }
    if (!Throwable.class.isAssignableFrom(cleanup.type().parameterType(0))) {
      throw new IllegalArgumentException("cleanup first parameter must be Throwable: " + cleanup.type().parameterType(0));
    }
    if (rtype != void.class && cleanup.type().parameterType(1) != rtype) {
      throw new IllegalArgumentException("cleanup second parameter must be " + rtype + ": " + cleanup.type().parameterType(1));
    }
    return new MethodHandle.TryFinallyMethodHandle(target, cleanup);
  }

  public static MethodHandle whileLoop(MethodHandle init, MethodHandle pred, MethodHandle body) {
    Objects.requireNonNull(pred, "pred");
    Objects.requireNonNull(body, "body");
    if (pred.type().returnType() != boolean.class) {
      throw new IllegalArgumentException("pred must return boolean: " + pred.type());
    }
    Class<?> varType = (init != null) ? init.type().returnType() : body.type().returnType();
    if (body.type().returnType() != varType) {
      throw new IllegalArgumentException("body return type must match varType: " + body.type().returnType() + " != " + varType);
    }
    MethodType type;
    if (init != null) {
      type = init.type();
    } else if (pred.type().parameterCount() > 0 && varType == void.class) {
      type = MethodType.methodType(void.class, pred.type().parameterArray());
    } else {
      type = body.type().changeReturnType(varType);
    }
    return new MethodHandle.WhileLoopMethodHandle(type, init, pred, body, varType);
  }

  public static MethodHandle doWhileLoop(MethodHandle init, MethodHandle body, MethodHandle pred) {
    Objects.requireNonNull(pred, "pred");
    Objects.requireNonNull(body, "body");
    if (pred.type().returnType() != boolean.class) {
      throw new IllegalArgumentException("pred must return boolean: " + pred.type());
    }
    Class<?> varType = (init != null) ? init.type().returnType() : body.type().returnType();
    if (body.type().returnType() != varType) {
      throw new IllegalArgumentException("body return type must match varType: " + body.type().returnType() + " != " + varType);
    }
    MethodType type;
    if (init != null) {
      type = init.type();
    } else if (pred.type().parameterCount() > 0 && varType == void.class) {
      type = MethodType.methodType(void.class, pred.type().parameterArray());
    } else {
      type = body.type().changeReturnType(varType);
    }
    return new MethodHandle.DoWhileLoopMethodHandle(type, init, body, pred, varType);
  }

  public static MethodHandle countedLoop(MethodHandle iterations, MethodHandle init, MethodHandle body) {
    Objects.requireNonNull(iterations, "iterations");
    Objects.requireNonNull(body, "body");
    if (iterations.type().returnType() != int.class) {
      throw new IllegalArgumentException("iterations must return int: " + iterations.type());
    }
    Class<?> varType = (init != null) ? init.type().returnType() : body.type().returnType();
    MethodType type = (init != null) ? init.type() : iterations.type().changeReturnType(varType);
    return new MethodHandle.CountedLoopMethodHandle(type, null, iterations, init, body, varType);
  }

  public static MethodHandle countedLoop(MethodHandle start, MethodHandle end, MethodHandle init, MethodHandle body) {
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(end, "end");
    Objects.requireNonNull(body, "body");
    if (start.type().returnType() != int.class || end.type().returnType() != int.class) {
      throw new IllegalArgumentException("start and end must return int");
    }
    Class<?> varType = (init != null) ? init.type().returnType() : body.type().returnType();
    MethodType type = (init != null) ? init.type() : start.type().changeReturnType(varType);
    return new MethodHandle.CountedLoopMethodHandle(type, start, end, init, body, varType);
  }

  public static MethodHandle iteratedLoop(MethodHandle iterator, MethodHandle init, MethodHandle body) {
    Objects.requireNonNull(body, "body");
    Class<?> varType = (init != null) ? init.type().returnType() : body.type().returnType();
    MethodType type;
    if (iterator != null) {
      type = (init != null)
          ? MethodType.methodType(init.type().returnType(), iterator.type().parameterArray())
          : MethodType.methodType(varType, iterator.type().parameterArray());
    } else {
      if (init != null) {
        type = init.type().insertParameterTypes(0, Iterable.class);
      } else {
        type = MethodType.methodType(varType, Iterable.class);
      }
    }
    return new MethodHandle.IteratedLoopMethodHandle(type, iterator, init, body, varType);
  }

  public static MethodHandle loop(MethodHandle[]... clauses) {
    if (clauses == null || clauses.length == 0) {
      throw new IllegalArgumentException("null or no clauses passed");
    }
    int clauseCount = clauses.length;
    MethodHandle[] inits = new MethodHandle[clauseCount];
    MethodHandle[] steps = new MethodHandle[clauseCount];
    MethodHandle[] preds = new MethodHandle[clauseCount];
    MethodHandle[] finis = new MethodHandle[clauseCount];
    Class<?>[] varTypes = new Class<?>[clauseCount];

    boolean hasPred = false;
    Class<?> returnType = void.class;

    List<Integer> nonVoidVarIndices = new ArrayList<Integer>();

    for (int j = 0; j < clauseCount; j++) {
      MethodHandle[] clause = clauses[j];
      if (clause == null) {
        throw new IllegalArgumentException("null clauses are not allowed");
      }
      if (clause.length > 4) {
        throw new IllegalArgumentException("All loop clauses must be represented as MethodHandle arrays with at most 4 elements.");
      }
      inits[j] = clause.length > 0 ? clause[0] : null;
      steps[j] = clause.length > 1 ? clause[1] : null;
      preds[j] = clause.length > 2 ? clause[2] : null;
      finis[j] = clause.length > 3 ? clause[3] : null;

      if (preds[j] != null) {
        hasPred = true;
      }
      Class<?> vtype = void.class;
      if (inits[j] != null) {
        vtype = inits[j].type().returnType();
      } else if (steps[j] != null) {
        vtype = steps[j].type().returnType();
      }
      varTypes[j] = vtype;
      if (vtype != void.class) {
        nonVoidVarIndices.add(j);
      }
      if (finis[j] != null && finis[j].type().returnType() != void.class) {
        returnType = finis[j].type().returnType();
      }
    }

    if (!hasPred) {
      throw new IllegalArgumentException("no predicate found: " + Arrays.toString(clauses));
    }

    int[] varIndices = new int[nonVoidVarIndices.size()];
    for (int i = 0; i < varIndices.length; i++) {
      varIndices[i] = nonVoidVarIndices.get(i);
    }

    MethodType type = MethodType.methodType(returnType);
    for (int j = 0; j < clauseCount; j++) {
      if (inits[j] != null && inits[j].type().parameterCount() > 0) {
        type = MethodType.methodType(returnType, inits[j].type().parameterArray());
        break;
      }
    }

    return new MethodHandle.LoopMethodHandle(type, inits, steps, preds, finis, varTypes, varIndices, returnType);
  }

  public static MethodHandle zero(Class<?> type) {
    Objects.requireNonNull(type);
    if (type == void.class) {
      return constant(void.class, null);
    }
    if (type == boolean.class) {
      return constant(boolean.class, Boolean.FALSE);
    }
    if (type == byte.class) {
      return constant(byte.class, Byte.valueOf((byte) 0));
    }
    if (type == short.class) {
      return constant(short.class, Short.valueOf((short) 0));
    }
    if (type == char.class) {
      return constant(char.class, Character.valueOf('\0'));
    }
    if (type == int.class) {
      return constant(int.class, Integer.valueOf(0));
    }
    if (type == long.class) {
      return constant(long.class, Long.valueOf(0L));
    }
    if (type == float.class) {
      return constant(float.class, Float.valueOf(0.0f));
    }
    if (type == double.class) {
      return constant(double.class, Double.valueOf(0.0d));
    }
    return constant(type, null);
  }

  public static MethodHandle empty(MethodType type) {
    Objects.requireNonNull(type);
    MethodHandle z = zero(type.returnType());
    return dropArguments(z, 0, type.parameterList());
  }

  public static MethodHandle arrayConstructor(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return new MethodHandle.ArrayConstructorMethodHandle(arrayClass);
  }

  public static MethodHandle arrayLength(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return new MethodHandle.ArrayLengthMethodHandle(arrayClass);
  }

  public static MethodHandle arrayElementGetter(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return new MethodHandle.ArrayElementGetterMethodHandle(arrayClass);
  }

  public static MethodHandle arrayElementSetter(Class<?> arrayClass) {
    Objects.requireNonNull(arrayClass);
    if (!arrayClass.isArray()) {
      throw new IllegalArgumentException("Not an array class: " + arrayClass);
    }
    return new MethodHandle.ArrayElementSetterMethodHandle(arrayClass);
  }

  public static MethodHandle collectArguments(MethodHandle target, int pos, MethodHandle filter) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(filter, "filter");
    return new MethodHandle.CollectArgumentsMethodHandle(target, pos, filter);
  }

  public static MethodHandle exactInvoker(MethodType type) {
    Objects.requireNonNull(type, "type");
    return new MethodHandle.ExactInvokerMethodHandle(type);
  }

  public static MethodHandle invoker(MethodType type) {
    return exactInvoker(type);
  }

  public static MethodHandle spreadInvoker(MethodType type, int leadingArgCount) {
    Objects.requireNonNull(type, "type");
    if (leadingArgCount < 0 || leadingArgCount > type.parameterCount()) {
      throw new IllegalArgumentException("bad argument count");
    }
    MethodHandle invoker = exactInvoker(type);
    int spreadArgCount = type.parameterCount() - leadingArgCount;
    return invoker.asSpreader(Object[].class, spreadArgCount);
  }

  public static MethodHandle guardWithTest(SwitchPoint switchPoint, MethodHandle target, MethodHandle fallback) {
    Objects.requireNonNull(switchPoint, "switchPoint");
    return switchPoint.guardWithTest(target, fallback);
  }

  public static Lookup privateLookupIn(Class<?> targetClass, Lookup lookup) throws IllegalAccessException {
    Objects.requireNonNull(targetClass);
    Objects.requireNonNull(lookup);
    if (targetClass.isPrimitive() || targetClass.isArray()) {
      throw new IllegalArgumentException("Target class is primitive or array: " + targetClass);
    }
    if (!lookup.hasFullPrivilegeAccess()) {
      throw new IllegalAccessException("Lookup does not have full privilege access");
    }
    return new Lookup(shim.Classes.toVMClass(targetClass), 0x1f);
  }

  public static Object classData(Lookup lookup, String name, Class<?> type) throws IllegalAccessException {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
    if ((lookup.lookupModes() & Lookup.ORIGINAL) == 0) {
      throw new IllegalAccessException(lookup + " does not have ORIGINAL access");
    }
    if (!"_".equals(name)) throw new IllegalArgumentException("class data name must be _");
    shim.ClassAddendum addendum = shim.Classes.toVMClass(lookup.lookupClass()).addendum;
    Object data = addendum == null ? null : addendum.classData;
    if (data == null) {
      return null;
    }
    return type.cast(data);
  }

  public static Object classDataAt(Lookup lookup, String name, Class<?> type, int index) throws IllegalAccessException {
    Objects.requireNonNull(lookup, "lookup");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
    if ((lookup.lookupModes() & Lookup.ORIGINAL) == 0) {
      throw new IllegalAccessException(lookup + " does not have ORIGINAL access");
    }
    if (!"_".equals(name)) throw new IllegalArgumentException("class data name must be _");
    shim.ClassAddendum addendum = shim.Classes.toVMClass(lookup.lookupClass()).addendum;
    Object data = addendum == null ? null : addendum.classData;
    if (data == null) {
      return null;
    }
    if (!(data instanceof List)) {
      throw new IllegalArgumentException("class data is not a List: " + data.getClass().getName());
    }
    List<?> list = (List<?>) data;
    Object elem = list.get(index);
    return type.cast(elem);
  }

  public static class Lookup {
    public static final int PUBLIC = 0x0001;
    public static final int PRIVATE = 0x0002;
    public static final int PROTECTED = 0x0004;
    public static final int PACKAGE = 0x0008;
    public static final int MODULE = 0x0010;
    public static final int UNCONDITIONAL = 0x0020;
    public static final int ORIGINAL = 0x0040;

    final shim.VMClass class_;
    private final int modes;

    Lookup(shim.VMClass class_, int modes) {
      this.class_ = class_;
      this.modes = modes;
    }

    public Class<?> lookupClass() {
      return shim.SystemClassLoader.getClass(class_);
    }

    public int lookupModes() {
      return modes;
    }

    public boolean hasFullPrivilegeAccess() {
      return (modes & 0x12) == 0x12;
    }

    public Lookup in(Class<?> requestedLookupClass) {
      Objects.requireNonNull(requestedLookupClass);
      if (requestedLookupClass == lookupClass()) {
        return this;
      }
      int newModes = modes & ~(PROTECTED | PRIVATE | ORIGINAL);
      if (!requestedLookupClass.getPackageName().equals(lookupClass().getPackageName())) {
        newModes &= ~PACKAGE;
      }
      return new Lookup(shim.Classes.toVMClass(requestedLookupClass), newModes);
    }

    public Lookup dropLookupMode(int modeToDrop) {
      if (modeToDrop != PUBLIC && modeToDrop != PRIVATE && modeToDrop != PROTECTED
          && modeToDrop != PACKAGE && modeToDrop != MODULE && modeToDrop != UNCONDITIONAL
          && modeToDrop != ORIGINAL) {
        throw new IllegalArgumentException("Invalid lookup mode");
      }
      int remaining = modes & ~modeToDrop;
      if ((modes & UNCONDITIONAL) == 0) {
        remaining &= ~(PROTECTED | ORIGINAL);
        if (modeToDrop == PUBLIC) remaining = 0;
        if (modeToDrop == MODULE) remaining &= ~(PACKAGE | PRIVATE);
        if (modeToDrop == PACKAGE) remaining &= ~PRIVATE;
      }
      return new Lookup(class_, remaining);
    }

    public MethodHandle findVirtual(Class<?> refc, String name, MethodType type)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(refc);
      Objects.requireNonNull(name);
      Objects.requireNonNull(type);
      if (name.startsWith("<")) {
        throw new NoSuchMethodException(name);
      }
      Method m = refc.getMethod(name, type.parameterArray());
      if (!m.getReturnType().equals(type.returnType())) {
        throw new NoSuchMethodException(name);
      }
      if ((m.getModifiers() & Modifier.STATIC) != 0) {
        throw new IllegalAccessException("Method is static: " + name);
      }
      int kind = refc.isInterface() ? MethodHandle.REF_invokeInterface : MethodHandle.REF_invokeVirtual;
      return new MethodHandle(kind, refc.getClassLoader(), m.vmMethod);
    }

    public MethodHandle findStatic(Class<?> refc, String name, MethodType type)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(refc);
      Objects.requireNonNull(name);
      Objects.requireNonNull(type);
      if (name.startsWith("<")) {
        throw new NoSuchMethodException(name);
      }
      Method m = null;
      try {
        m = refc.getDeclaredMethod(name, type.parameterArray());
      } catch (NoSuchMethodException e) {
        m = refc.getMethod(name, type.parameterArray());
      }
      if (!m.getReturnType().equals(type.returnType())) {
        throw new NoSuchMethodException(name);
      }
      if ((m.getModifiers() & Modifier.STATIC) == 0) {
        throw new IllegalAccessException("Method is not static: " + name);
      }
      return new MethodHandle(MethodHandle.REF_invokeStatic, refc.getClassLoader(), m.vmMethod);
    }

    public MethodHandle findSpecial(Class<?> refc, String name, MethodType type, Class<?> specialCaller)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(refc);
      Objects.requireNonNull(name);
      Objects.requireNonNull(type);
      Objects.requireNonNull(specialCaller);
      if (name.startsWith("<")) {
        throw new NoSuchMethodException(name);
      }
      Method m = refc.getDeclaredMethod(name, type.parameterArray());
      if (!m.getReturnType().equals(type.returnType())) {
        throw new NoSuchMethodException(name);
      }
      return new MethodHandle(MethodHandle.REF_invokeSpecial, refc.getClassLoader(), m.vmMethod);
    }

    public MethodHandle findConstructor(Class<?> refc, MethodType type)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(refc);
      Objects.requireNonNull(type);
      if (type.returnType() != void.class) {
        throw new NoSuchMethodException("Constructor must return void");
      }
      Constructor<?> c = refc.getDeclaredConstructor(type.parameterArray());
      return new MethodHandle(MethodHandle.REF_newInvokeSpecial, refc.getClassLoader(), c.method.vmMethod);
    }

    public MethodHandle unreflect(Method m) throws IllegalAccessException {
      Objects.requireNonNull(m);
      if ((m.getModifiers() & Modifier.STATIC) != 0) {
        return new MethodHandle(MethodHandle.REF_invokeStatic, m.getDeclaringClass().getClassLoader(), m.vmMethod);
      }
      int kind = m.getDeclaringClass().isInterface() ? MethodHandle.REF_invokeInterface : MethodHandle.REF_invokeVirtual;
      return new MethodHandle(kind, m.getDeclaringClass().getClassLoader(), m.vmMethod);
    }

    public MethodHandle unreflectSpecial(Method m, Class<?> specialCaller)
        throws IllegalAccessException {
      Objects.requireNonNull(m);
      return new MethodHandle(MethodHandle.REF_invokeSpecial, m.getDeclaringClass().getClassLoader(), m.vmMethod);
    }

    public MethodHandle unreflectConstructor(Constructor<?> c) throws IllegalAccessException {
      Objects.requireNonNull(c);
      return new MethodHandle(MethodHandle.REF_newInvokeSpecial, c.getDeclaringClass().getClassLoader(), c.method.vmMethod);
    }

    public MethodHandle findGetter(Class<?> refc, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = refc.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) != 0) {
        throw new NoSuchFieldException(name);
      }
      return MethodHandle.makeFieldHandle(MethodHandle.REF_getField, f);
    }

    public MethodHandle findSetter(Class<?> refc, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = refc.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) != 0) {
        throw new NoSuchFieldException(name);
      }
      return MethodHandle.makeFieldHandle(MethodHandle.REF_putField, f);
    }

    public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = refc.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) == 0) {
        throw new NoSuchFieldException(name);
      }
      return MethodHandle.makeFieldHandle(MethodHandle.REF_getStatic, f);
    }

    public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f = refc.getField(name);
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) == 0) {
        throw new NoSuchFieldException(name);
      }
      return MethodHandle.makeFieldHandle(MethodHandle.REF_putStatic, f);
    }

    public MethodHandle unreflectGetter(Field f) throws IllegalAccessException {
      Objects.requireNonNull(f);
      int kind = (f.getModifiers() & Modifier.STATIC) != 0
          ? MethodHandle.REF_getStatic
          : MethodHandle.REF_getField;
      return MethodHandle.makeFieldHandle(kind, f);
    }

    public MethodHandle unreflectSetter(Field f) throws IllegalAccessException {
      Objects.requireNonNull(f);
      int kind = (f.getModifiers() & Modifier.STATIC) != 0
          ? MethodHandle.REF_putStatic
          : MethodHandle.REF_putField;
      return MethodHandle.makeFieldHandle(kind, f);
    }

    public VarHandle findVarHandle(Class<?> recv, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f;
      try {
        f = recv.getField(name);
      } catch (NoSuchFieldException e) {
        f = recv.getDeclaredField(name);
      }
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) != 0) {
        throw new NoSuchFieldException(name);
      }
      f.setAccessible(true);
      return VarHandle.makeFieldHandle(f, false);
    }

    public VarHandle findStaticVarHandle(Class<?> decl, String name, Class<?> type)
        throws NoSuchFieldException, IllegalAccessException {
      Field f;
      try {
        f = decl.getField(name);
      } catch (NoSuchFieldException e) {
        f = decl.getDeclaredField(name);
      }
      if (!f.getType().equals(type) || (f.getModifiers() & Modifier.STATIC) == 0) {
        throw new NoSuchFieldException(name);
      }
      f.setAccessible(true);
      return VarHandle.makeFieldHandle(f, true);
    }

    public VarHandle unreflectVarHandle(Field f) throws IllegalAccessException {
      Objects.requireNonNull(f);
      return VarHandle.makeFieldHandle(f, (f.getModifiers() & Modifier.STATIC) != 0);
    }

    public Class<?> findClass(String targetName) throws ClassNotFoundException, IllegalAccessException {
      Objects.requireNonNull(targetName, "targetName");
      Class<?> decl = shim.SystemClassLoader.getClass(class_);
      ClassLoader loader = (decl != null) ? decl.getClassLoader() : ClassLoader.getSystemClassLoader();
      return Class.forName(targetName, false, loader);
    }

    public boolean hasPrivateAccess() {
      return (modes & PRIVATE) != 0;
    }

    public Class<?> accessClass(Class<?> targetClass) throws IllegalAccessException {
      Objects.requireNonNull(targetClass, "targetClass");
      return targetClass;
    }

    public Class<?> ensureInitialized(Class<?> targetClass) throws IllegalAccessException {
      Objects.requireNonNull(targetClass, "targetClass");
      if (targetClass.isPrimitive() || targetClass.isArray()) {
        throw new IllegalArgumentException("targetClass is primitive or array: " + targetClass);
      }
      shim.Classes.initialize(shim.Classes.toVMClass(targetClass));
      return targetClass;
    }

    public MethodHandle bind(Object receiver, String name, MethodType type)
        throws NoSuchMethodException, IllegalAccessException {
      Objects.requireNonNull(receiver, "receiver");
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(type, "type");
      return findVirtual(receiver.getClass(), name, type).bindTo(receiver);
    }

    public MethodHandleInfo revealDirect(MethodHandle target) {
      Objects.requireNonNull(target, "target");
      if (target instanceof MethodHandle.FieldMethodHandle) {
        MethodHandle.FieldMethodHandle fmh = (MethodHandle.FieldMethodHandle) target;
        return new DirectMethodHandleInfo(fmh.kind, fmh.targetField);
      }
      if (target.method != null) {
        return new DirectMethodHandleInfo(target.kind, target.method);
      }
      throw new IllegalArgumentException("not a direct method handle");
    }

    public Class<?> defineClass(byte[] bytes) throws IllegalAccessException {
      Objects.requireNonNull(bytes, "bytes");
      if (!hasFullPrivilegeAccess()) {
        throw new IllegalAccessException("Lookup does not have full privilege access");
      }
      ClassLoader loader = lookupClass().getClassLoader();
      if (loader == null) {
        loader = ClassLoader.getSystemClassLoader();
      }
      return shim.SystemClassLoader.getClass(shim.Classes.defineVMClass(loader, bytes, 0, bytes.length));
    }

    public enum ClassOption {
      NESTMATE,
      STRONG
    }

    public Lookup defineHiddenClass(byte[] bytes, boolean initialize, ClassOption... options) throws IllegalAccessException {
      return defineHiddenClass0(bytes, null, initialize, options);
    }

    public Lookup defineHiddenClassWithClassData(byte[] bytes, Object classData, boolean initialize, ClassOption... options)
        throws IllegalAccessException {
      Objects.requireNonNull(classData, "classData");
      return defineHiddenClass0(bytes, classData, initialize, options);
    }

    private Lookup defineHiddenClass0(byte[] bytes, Object classData, boolean initialize, ClassOption... options)
        throws IllegalAccessException {
      Objects.requireNonNull(bytes, "bytes");
      Objects.requireNonNull(options, "options");
      if (!hasFullPrivilegeAccess()) {
        throw new IllegalAccessException("Lookup does not have full privilege access");
      }
      boolean nestmate = false;
      for (ClassOption option : options) {
        Objects.requireNonNull(option, "option");
        nestmate |= option == ClassOption.NESTMATE;
      }
      shim.VMClass defined = shim.Classes.defineHiddenVMClass(class_, bytes);
      Class<?> definedClass = shim.SystemClassLoader.getClass(defined);
      defined.addendum.hiddenNestHost = nestmate
          ? shim.Classes.toVMClass(lookupClass().getNestHost()) : null;
      defined.addendum.classData = classData;
      if (initialize) {
        ensureInitialized(definedClass);
      }
      return new Lookup(shim.Classes.toVMClass(definedClass), 0x5f);
    }

    public String toString() {
      return "lookup[" + shim.SystemClassLoader.getClass(class_) + ", "
        + modes + "]";
    }
  }

  static class DirectMethodHandleInfo implements MethodHandleInfo {
    private final int kind;
    private final Member member;
    private final String name;
    private final MethodType type;

    DirectMethodHandleInfo(int kind, Field field) {
      this.kind = kind;
      this.member = field;
      this.name = field.getName();
      Class<?> fieldType = field.getType();
      if (kind == REF_getField || kind == REF_getStatic) {
        this.type = MethodType.methodType(fieldType);
      } else {
        this.type = MethodType.methodType(void.class, fieldType);
      }
    }

    DirectMethodHandleInfo(int kind, shim.VMMethod vmMethod) {
      this.kind = kind;
      Method m = new Method(vmMethod);
      if (kind == REF_newInvokeSpecial) {
        this.member = new Constructor<>(m);
        this.name = "<init>";
        this.type = MethodType.methodType(void.class, m.getParameterTypes());
      } else {
        this.member = m;
        this.name = m.getName();
        this.type = MethodType.methodType(m.getReturnType(), m.getParameterTypes());
      }
    }

    @Override
    public int getReferenceKind() {
      return kind;
    }

    @Override
    public Class<?> getDeclaringClass() {
      return member.getDeclaringClass();
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public MethodType getMethodType() {
      return type;
    }

    @Override
    public int getModifiers() {
      return member.getModifiers();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Member> T reflectAs(Class<T> expected, Lookup lookup) {
      Objects.requireNonNull(expected, "expected");
      Objects.requireNonNull(lookup, "lookup");
      if (!expected.isInstance(member)) {
        throw new ClassCastException("Cannot cast " + member.getClass().getName() + " to " + expected.getName());
      }
      return (T) member;
    }

    @Override
    public String toString() {
      return MethodHandleInfo.toString(kind, getDeclaringClass(), name, type);
    }
  }
}
