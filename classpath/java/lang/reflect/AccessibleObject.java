/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

import java.lang.annotation.Annotation;

public abstract class AccessibleObject implements AnnotatedElement {
  protected static final int Accessible = 1 << 0;

  // Access and property flags for Member descendants
  protected static final int ACC_VARARGS = 0x0080;
  protected static final int ACC_SYNTHETIC = 0x1000;
  
  public boolean isAnnotationPresent
    (Class<? extends Annotation> class_)
  {
    return getAnnotation(class_) != null;
  }

  public abstract boolean isAccessible();

  public abstract void setAccessible(boolean v);

  public static void setAccessible(AccessibleObject[] array, boolean v) {
    for (AccessibleObject o: array) o.setAccessible(v);
  }
}
