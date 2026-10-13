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

#include "shim/common.h"

#include <shim/codegen/targets.h>

#include "shim/environment.h"

namespace shim {
namespace codegen {

Architecture* makeArchitectureNative(vm::System* system,
                                     bool useNativeFeatures UNUSED)
{
#ifndef SHIM_TARGET_ARCH
#error "Must specify native target!"
#endif

#if SHIM_TARGET_ARCH == SHIM_ARCH_UNKNOWN
  system->abort();
  return 0;
#elif(SHIM_TARGET_ARCH == SHIM_ARCH_X86) \
    || (SHIM_TARGET_ARCH == SHIM_ARCH_X86_64)
  return makeArchitectureX86(system, useNativeFeatures);
#elif (SHIM_TARGET_ARCH == SHIM_ARCH_ARM) \
    || (SHIM_TARGET_ARCH == SHIM_ARCH_ARM64)
  return makeArchitectureArm(system, useNativeFeatures);
#else
#error "Unsupported codegen target"
#endif
}

}  // namespace codegen
}  // namespace shim
