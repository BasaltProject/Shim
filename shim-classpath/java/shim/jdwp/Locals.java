/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.jdwp;

import java.io.InputStream;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.LocalVariableInfo;
import java.lang.classfile.attribute.LocalVariableTypeInfo;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Local variable tables and values captured by the JIT debug hooks. */
final class Locals {
  static final class Var {
    int start;
    int length;
    int slot;
    String name;
    String sig;
    String generic = "";
  }

  static final class Info {
    List<Var> vars = new ArrayList<Var>();
    int maxLocals;
    int argSlots;
    boolean isStatic;
  }

  static final class Snap {
    long[] bits;
    Object[] refs;
    Map<Integer, Object> pending = new HashMap<Integer, Object>();
  }

  private static final Map<Class<?>, Map<String, Info>> cache = new HashMap<Class<?>, Map<String, Info>>();

  private static native long[] localBits();

  private static native Object[] localRefs();

  private static native void putLocalBits(int slot, long bits);

  private static native void putLocalRef(int slot, Object ref);

  static native Thread monitorOwner(Object o);

  static native int monitorDepth(Object o);

  static Snap capture() {
    Snap s = new Snap();
    s.bits = localBits();
    s.refs = localRefs();
    return s;
  }

  static final Object NULL = new Object();

  static void apply(Snap s) {
    for (Map.Entry<Integer, Object> e : s.pending.entrySet()) {
      int slot = e.getKey().intValue();
      Object v = e.getValue();
      if (v instanceof Locals.Raw) {
        putLocalBits(slot, ((Raw) v).bits);
      } else {
        putLocalRef(slot, v == NULL ? null : v);
      }
    }
    s.pending.clear();
  }

  static final class Raw {
    final long bits;

    Raw(long bits) {
      this.bits = bits;
    }
  }

  static synchronized Info info(Class<?> c, String name, String desc) {
    Map<String, Info> m = cache.get(c);
    if (m == null) {
      m = parse(c);
      cache.put(c, m);
    }
    return m.get(name + desc);
  }

  private static Map<String, Info> parse(Class<?> c) {
    Map<String, Info> out = new HashMap<String, Info>();
    try {
      String res = c.getName().replace('.', '/') + ".class";
      ClassLoader l = c.getClassLoader();
      InputStream is = l == null ? ClassLoader.getSystemResourceAsStream(res) : l.getResourceAsStream(res);
      if (is == null) {
        return out;
      }
      byte[] bytes;
      try {
        bytes = is.readAllBytes();
      } finally {
        is.close();
      }
      ClassModel cm = ClassFile.of().parse(bytes);
      for (MethodModel mm : cm.methods()) {
        java.util.Optional<CodeModel> code = mm.code();
        if (!code.isPresent()) {
          continue;
        }
        Info info = new Info();
        info.isStatic = mm.flags().has(AccessFlag.STATIC);
        info.maxLocals = code.get() instanceof java.lang.classfile.attribute.CodeAttribute
            ? ((java.lang.classfile.attribute.CodeAttribute) code.get()).maxLocals() : 0;
        info.argSlots = (info.isStatic ? 0 : 1) + argSlots(mm.methodType().stringValue());
        Map<Long, String> generics = new HashMap<Long, String>();
        for (java.lang.classfile.attribute.LocalVariableTypeTableAttribute lt :
            code.get().findAttribute(Attributes.localVariableTypeTable()).stream().toList()) {
          for (LocalVariableTypeInfo li : lt.localVariableTypes()) {
            generics.put(Long.valueOf(((long) li.startPc() << 32) | li.slot()), li.signature().stringValue());
          }
        }
        for (java.lang.classfile.attribute.LocalVariableTableAttribute lv :
            code.get().findAttribute(Attributes.localVariableTable()).stream().toList()) {
          for (LocalVariableInfo li : lv.localVariables()) {
            Var v = new Var();
            v.start = li.startPc();
            v.length = li.length();
            v.slot = li.slot();
            v.name = li.name().stringValue();
            v.sig = li.type().stringValue();
            String g = generics.get(Long.valueOf(((long) v.start << 32) | v.slot));
            if (g != null) {
              v.generic = g;
            }
            info.vars.add(v);
          }
        }
        out.put(mm.methodName().stringValue() + mm.methodType().stringValue(), info);
      }
    } catch (Throwable t) {
      return out;
    }
    return out;
  }

  static int argSlots(String desc) {
    int n = 0;
    int i = 1;
    while (desc.charAt(i) != ')') {
      char ch = desc.charAt(i);
      if (ch == 'J' || ch == 'D') {
        n += 2;
        i++;
      } else if (ch == 'L') {
        n++;
        i = desc.indexOf(';', i) + 1;
      } else if (ch == '[') {
        while (desc.charAt(i) == '[') {
          i++;
        }
        if (desc.charAt(i) == 'L') {
          i = desc.indexOf(';', i) + 1;
        } else {
          i++;
        }
        n++;
      } else {
        n++;
        i++;
      }
    }
    return n;
  }
}
