/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
#include <string.h>
#include <stdlib.h>
typedef long LONG;
typedef unsigned long DWORD;
typedef struct { DWORD dwProtocol; DWORD cbPciLength; } SCARD_IO_REQUEST;
typedef struct {
  const char *szReader; void *pvUserData; DWORD dwCurrentState; DWORD dwEventState; DWORD cbAtr;
  unsigned char rgbAtr[33];
} SCARD_READERSTATE;
SCARD_IO_REQUEST g_rgSCardT0Pci = {1, 8}, g_rgSCardT1Pci = {2, 8}, g_rgSCardRawPci = {4, 8};
static const unsigned char kAtr[] = {0x3b, 0x8f, 0x80, 0x01, 0x80, 0x4f, 0x0c, 0xa0, 0, 0, 3, 6, 3, 0, 3, 0, 0, 0, 0, 0x68};
static int disconnected;
static int proto = 2;
LONG SCardEstablishContext(DWORD scope, const void *a, const void *b, LONG *ctx) { *ctx = 4242; return 0; }
LONG SCardListReaders(LONG ctx, const char *g, char *r, DWORD *len) {
  static const char names[] = "Fake Reader 0\0Fake Reader 1\0";
  if (ctx != 4242) return 0x80100003;
  if (r == 0) { *len = sizeof(names); return 0; }
  if (*len < sizeof(names)) return 0x80100008;
  memcpy(r, names, sizeof(names)); *len = sizeof(names); return 0;
}
LONG SCardGetStatusChange(LONG ctx, DWORD timeout, SCARD_READERSTATE *s, DWORD n) {
  int changed = 0;
  for (DWORD i = 0; i < n; i++) {
    DWORD ev = strcmp(s[i].szReader, "Fake Reader 0") == 0 ? 0x20 : 0x10;
    ev |= 0x10000;
    if ((s[i].dwCurrentState & 0xffff) != (ev & 0xffff)) { ev |= 2; changed = 1; }
    s[i].dwEventState = ev;
  }
  if (!changed && timeout != 0 && n && s[0].dwCurrentState != 0) return 0x8010000a;
  return 0;
}
LONG SCardConnect(LONG ctx, const char *r, DWORD share, DWORD protos, LONG *card, DWORD *active) {
  if (strcmp(r, "Fake Reader 0") != 0) return 0x8010000c;
  if (share == 3) { *card = 77; *active = 0; return 0; }
  if (!(protos & 2)) { proto = 1; } else proto = 2;
  *card = 77; *active = proto; disconnected = 0; return 0;
}
LONG SCardStatus(LONG card, char *r, DWORD *rl, DWORD *st, DWORD *p, unsigned char *atr, DWORD *al) {
  memcpy(atr, kAtr, sizeof(kAtr)); *al = sizeof(kAtr); *p = proto; return 0;
}
LONG SCardTransmit(LONG card, const SCARD_IO_REQUEST *pci, const unsigned char *s, DWORD sl, SCARD_IO_REQUEST *rp,
                   unsigned char *r, DWORD *rl) {
  unsigned char ins = sl > 1 ? s[1] : 0;
  DWORD n = 0;
  if (sl < 4) return 0x80100004;
  if (ins == 0x70) { if (s[2] == 0) { r[0] = 1; r[1] = 0x90; r[2] = 0; n = 3; } else { r[0] = 0x90; r[1] = 0; n = 2; } }
  else if (ins == 0xca) { r[0] = 1; r[1] = 2; r[2] = 3; r[3] = 0x90; r[4] = 0; n = 5; }
  else if (ins == 0x61) { r[0] = 0x61; r[1] = 4; n = 2; }
  else if (ins == 0xc0) { r[0] = 9; r[1] = 8; r[2] = 7; r[3] = 6; r[4] = 0x90; r[5] = 0; n = 6; }
  else if (ins == 0x6c) {
    if (sl >= 5 && s[4] != 3) { r[0] = 0x6c; r[1] = 3; n = 2; }
    else { r[0] = 5; r[1] = 5; r[2] = 5; r[3] = 0x90; r[4] = 0; n = 5; }
  } else if (ins == 0xee) { for (int i = 0; i < 300; i++) r[i] = (unsigned char)i; r[300] = 0x90; r[301] = 0; n = 302; }
  else { r[0] = (unsigned char)sl; r[1] = s[0]; r[2] = 0x90; r[3] = 0; n = 4; }
  *rl = n; return 0;
}
LONG SCardDisconnect(LONG card, DWORD d) { disconnected = 1; return 0; }
LONG SCardBeginTransaction(LONG card) { return 0; }
LONG SCardEndTransaction(LONG card, DWORD d) { return 0; }
LONG SCardControl(LONG card, DWORD code, const void *s, DWORD sl, void *r, DWORD rl, DWORD *got) {
  memcpy(r, s, sl); *got = sl; return 0;
}
