/*
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Parallel minor collection for the in-place generation with TenureThreshold 0.
// Included by heap.cpp inside namespace local. Forwarding uses ForwardTag in the
// low bits of the first word of a from-space object, installed with a compare and swap.

#ifdef SHIM_HEAP_PARALLEL

const unsigned ChunkItems = 128;
const unsigned DonateThreshold = 256;
const unsigned PlabWords = 8192;
const unsigned MinPlabSpanWords = 512;
const unsigned LargeObjectWords = 2048;
const unsigned MaxHelpers = 15;

struct Chunk {
  Chunk* next;
  unsigned n;
  uintptr_t items[ChunkItems];
};

class SpinLock {
 public:
  SpinLock() : v(0)
  {
  }

  void lock()
  {
    while (__atomic_exchange_n(&v, 1, __ATOMIC_ACQUIRE)) {
      while (__atomic_load_n(&v, __ATOMIC_RELAXED)) {
        cpuRelax();
      }
    }
  }

  void unlock()
  {
    __atomic_store_n(&v, 0, __ATOMIC_RELEASE);
  }

  static void cpuRelax()
  {
#if defined(__x86_64__) || defined(__i386__)
    __builtin_ia32_pause();
#elif defined(__aarch64__)
    __asm__ __volatile__("yield");
#endif
  }

 private:
  int v;
};

struct ParGc;

struct GcWorker {
  ParGc* par;
  unsigned id;
  uintptr_t* stack;
  unsigned count;
  unsigned capacity;
  uintptr_t* cur;
  uintptr_t* end;
  uint64_t objects;
  uint64_t words;
  uint64_t updates;
  uintptr_t* deferred;
  unsigned deferredCount;
  unsigned deferredCapacity;
  pthread_t thread;
};

struct ParGc {
  Context* c;
  unsigned workerCount;
  GcWorker* workers[MaxHelpers + 1];
  SpinLock lock;
  Chunk* pool;
  unsigned poolCount;
  Chunk* freeChunks;
  int busy;
  unsigned nextRegion;
  unsigned regionCount;
  pthread_mutex_t mu;
  pthread_cond_t cv;
  unsigned epoch;
  bool shutdown;
  bool started;
  int active;
  int helpersIn;
  bool major;
};

inline uintptr_t loadHeader(void* o)
{
  return __atomic_load_n(&fieldAtOffset<uintptr_t>(o, 0), __ATOMIC_ACQUIRE);
}

inline void atomicSetBit(uintptr_t* map, unsigned i)
{
  __atomic_fetch_or(map + wordOf(i), static_cast<uintptr_t>(1) << bitOf(i), __ATOMIC_RELAXED);
}

inline void atomicClearBit(uintptr_t* map, unsigned i)
{
  __atomic_fetch_and(map + wordOf(i), ~(static_cast<uintptr_t>(1) << bitOf(i)), __ATOMIC_RELAXED);
}

inline void markCard(Context* c, void** p)
{
  unsigned i = reinterpret_cast<uintptr_t*>(p) - c->gen2.data;
  atomicSetBit(c->pointerMap.data, i / c->pointerMap.scale);
  atomicSetBit(c->pageMap.data, i / c->pageMap.scale);
  atomicSetBit(c->heapMap.data, i / c->heapMap.scale);
}

void pushItem(Context* c, GcWorker* w, uintptr_t item)
{
  if (w->count == w->capacity) {
    grow(c, &w->stack, &w->capacity, w->count + 1);
  }
  w->stack[w->count++] = item;
}

void addTailSpan(Context* c, uintptr_t* from, uintptr_t* to)
{
  if (from == 0 or to <= from) {
    return;
  }
  unsigned length = to - from;
  if (length < MinimumSpanInWords) {
    return;
  }
  grow(c, &c->spans, &c->spanCapacity, c->spanCount + 1);
  c->spans[c->spanCount].start = from - c->gen2.data;
  c->spans[c->spanCount].length = length;
  ++c->spanCount;
  c->freeWords += length;
  c->count -= length * BytesPerWord;
}

void refillPlab(Context* c, GcWorker* w, unsigned size)
{
  ParGc* g = w->par;
  g->lock.lock();

  addTailSpan(c, w->cur, w->end);
  w->cur = w->end = 0;

  unsigned wanted = max(size, MinPlabSpanWords);
  uintptr_t* got = 0;
  unsigned gotWords = 0;

  unsigned scanned = 0;
  while (scanned < MaxSpansScanned and scanned < c->spanCount) {
    if (c->spanCursor >= c->spanCount) {
      c->spanCursor = 0;
    }
    Span* s = c->spans + c->spanCursor;
    if (s->length >= wanted) {
      unsigned take = min(s->length, max(PlabWords, size));
      if (s->length - take < MinimumSpanInWords) {
        take = s->length;
      }
      got = c->gen2.data + s->start;
      gotWords = take;
      s->start += take;
      s->length -= take;
      c->freeWords -= take;
      c->count += take * BytesPerWord;
      break;
    }
    ++c->spanCursor;
    ++scanned;
  }

  if (got == 0) {
    unsigned n = min(max(PlabWords, size), c->gen2.remaining());
    if (n < size) {
      abort(c);
    }
    got = static_cast<uintptr_t*>(c->gen2.allocate(n));
    c->count += n * BytesPerWord;
    gotWords = n;
  }

  g->lock.unlock();

  w->cur = got;
  w->end = got + gotWords;
}

uintptr_t* allocateCopy(Context* c, GcWorker* w, unsigned size)
{
  if (size > LargeObjectWords) {
    ParGc* g = w->par;
    g->lock.lock();
    bool fromSpan;
    uintptr_t* p = static_cast<uintptr_t*>(allocateGen2(c, size, &fromSpan));
    g->lock.unlock();
    return p;
  }
  if (static_cast<unsigned>(w->end - w->cur) < size) {
    refillPlab(c, w, size);
  }
  uintptr_t* p = w->cur;
  w->cur += size;
  return p;
}

inline void releaseCopy(GcWorker* w, uintptr_t* p, unsigned size)
{
  if (w->cur == p + size) {
    w->cur = p;
  }
}

void* copyObject(Context* c, GcWorker* w, void* o, uintptr_t header)
{
  unsigned size = c->client->copiedSizeInWordsOf(o, header);
  uintptr_t* dst = allocateCopy(c, w, size);
  c->client->copyFrom(o, dst, header);

  __atomic_thread_fence(__ATOMIC_SEQ_CST);
  uintptr_t expected = header;
  if (__atomic_compare_exchange_n(&fieldAtOffset<uintptr_t>(o, 0),
                                  &expected,
                                  reinterpret_cast<uintptr_t>(dst) | ForwardTag,
                                  false,
                                  __ATOMIC_ACQ_REL,
                                  __ATOMIC_ACQUIRE)) {
    pushItem(c, w, reinterpret_cast<uintptr_t>(dst));
    ++w->objects;
    w->words += size;
    return dst;
  }

  releaseCopy(w, dst, size);
  return maskAlignedPointer(reinterpret_cast<void*>(expected));
}

void markFixieParallel(Context* c, GcWorker* w, void* o)
{
  Fixie* f = fixie(o);
  if (f->age >= FixieTenureThreshold or __atomic_load_n(&f->flags, __ATOMIC_RELAXED) & Fixie::Marked) {
    return;
  }
  ParGc* g = w->par;
  g->lock.lock();
  if (not f->marked()) {
    f->marked(true);
    f->dead(false);
    f->move(c, &(c->markedFixies));
    pushItem(c, w, reinterpret_cast<uintptr_t>(f->body()) | 1);
  }
  g->lock.unlock();
}

inline void* resolve(Context* c, GcWorker* w, void* o)
{
  if (c->gen2.contains(o) or immortalHeapContains(c, o)) {
    return o;
  }
  uintptr_t header = loadHeader(o);
  if (header & ForwardTag) {
    return maskAlignedPointer(reinterpret_cast<void*>(header));
  }
  if (c->client->isFixed(o)) {
    markFixieParallel(c, w, o);
    return o;
  }
  return copyObject(c, w, o, header);
}

inline bool needsCard(Context* c, void* result)
{
  return not(c->gen2.contains(result) or immortalHeapContains(c, result)
             or (c->client->isFixed(result) and fixie(result)->age >= FixieTenureThreshold));
}

inline void updateSlot(Context* c, GcWorker* w, void** p, bool slotInGen2)
{
  void* raw = *p;
  void* o = maskAlignedPointer(raw);
  if (o == 0) {
    return;
  }
  void* r = resolve(c, w, o);
  if (r != o) {
    *p = reinterpret_cast<void*>(reinterpret_cast<uintptr_t>(r) | (reinterpret_cast<uintptr_t>(raw) & ~PointerMask));
  }
  if (slotInGen2 and not c->gen2.contains(r) and needsCard(c, r)) {
    markCard(c, p);
  }
}

inline void updateFixieSlot(Context* c, GcWorker* w, Fixie* f, unsigned offset)
{
  void** p = f->body() + offset;
  void* raw = *p;
  void* o = maskAlignedPointer(raw);
  if (o == 0) {
    return;
  }
  void* r = resolve(c, w, o);
  if (r != o) {
    *p = reinterpret_cast<void*>(reinterpret_cast<uintptr_t>(r) | (reinterpret_cast<uintptr_t>(raw) & ~PointerMask));
  }
  if (not c->gen2.contains(r) and needsCard(c, r) and static_cast<unsigned>(f->age + 1) >= FixieTenureThreshold) {
    f->dirty(true);
    markBit(f->mask(), offset);
  }
}

class ScanWalker : public Heap::Walker {
 public:
  ScanWalker(Context* c, GcWorker* w, void* object) : c(c), w(w), object(object), updates(0)
  {
  }

  virtual bool visit(unsigned offset)
  {
    ++updates;
    updateSlot(c, w, getp(object, offset), true);
    return true;
  }

  Context* c;
  GcWorker* w;
  void* object;
  uint64_t updates;
};

class FixieWalker : public Heap::Walker {
 public:
  FixieWalker(Context* c, GcWorker* w, Fixie* f) : c(c), w(w), f(f)
  {
  }

  virtual bool visit(unsigned offset)
  {
    updateFixieSlot(c, w, f, offset);
    return true;
  }

  Context* c;
  GcWorker* w;
  Fixie* f;
};

class MajorWalker : public Heap::Walker {
 public:
  MajorWalker(Context* c, GcWorker* w, void* object) : c(c), w(w), object(object)
  {
  }

  virtual bool visit(unsigned offset)
  {
    void** p = getp(object, offset);
    void* o = maskAlignedPointer(*p);
    if (o == 0) {
      return true;
    }
    if (c->gen2.contains(o)) {
      unsigned i = gen2Index(c, o);
      uintptr_t bit = static_cast<uintptr_t>(1) << bitOf(i);
      uintptr_t* word = c->markBits + wordOf(i);
      if (not(__atomic_load_n(word, __ATOMIC_RELAXED) & bit)
          and not(__atomic_fetch_or(word, bit, __ATOMIC_RELAXED) & bit)) {
        __builtin_prefetch(o);
        pushItem(c, w, reinterpret_cast<uintptr_t>(o));
      }
      return true;
    }
    if (immortalHeapContains(c, o)) {
      return true;
    }
    if (w->deferredCount + 3 > w->deferredCapacity) {
      grow(c, &w->deferred, &w->deferredCapacity, w->deferredCount + 3);
    }
    uintptr_t* d = w->deferred + w->deferredCount;
    d[0] = reinterpret_cast<uintptr_t>(p);
    d[1] = reinterpret_cast<uintptr_t>(object);
    d[2] = offset;
    w->deferredCount += 3;
    return true;
  }

  Context* c;
  GcWorker* w;
  void* object;
};

class PrefetchWalker : public Heap::Walker {
 public:
  PrefetchWalker(void* object) : object(object)
  {
  }

  virtual bool visit(unsigned offset)
  {
    __builtin_prefetch(maskAlignedPointer(*getp(object, offset)));
    return true;
  }

  void* object;
};

void scanItem(Context* c, GcWorker* w, uintptr_t item)
{
  if (w->par->major) {
    MajorWalker walker(c, w, reinterpret_cast<void*>(item));
    c->client->walk(walker.object, &walker);
    return;
  }
  if (item & 1) {
    Fixie* f = fixie(reinterpret_cast<void*>(item & ~static_cast<uintptr_t>(1)));
    FixieWalker walker(c, w, f);
    c->client->walk(f->body(), &walker);
    w->par->lock.lock();
    f->move(c, &(c->visitedFixies));
    w->par->lock.unlock();
  } else {
    PrefetchWalker pre(reinterpret_cast<void*>(item));
    c->client->walk(pre.object, &pre);
    ScanWalker walker(c, w, reinterpret_cast<void*>(item));
    c->client->walk(walker.object, &walker);
    w->updates += walker.updates;
  }
}

void donate(Context* c, GcWorker* w)
{
  ParGc* g = w->par;
  Chunk* chunk = 0;
  g->lock.lock();
  if (g->freeChunks) {
    chunk = g->freeChunks;
    g->freeChunks = chunk->next;
  }
  g->lock.unlock();
  if (chunk == 0) {
    chunk = static_cast<Chunk*>(c->system->tryAllocate(sizeof(Chunk)));
    if (chunk == 0) {
      return;
    }
  }
  chunk->n = ChunkItems;
  memcpy(chunk->items, w->stack, ChunkItems * sizeof(uintptr_t));
  memmove(w->stack, w->stack + ChunkItems, (w->count - ChunkItems) * sizeof(uintptr_t));
  w->count -= ChunkItems;
  g->lock.lock();
  chunk->next = g->pool;
  g->pool = chunk;
  __atomic_store_n(&g->poolCount, g->poolCount + 1, __ATOMIC_RELEASE);
  g->lock.unlock();
}

void drainLocal(Context* c, GcWorker* w)
{
  ParGc* g = w->par;
  while (w->count) {
    uintptr_t item = w->stack[--w->count];
    scanItem(c, w, item);
    if (w->count >= DonateThreshold and g->workerCount > 1
        and __atomic_load_n(&g->poolCount, __ATOMIC_RELAXED) < g->workerCount - 1) {
      donate(c, w);
    }
  }
}

inline void maybeDonate(Context* c, GcWorker* w)
{
  ParGc* g = w->par;
  while (w->count >= DonateThreshold and g->workerCount > 1
         and __atomic_load_n(&g->poolCount, __ATOMIC_RELAXED) < g->workerCount - 1) {
    donate(c, w);
  }
}

void scanRegion(Context* c, GcWorker* w, unsigned region)
{
  uintptr_t* heapData = c->heapMap.data;
  uintptr_t* pageData = c->pageMap.data;
  uintptr_t* pointerData = c->pointerMap.data;

  if (not((__atomic_load_n(heapData + wordOf(region), __ATOMIC_RELAXED) >> bitOf(region)) & 1)) {
    return;
  }
  atomicClearBit(heapData, region);

  unsigned pagesPerRegion = c->heapMap.scale / c->pageMap.scale;
  unsigned pointersPerPage = c->pageMap.scale / c->pointerMap.scale;
  unsigned firstPage = region * pagesPerRegion;
  bool regionDirty = false;

  for (unsigned pw = wordOf(firstPage); pw < wordOf(firstPage + pagesPerRegion); ++pw) {
    uintptr_t pageBits = __atomic_load_n(pageData + pw, __ATOMIC_RELAXED);
    while (pageBits) {
      unsigned pageBit = __builtin_ctzl(pageBits);
      pageBits &= pageBits - 1;
      unsigned page = indexOf(pw, pageBit);
      atomicClearBit(pageData, page);
      bool pageDirty = false;

      unsigned firstPointer = page * pointersPerPage;
      for (unsigned lw = wordOf(firstPointer); lw < wordOf(firstPointer + pointersPerPage); ++lw) {
        uintptr_t bits = __atomic_load_n(pointerData + lw, __ATOMIC_RELAXED);
        while (bits) {
          unsigned bit = __builtin_ctzl(bits);
          bits &= bits - 1;
          unsigned index = indexOf(lw, bit);
          atomicClearBit(pointerData, index);
          void** p = reinterpret_cast<void**>(c->gen2.data + index * c->pointerMap.scale);
          ++w->updates;
          updateSlot(c, w, p, false);
          if (not c->gen2.contains(*p)) {
            atomicSetBit(pointerData, index);
            pageDirty = true;
          }
        }
      }

      if (pageDirty) {
        atomicSetBit(pageData, page);
        regionDirty = true;
      }
    }
  }

  if (regionDirty) {
    atomicSetBit(heapData, region);
  }
}

// 0: nothing available now, 1: did work, 2: nothing left anywhere
int workStep(Context* c, GcWorker* w)
{
  ParGc* g = w->par;
  Chunk* chunk = 0;
  int region = -1;

  g->lock.lock();
  if (g->pool) {
    chunk = g->pool;
    g->pool = chunk->next;
    __atomic_store_n(&g->poolCount, g->poolCount - 1, __ATOMIC_RELEASE);
    ++g->busy;
    grow(c, &w->stack, &w->capacity, w->count + chunk->n);
    memcpy(w->stack + w->count, chunk->items, chunk->n * sizeof(uintptr_t));
    w->count += chunk->n;
    chunk->next = g->freeChunks;
    g->freeChunks = chunk;
  } else if (g->nextRegion < g->regionCount) {
    region = g->nextRegion++;
    ++g->busy;
  }
  bool quiet = chunk == 0 and region < 0 and g->busy == 0;
  g->lock.unlock();

  if (chunk == 0 and region < 0) {
    return quiet ? 2 : 0;
  }

  if (region >= 0) {
    scanRegion(c, w, region);
  }
  drainLocal(c, w);

  g->lock.lock();
  --g->busy;
  g->lock.unlock();
  return 1;
}

void backoff(unsigned idle)
{
  if (idle < 256) {
    for (unsigned i = 0; i < 32; ++i) {
      SpinLock::cpuRelax();
    }
  } else if (idle < 1024) {
    sched_yield();
  } else {
    timespec ts;
    ts.tv_sec = 0;
    ts.tv_nsec = 50000;
    nanosleep(&ts, 0);
  }
}

void quiesce(Context* c, GcWorker* w)
{
  ParGc* g = w->par;
  if (w->count) {
    g->lock.lock();
    ++g->busy;
    g->lock.unlock();
    drainLocal(c, w);
    g->lock.lock();
    --g->busy;
    g->lock.unlock();
  }
  unsigned idle = 0;
  for (;;) {
    int r = workStep(c, w);
    if (r == 2) {
      break;
    }
    if (r == 0) {
      backoff(idle++);
    } else {
      idle = 0;
    }
  }
}

void* helperMain(void* arg)
{
  GcWorker* w = static_cast<GcWorker*>(arg);
  ParGc* g = w->par;
  unsigned seen = 0;

  for (;;) {
    pthread_mutex_lock(&g->mu);
    while (not g->shutdown and g->epoch == seen) {
      pthread_cond_wait(&g->cv, &g->mu);
    }
    if (g->shutdown) {
      pthread_mutex_unlock(&g->mu);
      break;
    }
    seen = g->epoch;
    if (not g->active) {
      pthread_mutex_unlock(&g->mu);
      continue;
    }
    ++g->helpersIn;
    pthread_mutex_unlock(&g->mu);

    unsigned idle = 0;
    while (__atomic_load_n(&g->active, __ATOMIC_ACQUIRE)) {
      int r = workStep(g->c, w);
      if (r == 1) {
        idle = 0;
      } else {
        backoff(idle++);
      }
    }

    pthread_mutex_lock(&g->mu);
    --g->helpersIn;
    pthread_mutex_unlock(&g->mu);
  }
  return 0;
}

ParGc* makeParGc(Context* c, unsigned threads)
{
  ParGc* g = static_cast<ParGc*>(c->system->tryAllocate(sizeof(ParGc)));
  if (g == 0) {
    return 0;
  }
  memset(static_cast<void*>(g), 0, sizeof(ParGc));
  new (&g->lock) SpinLock();
  g->c = c;
  g->workerCount = threads;
  pthread_mutex_init(&g->mu, 0);
  pthread_cond_init(&g->cv, 0);
  for (unsigned i = 0; i < threads; ++i) {
    GcWorker* w = static_cast<GcWorker*>(c->system->tryAllocate(sizeof(GcWorker)));
    if (w == 0) {
      return 0;
    }
    memset(static_cast<void*>(w), 0, sizeof(GcWorker));
    w->par = g;
    w->id = i;
    g->workers[i] = w;
  }
  return g;
}

void startHelpers(ParGc* g)
{
  if (g->started) {
    return;
  }
  g->started = true;
  pthread_attr_t attr;
  pthread_attr_init(&attr);
  pthread_attr_setstacksize(&attr, 1024 * 1024);
  for (unsigned i = 1; i < g->workerCount; ++i) {
    if (pthread_create(&g->workers[i]->thread, &attr, helperMain, g->workers[i]) != 0) {
      g->workerCount = i;
      break;
    }
  }
  pthread_attr_destroy(&attr);
}

void disposeParGc(Context* c)
{
  ParGc* g = c->par;
  if (g == 0) {
    return;
  }
  if (g->started) {
    pthread_mutex_lock(&g->mu);
    g->shutdown = true;
    pthread_cond_broadcast(&g->cv);
    pthread_mutex_unlock(&g->mu);
    for (unsigned i = 1; i < g->workerCount; ++i) {
      pthread_join(g->workers[i]->thread, 0);
    }
  }
  for (Chunk* ch = g->pool; ch;) {
    Chunk* n = ch->next;
    c->system->free(ch);
    ch = n;
  }
  for (Chunk* ch = g->freeChunks; ch;) {
    Chunk* n = ch->next;
    c->system->free(ch);
    ch = n;
  }
  for (unsigned i = 0; i < g->workerCount; ++i) {
    if (g->workers[i]->stack) {
      c->system->free(g->workers[i]->stack);
    }
    if (g->workers[i]->deferred) {
      c->system->free(g->workers[i]->deferred);
    }
    c->system->free(g->workers[i]);
  }
  c->system->free(g);
  c->par = 0;
}

void beginRegion(ParGc* g)
{
  startHelpers(g);
  pthread_mutex_lock(&g->mu);
  g->active = 1;
  ++g->epoch;
  pthread_cond_broadcast(&g->cv);
  pthread_mutex_unlock(&g->mu);
}

void endRegion(Context* c, ParGc* g)
{
  pthread_mutex_lock(&g->mu);
  __atomic_store_n(&g->active, 0, __ATOMIC_RELEASE);
  pthread_mutex_unlock(&g->mu);
  for (;;) {
    pthread_mutex_lock(&g->mu);
    int in = g->helpersIn;
    pthread_mutex_unlock(&g->mu);
    if (in == 0) {
      break;
    }
    sched_yield();
  }
  for (unsigned i = 0; i < g->workerCount; ++i) {
    GcWorker* w = g->workers[i];
    addTailSpan(c, w->cur, w->end);
    w->cur = w->end = 0;
    c->statObjects += w->objects;
    c->statWords += w->words;
    c->statUpdates += w->updates;
    w->objects = w->words = w->updates = 0;
  }
}

void visitDirtyFixiesParallel(Context* c, GcWorker* w)
{
  Fixie** p = &(c->dirtyTenuredFixies);
  while (*p) {
    Fixie* f = *p;
    bool clean = true;
    uintptr_t* mask = f->mask();

    unsigned word = 0;
    unsigned bit = 0;
    unsigned wordLimit = wordOf(f->size);
    unsigned bitLimit = bitOf(f->size);

    for (; word <= wordLimit and (word < wordLimit or bit < bitLimit); ++word) {
      if (mask[word]) {
        for (; bit < BitsPerWord and (word < wordLimit or bit < bitLimit); ++bit) {
          unsigned index = indexOf(word, bit);

          if (getBit(mask, index)) {
            clearBit(mask, index);
            updateFixieSlot(c, w, f, index);
            maybeDonate(c, w);

            if (getBit(mask, index)) {
              clean = false;
            }
          }
        }
        bit = 0;
      }
    }

    if (clean) {
      markClean(c, f);
    } else {
      p = &(f->next);
    }
  }
}

class ParallelVisitor : public Heap::Visitor {
 public:
  ParallelVisitor(Context* c, GcWorker* w) : c(c), w(w)
  {
  }

  virtual void visit(void* p)
  {
    void** slot = static_cast<void**>(p);
    updateSlot(c, w, slot, c->gen2.contains(slot));
    if (c->deferDrain) {
      maybeDonate(c, w);
    } else {
      quiesce(c, w);
    }
  }

  virtual void flush()
  {
    quiesce(c, w);
  }

  Context* c;
  GcWorker* w;
};

void collectMinorParallel(Context* c)
{
  ParGc* g = c->par;
  GcWorker* w = g->workers[0];

  beginRegion(g);

  int64_t t0 = c->logMajor ? nowNs() : 0;
  unsigned limit = c->gen2.position();
  if (limit) {
    g->lock.lock();
    g->nextRegion = 0;
    g->regionCount = ceilingDivide(limit, c->heapMap.scale);
    g->lock.unlock();
    quiesce(c, w);
  }
  int64_t t1 = c->logMajor ? nowNs() : 0;

  visitDirtyFixiesParallel(c, w);
  quiesce(c, w);
  int64_t t2 = c->logMajor ? nowNs() : 0;

  ParallelVisitor v(c, w);
  c->deferDrain = true;
  c->parWorker = w;
  c->client->visitRoots(&v);
  c->deferDrain = false;
  quiesce(c, w);

  endRegion(c, g);

  if (c->logMajor) {
    int64_t t3 = nowNs();
    c->statT[0] = t1 - t0;
    c->statT[1] = t2 - t1;
    c->statT[2] = t3 - t2;
  }
}

void drainMarkStackParallel(Context* c)
{
  ParGc* g = c->par;
  GcWorker* w0 = g->workers[0];

  int64_t tp0 = c->logMajor ? nowNs() : 0;
  g->major = true;
  beginRegion(g);
  while (c->markStackCount) {
    pushItem(c, w0, reinterpret_cast<uintptr_t>(c->markStack[--c->markStackCount]));
  }
  quiesce(c, w0);
  endRegion(c, g);
  g->major = false;
  if (c->logMajor) c->statParallelNs += nowNs() - tp0;

  for (unsigned i = 0; i < g->workerCount; ++i) {
    GcWorker* w = g->workers[i];
    for (unsigned j = 0; j < w->deferredCount; j += 3) {
      uintptr_t* d = w->deferred + j;
      local::collect(c, reinterpret_cast<void**>(d[0]), reinterpret_cast<void*>(d[1]), static_cast<unsigned>(d[2]));
    }
    c->statParallelDeferred += w->deferredCount / 3;
    w->deferredCount = 0;
  }
  ++c->statParallelRounds;
}

#endif  // SHIM_HEAP_PARALLEL
