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

#ifndef ARCH_H
#define ARCH_H

#ifdef _MSC_VER
#include "windows.h"
#pragma push_macro("assert")
#include "intrin.h"
#pragma pop_macro("assert")
#undef interface
#endif

#include "shim/common.h"

extern "C" void NO_RETURN vmJump(void* address,
                                 void* frame,
                                 void* stack,
                                 void* thread,
                                 uintptr_t returnLow,
                                 uintptr_t returnHigh);

namespace vm {

inline void compileTimeMemoryBarrier()
{
#ifdef _MSC_VER
  _ReadWriteBarrier();
#else
  __asm__ __volatile__("" : : : "memory");
#endif
}

}  // namespace vm

#if (defined ARCH_x86_32) || (defined ARCH_x86_64)
#include "x86.h"
#elif (defined ARCH_arm) || (defined ARCH_arm64)
#include "arm.h"
#else
#error unsupported architecture
#endif

#endif  // ARCH_H
