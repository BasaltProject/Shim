/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

import java.util.Objects;
import java.util.Set;

public final class ResolvedModule {
  private final Configuration cf;
  private final ModuleReference mref;

  ResolvedModule(Configuration cf, ModuleReference mref) {
    this.cf = Objects.requireNonNull(cf);
    this.mref = Objects.requireNonNull(mref);
  }

  public Configuration configuration() {
    return cf;
  }

  public ModuleReference reference() {
    return mref;
  }

  public String name() {
    return mref.descriptor().name();
  }

  public Set<ResolvedModule> reads() {
    return cf.readsOf(this);
  }

  @Override
  public int hashCode() {
    return cf.hashCode() * 31 + mref.hashCode();
  }

  @Override
  public boolean equals(Object ob) {
    if (!(ob instanceof ResolvedModule)) {
      return false;
    }
    ResolvedModule that = (ResolvedModule) ob;
    return mref.equals(that.mref) && cf.equals(that.cf);
  }

  @Override
  public String toString() {
    return System.identityHashCode(cf) + "/" + name();
  }
}
