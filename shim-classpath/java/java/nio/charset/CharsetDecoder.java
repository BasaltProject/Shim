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
import java.util.Objects;

public abstract class CharsetDecoder {
  private static final int ST_RESET = 0;
  private static final int ST_CODING = 1;
  private static final int ST_END = 2;
  private static final int ST_FLUSHED = 3;

  private final Charset charset;
  private final float averageCharsPerByte;
  private final float maxCharsPerByte;
  private String replacement = "\ufffd";
  private CodingErrorAction malformedInputAction = CodingErrorAction.REPORT;
  private CodingErrorAction unmappableCharacterAction = CodingErrorAction.REPORT;
  private int state = ST_RESET;

  protected CharsetDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte) {
    this(cs, averageCharsPerByte, maxCharsPerByte, "\ufffd");
  }

  protected CharsetDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte, String replacement) {
    if (averageCharsPerByte <= 0.0f || maxCharsPerByte <= 0.0f) {
      throw new IllegalArgumentException("Non-positive estimates");
    }
    if (averageCharsPerByte > maxCharsPerByte) {
      throw new IllegalArgumentException("averageCharsPerByte > maxCharsPerByte");
    }
    this.charset = cs;
    this.averageCharsPerByte = averageCharsPerByte;
    this.maxCharsPerByte = maxCharsPerByte;
    replaceWith(replacement);
  }

  public final Charset charset() {
    return charset;
  }

  public final String replacement() {
    return replacement;
  }

  public final CharsetDecoder replaceWith(String newReplacement) {
    Objects.requireNonNull(newReplacement, "newReplacement");
    if (newReplacement.isEmpty() || newReplacement.length() > maxCharsPerByte) {
      throw new IllegalArgumentException("Illegal replacement: " + newReplacement);
    }
    this.replacement = newReplacement;
    implReplaceWith(newReplacement);
    return this;
  }

  protected void implReplaceWith(String newReplacement) {
  }

  public CodingErrorAction malformedInputAction() {
    return malformedInputAction;
  }

  public final CharsetDecoder onMalformedInput(CodingErrorAction newAction) {
    this.malformedInputAction = Objects.requireNonNull(newAction, "newAction");
    implOnMalformedInput(newAction);
    return this;
  }

  protected void implOnMalformedInput(CodingErrorAction newAction) {
  }

  public CodingErrorAction unmappableCharacterAction() {
    return unmappableCharacterAction;
  }

  public final CharsetDecoder onUnmappableCharacter(CodingErrorAction newAction) {
    this.unmappableCharacterAction = Objects.requireNonNull(newAction, "newAction");
    implOnUnmappableCharacter(newAction);
    return this;
  }

  protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
  }

  public final float averageCharsPerByte() {
    return averageCharsPerByte;
  }

  public final float maxCharsPerByte() {
    return maxCharsPerByte;
  }

  public final CoderResult decode(ByteBuffer in, CharBuffer out, boolean endOfInput) {
    int newState = endOfInput ? ST_END : ST_CODING;
    if (state != ST_RESET && state != ST_CODING && !(endOfInput && state == ST_END)) {
      throw new IllegalStateException("Current state = " + state);
    }
    state = newState;

    while (true) {
      CoderResult cr;
      try {
        cr = decodeLoop(in, out);
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
        if (out.remaining() < replacement.length()) {
          return CoderResult.OVERFLOW;
        }
        for (int i = 0; i < replacement.length(); ++i) {
          out.put(replacement.charAt(i));
        }
      }
      in.position(in.position() + cr.length());
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
      throw new IllegalStateException("Current state = " + state);
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

  protected void implReset() {
  }

  protected abstract CoderResult decodeLoop(ByteBuffer in, CharBuffer out);

  public final CharBuffer decode(ByteBuffer in) throws CharacterCodingException {
    int n = (int) (in.remaining() * averageCharsPerByte());
    CharBuffer out = CharBuffer.allocate(Math.max(16, n));
    reset();
    while (true) {
      CoderResult cr = in.hasRemaining() ? decode(in, out, true) : flush(out);
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
}
