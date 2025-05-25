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

package avian;

public class VMClass {
  public short flags;
  public short vmFlags;
  public short fixedSize;
  public byte arrayElementSize;
  public byte arrayDimensions;
  public VMClass arrayElementClass;
  public int runtimeDataIndex;
  public int[] objectMask;
  public byte[] name;
  public byte[] sourceFile;
  public VMClass super_;
  public Object[] interfaceTable;
  public VMMethod[] virtualTable;
  public VMField[] fieldTable;
  /**
   * Methods declared in this class, plus any abstract virtual methods
   * inherited from implemented or extended interfaces.  If addendum
   * is non-null and addendum.declaredMethodCount is non-negative,
   * then the first addendum.declaredMethodCount methods are declared
   * methods, while the rest are abstract virtual methods.  If
   * addendum is null or addendum.declaredMethodCount is negative, all
   * are declared methods.
   */
  public VMMethod[] methodTable;
  public ClassAddendum addendum;
  public Singleton staticTable;
  public ClassLoader loader;
  public byte[] source;
}
