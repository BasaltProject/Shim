/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package netscape.javascript;

public abstract class JSObject {
  protected JSObject() {
  }

  public abstract java.lang.Object call(java.lang.String string, java.lang.Object... objects) throws netscape.javascript.JSException;
  public abstract java.lang.Object eval(java.lang.String string) throws netscape.javascript.JSException;
  public abstract java.lang.Object getMember(java.lang.String string) throws netscape.javascript.JSException;
  public abstract java.lang.Object getSlot(int i) throws netscape.javascript.JSException;
  public abstract void removeMember(java.lang.String string) throws netscape.javascript.JSException;
  public abstract void setMember(java.lang.String string, java.lang.Object object) throws netscape.javascript.JSException;
  public abstract void setSlot(int i, java.lang.Object object) throws netscape.javascript.JSException;
}
