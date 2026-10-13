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

#ifndef HEAPWALK_H
#define HEAPWALK_H

#include "shim/common.h"
#include "java-common.h"

namespace vm {

class Thread;

class HeapMap {
 public:
  virtual int find(object value) = 0;
  virtual void dispose() = 0;
};

class HeapVisitor {
 public:
  virtual void root() = 0;
  virtual unsigned visitNew(object value) = 0;
  virtual void visitOld(object value, unsigned number) = 0;
  virtual void push(object parent, unsigned parentNumber, unsigned childOffset)
      = 0;
  virtual void pop() = 0;
};

class HeapWalker {
 public:
  virtual unsigned visitRoot(object root) = 0;
  virtual void visitAllRoots() = 0;
  virtual HeapMap* map() = 0;
  virtual void dispose() = 0;
};

HeapWalker* makeHeapWalker(Thread* t, HeapVisitor* v);

}  // namespace vm

#endif  // HEAPWALK_H
