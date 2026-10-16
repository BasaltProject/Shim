/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public abstract class CharsetDecoder {
  private final Charset charset;
  private final float averageCharsPerByte;
  private final float maxCharsPerByte;

  private String replacement;
  private CodingErrorAction malformedInputAction = CodingErrorAction.REPORT;
  private CodingErrorAction unmappableCharacterAction = CodingErrorAction.REPORT;

  private static final int ST_RESET = 0;
  private static final int ST_CODING = 1;
  private static final int ST_END = 2;
  private static final int ST_FLUSHED = 3;

  private static final String[] kStateNames = {"RESET", "CODING", "CODING_END", "FLUSHED"};

  private int state = ST_RESET;

  private CharsetDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte, String replacement,
      boolean validate) {
    this.charset = cs;
    if (averageCharsPerByte <= 0.0f) {
      throw new IllegalArgumentException("Non-positive averageCharsPerByte");
    }
    if (maxCharsPerByte <= 0.0f) {
      throw new IllegalArgumentException("Non-positive maxCharsPerByte");
    }
    if (averageCharsPerByte > maxCharsPerByte) {
      throw new IllegalArgumentException("averageCharsPerByte exceeds maxCharsPerByte");
    }
    this.replacement = replacement;
    this.averageCharsPerByte = averageCharsPerByte;
    this.maxCharsPerByte = maxCharsPerByte;
  }

  protected CharsetDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte, String replacement) {
    this(cs, averageCharsPerByte, maxCharsPerByte, replacement, true);
    replaceWith(replacement);
  }

  protected CharsetDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte) {
    this(cs, averageCharsPerByte, maxCharsPerByte, "�");
  }

  public final Charset charset() {
    return charset;
  }

  public final String replacement() {
    return replacement;
  }

  public final CharsetDecoder replaceWith(String newReplacement) {
    if (newReplacement == null) {
      throw new IllegalArgumentException("Null replacement");
    }
    int len = newReplacement.length();
    if (len == 0) {
      throw new IllegalArgumentException("Empty replacement");
    }
    if (len > maxCharsPerByte) {
      throw new IllegalArgumentException("Replacement too long");
    }
    this.replacement = newReplacement;
    implReplaceWith(this.replacement);
    return this;
  }

  protected void implReplaceWith(String newReplacement) { }

  public CodingErrorAction malformedInputAction() {
    return malformedInputAction;
  }

  public final CharsetDecoder onMalformedInput(CodingErrorAction newAction) {
    if (newAction == null) {
      throw new IllegalArgumentException("Null action");
    }
    malformedInputAction = newAction;
    implOnMalformedInput(newAction);
    return this;
  }

  protected void implOnMalformedInput(CodingErrorAction newAction) { }

  public CodingErrorAction unmappableCharacterAction() {
    return unmappableCharacterAction;
  }

  public final CharsetDecoder onUnmappableCharacter(CodingErrorAction newAction) {
    if (newAction == null) {
      throw new IllegalArgumentException("Null action");
    }
    unmappableCharacterAction = newAction;
    implOnUnmappableCharacter(newAction);
    return this;
  }

  protected void implOnUnmappableCharacter(CodingErrorAction newAction) { }

  public final float averageCharsPerByte() {
    return averageCharsPerByte;
  }

  public final float maxCharsPerByte() {
    return maxCharsPerByte;
  }

  public final CoderResult decode(ByteBuffer in, CharBuffer out, boolean endOfInput) {
    int newState = endOfInput ? ST_END : ST_CODING;
    if (state != ST_RESET && state != ST_CODING && !(endOfInput && state == ST_END)) {
      throwIllegalStateException(state, newState);
    }
    state = newState;
    for (;;) {
      CoderResult cr;
      try {
        cr = decodeLoop(in, out);
      } catch (BufferUnderflowException x) {
        throw new CoderMalfunctionError(x);
      } catch (BufferOverflowException x) {
        throw new CoderMalfunctionError(x);
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
      CodingErrorAction action = null;
      if (cr.isMalformed()) {
        action = malformedInputAction;
      } else if (cr.isUnmappable()) {
        action = unmappableCharacterAction;
      }
      if (action == CodingErrorAction.REPORT) {
        return cr;
      }
      if (action == CodingErrorAction.REPLACE) {
        if (out.remaining() < replacement.length()) {
          return CoderResult.OVERFLOW;
        }
        out.put(replacement);
      }
      if (action == CodingErrorAction.IGNORE || action == CodingErrorAction.REPLACE) {
        in.position(in.position() + cr.length());
        continue;
      }
    }
  }

  public final CoderResult flush(CharBuffer out) {
    if (state == ST_END) {
      CoderResult cr = implFlush(out);
      if (cr.isUnderflow()) {
        state = ST_FLUSHED;
      }
      return cr;
    }
    if (state != ST_FLUSHED) {
      throwIllegalStateException(state, ST_FLUSHED);
    }
    return CoderResult.UNDERFLOW;
  }

  protected CoderResult implFlush(CharBuffer out) {
    return CoderResult.UNDERFLOW;
  }

  public final CharsetDecoder reset() {
    implReset();
    state = ST_RESET;
    return this;
  }

  protected void implReset() { }

  protected abstract CoderResult decodeLoop(ByteBuffer in, CharBuffer out);

  public final CharBuffer decode(ByteBuffer in) throws CharacterCodingException {
    int n = (int) (in.remaining() * averageCharsPerByte());
    CharBuffer out = CharBuffer.allocate(n);
    if (n == 0 && in.remaining() == 0) {
      return out;
    }
    reset();
    for (;;) {
      CoderResult cr = in.hasRemaining() ? decode(in, out, true) : CoderResult.UNDERFLOW;
      if (cr.isUnderflow()) {
        cr = flush(out);
      }
      if (cr.isUnderflow()) {
        break;
      }
      if (cr.isOverflow()) {
        n = 2 * n + 1;
        CharBuffer o = CharBuffer.allocate(n);
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

  public boolean isAutoDetecting() {
    return false;
  }

  public boolean isCharsetDetected() {
    throw new UnsupportedOperationException();
  }

  public Charset detectedCharset() {
    throw new UnsupportedOperationException();
  }

  private void throwIllegalStateException(int from, int to) {
    throw new IllegalStateException("Current state = " + kStateNames[from] + ", new state = " + kStateNames[to]);
  }
}
