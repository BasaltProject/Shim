/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdi;

import com.sun.jdi.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import shim.jdi.Wire.In;
import shim.jdi.Wire.Out;

public final class RefType {
  final Vm vm;
  final int tag;
  final long id;
  ReferenceType self;
  String signature;
  private String generic;
  private List<Field> fields;
  private List<Method> methods;
  private List<Fld> fieldImpls;
  private List<Mth> methodImpls;

  RefType(Vm vm, int tag, long id) {
    this.vm = vm;
    this.tag = tag;
    this.id = id;
  }

  public VirtualMachine virtualMachine() {
    return vm.proxy;
  }

  public String signature() {
    if (signature == null) {
      signature = vm.call(2, 1, new Out().id(id, vm.w.typeIdSize)).data.str();
    }
    return signature;
  }

  public String genericSignature() {
    if (generic == null) {
      In in = vm.call(2, 13, new Out().id(id, vm.w.typeIdSize)).data;
      signature = in.str();
      String g = in.str();
      generic = g == null || g.isEmpty() ? "" : g;
    }
    return generic.isEmpty() ? null : generic;
  }

  public String name() {
    return Vm.nameOfSig(signature());
  }

  public String toString() {
    return (tag == 2 ? "interface " : tag == 3 ? "" : "class ") + name() + " (" + (tag == 3 ? "" : "loaded, ")
        + "id=" + id + ")";
  }

  public int modifiers() {
    return vm.call(2, 3, new Out().id(id, vm.w.typeIdSize)).data.i32();
  }

  public boolean isPublic() {
    return (modifiers() & 1) != 0;
  }

  public boolean isPrivate() {
    return (modifiers() & 2) != 0;
  }

  public boolean isProtected() {
    return (modifiers() & 4) != 0;
  }

  public boolean isPackagePrivate() {
    return (modifiers() & 7) == 0;
  }

  public boolean isStatic() {
    return (modifiers() & 8) != 0;
  }

  public boolean isFinal() {
    return (modifiers() & 0x10) != 0;
  }

  public boolean isAbstract() {
    return (modifiers() & 0x400) != 0;
  }

  public int status() {
    return vm.call(2, 9, new Out().id(id, vm.w.typeIdSize)).data.i32();
  }

  public boolean isPrepared() {
    return (status() & 2) != 0;
  }

  public boolean isVerified() {
    return (status() & 1) != 0;
  }

  public boolean isInitialized() {
    return (status() & 4) != 0;
  }

  public boolean failedToInitialize() {
    return (status() & 8) != 0;
  }

  public boolean isEnum() {
    ClassType sup = (ClassType) superclass();
    return sup != null && sup.name().equals("java.lang.Enum");
  }

  public ClassLoaderReference classLoader() {
    long l = vm.call(2, 2, new Out().id(id, vm.w.typeIdSize)).data.id(vm.w.objectIdSize);
    return vm.obj('l', l);
  }

  public String sourceName() throws AbsentInformationException {
    Wire.Reply r;
    try {
      r = vm.w.send(2, 7, new Out().id(id, vm.w.typeIdSize));
    } catch (java.io.IOException e) {
      throw new VMDisconnectedException();
    }
    if (r.error != 0) {
      throw new AbsentInformationException();
    }
    return r.data.str();
  }

  public List<String> sourceNames(String stratum) throws AbsentInformationException {
    return Collections.singletonList(sourceName());
  }

  public List<String> sourcePaths(String stratum) throws AbsentInformationException {
    return Collections.singletonList(sourceName());
  }

  public String defaultStratum() {
    return "Java";
  }

  public List<String> availableStrata() {
    return Collections.singletonList("Java");
  }

  public ClassObjectReference classObject() {
    long o = vm.call(2, 11, new Out().id(id, vm.w.typeIdSize)).data.id(vm.w.objectIdSize);
    return vm.obj('c', o);
  }

  public ReferenceType superclassRef() {
    return null;
  }

  public ClassType superclass() {
    if (tag != 1) {
      return null;
    }
    long s = vm.call(3, 1, new Out().id(id, vm.w.typeIdSize)).data.id(vm.w.typeIdSize);
    return s == 0 ? null : (ClassType) vm.refTypeProxy(1, s);
  }

  public List<InterfaceType> interfaces() {
    In in = vm.call(2, 10, new Out().id(id, vm.w.typeIdSize)).data;
    int n = in.i32();
    List<InterfaceType> l = new ArrayList<InterfaceType>();
    for (int i = 0; i < n; i++) {
      l.add((InterfaceType) vm.refTypeProxy(2, in.id(vm.w.typeIdSize)));
    }
    return Collections.unmodifiableList(l);
  }

  public List<InterfaceType> allInterfaces() {
    List<InterfaceType> out = new ArrayList<InterfaceType>();
    collect(this, out);
    return Collections.unmodifiableList(out);
  }

  private static void collect(RefType t, List<InterfaceType> out) {
    for (InterfaceType i : t.interfaces()) {
      if (!out.contains(i)) {
        out.add(i);
      }
      collect((RefType) Proxies.implOf(i), out);
    }
    if (t.tag == 1) {
      ClassType s = t.superclass();
      if (s != null) {
        collect((RefType) Proxies.implOf(s), out);
      }
    }
  }

  public List<InterfaceType> superinterfaces() {
    return interfaces();
  }

  public List<ClassType> subclasses() {
    List<ClassType> l = new ArrayList<ClassType>();
    for (ReferenceType t : vm.proxy.allClasses()) {
      if (t instanceof ClassType && ((ClassType) t).superclass() != null
          && ((ClassType) t).superclass().equals(self)) {
        l.add((ClassType) t);
      }
    }
    return l;
  }

  public ClassType concreteMethodByName_() {
    return null;
  }

  List<Fld> loadFields() {
    if (fieldImpls == null) {
      In in = vm.call(2, 14, new Out().id(id, vm.w.typeIdSize)).data;
      int n = in.i32();
      List<Fld> l = new ArrayList<Fld>();
      List<Field> p = new ArrayList<Field>();
      for (int i = 0; i < n; i++) {
        long fid = in.id(vm.w.fieldIdSize);
        String name = in.str();
        String sig = in.str();
        String gen = in.str();
        int mod = in.i32();
        Fld f = new Fld(this, fid, name, sig, gen, mod);
        f.self = Proxies.make(f, Field.class);
        l.add(f);
        p.add(f.self);
      }
      fieldImpls = l;
      fields = Collections.unmodifiableList(p);
    }
    return fieldImpls;
  }

  List<Mth> loadMethods() {
    if (methodImpls == null) {
      In in = vm.call(2, 15, new Out().id(id, vm.w.typeIdSize)).data;
      int n = in.i32();
      List<Mth> l = new ArrayList<Mth>();
      List<Method> p = new ArrayList<Method>();
      for (int i = 0; i < n; i++) {
        long mid = in.id(vm.w.methodIdSize);
        String name = in.str();
        String sig = in.str();
        String gen = in.str();
        int mod = in.i32();
        Mth m = new Mth(this, mid, name, sig, gen, mod);
        m.self = Proxies.make(m, Method.class);
        l.add(m);
        p.add(m.self);
      }
      methodImpls = l;
      methods = Collections.unmodifiableList(p);
    }
    return methodImpls;
  }

  Mth methodById(long mid) {
    for (Mth m : loadMethods()) {
      if (m.id == mid) {
        return m;
      }
    }
    return null;
  }

  public List<Field> fields() {
    loadFields();
    return fields;
  }

  public List<Field> visibleFields() {
    return allFields();
  }

  public List<Field> allFields() {
    List<Field> out = new ArrayList<Field>(fields());
    if (tag == 1) {
      ClassType s = superclass();
      if (s != null) {
        out.addAll(s.allFields());
      }
    }
    for (InterfaceType i : interfaces()) {
      for (Field f : i.allFields()) {
        if (!out.contains(f)) {
          out.add(f);
        }
      }
    }
    return Collections.unmodifiableList(out);
  }

  public Field fieldByName(String name) {
    for (Field f : allFields()) {
      if (f.name().equals(name)) {
        return f;
      }
    }
    return null;
  }

  public List<Method> methods() {
    loadMethods();
    return methods;
  }

  public List<Method> visibleMethods() {
    return allMethods();
  }

  public List<Method> allMethods() {
    List<Method> out = new ArrayList<Method>(methods());
    if (tag == 1) {
      ClassType s = superclass();
      if (s != null) {
        for (Method m : s.allMethods()) {
          boolean dup = false;
          for (Method e : out) {
            if (e.name().equals(m.name()) && e.signature().equals(m.signature())) {
              dup = true;
            }
          }
          if (!dup) {
            out.add(m);
          }
        }
      }
    }
    for (InterfaceType i : interfaces()) {
      for (Method m : i.allMethods()) {
        if (!out.contains(m)) {
          out.add(m);
        }
      }
    }
    return Collections.unmodifiableList(out);
  }

  public List<Method> methodsByName(String name) {
    List<Method> l = new ArrayList<Method>();
    for (Method m : visibleMethods()) {
      if (m.name().equals(name)) {
        l.add(m);
      }
    }
    return l;
  }

  public List<Method> methodsByName(String name, String signature) {
    List<Method> l = new ArrayList<Method>();
    for (Method m : visibleMethods()) {
      if (m.name().equals(name) && m.signature().equals(signature)) {
        l.add(m);
      }
    }
    return l;
  }

  public Method concreteMethodByName(String name, String signature) {
    for (Method m : allMethods()) {
      if (m.name().equals(name) && m.signature().equals(signature) && !m.isAbstract()) {
        return m;
      }
    }
    return null;
  }

  public Value getValue(Field f) {
    List<Field> l = new ArrayList<Field>();
    l.add(f);
    return getValues(l).get(f);
  }

  public java.util.Map<Field, Value> getValues(List<? extends Field> fs) {
    Out o = new Out().id(id, vm.w.typeIdSize).i32(fs.size());
    for (Field f : fs) {
      o.id(((Fld) Proxies.implOf(f)).id, vm.w.fieldIdSize);
    }
    In in = vm.call(2, 6, o).data;
    int n = in.i32();
    java.util.Map<Field, Value> m = new java.util.LinkedHashMap<Field, Value>();
    for (int i = 0; i < n; i++) {
      m.put(fs.get(i), (Value) vm.readValue(in));
    }
    return m;
  }

  public List<Location> allLineLocations() throws AbsentInformationException {
    List<Location> out = new ArrayList<Location>();
    for (Method m : methods()) {
      if (!m.isAbstract() && !m.isNative()) {
        try {
          out.addAll(m.allLineLocations());
        } catch (AbsentInformationException e) {
          // method without line info
        }
      }
    }
    if (out.isEmpty()) {
      throw new AbsentInformationException();
    }
    return out;
  }

  public List<Location> allLineLocations(String stratum, String sourceName) throws AbsentInformationException {
    return allLineLocations();
  }

  public List<Location> locationsOfLine(int line) throws AbsentInformationException {
    List<Location> out = new ArrayList<Location>();
    for (Location l : allLineLocations()) {
      if (l.lineNumber() == line) {
        out.add(l);
      }
    }
    return out;
  }

  public List<ReferenceType> nestedTypes() {
    List<ReferenceType> l = new ArrayList<ReferenceType>();
    String prefix = name() + "$";
    for (ReferenceType t : vm.proxy.allClasses()) {
      if (t.name().startsWith(prefix)) {
        l.add(t);
      }
    }
    return l;
  }

  public List<ObjectReference> instances(long max) {
    return new ArrayList<ObjectReference>();
  }

  public int majorVersion() {
    return vm.call(2, 17, new Out().id(id, vm.w.typeIdSize)).data.i32();
  }

  public int minorVersion() {
    In in = vm.call(2, 17, new Out().id(id, vm.w.typeIdSize)).data;
    in.i32();
    return in.i32();
  }

  public boolean isArrayType_() {
    return tag == 3;
  }

  public boolean equals(Object o) {
    Object i = Proxies.implOf(o);
    return i instanceof RefType && ((RefType) i).id == id;
  }

  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }

  public int compareTo(Object o) {
    return name().compareTo(((ReferenceType) o).name());
  }

  /* ---------- ArrayType ---------- */

  public String componentSignature() {
    return signature().substring(1);
  }

  public String componentTypeName() {
    return Vm.nameOfSig(componentSignature());
  }

  public Type componentType() throws ClassNotLoadedException {
    return vm.typeOfSignature(componentSignature());
  }

  public ArrayReference newInstance(int length) {
    In in = vm.call(4, 1, new Out().id(id, vm.w.typeIdSize).i32(length)).data;
    in.u8();
    return vm.obj('[', in.id(vm.w.objectIdSize));
  }

  /* ---------- class invoke ---------- */

  public Value invokeMethod(ThreadReference thread, Method method, List<? extends Value> args, int options)
      throws InvocationException {
    Mth m = (Mth) Proxies.implOf(method);
    Out o = new Out().id(id, vm.w.typeIdSize).id(vm.idOf(thread), vm.w.objectIdSize).id(m.id, vm.w.methodIdSize)
        .i32(args.size());
    for (Value a : args) {
      vm.writeValue(o, a);
    }
    o.i32(options);
    In in = vm.call(3, 3, o).data;
    Object v = vm.readValue(in);
    in.u8();
    long ex = in.id(vm.w.objectIdSize);
    if (ex != 0) {
      throw new InvocationException(vm.<ObjectReference>obj('L', ex));
    }
    return (Value) v;
  }

  public ObjectReference newInstance(ThreadReference thread, Method method, List<? extends Value> args,
      int options) throws InvocationException {
    Mth m = (Mth) Proxies.implOf(method);
    Out o = new Out().id(id, vm.w.typeIdSize).id(vm.idOf(thread), vm.w.objectIdSize).id(m.id, vm.w.methodIdSize)
        .i32(args.size());
    for (Value a : args) {
      vm.writeValue(o, a);
    }
    o.i32(options);
    In in = vm.call(3, 4, o).data;
    int tg = in.u8();
    long obj = in.id(vm.w.objectIdSize);
    in.u8();
    long ex = in.id(vm.w.objectIdSize);
    if (ex != 0) {
      throw new InvocationException(vm.<ObjectReference>obj('L', ex));
    }
    return vm.obj(tg, obj);
  }

  /* ---------- members ---------- */

  public static final class Fld {
    final RefType owner;
    final long id;
    final String name;
    final String sig;
    final String gen;
    final int mod;
    Field self;

    Fld(RefType owner, long id, String name, String sig, String gen, int mod) {
      this.owner = owner;
      this.id = id;
      this.name = name;
      this.sig = sig;
      this.gen = gen;
      this.mod = mod;
    }

    public VirtualMachine virtualMachine() {
      return owner.vm.proxy;
    }

    public String name() {
      return name;
    }

    public String signature() {
      return sig;
    }

    public String genericSignature() {
      return gen == null || gen.isEmpty() ? null : gen;
    }

    public String typeName() {
      return Vm.nameOfSig(sig);
    }

    public Type type() throws ClassNotLoadedException {
      return owner.vm.typeOfSignature(sig);
    }

    public ReferenceType declaringType() {
      return owner.self;
    }

    public int modifiers() {
      return mod;
    }

    public boolean isPublic() {
      return (mod & 1) != 0;
    }

    public boolean isPrivate() {
      return (mod & 2) != 0;
    }

    public boolean isProtected() {
      return (mod & 4) != 0;
    }

    public boolean isPackagePrivate() {
      return (mod & 7) == 0;
    }

    public boolean isStatic() {
      return (mod & 8) != 0;
    }

    public boolean isFinal() {
      return (mod & 0x10) != 0;
    }

    public boolean isVolatile() {
      return (mod & 0x40) != 0;
    }

    public boolean isTransient() {
      return (mod & 0x80) != 0;
    }

    public boolean isSynthetic() {
      return (mod & 0x1000) != 0;
    }

    public boolean isEnumConstant() {
      return (mod & 0x4000) != 0;
    }

    public boolean equals(Object o) {
      Object i = Proxies.implOf(o);
      return i instanceof Fld && ((Fld) i).id == id && ((Fld) i).owner == owner;
    }

    public int hashCode() {
      return (int) id;
    }

    public int compareTo(Object o) {
      return name.compareTo(((Field) o).name());
    }

    public String toString() {
      return owner.name() + "." + name;
    }
  }

  public static final class Mth {
    final RefType owner;
    final long id;
    final String name;
    final String sig;
    final String gen;
    final int mod;
    Method self;
    private List<LocalVariable> vars;
    private int argCount;

    Mth(RefType owner, long id, String name, String sig, String gen, int mod) {
      this.owner = owner;
      this.id = id;
      this.name = name;
      this.sig = sig;
      this.gen = gen;
      this.mod = mod;
    }

    public VirtualMachine virtualMachine() {
      return owner.vm.proxy;
    }

    public String name() {
      return name;
    }

    public String signature() {
      return sig;
    }

    public String genericSignature() {
      return gen == null || gen.isEmpty() ? null : gen;
    }

    public ReferenceType declaringType() {
      return owner.self;
    }

    public int modifiers() {
      return mod;
    }

    public boolean isPublic() {
      return (mod & 1) != 0;
    }

    public boolean isPrivate() {
      return (mod & 2) != 0;
    }

    public boolean isProtected() {
      return (mod & 4) != 0;
    }

    public boolean isPackagePrivate() {
      return (mod & 7) == 0;
    }

    public boolean isStatic() {
      return (mod & 8) != 0;
    }

    public boolean isFinal() {
      return (mod & 0x10) != 0;
    }

    public boolean isSynchronized() {
      return (mod & 0x20) != 0;
    }

    public boolean isBridge() {
      return (mod & 0x40) != 0;
    }

    public boolean isVarArgs() {
      return (mod & 0x80) != 0;
    }

    public boolean isNative() {
      return (mod & 0x100) != 0;
    }

    public boolean isAbstract() {
      return (mod & 0x400) != 0;
    }

    public boolean isSynthetic() {
      return (mod & 0x1000) != 0;
    }

    public boolean isConstructor() {
      return name.equals("<init>");
    }

    public boolean isStaticInitializer() {
      return name.equals("<clinit>");
    }

    public boolean isObsolete() {
      return false;
    }

    public String returnTypeName() {
      return Vm.nameOfSig(sig.substring(sig.indexOf(')') + 1));
    }

    public Type returnType() throws ClassNotLoadedException {
      return owner.vm.typeOfSignature(sig.substring(sig.indexOf(')') + 1));
    }

    List<String> argSigs() {
      List<String> l = new ArrayList<String>();
      int i = 1;
      while (sig.charAt(i) != ')') {
        int s = i;
        while (sig.charAt(i) == '[') {
          i++;
        }
        if (sig.charAt(i) == 'L') {
          i = sig.indexOf(';', i);
        }
        i++;
        l.add(sig.substring(s, i));
      }
      return l;
    }

    public List<String> argumentTypeNames() {
      List<String> l = new ArrayList<String>();
      for (String s : argSigs()) {
        l.add(Vm.nameOfSig(s));
      }
      return l;
    }

    public List<String> argumentSignatures() {
      return argSigs();
    }

    public List<Type> argumentTypes() throws ClassNotLoadedException {
      List<Type> l = new ArrayList<Type>();
      for (String s : argSigs()) {
        l.add(owner.vm.typeOfSignature(s));
      }
      return l;
    }

    public List<Location> allLineLocations() throws AbsentInformationException {
      Wire.Reply r;
      try {
        r = owner.vm.w.send(6, 1, new Out().id(owner.id, owner.vm.w.typeIdSize).id(id, owner.vm.w.methodIdSize));
      } catch (java.io.IOException e) {
        throw new VMDisconnectedException();
      }
      if (r.error != 0) {
        throw new AbsentInformationException();
      }
      In in = r.data;
      long start = in.i64();
      in.i64();
      int n = in.i32();
      List<Location> l = new ArrayList<Location>();
      for (int i = 0; i < n; i++) {
        long idx = in.i64();
        int line = in.i32();
        l.add(Loc.make(owner.vm, owner, id, idx, line));
      }
      if (start == -1 || l.isEmpty()) {
        throw new AbsentInformationException();
      }
      return l;
    }

    public List<Location> allLineLocations(String stratum, String sourceName) throws AbsentInformationException {
      return allLineLocations();
    }

    public List<Location> locationsOfLine(int line) throws AbsentInformationException {
      List<Location> out = new ArrayList<Location>();
      for (Location l : allLineLocations()) {
        if (l.lineNumber() == line) {
          out.add(l);
        }
      }
      return out;
    }

    public Location location() {
      try {
        List<Location> l = allLineLocations();
        return l.get(0);
      } catch (AbsentInformationException e) {
        return Loc.make(owner.vm, owner, id, 0, -1);
      }
    }

    public Location locationOfCodeIndex(long idx) {
      Location best = null;
      try {
        for (Location l : allLineLocations()) {
          if (l.codeIndex() <= idx && (best == null || l.codeIndex() > best.codeIndex())) {
            best = l;
          }
        }
      } catch (AbsentInformationException e) {
        // no lines
      }
      return best != null ? best : Loc.make(owner.vm, owner, id, idx, -1);
    }

    public byte[] bytecodes() {
      In in = owner.vm.call(6, 3, new Out().id(owner.id, owner.vm.w.typeIdSize).id(id, owner.vm.w.methodIdSize))
          .data;
      return null;
    }

    void loadVars() throws AbsentInformationException {
      if (vars != null) {
        return;
      }
      Wire.Reply r;
      try {
        r = owner.vm.w.send(6, 2, new Out().id(owner.id, owner.vm.w.typeIdSize).id(id, owner.vm.w.methodIdSize));
      } catch (java.io.IOException e) {
        throw new VMDisconnectedException();
      }
      if (r.error != 0) {
        throw new AbsentInformationException();
      }
      In in = r.data;
      argCount = in.i32();
      int n = in.i32();
      List<LocalVariable> l = new ArrayList<LocalVariable>();
      for (int i = 0; i < n; i++) {
        long codeIdx = in.i64();
        String nm = in.str();
        String sg = in.str();
        int len = in.i32();
        int slot = in.i32();
        Var v = new Var(this, codeIdx, nm, sg, len, slot, i < argCount);
        l.add(Proxies.<LocalVariable>make(v, LocalVariable.class));
      }
      vars = l;
    }

    public List<LocalVariable> variables() throws AbsentInformationException {
      loadVars();
      return Collections.unmodifiableList(vars);
    }

    public List<LocalVariable> variablesByName(String name) throws AbsentInformationException {
      List<LocalVariable> l = new ArrayList<LocalVariable>();
      for (LocalVariable v : variables()) {
        if (v.name().equals(name)) {
          l.add(v);
        }
      }
      return l;
    }

    public List<LocalVariable> arguments() throws AbsentInformationException {
      loadVars();
      List<LocalVariable> l = new ArrayList<LocalVariable>();
      for (LocalVariable v : vars) {
        if (v.isArgument()) {
          l.add(v);
        }
      }
      return l;
    }

    public boolean equals(Object o) {
      Object i = Proxies.implOf(o);
      return i instanceof Mth && ((Mth) i).id == id && ((Mth) i).owner == owner;
    }

    public int hashCode() {
      return (int) id;
    }

    public int compareTo(Object o) {
      return name.compareTo(((Method) o).name());
    }

    public String toString() {
      return owner.name() + "." + name + "(" + String.join(", ", argumentTypeNames()) + ")";
    }
  }

  public static final class Var {
    final Mth method;
    final long codeIndex;
    final String name;
    final String sig;
    final int length;
    final int slot;
    final boolean arg;

    Var(Mth method, long codeIndex, String name, String sig, int length, int slot, boolean arg) {
      this.method = method;
      this.codeIndex = codeIndex;
      this.name = name;
      this.sig = sig;
      this.length = length;
      this.slot = slot;
      this.arg = arg;
    }

    public VirtualMachine virtualMachine() {
      return method.owner.vm.proxy;
    }

    public String name() {
      return name;
    }

    public String typeName() {
      return Vm.nameOfSig(sig);
    }

    public Type type() throws ClassNotLoadedException {
      return method.owner.vm.typeOfSignature(sig);
    }

    public String signature() {
      return sig;
    }

    public String genericSignature() {
      return null;
    }

    public boolean isArgument() {
      return arg;
    }

    public boolean isVisible(StackFrame frame) {
      long idx = frame.location().codeIndex();
      return idx >= codeIndex && idx < codeIndex + length;
    }

    public int compareTo(Object o) {
      return Integer.compare(slot, ((Var) Proxies.implOf(o)).slot);
    }

    public String toString() {
      return name;
    }

    public boolean equals(Object o) {
      Object i = Proxies.implOf(o);
      return i instanceof Var && ((Var) i).slot == slot && ((Var) i).method == method && ((Var) i).name.equals(name);
    }

    public int hashCode() {
      return slot * 31 + name.hashCode();
    }
  }
}
