/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class OpenType<T> implements Serializable {
  private static final long serialVersionUID = -9195195325186646468L;

  public static final List<String> ALLOWED_CLASSNAMES_LIST = Collections.unmodifiableList(Arrays.asList(
      "java.lang.Void", "java.lang.Boolean", "java.lang.Character", "java.lang.Byte", "java.lang.Short",
      "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.String",
      "java.math.BigDecimal", "java.math.BigInteger", "java.util.Date", "javax.management.ObjectName",
      CompositeData.class.getName(), TabularData.class.getName()));

  @Deprecated
  public static final String[] ALLOWED_CLASSNAMES = ALLOWED_CLASSNAMES_LIST.toArray(new String[0]);

  private final String className;
  private final String description;
  private final String typeName;
  private transient boolean isArray;

  protected OpenType(String className, String typeName, String description) throws OpenDataException {
    checkClassNameOverrides(className);
    checkNotNullOrEmpty(className, "className");
    checkNotNullOrEmpty(typeName, "typeName");
    checkNotNullOrEmpty(description, "description");
    String name = className;
    int dims = 0;
    while (name.startsWith("[")) {
      dims++;
      name = name.substring(1);
    }
    if (dims > 0) {
      isArray = true;
      if (name.startsWith("L") && name.endsWith(";")) {
        name = name.substring(1, name.length() - 1);
      }
    }
    if (!ALLOWED_CLASSNAMES_LIST.contains(name) && !(dims > 0 && name.length() == 1)) {
      throw new OpenDataException("Argument className=\"" + className + "\" is not a valid class name");
    }
    this.className = className;
    this.typeName = typeName;
    this.description = description;
  }

  private static void checkClassNameOverrides(String className) throws OpenDataException {
  }

  private static void checkNotNullOrEmpty(String value, String name) {
    if (value == null || value.trim().equals("")) {
      throw new IllegalArgumentException("Argument " + name + " cannot be null or empty");
    }
  }

  public String getClassName() {
    return className;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getDescription() {
    return description;
  }

  public boolean isArray() {
    return isArray;
  }

  public abstract boolean isValue(Object obj);

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();
}
