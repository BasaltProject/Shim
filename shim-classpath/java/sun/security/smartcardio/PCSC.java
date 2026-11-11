/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.smartcardio;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class PCSC {
  static final int SCARD_E_NO_SERVICE = 0x8010001d;
  static final int SCARD_E_NO_READERS_AVAILABLE = 0x8010002e;
  static final int SCARD_E_NO_SMARTCARD = 0x8010000c;
  static final int SCARD_W_REMOVED_CARD = 0x80100069;
  static final int SCARD_E_TIMEOUT = 0x8010000a;
  static final int SCARD_E_NOT_TRANSACTED = 0x80100016;

  static final int SCARD_PROTOCOL_T0 = 1;
  static final int SCARD_PROTOCOL_T1 = 2;
  static final int SCARD_SHARE_SHARED = 2;
  static final int SCARD_SHARE_DIRECT = 3;
  static final int SCARD_LEAVE_CARD = 0;
  static final int SCARD_RESET_CARD = 1;
  static final int SCARD_STATE_IGNORE = 1;
  static final int SCARD_STATE_UNKNOWN = 4;
  static final int SCARD_STATE_UNAVAILABLE = 8;
  static final int SCARD_STATE_EMPTY = 0x10;
  static final int SCARD_STATE_PRESENT = 0x20;
  static final int SCARD_STATE_EXCLUSIVE = 0x80;
  static final int SCARD_STATE_INUSE = 0x100;
  static final int SCARD_STATE_MUTE = 0x200;
  static final int SCARD_STATE_CHANGED = 2;
  static final int SCARD_STATE_UNAWARE = 0;
  static final int INFINITE = 0xffffffff;

  private static final int kStateSize = 80;
  private static final int kRecvSize = 8192 + 128;

  private static final ValueLayout.OfLong C_LONG = ValueLayout.JAVA_LONG;
  private static final ValueLayout.OfInt C_INT = ValueLayout.JAVA_INT;
  private static final java.lang.foreign.AddressLayout C_PTR = ValueLayout.ADDRESS;

  private static MethodHandle establish;
  private static MethodHandle listReaders;
  private static MethodHandle statusChange;
  private static MethodHandle connect;
  private static MethodHandle transmit;
  private static MethodHandle status;
  private static MethodHandle disconnect;
  private static MethodHandle begin;
  private static MethodHandle end;
  private static MethodHandle control;
  private static SymbolLookup lookup;
  private static Throwable initFailure;
  private static boolean initialized;

  private PCSC() {
  }

  static synchronized void checkAvailable() {
    if (!initialized) {
      initialized = true;
      try {
        init();
      } catch (Throwable t) {
        initFailure = t;
      }
    }
    if (initFailure != null) {
      throw new UnsupportedOperationException("PC/SC not available on this platform", initFailure);
    }
  }

  private static void init() {
    String custom = System.getProperty("sun.security.smartcardio.library");
    Arena global = Arena.global();
    SymbolLookup found = null;
    if (custom != null) {
      try {
        found = SymbolLookup.libraryLookup(custom, global);
      } catch (IllegalArgumentException e) {
        found = null;
      }
    }
    if (found == null) {
      String[] names = {"libpcsclite.so.1", "libpcsclite.so", "/usr/lib/x86_64-linux-gnu/libpcsclite.so.1",
          "/usr/lib/aarch64-linux-gnu/libpcsclite.so.1", "/usr/lib64/libpcsclite.so.1", "/usr/lib/libpcsclite.so.1",
          "/lib/x86_64-linux-gnu/libpcsclite.so.1", "/usr/local/lib/libpcsclite.so.1",
          "/System/Library/Frameworks/PCSC.framework/PCSC"};
      for (String n : names) {
        try {
          found = SymbolLookup.libraryLookup(n, global);
          break;
        } catch (IllegalArgumentException e) {
          found = null;
        }
      }
    }
    if (found == null) {
      throw new UnsupportedOperationException("PC/SC library not found");
    }
    lookup = found;
    Linker l = Linker.nativeLinker();
    establish = h(l, "SCardEstablishContext", C_INT, C_INT, C_PTR, C_PTR, C_PTR);
    listReaders = h(l, "SCardListReaders", C_INT, C_LONG, C_PTR, C_PTR, C_PTR);
    statusChange = h(l, "SCardGetStatusChange", C_INT, C_LONG, C_LONG, C_PTR, C_LONG);
    connect = h(l, "SCardConnect", C_INT, C_LONG, C_PTR, C_LONG, C_LONG, C_PTR, C_PTR);
    transmit = h(l, "SCardTransmit", C_INT, C_LONG, C_PTR, C_PTR, C_LONG, C_PTR, C_PTR, C_PTR);
    status = h(l, "SCardStatus", C_INT, C_LONG, C_PTR, C_PTR, C_PTR, C_PTR, C_PTR, C_PTR);
    disconnect = h(l, "SCardDisconnect", C_INT, C_LONG, C_LONG);
    begin = h(l, "SCardBeginTransaction", C_INT, C_LONG);
    end = h(l, "SCardEndTransaction", C_INT, C_LONG, C_LONG);
    control = h(l, "SCardControl", C_INT, C_LONG, C_LONG, C_PTR, C_LONG, C_PTR, C_LONG, C_PTR);
  }

  private static MethodHandle h(Linker l, String name, ValueLayout ret, ValueLayout... args) {
    MemorySegment sym = lookup.find(name).orElseThrow(() -> new UnsatisfiedLinkError(name));
    return l.downcallHandle(sym, FunctionDescriptor.of(ret, args));
  }

  private static void check(int rc) throws PCSCException {
    if (rc != 0) {
      throw new PCSCException(rc);
    }
  }

  private static int call(MethodHandle handle, Object... args) throws PCSCException {
    try {
      return (int) handle.invokeWithArguments(args);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new IllegalStateException(t);
    }
  }

  static long SCardEstablishContext(int scope) throws PCSCException {
    checkAvailable();
    try (Arena a = Arena.ofConfined()) {
      MemorySegment ctx = a.allocate(8);
      check(call(establish, scope, MemorySegment.NULL, MemorySegment.NULL, ctx));
      return ctx.get(C_LONG, 0);
    }
  }

  static String[] SCardListReaders(long ctx) throws PCSCException {
    try (Arena a = Arena.ofConfined()) {
      MemorySegment len = a.allocate(8);
      check(call(listReaders, ctx, MemorySegment.NULL, MemorySegment.NULL, len));
      long n = len.get(C_LONG, 0);
      MemorySegment buf = a.allocate(Math.max(n, 1));
      check(call(listReaders, ctx, MemorySegment.NULL, buf, len));
      n = len.get(C_LONG, 0);
      List<String> names = new ArrayList<String>();
      long start = 0;
      for (long i = 0; i < n; i++) {
        if (buf.get(ValueLayout.JAVA_BYTE, i) == 0) {
          if (i > start) {
            byte[] b = new byte[(int) (i - start)];
            MemorySegment.copy(buf, ValueLayout.JAVA_BYTE, start, b, 0, b.length);
            names.add(new String(b, StandardCharsets.UTF_8));
          }
          start = i + 1;
        }
      }
      return names.toArray(new String[0]);
    }
  }

  static int[] SCardGetStatusChange(long ctx, long timeout, int[] current, String[] readers) throws PCSCException {
    try (Arena a = Arena.ofConfined()) {
      int n = readers.length;
      MemorySegment states = a.allocate((long) kStateSize * Math.max(n, 1));
      for (int i = 0; i < n; i++) {
        long base = (long) i * kStateSize;
        states.set(C_PTR, base, a.allocateFrom(readers[i]));
        states.set(C_LONG, base + 16, current[i] & 0xffffffffL);
      }
      check(call(statusChange, ctx, timeout & 0xffffffffL, states, (long) n));
      int[] out = new int[n];
      for (int i = 0; i < n; i++) {
        out[i] = (int) states.get(C_LONG, (long) i * kStateSize + 24);
      }
      return out;
    }
  }

  static long[] SCardConnect(long ctx, String reader, int share, int protocols) throws PCSCException {
    try (Arena a = Arena.ofConfined()) {
      MemorySegment card = a.allocate(8);
      MemorySegment active = a.allocate(8);
      check(call(connect, ctx, a.allocateFrom(reader), (long) share, (long) protocols, card, active));
      return new long[] {card.get(C_LONG, 0), active.get(C_LONG, 0)};
    }
  }

  static byte[] SCardTransmit(long card, int protocol, byte[] command, int off, int len) throws PCSCException {
    try (Arena a = Arena.ofConfined()) {
      MemorySegment pci = lookup.find(protocol == SCARD_PROTOCOL_T0 ? "g_rgSCardT0Pci"
          : protocol == SCARD_PROTOCOL_T1 ? "g_rgSCardT1Pci" : "g_rgSCardRawPci").orElseThrow(
              () -> new UnsatisfiedLinkError("g_rgSCardPci"));
      MemorySegment send = a.allocate(Math.max(len, 1));
      MemorySegment.copy(command, off, send, ValueLayout.JAVA_BYTE, 0, len);
      MemorySegment recv = a.allocate(kRecvSize);
      MemorySegment recvLen = a.allocate(8);
      recvLen.set(C_LONG, 0, kRecvSize);
      check(call(transmit, card, pci.reinterpret(16), send, (long) len, MemorySegment.NULL, recv, recvLen));
      byte[] out = new byte[(int) recvLen.get(C_LONG, 0)];
      MemorySegment.copy(recv, ValueLayout.JAVA_BYTE, 0, out, 0, out.length);
      return out;
    }
  }

  static byte[] SCardStatus(long card) throws PCSCException {
    try (Arena a = Arena.ofConfined()) {
      MemorySegment atr = a.allocate(33);
      MemorySegment atrLen = a.allocate(8);
      atrLen.set(C_LONG, 0, 33);
      MemorySegment readerLen = a.allocate(8);
      readerLen.set(C_LONG, 0, 0);
      MemorySegment st = a.allocate(8);
      MemorySegment proto = a.allocate(8);
      check(call(status, card, MemorySegment.NULL, readerLen, st, proto, atr, atrLen));
      byte[] out = new byte[(int) atrLen.get(C_LONG, 0)];
      MemorySegment.copy(atr, ValueLayout.JAVA_BYTE, 0, out, 0, out.length);
      return out;
    }
  }

  static void SCardDisconnect(long card, int disposition) throws PCSCException {
    check(call(disconnect, card, (long) disposition));
  }

  static void SCardBeginTransaction(long card) throws PCSCException {
    check(call(begin, card));
  }

  static void SCardEndTransaction(long card, int disposition) throws PCSCException {
    check(call(end, card, (long) disposition));
  }

  static byte[] SCardControl(long card, int code, byte[] command) throws PCSCException {
    try (Arena a = Arena.ofConfined()) {
      MemorySegment send = a.allocate(Math.max(command.length, 1));
      MemorySegment.copy(command, 0, send, ValueLayout.JAVA_BYTE, 0, command.length);
      MemorySegment recv = a.allocate(kRecvSize);
      MemorySegment got = a.allocate(8);
      check(call(control, card, (long) code & 0xffffffffL, send, (long) command.length, recv, (long) kRecvSize, got));
      byte[] out = new byte[(int) got.get(C_LONG, 0)];
      MemorySegment.copy(recv, ValueLayout.JAVA_BYTE, 0, out, 0, out.length);
      return out;
    }
  }
}
