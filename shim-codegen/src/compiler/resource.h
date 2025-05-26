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

#ifndef SHIM_CODEGEN_COMPILER_RESOURCE_H
#define SHIM_CODEGEN_COMPILER_RESOURCE_H

namespace shim {
namespace codegen {
namespace compiler {

class Context;
class Value;
class Site;

class Resource {
 public:
  Resource(bool reserved = false);

  virtual void freeze(Context*, Value*) = 0;

  virtual void thaw(Context*, Value*) = 0;

  virtual unsigned toString(Context*, char*, unsigned) = 0;

  Value* value;
  Site* site;
  Resource* previousAcquired;
  Resource* nextAcquired;
  uint8_t freezeCount;
  uint8_t referenceCount;
  bool reserved;
};

class RegisterResource : public Resource {
 public:
  RegisterResource(bool reserved);

  virtual void freeze(Context*, Value*);

  virtual void thaw(Context*, Value*);

  virtual unsigned toString(Context* c, char* buffer, unsigned bufferSize);

  virtual Register index(Context*);

  void increment(Context*);

  void decrement(Context*);
};

class FrameResource : public Resource {
 public:
  virtual void freeze(Context*, Value*);

  virtual void thaw(Context*, Value*);

  virtual unsigned toString(Context* c, char* buffer, unsigned bufferSize);

  virtual unsigned index(Context*);
};

void acquire(Context* c, Resource* resource, Value* value, Site* site);

void release(Context* c, Resource* resource, Value* value, Site* site);

}  // namespace compiler
}  // namespace codegen
}  // namespace shim

#endif  // SHIM_CODEGEN_COMPILER_RESOURCE_H
