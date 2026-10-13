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

#ifndef SHIM_TOOLS_TYPE_GENERATOR_SEXPR_H
#define SHIM_TOOLS_TYPE_GENERATOR_SEXPR_H

namespace shim {
namespace tools {
namespace typegenerator {

template <class T>
inline T* allocate()
{
  T* t = static_cast<T*>(malloc(sizeof(T)));
  assert(t);
  return t;
}

class Object {
 public:
  typedef enum {
    Scalar,
    Array,
    Method,
    Type,
    Pair,
    Character,
    String,
    Eos
  } ObjectType;

  ObjectType type;
};

class Pair : public Object {
 public:
  Object* car;
  Object* cdr;

  static Pair* make(Object* car, Object* cdr)
  {
    Pair* o = allocate<Pair>();
    o->type = Object::Pair;
    o->car = car;
    o->cdr = cdr;
    return o;
  }
};

inline Object* cons(Object* car, Object* cdr)
{
  return Pair::make(car, cdr);
}

inline Object*& car(Object* o)
{
  assert(o->type == Object::Pair);
  return static_cast<Pair*>(o)->car;
}

inline void setCar(Object* o, Object* v)
{
  assert(o->type == Object::Pair);
  static_cast<Pair*>(o)->car = v;
}

inline Object*& cdr(Object* o)
{
  assert(o->type == Object::Pair);
  return static_cast<Pair*>(o)->cdr;
}

inline void setCdr(Object* o, Object* v)
{
  assert(o->type == Object::Pair);
  static_cast<Pair*>(o)->cdr = v;
}

class List {
 public:
  Object* first;
  Object* last;

  List() : first(0), last(0)
  {
  }

  void append(Object* o)
  {
    Object* p = cons(o, 0);
    if (last) {
      setCdr(last, p);
      last = p;
    } else {
      first = last = p;
    }
  }
};

}  // namespace typegenerator
}  // namespace tools
}  // namespace shim

#endif  // SHIM_TOOLS_TYPE_GENERATOR_SEXPR_H
