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
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

public interface DirStateFactory extends StateFactory {
  public static class Result {
    private Object obj;
    private Attributes attrs;

    public Result(Object obj, Attributes outAttrs) {
      this.obj = obj;
      this.attrs = outAttrs;
    }

    public Object getObject() {
      return obj;
    }

    public Attributes getAttributes() {
      return attrs;
    }
  }

  Result getStateToBind(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment, Attributes inAttrs)
      throws NamingException;
}
