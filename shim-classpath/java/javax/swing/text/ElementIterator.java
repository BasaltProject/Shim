/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.util.ArrayList;
import java.util.List;

public class ElementIterator implements Cloneable {
  private Element root;
  private List<StackItem> elementStack;

  private static class StackItem implements Cloneable {
    Element item;
    int childIndex;

    private StackItem(Element elem) {
      item = elem;
      childIndex = -1;
    }

    protected Object clone() throws CloneNotSupportedException { return super.clone(); }
  }

  public ElementIterator(Document document) {
    root = document.getDefaultRootElement();
  }

  public ElementIterator(Element root) {
    this.root = root;
  }

  public synchronized Object clone() {
    try {
      ElementIterator it = new ElementIterator(root);
      if (elementStack != null) {
        it.elementStack = new ArrayList<StackItem>();
        for (StackItem si : elementStack) {
          StackItem c = new StackItem(si.item);
          c.childIndex = si.childIndex;
          it.elementStack.add(c);
        }
      }
      return it;
    } catch (RuntimeException e) {
      throw new InternalError();
    }
  }

  public Element first() {
    if (root == null) {
      return null;
    }
    elementStack = new ArrayList<StackItem>();
    if (root.getElementCount() != 0) {
      elementStack.add(new StackItem(root));
    }
    return root;
  }

  public int depth() {
    if (elementStack == null) {
      return 0;
    }
    return elementStack.size();
  }

  public Element current() {
    if (elementStack == null) {
      return first();
    }
    if (!elementStack.isEmpty()) {
      StackItem item = elementStack.get(elementStack.size() - 1);
      Element elem = item.item;
      int index = item.childIndex;
      if (index == -1) {
        return elem;
      }
      return elem.getElement(index);
    }
    return null;
  }

  public Element next() {
    if (elementStack == null) {
      return first();
    }
    if (elementStack.isEmpty()) {
      return null;
    }
    StackItem item = elementStack.get(elementStack.size() - 1);
    Element elem = item.item;
    int index = item.childIndex;
    if (index + 1 < elem.getElementCount()) {
      item.childIndex = index + 1;
      Element child = elem.getElement(item.childIndex);
      if (child.getElementCount() != 0) {
        elementStack.add(new StackItem(child));
      }
      return child;
    }
    elementStack.remove(elementStack.size() - 1);
    return next();
  }

  public Element previous() {
    int size;
    if (elementStack == null || (size = elementStack.size()) == 0) {
      return null;
    }
    StackItem item = elementStack.get(size - 1);
    Element elem = item.item;
    int index = item.childIndex;
    if (index > 0) {
      item.childIndex = index - 1;
      return deepestLast(elem.getElement(item.childIndex));
    }
    if (index == 0) {
      item.childIndex = -1;
      return elem;
    }
    elementStack.remove(size - 1);
    return previous();
  }

  private Element deepestLast(Element e) {
    if (e.getElementCount() == 0) {
      return e;
    }
    StackItem si = new StackItem(e);
    si.childIndex = e.getElementCount() - 1;
    elementStack.add(si);
    return deepestLast(e.getElement(si.childIndex));
  }
}
