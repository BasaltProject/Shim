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

#ifndef AVIAN_UTIL_TOKENIZER_H
#define AVIAN_UTIL_TOKENIZER_H

#include "string.h"

namespace avian {
namespace util {

class Tokenizer {
 public:
  Tokenizer(const char* s, char delimiter)
      : s(s), limit(0), delimiter(delimiter)
  {
  }

  Tokenizer(String str, char delimiter)
      : s(str.text), limit(str.text + str.length), delimiter(delimiter)
  {
  }

  bool hasMore()
  {
    while (s != limit and *s == delimiter)
      ++s;
    return s != limit and *s != 0;
  }

  String next()
  {
    const char* p = s;
    while (s != limit and *s and *s != delimiter)
      ++s;
    return String(p, s - p);
  }

  const char* s;
  const char* limit;
  char delimiter;
};

}  // namespace util
}  // namespace avain

#endif  // AVIAN_UTIL_TOKENIZER_H
