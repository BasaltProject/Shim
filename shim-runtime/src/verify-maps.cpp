/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

#include "verify-types.h"
#include "shim/constants.h"

namespace vm {
using namespace verification;
namespace {

void format(Thread* t, bool valid)
{
    if (!valid) throwNew(t, GcClassFormatError::Type, "Invalid StackMapTable format");
}

class Reader {
public:
    Reader(Thread* t, GcByteArray** data) : t(t), data(data), position(0) { }
    unsigned byte()
    {
        format(t, position < (*data)->length());
        return static_cast<uint8_t>((*data)->body()[position++]);
    }
    unsigned word() { unsigned high = byte(); return (high << 8) | byte(); }
    Thread* t;
    GcByteArray** data;
    unsigned position;
};

unsigned type(Thread* t, Reader& in, GcCode* code, GcByteArray* tags, bool constructor, const uint8_t* starts)
{
    unsigned tag = in.byte();
    switch (tag) {
    case 0: return kTop;
    case 1: return kInt;
    case 2: return kFloat;
    case 3: return kDouble;
    case 4: return kLong;
    case 5: return kNull;
    case 6:
        if (!constructor) throwNew(t, GcVerifyError::Type, "UninitializedThis outside constructor");
        return kUninitializedThis;
    case 7: {
        unsigned index = in.word();
        format(t, index && index <= tags->length() / 2 && tags->body()[index - 1] == CONSTANT_Class);
        return kReference;
    }
    case 8: {
        unsigned pc = in.word();
        if (pc >= code->length() || !starts[pc] || code->body()[pc] != 187) {
            throwNew(t, GcClassFormatError::Type, "Invalid uninitialized offset in StackMapTable");
        }
        return kAllocationBase + pc;
    }
    default: format(t, false); return 0;
    }
}

void push(Thread* t, Value* values, unsigned* count, unsigned limit, unsigned kind)
{
    unsigned width = isWideKind(kind) ? 2 : 1;
    format(t, *count <= limit && width <= limit - *count);
    values[(*count)++] = kind;
    if (width == 2) values[(*count)++] = kSecond;
}

void word(uint8_t* bytes, unsigned value) { bytes[0] = value >> 8; bytes[1] = value; }

} // namespace

GcArray* readStackMaps(Thread* t, GcCode* code, GcByteArray* tags, GcByteArray* raw,
                       GcByteArray* descriptor, bool instance, bool constructor, const uint8_t* starts)
{
    if (!raw || !raw->length()) return 0;
    PROTECT(t, code);
    PROTECT(t, tags);
    PROTECT(t, raw);
    PROTECT(t, descriptor);
    Reader in(t, &raw);
    unsigned entries = in.word();
    // OpenJDK treats zero entries as an absent map without reading the remaining payload.
    if (!entries) return 0;
    unsigned localLimit = code->maxLocals();
    unsigned stackLimit = code->maxStack();
    size_t size = (localLimit + stackLimit + 2) * sizeof(Value);
    Value* storage = static_cast<Value*>(t->m->heap->tryAllocate(size));
    if (!storage) throwNew(t, GcOutOfMemoryError::Type, "StackMapTable storage exhausted");
    THREAD_RESOURCE2(t, Value*, storage, size_t, size, t->m->heap->free(storage, size));
    Value* locals = storage;
    Value* stack = storage + localLimit + 1;
    unsigned localCount = 0;
    if (instance) push(t, locals, &localCount, localLimit, constructor ? kUninitializedThis : kReference);
    const int8_t* parameters = descriptor->body().begin();
    for (unsigned position = 1; parameters[position] != ')';) {
        push(t, locals, &localCount, localLimit, descriptorKind(parameters[position]));
        while (parameters[position] == '[') ++position;
        if (parameters[position] == 'L') while (parameters[position] != ';') ++position;
        ++position;
    }
    GcArray* maps = makeArray(t, entries);
    PROTECT(t, maps);
    int64_t previous = -1;
    for (unsigned index = 0; index < entries; ++index) {
        unsigned frameType = in.byte();
        unsigned delta;
        unsigned stackCount = 0;
        if (frameType <= 63) delta = frameType;
        else if (frameType <= 127) {
            delta = frameType - 64;
            push(t, stack, &stackCount, stackLimit, type(t, in, code, tags, constructor, starts));
        } else if (frameType == 247) {
            delta = in.word();
            push(t, stack, &stackCount, stackLimit, type(t, in, code, tags, constructor, starts));
        } else if (frameType >= 248 && frameType <= 250) {
            delta = in.word();
            for (unsigned i = frameType; i < 251; ++i) {
                format(t, localCount != 0);
                if (locals[--localCount] == kSecond) {
                    format(t, localCount != 0 && isWideKind(locals[localCount - 1]));
                    --localCount;
                }
            }
        } else if (frameType == 251) delta = in.word();
        else if (frameType >= 252 && frameType <= 254) {
            delta = in.word();
            for (unsigned i = 251; i < frameType; ++i) {
                push(t, locals, &localCount, localLimit, type(t, in, code, tags, constructor, starts));
            }
        } else if (frameType == 255) {
            delta = in.word();
            localCount = 0;
            unsigned count = in.word();
            for (unsigned i = 0; i < count; ++i) {
                push(t, locals, &localCount, localLimit, type(t, in, code, tags, constructor, starts));
            }
            count = in.word();
            for (unsigned i = 0; i < count; ++i) {
                push(t, stack, &stackCount, stackLimit, type(t, in, code, tags, constructor, starts));
            }
        } else { format(t, false); delta = 0; }
        int64_t pc = previous + delta + 1;
        if (pc >= code->length() || !starts[pc]) throwNew(t, GcVerifyError::Type, "Invalid StackMapTable offset");
        previous = pc;
        GcByteArray* frame = makeByteArray(t, 6 + 4 * (localCount + stackCount));
        uint8_t* bytes = reinterpret_cast<uint8_t*>(frame->body().begin());
        word(bytes, pc); word(bytes + 2, localCount); word(bytes + 4, stackCount);
        for (unsigned j = 0; j < localCount + stackCount; ++j) {
            unsigned value = j < localCount ? locals[j] : stack[j - localCount];
            word(bytes + 6 + 4 * j, value >> 16);
            word(bytes + 8 + 4 * j, value);
        }
        maps->setBodyElement(t, index, frame);
    }
    format(t, in.position == raw->length());
    return maps;
}

} // namespace vm
