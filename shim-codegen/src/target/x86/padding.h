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

#ifndef SHIM_CODEGEN_ASSEMBLER_X86_PADDING_H
#define SHIM_CODEGEN_ASSEMBLER_X86_PADDING_H

namespace shim {
namespace codegen {
namespace x86 {

class Context;

class AlignmentPadding {
 public:
  AlignmentPadding(Context* c, unsigned instructionOffset, unsigned alignment);

  unsigned offset;
  unsigned instructionOffset;
  unsigned alignment;
  AlignmentPadding* next;
  int padding;
};

unsigned padding(AlignmentPadding* p,
                 unsigned start,
                 unsigned offset,
                 AlignmentPadding* limit);

}  // namespace x86
}  // namespace codegen
}  // namespace shim

#endif  // SHIM_CODEGEN_ASSEMBLER_X86_PADDING_H
