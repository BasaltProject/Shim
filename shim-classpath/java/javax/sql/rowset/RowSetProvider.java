/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

import java.sql.SQLException;

public class RowSetProvider {
  private static final String ROWSET_FACTORY_IMPL = "com.sun.rowset.RowSetFactoryImpl";
  private static final String ROWSET_FACTORY_NAME = "javax.sql.rowset.RowSetFactory";

  protected RowSetProvider() {
  }

  public static RowSetFactory newFactory() throws SQLException {
    String className = System.getProperty(ROWSET_FACTORY_NAME);
    if (className != null) {
      return load(className, null);
    }
    java.util.ServiceLoader<RowSetFactory> loader = java.util.ServiceLoader.load(RowSetFactory.class);
    for (RowSetFactory f : loader) {
      return f;
    }
    return load(ROWSET_FACTORY_IMPL, null);
  }

  public static RowSetFactory newFactory(String factoryClassName, ClassLoader cl) throws SQLException {
    if (factoryClassName == null) {
      throw new SQLException("Error: factoryClassName cannot be null");
    }
    return load(factoryClassName, cl);
  }

  private static RowSetFactory load(String className, ClassLoader cl) throws SQLException {
    try {
      Class<?> c = cl != null ? Class.forName(className, true, cl) : Class.forName(className);
      return (RowSetFactory) c.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new SQLException("RowSetFactory: " + className + " could not be instantiated: " + e, e);
    }
  }
}
