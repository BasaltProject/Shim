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

#ifndef JNIENV_H
#define JNIENV_H

#include "shim/machine.h"

#define BOOTSTRAP_PROPERTY "shim.bootstrap"
#define JAVA_COMMAND_PROPERTY "sun.java.command"
#define JAVA_LAUNCHER_PROPERTY "sun.java.launcher"
#define CRASHDIR_PROPERTY "shim.crash.dir"
#define EMBED_PREFIX_PROPERTY "shim.embed.prefix"
#define CLASSPATH_PROPERTY "java.class.path"
#define JAVA_HOME_PROPERTY "java.home"
#define REENTRANT_PROPERTY "shim.reentrant"
#define BOOTCLASSPATH_PREPEND_OPTION "bootclasspath/p"
#define BOOTCLASSPATH_OPTION "bootclasspath"
#define BOOTCLASSPATH_APPEND_OPTION "bootclasspath/a"

namespace vm {

void populateJNITables(JavaVMVTable* vmTable, JNIEnvVTable* envTable);

}  // namespace vm

#endif  // JNIENV_H
