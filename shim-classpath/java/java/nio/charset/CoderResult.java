/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;

public class CoderResult {
  private static final int CR_UNDERFLOW = 0;
  private static final int CR_OVERFLOW = 1;
  private static final int CR_MALFORMED = 2;
  private static final int CR_UNMAPPABLE = 3;

  private static final String[] kNames = {"UNDERFLOW", "OVERFLOW", "MALFORMED", "UNMAPPABLE"};

  private final int type;
  private final int length;

  private CoderResult(int type, int length) {
    this.type = type;
    this.length = length;
  }

  public String toString() {
    String nm = kNames[type];
    return isError() ? nm + "[" + length + "]" : nm;
  }

  public boolean isUnderflow() {
    return type == CR_UNDERFLOW;
  }

  public boolean isOverflow() {
    return type == CR_OVERFLOW;
  }

  public boolean isError() {
    return type >= CR_MALFORMED;
  }

  public boolean isMalformed() {
    return type == CR_MALFORMED;
  }

  public boolean isUnmappable() {
    return type == CR_UNMAPPABLE;
  }

  public int length() {
    if (!isError()) {
      throw new UnsupportedOperationException();
    }
    return length;
  }

  public static final CoderResult UNDERFLOW = new CoderResult(CR_UNDERFLOW, 0);
  public static final CoderResult OVERFLOW = new CoderResult(CR_OVERFLOW, 0);

  private abstract static class Cache {
    private final Map<Integer, WeakReference<CoderResult>> cache = new HashMap<Integer, WeakReference<CoderResult>>();

    protected abstract CoderResult create(int len);

    private synchronized CoderResult get(int len) {
      if (len <= 0) {
        throw new IllegalArgumentException("Non-positive length");
      }
      Integer k = Integer.valueOf(len);
      WeakReference<CoderResult> w = cache.get(k);
      CoderResult e = w == null ? null : w.get();
      if (e == null) {
        e = create(len);
        cache.put(k, new WeakReference<CoderResult>(e));
      }
      return e;
    }
  }

  private static final Cache kMalformedCache = new Cache() {
    public CoderResult create(int len) {
      return new CoderResult(CR_MALFORMED, len);
    }
  };

  public static CoderResult malformedForLength(int length) {
    return kMalformedCache.get(length);
  }

  private static final Cache kUnmappableCache = new Cache() {
    public CoderResult create(int len) {
      return new CoderResult(CR_UNMAPPABLE, len);
    }
  };

  public static CoderResult unmappableForLength(int length) {
    return kUnmappableCache.get(length);
  }

  public void throwException() throws CharacterCodingException {
    switch (type) {
    case CR_UNDERFLOW:
      throw new BufferUnderflowException();
    case CR_OVERFLOW:
      throw new BufferOverflowException();
    case CR_MALFORMED:
      throw new MalformedInputException(length);
    case CR_UNMAPPABLE:
      throw new UnmappableCharacterException(length);
    default:
      throw new AssertionError();
    }
  }
}
