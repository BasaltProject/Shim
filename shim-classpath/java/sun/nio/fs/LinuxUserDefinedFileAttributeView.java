/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import shim.NioNative;

final class LinuxUserDefinedFileAttributeView implements UserDefinedFileAttributeView, DynamicFileAttributeView {
  private static final String kUserNamespace = "user.";

  private final UnixPath file;
  private final boolean followLinks;

  LinuxUserDefinedFileAttributeView(UnixPath file, boolean followLinks) {
    this.file = file;
    this.followLinks = followLinks;
  }

  @Override
  public String name() {
    return "user";
  }

  private List<String> list0() throws IOException {
    int size = NioNative.listxattr(file.getPathForSysCalls(), null, followLinks);
    if (size < 0) {
      new UnixException(-size).rethrowAsIOException(file);
    }
    byte[] buf = new byte[size];
    if (size > 0) {
      int r = NioNative.listxattr(file.getPathForSysCalls(), buf, followLinks);
      if (r < 0) {
        new UnixException(-r).rethrowAsIOException(file);
      }
    }
    List<String> list = new ArrayList<String>();
    int start = 0;
    for (int i = 0; i < size; i++) {
      if (buf[i] == 0) {
        String s = new String(buf, start, i - start, java.nio.charset.StandardCharsets.UTF_8);
        if (s.startsWith(kUserNamespace)) {
          list.add(s.substring(kUserNamespace.length()));
        }
        start = i + 1;
      }
    }
    return list;
  }

  @Override
  public List<String> list() throws IOException {
    return Collections.unmodifiableList(list0());
  }

  private static String key(String name) {
    if (name == null) {
      throw new NullPointerException("'name' is null");
    }
    return kUserNamespace + name;
  }

  @Override
  public int size(String name) throws IOException {
    int r = NioNative.getxattr(file.getPathForSysCalls(), key(name), null, 0, 0, followLinks);
    if (r < 0) {
      new UnixException(-r).rethrowAsIOException(file);
    }
    return r;
  }

  @Override
  public int read(String name, ByteBuffer dst) throws IOException {
    if (dst.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    }
    int pos = dst.position();
    int lim = dst.limit();
    int rem = (pos <= lim ? lim - pos : 0);
    byte[] buf = new byte[rem];
    int r = NioNative.getxattr(file.getPathForSysCalls(), key(name), buf, 0, rem, followLinks);
    if (r < 0) {
      if (-r == UnixConstants.ERANGE) {
        throw new java.nio.file.FileSystemException(file.getPathForExceptionMessage(), null,
            "Unable to read extended attribute '" + name + "': " + "Required buffer size too large"
            + " (remaining: " + rem + ")");
      }
      new UnixException(-r).rethrowAsIOException(file);
    }
    dst.put(buf, 0, r);
    return r;
  }

  @Override
  public int write(String name, ByteBuffer src) throws IOException {
    int pos = src.position();
    int lim = src.limit();
    int rem = (pos <= lim ? lim - pos : 0);
    byte[] buf = new byte[rem];
    src.get(buf);
    int r = NioNative.setxattr(file.getPathForSysCalls(), key(name), buf, 0, rem);
    if (r < 0) {
      new UnixException(-r).rethrowAsIOException(file);
    }
    return rem;
  }

  @Override
  public void delete(String name) throws IOException {
    int r = NioNative.removexattr(file.getPathForSysCalls(), key(name));
    if (r < 0) {
      new UnixException(-r).rethrowAsIOException(file);
    }
  }

  @Override
  public void setAttribute(String attribute, Object value) throws IOException {
    ByteBuffer bb;
    if (value instanceof byte[]) {
      bb = ByteBuffer.wrap((byte[]) value);
    } else {
      bb = (ByteBuffer) value;
    }
    write(attribute, bb);
  }

  @Override
  public Map<String, Object> readAttributes(String[] requested) throws IOException {
    List<String> names = new ArrayList<String>();
    for (String name : requested) {
      if (name.equals("*")) {
        names = list0();
        break;
      } else {
        if (name.isEmpty()) {
          throw new IllegalArgumentException();
        }
        names.add(name);
      }
    }
    Map<String, Object> result = new HashMap<String, Object>();
    for (String name : names) {
      int size = size(name);
      byte[] buf = new byte[size];
      int n = read(name, ByteBuffer.wrap(buf));
      byte[] value = (n == size) ? buf : Arrays.copyOf(buf, n);
      result.put(name, value);
    }
    return result;
  }
}
