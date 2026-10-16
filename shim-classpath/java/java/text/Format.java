/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.text;

import java.io.Serializable;

public abstract class Format implements Serializable, Cloneable {
  private static final long serialVersionUID = -299282585814624189L;

  public final String format(Object o) {
    return format(o, new StringBuffer(), new FieldPosition(0)).toString();
  }

  public abstract StringBuffer format(Object o, StringBuffer target,
                                      FieldPosition p);

  public Object parseObject(String source) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Object result = parseObject(source, pos);
    if (pos.getIndex() == 0) {
      throw new ParseException("Format.parseObject(String) failed", pos.getErrorIndex());
    }
    return result;
  }

  public abstract Object parseObject(String source, ParsePosition pos);

  public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
    return createAttributedCharacterIterator(format(obj));
  }

  AttributedCharacterIterator createAttributedCharacterIterator(String s) {
    return new AttributedString(s).getIterator();
  }

  AttributedCharacterIterator createAttributedCharacterIterator(AttributedCharacterIterator[] iterators) {
    StringBuilder sb = new StringBuilder();
    java.util.List<java.util.Map<AttributedCharacterIterator.Attribute, Object>> maps =
        new java.util.ArrayList<java.util.Map<AttributedCharacterIterator.Attribute, Object>>();
    for (AttributedCharacterIterator it : iterators) {
      for (int i = it.getBeginIndex(); i < it.getEndIndex(); i++) {
        it.setIndex(i);
        sb.append(it.current());
        maps.add(new java.util.HashMap<AttributedCharacterIterator.Attribute, Object>(it.getAttributes()));
      }
    }
    return new AttributedString(sb.toString(), maps).getIterator();
  }

  public static class Field extends AttributedCharacterIterator.Attribute {
    private static final long serialVersionUID = 276966692217360283L;

    protected Field(String name) {
      super(name);
    }
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
