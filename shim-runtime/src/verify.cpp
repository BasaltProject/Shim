/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

#include "shim/machine.h"
#include "shim/constants.h"
#include "verify-types.h"
#include "shim/util/runtime-array.h"

namespace vm {
using namespace verification;
namespace {

void require(Thread* t, bool valid, const char* message)
{
    if (!valid) throwNew(t, GcVerifyError::Type, "%s", message);
}

unsigned word16(const uint8_t* bytes)
{
    return (unsigned(bytes[0]) << 8) | bytes[1];
}

int32_t word32(const uint8_t* bytes)
{
    return static_cast<int32_t>((uint32_t(bytes[0]) << 24) | (uint32_t(bytes[1]) << 16)
        | (uint32_t(bytes[2]) << 8) | bytes[3]);
}

unsigned constantTag(Thread* t, GcByteArray* tags, unsigned index)
{
    require(t, index != 0 && index <= tags->length() / 2, "Invalid bytecode constant pool index");
    return tags->body()[index - 1];
}

void loadConstant(Thread* t, GcCode* code, GcByteArray* tags, unsigned index, bool wide)
{
    unsigned tag = constantTag(t, tags, index);
    bool valid;
    if (tag == CONSTANT_Dynamic) {
        GcDynamicConstant* constant = cast<GcDynamicConstant>(t, singletonObject(t, code->pool(), index - 1));
        unsigned result = constant->template_()->returnCode();
        valid = wide == (result == LongField || result == DoubleField);
    } else if (wide) {
        valid = tag == CONSTANT_Long || tag == CONSTANT_Double;
    } else {
        valid = tag == CONSTANT_Integer || tag == CONSTANT_Float || tag == CONSTANT_Class
            || tag == CONSTANT_String || tag == CONSTANT_MethodType || tag == CONSTANT_MethodHandle;
    }
    require(t, valid, "Incompatible constant for ldc instruction");
}

void branch(Thread* t, const uint8_t* starts, unsigned length, unsigned pc, int32_t displacement)
{
    if (!starts) return;
    int64_t target = int64_t(pc) + displacement;
    require(t, target >= 0 && target < length && starts[target], "Invalid bytecode branch target");
}

void local(Thread* t, GcCode* code, unsigned opcode, unsigned index)
{
    unsigned width = opcode == 22 || opcode == 24 || opcode == 55 || opcode == 57 ? 2 : 1;
    require(t, index < code->maxLocals() && width <= code->maxLocals() - index,
            "Local variable index exceeds max_locals");
}

unsigned instruction(Thread* t, GcCode* code, GcByteArray* tags, unsigned pc, const uint8_t* starts, unsigned major)
{
    const uint8_t* bytes = code->body().begin();
    unsigned length = code->length();
    unsigned opcode = bytes[pc];
    require(t, opcode <= 201, "Reserved or unknown bytecode instruction");
    unsigned size = 1;
    if (opcode == 16 || opcode == 18 || (opcode >= 21 && opcode <= 25)
        || (opcode >= 54 && opcode <= 58) || opcode == 169 || opcode == 188) size = 2;
    else if (opcode == 17 || opcode == 19 || opcode == 20 || opcode == 132
        || (opcode >= 153 && opcode <= 168) || (opcode >= 178 && opcode <= 184)
        || opcode == 187 || opcode == 189 || opcode == 192 || opcode == 193
        || opcode == 198 || opcode == 199) size = 3;
    else if (opcode == 197) size = 4;
    else if (opcode == 185 || opcode == 186 || opcode == 200 || opcode == 201) size = 5;
    else if (opcode == 196) {
        require(t, length - pc >= 2, "Truncated wide instruction");
        unsigned modified = bytes[pc + 1];
        require(t, modified == 132 || modified == 169 || (modified >= 21 && modified <= 25)
            || (modified >= 54 && modified <= 58), "Illegal wide instruction");
        size = modified == 132 ? 6 : 4;
    } else if (opcode == 170 || opcode == 171) {
        unsigned position = (pc + 4) & ~3u;
        unsigned header = opcode == 170 ? 12 : 8;
        require(t, position <= length && header <= length - position, "Truncated switch instruction");
        branch(t, starts, length, pc, word32(bytes + position));
        int64_t count;
        if (opcode == 170) {
            int32_t low = word32(bytes + position + 4);
            int32_t high = word32(bytes + position + 8);
            require(t, high >= low, "Invalid tableswitch range");
            count = int64_t(high) - low + 1;
        } else {
            count = word32(bytes + position + 4);
            require(t, count >= 0, "Invalid lookupswitch pair count");
        }
        unsigned stride = opcode == 170 ? 4 : 8;
        require(t, count <= (length - position - header) / stride, "Truncated switch targets");
        for (int64_t i = 0; i < count; ++i) {
            unsigned entry = position + header + static_cast<unsigned>(i) * stride;
            if (opcode == 171 && i) {
                require(t, word32(bytes + entry - stride) < word32(bytes + entry), "Unsorted lookupswitch keys");
            }
            branch(t, starts, length, pc, word32(bytes + entry + (opcode == 171 ? 4 : 0)));
        }
        return position + header + static_cast<unsigned>(count) * stride;
    }
    require(t, size <= length - pc, "Truncated bytecode instruction");
    if ((opcode >= 21 && opcode <= 25) || (opcode >= 54 && opcode <= 58) || opcode == 169 || opcode == 132) {
        local(t, code, opcode, bytes[pc + 1]);
    } else if (opcode >= 26 && opcode <= 45) {
        local(t, code, 21 + (opcode - 26) / 4, (opcode - 26) % 4);
    } else if (opcode >= 59 && opcode <= 78) {
        local(t, code, 54 + (opcode - 59) / 4, (opcode - 59) % 4);
    } else if (opcode == 196) {
        local(t, code, bytes[pc + 1], word16(bytes + pc + 2));
    }
    if (opcode == 18 || opcode == 19 || opcode == 20) {
        loadConstant(t, code, tags, opcode == 18 ? bytes[pc + 1] : word16(bytes + pc + 1), opcode == 20);
    } else if (opcode >= 178 && opcode <= 186) {
        unsigned tag = constantTag(t, tags, word16(bytes + pc + 1));
        bool valid;
        if (opcode <= 181) valid = tag == CONSTANT_Fieldref;
        else if (opcode == 182) valid = tag == CONSTANT_Methodref;
        else if (opcode == 183 || opcode == 184) valid = tag == CONSTANT_Methodref || (major >= 52 && tag == CONSTANT_InterfaceMethodref);
        else if (opcode == 185) valid = tag == CONSTANT_InterfaceMethodref;
        else valid = tag == CONSTANT_InvokeDynamic;
        require(t, valid, "Incompatible member reference for bytecode instruction");
        if (opcode == 185) {
            require(t, bytes[pc + 3] != 0 && bytes[pc + 4] == 0, "Invalid invokeinterface operands");
            GcReference* reference = cast<GcReference>(t, singletonObject(t, code->pool(), word16(bytes + pc + 1) - 1));
            require(t, bytes[pc + 3] == 1 + validateMethodDescriptor(t, reference->spec(), false), "Wrong invokeinterface argument count");
        }
        if (opcode == 186) require(t, bytes[pc + 3] == 0 && bytes[pc + 4] == 0, "Invalid invokedynamic operands");
    } else if (opcode == 187 || opcode == 189 || opcode == 192 || opcode == 193 || opcode == 197) {
        require(t, constantTag(t, tags, word16(bytes + pc + 1)) == CONSTANT_Class, "Invalid class reference in bytecode");
        if (opcode == 187 || opcode == 189 || opcode == 197) {
            GcReference* reference = cast<GcReference>(t, singletonObject(t, code->pool(), word16(bytes + pc + 1) - 1));
            GcByteArray* name = reference->name();
            unsigned dimensions = 0;
            while (dimensions + 1 < name->length() && name->body()[dimensions] == '[') ++dimensions;
            if (opcode == 187) require(t, dimensions == 0, "new cannot allocate an array class");
            if (opcode == 189) require(t, dimensions < 255, "anewarray exceeds the array dimension limit");
            if (opcode == 197) require(t, bytes[pc + 3] != 0 && bytes[pc + 3] <= dimensions,
                                      "Invalid multianewarray dimensions");
        }
    } else if (opcode == 188) {
        require(t, bytes[pc + 1] >= 4 && bytes[pc + 1] <= 11, "Invalid newarray primitive type");
    }
    if ((opcode >= 153 && opcode <= 168) || opcode == 198 || opcode == 199) {
        branch(t, starts, length, pc, static_cast<int16_t>(word16(bytes + pc + 1)));
    } else if (opcode == 200 || opcode == 201) {
        branch(t, starts, length, pc, word32(bytes + pc + 1));
    }
    return pc + size;
}

} // namespace

namespace {

unsigned descriptorType(Thread* t, GcByteArray* descriptor, unsigned position, bool allowVoid)
{
    const int8_t* bytes = descriptor->body().begin();
    unsigned length = descriptor->length() - 1;
    unsigned start = position;
    while (position < length && bytes[position] == '[') ++position;
    bool valid = position - start <= 255 && position < length;
    if (valid && bytes[position] == 'L') {
        unsigned name = ++position;
        while (position < length && bytes[position] != ';') {
            if (bytes[position] == '.' || bytes[position] == '[' || bytes[position] == 0) valid = false;
            ++position;
        }
        valid = valid && position < length && position > name;
        ++position;
    } else if (valid) {
        unsigned kind = bytes[position++];
        valid = (kind != 0 && strchr("ZBCSIFJD", kind)) || (kind == 'V' && allowVoid && position == start + 1);
    }
    if (!valid) throwNew(t, GcClassFormatError::Type, "Invalid type descriptor");
    return position;
}

} // namespace

void validateFieldDescriptor(Thread* t, GcByteArray* descriptor)
{
    if (descriptorType(t, descriptor, 0, false) != descriptor->length() - 1) {
        throwNew(t, GcClassFormatError::Type, "Trailing field descriptor data");
    }
}

unsigned validateMethodDescriptor(Thread* t, GcByteArray* descriptor, bool instance)
{
    unsigned length = descriptor->length() - 1;
    if (length < 3 || descriptor->body()[0] != '(') {
        throwNew(t, GcClassFormatError::Type, "Invalid method descriptor");
    }
    unsigned position = 1;
    unsigned slots = instance ? 1 : 0;
    while (position < length && descriptor->body()[position] != ')') {
        unsigned kind = descriptor->body()[position];
        position = descriptorType(t, descriptor, position, false);
        slots += kind == 'J' || kind == 'D' ? 2 : 1;
        if (slots > 255) throwNew(t, GcClassFormatError::Type, "Too many method parameter slots");
    }
    if (position >= length || descriptorType(t, descriptor, position + 1, true) != length) {
        throwNew(t, GcClassFormatError::Type, "Invalid method return descriptor");
    }
    return slots;
}

namespace {

unsigned slotsOf(unsigned kind)
{
    return kind == 'J' || kind == 'D' ? 2 : 1;
}

void methodShape(GcByteArray* descriptor, unsigned* argumentSlots, unsigned* returnSlots)
{
    const int8_t* bytes = descriptor->body().begin();
    unsigned position = 1;
    unsigned slots = 0;
    while (bytes[position] != ')') {
        unsigned kind = bytes[position];
        while (bytes[position] == '[') ++position;
        if (bytes[position] == 'L') {
            while (bytes[position] != ';') ++position;
        }
        ++position;
        slots += kind == '[' ? 1 : slotsOf(kind);
    }
    *argumentSlots = slots;
    unsigned kind = bytes[position + 1];
    *returnSlots = kind == 'V' ? 0 : slotsOf(kind);
}

void stackEffect(GcCode* code, unsigned pc, unsigned* pops, unsigned* pushes)
{
    const uint8_t* bytes = code->body().begin();
    unsigned opcode = bytes[pc];
    if (opcode == 196) opcode = bytes[pc + 1];
    unsigned in = 0, out = 0;
    if (opcode >= 1 && opcode <= 8) out = 1;
    else if (opcode == 9 || opcode == 10 || opcode == 14 || opcode == 15 || opcode == 20) out = 2;
    else if ((opcode >= 11 && opcode <= 13) || (opcode >= 16 && opcode <= 19)) out = 1;
    else if (opcode >= 21 && opcode <= 25) out = opcode == 22 || opcode == 24 ? 2 : 1;
    else if (opcode >= 26 && opcode <= 45) out = ((opcode - 26) / 4) % 2 == 1 ? 2 : 1;
    else if (opcode >= 46 && opcode <= 53) { in = 2; out = opcode == 47 || opcode == 49 ? 2 : 1; }
    else if (opcode >= 54 && opcode <= 58) in = opcode == 55 || opcode == 57 ? 2 : 1;
    else if (opcode >= 59 && opcode <= 78) in = ((opcode - 59) / 4) % 2 == 1 ? 2 : 1;
    else if (opcode >= 79 && opcode <= 86) in = opcode == 80 || opcode == 82 ? 4 : 3;
    else if (opcode == 87) in = 1;
    else if (opcode == 88) in = 2;
    else if (opcode == 89) { in = 1; out = 2; }
    else if (opcode == 90) { in = 2; out = 3; }
    else if (opcode == 91) { in = 3; out = 4; }
    else if (opcode == 92) { in = 2; out = 4; }
    else if (opcode == 93) { in = 3; out = 5; }
    else if (opcode == 94) { in = 4; out = 6; }
    else if (opcode == 95) { in = 2; out = 2; }
    else if (opcode >= 96 && opcode <= 115) { out = (opcode - 96) % 2 == 1 ? 2 : 1; in = 2 * out; }
    else if (opcode >= 116 && opcode <= 119) { out = opcode == 117 || opcode == 119 ? 2 : 1; in = out; }
    else if (opcode >= 120 && opcode <= 125) { out = opcode % 2 == 1 ? 2 : 1; in = out + 1; }
    else if (opcode >= 126 && opcode <= 131) { out = opcode % 2 == 1 ? 2 : 1; in = 2 * out; }
    else if (opcode >= 133 && opcode <= 147) {
        static const uint8_t table[15][2] = { {1,2}, {1,1}, {1,2}, {2,1}, {2,1}, {2,2}, {1,1}, {1,2},
            {1,2}, {2,1}, {2,2}, {2,1}, {1,1}, {1,1}, {1,1} };
        in = table[opcode - 133][0];
        out = table[opcode - 133][1];
    } else if (opcode == 148 || opcode == 151 || opcode == 152) { in = 4; out = 1; }
    else if (opcode == 149 || opcode == 150) { in = 2; out = 1; }
    else if ((opcode >= 153 && opcode <= 158) || opcode == 170 || opcode == 171
             || opcode == 198 || opcode == 199) in = 1;
    else if (opcode >= 159 && opcode <= 166) in = 2;
    else if (opcode == 168 || opcode == 201) out = 1;
    else if (opcode >= 172 && opcode <= 176) in = opcode == 173 || opcode == 175 ? 2 : 1;
    else if (opcode == 187) out = 1;
    else if (opcode == 188 || opcode == 189 || opcode == 190 || opcode == 192 || opcode == 193) { in = 1; out = 1; }
    else if (opcode == 191 || opcode == 194 || opcode == 195) in = 1;
    else if (opcode == 197) { in = bytes[pc + 3]; out = 1; }
    *pops = in;
    *pushes = out;
}

void memberEffect(Thread* t, GcCode* code, unsigned pc, unsigned* pops, unsigned* pushes)
{
    const uint8_t* bytes = code->body().begin();
    unsigned opcode = bytes[pc];
    unsigned index = word16(bytes + pc + 1) - 1;
    if (opcode == 186) {
        GcInvocation* invocation = cast<GcInvocation>(t, singletonObject(t, code->pool(), index));
        unsigned arguments, result;
        methodShape(invocation->template_()->spec(), &arguments, &result);
        *pops = arguments;
        *pushes = result;
        return;
    }
    GcReference* reference = cast<GcReference>(t, singletonObject(t, code->pool(), index));
    if (opcode >= 178 && opcode <= 181) {
        unsigned size = slotsOf(reference->spec()->body()[0]);
        *pops = opcode == 179 ? size : (opcode == 180 ? 1 : (opcode == 181 ? 1 + size : 0));
        *pushes = opcode == 178 || opcode == 180 ? size : 0;
        return;
    }
    unsigned arguments, result;
    methodShape(reference->spec(), &arguments, &result);
    *pops = arguments + (opcode == 184 ? 0 : 1);
    *pushes = result;
}

} // namespace

void verifyCode(Thread* t, GcCode* code, GcByteArray* tags, unsigned major, GcByteArray* descriptor,
                bool instance, bool constructor, GcByteArray* stackMaps, GcClass* owner)
{
    PROTECT(t, code);
    PROTECT(t, tags);
    PROTECT(t, descriptor);
    PROTECT(t, stackMaps);
    PROTECT(t, owner);
    unsigned returnKind = strchr(reinterpret_cast<const char*>(descriptor->body().begin()), ')')[1];
    THREAD_RUNTIME_ARRAY(t, uint8_t, starts, code->length());
    memset(RUNTIME_ARRAY_BODY(starts), 0, code->length());
    for (unsigned pc = 0; pc < code->length();) {
        RUNTIME_ARRAY_BODY(starts)[pc] = 1;
        unsigned opcode = code->body()[pc];
        if (opcode >= 172 && opcode <= 177) {
            unsigned expected = returnKind == 'V' ? 177 : (returnKind == 'J' ? 173
                : (returnKind == 'F' ? 174 : (returnKind == 'D' ? 175
                : (returnKind == 'L' || returnKind == '[' ? 176 : 172))));
            require(t, opcode == expected, "Return instruction does not match method descriptor");
        }
        require(t, major < 51 || (opcode != 168 && opcode != 169 && opcode != 201
            && !(opcode == 196 && pc + 1 < code->length() && code->body()[pc + 1] == 169)),
            "Subroutine instruction requires class file version below 51");
        pc = instruction(t, code, tags, pc, 0, major);
    }
    for (unsigned pc = 0; pc < code->length();) {
        pc = instruction(t, code, tags, pc, RUNTIME_ARRAY_BODY(starts), major);
    }
    GcExceptionHandlerTable* handlers = cast<GcExceptionHandlerTable>(t, code->exceptionHandlerTable());
    PROTECT(t, handlers);
    if (handlers) {
        for (unsigned i = 0; i < handlers->length(); ++i) {
            uint64_t entry = handlers->body()[i];
            if (!RUNTIME_ARRAY_BODY(starts)[exceptionHandlerStart(entry)]
                || (exceptionHandlerEnd(entry) != code->length() && !RUNTIME_ARRAY_BODY(starts)[exceptionHandlerEnd(entry)])
                || !RUNTIME_ARRAY_BODY(starts)[exceptionHandlerIp(entry)]) {
                throwNew(t, GcClassFormatError::Type, "Exception handler offset is not an instruction boundary");
            }
        }
    }
    // Legacy subroutine return addresses need a separate flow model.
    if (major < 51) return;
    GcArray* maps = readStackMaps(t, code, tags, stackMaps, descriptor, instance, constructor && owner->super(),
                                 RUNTIME_ARRAY_BODY(starts));
    PROTECT(t, maps);
    auto needMap = [&](unsigned target) {
        require(t, mapAt(t, maps, target) != 0, "Missing StackMapTable frame at control flow target");
    };
    if (handlers) {
        for (unsigned i = 0; i < handlers->length(); ++i) needMap(exceptionHandlerIp(handlers->body()[i]));
    }
    for (unsigned pc = 0; pc < code->length();) {
        const uint8_t* bytes = code->body().begin();
        unsigned opcode = bytes[pc];
        unsigned next = instruction(t, code, tags, pc, RUNTIME_ARRAY_BODY(starts), major);
        if ((opcode >= 153 && opcode <= 167) || opcode == 198 || opcode == 199) {
            needMap(pc + static_cast<int16_t>(word16(bytes + pc + 1)));
        } else if (opcode == 200) needMap(pc + word32(bytes + pc + 1));
        else if (opcode == 170 || opcode == 171) {
            unsigned position = (pc + 4) & ~3u;
            needMap(pc + word32(bytes + position));
            unsigned count = opcode == 170 ? word32(bytes + position + 8) - word32(bytes + position + 4) + 1
                : word32(bytes + position + 4);
            unsigned stride = opcode == 170 ? 4 : 8;
            position += opcode == 170 ? 12 : 8;
            for (unsigned i = 0; i < count; ++i) {
                needMap(pc + word32(bytes + position + i * stride + (opcode == 171 ? 4 : 0)));
            }
        }
        bool terminal = opcode == 167 || opcode == 200 || opcode == 170 || opcode == 171
            || (opcode >= 172 && opcode <= 177) || opcode == 191;
        if (terminal && next < code->length()) needMap(next);
        pc = next;
    }
    THREAD_RUNTIME_ARRAY(t, int32_t, depths, code->length());
    THREAD_RUNTIME_ARRAY(t, unsigned, pending, code->length());
    for (unsigned i = 0; i < code->length(); ++i) RUNTIME_ARRAY_BODY(depths)[i] = -1;
    unsigned head = 0, tail = 0;
    auto enqueue = [&](unsigned pc, unsigned depth) {
        require(t, pc < code->length(), "Execution falls outside Code array");
        require(t, depth <= code->maxStack(), "Operand stack exceeds max_stack");
        if (RUNTIME_ARRAY_BODY(depths)[pc] < 0) {
            RUNTIME_ARRAY_BODY(depths)[pc] = static_cast<int32_t>(depth);
            RUNTIME_ARRAY_BODY(pending)[tail++] = pc;
        } else {
            require(t, RUNTIME_ARRAY_BODY(depths)[pc] == static_cast<int32_t>(depth),
                    "Inconsistent operand stack height at merge point");
        }
    };
    enqueue(0, 0);
    for (unsigned i = 0; maps && i < maps->length(); ++i) {
        GcByteArray* frame = cast<GcByteArray>(t, maps->body()[i]);
        enqueue(mapPc(frame), mapStackCount(frame));
    }
    while (head < tail) {
        unsigned pc = RUNTIME_ARRAY_BODY(pending)[head++];
        unsigned depth = RUNTIME_ARRAY_BODY(depths)[pc];
        const uint8_t* bytes = code->body().begin();
        unsigned opcode = bytes[pc];
        unsigned next = instruction(t, code, tags, pc, RUNTIME_ARRAY_BODY(starts), major);
        unsigned pops, pushes;
        if (opcode >= 178 && opcode <= 186) memberEffect(t, code, pc, &pops, &pushes);
        else stackEffect(code, pc, &pops, &pushes);
        require(t, depth >= pops, "Operand stack underflow");
        unsigned after = depth - pops + pushes;
        require(t, after <= code->maxStack(), "Operand stack exceeds max_stack");
        if (handlers) {
            for (unsigned i = 0; i < handlers->length(); ++i) {
                uint64_t entry = handlers->body()[i];
                if (pc >= exceptionHandlerStart(entry) && pc < exceptionHandlerEnd(entry)) {
                    enqueue(exceptionHandlerIp(entry), 1);
                }
            }
        }
        if ((opcode >= 153 && opcode <= 168) || opcode == 198 || opcode == 199) {
            enqueue(pc + static_cast<int16_t>(word16(bytes + pc + 1)), after);
        } else if (opcode == 200 || opcode == 201) {
            enqueue(pc + word32(bytes + pc + 1), after);
        } else if (opcode == 170 || opcode == 171) {
            unsigned position = (pc + 4) & ~3u;
            enqueue(pc + word32(bytes + position), after);
            unsigned count = opcode == 170 ? word32(bytes + position + 8) - word32(bytes + position + 4) + 1
                : word32(bytes + position + 4);
            unsigned stride = opcode == 170 ? 4 : 8;
            position += opcode == 170 ? 12 : 8;
            for (unsigned i = 0; i < count; ++i) {
                enqueue(pc + word32(bytes + position + i * stride + (opcode == 171 ? 4 : 0)), after);
            }
        }
        bool terminal = opcode == 167 || opcode == 200 || opcode == 170 || opcode == 171
            || (opcode >= 172 && opcode <= 177) || opcode == 191 || opcode == 169
            || (opcode == 196 && bytes[pc + 1] == 169);
        if (!terminal) enqueue(next, after);
    }
    for (unsigned pc = 0; pc < code->length();) {
        unsigned next = instruction(t, code, tags, pc, RUNTIME_ARRAY_BODY(starts), major);
        RUNTIME_ARRAY_BODY(pending)[pc] = next;
        pc = next;
    }
    verifyStackTypes(t, code, tags, RUNTIME_ARRAY_BODY(depths), RUNTIME_ARRAY_BODY(pending), descriptor, instance, maps, constructor && owner->super(), owner);
}

} // namespace vm
