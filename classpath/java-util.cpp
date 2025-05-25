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

#include "time.h"
#include "jni.h"
#include "jni-util.h"

namespace {

#if (!defined PLATFORM_WINDOWS) || (defined _MSC_VER)

void removeNewline(char* s)
{
  for (; s; ++s) {
    if (*s == '\n') {
      *s = 0;
      break;
    }
  }
}

#endif

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
    Java_java_util_Date_toString(JNIEnv* e, jclass c UNUSED, jlong when)
{
  const unsigned BufferSize UNUSED = 27;

  time_t time = when / 1000;

#ifdef PLATFORM_WINDOWS
  e->MonitorEnter(c);
#ifdef _MSC_VER
  char buffer[BufferSize];
  ctime_s(buffer, BufferSize, &time);
  removeNewline(buffer);
#else
  char* buffer = ctime(&time);
#endif
  jstring r = e->NewStringUTF(buffer);
  e->MonitorExit(c);
  return r;
#else
  char buffer[BufferSize];
  ctime_r(&time, buffer);
  removeNewline(buffer);
  return e->NewStringUTF(buffer);
#endif
}
