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

#include "shim/machine.h"
#include "shim/heapwalk.h"

using namespace vm;

namespace {

namespace local {

enum { Root, Size, ClassName, Push, Pop };

void write1(FILE* out, uint8_t v)
{
  size_t n UNUSED = fwrite(&v, 1, 1, out);
}

void write4(FILE* out, uint32_t v)
{
  uint8_t b[] = {static_cast<uint8_t>(v >> 24),
                 static_cast<uint8_t>((v >> 16) & 0xFF),
                 static_cast<uint8_t>((v >> 8) & 0xFF),
                 static_cast<uint8_t>(v & 0xFF)};

  size_t n UNUSED = fwrite(b, 4, 1, out);
}

void writeString(FILE* out, int8_t* p, unsigned size)
{
  write4(out, size);
  size_t n UNUSED = fwrite(p, size, 1, out);
}

unsigned objectSize(Thread* t, object o)
{
  return extendedSize(t, o, baseSize(t, o, objectClass(t, o)));
}

}  // namespace local

}  // namespace

namespace vm {

void dumpHeap(Thread* t, FILE* out)
{
  class Visitor : public HeapVisitor {
   public:
    Visitor(Thread* t, FILE* out) : t(t), out(out), nextNumber(1)
    {
    }

    virtual void root()
    {
      write1(out, local::Root);
    }

    virtual unsigned visitNew(object p)
    {
      if (p) {
        unsigned number = nextNumber++;
        local::write4(out, number);

        local::write1(out, local::Size);
        local::write4(out, local::objectSize(t, p));

        if (objectClass(t, p) == type(t, GcClass::Type)) {
          GcByteArray* name = static_cast<GcClass*>(p)->name();
          if (name) {
            local::write1(out, local::ClassName);
            local::writeString(out, name->body().begin(), name->length() - 1);
          }
        }

        return number;
      } else {
        return 0;
      }
    }

    virtual void visitOld(object, unsigned number)
    {
      local::write4(out, number);
    }

    virtual void push(object, unsigned, unsigned)
    {
      local::write1(out, local::Push);
    }

    virtual void pop()
    {
      local::write1(out, local::Pop);
    }

    Thread* t;
    FILE* out;
    unsigned nextNumber;
  } visitor(t, out);

  HeapWalker* w = makeHeapWalker(t, &visitor);
  w->visitAllRoots();
  w->dispose();
}

}  // namespace vm

namespace vm {

void dumpHeapStats(Thread* t, FILE* out)
{
  enum Category { Application, Metadata, Statics, Bytecode, ClassPool, CategoryCount };
  static const char* const categoryNames[]
      = {"application objects", "class metadata (classes, methods, fields, tables)", "static fields",
         "bytecode and code attributes", "constant pools"};

  struct Entry {
    GcClass* type;
    unsigned count;
    uint64_t bytes;
    unsigned category;
  };

  class Visitor : public HeapVisitor {
   public:
    Visitor(Thread* t) : t(t), categories(0), categoryCapacity(0), nextNumber(1), stack(0), stackCount(0),
                         stackCapacity(0), entries(0), entryCount(0), entryCapacity(0)
    {
      memset(totals, 0, sizeof(totals));
      memset(counts, 0, sizeof(counts));
    }

    virtual void root()
    {
    }

    unsigned classify(object p, unsigned parentCategory, object parent)
    {
      GcClass* c = objectClass(t, p);
      if (c == type(t, GcClass::Type) or c == type(t, GcMethod::Type) or c == type(t, GcField::Type)
          or c == type(t, GcAddendum::Type) or c == type(t, GcClassAddendum::Type)
          or c == type(t, GcMethodAddendum::Type) or c == type(t, GcFieldAddendum::Type)
          or c == type(t, GcClassRuntimeData::Type) or c == type(t, GcMethodRuntimeData::Type)
          or c == type(t, GcExceptionHandlerTable::Type) or c == type(t, GcLineNumberTable::Type)
          or c == type(t, GcCallNode::Type) or c == type(t, GcTreeNode::Type) or c == type(t, GcReference::Type)
          or c == type(t, GcPointer::Type) or c == type(t, GcRegion::Type) or c == type(t, GcInvocation::Type)) {
        return Metadata;
      } else if (c == type(t, GcCode::Type)) {
        return Bytecode;
      } else if (c == type(t, GcSingleton::Type)) {
        return parent and objectClass(t, parent) == type(t, GcClass::Type) ? Statics : ClassPool;
      } else if (c == type(t, GcArray::Type) or c == type(t, GcByteArray::Type) or c == type(t, GcIntArray::Type)
                 or c == type(t, GcCharArray::Type) or c == type(t, GcLongArray::Type)
                 or c == type(t, GcShortArray::Type) or c == type(t, GcWordArray::Type)
                 or c == type(t, GcBooleanArray::Type) or c == type(t, GcFloatArray::Type)
                 or c == type(t, GcDoubleArray::Type)) {
        return parent ? parentCategory : Application;
      } else {
        return Application;
      }
    }

    virtual unsigned visitNew(object p)
    {
      if (p == 0) {
        return 0;
      }
      unsigned number = nextNumber++;
      object parent = stackCount ? stack[stackCount - 1].parent : 0;
      unsigned parentCategory = stackCount ? stack[stackCount - 1].category : Application;
      unsigned category = classify(p, parentCategory, parent);
      if (number >= categoryCapacity) {
        unsigned n = categoryCapacity ? categoryCapacity * 2 : 1 << 20;
        uint8_t* a = static_cast<uint8_t*>(malloc(n));
        if (categories) {
          memcpy(a, categories, categoryCapacity);
          free(categories);
        }
        categories = a;
        categoryCapacity = n;
      }
      categories[number] = category;
      unsigned size = local::objectSize(t, p) * BytesPerWord;
      totals[category] += size;
      counts[category] += 1;
      record(objectClass(t, p), size, category);
      return number;
    }

    virtual void visitOld(object, unsigned)
    {
    }

    virtual void push(object parent, unsigned parentNumber, unsigned)
    {
      if (stackCount == stackCapacity) {
        stackCapacity = stackCapacity ? stackCapacity * 2 : 1024;
        Frame* a = static_cast<Frame*>(malloc(stackCapacity * sizeof(Frame)));
        if (stack) {
          memcpy(a, stack, stackCount * sizeof(Frame));
          free(stack);
        }
        stack = a;
      }
      stack[stackCount].parent = parent;
      stack[stackCount].category = parentNumber < categoryCapacity ? categories[parentNumber] : Application;
      ++stackCount;
    }

    virtual void pop()
    {
      if (stackCount) {
        --stackCount;
      }
    }

    void record(GcClass* type, unsigned size, unsigned category)
    {
      for (unsigned i = 0; i < entryCount; ++i) {
        if (entries[i].type == type and entries[i].category == category) {
          entries[i].count += 1;
          entries[i].bytes += size;
          return;
        }
      }
      if (entryCount == entryCapacity) {
        entryCapacity = entryCapacity ? entryCapacity * 2 : 256;
        Entry* a = static_cast<Entry*>(malloc(entryCapacity * sizeof(Entry)));
        if (entries) {
          memcpy(a, entries, entryCount * sizeof(Entry));
          free(entries);
        }
        entries = a;
      }
      entries[entryCount].type = type;
      entries[entryCount].count = 1;
      entries[entryCount].bytes = size;
      entries[entryCount].category = category;
      ++entryCount;
    }

    struct Frame {
      object parent;
      unsigned category;
    };

    Thread* t;
    uint8_t* categories;
    unsigned categoryCapacity;
    unsigned nextNumber;
    Frame* stack;
    unsigned stackCount;
    unsigned stackCapacity;
    Entry* entries;
    unsigned entryCount;
    unsigned entryCapacity;
    uint64_t totals[CategoryCount];
    uint64_t counts[CategoryCount];
  } visitor(t);

  HeapWalker* w = makeHeapWalker(t, &visitor);
  w->visitAllRoots();
  w->dispose();

  uint64_t total = 0;
  for (unsigned i = 0; i < CategoryCount; ++i) {
    total += visitor.totals[i];
  }
  fprintf(out, "heap stats: %u objects, %llu KB reachable\n", visitor.nextNumber - 1,
          static_cast<unsigned long long>(total / 1024));
  for (unsigned i = 0; i < CategoryCount; ++i) {
    fprintf(out, "  %-52s %9llu KB %5.1f%% %9llu objects\n", categoryNames[i],
            static_cast<unsigned long long>(visitor.totals[i] / 1024),
            total ? 100.0 * visitor.totals[i] / total : 0.0, static_cast<unsigned long long>(visitor.counts[i]));
  }
  for (unsigned k = 0; k < 40; ++k) {
    unsigned best = visitor.entryCount;
    uint64_t bestBytes = 0;
    for (unsigned i = 0; i < visitor.entryCount; ++i) {
      if (visitor.entries[i].bytes > bestBytes) {
        bestBytes = visitor.entries[i].bytes;
        best = i;
      }
    }
    if (best == visitor.entryCount) {
      break;
    }
    Entry& e = visitor.entries[best];
    const char* name = e.type->name() ? reinterpret_cast<const char*>(e.type->name()->body().begin()) : "?";
    fprintf(out, "  %9llu KB %9u x  [%s] %s\n", static_cast<unsigned long long>(e.bytes / 1024), e.count,
            categoryNames[e.category], name);
    e.bytes = 0;
  }
}

}  // namespace vm

