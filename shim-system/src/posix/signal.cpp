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

#include <stdio.h>
#include <unistd.h>
#ifdef __GLIBC__
#include <execinfo.h>
#endif
#include "signal.h"
#include "sys/types.h"
#ifdef __APPLE__
#include "CoreFoundation/CoreFoundation.h"
#include "sys/ucontext.h"
#undef assert
#else
#if defined __FreeBSD__
#include "limits.h"
#endif
#include "ucontext.h"
#endif

#include "shim/arch.h"
#include <shim/system/system.h>
#include <shim/system/signal.h>

namespace shim {
namespace system {

namespace posix {

const int InvalidSignal = -1;
const int SegFaultSignal = SIGSEGV;
const unsigned SegFaultSignalIndex = 0;
#ifdef __APPLE__
const int AltSegFaultSignal = SIGBUS;
#else
const int AltSegFaultSignal = InvalidSignal;
#endif
const unsigned AltSegFaultSignalIndex = 1;
const int DivideByZeroSignal = SIGFPE;
const unsigned DivideByZeroSignalIndex = 2;

const int signals[] = {SegFaultSignal, AltSegFaultSignal, DivideByZeroSignal};

const unsigned SignalCount = 3;
}

struct SignalRegistrar::Data {
  Handler* handlers[posix::SignalCount];
  struct sigaction oldHandlers[posix::SignalCount];

  bool registerHandler(Handler* handler, int index);

  Data()
  {
    if (instance) {
      crash();
    }

    instance = this;
  }

  ~Data()
  {
    instance = 0;
  }

  static SignalRegistrar::Data* instance;
};

SignalRegistrar::Data* SignalRegistrar::Data::instance = 0;

namespace posix {

using namespace vm;

thread_local int signalDepth = 0;

void handleSignal(int signal, siginfo_t* info, void* context)
{
  ucontext_t* c = static_cast<ucontext_t*>(context);

  if (signalDepth++ > 0) {
    static const char message[] = "fatal signal inside the signal handler\n";
    ssize_t ignored = write(2, message, sizeof message - 1);
    (void) ignored;
#ifdef __GLIBC__
    void* frames[48];
    int count = backtrace(frames, 48);
    backtrace_symbols_fd(frames, count, 2);
#endif
    _exit(139);
  }

  void* ip = reinterpret_cast<void*>(IP_REGISTER(c));
  void* stack = reinterpret_cast<void*>(STACK_REGISTER(c));
  void* thread = reinterpret_cast<void*>(THREAD_REGISTER(c));
#ifdef FRAME_REGISTER
  void* frame = reinterpret_cast<void*>(FRAME_REGISTER(c));
#else
  void* frame = 0;
#endif

  unsigned index;

  switch (signal) {
  case SegFaultSignal:
  case AltSegFaultSignal:
  case DivideByZeroSignal: {
    switch (signal) {
    case SegFaultSignal:
      index = SegFaultSignalIndex;
      break;

    case AltSegFaultSignal:
      index = AltSegFaultSignalIndex;
      break;

    case DivideByZeroSignal:
      index = DivideByZeroSignalIndex;
      break;

    default:
      crash();
    }

    bool jump = SignalRegistrar::Data::instance->handlers[index]->handleSignal(
        &ip, &frame, &stack, &thread);

    --signalDepth;

    if (jump) {
      // I'd like to use setcontext here (and get rid of the
      // sigprocmask call), but it doesn't work on my Linux x86_64
      // system, and I can't tell from the documentation if it's even
      // supposed to work.

      sigset_t set;
      sigemptyset(&set);
      sigaddset(&set, signal);
      pthread_sigmask(SIG_UNBLOCK, &set, 0);

      vmJump(ip, frame, stack, thread, 0, 0);
    } else {
      fprintf(stderr, "fatal signal %d at address %p\n", signal, info ? info->si_addr : 0);
      crash();
    }
  } break;

  default:
    crash();
  }
}

}  // namespace posix

SignalRegistrar::SignalRegistrar()
{
  data = new (malloc(sizeof(Data))) Data();
}

SignalRegistrar::~SignalRegistrar()
{
  data->~Data();
  free(data);
}

bool SignalRegistrar::Data::registerHandler(Handler* handler, int index)
{
  if (handler) {
    handlers[index] = handler;

    struct sigaction sa;
    memset(&sa, 0, sizeof(struct sigaction));
    sigemptyset(&(sa.sa_mask));
    sa.sa_flags = SA_SIGINFO | SA_NODEFER;
    sa.sa_sigaction = posix::handleSignal;

    return sigaction(posix::signals[index], &sa, oldHandlers + index) == 0;
  } else if (handlers[index]) {
    handlers[index] = 0;
    return sigaction(posix::signals[index], oldHandlers + index, 0) == 0;
  } else {
    return false;
  }
}

bool SignalRegistrar::registerHandler(Signal signal, Handler* handler)
{
  switch (signal) {
  case SegFault:
    if (!data->registerHandler(handler, posix::SegFaultSignalIndex)) {
      return false;
    }
    if (posix::AltSegFaultSignal != posix::InvalidSignal) {
      return data->registerHandler(handler, posix::AltSegFaultSignalIndex);
    } else {
      return true;
    }
  case DivideByZero:
    return data->registerHandler(handler, posix::DivideByZeroSignalIndex);
  default:
    crash();
  }
}

bool SignalRegistrar::unregisterHandler(Signal signal)
{
  switch (signal) {
  case SegFault:
    if (!data->registerHandler(0, posix::SegFaultSignalIndex)) {
      return false;
    }
    if (posix::AltSegFaultSignal != posix::InvalidSignal) {
      return data->registerHandler(0, posix::AltSegFaultSignalIndex);
    } else {
      return true;
    }
  case DivideByZero:
    return data->registerHandler(0, posix::DivideByZeroSignalIndex);
  default:
    crash();
  }
}

void SignalRegistrar::setCrashDumpDirectory(const char*)
{
  // Do nothing, not currently supported on posix
}

}  // namespace system
}  // namespace shim
