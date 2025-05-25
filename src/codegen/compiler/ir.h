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

#ifndef AVIAN_CODEGEN_COMPILER_IR_H
#define AVIAN_CODEGEN_COMPILER_IR_H

namespace avian {
namespace codegen {
namespace compiler {

class MultiRead;

class ForkElement {
 public:
  Value* value;
  MultiRead* read;
  bool local;
};

class ForkState : public Compiler::State {
 public:
  ForkState(Stack* stack,
            Local* locals,
            List<Value*>* saved,
            Event* predecessor,
            unsigned logicalIp)
      : stack(stack),
        locals(locals),
        saved(saved),
        predecessor(predecessor),
        logicalIp(logicalIp),
        readCount(0)
  {
  }

  Stack* stack;
  Local* locals;
  List<Value*>* saved;
  Event* predecessor;
  unsigned logicalIp;
  unsigned readCount;
  ForkElement elements[0];
};

class LogicalInstruction {
 public:
  LogicalInstruction(int index, Stack* stack, Local* locals);

  LogicalInstruction* next(Context* c);

  Event* firstEvent;
  Event* lastEvent;
  LogicalInstruction* immediatePredecessor;
  Stack* stack;
  Local* locals;
  Promise* machineOffset;
  int index;
};

class Block {
 public:
  Block(Event* head);

  Event* head;
  Block* nextBlock;
  LogicalInstruction* nextInstruction;
  Assembler::Block* assemblerBlock;
  unsigned start;
};

Block* block(Context* c, Event* head);

unsigned machineOffset(Context* c, int logicalIp);

}  // namespace compiler
}  // namespace codegen
}  // namespace avian

#endif  // AVIAN_CODEGEN_COMPILER_IR_H
