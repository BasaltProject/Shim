/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

#include "shim/npe-message.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

using namespace vm;

namespace {

const int kInvalid = -1;
const int kMaxCauseDetail = 5;

struct Text {
  char data[1024];
  unsigned size;

  Text() : size(0)
  {
    data[0] = 0;
  }

  void put(const char* s)
  {
    while (*s and size + 1 < sizeof(data)) {
      data[size++] = *s++;
    }
    data[size] = 0;
  }

  void put(char c)
  {
    if (size + 1 < sizeof(data)) {
      data[size++] = c;
      data[size] = 0;
    }
  }

  void putInt(int v)
  {
    char tmp[16];
    ::snprintf(tmp, sizeof(tmp), "%d", v);
    put(tmp);
  }
};

struct State {
  int32_t* stack;
  unsigned depth;
  uint8_t* written;
};

unsigned u2(const uint8_t* c, unsigned pc)
{
  return (c[pc] << 8) | c[pc + 1];
}

int32_t s4(const uint8_t* c, unsigned pc)
{
  return static_cast<int32_t>((static_cast<uint32_t>(c[pc]) << 24) | (c[pc + 1] << 16) | (c[pc + 2] << 8) | c[pc + 3]);
}

unsigned pad4(unsigned pc)
{
  return (pc + 4) & ~3u;
}

const char* utf(object o)
{
  return reinterpret_cast<const char*>(reinterpret_cast<GcByteArray*>(o)->body().begin());
}

bool sameChars(const char* a, const char* b)
{
  return strcmp(a, b) == 0;
}

void putClassName(Text& out, const char* internal)
{
  char tmp[512];
  unsigned n = 0;
  for (; internal[n] and n + 1 < sizeof(tmp); ++n) {
    tmp[n] = internal[n] == '/' ? '.' : internal[n];
  }
  tmp[n] = 0;
  if (sameChars(tmp, "java.lang.Object")) {
    out.put("Object");
  } else if (sameChars(tmp, "java.lang.String")) {
    out.put("String");
  } else {
    out.put(tmp);
  }
}


const char* putType(Text& out, const char* d)
{
  unsigned dims = 0;
  while (*d == '[') {
    ++dims;
    ++d;
  }
  switch (*d) {
  case 'B': out.put("byte"); ++d; break;
  case 'C': out.put("char"); ++d; break;
  case 'D': out.put("double"); ++d; break;
  case 'F': out.put("float"); ++d; break;
  case 'I': out.put("int"); ++d; break;
  case 'J': out.put("long"); ++d; break;
  case 'S': out.put("short"); ++d; break;
  case 'Z': out.put("boolean"); ++d; break;
  case 'V': out.put("void"); ++d; break;
  case 'L': {
    const char* end = strchr(d, ';');
    char tmp[512];
    unsigned n = static_cast<unsigned>(end - (d + 1));
    if (n >= sizeof(tmp)) {
      n = sizeof(tmp) - 1;
    }
    memcpy(tmp, d + 1, n);
    tmp[n] = 0;
    putClassName(out, tmp);
    d = end + 1;
  } break;
  default:
    ++d;
    break;
  }
  while (dims--) {
    out.put("[]");
  }
  return d;
}

unsigned typeWords(char c)
{
  return (c == 'J' or c == 'D') ? 2 : 1;
}

unsigned argumentWords(const char* spec)
{
  unsigned words = 0;
  ++spec;
  while (*spec != ')') {
    char c = *spec;
    while (*spec == '[') {
      ++spec;
    }
    if (*spec == 'L') {
      spec = strchr(spec, ';');
    }
    ++spec;
    words += (c == 'J' or c == 'D') ? 2 : 1;
  }
  return words;
}

unsigned returnWords(const char* spec)
{
  const char* r = strchr(spec, ')') + 1;
  if (*r == 'V') {
    return 0;
  }
  return typeWords(*r);
}

struct MemberRef {
  const char* owner;
  const char* name;
  const char* spec;
  bool valid;
};

struct Frame {
  Thread* t;
  GcMethod* method;
  GcCode* code;
  GcSingleton* pool;
  const uint8_t* body;
  unsigned length;
  unsigned maxLocals;
  unsigned maxStack;
  State** states;
  uint8_t* queued;
  unsigned* work;
  unsigned workSize;
  bool failed;
};

const char* ownerName(Thread* t, object o)
{
  if (objectClass(t, o) == type(t, GcByteArray::Type)) {
    return utf(o);
  }
  return utf(cast<GcClass>(t, o)->name());
}

MemberRef memberAt(Frame& f, unsigned index)
{
  MemberRef r = {0, 0, 0, false};
  object o = singletonObject(f.t, f.pool, index - 1);
  if (o == 0) {
    return r;
  }
  Thread* t = f.t;
  if (objectClass(t, o) == type(t, GcReference::Type)) {
    GcReference* ref = cast<GcReference>(t, o);
    r.owner = ownerName(t, ref->class_());
    r.name = utf(ref->name());
    r.spec = utf(ref->spec());
    r.valid = true;
  } else if (objectClass(t, o) == type(t, GcMethodHandle::Type)) {
    GcMethodHandle* h = cast<GcMethodHandle>(t, o);
    GcMethod* m = h->method();
    r.owner = h->symbolicOwner() ? ownerName(t, h->symbolicOwner()) : utf(m->class_()->name());
    r.name = utf(m->name());
    r.spec = utf(m->spec());
    r.valid = true;
  } else if (objectClass(t, o) == type(t, GcInvocation::Type)) {
    GcMethod* m = cast<GcInvocation>(t, o)->template_();
    r.owner = "";
    r.name = utf(m->name());
    r.spec = utf(m->spec());
    r.valid = true;
  } else if (objectClass(t, o) == type(t, GcField::Type)) {
    GcField* fl = cast<GcField>(t, o);
    r.owner = utf(fl->class_()->name());
    r.name = utf(fl->name());
    r.spec = utf(fl->spec());
    r.valid = true;
  }
  return r;
}

State* copyState(Frame& f, const State* s)
{
  State* c = static_cast<State*>(malloc(sizeof(State)));
  c->stack = static_cast<int32_t*>(malloc((f.maxStack + 8) * sizeof(int32_t)));
  c->written = static_cast<uint8_t*>(malloc(f.maxLocals + 2));
  c->depth = s->depth;
  memcpy(c->stack, s->stack, s->depth * sizeof(int32_t));
  memcpy(c->written, s->written, f.maxLocals + 2);
  return c;
}

void freeState(State* s)
{
  if (s) {
    free(s->stack);
    free(s->written);
    free(s);
  }
}

void enqueue(Frame& f, unsigned pc)
{
  if (not f.queued[pc]) {
    f.queued[pc] = 1;
    f.work[f.workSize++] = pc;
  }
}

void flowTo(Frame& f, unsigned pc, const State& s)
{
  if (pc >= f.length) {
    f.failed = true;
    return;
  }
  State* old = f.states[pc];
  if (old == 0) {
    f.states[pc] = copyState(f, &s);
    enqueue(f, pc);
    return;
  }
  bool changed = false;
  if (old->depth != s.depth) {
    return;
  }
  for (unsigned i = 0; i < s.depth; ++i) {
    if (old->stack[i] != s.stack[i] and old->stack[i] != kInvalid) {
      old->stack[i] = kInvalid;
      changed = true;
    }
  }
  for (unsigned i = 0; i < f.maxLocals + 2u; ++i) {
    if (s.written[i] and not old->written[i]) {
      old->written[i] = 1;
      changed = true;
    }
  }
  if (changed) {
    enqueue(f, pc);
  }
}

struct Sim {
  Frame& f;
  State s;

  Sim(Frame& f, const State& pre) : f(f)
  {
    s.stack = static_cast<int32_t*>(malloc((f.maxStack + 8) * sizeof(int32_t)));
    s.written = static_cast<uint8_t*>(malloc(f.maxLocals + 2));
    s.depth = pre.depth;
    memcpy(s.stack, pre.stack, pre.depth * sizeof(int32_t));
    memcpy(s.written, pre.written, f.maxLocals + 2);
  }

  ~Sim()
  {
    free(s.stack);
    free(s.written);
  }

  void pop(unsigned n)
  {
    if (s.depth < n) {
      f.failed = true;
      s.depth = 0;
    } else {
      s.depth -= n;
    }
  }

  void push(unsigned n, int source)
  {
    for (unsigned i = 0; i < n; ++i) {
      if (s.depth >= f.maxStack + 8) {
        f.failed = true;
        return;
      }
      s.stack[s.depth++] = source;
    }
  }

  void write(unsigned slot, unsigned words)
  {
    for (unsigned i = 0; i < words; ++i) {
      if (slot + i < f.maxLocals + 2u) {
        s.written[slot + i] = 1;
      }
    }
  }

  int top(unsigned fromTop)
  {
    return s.depth > fromTop ? s.stack[s.depth - 1 - fromTop] : kInvalid;
  }
};

}  // namespace

namespace vm {

bool npeCapableOpcode(unsigned op)
{
  switch (op) {
  case 0x2e: case 0x2f: case 0x30: case 0x31: case 0x32: case 0x33: case 0x34: case 0x35:
  case 0x4f: case 0x50: case 0x51: case 0x52: case 0x53: case 0x54: case 0x55: case 0x56:
  case 0xb4: case 0xb5: case 0xb6: case 0xb7: case 0xb9:
  case 0xbe: case 0xbf: case 0xc2: case 0xc3:
    return true;
  default:
    return false;
  }
}

unsigned npeInstructionLength(const uint8_t* code, unsigned length, unsigned pc)
{
  unsigned op = code[pc];
  switch (op) {
  case 0x10: case 0x12: case 0x15: case 0x16: case 0x17: case 0x18: case 0x19: case 0x36: case 0x37: case 0x38:
  case 0x39: case 0x3a: case 0xa9: case 0xbc:
    return 2;
  case 0x11: case 0x13: case 0x14: case 0x84: case 0xb2: case 0xb3: case 0xb4: case 0xb5: case 0xb6: case 0xb7:
  case 0xb8: case 0xbb: case 0xbd: case 0xc0: case 0xc1: case 0xc6: case 0xc7:
    return 3;
  case 0xc5:
    return 4;
  case 0xb9: case 0xba: case 0xc8: case 0xc9:
    return 5;
  case 0xc4:
    return code[pc + 1] == 0x84 ? 6 : 4;
  case 0xaa: {
    unsigned p = pad4(pc);
    if (p + 12 > length) {
      return length - pc;
    }
    int32_t low = s4(code, p + 4);
    int32_t high = s4(code, p + 8);
    return p + 12 + (static_cast<int64_t>(high) - low + 1) * 4 - pc;
  }
  case 0xab: {
    unsigned p = pad4(pc);
    if (p + 8 > length) {
      return length - pc;
    }
    return p + 8 + static_cast<int64_t>(s4(code, p + 4)) * 8 - pc;
  }
  default:
    if (op >= 0x99 and op <= 0xa8) {
      return 3;
    }
    return 1;
  }
}

}  // namespace vm

namespace {

void simulate(Frame& f, unsigned pc, const State& pre);

void successors(Frame& f, unsigned pc, const State& post, unsigned op)
{
  const uint8_t* c = f.body;
  unsigned next = pc + npeInstructionLength(c, f.length, pc);
  if ((op >= 0x99 and op <= 0xa6) or op == 0xc6 or op == 0xc7) {
    int16_t off = static_cast<int16_t>(u2(c, pc + 1));
    flowTo(f, pc + off, post);
    flowTo(f, next, post);
  } else if (op == 0xa7 or op == 0xa8) {
    int16_t off = static_cast<int16_t>(u2(c, pc + 1));
    flowTo(f, pc + off, post);
  } else if (op == 0xc8 or op == 0xc9) {
    flowTo(f, pc + s4(c, pc + 1), post);
  } else if (op == 0xaa) {
    unsigned p = pad4(pc);
    flowTo(f, pc + s4(c, p), post);
    int32_t low = s4(c, p + 4);
    int32_t high = s4(c, p + 8);
    for (int64_t i = 0; i <= static_cast<int64_t>(high) - low; ++i) {
      flowTo(f, pc + s4(c, p + 12 + i * 4), post);
    }
  } else if (op == 0xab) {
    unsigned p = pad4(pc);
    flowTo(f, pc + s4(c, p), post);
    int32_t n = s4(c, p + 4);
    for (int32_t i = 0; i < n; ++i) {
      flowTo(f, pc + s4(c, p + 12 + i * 8), post);
    }
  } else if ((op >= 0xac and op <= 0xb1) or op == 0xbf or op == 0xa9) {
    return;
  } else {
    flowTo(f, next, post);
  }
}

void simulate(Frame& f, unsigned pc, const State& pre)
{
  const uint8_t* c = f.body;
  unsigned op = c[pc];
  Sim m(f, pre);
  int me = static_cast<int>(pc);

  if (op >= 0x03 and op <= 0x08) m.push(1, me);
  else if (op == 0x01 or op == 0x02) m.push(1, me);
  else if (op == 0x09 or op == 0x0a or op == 0x0e or op == 0x0f) m.push(2, me);
  else if (op >= 0x0b and op <= 0x0d) m.push(1, me);
  else if (op == 0x10 or op == 0x11 or op == 0x12 or op == 0x13) m.push(1, me);
  else if (op == 0x14) m.push(2, me);
  else if (op == 0x15 or op == 0x17 or op == 0x19) m.push(1, me);
  else if (op == 0x16 or op == 0x18) m.push(2, me);
  else if ((op >= 0x1a and op <= 0x1d) or (op >= 0x22 and op <= 0x25) or (op >= 0x2a and op <= 0x2d)) m.push(1, me);
  else if ((op >= 0x1e and op <= 0x21) or (op >= 0x26 and op <= 0x29)) m.push(2, me);
  else if (op == 0x2e or op == 0x30 or op == 0x32 or (op >= 0x33 and op <= 0x35)) { m.pop(2); m.push(1, me); }
  else if (op == 0x2f or op == 0x31) { m.pop(2); m.push(2, me); }
  else if (op == 0x36 or op == 0x38 or op == 0x3a) { m.pop(1); m.write(c[pc + 1], 1); }
  else if (op == 0x37 or op == 0x39) { m.pop(2); m.write(c[pc + 1], 2); }
  else if (op >= 0x3b and op <= 0x3e) { m.pop(1); m.write(op - 0x3b, 1); }
  else if (op >= 0x3f and op <= 0x42) { m.pop(2); m.write(op - 0x3f, 2); }
  else if (op >= 0x43 and op <= 0x46) { m.pop(1); m.write(op - 0x43, 1); }
  else if (op >= 0x47 and op <= 0x4a) { m.pop(2); m.write(op - 0x47, 2); }
  else if (op >= 0x4b and op <= 0x4e) { m.pop(1); m.write(op - 0x4b, 1); }
  else if (op == 0x4f or op == 0x51 or (op >= 0x53 and op <= 0x56)) m.pop(3);
  else if (op == 0x50 or op == 0x52) m.pop(4);
  else if (op == 0x57) m.pop(1);
  else if (op == 0x58) m.pop(2);
  else if (op == 0x59) { int a = m.top(0); m.push(1, a); }
  else if (op == 0x5a) { int a = m.top(0), b = m.top(1); m.pop(2); m.push(1, a); m.push(1, b); m.push(1, a); }
  else if (op == 0x5b) {
    int a = m.top(0), b = m.top(1), d = m.top(2);
    m.pop(3); m.push(1, a); m.push(1, d); m.push(1, b); m.push(1, a);
  } else if (op == 0x5c) { int a = m.top(0), b = m.top(1); m.push(1, b); m.push(1, a); }
  else if (op == 0x5d) {
    int a = m.top(0), b = m.top(1), d = m.top(2);
    m.pop(3); m.push(1, b); m.push(1, a); m.push(1, d); m.push(1, b); m.push(1, a);
  } else if (op == 0x5e) {
    int a = m.top(0), b = m.top(1), d = m.top(2), e = m.top(3);
    m.pop(4); m.push(1, b); m.push(1, a); m.push(1, e); m.push(1, d); m.push(1, b); m.push(1, a);
  } else if (op == 0x5f) { int a = m.top(0), b = m.top(1); m.pop(2); m.push(1, a); m.push(1, b); }
  else if (op >= 0x60 and op <= 0x73) { unsigned w = (op & 1) ? 2 : 1; m.pop(2 * w); m.push(w, me); }
  else if (op >= 0x74 and op <= 0x77) { m.pop(typeWords("IJFD"[op - 0x74])); m.push(typeWords("IJFD"[op - 0x74]), me); }
  else if (op == 0x78 or op == 0x7a or op == 0x7c) { m.pop(2); m.push(1, me); }
  else if (op == 0x79 or op == 0x7b or op == 0x7d) { m.pop(3); m.push(2, me); }
  else if (op == 0x7e or op == 0x80 or op == 0x82) { m.pop(2); m.push(1, me); }
  else if (op == 0x7f or op == 0x81 or op == 0x83) { m.pop(4); m.push(2, me); }
  else if (op == 0x84) m.write(c[pc + 1], 1);
  else if (op == 0x85 or op == 0x87 or op == 0x8c or op == 0x8d) { m.pop(1); m.push(2, me); }
  else if (op == 0x86 or op == 0x8b or (op >= 0x91 and op <= 0x93)) { m.pop(1); m.push(1, me); }
  else if (op == 0x88 or op == 0x89) { m.pop(2); m.push(1, me); }
  else if (op == 0x8a) { m.pop(2); m.push(2, me); }
  else if (op == 0x8e or op == 0x90) { m.pop(2); m.push(1, me); }
  else if (op == 0x8f) { m.pop(2); m.push(2, me); }
  else if (op == 0x94 or op == 0x97 or op == 0x98) { m.pop(4); m.push(1, me); }
  else if (op == 0x95 or op == 0x96) { m.pop(2); m.push(1, me); }
  else if ((op >= 0x99 and op <= 0x9e) or op == 0xc6 or op == 0xc7 or op == 0xaa or op == 0xab) m.pop(1);
  else if (op >= 0x9f and op <= 0xa6) m.pop(2);
  else if (op == 0xa7 or op == 0xc8) {}
  else if (op == 0xa8 or op == 0xc9) m.push(1, me);
  else if (op == 0xa9) {}
  else if (op == 0xac or op == 0xae or op == 0xb0) m.pop(1);
  else if (op == 0xad or op == 0xaf) m.pop(2);
  else if (op == 0xb1) {}
  else if (op >= 0xb2 and op <= 0xb5) {
    MemberRef r = memberAt(f, u2(c, pc + 1));
    if (not r.valid) { f.failed = true; return; }
    unsigned w = typeWords(r.spec[0]);
    if (op == 0xb2) m.push(w, me);
    else if (op == 0xb3) m.pop(w);
    else if (op == 0xb4) { m.pop(1); m.push(w, me); }
    else m.pop(w + 1);
  } else if (op >= 0xb6 and op <= 0xba) {
    MemberRef r = memberAt(f, u2(c, pc + 1));
    if (not r.valid) { f.failed = true; return; }
    m.pop(argumentWords(r.spec) + ((op == 0xb8 or op == 0xba) ? 0 : 1));
    m.push(returnWords(r.spec), me);
  } else if (op == 0xbb) m.push(1, me);
  else if (op >= 0xbc and op <= 0xbe) { m.pop(1); m.push(1, me); }
  else if (op == 0xbf) m.pop(1);
  else if (op == 0xc0) {}
  else if (op == 0xc1) { m.pop(1); m.push(1, me); }
  else if (op == 0xc2 or op == 0xc3) m.pop(1);
  else if (op == 0xc4) {
    unsigned wop = c[pc + 1];
    unsigned idx = u2(c, pc + 2);
    if (wop >= 0x15 and wop <= 0x19) m.push((wop == 0x16 or wop == 0x18) ? 2 : 1, me);
    else if (wop >= 0x36 and wop <= 0x3a) { unsigned w = (wop == 0x37 or wop == 0x39) ? 2 : 1; m.pop(w); m.write(idx, w); }
    else if (wop == 0x84) m.write(idx, 1);
  } else if (op == 0xc5) { m.pop(c[pc + 3]); m.push(1, me); }
  else if (op == 0x00) {}
  else { f.failed = true; return; }

  if (f.failed) {
    return;
  }

  GcExceptionHandlerTable* eht = f.code->exceptionHandlerTable() ? cast<GcExceptionHandlerTable>(f.t, f.code->exceptionHandlerTable()) : 0;
  for (unsigned i = 0; eht and i < eht->length(); ++i) {
    uint64_t eh = eht->body()[i];
    if (pc >= exceptionHandlerStart(eh) and pc < exceptionHandlerEnd(eh)) {
      int32_t one = kInvalid;
      State hs = {&one, 1, pre.written};
      flowTo(f, exceptionHandlerIp(eh), hs);
    }
  }
  successors(f, pc, m.s, op);
}

bool analyze(Frame& f)
{
  f.states = static_cast<State**>(calloc(f.length + 1, sizeof(State*)));
  f.queued = static_cast<uint8_t*>(calloc(f.length + 1, 1));
  f.work = static_cast<unsigned*>(malloc((f.length + 1) * sizeof(unsigned) * 4));
  f.workSize = 0;
  int32_t none = 0;
  uint8_t* written = static_cast<uint8_t*>(calloc(f.maxLocals + 2, 1));
  State init = {&none, 0, written};
  flowTo(f, 0, init);
  free(written);
  unsigned guard = 0;
  while (f.workSize and not f.failed and ++guard < 1000000) {
    unsigned pc = f.work[--f.workSize];
    f.queued[pc] = 0;
    State* s = f.states[pc];
    if (s) {
      simulate(f, pc, *s);
    }
  }
  return not f.failed;
}

bool printLocal(Frame& f, Text& out, unsigned bci, unsigned slot, bool isParameter)
{
  Thread* t = f.t;
  GcLineNumberTable* lvt = f.code->localVars();
  for (unsigned i = 0; lvt and i < lvt->length(); i += 2) {
    uint64_t e = lvt->body()[i];
    unsigned start = e & 0xffff;
    unsigned span = (e >> 16) & 0xffff;
    unsigned vslot = (e >> 32) & 0xffff;
    if (vslot == slot and bci >= start and bci < start + span) {
      object nm = singletonObject(t, f.pool, static_cast<unsigned>(lvt->body()[i + 1]) - 1);
      out.put(utf(nm));
      return true;
    }
  }
  bool isStatic = f.method->flags() & ACC_STATIC;
  if (not isStatic and slot == 0 and isParameter) {
    out.put("this");
    return true;
  }
  unsigned cur = isStatic ? 0 : 1;
  unsigned paramIndex = 1;
  const char* spec = utf(f.method->spec()) + 1;
  bool found = false;
  while (*spec != ')') {
    char first = *spec;
    const char* p = spec;
    while (*p == '[') ++p;
    if (*p == 'L') p = strchr(p, ';');
    ++p;
    unsigned size = (first == 'J' or first == 'D') ? 2 : 1;
    if (slot >= cur and slot < cur + size) {
      found = true;
      break;
    }
    ++paramIndex;
    cur += size;
    spec = p;
  }
  if (found and isParameter) {
    out.put("<parameter");
    out.putInt(paramIndex);
    out.put('>');
  } else {
    out.put("<local");
    out.putInt(slot);
    out.put('>');
  }
  return true;
}

void printMethod(Frame& f, Text& out, const MemberRef& r)
{
  putClassName(out, r.owner);
  out.put('.');
  out.put(r.name);
  out.put('(');
  const char* d = r.spec + 1;
  bool first = true;
  while (*d != ')') {
    if (not first) out.put(", ");
    first = false;
    d = putType(out, d);
  }
  out.put(')');
}

bool printCause(Frame& f, Text& out, unsigned bci, unsigned slot, int maxDetail, bool inner);

bool printSource(Frame& f, Text& out, int source, int maxDetail, bool inner)
{
  if (source == kInvalid) {
    return false;
  }
  const uint8_t* c = f.body;
  unsigned sb = static_cast<unsigned>(source);
  unsigned op = c[sb];
  if (op == 0x01) {
    out.put("null");
    return true;
  }
  if (op >= 0x02 and op <= 0x08) {
    out.putInt(static_cast<int>(op) - 3);
    return true;
  }
  if (op == 0x10) {
    out.putInt(static_cast<int8_t>(c[sb + 1]));
    return true;
  }
  if (op == 0x11) {
    out.putInt(static_cast<int16_t>(u2(c, sb + 1)));
    return true;
  }
  State* st = f.states[sb];
  unsigned slot = 0;
  bool local = false;
  if (op >= 0x1a and op <= 0x1d) { slot = op - 0x1a; local = true; }
  else if (op >= 0x2a and op <= 0x2d) { slot = op - 0x2a; local = true; }
  else if (op == 0x15 or op == 0x19) { slot = c[sb + 1]; local = true; }
  else if (op == 0xc4 and (c[sb + 1] == 0x15 or c[sb + 1] == 0x19)) { slot = u2(c, sb + 2); local = true; }
  if (local) {
    return st ? printLocal(f, out, sb, slot, not st->written[slot]) : false;
  }
  switch (op) {
  case 0xb2: {
    MemberRef r = memberAt(f, u2(c, sb + 1));
    if (not r.valid) return false;
    putClassName(out, r.owner);
    out.put('.');
    out.put(r.name);
    return true;
  }
  case 0xb4: {
    MemberRef r = memberAt(f, u2(c, sb + 1));
    if (not r.valid) return false;
    if (maxDetail > 1 and st and printCause(f, out, sb, 0, maxDetail - 1, inner)) {
      out.put('.');
    }
    out.put(r.name);
    return true;
  }
  case 0xb6: case 0xb7: case 0xb8: case 0xb9: {
    MemberRef r = memberAt(f, u2(c, sb + 1));
    if (not r.valid) return false;
    printMethod(f, out, r);
    return true;
  }
  case 0x2e: case 0x2f: case 0x30: case 0x31: case 0x32: case 0x33: case 0x34: case 0x35: {
    if (not st) return false;
    if (printCause(f, out, sb, 1, maxDetail - 1, inner)) {
      out.put('[');
    } else {
      out.put("<array>[");
    }
    if (not printCause(f, out, sb, 0, maxDetail - 1, true)) {
      out.put("...");
    }
    out.put(']');
    return true;
  }
  default:
    return false;
  }
}

bool printCause(Frame& f, Text& out, unsigned bci, unsigned slot, int maxDetail, bool inner)
{
  if (maxDetail <= 0) {
    return false;
  }
  State* st = f.states[bci];
  if (st == 0 or slot >= st->depth) {
    return false;
  }
  return printSource(f, out, st->stack[st->depth - 1 - slot], maxDetail, inner);
}

const char* arrayKind(unsigned op)
{
  switch (op) {
  case 0x2e: case 0x4f: return "int";
  case 0x2f: case 0x50: return "long";
  case 0x30: case 0x51: return "float";
  case 0x31: case 0x52: return "double";
  case 0x32: case 0x53: return "object";
  case 0x33: case 0x54: return "byte/boolean";
  case 0x34: case 0x55: return "char";
  default: return "short";
  }
}

}  // namespace

namespace vm {

GcString* extendedNullPointerMessage(Thread* t, GcThrowable* npe)
{
  object trace = npe->trace();
  if (trace == 0 or objectArrayLength(t, trace) == 0) {
    return 0;
  }
  GcTraceElement* e = cast<GcTraceElement>(t, objectArrayBody(t, trace, 0));
  GcMethod* method = cast<GcMethod>(t, e->method());
  if (method == 0 or (method->flags() & ACC_NATIVE) or method->code() == 0) {
    return 0;
  }
  GcCode* code = method->code();
  GcCode* source = code->origin() ? code->origin() : code;
  unsigned length = source->length();
  if (length == 0 or source->pool() == 0) {
    return 0;
  }
  int ip = e->ip();
  unsigned bci = 0;
  if (code->compiled()) {
    GcLineNumberTable* table = code->bciTable();
    if (table == 0 or ip <= 0) {
      return 0;
    }
    unsigned off = static_cast<unsigned>(ip) - 1;
    bool found = false;
    for (unsigned i = 0; i < table->length(); ++i) {
      if (lineNumberIp(table->body()[i]) <= off) {
        bci = lineNumberLine(table->body()[i]);
        found = true;
      } else {
        break;
      }
    }
    if (not found) {
      return 0;
    }
  } else {
    unsigned target = ip > 0 ? static_cast<unsigned>(ip) - 1 : 0;
    const uint8_t* body = source->body().begin();
    unsigned pc = 0;
    while (pc < length) {
      unsigned n = npeInstructionLength(body, length, pc);
      if (target < pc + n) {
        break;
      }
      pc += n;
    }
    if (pc >= length) {
      return 0;
    }
    bci = pc;
  }

  const uint8_t* body = source->body().begin();
  if (bci >= length or not npeCapableOpcode(body[bci])) {
    return 0;
  }

  PROTECT(t, method);
  PROTECT(t, source);

  Frame f = {t, method, source, cast<GcSingleton>(t, source->pool()), body, length, source->maxLocals(),
             source->maxStack(), 0, 0, 0, 0, false};
  bool ok = analyze(f);
  Text out;
  if (ok and f.states[bci]) {
    unsigned op = body[bci];
    State* st = f.states[bci];
    unsigned slot = 0;
    if (op >= 0x2e and op <= 0x35) {
      out.put("Cannot load from ");
      out.put(arrayKind(op));
      out.put(" array");
      slot = 1;
    } else if (op >= 0x4f and op <= 0x56) {
      out.put("Cannot store to ");
      out.put(arrayKind(op));
      out.put(" array");
      slot = (op == 0x50 or op == 0x52) ? 3 : 2;
    } else if (op == 0xbe) {
      out.put("Cannot read the array length");
    } else if (op == 0xbf) {
      out.put("Cannot throw exception");
    } else if (op == 0xc2) {
      out.put("Cannot enter synchronized block");
    } else if (op == 0xc3) {
      out.put("Cannot exit synchronized block");
    } else if (op == 0xb4 or op == 0xb5) {
      MemberRef r = memberAt(f, u2(body, bci + 1));
      if (not r.valid) {
        ok = false;
      } else {
        out.put(op == 0xb4 ? "Cannot read field \"" : "Cannot assign field \"");
        out.put(r.name);
        out.put('"');
        slot = op == 0xb4 ? 0 : typeWords(r.spec[0]);
      }
    } else {
      MemberRef r = memberAt(f, u2(body, bci + 1));
      if (not r.valid) {
        ok = false;
      } else {
        out.put("Cannot invoke \"");
        printMethod(f, out, r);
        out.put('"');
        slot = argumentWords(r.spec);
      }
    }
    if (ok and slot < st->depth) {
      int src = st->stack[st->depth - 1 - slot];
      Text cause;
      if (src != kInvalid and printSource(f, cause, src, kMaxCauseDetail, false)) {
        unsigned sop = body[src];
        bool invoke = sop >= 0xb6 and sop <= 0xb9;
        out.put(invoke ? " because the return value of \"" : " because \"");
        out.put(cause.data);
        out.put("\" is null");
      }
    }
  } else {
    ok = false;
  }

  for (unsigned i = 0; i < length + 1; ++i) {
    freeState(f.states ? f.states[i] : 0);
  }
  free(f.states);
  free(f.queued);
  free(f.work);
  return ok ? makeString(t, "%s", out.data) : 0;
}

}  // namespace vm
