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

package java.util.concurrent.atomic;

public class AtomicBoolean implements java.io.Serializable {
  private static final long serialVersionUID = 4654671469794556979L;
  
  private static final int FALSE_VALUE = 0;
  private static final int TRUE_VALUE = 1;
  
  private final AtomicInteger value;
  
  public AtomicBoolean() {
    this(false);
  }
  
  public AtomicBoolean(boolean initialValue) {
    value = new AtomicInteger(intValue(initialValue));
  }
  
  private static int intValue(boolean value) {
    return value ? TRUE_VALUE : FALSE_VALUE;
  }
  
  private static boolean booleanValue(int value) {
    return value == TRUE_VALUE;
  }
  
  public boolean get() {
    return booleanValue(value.get());
  }
  
  public boolean compareAndSet(boolean expect, boolean update) {
    return value.compareAndSet(intValue(expect), intValue(update));
  }
  
  public boolean weakCompareAndSet(boolean expect, boolean update) {
    return value.weakCompareAndSet(intValue(expect), intValue(update));
  }
  
  public void set(boolean newValue) {
    value.set(intValue(newValue));
  }
  
  public void lazySet(boolean newValue) {
    value.lazySet(intValue(newValue));
  }
  
  public boolean getAndSet(boolean newValue) {
    int intResult = value.getAndSet(intValue(newValue));
    
    return booleanValue(intResult);
  }
  
  public boolean getPlain() {
    return booleanValue(value.getPlain());
  }

  public void setPlain(boolean newValue) {
    value.setPlain(intValue(newValue));
  }

  public boolean getOpaque() {
    return booleanValue(value.getOpaque());
  }

  public void setOpaque(boolean newValue) {
    value.setOpaque(intValue(newValue));
  }

  public boolean getAcquire() {
    return booleanValue(value.getAcquire());
  }

  public void setRelease(boolean newValue) {
    value.setRelease(intValue(newValue));
  }

  public final boolean compareAndExchange(boolean expectedValue, boolean newValue) {
    int cur = value.compareAndExchange(intValue(expectedValue), intValue(newValue));
    return booleanValue(cur);
  }

  public final boolean compareAndExchangeAcquire(boolean expectedValue, boolean newValue) {
    return compareAndExchange(expectedValue, newValue);
  }

  public final boolean compareAndExchangeRelease(boolean expectedValue, boolean newValue) {
    return compareAndExchange(expectedValue, newValue);
  }

  public boolean weakCompareAndSetVolatile(boolean expectedValue, boolean newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetAcquire(boolean expectedValue, boolean newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetRelease(boolean expectedValue, boolean newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  public boolean weakCompareAndSetPlain(boolean expectedValue, boolean newValue) {
    return compareAndSet(expectedValue, newValue);
  }

  @Override
  public String toString() {
    return Boolean.toString(get());
  }
}
