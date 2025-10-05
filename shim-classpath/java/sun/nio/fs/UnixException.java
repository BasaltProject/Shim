/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import shim.FileNative;

class UnixException extends Exception {
  static final long serialVersionUID = 7227016794320723218L;

  private int errno;
  private String msg;

  UnixException(int errno) {
    this.errno = errno;
    this.msg = null;
  }

  UnixException(String msg) {
    this.errno = 0;
    this.msg = msg;
  }

  int errno() {
    return errno;
  }

  void setError(int errno) {
    this.errno = errno;
    this.msg = null;
  }

  String errorString() {
    if (errno == 0) {
      return msg;
    } else {
      return FileNative.strerror(errno);
    }
  }

  @Override
  public String getMessage() {
    return errorString();
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }

  private IOException translateToIOException(String file, String other) {
    if (msg != null) {
      return new IOException(msg);
    }
    if (errno() == UnixConstants.EACCES) {
      return new AccessDeniedException(file, other, null);
    }
    if (errno() == UnixConstants.ENOENT) {
      return new NoSuchFileException(file, other, null);
    }
    if (errno() == UnixConstants.EEXIST) {
      return new FileAlreadyExistsException(file, other, null);
    }
    if (errno() == UnixConstants.ELOOP) {
      return new FileSystemException(file, other, errorString() + " or unable to access attributes of symbolic link");
    }
    return new FileSystemException(file, other, errorString());
  }

  void rethrowAsIOException(String file) throws IOException {
    IOException x = translateToIOException(file, null);
    throw x;
  }

  void rethrowAsIOException(UnixPath file, UnixPath other) throws IOException {
    String a = (file == null) ? null : file.getPathForExceptionMessage();
    String b = (other == null) ? null : other.getPathForExceptionMessage();
    IOException x = translateToIOException(a, b);
    throw x;
  }

  void rethrowAsIOException(UnixPath file) throws IOException {
    rethrowAsIOException(file, null);
  }

  IOException asIOException(UnixPath file) {
    return translateToIOException(file.getPathForExceptionMessage(), null);
  }
}
