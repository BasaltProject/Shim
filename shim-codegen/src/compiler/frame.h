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

#ifndef SHIM_CODEGEN_COMPILER_FRAME_H
#define SHIM_CODEGEN_COMPILER_FRAME_H

namespace shim {
namespace codegen {
namespace compiler {

unsigned totalFrameSize(Context* c);

int frameIndex(Context* c, int localIndex);

unsigned frameIndexToOffset(Context* c, unsigned frameIndex);

unsigned offsetToFrameIndex(Context* c, unsigned offset);

unsigned frameBase(Context* c);

class FrameIterator {
 public:
  class Element {
   public:
    Element(Value* value, unsigned localIndex);

    int frameIndex(Context* c);

    Value* const value;
    const unsigned localIndex;
  };

  FrameIterator(Context* c,
                Stack* stack,
                Local* locals,
                bool includeEmpty = false);

  bool hasMore();

  Element next(Context* c);

  Stack* stack;
  Local* locals;
  int localIndex;
  bool includeEmpty;
};

class Local {
 public:
  Value* value;
};

class Stack {
 public:
  Stack(unsigned index, Value* value, Stack* next)
      : index(index), value(value), next(next)
  {
  }

  unsigned index;
  Value* value;
  Stack* next;
};

Stack* stack(Context* c, Value* value, Stack* next);

}  // namespace compiler
}  // namespace codegen
}  // namespace shim

#endif  // SHIM_CODEGEN_COMPILER_FRAME_H
