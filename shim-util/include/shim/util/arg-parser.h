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

#ifndef SHIM_UTIL_ARG_PARSER_H
#define SHIM_UTIL_ARG_PARSER_H

namespace shim {
namespace util {

class Arg;

class ArgParser {
 public:
  ArgParser();

  bool parse(int ac, const char* const* av);
  void printUsage(const char* exe);

 private:
  friend class Arg;

  Arg* first;
  Arg** last;
};

class Arg {
 public:
  Arg* next;
  bool required;
  const char* name;
  const char* desc;

  const char* value;

  Arg(ArgParser& parser, bool required, const char* name, const char* desc);
};

}  // namespace shim
}  // namespace util

#endif  // SHIM_UTIL_ARG_PARSER_H
