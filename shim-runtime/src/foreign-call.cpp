/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

#include "shim/machine.h"

#include <pthread.h>
#include <stdlib.h>
#include <string.h>

#if !defined(PLATFORM_WINDOWS) && !defined(_WIN32)
#include <dlfcn.h>
#endif

using namespace vm;

namespace {

int64_t slot(uintptr_t* arguments, int index)
{
  int64_t v;
  memcpy(&v, arguments + index, 8);
  return v;
}

}  // namespace

extern "C" SHIM_EXPORT int64_t JNICALL
    Shim_java_lang_foreign_NativeLinkerImpl_dlopen0(Thread*, object, uintptr_t* arguments)
{
#if !defined(PLATFORM_WINDOWS) && !defined(_WIN32)
  return reinterpret_cast<int64_t>(dlopen(reinterpret_cast<const char*>(slot(arguments, 0)), RTLD_NOW | RTLD_LOCAL));
#else
  (void)arguments;
  return 0;
#endif
}

extern "C" SHIM_EXPORT int64_t JNICALL
    Shim_java_lang_foreign_NativeLinkerImpl_dlsym0(Thread*, object, uintptr_t* arguments)
{
#if !defined(PLATFORM_WINDOWS) && !defined(_WIN32)
  void* handle = reinterpret_cast<void*>(slot(arguments, 0));
  const char* name = reinterpret_cast<const char*>(slot(arguments, 2));
  return reinterpret_cast<int64_t>(dlsym(handle ? handle : RTLD_DEFAULT, name));
#else
  (void)arguments;
  return 0;
#endif
}

#if defined(__x86_64__) && defined(__linux__)
#include <sys/mman.h>

namespace {

Machine* gMachine;
thread_local Thread* gThread;

}  // namespace

extern "C" SHIM_EXPORT void shimUpcallC(int64_t* frame)
{
  Thread* t = gThread;
  if (t == 0 && gMachine != 0) {
    t = static_cast<Thread*>(gMachine->localThread->get());
    if (t == 0) {
      t = attachThread(gMachine, true);
    }
  }
  if (t == 0) {
    abort();
  }
  GcMethod* method = resolveMethod(
      t, roots(t)->bootLoader(), "java/lang/foreign/NativeLinkerImpl", "upcall", "(JJ)V");
  t->m->processor->invoke(t, method, 0, frame[19], static_cast<int64_t>(reinterpret_cast<intptr_t>(frame)));
}

extern "C" void shimUpcallEntry();

__asm__(
    ".text\n"
    ".globl shimUpcallEntry\n"
    ".type shimUpcallEntry,@function\n"
    "shimUpcallEntry:\n"
    "pushq %rbp\n"
    "movq %rsp, %rbp\n"
    "subq $160, %rsp\n"
    "movq %rdi, 0(%rsp)\n"
    "movq %rsi, 8(%rsp)\n"
    "movq %rdx, 16(%rsp)\n"
    "movq %rcx, 24(%rsp)\n"
    "movq %r8, 32(%rsp)\n"
    "movq %r9, 40(%rsp)\n"
    "movsd %xmm0, 48(%rsp)\n"
    "movsd %xmm1, 56(%rsp)\n"
    "movsd %xmm2, 64(%rsp)\n"
    "movsd %xmm3, 72(%rsp)\n"
    "movsd %xmm4, 80(%rsp)\n"
    "movsd %xmm5, 88(%rsp)\n"
    "movsd %xmm6, 96(%rsp)\n"
    "movsd %xmm7, 104(%rsp)\n"
    "leaq 16(%rbp), %rax\n"
    "movq %rax, 144(%rsp)\n"
    "movq %r10, 152(%rsp)\n"
    "movq %rsp, %rdi\n"
    "call shimUpcallC@PLT\n"
    "movq 112(%rsp), %rax\n"
    "movq 120(%rsp), %rdx\n"
    "movsd 128(%rsp), %xmm0\n"
    "movsd 136(%rsp), %xmm1\n"
    "leave\n"
    "ret\n"
    ".size shimUpcallEntry, .-shimUpcallEntry\n");

extern "C" SHIM_EXPORT int64_t JNICALL
    Shim_java_lang_foreign_NativeLinkerImpl_entry0(Thread*, object, uintptr_t*)
{
  return reinterpret_cast<int64_t>(&shimUpcallEntry);
}

extern "C" SHIM_EXPORT int64_t JNICALL
    Shim_java_lang_foreign_NativeLinkerImpl_stubAlloc0(Thread*, object, uintptr_t*)
{
  static const size_t kRegion = 1 << 16;
  static const size_t kSlot = 32;
  static char* region = 0;
  static size_t used = kRegion;
  static pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
  pthread_mutex_lock(&lock);
  int64_t result = 0;
  if (used + kSlot > kRegion) {
    void* p = mmap(0, kRegion, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (p != MAP_FAILED) {
      region = static_cast<char*>(p);
      used = 0;
    }
  }
  if (used + kSlot <= kRegion) {
    result = reinterpret_cast<int64_t>(region + used);
    used += kSlot;
  }
  pthread_mutex_unlock(&lock);
  return result;
}

#else

namespace {

[[maybe_unused]] Machine* gMachine;
[[maybe_unused]] Thread* gThread;

}  // namespace

extern "C" SHIM_EXPORT int64_t JNICALL
    Shim_java_lang_foreign_NativeLinkerImpl_entry0(Thread*, object, uintptr_t*)
{
  return 0;
}

extern "C" SHIM_EXPORT int64_t JNICALL
    Shim_java_lang_foreign_NativeLinkerImpl_stubAlloc0(Thread*, object, uintptr_t*)
{
  return 0;
}

#endif

extern "C" SHIM_EXPORT int64_t JNICALL
    Shim_java_lang_foreign_NativeLinkerImpl_supported0(Thread*, object, uintptr_t*)
{
#if defined(__x86_64__) && !defined(__APPLE__) && !defined(_WIN32)
  return 6;
#elif defined(__aarch64__) && !defined(__APPLE__) && !defined(_WIN32)
  return 8;
#else
  return 0;
#endif
}

extern "C" SHIM_EXPORT void JNICALL
    Shim_java_lang_foreign_NativeLinkerImpl_call0(Thread* t, object, uintptr_t* arguments)
{
#if (defined(__x86_64__) || defined(__aarch64__)) && !defined(__APPLE__) && !defined(_WIN32)
  void* fn = reinterpret_cast<void*>(slot(arguments, 0));
  int64_t* buf = reinterpret_cast<int64_t*>(slot(arguments, 2));
  int64_t kind = slot(arguments, 4);
  Thread* previousThread = gThread;
  gThread = t;
  gMachine = t->m;
  int64_t a[8];
  double d[8];
  int64_t s[32];
  memcpy(a, buf, sizeof a);
  memcpy(d, buf + 8, sizeof d);
  memcpy(s, buf + 24, sizeof s);
#define STACK                                                                                                       \
  s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10], s[11], s[12], s[13], s[14], s[15], s[16], s[17], \
      s[18], s[19], s[20], s[21], s[22], s[23], s[24], s[25], s[26], s[27], s[28], s[29], s[30], s[31]
#if defined(__x86_64__)
#define REGS a[0], a[1], a[2], a[3], a[4], a[5], d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], STACK
  typedef int64_t (*FnI)(int64_t, int64_t, int64_t, int64_t, int64_t, int64_t, ...);
  typedef double (*FnD)(int64_t, int64_t, int64_t, int64_t, int64_t, int64_t, ...);
  struct II {
    int64_t a, b;
  };
  struct DD {
    double a, b;
  };
  struct ID {
    int64_t a;
    double b;
  };
  struct DI {
    double a;
    int64_t b;
  };
  switch (kind) {
  case 0:
    buf[16] = reinterpret_cast<FnI>(fn)(REGS);
    break;
  case 1: {
    double r = reinterpret_cast<FnD>(fn)(REGS);
    memcpy(buf + 18, &r, 8);
  } break;
  case 2: {
    II r = reinterpret_cast<II (*)(int64_t, int64_t, int64_t, int64_t, int64_t, int64_t, ...)>(fn)(REGS);
    buf[16] = r.a;
    buf[17] = r.b;
  } break;
  case 3: {
    DD r = reinterpret_cast<DD (*)(int64_t, int64_t, int64_t, int64_t, int64_t, int64_t, ...)>(fn)(REGS);
    memcpy(buf + 18, &r.a, 8);
    memcpy(buf + 19, &r.b, 8);
  } break;
  case 4: {
    ID r = reinterpret_cast<ID (*)(int64_t, int64_t, int64_t, int64_t, int64_t, int64_t, ...)>(fn)(REGS);
    buf[16] = r.a;
    memcpy(buf + 18, &r.b, 8);
  } break;
  default: {
    DI r = reinterpret_cast<DI (*)(int64_t, int64_t, int64_t, int64_t, int64_t, int64_t, ...)>(fn)(REGS);
    memcpy(buf + 18, &r.a, 8);
    buf[16] = r.b;
  } break;
  }
#else
#define REGS a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], STACK
#define I64_8 int64_t, int64_t, int64_t, int64_t, int64_t, int64_t, int64_t, int64_t
#define F64_8 double, double, double, double, double, double, double, double
  typedef int64_t (*FnI)(I64_8, F64_8, I64_8, I64_8, I64_8, I64_8);
  typedef double (*FnD)(I64_8, F64_8, I64_8, I64_8, I64_8, I64_8);
  if (kind == 0) {
    buf[16] = reinterpret_cast<FnI>(fn)(REGS);
  } else {
    double r = reinterpret_cast<FnD>(fn)(REGS);
    memcpy(buf + 18, &r, 8);
  }
#endif
  gThread = previousThread;
#else
  (void)arguments;
  (void)t;
#endif
}
