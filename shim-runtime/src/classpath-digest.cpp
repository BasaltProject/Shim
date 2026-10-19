/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

#include "shim/machine.h"

using namespace vm;

namespace {
uint32_t rotateLeft(uint32_t value, unsigned bits)
{
  return (value << bits) | (value >> (32 - bits));
}
} // namespace

// Only the compression function is native. DigestBase retains buffering,
// padding, length accounting, resets and public API validation. All ranges are
// internal complete-block ranges; this helper allocates nothing and keeps no
// borrowed body pointers beyond the call.
extern "C" SHIM_EXPORT void JNICALL
Shim_sun_security_provider_SHA_compressBlocks(Thread*, object, uintptr_t* arguments)
{
  GcIntArray* state = reinterpret_cast<GcIntArray*>(arguments[0]);
  GcByteArray* input = reinterpret_cast<GcByteArray*>(arguments[1]);
  const int32_t offset = arguments[2];
  const int32_t length = arguments[3];
  const uint8_t* bytes = reinterpret_cast<const uint8_t*>(input->body().begin()) + offset;
  uint32_t h[5];
  for (unsigned i = 0; i < 5; ++i) h[i] = static_cast<uint32_t>(state->body()[i]);
  for (int32_t block = 0; block < length; block += 64) {
    uint32_t w[80];
    for (unsigned i = 0; i < 16; ++i) {
      const uint8_t* p = bytes + block + i * 4;
      w[i] = (static_cast<uint32_t>(p[0]) << 24)
          | (static_cast<uint32_t>(p[1]) << 16)
          | (static_cast<uint32_t>(p[2]) << 8) | p[3];
    }
    for (unsigned i = 16; i < 80; ++i) {
      w[i] = rotateLeft(w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16], 1);
    }
    uint32_t a = h[0], b = h[1], c = h[2], d = h[3], e = h[4];
    for (unsigned i = 0; i < 80; ++i) {
      uint32_t f, k;
      if (i < 20) { f = (b & c) | (~b & d); k = 0x5a827999; }
      else if (i < 40) { f = b ^ c ^ d; k = 0x6ed9eba1; }
      else if (i < 60) { f = (b & c) | (b & d) | (c & d); k = 0x8f1bbcdc; }
      else { f = b ^ c ^ d; k = 0xca62c1d6; }
      const uint32_t next = rotateLeft(a, 5) + f + e + k + w[i];
      e = d; d = c; c = rotateLeft(b, 30); b = a; a = next;
    }
    h[0] += a; h[1] += b; h[2] += c; h[3] += d; h[4] += e;
  }
  for (unsigned i = 0; i < 5; ++i) state->body()[i] = static_cast<int32_t>(h[i]);
}
