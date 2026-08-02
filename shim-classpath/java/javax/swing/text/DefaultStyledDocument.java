/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class DefaultStyledDocument extends AbstractDocument implements StyledDocument {
  public static final int BUFFER_SIZE_DEFAULT = 4096;

  protected ElementBuffer buffer;

  private final ChangeListener styleListener = new ChangeListener() {
    public void stateChanged(ChangeEvent e) {
      Object source = e.getSource();
      if (source instanceof Style) {
        styleChanged((Style) source);
      }
    }
  };

  public DefaultStyledDocument(Content c, StyleContext styles) {
    super(c, styles);
    buffer = new ElementBuffer(createDefaultRoot());
    Style defaultStyle = styles.getStyle(StyleContext.DEFAULT_STYLE);
    setLogicalStyle(0, defaultStyle);
  }

  public DefaultStyledDocument(StyleContext styles) { this(new GapContent(BUFFER_SIZE_DEFAULT), styles); }

  public DefaultStyledDocument() { this(new GapContent(BUFFER_SIZE_DEFAULT), new StyleContext()); }

  public Element getDefaultRootElement() { return buffer.getRootElement(); }

  protected void create(ElementSpec[] data) {
    try {
      if (getLength() != 0) {
        remove(0, getLength());
      }
      writeLock();
      StringBuilder text = new StringBuilder();
      for (ElementSpec spec : data) {
        if (spec.getArray() != null && spec.getType() == ElementSpec.ContentType) {
          text.append(spec.getArray(), spec.getOffset(), spec.getLength());
        }
      }
      if (text.length() > 0) {
        getContent().insertString(0, text.toString());
      }
      BranchElement root = (BranchElement) createBranchElement(null, null);
      List<BranchElement> stack = new ArrayList<>();
      stack.add(root);
      int pos = 0;
      for (ElementSpec spec : data) {
        BranchElement top = stack.get(stack.size() - 1);
        switch (spec.getType()) {
          case ElementSpec.StartTagType: {
            BranchElement b = (BranchElement) createBranchElement(top, spec.getAttributes());
            top.replace(top.getElementCount(), 0, new Element[] {b});
            stack.add(b);
            break;
          }
          case ElementSpec.EndTagType:
            if (stack.size() > 1) {
              stack.remove(stack.size() - 1);
            }
            break;
          default: {
            int len = spec.getLength();
            Element leaf = createLeafElement(top, spec.getAttributes(), pos, pos + len);
            top.replace(top.getElementCount(), 0, new Element[] {leaf});
            pos += len;
            break;
          }
        }
      }
      buffer = new ElementBuffer(root);
      writeUnlock();
    } catch (BadLocationException ble) {
      writeUnlock();
      throw new IllegalStateException(ble);
    }
  }

  protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
    if (data == null || data.length == 0) {
      return;
    }
    writeLock();
    try {
      StringBuilder text = new StringBuilder();
      for (ElementSpec spec : data) {
        if (spec.getType() == ElementSpec.ContentType && spec.getArray() != null) {
          text.append(spec.getArray(), spec.getOffset(), spec.getLength());
        }
      }
      int length = text.length();
      buffer.prepare(offset);
      UndoableEdit cEdit = getContent().insertString(offset, text.toString());
      DefaultDocumentEvent evnt = new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.INSERT);
      if (cEdit != null) {
        evnt.addEdit(cEdit);
      }
      buffer.insert(offset, length, data, evnt);
      evnt.end();
      fireInsertUpdate(evnt);
      fireUndoableEditUpdate(new javax.swing.event.UndoableEditEvent(this, evnt));
    } finally {
      writeUnlock();
    }
  }

  public void removeElement(Element elem) {
    Element parent = elem.getParentElement();
    if (parent instanceof BranchElement) {
      BranchElement b = (BranchElement) parent;
      int idx = b.getElementIndex(elem.getStartOffset());
      if (idx >= 0 && b.getElement(idx) == elem) {
        b.replace(idx, 1, new Element[0]);
      }
    }
  }

  protected AbstractElement createDefaultRoot() {
    writeLock();
    try {
      BranchElement section = new SectionElement();
      BranchElement paragraph = (BranchElement) createBranchElement(section, null);
      Element brk = createLeafElement(paragraph, null, 0, 1);
      paragraph.replace(0, 0, new Element[] {brk});
      section.replace(0, 0, new Element[] {paragraph});
      return section;
    } finally {
      writeUnlock();
    }
  }

  public Style addStyle(String nm, Style parent) {
    StyleContext styles = (StyleContext) getAttributeContext();
    Style s = styles.addStyle(nm, parent);
    s.addChangeListener(styleListener);
    return s;
  }

  public void removeStyle(String nm) { ((StyleContext) getAttributeContext()).removeStyle(nm); }

  public Style getStyle(String nm) { return ((StyleContext) getAttributeContext()).getStyle(nm); }

  public Enumeration<?> getStyleNames() { return ((StyleContext) getAttributeContext()).getStyleNames(); }

  protected void styleChanged(Style style) { }

  public void setLogicalStyle(int pos, Style s) {
    Element paragraph = getParagraphElement(pos);
    if (paragraph instanceof AbstractElement) {
      writeLock();
      try {
        DefaultDocumentEvent changes = new DefaultDocumentEvent(paragraph.getStartOffset(),
            paragraph.getEndOffset() - paragraph.getStartOffset(), DocumentEvent.EventType.CHANGE);
        AttributeSet attr = paragraph.getAttributes();
        if (attr instanceof AbstractElement) {
          changes.addEdit(new AttributeUndoableEdit(paragraph, s, false));
          ((AbstractElement) attr).setResolveParent(s);
        }
        changes.end();
        fireChangedUpdate(changes);
        fireUndoableEditUpdate(new javax.swing.event.UndoableEditEvent(this, changes));
      } finally {
        writeUnlock();
      }
    }
  }

  public Style getLogicalStyle(int p) {
    Style s = null;
    Element paragraph = getParagraphElement(p);
    if (paragraph != null) {
      AttributeSet a = paragraph.getAttributes();
      AttributeSet parent = a.getResolveParent();
      if (parent instanceof Style) {
        s = (Style) parent;
      }
    }
    return s;
  }

  public Element getParagraphElement(int pos) {
    Element e = getDefaultRootElement();
    while (!e.isLeaf()) {
      int index = e.getElementIndex(pos);
      Element next = e.getElement(index);
      if (next.isLeaf()) {
        return e;
      }
      e = next;
    }
    return e;
  }

  public Element getCharacterElement(int pos) {
    Element e = getDefaultRootElement();
    while (!e.isLeaf()) {
      e = e.getElement(e.getElementIndex(pos));
    }
    return e;
  }

  public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
    if (length == 0) {
      return;
    }
    writeLock();
    try {
      DefaultDocumentEvent changes = new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
      DefaultDocumentEvent scratch = new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
      List<BranchElement> paras = new ArrayList<>();
      List<Element[]> before = new ArrayList<>();
      int scan = offset;
      int end = offset + length;
      while (scan < end) {
        Element para = getParagraphElement(scan);
        if (para instanceof BranchElement && !paras.contains(para)) {
          paras.add((BranchElement) para);
          before.add(childrenOf(para));
        }
        scan = Math.max(para.getEndOffset(), scan + 1);
      }
      buffer.change(offset, length, scratch);
      int pos = offset;
      while (pos < end) {
        Element leaf = getCharacterElement(pos);
        AttributeSet attr = leaf.getAttributes();
        if (attr instanceof AbstractElement) {
          changes.addEdit(new AttributeUndoableEdit(leaf, s, replace));
          AbstractElement ae = (AbstractElement) attr;
          if (replace) {
            ae.removeAttributes(ae);
          }
          ae.addAttributes(s);
        }
        pos = Math.max(leaf.getEndOffset(), pos + 1);
      }
      mergeEqualRuns(offset, end, scratch);
      for (int i = 0; i < paras.size(); i++) {
        Element[] now = childrenOf(paras.get(i));
        Element[] old = before.get(i);
        boolean same = now.length == old.length;
        for (int k = 0; same && k < now.length; k++) {
          same = now[k] == old[k];
        }
        if (!same) {
          changes.addEdit(new ElementEdit(paras.get(i), 0, old, now));
        }
      }
      changes.end();
      fireChangedUpdate(changes);
      fireUndoableEditUpdate(new javax.swing.event.UndoableEditEvent(this, changes));
    } finally {
      writeUnlock();
    }
  }

  private static Element[] childrenOf(Element e) {
    Element[] kids = new Element[e.getElementCount()];
    for (int i = 0; i < kids.length; i++) {
      kids[i] = e.getElement(i);
    }
    return kids;
  }

  private void mergeEqualRuns(int from, int to, DefaultDocumentEvent changes) {
    int pos = Math.max(0, from - 1);
    int limit = Math.min(to + 1, getLength());
    while (pos <= limit) {
      Element para = getParagraphElement(pos);
      if (para instanceof BranchElement && !para.getElement(0).isLeaf() == false) {
        BranchElement b = (BranchElement) para;
        int i = 0;
        while (i + 1 < b.getElementCount()) {
          Element a = b.getElement(i);
          Element c = b.getElement(i + 1);
          if (a.isLeaf() && c.isLeaf() && a.getAttributes().isEqual(c.getAttributes())) {
            Element merged = createLeafElement(b, a.getAttributes().copyAttributes(), a.getStartOffset(),
                c.getEndOffset());
            changes.addEdit(new ElementEdit(b, i, new Element[] {a, c}, new Element[] {merged}));
            b.replace(i, 2, new Element[] {merged});
          } else {
            i++;
          }
        }
      }
      int next = para.getEndOffset();
      if (next <= pos) {
        break;
      }
      pos = next;
    }
  }

  public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
    writeLock();
    try {
      DefaultDocumentEvent changes = new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);
      int end = Math.min(offset + Math.max(length - 1, 0), getLength());
      int pos = offset;
      Element last = null;
      do {
        Element para = getParagraphElement(pos);
        if (para != last) {
          AttributeSet attr = para.getAttributes();
          if (attr instanceof AbstractElement) {
            changes.addEdit(new AttributeUndoableEdit(para, s, replace));
            AbstractElement ae = (AbstractElement) attr;
            if (replace) {
              ae.removeAttributes(ae);
            }
            ae.addAttributes(s);
          }
          last = para;
        }
        pos = Math.max(para.getEndOffset(), pos + 1);
      } while (pos <= end);
      changes.end();
      fireChangedUpdate(changes);
      fireUndoableEditUpdate(new javax.swing.event.UndoableEditEvent(this, changes));
    } finally {
      writeUnlock();
    }
  }

  public Font getFont(AttributeSet attr) { return ((StyleContext) getAttributeContext()).getFont(attr); }

  public Color getForeground(AttributeSet attr) { return ((StyleContext) getAttributeContext()).getForeground(attr); }

  public Color getBackground(AttributeSet attr) { return ((StyleContext) getAttributeContext()).getBackground(attr); }

  protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
    if (attr == null) {
      attr = SimpleAttributeSet.EMPTY;
    }
    buffer.insert(chng.getOffset(), chng.getLength(), attr, chng);
    super.insertUpdate(chng, attr);
  }

  protected void removeUpdate(DefaultDocumentEvent chng) {
    super.removeUpdate(chng);
    buffer.remove(chng.getOffset(), chng.getLength(), chng);
  }

  public class ElementBuffer implements Serializable {
    private final Element root;

    public ElementBuffer(Element root) { this.root = root; }

    public Element getRootElement() { return root; }

    public Element clone(Element parent, Element clonee) {
      if (clonee.isLeaf()) {
        return createLeafElement(parent, clonee.getAttributes(), clonee.getStartOffset(), clonee.getEndOffset());
      }
      Element copy = createBranchElement(parent, clonee.getAttributes());
      int n = clonee.getElementCount();
      Element[] kids = new Element[n];
      for (int i = 0; i < n; i++) {
        kids[i] = clone(copy, clonee.getElement(i));
      }
      ((BranchElement) copy).replace(0, 0, kids);
      return copy;
    }

    void insert(int offset, int length, AttributeSet attr, DefaultDocumentEvent dc) {
      Element leaf = leafAt(offset);
      BranchElement para = (BranchElement) leaf.getParentElement();
      int idx = para.getElementIndex(offset);
      int start = leaf.getStartOffset();
      int end = leaf.getEndOffset();
      List<Element> pieces = new ArrayList<>();
      AttributeSet old = leaf.getAttributes();
      if (old.isEqual(attr) || (attr.getAttributeCount() == 0 && old.getAttributeCount() == 0)) {
        pieces.add(createLeafElement(para, old, start, end));
      } else {
        if (offset > start) {
          pieces.add(createLeafElement(para, old, start, offset));
        }
        pieces.add(createLeafElement(para, attr, offset, offset + length));
        if (offset + length < end) {
          pieces.add(createLeafElement(para, old, offset + length, end));
        }
      }
      Element[] added = pieces.toArray(new Element[0]);
      Element[] removed = {leaf};
      dc.addEdit(new ElementEdit(para, idx, removed, added));
      para.replace(idx, 1, added);
      splitParagraphs(offset, length, dc);
    }

    private void splitParagraphs(int offset, int length, DefaultDocumentEvent dc) {
      String text;
      try {
        text = getText(offset, length);
      } catch (BadLocationException e) {
        return;
      }
      int nl = text.indexOf('\n');
      while (nl >= 0) {
        int cut = offset + nl + 1;
        splitAt(cut, dc);
        nl = text.indexOf('\n', nl + 1);
      }
    }

    private void splitAt(int cut, DefaultDocumentEvent dc) {
      Element leaf = leafAt(cut - 1);
      BranchElement para = (BranchElement) leaf.getParentElement();
      if (para.getEndOffset() <= cut) {
        return;
      }
      BranchElement parent = (BranchElement) para.getParentElement();
      int pidx = parent.getElementIndex(para.getStartOffset());
      BranchElement first = (BranchElement) createBranchElement(parent, para.getAttributes());
      BranchElement second = (BranchElement) createBranchElement(parent, para.getAttributes());
      List<Element> a = new ArrayList<>();
      List<Element> b = new ArrayList<>();
      for (int i = 0; i < para.getElementCount(); i++) {
        Element k = para.getElement(i);
        int s = k.getStartOffset();
        int e = k.getEndOffset();
        if (e <= cut) {
          a.add(createLeafElement(first, k.getAttributes(), s, e));
        } else if (s >= cut) {
          b.add(createLeafElement(second, k.getAttributes(), s, e));
        } else {
          a.add(createLeafElement(first, k.getAttributes(), s, cut));
          b.add(createLeafElement(second, k.getAttributes(), cut, e));
        }
      }
      first.replace(0, 0, a.toArray(new Element[0]));
      second.replace(0, 0, b.toArray(new Element[0]));
      Element[] added = {first, second};
      Element[] removed = {para};
      dc.addEdit(new ElementEdit(parent, pidx, removed, added));
      parent.replace(pidx, 1, added);
    }

    private Element leafAt(int pos) {
      Element e = root;
      while (!e.isLeaf()) {
        e = e.getElement(e.getElementIndex(pos));
      }
      return e;
    }

    private SpecInserter pending;

    void prepare(int offset) { pending = new SpecInserter(DefaultStyledDocument.this, root, offset); }

    public void insert(int offset, int length, ElementSpec[] data, DefaultDocumentEvent de) {
      if (length == 0 && (data == null || data.length == 0)) {
        return;
      }
      SpecInserter inserter = pending != null ? pending : new SpecInserter(DefaultStyledDocument.this, root, offset);
      pending = null;
      inserter.apply(length, data, de);
      insertUpdate(data);
    }

    public void remove(int offset, int length, DefaultDocumentEvent dc) {
      int end = offset + length;
      Element firstLeaf = leafAt(offset);
      BranchElement firstPara = (BranchElement) firstLeaf.getParentElement();
      Element lastLeaf = leafAt(end);
      BranchElement lastPara = (BranchElement) lastLeaf.getParentElement();
      List<Element> kept = new ArrayList<>();
      for (int i = 0; i < firstPara.getElementCount(); i++) {
        Element k = firstPara.getElement(i);
        if (!(k.getStartOffset() >= offset && k.getEndOffset() <= end)) {
          kept.add(k);
        }
      }
      int firstIdx = firstPara.getElementCount();
      Element[] oldKids = new Element[firstPara.getElementCount()];
      for (int i = 0; i < oldKids.length; i++) {
        oldKids[i] = firstPara.getElement(i);
      }
      if (firstPara != lastPara) {
        for (int i = 0; i < lastPara.getElementCount(); i++) {
          Element k = lastPara.getElement(i);
          if (k.getEndOffset() > end) {
            kept.add(createLeafElement(firstPara, k.getAttributes(), k.getStartOffset(), k.getEndOffset()));
          }
        }
      }
      if (firstPara == lastPara && kept.size() == oldKids.length) {
        return;
      }
      Element[] keptArr = kept.toArray(new Element[0]);
      if (keptArr.length == 0) {
        keptArr = new Element[] {createLeafElement(firstPara, null, offset, offset + 1)};
      }
      dc.addEdit(new ElementEdit(firstPara, 0, oldKids, keptArr));
      firstPara.replace(0, firstIdx, keptArr);
      if (firstPara != lastPara) {
        BranchElement parent = (BranchElement) firstPara.getParentElement();
        int fi = parent.getElementIndex(firstPara.getStartOffset());
        int li = parent.getElementIndex(lastPara.getStartOffset());
        if (li > fi) {
          Element[] gone = new Element[li - fi];
          for (int i = 0; i < gone.length; i++) {
            gone[i] = parent.getElement(fi + 1 + i);
          }
          dc.addEdit(new ElementEdit(parent, fi + 1, gone, new Element[0]));
          parent.replace(fi + 1, gone.length, new Element[0]);
        }
      }
    }

    public void change(int offset, int length, DefaultDocumentEvent dc) {
      splitLeafAt(offset, dc);
      splitLeafAt(offset + length, dc);
    }

    private void splitLeafAt(int pos, DefaultDocumentEvent dc) {
      if (pos > getLength()) {
        return;
      }
      Element leaf = leafAt(pos);
      if (leaf.getStartOffset() == pos) {
        return;
      }
      BranchElement para = (BranchElement) leaf.getParentElement();
      int idx = para.getElementIndex(pos);
      Element a = createLeafElement(para, leaf.getAttributes(), leaf.getStartOffset(), pos);
      Element b = createLeafElement(para, leaf.getAttributes(), pos, leaf.getEndOffset());
      Element[] added = {a, b};
      dc.addEdit(new ElementEdit(para, idx, new Element[] {leaf}, added));
      para.replace(idx, 1, added);
    }

    protected void insertUpdate(ElementSpec[] data) { }

    protected void removeUpdate() { }

    protected void changeUpdate() { }
  }

  public static class ElementSpec {
    public static final short StartTagType = 1;
    public static final short EndTagType = 2;
    public static final short ContentType = 3;
    public static final short JoinPreviousDirection = 4;
    public static final short JoinNextDirection = 5;
    public static final short OriginateDirection = 6;
    public static final short JoinFractureDirection = 7;

    private AttributeSet attr;
    private int len;
    private short type;
    private short direction;
    private int offs;
    private char[] data;

    public ElementSpec(AttributeSet a, short type) { this(a, type, null, 0, 0); }

    public ElementSpec(AttributeSet a, short type, int len) { this(a, type, null, 0, len); }

    public ElementSpec(AttributeSet a, short type, char[] txt, int offs, int len) {
      attr = a;
      this.type = type;
      this.data = txt;
      this.offs = offs;
      this.len = len;
      this.direction = OriginateDirection;
    }

    public void setType(short type) { this.type = type; }

    public short getType() { return type; }

    public void setDirection(short direction) { this.direction = direction; }

    public short getDirection() { return direction; }

    public AttributeSet getAttributes() { return attr; }

    public char[] getArray() { return data; }

    public int getOffset() { return offs; }

    public int getLength() { return len; }

    public String toString() {
      String tlbl = "??";
      String plbl = "??";
      switch (type) {
        case StartTagType: tlbl = "StartTag"; break;
        case ContentType: tlbl = "Content"; break;
        case EndTagType: tlbl = "EndTag"; break;
        default: break;
      }
      switch (direction) {
        case JoinPreviousDirection: plbl = "JoinPrevious"; break;
        case JoinNextDirection: plbl = "JoinNext"; break;
        case OriginateDirection: plbl = "Originate"; break;
        case JoinFractureDirection: plbl = "Fracture"; break;
        default: break;
      }
      return tlbl + ":" + plbl + ":" + getLength();
    }
  }

  public static class AttributeUndoableEdit extends AbstractUndoableEdit {
    protected AttributeSet newAttributes;
    protected AttributeSet copy;
    protected boolean isReplacing;
    protected Element element;

    public AttributeUndoableEdit(Element element, AttributeSet newAttributes, boolean isReplacing) {
      this.element = element;
      this.newAttributes = newAttributes;
      this.isReplacing = isReplacing;
      copy = element.getAttributes().copyAttributes();
    }

    public void redo() throws CannotRedoException {
      super.redo();
      MutableAttributeSet as = (MutableAttributeSet) element.getAttributes();
      if (isReplacing) {
        as.removeAttributes(as);
      }
      if (newAttributes != null) {
        as.addAttributes(newAttributes);
      }
    }

    public void undo() throws CannotUndoException {
      super.undo();
      MutableAttributeSet as = (MutableAttributeSet) element.getAttributes();
      Enumeration<?> names = as.getAttributeNames();
      List<Object> toRemove = new ArrayList<>();
      while (names.hasMoreElements()) {
        toRemove.add(names.nextElement());
      }
      for (Object n : toRemove) {
        as.removeAttribute(n);
      }
      as.addAttributes(copy);
    }
  }

  private class SectionElement extends BranchElement {
    SectionElement() { super(null, null); }

    public String getName() { return SectionElementName; }
  }
}
