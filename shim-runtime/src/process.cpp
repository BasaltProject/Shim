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

#include "shim/process.h"

#include <shim/util/runtime-array.h>

using namespace vm;

namespace {

unsigned mangledSize(int8_t c)
{
  switch (c) {
  case '_':
  case ';':
  case '[':
    return 2;

  case '$':
    return 6;

  default:
    return 1;
  }
}

unsigned mangle(int8_t c, char* dst)
{
  switch (c) {
  case '/':
    dst[0] = '_';
    return 1;

  case '_':
    dst[0] = '_';
    dst[1] = '1';
    return 2;

  case ';':
    dst[0] = '_';
    dst[1] = '2';
    return 2;

  case '[':
    dst[0] = '_';
    dst[1] = '3';
    return 2;

  case '$':
    memcpy(dst, "_00024", 6);
    return 6;

  default:
    dst[0] = c;
    return 1;
  }
}

__thread unsigned tlsStripPrefix = 0;
char* nativePrefixes = 0;
unsigned nativePrefixesLength = 0;

unsigned jniNameLength(Thread* t UNUSED, GcMethod* method, bool decorate)
{
  unsigned size = 0;

  GcByteArray* className = method->class_()->name();
  for (unsigned i = 0; i < className->length() - 1; ++i) {
    size += mangledSize(className->body()[i]);
  }

  ++size;

  GcByteArray* methodName = method->name();
  for (unsigned i = tlsStripPrefix; i < methodName->length() - 1; ++i) {
    size += mangledSize(methodName->body()[i]);
  }

  if (decorate) {
    size += 2;

    GcByteArray* methodSpec = method->spec();
    for (unsigned i = 1;
         i < methodSpec->length() - 1 and methodSpec->body()[i] != ')';
         ++i) {
      size += mangledSize(methodSpec->body()[i]);
    }
  }

  return size;
}

void makeJNIName(Thread* t UNUSED,
                 const char* prefix,
                 unsigned prefixLength,
                 char* name,
                 GcMethod* method,
                 bool decorate)
{
  memcpy(name, prefix, prefixLength);
  name += prefixLength;

  GcByteArray* className = method->class_()->name();
  for (unsigned i = 0; i < className->length() - 1; ++i) {
    name += mangle(className->body()[i], name);
  }

  *(name++) = '_';

  GcByteArray* methodName = method->name();
  for (unsigned i = tlsStripPrefix; i < methodName->length() - 1; ++i) {
    name += mangle(methodName->body()[i], name);
  }

  if (decorate) {
    *(name++) = '_';
    *(name++) = '_';

    GcByteArray* methodSpec = method->spec();
    for (unsigned i = 1;
         i < methodSpec->length() - 1 and methodSpec->body()[i] != ')';
         ++i) {
      name += mangle(methodSpec->body()[i], name);
    }
  }

  *(name++) = 0;
}

void* resolveNativeMethod(Thread* t,
                          const char* undecorated,
                          const char* decorated)
{
  for (System::Library* lib = t->m->libraries; lib; lib = lib->next()) {
    void* p = lib->resolve(undecorated);
    if (p) {
      return p;
    } else {
      p = lib->resolve(decorated);
      if (p) {
        return p;
      }
    }
  }

  return 0;
}

void* resolveNativeMethod(Thread* t,
                          GcMethod* method,
                          const char* prefix,
                          unsigned prefixLength,
                          int footprint UNUSED)
{
  unsigned undecoratedSize = prefixLength + jniNameLength(t, method, false);
  // extra 6 is for code below:
  THREAD_RUNTIME_ARRAY(t, char, undecorated, undecoratedSize + 1 + 6);
  makeJNIName(t,
              prefix,
              prefixLength,
              RUNTIME_ARRAY_BODY(undecorated) + 1,
              method,
              false);

  unsigned decoratedSize = prefixLength + jniNameLength(t, method, true);
  // extra 6 is for code below:
  THREAD_RUNTIME_ARRAY(t, char, decorated, decoratedSize + 1 + 6);
  makeJNIName(
      t, prefix, prefixLength, RUNTIME_ARRAY_BODY(decorated) + 1, method, true);

  void* p = resolveNativeMethod(t,
                                RUNTIME_ARRAY_BODY(undecorated) + 1,
                                RUNTIME_ARRAY_BODY(decorated) + 1);
  if (p) {
    return p;
  }

#ifdef PLATFORM_WINDOWS
  // on windows, we also try the _%s@%d and %s@%d variants
  if (footprint == -1) {
    footprint = method->parameterFootprint() + 1;
    if (method->flags() & ACC_STATIC) {
      ++footprint;
    }
  }

  *RUNTIME_ARRAY_BODY(undecorated) = '_';
  vm::snprintf(RUNTIME_ARRAY_BODY(undecorated) + undecoratedSize + 1,
               5,
               "@%d",
               footprint * BytesPerWord);

  *RUNTIME_ARRAY_BODY(decorated) = '_';
  vm::snprintf(RUNTIME_ARRAY_BODY(decorated) + decoratedSize + 1,
               5,
               "@%d",
               footprint * BytesPerWord);

  p = resolveNativeMethod(
      t, RUNTIME_ARRAY_BODY(undecorated), RUNTIME_ARRAY_BODY(decorated));
  if (p) {
    return p;
  }

  // one more try without the leading underscore
  p = resolveNativeMethod(t,
                          RUNTIME_ARRAY_BODY(undecorated) + 1,
                          RUNTIME_ARRAY_BODY(decorated) + 1);
  if (p) {
    return p;
  }
#endif

  return 0;
}

bool hasWrapper(Thread* t UNUSED, GcMethod* method, unsigned strip)
{
  GcByteArray* name = method->name();
  GcByteArray* spec = method->spec();
  GcArray* table = cast<GcArray>(t, method->class_()->methodTable());
  if (table == 0) {
    return false;
  }
  for (unsigned i = 0; i < table->length(); ++i) {
    GcMethod* m = cast<GcMethod>(t, table->body()[i]);
    if (m->flags() & ACC_NATIVE) {
      continue;
    }
    GcByteArray* mn = m->name();
    GcByteArray* ms = m->spec();
    if (mn->length() == name->length() - strip && ms->length() == spec->length()
        && memcmp(mn->body().begin(), name->body().begin() + strip, mn->length() - 1) == 0
        && memcmp(ms->body().begin(), spec->body().begin(), spec->length() - 1) == 0) {
      return true;
    }
  }
  return false;
}

GcNative* resolveNativeMethodExact(Thread* t, GcMethod* method)
{
  void* p = resolveNativeMethod(t, method, "Shim_", sizeof("Shim_") - 1, 3);
  if (p) {
    return makeNative(t, p, true);
  }

  p = resolveNativeMethod(t, method, "Java_", 5, -1);
  if (p) {
    return makeNative(t, p, false);
  }

  return 0;
}

GcNative* resolveNativeMethod(Thread* t, GcMethod* method)
{
  GcNative* native = resolveNativeMethodExact(t, method);
  if (native || nativePrefixesLength == 0) {
    return native;
  }
  const char* name = reinterpret_cast<const char*>(method->name()->body().begin());
  unsigned total = method->name()->length() - 1;
  unsigned strip = 0;
  for (bool progressed = true; progressed;) {
    progressed = false;
    unsigned end = nativePrefixesLength;
    while (end > 0) {
      unsigned start = end - 1;
      while (start > 0 && nativePrefixes[start - 1] != '\n') {
        --start;
      }
      unsigned len = end - 1 - start;
      if (len > 0 && strip + len < total && strncmp(name + strip, nativePrefixes + start, len) == 0) {
        strip += len;
        if (!hasWrapper(t, method, strip)) {
          continue;
        }
        tlsStripPrefix = strip;
        native = resolveNativeMethodExact(t, method);
        tlsStripPrefix = 0;
        if (native) {
          return native;
        }
        progressed = true;
        break;
      }
      end = start;
    }
  }
  return 0;
}

}  // namespace

namespace vm {

void setNativeMethodPrefixes(const char* joined, unsigned length)
{
  char* copy = length ? static_cast<char*>(malloc(length)) : 0;
  if (length) {
    memcpy(copy, joined, length);
  }
  char* old = nativePrefixes;
  nativePrefixes = copy;
  nativePrefixesLength = length;
  free(old);
}

namespace {

unsigned appendType(const char* spec, unsigned i, char* out, unsigned* n, unsigned cap)
{
  unsigned dims = 0;
  while (spec[i] == '[') {
    ++dims;
    ++i;
  }
  const char* prim = 0;
  switch (spec[i]) {
  case 'B': prim = "byte"; break;
  case 'C': prim = "char"; break;
  case 'D': prim = "double"; break;
  case 'F': prim = "float"; break;
  case 'I': prim = "int"; break;
  case 'J': prim = "long"; break;
  case 'S': prim = "short"; break;
  case 'Z': prim = "boolean"; break;
  case 'V': prim = "void"; break;
  default: break;
  }
  if (prim) {
    for (; *prim && *n + 1 < cap; ++prim) {
      out[(*n)++] = *prim;
    }
    ++i;
  } else {
    ++i;
    while (spec[i] != ';' && *n + 1 < cap) {
      out[(*n)++] = spec[i] == '/' ? '.' : spec[i];
      ++i;
    }
    ++i;
  }
  for (; dims && *n + 2 < cap; --dims) {
    out[(*n)++] = '[';
    out[(*n)++] = ']';
  }
  return i;
}

void describeMethod(GcMethod* method, char* out, unsigned cap)
{
  const char* spec = reinterpret_cast<const char*>(method->spec()->body().begin());
  unsigned n = 0;
  unsigned i = 1;
  unsigned close = 0;
  while (spec[close] != ')') {
    ++close;
  }
  unsigned r = appendType(spec, close + 1, out, &n, cap);
  (void)r;
  if (n + 1 < cap) {
    out[n++] = ' ';
  }
  const char* cn = reinterpret_cast<const char*>(method->class_()->name()->body().begin());
  for (unsigned k = 0; k < method->class_()->name()->length() - 1 && n + 1 < cap; ++k) {
    out[n++] = cn[k] == '/' ? '.' : cn[k];
  }
  if (n + 1 < cap) {
    out[n++] = '.';
  }
  const char* mn = reinterpret_cast<const char*>(method->name()->body().begin());
  for (unsigned k = 0; k < method->name()->length() - 1 && n + 1 < cap; ++k) {
    out[n++] = mn[k];
  }
  if (n + 1 < cap) {
    out[n++] = '(';
  }
  bool first = true;
  while (i < close) {
    if (!first && n + 2 < cap) {
      out[n++] = ',';
      out[n++] = ' ';
    }
    first = false;
    i = appendType(spec, i, out, &n, cap);
  }
  if (n + 1 < cap) {
    out[n++] = ')';
  }
  out[n] = 0;
}

}  // namespace

void resolveNative(Thread* t, GcMethod* method)
{
  PROTECT(t, method);

  assertT(t, method->flags() & ACC_NATIVE);

  initClass(t, method->class_());

  if (getMethodRuntimeData(t, method)->native() == 0) {
    GcNative* native = resolveNativeMethod(t, method);
    if (UNLIKELY(native == 0)) {
      char description[1024];
      describeMethod(method, description, sizeof description);
      throwNew(t, GcUnsatisfiedLinkError::Type, "'%s'", description);
    }

    PROTECT(t, native);

    GcMethodRuntimeData* runtimeData = getMethodRuntimeData(t, method);

    // ensure other threads only see the methodRuntimeDataNative field
    // populated once the object it points to has been populated:
    storeStoreMemoryBarrier();

    runtimeData->setNative(t, native);
  }
}

int findLineNumber(Thread* t UNUSED, GcMethod* method, unsigned ip)
{
  if (method->flags() & ACC_NATIVE) {
    return NativeLine;
  }

  // our parameter indicates the instruction following the one we care
  // about, so we back up first:
  --ip;

  GcLineNumberTable* lnt = method->code()->lineNumberTable();
  if (lnt) {
    unsigned bottom = 0;
    unsigned top = lnt->length();
    for (unsigned span = top - bottom; span; span = top - bottom) {
      unsigned middle = bottom + (span / 2);
      uint64_t ln = lnt->body()[middle];

      if (ip >= lineNumberIp(ln)
          and (middle + 1 == lnt->length()
               or ip < lineNumberIp(lnt->body()[middle + 1]))) {
        return lineNumberLine(ln);
      } else if (ip < lineNumberIp(ln)) {
        top = middle;
      } else if (ip > lineNumberIp(ln)) {
        bottom = middle + 1;
      }
    }

    if (top < lnt->length()) {
      return lineNumberLine(lnt->body()[top]);
    } else {
      return UnknownLine;
    }
  } else {
    return UnknownLine;
  }
}

}  // namespace vm
