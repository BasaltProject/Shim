/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.directory;

import javax.naming.NamingException;

public class AttributeModificationException extends NamingException {
  private static final long serialVersionUID = 8060676069678710186L;

  private ModificationItem[] unexecs = null;

  public AttributeModificationException(String explanation) {
    super(explanation);
  }

  public AttributeModificationException() {
    super();
  }

  public void setUnexecutedModifications(ModificationItem[] e) {
    unexecs = e;
  }

  public ModificationItem[] getUnexecutedModifications() {
    return unexecs;
  }

  public String toString() {
    String orig = super.toString();
    if (unexecs != null) {
      orig += "First unexecuted modification: " + unexecs[0].toString();
    }
    return orig;
  }
}
