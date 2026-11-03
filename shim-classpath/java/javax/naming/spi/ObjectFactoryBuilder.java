/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.spi;

public interface ObjectFactoryBuilder {
  javax.naming.spi.ObjectFactory createObjectFactory(java.lang.Object object, java.util.Hashtable<?, ?> hashtable) throws javax.naming.NamingException;
}
