/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.regex;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class RegexProperties {
  private RegexProperties() { }

  static CharPredicate range(final int lo, final int hi) {
    return new CharPredicate() {
      @Override
      public boolean is(int ch) {
        return ch >= lo && ch <= hi;
      }
    };
  }

  static CharPredicate single(final int c) {
    return new CharPredicate() {
      @Override
      public boolean is(int ch) {
        return ch == c;
      }
    };
  }

  static CharPredicate types(final int... types) {
    return new CharPredicate() {
      @Override
      public boolean is(int ch) {
        int t = Character.getType(ch);
        for (int x : types) {
          if (x == t) {
            return true;
          }
        }
        return false;
      }
    };
  }

  static final CharPredicate kAny = new CharPredicate() {
    @Override
    public boolean is(int ch) {
      return true;
    }
  };

  static final CharPredicate kAsciiDigit = range('0', '9');
  static final CharPredicate kAsciiWord = range('a', 'z').union(range('A', 'Z')).union(range('0', '9'))
      .union(single('_'));
  static final CharPredicate kAsciiSpace = new CharPredicate() {
    @Override
    public boolean is(int ch) {
      return ch == ' ' || (ch >= 9 && ch <= 13);
    }
  };

  static final CharPredicate kUnicodeSpace = new CharPredicate() {
    @Override
    public boolean is(int ch) {
      return Character.isWhitespace(ch) || ch == 0x85 || Character.isSpaceChar(ch);
    }
  };

  static final CharPredicate kUnicodeDigit = new CharPredicate() {
    @Override
    public boolean is(int ch) {
      return Character.isDigit(ch);
    }
  };

  static final CharPredicate kUnicodeWord = new CharPredicate() {
    @Override
    public boolean is(int ch) {
      return Character.isAlphabetic(ch) || Character.getType(ch) == Character.NON_SPACING_MARK
          || Character.getType(ch) == Character.ENCLOSING_MARK
              || Character.getType(ch) == Character.COMBINING_SPACING_MARK
          || Character.getType(ch) == Character.CONNECTOR_PUNCTUATION || Character.isDigit(ch) || ch == 0x200C
          || ch == 0x200D;
    }
  };

  static final CharPredicate kHorizontalSpace = new CharPredicate() {
    @Override
    public boolean is(int ch) {
      return ch == 0x09 || ch == 0x20 || ch == 0xa0 || ch == 0x1680 || ch == 0x180e || (ch >= 0x2000 && ch <= 0x200a)
          || ch == 0x202f || ch == 0x205f || ch == 0x3000;
    }
  };

  static final CharPredicate kVerticalSpace = new CharPredicate() {
    @Override
    public boolean is(int ch) {
      return (ch >= 0x0a && ch <= 0x0d) || ch == 0x85 || ch == 0x2028 || ch == 0x2029;
    }
  };

  private static final Map<String, CharPredicate> kCategories = new HashMap<String, CharPredicate>();
  private static final Map<String, CharPredicate> kBinary = new HashMap<String, CharPredicate>();
  private static final Map<String, CharPredicate> kJava = new HashMap<String, CharPredicate>();
  private static final Map<String, CharPredicate> kPosixAscii = new HashMap<String, CharPredicate>();
  private static final Map<String, CharPredicate> kPosixUnicode = new HashMap<String, CharPredicate>();

  private static void category(String name, int... types) {
    kCategories.put(name.toLowerCase(Locale.ROOT), types(types));
  }

  static {
    category("Lu", Character.UPPERCASE_LETTER);
    category("Ll", Character.LOWERCASE_LETTER);
    category("Lt", Character.TITLECASE_LETTER);
    category("Lm", Character.MODIFIER_LETTER);
    category("Lo", Character.OTHER_LETTER);
    category("LC", Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER, Character.TITLECASE_LETTER);
    category("L", Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER, Character.TITLECASE_LETTER,
        Character.MODIFIER_LETTER, Character.OTHER_LETTER);
    category("Mn", Character.NON_SPACING_MARK);
    category("Mc", Character.COMBINING_SPACING_MARK);
    category("Me", Character.ENCLOSING_MARK);
    category("M", Character.NON_SPACING_MARK, Character.COMBINING_SPACING_MARK, Character.ENCLOSING_MARK);
    category("Nd", Character.DECIMAL_DIGIT_NUMBER);
    category("Nl", Character.LETTER_NUMBER);
    category("No", Character.OTHER_NUMBER);
    category("N", Character.DECIMAL_DIGIT_NUMBER, Character.LETTER_NUMBER, Character.OTHER_NUMBER);
    category("Pc", Character.CONNECTOR_PUNCTUATION);
    category("Pd", Character.DASH_PUNCTUATION);
    category("Ps", Character.START_PUNCTUATION);
    category("Pe", Character.END_PUNCTUATION);
    category("Pi", Character.INITIAL_QUOTE_PUNCTUATION);
    category("Pf", Character.FINAL_QUOTE_PUNCTUATION);
    category("Po", Character.OTHER_PUNCTUATION);
    category("P", Character.CONNECTOR_PUNCTUATION, Character.DASH_PUNCTUATION, Character.START_PUNCTUATION,
        Character.END_PUNCTUATION, Character.INITIAL_QUOTE_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION,
        Character.OTHER_PUNCTUATION);
    category("Sm", Character.MATH_SYMBOL);
    category("Sc", Character.CURRENCY_SYMBOL);
    category("Sk", Character.MODIFIER_SYMBOL);
    category("So", Character.OTHER_SYMBOL);
    category("S", Character.MATH_SYMBOL, Character.CURRENCY_SYMBOL, Character.MODIFIER_SYMBOL, Character.OTHER_SYMBOL);
    category("Zs", Character.SPACE_SEPARATOR);
    category("Zl", Character.LINE_SEPARATOR);
    category("Zp", Character.PARAGRAPH_SEPARATOR);
    category("Z", Character.SPACE_SEPARATOR, Character.LINE_SEPARATOR, Character.PARAGRAPH_SEPARATOR);
    category("Cc", Character.CONTROL);
    category("Cf", Character.FORMAT);
    category("Cs", Character.SURROGATE);
    category("Co", Character.PRIVATE_USE);
    category("Cn", Character.UNASSIGNED);
    category("C", Character.CONTROL, Character.FORMAT, Character.SURROGATE, Character.PRIVATE_USE,
        Character.UNASSIGNED);

    kBinary.put("alphabetic", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isAlphabetic(ch);
      }
    });
    kBinary.put("ideographic", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isIdeographic(ch);
      }
    });
    kBinary.put("letter", kCategories.get("l"));
    kBinary.put("lowercase", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isLowerCase(ch);
      }
    });
    kBinary.put("uppercase", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isUpperCase(ch);
      }
    });
    kBinary.put("titlecase", kCategories.get("lt"));
    kBinary.put("punctuation", kCategories.get("p"));
    kBinary.put("control", kCategories.get("cc"));
    kBinary.put("white_space", new CharPredicate() {
      public boolean is(int ch) {
        return (ch >= 9 && ch <= 13) || ch == 0x20 || ch == 0x85 || ch == 0xa0 || ch == 0x1680
            || (ch >= 0x2000 && ch <= 0x200a) || ch == 0x2028 || ch == 0x2029 || ch == 0x202f || ch == 0x205f
            || ch == 0x3000;
      }
    });
    kBinary.put("digit", kCategories.get("nd"));
    kBinary.put("hex_digit", new CharPredicate() {
      public boolean is(int ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F')
            || (ch >= 0xff10 && ch <= 0xff19) || (ch >= 0xff21 && ch <= 0xff26) || (ch >= 0xff41 && ch <= 0xff46);
      }
    });
    kBinary.put("join_control", new CharPredicate() {
      public boolean is(int ch) {
        return ch == 0x200c || ch == 0x200d;
      }
    });
    kBinary.put("noncharacter_code_point", new CharPredicate() {
      public boolean is(int ch) {
        return (ch >= 0xfdd0 && ch <= 0xfdef) || (ch & 0xfffe) == 0xfffe;
      }
    });
    kBinary.put("assigned", kCategories.get("cn").negate());

    kJava.put("javaLowerCase", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isLowerCase(ch);
      }
    });
    kJava.put("javaUpperCase", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isUpperCase(ch);
      }
    });
    kJava.put("javaAlphabetic", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isAlphabetic(ch);
      }
    });
    kJava.put("javaIdeographic", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isIdeographic(ch);
      }
    });
    kJava.put("javaTitleCase", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isTitleCase(ch);
      }
    });
    kJava.put("javaDigit", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isDigit(ch);
      }
    });
    kJava.put("javaDefined", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isDefined(ch);
      }
    });
    kJava.put("javaLetter", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isLetter(ch);
      }
    });
    kJava.put("javaLetterOrDigit", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isLetterOrDigit(ch);
      }
    });
    kJava.put("javaJavaIdentifierStart", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isJavaIdentifierStart(ch);
      }
    });
    kJava.put("javaJavaIdentifierPart", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isJavaIdentifierPart(ch);
      }
    });
    kJava.put("javaUnicodeIdentifierStart", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isUnicodeIdentifierStart(ch);
      }
    });
    kJava.put("javaUnicodeIdentifierPart", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isUnicodeIdentifierPart(ch);
      }
    });
    kJava.put("javaIdentifierIgnorable", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isIdentifierIgnorable(ch);
      }
    });
    kJava.put("javaSpaceChar", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isSpaceChar(ch);
      }
    });
    kJava.put("javaWhitespace", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isWhitespace(ch);
      }
    });
    kJava.put("javaISOControl", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isISOControl(ch);
      }
    });
    kJava.put("javaMirrored", new CharPredicate() {
      public boolean is(int ch) {
        return Character.isMirrored(ch);
      }
    });

    CharPredicate lower = range('a', 'z');
    CharPredicate upper = range('A', 'Z');
    CharPredicate alpha = lower.union(upper);
    CharPredicate alnum = alpha.union(kAsciiDigit);
    CharPredicate punct = range(0x21, 0x2f).union(range(0x3a, 0x40)).union(range(0x5b, 0x60)).union(range(0x7b, 0x7e));
    CharPredicate graph = alnum.union(punct);
    kPosixAscii.put("Lower", lower);
    kPosixAscii.put("Upper", upper);
    kPosixAscii.put("ASCII", range(0, 0x7f));
    kPosixAscii.put("Alpha", alpha);
    kPosixAscii.put("Digit", kAsciiDigit);
    kPosixAscii.put("Alnum", alnum);
    kPosixAscii.put("Punct", punct);
    kPosixAscii.put("Graph", graph);
    kPosixAscii.put("Print", graph.union(single(' ')));
    kPosixAscii.put("Blank", single(' ').union(single('\t')));
    kPosixAscii.put("Cntrl", range(0, 0x1f).union(single(0x7f)));
    kPosixAscii.put("XDigit", range('0', '9').union(range('a', 'f')).union(range('A', 'F')));
    kPosixAscii.put("Space", kAsciiSpace);

    CharPredicate uAlpha = kBinary.get("alphabetic");
    CharPredicate uDigit = kUnicodeDigit;
    CharPredicate uAlnum = uAlpha.union(uDigit);
    CharPredicate uPunct = kCategories.get("p");
    CharPredicate uGraph = new CharPredicate() {
      public boolean is(int ch) {
        return !(kBinary.get("white_space").is(ch) || Character.getType(ch) == Character.CONTROL
            || Character.getType(ch) == Character.SURROGATE || Character.getType(ch) == Character.UNASSIGNED);
      }
    };
    kPosixUnicode.put("Lower", kJava.get("javaLowerCase"));
    kPosixUnicode.put("Upper", kJava.get("javaUpperCase"));
    kPosixUnicode.put("ASCII", range(0, 0x7f));
    kPosixUnicode.put("Alpha", uAlpha);
    kPosixUnicode.put("Digit", uDigit);
    kPosixUnicode.put("Alnum", uAlnum);
    kPosixUnicode.put("Punct", uPunct.union(range(0x21, 0x2f).and(kCategories.get("s"))));
    kPosixUnicode.put("Graph", uGraph);
    kPosixUnicode.put("Print", uGraph.union(kCategories.get("zs")).and(kCategories.get("cc").negate()));
    kPosixUnicode.put("Blank", kCategories.get("zs").union(single('\t')));
    kPosixUnicode.put("Cntrl", kCategories.get("cc"));
    kPosixUnicode.put("XDigit", kBinary.get("hex_digit"));
    kPosixUnicode.put("Space", kBinary.get("white_space"));
  }

  static CharPredicate lookup(String name, boolean unicodeClass, String pattern, int index) {
    CharPredicate p;
    if (name.startsWith("Is") && name.length() > 2) {
      String rest = name.substring(2);
      p = category(rest);
      if (p == null) {
        p = kBinary.get(rest.toLowerCase(Locale.ROOT));
      }
      if (p == null) {
        p = (unicodeClass ? kPosixUnicode : kPosixAscii).get(rest);
      }
      if (p == null) {
        p = script(rest);
      }
      if (p == null) {
        throw new PatternSyntaxException("Unknown character property name {" + name + "}", pattern, index);
      }
      return p;
    }
    int eq = name.indexOf('=');
    if (eq >= 0) {
      String key = name.substring(0, eq).toLowerCase(Locale.ROOT);
      String value = name.substring(eq + 1);
      if (key.equals("general_category") || key.equals("gc")) {
        p = category(value);
        if (p == null) {
          throw new PatternSyntaxException("Unknown Unicode category {" + value + "}", pattern, index);
        }
        return p;
      }
      if (key.equals("script") || key.equals("sc")) {
        p = script(value);
        if (p != null) {
          return p;
        }
      }
      if (key.equals("block") || key.equals("blk")) {
        p = block(value);
        if (p != null) {
          return p;
        }
      }
      throw new PatternSyntaxException("Unknown Unicode property {name=<" + name.substring(0, eq) + ">, value=<"
          + value + ">}", pattern, index);
    }
    if (name.startsWith("In") && name.length() > 2) {
      p = block(name.substring(2));
      if (p == null) {
        throw new PatternSyntaxException("Unknown character property name {" + name + "}", pattern, index);
      }
      return p;
    }
    p = kJava.get(name);
    if (p == null) {
      p = (unicodeClass ? kPosixUnicode : kPosixAscii).get(name);
    }
    if (p == null) {
      p = category(name);
    }
    if (p == null) {
      throw new PatternSyntaxException("Unknown character property name {" + name + "}", pattern, index);
    }
    return p;
  }

  private static CharPredicate category(String name) {
    return kCategories.get(name.toLowerCase(Locale.ROOT));
  }

  private static CharPredicate script(String name) {
    final Character.UnicodeScript sc;
    try {
      sc = Character.UnicodeScript.forName(name);
    } catch (IllegalArgumentException e) {
      return null;
    }
    return new CharPredicate() {
      public boolean is(int ch) {
        return Character.UnicodeScript.of(ch) == sc;
      }
    };
  }

  private static CharPredicate block(String name) {
    final Character.UnicodeBlock blk;
    try {
      blk = Character.UnicodeBlock.forName(name);
    } catch (IllegalArgumentException e) {
      return null;
    }
    return new CharPredicate() {
      public boolean is(int ch) {
        return Character.UnicodeBlock.of(ch) == blk;
      }
    };
  }
}
