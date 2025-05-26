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

#ifndef LZMA_UTIL_H
#define LZMA_UTIL_H

#include <shim/lzma.h>
#include <C/Types.h>
#include <shim/system/system.h>
#include <shim/util/allocator.h>

namespace vm {

const unsigned Padding = 16;

class LzmaAllocator {
 public:
  LzmaAllocator(shim::util::Alloc* a) : a(a)
  {
    allocator.Alloc = allocate;
    allocator.Free = free;
  }

  ISzAlloc allocator;
  shim::util::Alloc* a;

  static void* allocate(void* allocator, size_t size)
  {
    uint8_t* p = static_cast<uint8_t*>(
        static_cast<LzmaAllocator*>(allocator)->a->allocate(size + Padding));
    int32_t size32 = size;
    memcpy(p, &size32, 4);
    return p + Padding;
  }

  static void free(void* allocator, void* address)
  {
    if (address) {
      void* p = static_cast<uint8_t*>(address) - Padding;
      int32_t size32;
      memcpy(&size32, p, 4);
      static_cast<LzmaAllocator*>(allocator)->a->free(p, size32 + Padding);
    }
  }
};

}  // namespace vm

#endif  // LZMA_UTIL_H
