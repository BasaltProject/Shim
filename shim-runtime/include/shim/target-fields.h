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

#ifndef SHIM_TARGET_FIELDS_H
#define SHIM_TARGET_FIELDS_H

#ifdef TARGET_BYTES_PER_WORD
#if (TARGET_BYTES_PER_WORD == 8)

#define TARGET_THREAD_JAVATHREAD 72
#define TARGET_THREAD_POLLREQUEST 80
#define TARGET_THREAD_EXCEPTION 88
#define TARGET_THREAD_EXCEPTIONSTACKADJUSTMENT 3256
#define TARGET_THREAD_EXCEPTIONOFFSET 3264
#define TARGET_THREAD_EXCEPTIONHANDLER 3272

#define TARGET_THREAD_IP 3216
#define TARGET_THREAD_STACK 3224
#define TARGET_THREAD_NEWSTACK 3232
#define TARGET_THREAD_SCRATCH 3240
#define TARGET_THREAD_CONTINUATION 3248
#define TARGET_THREAD_TAILADDRESS 3280
#define TARGET_THREAD_VIRTUALCALLTARGET 3288
#define TARGET_THREAD_VIRTUALCALLINDEX 3296
#define TARGET_THREAD_HEAPIMAGE 3304
#define TARGET_THREAD_CODEIMAGE 3312
#define TARGET_THREAD_THUNKTABLE 3320
#define TARGET_THREAD_DYNAMICTABLE 3328
#define TARGET_THREAD_STACKLIMIT 3376

#elif(TARGET_BYTES_PER_WORD == 4)

#define TARGET_THREAD_JAVATHREAD 40
#define TARGET_THREAD_POLLREQUEST 44
#define TARGET_THREAD_EXCEPTION 48
#define TARGET_THREAD_EXCEPTIONSTACKADJUSTMENT 3140
#define TARGET_THREAD_EXCEPTIONOFFSET 3144
#define TARGET_THREAD_EXCEPTIONHANDLER 3148

#define TARGET_THREAD_IP 3120
#define TARGET_THREAD_STACK 3124
#define TARGET_THREAD_NEWSTACK 3128
#define TARGET_THREAD_SCRATCH 3132
#define TARGET_THREAD_CONTINUATION 3136
#define TARGET_THREAD_TAILADDRESS 3152
#define TARGET_THREAD_VIRTUALCALLTARGET 3156
#define TARGET_THREAD_VIRTUALCALLINDEX 3160
#define TARGET_THREAD_HEAPIMAGE 3164
#define TARGET_THREAD_CODEIMAGE 3168
#define TARGET_THREAD_THUNKTABLE 3172
#define TARGET_THREAD_DYNAMICTABLE 3176
#define TARGET_THREAD_STACKLIMIT 3200

#else
#error
#endif
#else
#error
#endif

#endif
