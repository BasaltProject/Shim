/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

import java.io.Serializable;
import java.util.Enumeration;

public interface Name extends Cloneable, Serializable, Comparable<Object> {
  @SuppressWarnings("serial")
  long serialVersionUID = 1L;

  Object clone();

  int compareTo(Object obj);

  int size();

  boolean isEmpty();

  Enumeration<String> getAll();

  String get(int posn);

  Name getPrefix(int posn);

  Name getSuffix(int posn);

  boolean startsWith(Name n);

  boolean endsWith(Name n);

  Name addAll(Name suffix) throws InvalidNameException;

  Name addAll(int posn, Name n) throws InvalidNameException;

  Name add(String comp) throws InvalidNameException;

  Name add(int posn, String comp) throws InvalidNameException;

  Object remove(int posn) throws InvalidNameException;
}
