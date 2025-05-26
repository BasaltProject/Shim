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

#ifndef SHIM_CODEGEN_ASSEMBLER_ARM_BLOCK_H
#define SHIM_CODEGEN_ASSEMBLER_ARM_BLOCK_H

#include <shim/codegen/lir.h>
#include <shim/codegen/assembler.h>

namespace shim {
namespace codegen {
namespace arm {

class PoolEvent;

class MyBlock : public Assembler::Block {
 public:
  MyBlock(Context* context, unsigned offset);

  virtual unsigned resolve(unsigned start, Assembler::Block* next);

  Context* context;
  MyBlock* next;
  PoolOffset* poolOffsetHead;
  PoolOffset* poolOffsetTail;
  PoolOffset* lastPoolOffsetTail;
  PoolEvent* poolEventHead;
  PoolEvent* poolEventTail;
  unsigned lastEventOffset;
  unsigned offset;
  unsigned start;
  unsigned size;
};

}  // namespace arm
}  // namespace codegen
}  // namespace shim

#endif  // SHIM_CODEGEN_ASSEMBLER_ARM_BLOCK_H
