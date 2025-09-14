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
inline bool isUninitialized(unsigned kind) { return kind >= kUninitializedThis; }
inline bool isMovableReference(unsigned kind) { return isUninitialized(kind) || kind == kReference || kind == kNull; }

inline bool isWideKind(unsigned kind) { return kind == kLong || kind == kDouble; }
inline bool isReferenceKind(unsigned kind) { return kind == kReference || kind == kNull; }
inline bool assignable(unsigned actual, unsigned expected)
{
    return expected == kTop || actual == expected || (actual == kNull && expected == kReference);
}
inline unsigned mergeKind(unsigned first, unsigned second)
{
    return first == second ? first : (isReferenceKind(first) && isReferenceKind(second) ? kReference : kTop);
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
