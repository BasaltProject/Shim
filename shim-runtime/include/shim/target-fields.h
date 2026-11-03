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
#define TARGET_THREAD_EXCEPTIONSTACKADJUSTMENT 2296
#define TARGET_THREAD_EXCEPTIONOFFSET 2304
#define TARGET_THREAD_EXCEPTIONHANDLER 2312

#define TARGET_THREAD_IP 2256
#define TARGET_THREAD_STACK 2264
#define TARGET_THREAD_NEWSTACK 2272
#define TARGET_THREAD_SCRATCH 2280
#define TARGET_THREAD_CONTINUATION 2288
#define TARGET_THREAD_TAILADDRESS 2320
#define TARGET_THREAD_VIRTUALCALLTARGET 2328
#define TARGET_THREAD_VIRTUALCALLINDEX 2336
#define TARGET_THREAD_HEAPIMAGE 2344
#define TARGET_THREAD_CODEIMAGE 2352
#define TARGET_THREAD_THUNKTABLE 2360
#define TARGET_THREAD_DYNAMICTABLE 2368
#define TARGET_THREAD_STACKLIMIT 2416

#elif(TARGET_BYTES_PER_WORD == 4)

#define TARGET_THREAD_JAVATHREAD 40
#define TARGET_THREAD_POLLREQUEST 44
#define TARGET_THREAD_EXCEPTION 48
#define TARGET_THREAD_EXCEPTIONSTACKADJUSTMENT 2180
#define TARGET_THREAD_EXCEPTIONOFFSET 2184
#define TARGET_THREAD_EXCEPTIONHANDLER 2188

#define TARGET_THREAD_IP 2160
#define TARGET_THREAD_STACK 2164
#define TARGET_THREAD_NEWSTACK 2168
#define TARGET_THREAD_SCRATCH 2172
#define TARGET_THREAD_CONTINUATION 2176
#define TARGET_THREAD_TAILADDRESS 2192
#define TARGET_THREAD_VIRTUALCALLTARGET 2196
#define TARGET_THREAD_VIRTUALCALLINDEX 2200
#define TARGET_THREAD_HEAPIMAGE 2204
#define TARGET_THREAD_CODEIMAGE 2208
#define TARGET_THREAD_THUNKTABLE 2212
#define TARGET_THREAD_DYNAMICTABLE 2216
#define TARGET_THREAD_STACKLIMIT 2240

#else
#error
#endif
#else
#error
#endif

#endif
