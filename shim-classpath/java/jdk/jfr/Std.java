/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class Std {
  private Std() {
  }

  static ValueDescriptor vd(String type, String name, String label, boolean cp, List<ValueDescriptor> fields,
      boolean array) {
    List<AnnotationElement> a = new ArrayList<AnnotationElement>();
    if (label != null) {
      a.add(new AnnotationElement(Label.class, label));
    }
    return ValueDescriptor.internal(type, name, a, cp, fields, array);
  }

  static ValueDescriptor str(String name, String label) {
    return vd("java.lang.String", name, label, false, new ArrayList<ValueDescriptor>(), false);
  }

  static ValueDescriptor prim(String type, String name, String label) {
    return vd(type, name, label, false, new ArrayList<ValueDescriptor>(), false);
  }

  static ValueDescriptor symbol(String name, String label) {
    return vd("jdk.types.Symbol", name, label, true, new ArrayList<ValueDescriptor>(Arrays.asList(str("string",
        null))), false);
  }

  static ValueDescriptor clazz(String name, String label) {
    return vd("java.lang.Class", name, label, true, new ArrayList<ValueDescriptor>(Arrays.asList(
        vd("jdk.types.ClassLoader", "classLoader", "Class Loader", true, new ArrayList<ValueDescriptor>(
            Arrays.asList(symbol("name", "Name"))), false),
        symbol("name", "Name"),
        vd("jdk.types.Package", "package", "Package", true, new ArrayList<ValueDescriptor>(Arrays.asList(
            symbol("name", "Name"), prim("boolean", "exported", "Exported"))), false),
        prim("int", "modifiers", "Access Modifiers"), prim("boolean", "hidden", "Hidden"))), false);
  }

  static ValueDescriptor method(String name, String label) {
    return vd("jdk.types.Method", name, label, true, new ArrayList<ValueDescriptor>(Arrays.asList(
        clazz("type", "Type"), symbol("name", "Name"), symbol("descriptor", "Descriptor"),
        prim("int", "modifiers", "Access Modifiers"), prim("boolean", "hidden", "Hidden"))), false);
  }

  static ValueDescriptor stackTrace() {
    List<ValueDescriptor> frame = new ArrayList<ValueDescriptor>(Arrays.asList(
        method("method", "Java Method"), prim("int", "lineNumber", "Line Number"),
        prim("int", "bytecodeIndex", "Bytecode Index"),
        vd("jdk.types.FrameType", "type", "Frame Type", true, new ArrayList<ValueDescriptor>(Arrays.asList(
            str("description", null))), false)));
    List<ValueDescriptor> st = new ArrayList<ValueDescriptor>(Arrays.asList(
        prim("boolean", "truncated", "Truncated"),
        vd("jdk.types.StackFrame", "frames", "Stack Frames", false, frame, true)));
    List<AnnotationElement> a = new ArrayList<AnnotationElement>();
    a.add(new AnnotationElement(Label.class, "Stack Trace"));
    a.add(new AnnotationElement(Description.class, "Stack Trace starting from the method the event was committed in"));
    return ValueDescriptor.internal("jdk.types.StackTrace", "stackTrace", a, true, st, false);
  }
}
