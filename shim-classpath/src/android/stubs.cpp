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

struct JavaVM;

extern "C" int JNI_OnLoad(JavaVM*, void*)
{
  return 0;
}

struct _JNIEnv;

struct JniConstants {
  static void init(_JNIEnv* env);
};

void JniConstants::init(_JNIEnv*)
{
  // ignore
}

struct _JavaVM;

int libconscrypt_JNI_OnLoad(_JavaVM*, void*)
{
  return 0;
}
