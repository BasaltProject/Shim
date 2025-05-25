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

#ifndef AVIAN_UTIL_CPP_H
#define AVIAN_UTIL_CPP_H

#include "allocator.h"
#include "math.h"
#include "assert.h"

namespace avian {
namespace util {

template <class T>
struct NonConst;

template <class T>
struct NonConst<const T> {
  typedef T Type;
};

template <class T>
struct NonConst {
  typedef T Type;
};

template <class... Ts>
struct ArgumentCount;

template <class T, class... Ts>
struct ArgumentCount<T, Ts...> {
  enum { Result = 1 + ArgumentCount<Ts...>::Result };
};

template <>
struct ArgumentCount<> {
  enum { Result = 0 };
};

template<class T>
void setArrayElements(T*) {
}

template<class T, class... Ts>
void setArrayElements(T* arr, T elem, Ts... ts) {
  *arr = elem;
  setArrayElements(arr, ts...);
}

}  // namespace util
}  // namespace avian

#endif  // AVIAN_UTIL_CPP_H
