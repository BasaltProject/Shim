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

#include "avian/machine.h"

using namespace vm;

extern "C" AVIAN_EXPORT jint JNICALL net_JNI_OnLoad(JavaVM*, void*)
{
  return 0;
}

extern "C" AVIAN_EXPORT jint JNICALL management_JNI_OnLoad(JavaVM*, void*)
{
  return 0;
}

extern "C" char* findJavaTZ_md(const char*, const char*)
{
  return 0;
}
