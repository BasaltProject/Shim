/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface CharacterData extends Node {
  String getData() throws DOMException;

  void setData(String p0) throws DOMException;

  int getLength();

  String substringData(int p0, int p1) throws DOMException;

  void appendData(String p0) throws DOMException;

  void insertData(int p0, String p1) throws DOMException;

  void deleteData(int p0, int p1) throws DOMException;

  void replaceData(int p0, int p1, String p2) throws DOMException;
}
