/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.io.Serializable;

public abstract class QueryEval implements Serializable {
  private static final long serialVersionUID = 2675899265640874796L;

  private static final ThreadLocal<MBeanServer> server = new ThreadLocal<MBeanServer>();

  public QueryEval() {
  }

  public void setMBeanServer(MBeanServer s) {
    server.set(s);
  }

  public static MBeanServer getMBeanServer() {
    return server.get();
  }
}
