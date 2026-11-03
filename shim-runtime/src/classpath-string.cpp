/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

#include "shim/machine.h"
#include <cstring>

using namespace vm;

namespace {
// A borrowed view, valid only within allocation-free VM helpers.
struct StringView {
  const uint8_t* bytes;
  const uint16_t* chars;
  int32_t length;

  StringView(Thread* t, GcString* value) : bytes(0), chars(0), length(value->length(t)) {
    const int32_t offset = value->offset(t);
    if (objectClass(t, value->data()) == type(t, GcByteArray::Type)) {
      bytes = reinterpret_cast<const uint8_t*>(cast<GcByteArray>(t, value->data())->body().begin() + offset);
    } else {
      chars = cast<GcCharArray>(t, value->data())->body().begin() + offset;
    }
  }

  uint16_t at(int32_t index) const { return bytes ? bytes[index] : chars[index]; }
};

// Exact UTF-16 code-unit search, also when the two Strings use different
// storage widths. Offsets and ranges were checked by String.indexOf.
template <class Source, class Target>
int32_t findString(const Source* source, int32_t length,
                   const Target* target, int32_t targetLength, int32_t start)
{
  const int32_t last = length - targetLength;
  for (int32_t i = start; i <= last; ++i) {
    if (source[i] != target[0]) continue;
    int32_t j = 1;
    while (j < targetLength && source[i + j] == target[j]) ++j;
    if (j == targetLength) return i;
  }
  return -1;
}

inline int32_t firstMismatch(const uint8_t* a, const uint8_t* b, int32_t n)
{
  int32_t i = 0;
  for (; i + 8 <= n; i += 8) {
    uint64_t x, y;
    std::memcpy(&x, a + i, 8);
    std::memcpy(&y, b + i, 8);
    if (x != y) break;
  }
  while (i < n && a[i] == b[i]) ++i;
  return i;
}

template <class Unit>
inline uint32_t hashUnits(const Unit* p, int32_t n)
{
  uint32_t h = 0;
  int32_t i = 0;
  for (; i + 4 <= n; i += 4) {
    h = h * 923521u + p[i] * 29791u + p[i + 1] * 961u + p[i + 2] * 31u + p[i + 3];
  }
  for (; i < n; ++i) h = h * 31 + p[i];
  return h;
}

template <class A, class B>
inline int32_t compareUnits(const A* a, const B* b, int32_t n)
{
  for (int32_t i = 0; i < n; ++i) {
    const int32_t d = static_cast<int32_t>(a[i]) - b[i];
    if (d) return d;
  }
  return 0;
}
} // namespace

extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_copyCharStorage(Thread* t, object, uintptr_t* arguments)
{
  GcCharArray* source = reinterpret_cast<GcCharArray*>(arguments[0]);
  const int32_t offset = arguments[1];
  const int32_t length = arguments[2];
  // Allocation may move the input. Keep a rooted reference and reacquire its
  // body afterwards; never retain a body pointer across a GC safepoint.
  PROTECT(t, source);
  uint16_t bits = 0;
  {
    const uint16_t* in = source->body().begin() + offset;
    for (int32_t i = 0; i < length; ++i) bits |= in[i];
  }
  if (bits < 0x80) {
    GcByteArray* result = makeByteArray(t, length);
    const uint16_t* in = source->body().begin() + offset;
    uint8_t* out = reinterpret_cast<uint8_t*>(result->body().begin());
    for (int32_t i = 0; i < length; ++i) out[i] = static_cast<uint8_t>(in[i]);
    return reinterpret_cast<int64_t>(result);
  }
  GcCharArray* result = makeCharArray(t, length);
  if (length != 0) std::memcpy(result->body().begin(), source->body().begin() + offset, length * sizeof(uint16_t));
  return reinterpret_cast<int64_t>(result);
}

extern "C" SHIM_EXPORT void JNICALL
Shim_java_lang_String_widenBytes(Thread*, object, uintptr_t* arguments)
{
  GcByteArray* source = reinterpret_cast<GcByteArray*>(arguments[0]);
  const int32_t offset = arguments[1];
  GcCharArray* target = reinterpret_cast<GcCharArray*>(arguments[2]);
  const int32_t targetOffset = arguments[3];
  const int32_t length = arguments[4];
  for (int32_t i = 0; i < length; ++i) {
    target->body()[targetOffset + i] = static_cast<uint8_t>(source->body()[offset + i]);
  }
}

extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_indexOfString(Thread* t, object, uintptr_t* arguments)
{
  GcString* source = reinterpret_cast<GcString*>(arguments[0]);
  GcString* target = reinterpret_cast<GcString*>(arguments[1]);
  const int32_t start = arguments[2];
  const bool sourceBytes = objectClass(t, source->data()) == type(t, GcByteArray::Type);
  const bool targetBytes = objectClass(t, target->data()) == type(t, GcByteArray::Type);
  const int32_t offset = source->offset(t);
  const int32_t targetOffset = target->offset(t);
  const int32_t length = source->length(t);
  const int32_t targetLength = target->length(t);
  if (sourceBytes) {
    const uint8_t* data = reinterpret_cast<const uint8_t*>(&cast<GcByteArray>(t, source->data())->body()[offset]);
    if (targetBytes) {
      return findString(data, length, reinterpret_cast<const uint8_t*>(&cast<GcByteArray>(t, target->data())->body()[targetOffset]), targetLength, start);
    }
    return findString(data, length, &cast<GcCharArray>(t, target->data())->body()[targetOffset], targetLength, start);
  }
  const uint16_t* data = &cast<GcCharArray>(t, source->data())->body()[offset];
  if (targetBytes) {
    return findString(data, length, reinterpret_cast<const uint8_t*>(&cast<GcByteArray>(t, target->data())->body()[targetOffset]), targetLength, start);
  }
  return findString(data, length, &cast<GcCharArray>(t, target->data())->body()[targetOffset], targetLength, start);
}

extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_indexOfCodePoint(Thread* t, object, uintptr_t* arguments)
{
  StringView source(t, reinterpret_cast<GcString*>(arguments[0]));
  const int32_t codePoint = arguments[1];
  const int32_t start = arguments[2];
  if (codePoint < 0x10000) {
    if (source.bytes && codePoint >= 0x80) return -1;
    for (int32_t i = start; i < source.length; ++i) {
      if (source.at(i) == codePoint) return i;
    }
  } else if (!source.bytes) {
    const uint16_t high = 0xd800 + ((codePoint - 0x10000) >> 10);
    const uint16_t low = 0xdc00 + ((codePoint - 0x10000) & 0x3ff);
    for (int32_t i = start; i < source.length - 1; ++i) {
      if (source.at(i) == high && source.at(i + 1) == low) return i;
    }
  }
  return -1;
}

extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_hashStorage(Thread* t, object, uintptr_t* arguments)
{
  StringView source(t, reinterpret_cast<GcString*>(arguments[0]));
  const uint32_t hash = source.bytes ? hashUnits(source.bytes, source.length) : hashUnits(source.chars, source.length);
  return static_cast<int32_t>(hash);
}

extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_compareStorage(Thread* t, object, uintptr_t* arguments)
{
  StringView source(t, reinterpret_cast<GcString*>(arguments[0]));
  StringView target(t, reinterpret_cast<GcString*>(arguments[1]));
  const int32_t end = source.length < target.length ? source.length : target.length;
  int32_t difference;
  if (source.bytes && target.bytes) {
    const int32_t i = firstMismatch(source.bytes, target.bytes, end);
    difference = i < end ? static_cast<int32_t>(source.bytes[i]) - target.bytes[i] : 0;
  } else if (source.bytes) {
    difference = compareUnits(source.bytes, target.chars, end);
  } else if (target.bytes) {
    difference = compareUnits(source.chars, target.bytes, end);
  } else {
    difference = compareUnits(source.chars, target.chars, end);
  }
  return difference ? difference : source.length - target.length;
}

// This is only a fast path for validated UTF-8/US-ASCII input. Any high byte
// falls back to StringCoding's standard decoder and replacement semantics.
extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_decodeAsciiBytes(Thread* t, object, uintptr_t* arguments)
{
  GcByteArray* source = reinterpret_cast<GcByteArray*>(arguments[0]);
  const int32_t offset = arguments[1];
  const int32_t length = arguments[2];
  PROTECT(t, source);
  {
    const uint8_t* in = reinterpret_cast<const uint8_t*>(source->body().begin() + offset);
    uint8_t bits = 0;
    for (int32_t i = 0; i < length; ++i) bits |= in[i];
    if (bits & 0x80) return 0;
  }
  GcCharArray* result = makeCharArray(t, length);
  {
    const uint8_t* in = reinterpret_cast<const uint8_t*>(source->body().begin() + offset);
    uint16_t* out = result->body().begin();
    for (int32_t i = 0; i < length; ++i) out[i] = in[i];
  }
  return reinterpret_cast<int64_t>(result);
}

namespace {
template <class Unit>
inline int32_t scanCase(const Unit* p, int32_t n, uint16_t lo, uint16_t hi)
{
  uint16_t bits = 0;
  int32_t changed = 0;
  for (int32_t i = 0; i < n; ++i) {
    bits |= p[i];
    changed |= (p[i] >= lo && p[i] <= hi);
  }
  return bits >= 0x80 ? -1 : changed;
}
}

// Returns null for non-ASCII input, this when nothing changes, else the converted bytes.
extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_caseAscii(Thread* t, object, uintptr_t* arguments)
{
  GcString* self = reinterpret_cast<GcString*>(arguments[0]);
  const bool upper = arguments[1] != 0;
  const uint16_t lo = upper ? 'a' : 'A';
  const uint16_t hi = upper ? 'z' : 'Z';
  int32_t changed;
  {
    StringView v(t, self);
    changed = v.bytes ? scanCase(v.bytes, v.length, lo, hi) : scanCase(v.chars, v.length, lo, hi);
  }
  if (changed < 0) return 0;
  if (changed == 0) return reinterpret_cast<int64_t>(self);
  PROTECT(t, self);
  GcByteArray* result = makeByteArray(t, self->length(t));
  StringView v(t, self);
  uint8_t* out = reinterpret_cast<uint8_t*>(result->body().begin());
  for (int32_t i = 0; i < v.length; ++i) {
    const uint16_t c = v.at(i);
    out[i] = static_cast<uint8_t>(c >= lo && c <= hi ? (upper ? c - 32 : c + 32) : c);
  }
  return reinterpret_cast<int64_t>(result);
}

extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_equalsStorage(Thread* t, object, uintptr_t* arguments)
{
  StringView a(t, reinterpret_cast<GcString*>(arguments[0]));
  StringView b(t, reinterpret_cast<GcString*>(arguments[1]));
  if (a.length != b.length) return 0;
  if (a.bytes && b.bytes) return std::memcmp(a.bytes, b.bytes, a.length) == 0;
  if (a.chars && b.chars) return std::memcmp(a.chars, b.chars, a.length * sizeof(uint16_t)) == 0;
  return compareUnits(a.bytes ? a.bytes : b.bytes, a.bytes ? b.chars : a.chars, a.length) == 0;
}

// Returns null when a unit is not ASCII.
extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_encodeAscii(Thread* t, object, uintptr_t* arguments)
{
  GcString* self = reinterpret_cast<GcString*>(arguments[0]);
  int32_t bits;
  {
    StringView v(t, self);
    bits = v.bytes ? scanCase(v.bytes, v.length, 1, 0) : scanCase(v.chars, v.length, 1, 0);
  }
  if (bits < 0) return 0;
  PROTECT(t, self);
  GcByteArray* result = makeByteArray(t, self->length(t));
  StringView v(t, self);
  uint8_t* out = reinterpret_cast<uint8_t*>(result->body().begin());
  if (v.bytes) {
    std::memcpy(out, v.bytes, v.length);
  } else {
    for (int32_t i = 0; i < v.length; ++i) out[i] = static_cast<uint8_t>(v.chars[i]);
  }
  return reinterpret_cast<int64_t>(result);
}
