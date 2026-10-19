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
  bool ascii = true;
  for (int32_t i = 0; i < length; ++i) {
    if (source->body()[offset + i] >= 0x80) { ascii = false; break; }
  }
  if (ascii) {
    GcByteArray* result = makeByteArray(t, length);
    for (int32_t i = 0; i < length; ++i) {
      result->body()[i] = source->body()[offset + i];
    }
    return reinterpret_cast<int64_t>(result);
  }
  GcCharArray* result = makeCharArray(t, length);
  if (length != 0) std::memcpy(&result->body()[0], &source->body()[offset], length * sizeof(uint16_t));
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
  uint32_t hash = 0;
  for (int32_t i = 0; i < source.length; ++i) hash = hash * 31 + source.at(i);
  return static_cast<int32_t>(hash);
}

extern "C" SHIM_EXPORT int64_t JNICALL
Shim_java_lang_String_compareStorage(Thread* t, object, uintptr_t* arguments)
{
  StringView source(t, reinterpret_cast<GcString*>(arguments[0]));
  StringView target(t, reinterpret_cast<GcString*>(arguments[1]));
  const int32_t end = source.length < target.length ? source.length : target.length;
  for (int32_t i = 0; i < end; ++i) {
    const int32_t difference = static_cast<int32_t>(source.at(i)) - target.at(i);
    if (difference) return difference;
  }
  return source.length - target.length;
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
  for (int32_t i = 0; i < length; ++i) {
    if (source->body()[offset + i] < 0) return 0;
  }
  GcCharArray* result = makeCharArray(t, length);
  for (int32_t i = 0; i < length; ++i) {
    result->body()[i] = static_cast<uint8_t>(source->body()[offset + i]);
  }
  return reinterpret_cast<int64_t>(result);
}
