/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import javax.swing.event.DocumentEvent;

public class PlainDocument extends AbstractDocument {
  public static final String tabSizeAttribute = "tabSize";
  public static final String lineLimitAttribute = "lineLimit";
  private static final String kFilterNewlines = "filterNewlines";

  private AbstractElement defaultRoot;

  public PlainDocument() { this(new GapContent(4096)); }

  public PlainDocument(Content c) {
    super(c);
    putProperty(tabSizeAttribute, Integer.valueOf(8));
    defaultRoot = createDefaultRoot();
  }

  public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
    if (str != null && Boolean.TRUE.equals(getProperty(kFilterNewlines)) && str.indexOf('\n') >= 0) {
      str = str.replace('\n', ' ');
    }
    super.insertString(offs, str, a);
  }

  public Element getDefaultRootElement() { return defaultRoot; }

  protected AbstractElement createDefaultRoot() {
    BranchElement map = (BranchElement) createBranchElement(null, null);
    Element line = createLeafElement(map, null, 0, 1);
    map.replace(0, 0, new Element[] {line});
    return map;
  }

  public Element getParagraphElement(int pos) {
    Element lineMap = getDefaultRootElement();
    return lineMap.getElement(lineMap.getElementIndex(pos));
  }

  protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
    int off = chng.getOffset();
    int n = chng.getLength();
    BranchElement lineMap = (BranchElement) getDefaultRootElement();
    String inserted;
    try {
      inserted = getText(off, n);
    } catch (BadLocationException e) {
      return;
    }
    if (inserted.indexOf('\n') < 0) {
      return;
    }
    int idx = lineMap.getElementIndex(off);
    Element line = lineMap.getElement(idx);
    int start = line.getStartOffset();
    int end = line.getEndOffset();
    String span;
    try {
      span = getText(start, end - start);
    } catch (BadLocationException e) {
      return;
    }
    java.util.List<Element> pieces = new java.util.ArrayList<>();
    int from = start;
    for (int i = 0; i < span.length(); i++) {
      if (span.charAt(i) == '\n') {
        pieces.add(createLeafElement(lineMap, null, from, start + i + 1));
        from = start + i + 1;
      }
    }
    if (from < end) {
      pieces.add(createLeafElement(lineMap, null, from, end));
    }
    Element[] added = pieces.toArray(new Element[0]);
    Element[] removed = {line};
    chng.addEdit(new ElementEdit(lineMap, idx, removed, added));
    lineMap.replace(idx, 1, added);
  }

  protected void removeUpdate(DefaultDocumentEvent chng) {
    BranchElement lineMap = (BranchElement) getDefaultRootElement();
    int off = chng.getOffset();
    int len = chng.getLength();
    int first = lineMap.getElementIndex(off);
    int last = lineMap.getElementIndex(off + len);
    if (first == last) {
      return;
    }
    Element[] removed = new Element[last - first + 1];
    for (int i = 0; i < removed.length; i++) {
      removed[i] = lineMap.getElement(first + i);
    }
    Element merged = createLeafElement(lineMap, null, removed[0].getStartOffset(),
        removed[removed.length - 1].getEndOffset());
    Element[] added = {merged};
    chng.addEdit(new ElementEdit(lineMap, first, removed, added));
    lineMap.replace(first, removed.length, added);
  }
}
