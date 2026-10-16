/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class StackTraceElement implements java.io.Serializable {
  private static final long serialVersionUID = 6992337162326171013L;
  private static final java.io.ObjectStreamField[] serialPersistentFields = {
    new java.io.ObjectStreamField("classLoaderName", String.class),
    new java.io.ObjectStreamField("moduleName", String.class),
    new java.io.ObjectStreamField("moduleVersion", String.class),
    new java.io.ObjectStreamField("declaringClass", String.class),
    new java.io.ObjectStreamField("methodName", String.class),
    new java.io.ObjectStreamField("fileName", String.class),
    new java.io.ObjectStreamField("lineNumber", int.class),
    new java.io.ObjectStreamField("format", byte.class)
  };

  private static int NativeLine = -1;

  private String class_;
  private String method;
  private String file;
  private int line;

  public StackTraceElement(String classLoaderName, String moduleName, String moduleVersion,
                           String declaringClass, String methodName, String fileName,
                           int lineNumber)
  {
    this(declaringClass, methodName, fileName, lineNumber);
  }

  public StackTraceElement(String class_, String method, String file,
                           int line)
  {
    this.class_ = class_;
    this.method = method;
    this.file = file;
    this.line = line;
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    java.io.ObjectOutputStream.PutField fields = s.putFields();
    fields.put("classLoaderName", (Object) null);
    fields.put("moduleName", (Object) null);
    fields.put("moduleVersion", (Object) null);
    fields.put("declaringClass", class_);
    fields.put("methodName", method);
    fields.put("fileName", file);
    fields.put("lineNumber", line);
    fields.put("format", (byte) 0);
    s.writeFields();
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    java.io.ObjectInputStream.GetField fields = s.readFields();
    class_ = (String) fields.get("declaringClass", null);
    method = (String) fields.get("methodName", null);
    file = (String) fields.get("fileName", null);
    line = fields.get("lineNumber", -1);
  }

  public int hashCode() {
    return class_.hashCode() ^ method.hashCode() ^ line;
  }

  public boolean equals(Object o) {
    if (o instanceof StackTraceElement) {
      StackTraceElement e = (StackTraceElement) o;
      return class_.equals(e.class_)
        && method.equals(e.method)
        && line == e.line;
    } else {
      return false;
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(class_).append(".").append(method);

    if (line == NativeLine) {
      sb.append(" (native)");
    } else if (line >= 0) {
      sb.append(" (line ").append(line).append(")");
    }

    return sb.toString();
  }

  public String getClassName() {
    return class_;
  }

  public String getMethodName() {
    return method;
  }

  public String getFileName() {
    return file;
  }

  public int getLineNumber() {
    return line;
  }

  public boolean isNativeMethod() {
    return line == NativeLine;
  }

  public String getClassLoaderName() {
    return null;
  }

  public String getModuleName() {
    return null;
  }

  public String getModuleVersion() {
    return null;
  }
}
