/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

#ifndef SHIM_VERIFY_TYPES_H
#define SHIM_VERIFY_TYPES_H

#include "shim/machine.h"

namespace vm {
namespace verification {

enum Kind { kTop = 0, kInt, kFloat, kReference, kLong, kDouble, kSecond, kNull, kUninitializedThis, kAllocationBase };
// Allocation values encode kAllocationBase + bytecode offset, including offsets above 65,526.
typedef uint32_t Value;
enum ArrayElement { eBoolean = 1, eByte, eChar, eShort, eInt, eLong, eFloat, eDouble, eObject };
const unsigned kArrayBase = 0x20000;
inline bool isArrayKind(unsigned kind) { return kind >= kArrayBase && kind < kArrayBase + 0x1000; }
inline unsigned arrayRank(unsigned kind) { return (kind - kArrayBase) >> 4; }
inline unsigned arrayElement(unsigned kind) { return kind & 15; }
inline unsigned arrayKind(unsigned rank, unsigned element) { return kArrayBase + (rank << 4) + element; }
inline bool isUninitialized(unsigned kind) { return kind >= kUninitializedThis && kind < kArrayBase; }
inline bool isWideKind(unsigned kind) { return kind == kLong || kind == kDouble; }
inline bool isReferenceKind(unsigned kind) { return kind == kReference || kind == kNull || isArrayKind(kind); }
inline bool isMovableReference(unsigned kind) { return isUninitialized(kind) || isReferenceKind(kind); }
inline bool assignable(unsigned actual, unsigned expected)
{
    if (expected == kTop || actual == expected) return true;
    if (actual == kNull && isReferenceKind(expected)) return true;
    if (expected == kReference && isReferenceKind(actual)) return true;
    if (isArrayKind(actual) && isArrayKind(expected)) {
        return arrayElement(expected) == eObject &&
            (arrayRank(actual) > arrayRank(expected)
             || (arrayRank(actual) == arrayRank(expected) && arrayElement(actual) == eObject));
    }
    return false;
}
inline unsigned mergeKind(unsigned first, unsigned second)
{
    if (first == second) return first;
    if (first == kNull && isReferenceKind(second)) return second;
    if (second == kNull && isReferenceKind(first)) return first;
    if (isArrayKind(first) && isArrayKind(second)) {
        unsigned rank = arrayRank(first) < arrayRank(second) ? arrayRank(first) : arrayRank(second);
        bool primitive = (arrayRank(first) == rank && arrayElement(first) != eObject)
            || (arrayRank(second) == rank && arrayElement(second) != eObject);
        if (primitive) --rank;
        return rank ? arrayKind(rank, eObject) : kReference;
    }
    return isReferenceKind(first) && isReferenceKind(second) ? kReference : kTop;
}
inline unsigned descriptorKind(unsigned kind)
{
    switch (kind) {
    case 'V': return 0;
    case 'J': return kLong;
    case 'D': return kDouble;
    case 'F': return kFloat;
    case 'L': case '[': return kReference;
    default: return kInt;
    }
}
inline unsigned descriptorValue(const int8_t* bytes)
{
    unsigned rank = 0;
    while (bytes[rank] == '[') ++rank;
    if (!rank) return descriptorKind(bytes[0]);
    unsigned element;
    switch (bytes[rank]) {
    case 'Z': element = eBoolean; break;
    case 'B': element = eByte; break;
    case 'C': element = eChar; break;
    case 'S': element = eShort; break;
    case 'I': element = eInt; break;
    case 'J': element = eLong; break;
    case 'F': element = eFloat; break;
    case 'D': element = eDouble; break;
    default: element = eObject; break;
    }
    return arrayKind(rank, element);
}
inline unsigned classValue(GcByteArray* name)
{
    return name->body()[0] == '[' ? descriptorValue(name->body().begin()) : kReference;
}
inline unsigned componentValue(unsigned kind)
{
    return arrayRank(kind) > 1 ? arrayKind(arrayRank(kind) - 1, arrayElement(kind)) : kReference;
}
inline unsigned mapWord(GcByteArray* frame, unsigned position)
{
    const uint8_t* bytes = reinterpret_cast<const uint8_t*>(frame->body().begin());
    return (unsigned(bytes[position]) << 8) | bytes[position + 1];
}
inline unsigned mapPc(GcByteArray* frame) { return mapWord(frame, 0); }
inline unsigned mapLocalCount(GcByteArray* frame) { return mapWord(frame, 2); }
inline unsigned mapStackCount(GcByteArray* frame) { return mapWord(frame, 4); }
// Frames contain three big-endian u16 header fields followed by big-endian u32 values.
inline unsigned mapValue(GcByteArray* frame, unsigned index)
{
    const uint8_t* bytes = reinterpret_cast<const uint8_t*>(frame->body().begin()) + 6 + 4 * index;
    return (unsigned(bytes[0]) << 24) | (unsigned(bytes[1]) << 16) | (unsigned(bytes[2]) << 8) | bytes[3];
}
inline unsigned mapLocal(GcByteArray* frame, unsigned index)
{
    return index < mapLocalCount(frame) ? mapValue(frame, index) : kTop;
}
inline unsigned mapStackValue(GcByteArray* frame, unsigned index)
{
    return mapValue(frame, mapLocalCount(frame) + index);
}
inline bool mapUninitialized(GcByteArray* frame)
{
    for (unsigned i = 0; i < mapLocalCount(frame); ++i) if (mapLocal(frame, i) == kUninitializedThis) return true;
    return false;
}
inline GcByteArray* mapAt(Thread* t, GcArray* maps, unsigned pc)
{
    unsigned low = 0, high = maps ? maps->length() : 0;
    while (low < high) {
        unsigned middle = (low + high) / 2;
        GcByteArray* frame = cast<GcByteArray>(t, maps->body()[middle]);
        unsigned target = mapPc(frame);
        if (target == pc) return frame;
        if (target < pc) low = middle + 1;
        else high = middle;
    }
    return 0;
}

} // namespace verification

GcArray* readStackMaps(Thread* t, GcCode* code, GcByteArray* tags, GcByteArray* raw,
                       GcByteArray* descriptor, bool instance, bool constructor, const uint8_t* starts);

} // namespace vm
#endif
