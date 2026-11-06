/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

#ifndef SHIM_NPE_MESSAGE_H
#define SHIM_NPE_MESSAGE_H

#include "shim/machine.h"

namespace vm {

unsigned npeInstructionLength(const uint8_t* code, unsigned length, unsigned pc);

bool npeCapableOpcode(unsigned opcode);

GcString* extendedNullPointerMessage(Thread* t, GcThrowable* npe);

}  // namespace vm

#endif
