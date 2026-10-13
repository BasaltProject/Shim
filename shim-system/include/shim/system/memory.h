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

#ifndef SHIM_SYSTEM_MEMORY_H
#define SHIM_SYSTEM_MEMORY_H

#include <shim/util/slice.h>

#include <stdint.h>

namespace shim {
namespace system {

class Memory {
 public:
  enum Permissions {
    Read = 1 << 0,
    Write = 1 << 1,
    Execute = 1 << 2,

    // Utility munged constants
    ReadWrite = Read | Write,
    ReadExecute = Read | Execute,
    ReadWriteExecute = Read | Write | Execute
  };

  static const size_t PageSize;

  // Allocate a contiguous range of pages.
  static util::Slice<uint8_t> allocate(size_t sizeInBytes, Permissions perms = ReadWrite);

  // Free a contiguous range of pages.
  static void free(util::Slice<uint8_t> pages);

  // TODO: In the future:
  // static void setPermissions(util::Slice<uint8_t> pages, Permissions perms);
};

}  // namespace system
}  // namespace shim

#endif
