/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.util.regex.PatternSyntaxException;

public class Globs {
  private Globs() { }

  private static final String kRegexMetaChars = ".^$+{[]|()";
  private static final String kGlobMetaChars = "\\*?[{";

  private static boolean isRegexMeta(char c) {
    return kRegexMetaChars.indexOf(c) != -1;
  }

  private static boolean isGlobMeta(char c) {
    return kGlobMetaChars.indexOf(c) != -1;
  }

  private static final char kEol = 0;

  private static char next(String glob, int i) {
    if (i < glob.length()) {
      return glob.charAt(i);
    }
    return kEol;
  }

  private static String toRegexPattern(String globPattern, boolean isDos) {
    boolean inGroup = false;
    StringBuilder regex = new StringBuilder("^");
    int i = 0;
    while (i < globPattern.length()) {
      char c = globPattern.charAt(i++);
      switch (c) {
        case '\\':
          if (i == globPattern.length()) {
            throw new PatternSyntaxException("No character to escape", globPattern, i - 1);
          }
          char next = globPattern.charAt(i++);
          if (isGlobMeta(next) || isRegexMeta(next)) {
            regex.append('\\');
          }
          regex.append(next);
          break;
        case '/':
          if (isDos) {
            regex.append("\\\\");
          } else {
            regex.append(c);
          }
          break;
        case '[':
          if (isDos) {
            regex.append("[[^\\\\]&&[");
          } else {
            regex.append("[[^/]&&[");
          }
          if (next(globPattern, i) == '^') {
            regex.append("\\^");
            i++;
          } else {
            if (next(globPattern, i) == '!') {
              regex.append('^');
              i++;
            }
            if (next(globPattern, i) == '-') {
              regex.append('-');
              i++;
            }
          }
          boolean hasRangeStart = false;
          char last = 0;
          while (i < globPattern.length()) {
            c = globPattern.charAt(i++);
            if (c == ']') {
              break;
            }
            if (c == '/' || (isDos && c == '\\')) {
              throw new PatternSyntaxException("Explicit 'name separator' in class", globPattern, i - 1);
            }
            if (c == '\\' || c == '[' || c == '&' && next(globPattern, i) == '&') {
              regex.append('\\');
            }
            regex.append(c);
            if (c == '-') {
              if (!hasRangeStart) {
                throw new PatternSyntaxException("Invalid range", globPattern, i - 1);
              }
              if ((c = next(globPattern, i++)) == kEol || c == ']') {
                break;
              }
              if (c < last) {
                throw new PatternSyntaxException("Invalid range", globPattern, i - 3);
              }
              regex.append(c);
              hasRangeStart = false;
            } else {
              hasRangeStart = true;
              last = c;
            }
          }
          if (c != ']') {
            throw new PatternSyntaxException("Missing ']", globPattern, i - 1);
          }
          regex.append("]]");
          break;
        case '{':
          if (inGroup) {
            throw new PatternSyntaxException("Cannot nest groups", globPattern, i - 1);
          }
          regex.append("(?:(?:");
          inGroup = true;
          break;
        case '}':
          if (inGroup) {
            regex.append("))");
            inGroup = false;
          } else {
            regex.append('}');
          }
          break;
        case ',':
          if (inGroup) {
            regex.append(")|(?:");
          } else {
            regex.append(',');
          }
          break;
        case '*':
          if (next(globPattern, i) == '*') {
            regex.append(".*");
            i++;
          } else {
            if (isDos) {
              regex.append("[^\\\\]*");
            } else {
              regex.append("[^/]*");
            }
          }
          break;
        case '?':
          if (isDos) {
            regex.append("[^\\\\]");
          } else {
            regex.append("[^/]");
          }
          break;
        default:
          if (isRegexMeta(c)) {
            regex.append('\\');
          }
          regex.append(c);
      }
    }
    if (inGroup) {
      throw new PatternSyntaxException("Missing '}", globPattern, i - 1);
    }
    return regex.append('$').toString();
  }

  public static String toUnixRegexPattern(String globPattern) {
    return toRegexPattern(globPattern, false);
  }

  public static String toWindowsRegexPattern(String globPattern) {
    return toRegexPattern(globPattern, true);
  }
}
