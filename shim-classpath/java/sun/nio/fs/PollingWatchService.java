/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

class PollingWatchService extends AbstractWatchService {
  private final Map<Object, PollingWatchKey> map = new HashMap<Object, PollingWatchKey>();
  private final ScheduledExecutorService scheduledExecutor;

  PollingWatchService() {
    scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      }
    });
  }

  @Override
  WatchKey register(final Path path, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers)
      throws IOException {
    final Set<WatchEvent.Kind<?>> eventSet = new java.util.HashSet<WatchEvent.Kind<?>>(kinds.length);
    for (WatchEvent.Kind<?> kind : kinds) {
      if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY
          || kind == StandardWatchEventKinds.ENTRY_DELETE) {
        eventSet.add(kind);
        continue;
      }
      if (kind == StandardWatchEventKinds.OVERFLOW) {
        continue;
      }
      if (kind == null) {
        throw new NullPointerException("An element in event set is 'null'");
      }
      throw new UnsupportedOperationException(kind.name());
    }
    if (eventSet.isEmpty()) {
      throw new IllegalArgumentException("No events to register");
    }
    int sensitivity = 10;
    for (WatchEvent.Modifier modifier : modifiers) {
      if (modifier == null) {
        throw new NullPointerException();
      }
      throw new UnsupportedOperationException("Modifier not supported");
    }
    if (!isOpen()) {
      throw new ClosedWatchServiceException();
    }
    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
    if (!attrs.isDirectory()) {
      throw new NotDirectoryException(path.toString());
    }
    Object fileKey = attrs.fileKey();
    if (fileKey == null) {
      throw new AssertionError("File keys must be supported");
    }
    synchronized (closeLock()) {
      if (!isOpen()) {
        throw new ClosedWatchServiceException();
      }
      PollingWatchKey watchKey;
      synchronized (map) {
        watchKey = map.get(fileKey);
        if (watchKey == null) {
          watchKey = new PollingWatchKey(path, this, fileKey);
          map.put(fileKey, watchKey);
        } else {
          watchKey.disable();
        }
      }
      watchKey.enable(eventSet, sensitivity);
      return watchKey;
    }
  }

  @Override
  void implClose() throws IOException {
    synchronized (map) {
      for (Map.Entry<Object, PollingWatchKey> entry : map.entrySet()) {
        PollingWatchKey watchKey = entry.getValue();
        watchKey.disable();
        watchKey.invalidate();
      }
      map.clear();
    }
    scheduledExecutor.shutdown();
  }

  private class PollingWatchKey extends AbstractWatchKey {
    private final Object fileKey;
    private Set<? extends WatchEvent.Kind<?>> events;
    private ScheduledFuture<?> poller;
    private volatile boolean valid;
    private int tickCount;
    private Map<Path, CacheEntry> entries;

    PollingWatchKey(Path dir, PollingWatchService watchService, Object fileKey) throws IOException {
      super(dir, watchService);
      this.fileKey = fileKey;
      this.valid = true;
      this.tickCount = 0;
      this.entries = new HashMap<Path, CacheEntry>();
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
        for (Path entry : stream) {
          long lastModified = Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
          entries.put(entry.getFileName(), new CacheEntry(lastModified, tickCount));
        }
      } catch (java.nio.file.DirectoryIteratorException e) {
        throw e.getCause();
      }
    }

    void enable(Set<? extends WatchEvent.Kind<?>> events, long period) {
      synchronized (this) {
        this.events = events;
        Runnable thunk = new Runnable() {
          @Override
          public void run() {
            poll();
          }
        };
        this.poller = scheduledExecutor.scheduleAtFixedRate(thunk, period, period, TimeUnit.SECONDS);
      }
    }

    void disable() {
      synchronized (this) {
        if (poller != null) {
          poller.cancel(false);
        }
      }
    }

    @Override
    public boolean isValid() {
      return valid;
    }

    void invalidate() {
      valid = false;
    }

    @Override
    public void cancel() {
      valid = false;
      synchronized (map) {
        map.remove(fileKey);
      }
      disable();
    }

    synchronized void poll() {
      if (!valid) {
        return;
      }
      tickCount++;
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(watchable())) {
        for (Path entry : stream) {
          long lastModified;
          try {
            lastModified = Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
          } catch (IOException x) {
            continue;
          }
          CacheEntry e = entries.get(entry.getFileName());
          if (e == null) {
            entries.put(entry.getFileName(), new CacheEntry(lastModified, tickCount));
            if (events.contains(StandardWatchEventKinds.ENTRY_CREATE)) {
              signalEvent(StandardWatchEventKinds.ENTRY_CREATE, entry.getFileName());
              continue;
            } else {
              if (events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
                signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, entry.getFileName());
              }
            }
            continue;
          }
          if (e.lastModified != lastModified) {
            if (events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
              signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, entry.getFileName());
            }
          }
          e.update(lastModified, tickCount);
        }
      } catch (IOException | java.nio.file.DirectoryIteratorException x) {
        cancel();
        signal();
        return;
      }
      java.util.Iterator<Map.Entry<Path, CacheEntry>> i = entries.entrySet().iterator();
      while (i.hasNext()) {
        Map.Entry<Path, CacheEntry> mapEntry = i.next();
        CacheEntry entry = mapEntry.getValue();
        if (entry.lastTickCount() != tickCount) {
          Path name = mapEntry.getKey();
          i.remove();
          if (events.contains(StandardWatchEventKinds.ENTRY_DELETE)) {
            signalEvent(StandardWatchEventKinds.ENTRY_DELETE, name);
          }
        }
      }
    }
  }

  private static class CacheEntry {
    private long lastModified;
    private int lastTickCount;

    CacheEntry(long lastModified, int lastTickCount) {
      this.lastModified = lastModified;
      this.lastTickCount = lastTickCount;
    }

    int lastTickCount() {
      return lastTickCount;
    }

    void update(long lastModified, int tickCount) {
      this.lastModified = lastModified;
      this.lastTickCount = tickCount;
    }
  }
}
