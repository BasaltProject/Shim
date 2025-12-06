/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Scanner implements Iterator<String>, Closeable, AutoCloseable {
  private static final Pattern DEFAULT_DELIMITER = Pattern.compile("\\s+");
  private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
  private static final Pattern LINE_SEPARATOR_PATTERN = Pattern.compile("\r\n|[\r\n]");

  private final Readable source;
  private final Closeable closeable;
  private Pattern delimiter = DEFAULT_DELIMITER;
  private Locale locale = Locale.getDefault();
  private int radix = 10;
  private boolean closed = false;
  private IOException lastException;
  private MatchResult lastMatchResult;

  private final StringBuilder buffer = new StringBuilder();
  private int position = 0;
  private boolean sourceExhausted = false;

  private String nextToken;
  private boolean hasNextCached = false;

  public Scanner(Readable source) {
    this.source = Objects.requireNonNull(source, "source");
    this.closeable = (source instanceof Closeable) ? (Closeable) source : null;
  }

  public Scanner(InputStream source) {
    this(new InputStreamReader(source, StandardCharsets.UTF_8));
  }

  public Scanner(InputStream source, String charsetName) {
    this(new InputStreamReader(source, Charset.forName(charsetName)));
  }

  public Scanner(InputStream source, Charset charset) {
    this(new InputStreamReader(source, charset));
  }

  public Scanner(File source) throws FileNotFoundException {
    this(openFile(source));
  }

  public Scanner(File source, String charsetName) throws FileNotFoundException {
    this(openFile(source), charsetName);
  }

  public Scanner(File source, Charset charset) throws FileNotFoundException {
    this(openFile(source), charset);
  }

  private static InputStream openFile(File file) throws FileNotFoundException {
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw e;
    } catch (IOException e) {
      FileNotFoundException f = new FileNotFoundException(e.getMessage());
      f.initCause(e);
      throw f;
    }
  }

  public Scanner(Path source) throws IOException {
    this(Files.newInputStream(source));
  }

  public Scanner(Path source, String charsetName) throws IOException {
    this(Files.newInputStream(source), charsetName);
  }

  public Scanner(Path source, Charset charset) throws IOException {
    this(Files.newInputStream(source), charset);
  }

  public Scanner(String source) {
    this(new StringReader(Objects.requireNonNull(source, "source")));
  }

  public Scanner(ReadableByteChannel source) {
    this(Channels.newReader(source, StandardCharsets.UTF_8));
  }

  public Scanner(ReadableByteChannel source, String charsetName) {
    this(Channels.newReader(source, Charset.forName(charsetName)));
  }

  public Scanner(ReadableByteChannel source, Charset charset) {
    this(Channels.newReader(source, charset));
  }

  private void ensureOpen() {
    if (closed) {
      throw new IllegalStateException("Scanner closed");
    }
  }

  private boolean readMore() {
    if (sourceExhausted) return false;
    char[] buf = new char[1024];
    CharBuffer cb = CharBuffer.wrap(buf);
    try {
      int count = source.read(cb);
      if (count < 0) {
        sourceExhausted = true;
        return false;
      }
      buffer.append(buf, 0, count);
      return true;
    } catch (IOException e) {
      lastException = e;
      sourceExhausted = true;
      return false;
    }
  }

  private void compactBuffer() {
    if (position > 0) {
      buffer.delete(0, position);
      position = 0;
    }
  }

  private boolean findNextToken() {
    if (hasNextCached) return true;
    ensureOpen();
    while (true) {
      compactBuffer();
      Matcher delimMatcher = delimiter.matcher(buffer);
      int tokenStart = 0;
      if (delimMatcher.lookingAt()) {
        tokenStart = delimMatcher.end();
      }
      if (tokenStart < buffer.length()) {
        Matcher nextDelim = delimiter.matcher(buffer);
        if (nextDelim.find(tokenStart)) {
          int tokenEnd = nextDelim.start();
          nextToken = buffer.substring(tokenStart, tokenEnd);
          position = tokenEnd;
          hasNextCached = true;
          return true;
        } else if (sourceExhausted) {
          nextToken = buffer.substring(tokenStart);
          position = buffer.length();
          hasNextCached = true;
          return true;
        }
      } else if (sourceExhausted) {
        position = buffer.length();
        return false;
      }
      if (!readMore()) {
        if (tokenStart < buffer.length()) {
          nextToken = buffer.substring(tokenStart);
          position = buffer.length();
          hasNextCached = true;
          return true;
        }
        return false;
      }
    }
  }

  @Override
  public boolean hasNext() {
    return findNextToken();
  }

  @Override
  public String next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    hasNextCached = false;
    return nextToken;
  }

  public boolean hasNext(String pattern) {
    return hasNext(Pattern.compile(pattern));
  }

  public boolean hasNext(Pattern pattern) {
    Objects.requireNonNull(pattern, "pattern");
    if (!hasNext()) return false;
    Matcher m = pattern.matcher(nextToken);
    if (m.matches()) {
      lastMatchResult = m.toMatchResult();
      return true;
    }
    return false;
  }

  public String next(String pattern) {
    return next(Pattern.compile(pattern));
  }

  public String next(Pattern pattern) {
    if (!hasNext(pattern)) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return next();
  }

  public boolean hasNextLine() {
    ensureOpen();
    if (position < buffer.length()) return true;
    while (!sourceExhausted) {
      if (readMore() && position < buffer.length()) return true;
    }
    return position < buffer.length();
  }

  public String nextLine() {
    ensureOpen();
    if (!hasNextLine()) {
      throw new NoSuchElementException("No line found");
    }
    compactBuffer();
    while (true) {
      Matcher m = LINE_SEPARATOR_PATTERN.matcher(buffer);
      if (m.find()) {
        int lineEnd = m.start();
        int afterSep = m.end();
        String line = buffer.substring(0, lineEnd);
        position = afterSep;
        hasNextCached = false;
        lastMatchResult = m.toMatchResult();
        return line;
      }
      if (!readMore()) {
        String line = buffer.substring(0);
        position = buffer.length();
        hasNextCached = false;
        return line;
      }
    }
  }

  public String findInLine(String pattern) {
    return findInLine(Pattern.compile(pattern));
  }

  public String findInLine(Pattern pattern) {
    Objects.requireNonNull(pattern, "pattern");
    ensureOpen();
    if (!hasNextLine()) return null;
    compactBuffer();
    Matcher lineSep = LINE_SEPARATOR_PATTERN.matcher(buffer);
    int lineEnd = lineSep.find() ? lineSep.start() : buffer.length();
    String line = buffer.substring(0, lineEnd);
    Matcher m = pattern.matcher(line);
    if (m.find()) {
      position = m.end();
      lastMatchResult = m.toMatchResult();
      return m.group();
    }
    return null;
  }

  public String findWithinHorizon(String pattern, int horizon) {
    return findWithinHorizon(Pattern.compile(pattern), horizon);
  }

  public String findWithinHorizon(Pattern pattern, int horizon) {
    Objects.requireNonNull(pattern, "pattern");
    if (horizon < 0) throw new IllegalArgumentException("horizon < 0");
    ensureOpen();
    compactBuffer();
    while (horizon == 0 || buffer.length() < horizon) {
      if (!readMore()) break;
    }
    int searchLen = (horizon > 0) ? Math.min(horizon, buffer.length()) : buffer.length();
    Matcher m = pattern.matcher(buffer.substring(0, searchLen));
    if (m.find()) {
      position = m.end();
      lastMatchResult = m.toMatchResult();
      return m.group();
    }
    return null;
  }

  public Scanner skip(Pattern pattern) {
    Objects.requireNonNull(pattern, "pattern");
    ensureOpen();
    compactBuffer();
    while (true) {
      Matcher m = pattern.matcher(buffer);
      if (m.lookingAt()) {
        position = m.end();
        lastMatchResult = m.toMatchResult();
        hasNextCached = false;
        return this;
      }
      if (!readMore()) {
        throw new NoSuchElementException();
      }
    }
  }

  public Scanner skip(String pattern) {
    return skip(Pattern.compile(pattern));
  }

  public boolean hasNextBoolean() {
    if (!hasNext()) return false;
    return BOOLEAN_PATTERN.matcher(nextToken).matches();
  }

  public boolean nextBoolean() {
    if (!hasNextBoolean()) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return Boolean.parseBoolean(next());
  }

  public boolean hasNextByte() {
    return hasNextByte(radix);
  }

  public boolean hasNextByte(int radix) {
    checkRadix(radix);
    if (!hasNext()) return false;
    try {
      Byte.parseByte(nextToken, radix);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public byte nextByte() {
    return nextByte(radix);
  }

  public byte nextByte(int radix) {
    checkRadix(radix);
    if (!hasNextByte(radix)) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return Byte.parseByte(next(), radix);
  }

  public boolean hasNextShort() {
    return hasNextShort(radix);
  }

  public boolean hasNextShort(int radix) {
    checkRadix(radix);
    if (!hasNext()) return false;
    try {
      Short.parseShort(nextToken, radix);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public short nextShort() {
    return nextShort(radix);
  }

  public short nextShort(int radix) {
    checkRadix(radix);
    if (!hasNextShort(radix)) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return Short.parseShort(next(), radix);
  }

  public boolean hasNextInt() {
    return hasNextInt(radix);
  }

  public boolean hasNextInt(int radix) {
    checkRadix(radix);
    if (!hasNext()) return false;
    try {
      Integer.parseInt(nextToken, radix);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public int nextInt() {
    return nextInt(radix);
  }

  public int nextInt(int radix) {
    checkRadix(radix);
    if (!hasNextInt(radix)) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return Integer.parseInt(next(), radix);
  }

  public boolean hasNextLong() {
    return hasNextLong(radix);
  }

  public boolean hasNextLong(int radix) {
    checkRadix(radix);
    if (!hasNext()) return false;
    try {
      Long.parseLong(nextToken, radix);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public long nextLong() {
    return nextLong(radix);
  }

  public long nextLong(int radix) {
    checkRadix(radix);
    if (!hasNextLong(radix)) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return Long.parseLong(next(), radix);
  }

  public boolean hasNextFloat() {
    if (!hasNext()) return false;
    try {
      Float.parseFloat(nextToken);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public float nextFloat() {
    if (!hasNextFloat()) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return Float.parseFloat(next());
  }

  public boolean hasNextDouble() {
    if (!hasNext()) return false;
    try {
      Double.parseDouble(nextToken);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public double nextDouble() {
    if (!hasNextDouble()) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return Double.parseDouble(next());
  }

  public boolean hasNextBigInteger() {
    return hasNextBigInteger(radix);
  }

  public boolean hasNextBigInteger(int radix) {
    checkRadix(radix);
    if (!hasNext()) return false;
    try {
      new BigInteger(nextToken, radix);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public BigInteger nextBigInteger() {
    return nextBigInteger(radix);
  }

  public BigInteger nextBigInteger(int radix) {
    checkRadix(radix);
    if (!hasNextBigInteger(radix)) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return new BigInteger(next(), radix);
  }

  public boolean hasNextBigDecimal() {
    if (!hasNext()) return false;
    try {
      new BigDecimal(nextToken);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public BigDecimal nextBigDecimal() {
    if (!hasNextBigDecimal()) {
      if (!hasNext()) throw new NoSuchElementException();
      throw new InputMismatchException();
    }
    return new BigDecimal(next());
  }

  public Pattern delimiter() {
    return delimiter;
  }

  public Scanner useDelimiter(Pattern pattern) {
    this.delimiter = Objects.requireNonNull(pattern, "pattern");
    return this;
  }

  public Scanner useDelimiter(String pattern) {
    return useDelimiter(Pattern.compile(pattern));
  }

  public Locale locale() {
    return locale;
  }

  public Scanner useLocale(Locale locale) {
    this.locale = Objects.requireNonNull(locale, "locale");
    return this;
  }

  public int radix() {
    return radix;
  }

  private static void checkRadix(int radix) {
    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
      throw new IllegalArgumentException("radix:" + radix);
    }
  }

  public Scanner useRadix(int radix) {
    checkRadix(radix);
    this.radix = radix;
    return this;
  }

  public MatchResult match() {
    if (lastMatchResult == null) {
      throw new IllegalStateException("No match result available");
    }
    return lastMatchResult;
  }

  public Scanner reset() {
    this.delimiter = DEFAULT_DELIMITER;
    this.locale = Locale.getDefault();
    this.radix = 10;
    return this;
  }

  public IOException ioException() {
    return lastException;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    if (closed) return;
    closed = true;
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        lastException = e;
      }
    }
  }

  public Stream<String> tokens() {
    ensureOpen();
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED | Spliterator.NONNULL), false);
  }

  public Stream<MatchResult> findAll(Pattern pattern) {
    Objects.requireNonNull(pattern, "pattern");
    ensureOpen();
    while (readMore()) {}
    compactBuffer();
    final Matcher matcher = pattern.matcher(buffer.toString());
    Iterator<MatchResult> it = new Iterator<MatchResult>() {
      private MatchResult nextResult;
      private boolean hasNextCached;

      @Override
      public boolean hasNext() {
        if (hasNextCached) return true;
        if (matcher.find()) {
          nextResult = matcher.toMatchResult();
          hasNextCached = true;
          return true;
        }
        return false;
      }

      @Override
      public MatchResult next() {
        if (!hasNext()) throw new NoSuchElementException("No more matches");
        hasNextCached = false;
        return nextResult;
      }
    };
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED | Spliterator.NONNULL), false);
  }

  public Stream<MatchResult> findAll(String patString) {
    Objects.requireNonNull(patString, "patString");
    return findAll(Pattern.compile(patString));
  }
}
