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

#include <stdint.h>

#include "jni.h"
#include "jni-util.h"

extern "C" JNIEXPORT jboolean JNICALL
    Java_java_nio_ByteOrder_isNativeBigEndian(JNIEnv*, jclass)
{
  union {
    uint32_t i;
    char c[4];
  } u = {0x01020304};

  if (u.c[0] == 1)
    return JNI_TRUE;
  return JNI_FALSE;
}
