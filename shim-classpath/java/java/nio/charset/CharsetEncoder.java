/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Objects;

public abstract class CharsetEncoder {
  private static final int ST_RESET = 0;
  private static final int ST_CODING = 1;
  private static final int ST_END = 2;
  private static final int ST_FLUSHED = 3;

  private final Charset charset;
  private final float averageBytesPerChar;
  private final float maxBytesPerChar;
  private byte[] replacement = new byte[] { (byte) '?' };
  private CodingErrorAction malformedInputAction = CodingErrorAction.REPORT;
  private CodingErrorAction unmappableCharacterAction = CodingErrorAction.REPORT;
  private int state = ST_RESET;

  protected CharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar) {
    this(cs, averageBytesPerChar, maxBytesPerChar, new byte[] { (byte) '?' });
  }

  protected CharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar, byte[] replacement) {
    if (averageBytesPerChar <= 0.0f || maxBytesPerChar <= 0.0f) {
      throw new IllegalArgumentException("Non-positive estimates");
    }
    if (averageBytesPerChar > maxBytesPerChar) {
      throw new IllegalArgumentException("averageBytesPerChar > maxBytesPerChar");
    }
    this.charset = cs;
    this.averageBytesPerChar = averageBytesPerChar;
    this.maxBytesPerChar = maxBytesPerChar;
    replaceWith(replacement);
  }

  public final Charset charset() {
    return charset;
  }

  public final byte[] replacement() {
    return Arrays.copyOf(replacement, replacement.length);
  }

  public final CharsetEncoder replaceWith(byte[] newReplacement) {
    Objects.requireNonNull(newReplacement, "newReplacement");
    if (newReplacement.length == 0 || newReplacement.length > maxBytesPerChar) {
      throw new IllegalArgumentException("Illegal replacement length");
    }
    this.replacement = Arrays.copyOf(newReplacement, newReplacement.length);
    implReplaceWith(this.replacement);
    return this;
  }

  protected void implReplaceWith(byte[] newReplacement) {
  }

  public boolean isLegalReplacement(byte[] repl) {
    return repl != null && repl.length > 0 && repl.length <= maxBytesPerChar;
  }

  public CodingErrorAction malformedInputAction() {
    return malformedInputAction;
  }

  public final CharsetEncoder onMalformedInput(CodingErrorAction newAction) {
    this.malformedInputAction = Objects.requireNonNull(newAction, "newAction");
    implOnMalformedInput(newAction);
    return this;
  }

  protected void implOnMalformedInput(CodingErrorAction newAction) {
  }

  public CodingErrorAction unmappableCharacterAction() {
    return unmappableCharacterAction;
  }

  public final CharsetEncoder onUnmappableCharacter(CodingErrorAction newAction) {
    this.unmappableCharacterAction = Objects.requireNonNull(newAction, "newAction");
    implOnUnmappableCharacter(newAction);
    return this;
  }

  protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
  }

  public final float averageBytesPerChar() {
    return averageBytesPerChar;
  }

  public final float maxBytesPerChar() {
    return maxBytesPerChar;
  }

  public final CoderResult encode(CharBuffer in, ByteBuffer out, boolean endOfInput) {
    int newState = endOfInput ? ST_END : ST_CODING;
    if (state != ST_RESET && state != ST_CODING && !(endOfInput && state == ST_END)) {
      throw new IllegalStateException("Current state = " + state);
    }
    state = newState;

    while (true) {
      CoderResult cr;
      try {
        cr = encodeLoop(in, out);
      } catch (BufferOverflowException ex) {
        throw ex;
      } catch (RuntimeException ex) {
        throw new CoderMalfunctionError(ex);
      }

      if (cr.isOverflow()) {
        return cr;
      }

      if (cr.isUnderflow()) {
        if (endOfInput && in.hasRemaining()) {
          cr = CoderResult.malformedForLength(in.remaining());
        } else {
          return cr;
        }
      }

      CodingErrorAction action = cr.isMalformed() ? malformedInputAction : unmappableCharacterAction;
      if (action == CodingErrorAction.REPORT) {
        return cr;
      }
      if (action == CodingErrorAction.REPLACE) {
        if (out.remaining() < replacement.length) {
          return CoderResult.OVERFLOW;
        }
        out.put(replacement);
      }
      in.position(in.position() + cr.length());
    }
  }

  public final CoderResult flush(ByteBuffer out) {
    if (state == ST_END) {
      CoderResult cr = implFlush(out);
      if (cr.isUnderflow()) {
        state = ST_FLUSHED;
      }
      return cr;
    }
    if (state != ST_FLUSHED) {
      throw new IllegalStateException("Current state = " + state);
    }
    return CoderResult.UNDERFLOW;
  }

  protected CoderResult implFlush(ByteBuffer out) {
    return CoderResult.UNDERFLOW;
  }

  public final CharsetEncoder reset() {
    implReset();
    state = ST_RESET;
    return this;
  }

  protected void implReset() {
  }

  protected abstract CoderResult encodeLoop(CharBuffer in, ByteBuffer out);

  public final ByteBuffer encode(CharBuffer in) throws CharacterCodingException {
    int n = (int) (in.remaining() * averageBytesPerChar());
    ByteBuffer out = ByteBuffer.allocate(Math.max(16, n));
    reset();
    while (true) {
      CoderResult cr = in.hasRemaining() ? encode(in, out, true) : flush(out);
      if (cr.isUnderflow()) {
        break;
      }
      if (cr.isOverflow()) {
        n = 2 * n + 1;
        ByteBuffer o = ByteBuffer.allocate(n);
        out.flip();
        o.put(out);
        out = o;
        continue;
      }
      cr.throwException();
    }
    out.flip();
    return out;
  }

  public boolean canEncode(char c) {
    CharBuffer cb = CharBuffer.allocate(1);
    cb.put(c);
    cb.flip();
    ByteBuffer bb = ByteBuffer.allocate((int) maxBytesPerChar());
    reset();
    CoderResult cr = encode(cb, bb, true);
    return !cr.isError();
  }

  public boolean canEncode(CharSequence cs) {
    CharBuffer cb = CharBuffer.wrap(cs);
    ByteBuffer bb = ByteBuffer.allocate((int) (cs.length() * maxBytesPerChar()));
    reset();
    CoderResult cr = encode(cb, bb, true);
    return !cr.isError();
  }
}
