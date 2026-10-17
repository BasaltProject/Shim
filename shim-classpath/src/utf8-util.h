/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

#ifndef SHIM_UTF8_UTIL_H
#define SHIM_UTF8_UTIL_H

#include "jni.h"
#include <stdlib.h>
#include <string.h>

namespace shimutf8 {

struct Utf8 {
  char* chars;

  Utf8(JNIEnv* e, jstring s) : chars(0) {
    if (s == 0) {
      return;
    }
    const jchar* u = e->GetStringChars(s, 0);
    if (u == 0) {
      return;
    }
    jsize n = e->GetStringLength(s);
    chars = static_cast<char*>(malloc(static_cast<size_t>(n) * 3 + 1));
    if (chars != 0) {
      char* o = chars;
      for (jsize i = 0; i < n; i++) {
        unsigned c = u[i];
        if (c >= 0xD800 && c <= 0xDBFF && i + 1 < n && u[i + 1] >= 0xDC00 && u[i + 1] <= 0xDFFF) {
          unsigned cp = 0x10000 + ((c - 0xD800) << 10) + (u[i + 1] - 0xDC00);
          i++;
          *o++ = static_cast<char>(0xF0 | (cp >> 18));
          *o++ = static_cast<char>(0x80 | ((cp >> 12) & 0x3F));
          *o++ = static_cast<char>(0x80 | ((cp >> 6) & 0x3F));
          *o++ = static_cast<char>(0x80 | (cp & 0x3F));
        } else if (c < 0x80) {
          *o++ = static_cast<char>(c);
        } else if (c < 0x800) {
          *o++ = static_cast<char>(0xC0 | (c >> 6));
          *o++ = static_cast<char>(0x80 | (c & 0x3F));
        } else {
          *o++ = static_cast<char>(0xE0 | (c >> 12));
          *o++ = static_cast<char>(0x80 | ((c >> 6) & 0x3F));
          *o++ = static_cast<char>(0x80 | (c & 0x3F));
        }
      }
      *o = 0;
    }
    e->ReleaseStringChars(s, u);
  }

  ~Utf8() { free(chars); }

 private:
  Utf8(const Utf8&);
  Utf8& operator=(const Utf8&);
};

inline jstring fromUtf8(JNIEnv* e, const char* s)
{
  size_t n = strlen(s);
  jchar* out = static_cast<jchar*>(malloc((n + 1) * sizeof(jchar)));
  if (out == 0) {
    return 0;
  }
  size_t k = 0;
  for (size_t i = 0; i < n;) {
    unsigned char c = static_cast<unsigned char>(s[i]);
    unsigned cp;
    int extra;
    if (c < 0x80) {
      cp = c;
      extra = 0;
    } else if ((c & 0xE0) == 0xC0) {
      cp = c & 0x1F;
      extra = 1;
    } else if ((c & 0xF0) == 0xE0) {
      cp = c & 0x0F;
      extra = 2;
    } else if ((c & 0xF8) == 0xF0) {
      cp = c & 0x07;
      extra = 3;
    } else {
      out[k++] = 0xFFFD;
      i++;
      continue;
    }
    if (extra > 0 && i + extra >= n) {
      out[k++] = 0xFFFD;
      i++;
      continue;
    }
    bool ok = true;
    for (int j = 1; j <= extra; j++) {
      unsigned char d = static_cast<unsigned char>(s[i + j]);
      if ((d & 0xC0) != 0x80) {
        ok = false;
        break;
      }
      cp = (cp << 6) | (d & 0x3F);
    }
    if (!ok) {
      out[k++] = 0xFFFD;
      i++;
      continue;
    }
    i += extra + 1;
    if (cp >= 0x10000) {
      cp -= 0x10000;
      out[k++] = static_cast<jchar>(0xD800 + (cp >> 10));
      out[k++] = static_cast<jchar>(0xDC00 + (cp & 0x3FF));
    } else {
      out[k++] = static_cast<jchar>(cp);
    }
  }
  jstring result = e->NewString(out, static_cast<jsize>(k));
  free(out);
  return result;
}


}  // namespace shimutf8

#endif
