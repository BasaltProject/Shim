/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss;

import org.ietf.jgss.ChannelBinding;

public final class CtxFlags {
  public boolean mutual = true;
  public boolean replay = true;
  public boolean seq = true;
  public boolean conf = true;
  public boolean integ = true;
  public boolean deleg;
  public boolean anon;
  public boolean delegPolicy;
  public int lifetime;
  public ChannelBinding cb;
}
