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

  private static final int NativeLine = -2;

  private String loader;
  private String module;
  private String moduleVersion;
  private String class_;
  private String method;
  private String file;
  private int line;

  public StackTraceElement(String classLoaderName, String moduleName, String moduleVersion,
                           String declaringClass, String methodName, String fileName,
                           int lineNumber)
  {
    this(declaringClass, methodName, fileName, lineNumber);
    if (declaringClass == null) throw new NullPointerException("Declaring class is null");
    if (methodName == null) throw new NullPointerException("Method name is null");
    this.loader = classLoaderName;
    this.module = moduleName;
    this.moduleVersion = moduleVersion;
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
    fields.put("classLoaderName", loader);
    fields.put("moduleName", module);
    fields.put("moduleVersion", moduleVersion);
    fields.put("declaringClass", class_);
    fields.put("methodName", method);
    fields.put("fileName", file);
    fields.put("lineNumber", line);
    fields.put("format", (byte) 0);
    s.writeFields();
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    java.io.ObjectInputStream.GetField fields = s.readFields();
    loader = (String) fields.get("classLoaderName", null);
    module = (String) fields.get("moduleName", null);
    moduleVersion = (String) fields.get("moduleVersion", null);
    class_ = (String) fields.get("declaringClass", null);
    method = (String) fields.get("methodName", null);
    file = (String) fields.get("fileName", null);
    line = fields.get("lineNumber", -1);
  }

  public int hashCode() {
    int result = 31 * class_.hashCode() + method.hashCode();
    result = 31 * result + java.util.Objects.hashCode(loader);
    result = 31 * result + java.util.Objects.hashCode(module);
    result = 31 * result + java.util.Objects.hashCode(moduleVersion);
    result = 31 * result + java.util.Objects.hashCode(file);
    return 31 * result + line;
  }

  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof StackTraceElement)) return false;
    StackTraceElement e = (StackTraceElement) o;
    return java.util.Objects.equals(loader, e.loader) && java.util.Objects.equals(module, e.module)
        && java.util.Objects.equals(moduleVersion, e.moduleVersion) && class_.equals(e.class_)
        && method.equals(e.method) && java.util.Objects.equals(file, e.file) && line == e.line;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (loader != null && !loader.isEmpty() && !loader.equals("app")) sb.append(loader).append('/');
    if (module != null && !module.isEmpty()) {
      sb.append(module);
      if (moduleVersion != null && !moduleVersion.isEmpty() && !module.startsWith("java.")) {
        sb.append('@').append(moduleVersion);
      }
    }
    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') sb.append('/');
    sb.append(class_).append('.').append(method).append('(');
    if (line == NativeLine) {
      sb.append("Native Method");
    } else if (file == null) {
      sb.append("Unknown Source");
    } else {
      sb.append(file);
      if (line >= 0) sb.append(':').append(line);
    }
    return sb.append(')').toString();
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
    return loader;
  }

  public String getModuleName() {
    return module;
  }

  public String getModuleVersion() {
    return moduleVersion;
  }
}
