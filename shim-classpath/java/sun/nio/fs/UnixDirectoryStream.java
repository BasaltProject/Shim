/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import shim.NioNative;

class UnixDirectoryStream implements DirectoryStream<Path> {
  private final UnixPath dir;
  private final DirectoryStream.Filter<? super Path> filter;
  private final String[] names;
  private volatile boolean isClosed;
  private Iterator<Path> iterator;

  UnixDirectoryStream(UnixPath dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
    this.dir = dir;
    this.filter = filter;
    int[] err = new int[1];
    String[] list = NioNative.listdir(dir.getPathForSysCalls(), err);
    if (list == null) {
      int errno = -err[0];
      if (errno == UnixConstants.ENOTDIR) {
        throw new NotDirectoryException(dir.getPathForExceptionMessage());
      }
      new UnixException(errno).rethrowAsIOException(dir);
    }
    this.names = list;
  }

  final UnixPath directory() {
    return dir;
  }

  final boolean isOpen() {
    return !isClosed;
  }

  @Override
  public void close() throws IOException {
    isClosed = true;
  }

  @Override
  public Iterator<Path> iterator() {
    if (isClosed) {
      throw new IllegalStateException("Directory stream is closed");
    }
    synchronized (this) {
      if (iterator != null) {
        throw new IllegalStateException("Iterator already obtained");
      }
      iterator = new UnixDirectoryIterator();
      return iterator;
    }
  }

  private class UnixDirectoryIterator implements Iterator<Path> {
    private int index;
    private Path nextEntry;
    private boolean atEof;

    private Path readNextEntry() {
      for (;;) {
        if (isClosed || index >= names.length) {
          atEof = true;
          return null;
        }
        Path entry = dir.resolve(new UnixPath(dir.getFileSystem(), names[index++]));
        try {
          if (filter == null || filter.accept(entry)) {
            return entry;
          }
        } catch (IOException ioe) {
          throw new DirectoryIteratorException(ioe);
        }
      }
    }

    @Override
    public synchronized boolean hasNext() {
      if (nextEntry == null && !atEof) {
        nextEntry = readNextEntry();
      }
      return nextEntry != null;
    }

    @Override
    public synchronized Path next() {
      Path result;
      if (nextEntry == null && !atEof) {
        result = readNextEntry();
      } else {
        result = nextEntry;
        nextEntry = null;
      }
      if (result == null) {
        throw new NoSuchElementException();
      }
      return result;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
