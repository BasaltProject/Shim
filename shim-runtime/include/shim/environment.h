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

#ifndef SHIM_ENVIRONMENT_H
#define SHIM_ENVIRONMENT_H

#ifndef SHIM_TARGET_FORMAT
#error build system should have defined SHIM_TARGET_FORMAT
#endif

#ifndef SHIM_TARGET_ARCH
#error build system should have defined SHIM_TARGET_ARCH
#endif

#define SHIM_FORMAT_UNKNOWN 0
#define SHIM_FORMAT_ELF 1
#define SHIM_FORMAT_PE 2
#define SHIM_FORMAT_MACHO 3

#define SHIM_ARCH_UNKNOWN 0
#define SHIM_ARCH_X86 (1 << 8)
#define SHIM_ARCH_X86_64 (2 << 8)
#define SHIM_ARCH_ARM (3 << 8)
#define SHIM_ARCH_ARM64 (4 << 8)

#endif
