/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.text.ParseException;
import javax.swing.JFormattedTextField;

public class MaskFormatter extends DefaultFormatter {
  private static final char DIGIT = '#';
  private static final char LITERAL = '\'';
  private static final char UPPERCASE = 'U';
  private static final char LOWERCASE = 'L';
  private static final char ALPHA_NUMERIC = 'A';
  private static final char CHARACTER = '?';
  private static final char ANYTHING = '*';
  private static final char HEX = 'H';

  private String mask;
  private String validCharacters;
  private String invalidCharacters;
  private String placeholderString;
  private char placeholder = ' ';
  private boolean containsLiteralChars = true;
  private char[] maskChars = new char[0];
  private boolean[] literal = new boolean[0];

  public MaskFormatter() {
    setAllowsInvalid(false);
    setCommitsOnValidEdit(false);
    setOverwriteMode(true);
  }

  public MaskFormatter(String mask) throws ParseException {
    this();
    setMask(mask);
  }

  public void setMask(String mask) throws ParseException {
    this.mask = mask;
    updateInternalMask();
  }

  public String getMask() { return mask; }

  public void setValidCharacters(String validCharacters) { this.validCharacters = validCharacters; }

  public String getValidCharacters() { return validCharacters; }

  public void setInvalidCharacters(String invalidCharacters) { this.invalidCharacters = invalidCharacters; }

  public String getInvalidCharacters() { return invalidCharacters; }

  public void setPlaceholder(String placeholder) { this.placeholderString = placeholder; }

  public String getPlaceholder() { return placeholderString; }

  public void setPlaceholderCharacter(char placeholder) { this.placeholder = placeholder; }

  public char getPlaceholderCharacter() { return placeholder; }

  public void setValueContainsLiteralCharacters(boolean containsLiteralChars) {
    this.containsLiteralChars = containsLiteralChars;
  }

  public boolean getValueContainsLiteralCharacters() { return containsLiteralChars; }

  private void updateInternalMask() throws ParseException {
    String m = getMask();
    java.util.List<Character> chars = new java.util.ArrayList<Character>();
    java.util.List<Boolean> lits = new java.util.ArrayList<Boolean>();
    if (m != null) {
      for (int i = 0; i < m.length(); i++) {
        char c = m.charAt(i);
        switch (c) {
          case DIGIT:
          case UPPERCASE:
          case LOWERCASE:
          case ALPHA_NUMERIC:
          case CHARACTER:
          case ANYTHING:
          case HEX:
            chars.add(c);
            lits.add(Boolean.FALSE);
            break;
          case LITERAL:
            if (++i < m.length()) {
              chars.add(m.charAt(i));
              lits.add(Boolean.TRUE);
            }
            break;
          default:
            chars.add(c);
            lits.add(Boolean.TRUE);
            break;
        }
      }
    }
    maskChars = new char[chars.size()];
    literal = new boolean[chars.size()];
    for (int i = 0; i < maskChars.length; i++) {
      maskChars[i] = chars.get(i);
      literal[i] = lits.get(i);
    }
  }

  private boolean isValidCharacter(int index, char aChar) {
    if (literal[index]) {
      return maskChars[index] == aChar;
    }
    char mc = maskChars[index];
    String invalid = getInvalidCharacters();
    String valid = getValidCharacters();
    if (invalid != null && invalid.indexOf(aChar) != -1) {
      return false;
    }
    if (valid != null && valid.indexOf(aChar) == -1) {
      return false;
    }
    switch (mc) {
      case DIGIT:
        return Character.isDigit(aChar);
      case LOWERCASE:
        return Character.isLetter(aChar);
      case UPPERCASE:
        return Character.isLetter(aChar);
      case ALPHA_NUMERIC:
        return Character.isLetterOrDigit(aChar);
      case CHARACTER:
        return Character.isLetter(aChar);
      case HEX:
        return "0123456789abcdefABCDEF".indexOf(aChar) != -1;
      default:
        return true;
    }
  }

  private char convert(int index, char c) {
    switch (maskChars[index]) {
      case UPPERCASE:
        return Character.toUpperCase(c);
      case LOWERCASE:
        return Character.toLowerCase(c);
      default:
        return c;
    }
  }

  public String valueToString(Object value) throws ParseException {
    String sValue = value == null ? "" : value.toString();
    StringBuilder result = new StringBuilder();
    String placeholder = getPlaceholder();
    int[] valueCounter = {0};
    append(result, sValue, valueCounter, placeholder);
    return result.toString();
  }

  private void append(StringBuilder result, String value, int[] index, String placeholder) throws ParseException {
    for (int counter = 0; counter < maskChars.length; counter++) {
      if (literal[counter]) {
        if (getValueContainsLiteralCharacters() && index[0] < value.length()) {
          if (value.charAt(index[0]) != maskChars[counter]) {
            throw new ParseException("Invalid character: " + value.charAt(index[0]), index[0]);
          }
          index[0]++;
        }
        result.append(maskChars[counter]);
      } else if (index[0] >= value.length()) {
        if (placeholder != null && counter < placeholder.length()) {
          result.append(placeholder.charAt(counter));
        } else {
          result.append(getPlaceholderCharacter());
        }
      } else {
        char c = value.charAt(index[0]);
        if (isValidCharacter(counter, c)) {
          result.append(convert(counter, c));
          index[0]++;
        } else {
          throw new ParseException("Invalid character: " + c, index[0]);
        }
      }
    }
  }

  public Object stringToValue(String value) throws ParseException {
    return stringToValue(value, true);
  }

  private Object stringToValue(String value, boolean completeMatch) throws ParseException {
    int errorOffset;
    if ((errorOffset = getInvalidOffset(value, completeMatch)) == -1) {
      if (!getValueContainsLiteralCharacters()) {
        value = stripLiteralChars(value);
      }
      return super.stringToValue(value);
    }
    throw new ParseException("stringToValue passed invalid value", errorOffset);
  }

  private int getInvalidOffset(String string, boolean completeMatch) {
    int iLength = string.length();
    if (iLength != maskChars.length) {
      return Math.min(iLength, maskChars.length);
    }
    for (int counter = 0; counter < iLength; counter++) {
      char aChar = string.charAt(counter);
      if (literal[counter] ? maskChars[counter] != aChar : !isValidCharacter(counter, aChar)) {
        return counter;
      }
    }
    return -1;
  }

  private String stripLiteralChars(String string) {
    StringBuilder sb = null;
    int last = 0;
    for (int counter = 0, max = string.length(); counter < max; counter++) {
      if (literal[counter]) {
        if (sb == null) {
          sb = new StringBuilder();
          if (counter > 0) {
            sb.append(string, 0, counter);
          }
          last = counter + 1;
        } else if (last != counter) {
          sb.append(string, last, counter);
        }
        last = counter + 1;
      }
    }
    if (sb == null) {
      return string;
    } else if (last != string.length()) {
      sb.append(string, last, string.length());
    }
    return sb.toString();
  }

  public void install(JFormattedTextField ftf) { super.install(ftf); }
}
