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
import java.nio.file.LinkOption;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.HashMap;
import java.util.Map;
import shim.FileNative;
import shim.NioNative;

class LinuxWatchService extends AbstractWatchService {
  private static final int kInModify = 0x00000002;
  private static final int kInAttrib = 0x00000004;
  private static final int kInMovedFrom = 0x00000040;
  private static final int kInMovedTo = 0x00000080;
  private static final int kInCreate = 0x00000100;
  private static final int kInDelete = 0x00000200;
  private static final int kInDeleteSelf = 0x00000400;
  private static final int kInMoveSelf = 0x00000800;
  private static final int kInQOverflow = 0x00004000;
  private static final int kInIgnored = 0x00008000;
  private static final int kInOnlyDir = 0x01000000;
  private static final int kInDontFollow = 0x02000000;

  private final int ifd;
  private final int[] wake = new int[2];
  private final Map<Integer, LinuxWatchKey> keys = new HashMap<Integer, LinuxWatchKey>();
  private final Map<Object, Integer> byDirectory = new HashMap<Object, Integer>();
  private final Thread poller;

  LinuxWatchService() throws IOException {
    int fd = NioNative.inotifyInit();
    if (fd < 0) {
      throw new IOException(FileNative.message(fd));
    }
    ifd = fd;
    if (NioNative.pipe(wake) < 0) {
      FileNative.close(ifd);
      throw new IOException("Unable to create watch service");
    }
    poller = new Thread(new Runnable() {
      @Override
      public void run() {
        loop();
      }
    });
    poller.setDaemon(true);
    poller.start();
  }

  private void loop() {
    byte[] buf = new byte[8192];
    for (;;) {
      int n = NioNative.inotifyRead(ifd, wake[0], buf);
      if (n <= 0) {
        return;
      }
      int pos = 0;
      while (pos + 16 <= n) {
        int wd = readInt(buf, pos);
        int mask = readInt(buf, pos + 4);
        int len = readInt(buf, pos + 12);
        String name = null;
        if (len > 0) {
          int end = pos + 16;
          int limit = end + len;
          while (end < limit && buf[end] != 0) {
            end++;
          }
          name = new String(buf, pos + 16, end - (pos + 16), java.nio.charset.StandardCharsets.UTF_8);
        }
        pos += 16 + len;
        LinuxWatchKey key;
        synchronized (keys) {
          key = keys.get(wd);
        }
        if (key != null) {
          key.handle(mask, name);
        }
      }
    }
  }

  private static int readInt(byte[] b, int off) {
    return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8) | ((b[off + 2] & 0xff) << 16) | ((b[off + 3] & 0xff) << 24);
  }

  @Override
  WatchKey register(Path obj, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
    UnixPath dir = UnixPath.toUnixPath(obj);
    int mask = 0;
    for (WatchEvent.Kind<?> event : events) {
      if (event == StandardWatchEventKinds.ENTRY_CREATE) {
        mask |= (kInCreate | kInMovedTo);
        continue;
      }
      if (event == StandardWatchEventKinds.ENTRY_DELETE) {
        mask |= (kInDelete | kInMovedFrom);
        continue;
      }
      if (event == StandardWatchEventKinds.ENTRY_MODIFY) {
        mask |= (kInModify | kInAttrib);
        continue;
      }
      if (event == StandardWatchEventKinds.OVERFLOW) {
        continue;
      }
      if (event == null) {
        throw new NullPointerException("An element in event set is 'null'");
      }
      throw new UnsupportedOperationException(event.name());
    }
    if (mask == 0) {
      throw new IllegalArgumentException("No events to register");
    }
    for (WatchEvent.Modifier modifier : modifiers) {
      if (modifier == null) {
        throw new NullPointerException();
      }
      throw new UnsupportedOperationException("Modifier not supported");
    }
    if (!isOpen()) {
      throw new ClosedWatchServiceException();
    }
    int wd = NioNative.inotifyAddWatch(ifd, dir.getPathForSysCalls(), mask | kInOnlyDir);
    if (wd < 0) {
      if (-wd == UnixConstants.ENOTDIR) {
        throw new NotDirectoryException(dir.getPathForExceptionMessage());
      }
      new UnixException(-wd).rethrowAsIOException(dir);
    }
    synchronized (keys) {
      LinuxWatchKey key = keys.get(wd);
      if (key == null) {
        key = new LinuxWatchKey(dir, this, wd);
        keys.put(wd, key);
      }
      key.setEvents(mask);
      return key;
    }
  }

  @Override
  void implClose() throws IOException {
    synchronized (keys) {
      for (LinuxWatchKey key : keys.values()) {
        key.invalidate();
      }
      keys.clear();
    }
    FileNative.write(wake[1], new byte[] {1}, 0, 1);
    try {
      poller.join(2000);
    } catch (InterruptedException ignore) {
    }
    FileNative.close(ifd);
    FileNative.close(wake[0]);
    FileNative.close(wake[1]);
  }

  void remove(int wd) {
    synchronized (keys) {
      keys.remove(wd);
    }
    NioNative.inotifyRmWatch(ifd, wd);
  }

  private static class LinuxWatchKey extends AbstractWatchKey {
    private final LinuxWatchService service;
    private final int wd;
    private volatile boolean valid = true;
    private volatile int mask;

    LinuxWatchKey(UnixPath dir, LinuxWatchService service, int wd) {
      super(dir, service);
      this.service = service;
      this.wd = wd;
    }

    void setEvents(int mask) {
      this.mask = mask;
    }

    void invalidate() {
      valid = false;
    }

    void handle(int m, String name) {
      if ((m & kInQOverflow) != 0) {
        signalEvent(StandardWatchEventKinds.OVERFLOW, null);
        return;
      }
      if ((m & kInIgnored) != 0) {
        valid = false;
        service.remove(wd);
        signal();
        return;
      }
      if (name == null) {
        return;
      }
      Path context = watchable().getFileSystem().getPath(name);
      if ((m & (kInMovedTo | kInCreate)) != 0 && (mask & (kInMovedTo | kInCreate)) != 0) {
        signalEvent(StandardWatchEventKinds.ENTRY_CREATE, context);
      }
      if ((m & (kInMovedFrom | kInDelete)) != 0 && (mask & (kInMovedFrom | kInDelete)) != 0) {
        signalEvent(StandardWatchEventKinds.ENTRY_DELETE, context);
      }
      if ((m & (kInAttrib | kInModify)) != 0 && (mask & (kInAttrib | kInModify)) != 0) {
        signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, context);
      }
    }

    @Override
    public boolean isValid() {
      return valid;
    }

    @Override
    public void cancel() {
      if (valid) {
        valid = false;
        service.remove(wd);
      }
    }
  }
}
