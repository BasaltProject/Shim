/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.naming.Context;
import javax.naming.NamingException;

public abstract class ControlFactory {
  protected ControlFactory() {
  }

  public abstract Control getControlInstance(Control ctl) throws NamingException;

  public static Control getControlInstance(Control ctl, Context ctx, Hashtable<?, ?> env) throws NamingException {
    String facs = env == null ? null : (String) env.get("java.naming.factory.control");
    if (facs == null) {
      facs = System.getProperty("java.naming.factory.control");
    }
    if (facs != null) {
      StringTokenizer parser = new StringTokenizer(facs, ":");
      while (parser.hasMoreTokens()) {
        String className = parser.nextToken();
        try {
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          Class<?> c = cl != null ? Class.forName(className, true, cl) : Class.forName(className);
          ControlFactory factory = (ControlFactory) c.getDeclaredConstructor().newInstance();
          Control answer = factory.getControlInstance(ctl);
          if (answer != null) {
            return answer;
          }
        } catch (NamingException e) {
          throw e;
        } catch (ReflectiveOperationException e) {
        }
      }
    }
    return ctl;
  }
}
