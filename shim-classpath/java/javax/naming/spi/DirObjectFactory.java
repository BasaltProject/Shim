/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.spi;

public interface DirObjectFactory extends javax.naming.spi.ObjectFactory {
  java.lang.Object getObjectInstance(java.lang.Object object, javax.naming.Name name, javax.naming.Context context, java.util.Hashtable<?, ?> hashtable, javax.naming.directory.Attributes attributes) throws java.lang.Exception;
}
