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

#ifndef SHIM_UTIL_ALLOCATOR_H
#define SHIM_UTIL_ALLOCATOR_H

#include <stddef.h>

namespace shim {
namespace util {

class AllocOnly {
 public:
  virtual void* allocate(size_t size) = 0;
};

class Alloc : public AllocOnly {
 public:
  virtual void free(const void* p, size_t size) = 0;
};

class Allocator : public Alloc {
 public:
  virtual void* tryAllocate(size_t size) = 0;
};

}  // namespace util
}  // namespace shim

inline void* operator new(size_t size, shim::util::AllocOnly* allocator)
{
  return allocator->allocate(size);
}

#endif  // SHIM_UTIL_ALLOCATOR_H
