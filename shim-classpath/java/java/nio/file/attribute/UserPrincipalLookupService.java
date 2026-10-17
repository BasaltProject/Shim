/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.attribute;

import java.io.IOException;

public abstract class UserPrincipalLookupService {
  protected UserPrincipalLookupService() { }

  public abstract UserPrincipal lookupPrincipalByName(String name) throws IOException;

  public abstract GroupPrincipal lookupPrincipalByGroupName(String group) throws IOException;
}
