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

#ifndef JNI_MD_H
#define JNI_MD_H

#include "stdint.h"

#if (defined __MINGW32__) || (defined _MSC_VER)
#define JNIEXPORT __declspec(dllexport)
#define JNICALL __stdcall
#else  // not (defined __MINGW32__) || (defined _MSC_VER)
#define JNIEXPORT __attribute__((visibility("default"))) __attribute__((used))
#define JNICALL
#endif  // not (defined __MINGW32__) || (defined _MSC_VER)

#define JNIIMPORT

typedef int32_t jint;
typedef int64_t jlong;
typedef int8_t jbyte;

#endif  // JNI_MD_H
