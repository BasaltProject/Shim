/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.spi;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;

public interface InitialContextFactory {
  Context getInitialContext(Hashtable<?, ?> environment) throws NamingException;
}
