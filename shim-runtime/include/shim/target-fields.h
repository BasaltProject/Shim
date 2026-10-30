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

#define TARGET_THREAD_EXCEPTION 80
#define TARGET_THREAD_EXCEPTIONSTACKADJUSTMENT 2288
#define TARGET_THREAD_EXCEPTIONOFFSET 2296
#define TARGET_THREAD_EXCEPTIONHANDLER 2304

#define TARGET_THREAD_IP 2248
#define TARGET_THREAD_STACK 2256
#define TARGET_THREAD_NEWSTACK 2264
#define TARGET_THREAD_SCRATCH 2264
#define TARGET_THREAD_CONTINUATION 2272
#define TARGET_THREAD_TAILADDRESS 2312
#define TARGET_THREAD_VIRTUALCALLTARGET 2320
#define TARGET_THREAD_VIRTUALCALLINDEX 2328
#define TARGET_THREAD_HEAPIMAGE 2336
#define TARGET_THREAD_CODEIMAGE 2344
#define TARGET_THREAD_THUNKTABLE 2352
#define TARGET_THREAD_DYNAMICTABLE 2360
#define TARGET_THREAD_STACKLIMIT 2408

#elif(TARGET_BYTES_PER_WORD == 4)

#define TARGET_THREAD_EXCEPTION 44
#define TARGET_THREAD_EXCEPTIONSTACKADJUSTMENT 2176
#define TARGET_THREAD_EXCEPTIONOFFSET 2180
#define TARGET_THREAD_EXCEPTIONHANDLER 2184

#define TARGET_THREAD_IP 2156
#define TARGET_THREAD_STACK 2160
#define TARGET_THREAD_NEWSTACK 2164
#define TARGET_THREAD_SCRATCH 2168
#define TARGET_THREAD_CONTINUATION 2172
#define TARGET_THREAD_TAILADDRESS 2188
#define TARGET_THREAD_VIRTUALCALLTARGET 2192
#define TARGET_THREAD_VIRTUALCALLINDEX 2196
#define TARGET_THREAD_HEAPIMAGE 2200
#define TARGET_THREAD_CODEIMAGE 2204
#define TARGET_THREAD_THUNKTABLE 2208
#define TARGET_THREAD_DYNAMICTABLE 2212
#define TARGET_THREAD_STACKLIMIT 2236

#else
#error
#endif
#else
#error
#endif

#endif
