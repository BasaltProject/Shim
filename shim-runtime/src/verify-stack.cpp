/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

#include "shim/machine.h"
#include "shim/constants.h"
#include "verify-types.h"

namespace vm {
using namespace verification;
namespace {

void requireType(Thread* t, bool valid)
{
    if (!valid) throwNew(t, GcVerifyError::Type, "Incompatible operand stack type");
}

unsigned read16(const uint8_t* bytes) { return (unsigned(bytes[0]) << 8) | bytes[1]; }

int32_t read32(const uint8_t* bytes)
{
    return static_cast<int32_t>((uint32_t(bytes[0]) << 24) | (uint32_t(bytes[1]) << 16)
        | (uint32_t(bytes[2]) << 8) | bytes[3]);
}

class Stack {
public:
    Stack(Thread* t, Value* values, unsigned depth, unsigned capacity)
        : t(t), values(values), depth(depth), capacity(capacity) { }

    void push(unsigned kind)
    {
        requireType(t, depth + (isWideKind(kind) ? 2 : 1) <= capacity);
        values[depth++] = kind;
        if (isWideKind(kind)) values[depth++] = kSecond;
    }

    unsigned take()
    {
        requireType(t, depth != 0 && values[depth - 1] != kTop);
        unsigned kind = values[--depth];
        if (kind == kSecond) {
            requireType(t, depth != 0 && isWideKind(values[depth - 1]));
            kind = values[--depth];
        } else {
            requireType(t, !isWideKind(kind));
        }
        return kind;
    }

    unsigned one()
    {
        unsigned kind = take();
        requireType(t, !isWideKind(kind));
        return kind;
    }

    unsigned pop(unsigned kind)
    {
        unsigned actual = take();
        requireType(t, assignable(actual, kind));
        return actual;
    }

    unsigned popMovable()
    {
        unsigned actual = take();
        requireType(t, isMovableReference(actual));
        return actual;
    }

    void replace(unsigned old, unsigned replacement)
    {
        for (unsigned i = 0; i < depth; ++i) if (values[i] == old) values[i] = replacement;
    }

    Thread* t;
    Value* values;
    unsigned depth;
    unsigned capacity;
};

class Locals {
public:
    Locals(Thread* t, Value* values, const uint16_t* mapping, unsigned limit, unsigned count)
        : t(t), values(values), mapping(mapping), limit(limit), count(count) { }

    unsigned load(unsigned index, unsigned kind)
    {
        unsigned actual = value(index);
        requireType(t, kind == kReference ? isMovableReference(actual) : assignable(actual, kind));
        if (isWideKind(kind)) requireType(t, value(index + 1) == kSecond);
        return actual;
    }

    void store(unsigned index, unsigned kind)
    {
        requireType(t, index < limit && mapping[index] != 0xffff);
        clear(index);
        if (isWideKind(kind)) clear(index + 1);
        assign(index, kind);
        if (isWideKind(kind)) assign(index + 1, kSecond);
    }

    void replace(unsigned old, unsigned replacement)
    {
        for (unsigned i = 0; i < count; ++i) if (values[i] == old) values[i] = replacement;
    }

private:
    unsigned value(unsigned index)
    {
        return index < limit && mapping[index] != 0xffff ? values[mapping[index]] : kTop;
    }

    void assign(unsigned index, unsigned kind)
    {
        if (index < limit && mapping[index] != 0xffff) values[mapping[index]] = kind;
    }

    void clear(unsigned index)
    {
        if (index && isWideKind(value(index - 1))) assign(index - 1, kTop);
        if (isWideKind(value(index))) assign(index + 1, kTop);
        assign(index, kTop);
    }

    Thread* t;
    Value* values;
    const uint16_t* mapping;
    unsigned limit;
    unsigned count;
};

void shuffle(Stack& stack, unsigned opcode)
{
    unsigned a = stack.take();
    if (opcode == 87) { requireType(stack.t, !isWideKind(a)); return; }
    if (opcode == 88) { if (!isWideKind(a)) stack.one(); return; }
    if (opcode == 89) {
        requireType(stack.t, !isWideKind(a));
        stack.push(a); stack.push(a);
    } else if (opcode == 90 || opcode == 91 || opcode == 95) {
        requireType(stack.t, !isWideKind(a));
        unsigned b = stack.take();
        if (opcode == 91 && isWideKind(b)) {
            stack.push(a); stack.push(b); stack.push(a);
        } else {
            requireType(stack.t, !isWideKind(b));
            if (opcode == 91) {
                unsigned c = stack.one();
                stack.push(a); stack.push(c); stack.push(b); stack.push(a);
            } else {
                stack.push(a); stack.push(b);
                if (opcode == 90) stack.push(a);
            }
        }
    } else if (opcode == 92) {
        if (isWideKind(a)) { stack.push(a); stack.push(a); }
        else {
            unsigned b = stack.one();
            stack.push(b); stack.push(a); stack.push(b); stack.push(a);
        }
    } else {
        unsigned b = isWideKind(a) ? 0 : stack.one();
        unsigned c = stack.take();
        unsigned d = 0;
        if (opcode == 93) requireType(stack.t, !isWideKind(c));
        else if (!isWideKind(c)) d = stack.one();
        if (b) stack.push(b);
        stack.push(a);
        if (d) stack.push(d);
        stack.push(c);
        if (b) stack.push(b);
        stack.push(a);
    }
}

unsigned invoke(Stack& stack, GcByteArray* descriptor, bool receiver, bool initialize = false)
{
    Value arguments[255];
    unsigned count = 0;
    unsigned position = 1;
    const int8_t* bytes = descriptor->body().begin();
    while (bytes[position] != ')') {
        arguments[count++] = descriptorValue(bytes + position);
        while (bytes[position] == '[') ++position;
        if (bytes[position] == 'L') while (bytes[position] != ';') ++position;
        ++position;
    }
    while (count) stack.pop(arguments[--count]);
    unsigned target = 0;
    if (receiver) {
        if (initialize) { target = stack.take(); requireType(stack.t, isUninitialized(target)); }
        else stack.pop(kReference);
    }
    unsigned result = descriptorValue(bytes + position + 1);
    if (result) stack.push(result);
    return target;
}

bool sameName(GcByteArray* first, GcByteArray* second)
{
    return first && second && first->length() == second->length()
        && memcmp(first->body().begin(), second->body().begin(), first->length()) == 0;
}

bool isConstructor(GcReference* reference)
{
    return vm::strcmp(reference->name()->body().begin(), reinterpret_cast<const int8_t*>("<init>")) == 0;
}

bool declaresField(Thread* t, GcClass* owner, GcReference* reference)
{
    if (!sameName(owner->name(), reference->class_())) return false;
    GcArray* fields = cast<GcArray>(t, owner->fieldTable());
    for (unsigned i = 0; fields && i < fields->length(); ++i) {
        GcField* field = cast<GcField>(t, fields->body()[i]);
        if (sameName(field->name(), reference->name()) && sameName(field->spec(), reference->spec())) return true;
    }
    return false;
}

void requireArray(Thread* t, unsigned array, unsigned element)
{
    if (array == kNull) return;
    requireType(t, isArrayKind(array));
    if (element == 4) requireType(t, arrayRank(array) > 1 || arrayElement(array) == eObject);
    else {
        static const uint8_t elements[] = {eInt, eLong, eFloat, eDouble, eObject, eByte, eChar, eShort};
        requireType(t, arrayRank(array) == 1 && (arrayElement(array) == elements[element]
            || (element == 5 && arrayElement(array) == eBoolean)));
    }
}

void transfer(Thread* t, GcCode* code, GcByteArray* tags, unsigned pc, Stack& stack, Locals& locals,
              GcClass* owner, bool& uninitialized, unsigned returnType)
{
    const uint8_t* bytes = code->body().begin();
    unsigned opcode = bytes[pc];
    if (opcode == 196) opcode = bytes[pc + 1];
    static const uint8_t kinds[] = {kInt, kLong, kFloat, kDouble, kReference};
    static const uint8_t arrayKinds[] = {kInt, kLong, kFloat, kDouble, kReference, kInt, kInt, kInt};
    if (opcode == 1) stack.push(kNull);
    else if ((opcode >= 2 && opcode <= 8) || opcode == 16 || opcode == 17) stack.push(kInt);
    else if (opcode == 9 || opcode == 10) stack.push(kLong);
    else if (opcode >= 11 && opcode <= 13) stack.push(kFloat);
    else if (opcode == 14 || opcode == 15) stack.push(kDouble);
    else if (opcode >= 18 && opcode <= 20) {
        unsigned index = (opcode == 18 ? bytes[pc + 1] : read16(bytes + pc + 1)) - 1;
        unsigned tag = tags->body()[index];
        unsigned kind = kReference;
        if (tag == CONSTANT_Integer) kind = kInt;
        else if (tag == CONSTANT_Float) kind = kFloat;
        else if (tag == CONSTANT_Long) kind = kLong;
        else if (tag == CONSTANT_Double) kind = kDouble;
        else if (tag == CONSTANT_Dynamic) {
            GcMethod* method = cast<GcDynamicConstant>(t, singletonObject(t, code->pool(), index))->template_();
            kind = descriptorValue(method->spec()->body().begin() + 2);
        }
        stack.push(kind);
    } else if (opcode >= 21 && opcode <= 45) {
        unsigned index = opcode <= 25 ? (bytes[pc] == 196 ? read16(bytes + pc + 2) : bytes[pc + 1])
            : (opcode - 26) % 4;
        unsigned kind = kinds[opcode <= 25 ? opcode - 21 : (opcode - 26) / 4];
        stack.push(locals.load(index, kind));
    }
    else if (opcode >= 46 && opcode <= 53) {
        stack.pop(kInt);
        unsigned array = stack.pop(kReference);
        unsigned element = opcode - 46;
        requireArray(t, array, element);
        stack.push(element == 4 ? (array == kNull ? kNull : componentValue(array)) : arrayKinds[element]);
    } else if (opcode >= 54 && opcode <= 78) {
        unsigned index = opcode <= 58 ? (bytes[pc] == 196 ? read16(bytes + pc + 2) : bytes[pc + 1])
            : (opcode - 59) % 4;
        unsigned kind = kinds[opcode <= 58 ? opcode - 54 : (opcode - 59) / 4];
        locals.store(index, kind == kReference ? stack.popMovable() : stack.pop(kind));
    }
    else if (opcode >= 79 && opcode <= 86) {
        unsigned element = opcode - 79;
        stack.pop(arrayKinds[element]); stack.pop(kInt);
        unsigned array = stack.pop(kReference);
        requireArray(t, array, element);
    } else if (opcode >= 87 && opcode <= 95) shuffle(stack, opcode);
    else if (opcode >= 96 && opcode <= 119) {
        unsigned kind = kinds[(opcode - 96) % 4];
        stack.pop(kind);
        if (opcode <= 115) stack.pop(kind);
        stack.push(kind);
    } else if (opcode >= 120 && opcode <= 131) {
        unsigned kind = opcode % 2 ? kLong : kInt;
        stack.pop(opcode <= 125 ? kInt : kind); stack.pop(kind); stack.push(kind);
    } else if (opcode == 132) {
        locals.load(bytes[pc] == 196 ? read16(bytes + pc + 2) : bytes[pc + 1], kInt);
    } else if (opcode >= 133 && opcode <= 147) {
        static const uint8_t from[] = {kInt,kInt,kInt,kLong,kLong,kLong,kFloat,kFloat,kFloat,
            kDouble,kDouble,kDouble,kInt,kInt,kInt};
        static const uint8_t to[] = {kLong,kFloat,kDouble,kInt,kFloat,kDouble,kInt,kLong,kDouble,
            kInt,kLong,kFloat,kInt,kInt,kInt};
        stack.pop(from[opcode - 133]); stack.push(to[opcode - 133]);
    } else if (opcode >= 148 && opcode <= 152) {
        unsigned kind = opcode == 148 ? kLong : (opcode <= 150 ? kFloat : kDouble);
        stack.pop(kind); stack.pop(kind); stack.push(kInt);
    } else if ((opcode >= 153 && opcode <= 158) || opcode == 170 || opcode == 171) stack.pop(kInt);
    else if (opcode >= 159 && opcode <= 166) {
        unsigned kind = opcode <= 164 ? kInt : kReference;
        if (kind == kReference) { stack.popMovable(); stack.popMovable(); }
        else { stack.pop(kind); stack.pop(kind); }
    } else if (opcode == 177) requireType(t, !uninitialized);
    else if (opcode >= 172 && opcode <= 176) stack.pop(opcode == 176 ? returnType : kinds[opcode - 172]);
    else if (opcode >= 178 && opcode <= 185) {
        GcReference* reference = cast<GcReference>(t, singletonObject(t, code->pool(), read16(bytes + pc + 1) - 1));
        if (opcode <= 181) {
            unsigned kind = descriptorValue(reference->spec()->body().begin());
            if (opcode == 179 || opcode == 181) stack.pop(kind);
            if (opcode == 180) stack.pop(kReference);
            else if (opcode == 181) {
                unsigned target = stack.take();
                requireType(t, isReferenceKind(target)
                    || (target == kUninitializedThis && declaresField(t, owner, reference)));
            }
            if (opcode == 178 || opcode == 180) stack.push(kind);
        } else {
            bool initialize = opcode == 183 && isConstructor(reference);
            unsigned target = invoke(stack, reference->spec(), opcode != 184, initialize);
            if (initialize) {
                if (target == kUninitializedThis) {
                    requireType(t, sameName(reference->class_(), owner->name())
                        || (owner->super() && sameName(reference->class_(), owner->super()->name())));
                    uninitialized = false;
                } else {
                    unsigned address = target - kAllocationBase;
                    GcReference* allocated = cast<GcReference>(t, singletonObject(t, code->pool(), read16(bytes + address + 1) - 1));
                    requireType(t, sameName(reference->class_(), allocated->name()));
                }
                stack.replace(target, kReference);
                locals.replace(target, kReference);
            }
        }
    } else if (opcode == 186) {
        GcInvocation* site = cast<GcInvocation>(t, singletonObject(t, code->pool(), read16(bytes + pc + 1) - 1));
        invoke(stack, site->template_()->spec(), false);
    } else if (opcode == 187) {
        // Match HotSpot's allocation-site identity, including asserted dead frames.
        stack.push(kAllocationBase + pc);
    }
    else if (opcode == 188) {
        static const uint8_t elements[] = {eBoolean, eChar, eFloat, eDouble, eByte, eShort, eInt, eLong};
        stack.pop(kInt); stack.push(arrayKind(1, elements[bytes[pc + 1] - 4]));
    } else if (opcode == 189) {
        GcReference* reference = cast<GcReference>(t, singletonObject(t, code->pool(), read16(bytes + pc + 1) - 1));
        unsigned component = classValue(reference->name());
        stack.pop(kInt);
        stack.push(isArrayKind(component) ? component + 16 : arrayKind(1, eObject));
    } else if (opcode == 190) {
        unsigned array = stack.pop(kReference);
        requireType(t, array == kNull || isArrayKind(array));
        stack.push(kInt);
    } else if (opcode == 193) { stack.pop(kReference); stack.push(kInt); }
    else if (opcode == 191) stack.pop(kReference);
    else if (opcode == 194 || opcode == 195 || opcode == 198 || opcode == 199) stack.popMovable();
    else if (opcode == 192) {
        GcReference* reference = cast<GcReference>(t, singletonObject(t, code->pool(), read16(bytes + pc + 1) - 1));
        stack.pop(kReference); stack.push(classValue(reference->name()));
    }
    else if (opcode == 197) {
        for (unsigned i = 0; i < bytes[pc + 3]; ++i) stack.pop(kInt);
        GcReference* reference = cast<GcReference>(t, singletonObject(t, code->pool(), read16(bytes + pc + 1) - 1));
        stack.push(classValue(reference->name()));
    }
}

} // namespace

void verifyStackTypes(Thread* t, GcCode* code, GcByteArray* tags, const int32_t* depths, const unsigned* nexts,
                      GcByteArray* descriptor, bool instance, GcArray* maps, bool constructor, GcClass* owner)
{
    PROTECT(t, code);
    PROTECT(t, tags);
    PROTECT(t, descriptor);
    PROTECT(t, maps);
    PROTECT(t, owner);
    unsigned returnType = descriptorValue(reinterpret_cast<const int8_t*>(strchr(reinterpret_cast<const char*>(descriptor->body().begin()), ')') + 1));
    unsigned length = code->length();
    unsigned localLimit = code->maxLocals();
    size_t mappingSize = (localLimit + 1) * 2 * sizeof(uint16_t);
    uint16_t* mapping = static_cast<uint16_t*>(t->m->heap->tryAllocate(mappingSize));
    if (!mapping) throwNew(t, GcOutOfMemoryError::Type, "Bytecode verification storage exhausted");
    THREAD_RESOURCE2(t, uint16_t*, mapping, size_t, mappingSize, t->m->heap->free(mapping, mappingSize));
    memset(mapping, 0xff, mappingSize);
    uint16_t* physical = mapping + localLimit + 1;
    unsigned arguments = validateMethodDescriptor(t, descriptor, instance);
    for (unsigned i = 0; i < arguments; ++i) mapping[i] = 0;
    for (unsigned pc = 0; pc < length; pc = nexts[pc]) {
        if (depths[pc] < 0) continue;
        const uint8_t* bytes = code->body().begin();
        unsigned opcode = bytes[pc];
        bool extended = opcode == 196;
        if (extended) opcode = bytes[pc + 1];
        unsigned index, kind;
        if ((opcode >= 21 && opcode <= 25) || (opcode >= 54 && opcode <= 58) || opcode == 132) {
            index = extended ? read16(bytes + pc + 2) : bytes[pc + 1];
            kind = opcode == 22 || opcode == 24 || opcode == 55 || opcode == 57 ? 2 : 1;
        } else if ((opcode >= 26 && opcode <= 45) || (opcode >= 59 && opcode <= 78)) {
            unsigned group = opcode <= 45 ? opcode - 26 : opcode - 59;
            index = group % 4;
            kind = group / 4 == 1 || group / 4 == 3 ? 2 : 1;
        } else continue;
        mapping[index] = 0;
        if (kind == 2) mapping[index + 1] = 0;
    }
    for (unsigned i = 0; maps && i < maps->length(); ++i) {
        GcByteArray* frame = cast<GcByteArray>(t, maps->body()[i]);
        for (unsigned j = 0; j < mapLocalCount(frame); ++j) {
            if (mapLocal(frame, j) != kTop) mapping[j] = 0;
        }
    }
    unsigned localCount = 0;
    for (unsigned i = 0; i < localLimit; ++i) {
        if (mapping[i] == 0) { physical[localCount] = i; mapping[i] = localCount++; }
    }
    uint64_t count = 0;
    for (unsigned i = 0; i < length; ++i) if (depths[i] >= 0) count += localCount + depths[i];
    uint64_t allocation = (count + code->maxStack() + 1 + localCount) * sizeof(Value) + uint64_t(length + 1) * sizeof(uint64_t)
        + uint64_t(length) * (sizeof(uint32_t) + 2);
    if (allocation > SIZE_MAX) throwNew(t, GcOutOfMemoryError::Type, "Bytecode verification storage exhausted");
    size_t size = static_cast<size_t>(allocation);
    uint8_t* storage = static_cast<uint8_t*>(t->m->heap->tryAllocate(size));
    if (!storage) throwNew(t, GcOutOfMemoryError::Type, "Bytecode verification storage exhausted");
    THREAD_RESOURCE2(t, uint8_t*, storage, size_t, size, t->m->heap->free(storage, size));
    uint64_t* offsets = reinterpret_cast<uint64_t*>(storage);
    uint32_t* pending = reinterpret_cast<uint32_t*>(offsets + length + 1);
    Value* work = reinterpret_cast<Value*>(pending + length);
    Value* workLocals = work + code->maxStack() + 1;
    Value* states = workLocals + localCount;
    uint8_t* seen = reinterpret_cast<uint8_t*>(states + count);
    uint8_t* flags = seen + length;
    memset(flags, 0, length);
    memset(seen, 0, length);
    memset(workLocals, 0, localCount * sizeof(Value));
    Locals initial(t, workLocals, mapping, localLimit, localCount);
    unsigned slot = 0;
    if (instance) initial.store(slot++, constructor ? kUninitializedThis : kReference);
    const int8_t* parameters = descriptor->body().begin();
    for (unsigned position = 1; parameters[position] != ')';) {
        unsigned kind = descriptorValue(parameters + position);
        initial.store(slot, kind);
        slot += isWideKind(kind) ? 2 : 1;
        while (parameters[position] == '[') ++position;
        if (parameters[position] == 'L') while (parameters[position] != ';') ++position;
        ++position;
    }
    offsets[0] = 0;
    for (unsigned i = 0; i < length; ++i) {
        offsets[i + 1] = offsets[i] + (depths[i] >= 0 ? localCount + depths[i] : 0);
    }
    unsigned head = 0, tail = 0, queued = 0;
    auto enqueue = [&](unsigned pc, const Value* values, unsigned depth, const Value* localValues, bool uninitialized) {
        requireType(t, pc < length && depths[pc] == static_cast<int32_t>(depth));
        GcByteArray* frame = mapAt(t, maps, pc);
        if (frame) requireType(t, mapStackCount(frame) == depth);
        Value* saved = states + offsets[pc];
        bool changed = !seen[pc];
        if (frame) {
            bool declared = mapUninitialized(frame);
            requireType(t, !uninitialized || declared);
            uninitialized = declared;
        }
        bool mergedFlag = uninitialized || (seen[pc] && flags[pc]);
        if (mergedFlag != bool(flags[pc])) changed = true;
        flags[pc] = mergedFlag;
        for (unsigned position = 0; position < localCount; ++position) {
            unsigned actual = localValues[position];
            unsigned kind = frame ? mapLocal(frame, physical[position]) : actual;
            requireType(t, assignable(actual, kind));
            if (seen[pc]) kind = mergeKind(saved[position], kind);
            if (!seen[pc] || saved[position] != kind) { saved[position] = kind; changed = true; }
        }
        for (unsigned i = 0; i < depth; ++i) {
            unsigned kind = frame ? mapStackValue(frame, i) : values[i];
            requireType(t, assignable(values[i], kind));
            unsigned position = localCount + i;
            if (seen[pc]) kind = mergeKind(saved[position], kind);
            if (!seen[pc] || saved[position] != kind) { saved[position] = kind; changed = true; }
        }
        seen[pc] |= 1;
        if (changed && !(seen[pc] & 2)) {
            pending[tail] = pc;
            if (++tail == length) tail = 0;
            ++queued;
            seen[pc] |= 2;
        }
    };
    enqueue(0, work, 0, workLocals, constructor);
    for (unsigned i = 0; maps && i < maps->length(); ++i) {
        GcByteArray* frame = cast<GcByteArray>(t, maps->body()[i]);
        for (unsigned j = 0; j < localCount; ++j) workLocals[j] = mapLocal(frame, physical[j]);
        unsigned depth = mapStackCount(frame);
        for (unsigned j = 0; j < depth; ++j) work[j] = mapStackValue(frame, j);
        enqueue(mapPc(frame), work, depth, workLocals, mapUninitialized(frame));
    }
    while (queued) {
        unsigned pc = pending[head];
        if (++head == length) head = 0;
        --queued;
        seen[pc] &= ~2;
        unsigned depth = depths[pc];
        memcpy(workLocals, states + offsets[pc], localCount * sizeof(Value));
        memcpy(work, states + offsets[pc] + localCount, depth * sizeof(Value));
        bool uninitialized = flags[pc];
        unsigned initializing = 0;
        if (code->body()[pc] == 183) {
            GcReference* reference = cast<GcReference>(t, singletonObject(t, code->pool(), read16(code->body().begin() + pc + 1) - 1));
            if (isConstructor(reference)) {
                unsigned arguments = validateMethodDescriptor(t, reference->spec(), false);
                requireType(t, depth > arguments);
                initializing = work[depth - arguments - 1];
            }
        }
        GcExceptionHandlerTable* handlers = cast<GcExceptionHandlerTable>(t, code->exceptionHandlerTable());
        if (handlers) {
            Value exception = kReference;
            for (unsigned i = 0; i < handlers->length(); ++i) {
                uint64_t entry = handlers->body()[i];
                if (pc >= exceptionHandlerStart(entry) && pc < exceptionHandlerEnd(entry)) {
                    // A failed constructor must never make its receiver usable again.
                    if (initializing) {
                        for (unsigned j = 0; j < localCount; ++j) {
                            if (workLocals[j] == initializing) workLocals[j] = kTop;
                        }
                    }
                    enqueue(exceptionHandlerIp(entry), &exception, 1, workLocals, uninitialized);
                }
            }
        }
        if (initializing) memcpy(workLocals, states + offsets[pc], localCount * sizeof(Value));
        Stack stack(t, work, depth, code->maxStack());
        Locals locals(t, workLocals, mapping, localLimit, localCount);
        transfer(t, code, tags, pc, stack, locals, owner, uninitialized, returnType);
        const uint8_t* bytes = code->body().begin();
        unsigned opcode = bytes[pc];
        if ((opcode >= 153 && opcode <= 166) || opcode == 167 || opcode == 198 || opcode == 199) {
            enqueue(pc + static_cast<int16_t>(read16(bytes + pc + 1)), work, stack.depth, workLocals, uninitialized);
        } else if (opcode == 200) enqueue(pc + read32(bytes + pc + 1), work, stack.depth, workLocals, uninitialized);
        else if (opcode == 170 || opcode == 171) {
            unsigned position = (pc + 4) & ~3u;
            enqueue(pc + read32(bytes + position), work, stack.depth, workLocals, uninitialized);
            unsigned entries = opcode == 170 ? read32(bytes + position + 8) - read32(bytes + position + 4) + 1
                : read32(bytes + position + 4);
            unsigned stride = opcode == 170 ? 4 : 8;
            position += opcode == 170 ? 12 : 8;
            for (unsigned i = 0; i < entries; ++i) {
                enqueue(pc + read32(bytes + position + i * stride + (opcode == 171 ? 4 : 0)), work, stack.depth, workLocals, uninitialized);
            }
        }
        bool terminal = opcode == 167 || opcode == 200 || opcode == 170 || opcode == 171
            || (opcode >= 172 && opcode <= 177) || opcode == 191;
        if (!terminal) enqueue(nexts[pc], work, stack.depth, workLocals, uninitialized);
    }
}

} // namespace vm
