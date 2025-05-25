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

public class VMMethod {
  public byte vmFlags;
  public byte returnCode;
  public byte parameterCount;
  public byte parameterFootprint;
  public short flags;
  public short offset;
  public int nativeID;
  public int runtimeDataIndex;
  public byte[] name;
  public byte[] spec;
  public MethodAddendum addendum;
  public VMClass class_;
  public Code code;
  
  public boolean hasAnnotations() {
    return addendum != null && addendum.annotationTable != null;
  }
}
