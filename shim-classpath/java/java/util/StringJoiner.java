/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

public final class StringJoiner {
  private final String prefix;
  private final String delimiter;
  private final String suffix;
  private StringBuilder value;
  private String emptyValue;

  public StringJoiner(CharSequence delimiter) {
    this(delimiter, "", "");
  }

  public StringJoiner(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
    Objects.requireNonNull(delimiter, "The delimiter must not be null");
    Objects.requireNonNull(prefix, "The prefix must not be null");
    Objects.requireNonNull(suffix, "The suffix must not be null");
    this.delimiter = delimiter.toString();
    this.prefix = prefix.toString();
    this.suffix = suffix.toString();
    this.emptyValue = this.prefix + this.suffix;
  }

  public StringJoiner setEmptyValue(CharSequence emptyValue) {
    this.emptyValue = Objects.requireNonNull(emptyValue, "The empty value must not be null").toString();
    return this;
  }

  public StringJoiner add(CharSequence newElement) {
    prepareBuilder().append(newElement);
    return this;
  }

  public StringJoiner merge(StringJoiner other) {
    Objects.requireNonNull(other);
    if (other.value != null) {
      StringBuilder builder = prepareBuilder();
      builder.append(other.value.substring(other.prefix.length()));
    }
    return this;
  }

  public int length() {
    return (value != null ? value.length() + suffix.length() : emptyValue.length());
  }

  private StringBuilder prepareBuilder() {
    if (value != null) {
      value.append(delimiter);
    } else {
      value = new StringBuilder().append(prefix);
    }
    return value;
  }

  @Override
  public String toString() {
    if (value == null) {
      return emptyValue;
    }
    if (suffix.equals("")) {
      return value.toString();
    }
    return new StringBuilder().append(value).append(suffix).toString();
  }
}
