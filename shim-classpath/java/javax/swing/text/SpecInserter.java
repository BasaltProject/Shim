/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.AbstractDocument.ElementEdit;
import javax.swing.text.DefaultStyledDocument.ElementSpec;

final class SpecInserter {
  private static final class LeafItem {
    AttributeSet attrs;
    int start;
    int end;

    LeafItem(AttributeSet attrs, int start, int end) {
      this.attrs = attrs;
      this.start = start;
      this.end = end;
    }
  }

  private static final class BranchItem {
    final AttributeSet attrs;
    final List<Object> items;

    BranchItem(AttributeSet attrs, List<Object> items) {
      this.attrs = attrs;
      this.items = items;
    }
  }

  private static final class Level {
    BranchElement elem;
    boolean fresh;
    int startIdx;
    Element[] removed = new Element[0];
    final List<Object> left = new ArrayList<Object>();
    final ArrayDeque<Object> right = new ArrayDeque<Object>();
  }

  private final DefaultStyledDocument doc;
  private final int offset;
  private final List<Level> stack = new ArrayList<Level>();
  private final List<Level> path = new ArrayList<Level>();
  private LeafItem leaf;

  SpecInserter(DefaultStyledDocument doc, Element root, int offset) {
    this.doc = doc;
    this.offset = offset;
    int probe = offset > 0 ? offset - 1 : 0;
    Element e = root;
    while (!e.isLeaf()) {
      int idx = e.getElementIndex(probe);
      Element child = e.getElement(idx);
      Level lv = new Level();
      lv.elem = (BranchElement) e;
      lv.startIdx = child.isLeaf() ? idx : idx + 1;
      int n = e.getElementCount();
      lv.removed = new Element[n - lv.startIdx];
      for (int i = lv.startIdx; i < n; i++) {
        lv.removed[i - lv.startIdx] = e.getElement(i);
        lv.right.add(e.getElement(i));
      }
      path.add(lv);
      e = child;
    }
    Level last = path.get(path.size() - 1);
    leaf = new LeafItem(e.getAttributes().copyAttributes(), e.getStartOffset(), e.getEndOffset());
    last.right.removeFirst();
    if (leaf.end > offset) {
      last.right.addFirst(leaf);
    }
    stack.addAll(path);
  }

  void apply(int length, ElementSpec[] data, DefaultDocumentEvent dc) {
    int oldStart = leaf.start;
    leaf.start = offset + length;
    leaf.end += length;
    if (oldStart < offset) {
      stack.get(stack.size() - 1).left.add(new LeafItem(leaf.attrs, oldStart, offset));
    }
    int pos = offset;
    for (ElementSpec spec : data) {
      Level cur = stack.get(stack.size() - 1);
      short dir = spec.getDirection();
      switch (spec.getType()) {
        case ElementSpec.StartTagType:
          openLevel(cur, spec, dir);
          break;
        case ElementSpec.EndTagType:
          if (stack.size() > 1) {
            closeLevel(cur, dc, true);
            stack.remove(stack.size() - 1);
          }
          break;
        default: {
          int len = spec.getLength();
          AttributeSet a = spec.getAttributes() == null ? SimpleAttributeSet.EMPTY : spec.getAttributes();
          Object next = cur.right.peekFirst();
          Object prev = cur.left.isEmpty() ? null : cur.left.get(cur.left.size() - 1);
          if (dir == ElementSpec.JoinPreviousDirection && prev instanceof LeafItem) {
            ((LeafItem) prev).end = pos + len;
          } else if (dir == ElementSpec.JoinNextDirection && next instanceof LeafItem) {
            ((LeafItem) next).start = pos;
          } else {
            cur.left.add(new LeafItem(a, pos, pos + len));
          }
          pos += len;
          break;
        }
      }
    }
    while (!stack.isEmpty()) {
      closeLevel(stack.get(stack.size() - 1), dc, false);
      stack.remove(stack.size() - 1);
    }
  }

  private void openLevel(Level cur, ElementSpec spec, short dir) {
    Object front = cur.right.peekFirst();
    Level lv = new Level();
    lv.fresh = true;
    if ((dir == ElementSpec.JoinNextDirection || dir == ElementSpec.JoinFractureDirection)
        && (front instanceof BranchItem || front instanceof BranchElement)) {
      cur.right.removeFirst();
      if (front instanceof BranchElement) {
        lv.elem = (BranchElement) front;
        lv.fresh = false;
        lv.startIdx = 0;
        int n = lv.elem.getElementCount();
        lv.removed = new Element[n];
        for (int i = 0; i < n; i++) {
          lv.removed[i] = lv.elem.getElement(i);
          lv.right.add(lv.elem.getElement(i));
        }
      } else {
        BranchItem bi = (BranchItem) front;
        lv.elem = (BranchElement) doc.createBranchElement(cur.elem, bi.attrs);
        lv.right.addAll(bi.items);
      }
    } else {
      AttributeSet a = spec.getAttributes() == null ? SimpleAttributeSet.EMPTY : spec.getAttributes();
      lv.elem = (BranchElement) doc.createBranchElement(cur.elem, a);
    }
    cur.left.add(lv.elem);
    stack.add(lv);
  }

  private void closeLevel(Level lv, DefaultDocumentEvent dc, boolean pop) {
    List<Object> children = new ArrayList<Object>(lv.left);
    if (pop && !lv.right.isEmpty() && stack.size() > 1) {
      Level parent = stack.get(stack.size() - 2);
      parent.right.addFirst(new BranchItem(lv.elem.getAttributes().copyAttributes(), new ArrayList<Object>(lv.right)));
    } else {
      children.addAll(lv.right);
    }
    Element[] kids = new Element[children.size()];
    for (int i = 0; i < kids.length; i++) {
      kids[i] = materialize(children.get(i), lv.elem);
    }
    if (lv.fresh) {
      lv.elem.replace(0, lv.elem.getElementCount(), kids);
    } else if (sameChildren(kids, lv.removed)) {
      return;
    } else {
      lv.elem.replace(lv.startIdx, lv.elem.getElementCount() - lv.startIdx, kids);
      dc.addEdit(new ElementEdit(lv.elem, lv.startIdx, lv.removed, kids));
    }
  }

  private static boolean sameChildren(Element[] a, Element[] b) {
    if (a.length != b.length) {
      return false;
    }
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) {
        return false;
      }
    }
    return true;
  }

  private Element materialize(Object item, BranchElement parent) {
    if (item instanceof Element) {
      Element e = (Element) item;
      if (e.getParentElement() == parent) {
        return e;
      }
      return cloneElement(parent, e);
    }
    if (item instanceof LeafItem) {
      LeafItem l = (LeafItem) item;
      return doc.createLeafElement(parent, l.attrs, l.start, l.end);
    }
    BranchItem bi = (BranchItem) item;
    BranchElement b = (BranchElement) doc.createBranchElement(parent, bi.attrs);
    Element[] kids = new Element[bi.items.size()];
    for (int i = 0; i < kids.length; i++) {
      kids[i] = materialize(bi.items.get(i), b);
    }
    b.replace(0, 0, kids);
    return b;
  }

  private Element cloneElement(Element parent, Element e) {
    if (e.isLeaf()) {
      return doc.createLeafElement(parent, e.getAttributes(), e.getStartOffset(), e.getEndOffset());
    }
    BranchElement b = (BranchElement) doc.createBranchElement(parent, e.getAttributes());
    int n = e.getElementCount();
    Element[] kids = new Element[n];
    for (int i = 0; i < n; i++) {
      kids[i] = cloneElement(b, e.getElement(i));
    }
    b.replace(0, 0, kids);
    return b;
  }
}
