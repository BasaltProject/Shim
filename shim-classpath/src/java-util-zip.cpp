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

#include "stdlib.h"
#include "string.h"
#include "shim/zlib-custom.h"

#include "jni.h"
#include "jni-util.h"

extern "C" JNIEXPORT jlong JNICALL
    Java_java_util_zip_Inflater_make(JNIEnv* e, jclass, jboolean nowrap)
{
  z_stream* s = static_cast<z_stream*>(malloc(sizeof(z_stream)));
  if (s == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return 0;
  }

  memset(s, 0, sizeof(z_stream));

  int r = inflateInit2(s, (nowrap ? -15 : 15));
  if (r != Z_OK) {
    free(s);
    throwNew(e, "java/lang/RuntimeException", zError(r));
    return 0;
  }

  return reinterpret_cast<jlong>(s);
}

extern "C" JNIEXPORT void JNICALL
    Java_java_util_zip_Inflater_dispose(JNIEnv*, jclass, jlong peer)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);
  inflateEnd(s);
  free(s);
}

extern "C" JNIEXPORT void JNICALL
    Java_java_util_zip_Inflater_inflate(JNIEnv* e,
                                        jclass,
                                        jlong peer,
                                        jbyteArray input,
                                        jint inputOffset,
                                        jint inputLength,
                                        jbyteArray output,
                                        jint outputOffset,
                                        jint outputLength,
                                        jintArray results)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);

  jbyte* in = static_cast<jbyte*>(malloc(inputLength));
  if (in == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return;
  }

  jbyte* out = static_cast<jbyte*>(malloc(outputLength));
  if (out == 0) {
    free(in);
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return;
  }

  e->GetByteArrayRegion(input, inputOffset, inputLength, in);

  s->next_in = reinterpret_cast<Bytef*>(in);
  s->avail_in = inputLength;
  s->next_out = reinterpret_cast<Bytef*>(out);
  s->avail_out = outputLength;

  int r = inflate(s, Z_SYNC_FLUSH);
  jint resultArray[3] = {r,
                         static_cast<jint>(inputLength - s->avail_in),
                         static_cast<jint>(outputLength - s->avail_out)};

  free(in);

  e->SetByteArrayRegion(output, outputOffset, resultArray[2], out);
  free(out);

  e->SetIntArrayRegion(results, 0, 3, resultArray);
}

extern "C" JNIEXPORT jboolean JNICALL
    Java_java_util_zip_Inflater_setDictionary(JNIEnv* e,
                                              jclass,
                                              jlong peer,
                                              jbyteArray dictionary,
                                              jint offset,
                                              jint length)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);
  jbyte* dict = static_cast<jbyte*>(malloc(length > 0 ? length : 1));
  if (dict == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return JNI_FALSE;
  }
  e->GetByteArrayRegion(dictionary, offset, length, dict);
  int r = inflateSetDictionary(s, reinterpret_cast<Bytef*>(dict), length);
  free(dict);
  return r == Z_OK;
}

extern "C" JNIEXPORT jint JNICALL
    Java_java_util_zip_Inflater_getAdler(JNIEnv*, jclass, jlong peer)
{
  return static_cast<jint>(reinterpret_cast<z_stream*>(peer)->adler);
}

extern "C" JNIEXPORT jstring JNICALL
    Java_java_util_zip_Inflater_message(JNIEnv* e, jclass, jlong peer)
{
  const char* m = reinterpret_cast<z_stream*>(peer)->msg;
  return m ? e->NewStringUTF(m) : 0;
}

extern "C" JNIEXPORT jlong JNICALL
    Java_java_util_zip_Deflater_make(JNIEnv* e,
                                     jclass,
                                     jboolean nowrap,
                                     jint level,
                                     jint strategy)
{
  z_stream* s = static_cast<z_stream*>(malloc(sizeof(z_stream)));
  if (s == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return 0;
  }

  memset(s, 0, sizeof(z_stream));

  int r = deflateInit2_(s,
                        level,
                        Z_DEFLATED,
                        (nowrap ? -15 : 15),
                        8,
                        strategy,
                        ZLIB_VERSION,
                        static_cast<int>(sizeof(z_stream)));
  if (r != Z_OK) {
    free(s);
    throwNew(e, "java/lang/RuntimeException", zError(r));
    return 0;
  }

  return reinterpret_cast<jlong>(s);
}

extern "C" JNIEXPORT void JNICALL
    Java_java_util_zip_Deflater_dispose(JNIEnv*, jclass, jlong peer)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);
  deflateEnd(s);
  free(s);
}

extern "C" JNIEXPORT void JNICALL
    Java_java_util_zip_Deflater_deflate(JNIEnv* e,
                                        jclass,
                                        jlong peer,
                                        jbyteArray input,
                                        jint inputOffset,
                                        jint inputLength,
                                        jbyteArray output,
                                        jint outputOffset,
                                        jint outputLength,
                                        jint flush,
                                        jboolean setParams,
                                        jint level,
                                        jint strategy,
                                        jintArray results)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);

  jbyte* in = static_cast<jbyte*>(malloc(inputLength));
  if (in == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return;
  }

  jbyte* out = static_cast<jbyte*>(malloc(outputLength));
  if (out == 0) {
    free(in);
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return;
  }

  e->GetByteArrayRegion(input, inputOffset, inputLength, in);

  s->next_in = reinterpret_cast<Bytef*>(in);
  s->avail_in = inputLength;
  s->next_out = reinterpret_cast<Bytef*>(out);
  s->avail_out = outputLength;

  jint paramsApplied = 0;
  int r = Z_OK;
  if (setParams) {
    r = deflateParams(s, level, strategy);
    if (r == Z_OK) {
      paramsApplied = 1;
    } else if (r == Z_BUF_ERROR) {
      r = Z_OK;
    }
  }
  if (r == Z_OK && (paramsApplied || !setParams)) {
    r = deflate(s, flush);
    if (r == Z_BUF_ERROR) {
      r = Z_OK;
    }
  }
  jint resultArray[4] = {r,
                         static_cast<jint>(inputLength - s->avail_in),
                         static_cast<jint>(outputLength - s->avail_out),
                         paramsApplied};

  free(in);

  e->SetByteArrayRegion(output, outputOffset, resultArray[2], out);
  free(out);

  e->SetIntArrayRegion(results, 0, 4, resultArray);
}

extern "C" JNIEXPORT jboolean JNICALL
    Java_java_util_zip_Deflater_setDictionary(JNIEnv* e,
                                              jclass,
                                              jlong peer,
                                              jbyteArray dictionary,
                                              jint offset,
                                              jint length)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);
  jbyte* dict = static_cast<jbyte*>(malloc(length > 0 ? length : 1));
  if (dict == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return JNI_FALSE;
  }
  e->GetByteArrayRegion(dictionary, offset, length, dict);
  int r = deflateSetDictionary(s, reinterpret_cast<Bytef*>(dict), length);
  free(dict);
  return r == Z_OK;
}

extern "C" JNIEXPORT jint JNICALL
    Java_java_util_zip_Deflater_getAdler(JNIEnv*, jclass, jlong peer)
{
  return static_cast<jint>(reinterpret_cast<z_stream*>(peer)->adler);
}
