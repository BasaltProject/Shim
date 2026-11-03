/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.TreeNode;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

public abstract class AbstractDocument implements Document, Serializable {
  public static final String ParagraphElementName = "paragraph";
  public static final String ContentElementName = "content";
  public static final String SectionElementName = "section";
  public static final String BidiElementName = "bidi level";
  public static final String ElementNameAttribute = "$ename";
  protected static final String BAD_LOCATION = "document location failure";

  protected EventListenerList listenerList = new EventListenerList();

  private final Content data;
  private final AttributeContext context;
  private Dictionary<Object, Object> documentProperties;
  private DocumentFilter documentFilter;
  private int asyncPriority;
  private transient DocumentFilter.FilterBypass filterBypass;
  private transient final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private transient Thread currWriter;
  private transient int numWriters;
  private BranchElement bidiRoot;

  protected AbstractDocument(Content data) { this(data, StyleContext.getDefaultStyleContext()); }

  protected AbstractDocument(Content data, AttributeContext context) {
    this.data = data;
    this.context = context;
    bidiRoot = new BidiRootElement();
  }

  public Dictionary<Object, Object> getDocumentProperties() {
    if (documentProperties == null) {
      documentProperties = new Hashtable<>(2);
    }
    return documentProperties;
  }

  public void setDocumentProperties(Dictionary<Object, Object> x) { documentProperties = x; }

  public final Object getProperty(Object key) { return getDocumentProperties().get(key); }

  public final void putProperty(Object key, Object value) {
    if (value != null) {
      getDocumentProperties().put(key, value);
    } else {
      getDocumentProperties().remove(key);
    }
    if (key.equals(TextAttribute_RunDirection)) {
      // bidi structure is not modelled
    }
  }

  private static final Object TextAttribute_RunDirection = java.awt.font.TextAttribute.RUN_DIRECTION;

  protected void fireInsertUpdate(DocumentEvent e) {
    Object[] l = listenerList.getListenerList();
    for (int i = l.length - 2; i >= 0; i -= 2) {
      if (l[i] == DocumentListener.class) {
        ((DocumentListener) l[i + 1]).insertUpdate(e);
      }
    }
  }

  protected void fireChangedUpdate(DocumentEvent e) {
    Object[] l = listenerList.getListenerList();
    for (int i = l.length - 2; i >= 0; i -= 2) {
      if (l[i] == DocumentListener.class) {
        ((DocumentListener) l[i + 1]).changedUpdate(e);
      }
    }
  }

  protected void fireRemoveUpdate(DocumentEvent e) {
    Object[] l = listenerList.getListenerList();
    for (int i = l.length - 2; i >= 0; i -= 2) {
      if (l[i] == DocumentListener.class) {
        ((DocumentListener) l[i + 1]).removeUpdate(e);
      }
    }
  }

  protected void fireUndoableEditUpdate(UndoableEditEvent e) {
    Object[] l = listenerList.getListenerList();
    for (int i = l.length - 2; i >= 0; i -= 2) {
      if (l[i] == UndoableEditListener.class) {
        ((UndoableEditListener) l[i + 1]).undoableEditHappened(e);
      }
    }
  }

  public void addDocumentListener(DocumentListener listener) { listenerList.add(DocumentListener.class, listener); }

  public void removeDocumentListener(DocumentListener listener) {
    listenerList.remove(DocumentListener.class, listener);
  }

  public DocumentListener[] getDocumentListeners() { return listenerList.getListeners(DocumentListener.class); }

  public void addUndoableEditListener(UndoableEditListener listener) {
    listenerList.add(UndoableEditListener.class, listener);
  }

  public void removeUndoableEditListener(UndoableEditListener listener) {
    listenerList.remove(UndoableEditListener.class, listener);
  }

  public UndoableEditListener[] getUndoableEditListeners() {
    return listenerList.getListeners(UndoableEditListener.class);
  }

  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    return listenerList.getListeners(listenerType);
  }

  public int getAsynchronousLoadPriority() { return asyncPriority; }

  public void setAsynchronousLoadPriority(int p) { asyncPriority = p; }

  public void setDocumentFilter(DocumentFilter filter) { documentFilter = filter; }

  public DocumentFilter getDocumentFilter() { return documentFilter; }

  public void render(Runnable r) {
    readLock();
    try {
      r.run();
    } finally {
      readUnlock();
    }
  }

  public int getLength() { return data.length() - 1; }

  public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
    if (str == null || str.length() == 0) {
      return;
    }
    DocumentFilter filter = getDocumentFilter();
    writeLock();
    try {
      if (filter != null) {
        filter.insertString(getFilterBypass(), offs, str, a);
      } else {
        handleInsertString(offs, str, a);
      }
    } finally {
      writeUnlock();
    }
  }

  private void handleInsertString(int offs, String str, AttributeSet a) throws BadLocationException {
    if (str == null || str.length() == 0) {
      return;
    }
    UndoableEdit u = data.insertString(offs, str);
    DefaultDocumentEvent e = new DefaultDocumentEvent(offs, str.length(), DocumentEvent.EventType.INSERT);
    if (u != null) {
      e.addEdit(u);
    }
    if (a == null) {
      a = SimpleAttributeSet.EMPTY;
    }
    insertUpdate(e, a);
    e.end();
    fireInsertUpdate(e);
    if (u != null && (a == null || !a.isDefined(StyleConstants.ComposedTextAttribute))) {
      fireUndoableEditUpdate(new UndoableEditEvent(this, e));
    }
  }

  public void remove(int offs, int len) throws BadLocationException {
    DocumentFilter filter = getDocumentFilter();
    writeLock();
    try {
      if (filter != null) {
        filter.remove(getFilterBypass(), offs, len);
      } else {
        handleRemove(offs, len);
      }
    } finally {
      writeUnlock();
    }
  }

  void handleRemove(int offs, int len) throws BadLocationException {
    if (len <= 0) {
      return;
    }
    if (offs < 0 || offs + len > getLength()) {
      throw new BadLocationException("Invalid remove", getLength() + 1);
    }
    DefaultDocumentEvent e = new DefaultDocumentEvent(offs, len, DocumentEvent.EventType.REMOVE);
    removeUpdate(e);
    UndoableEdit u = data.remove(offs, len);
    if (u != null) {
      e.addEdit(u);
    }
    postRemoveUpdate(e);
    e.end();
    fireRemoveUpdate(e);
    if (u != null) {
      fireUndoableEditUpdate(new UndoableEditEvent(this, e));
    }
  }

  public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    if (length == 0 && (text == null || text.length() == 0)) {
      return;
    }
    DocumentFilter filter = getDocumentFilter();
    writeLock();
    try {
      if (filter != null) {
        filter.replace(getFilterBypass(), offset, length, text, attrs);
      } else {
        if (length > 0) {
          remove(offset, length);
        }
        if (text != null && text.length() > 0) {
          insertString(offset, text, attrs);
        }
      }
    } finally {
      writeUnlock();
    }
  }

  public String getText(int offset, int length) throws BadLocationException {
    if (length < 0) {
      throw new BadLocationException("Length must be positive", length);
    }
    return data.getString(offset, length);
  }

  public void getText(int offset, int length, Segment txt) throws BadLocationException {
    if (length < 0) {
      throw new BadLocationException("Length must be positive", length);
    }
    data.getChars(offset, length, txt);
  }

  public Position createPosition(final int offs) throws BadLocationException { return data.createPosition(offs); }

  public final Position getStartPosition() {
    try {
      return createPosition(0);
    } catch (BadLocationException bl) {
      return null;
    }
  }

  public final Position getEndPosition() {
    try {
      return createPosition(data.length());
    } catch (BadLocationException bl) {
      return null;
    }
  }

  public Element[] getRootElements() {
    return new Element[] {getDefaultRootElement(), getBidiRootElement()};
  }

  public abstract Element getDefaultRootElement();

  public abstract Element getParagraphElement(int pos);

  public Element getBidiRootElement() { return bidiRoot; }

  private DocumentFilter.FilterBypass getFilterBypass() {
    if (filterBypass == null) {
      filterBypass = new DefaultFilterBypass();
    }
    return filterBypass;
  }

  protected final AttributeContext getAttributeContext() { return context; }

  protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) { }

  protected void removeUpdate(DefaultDocumentEvent chng) { }

  protected void postRemoveUpdate(DefaultDocumentEvent chng) { }

  public void dump(PrintStream out) {
    Element root = getDefaultRootElement();
    if (root instanceof AbstractElement) {
      ((AbstractElement) root).dump(out, 0);
    }
  }

  protected final Content getContent() { return data; }

  protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
    return new LeafElement(parent, a, p0, p1);
  }

  protected Element createBranchElement(Element parent, AttributeSet a) { return new BranchElement(parent, a); }

  protected final Thread getCurrentWriter() { return currWriter; }

  protected final synchronized void writeLock() {
    Thread me = Thread.currentThread();
    while (currWriter != null && currWriter != me) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new Error("Interrupted attempt to acquire write lock");
      }
    }
    currWriter = me;
    numWriters++;
  }

  protected final synchronized void writeUnlock() {
    if (--numWriters <= 0) {
      numWriters = 0;
      currWriter = null;
      notifyAll();
    }
  }

  public final void readLock() { lock.readLock().lock(); }

  public final void readUnlock() { lock.readLock().unlock(); }

  public interface Content {
    Position createPosition(int offset) throws BadLocationException;

    int length();

    UndoableEdit insertString(int where, String str) throws BadLocationException;

    UndoableEdit remove(int where, int nitems) throws BadLocationException;

    String getString(int where, int len) throws BadLocationException;

    void getChars(int where, int len, Segment txt) throws BadLocationException;
  }

  public interface AttributeContext {
    AttributeSet addAttribute(AttributeSet old, Object name, Object value);

    AttributeSet addAttributes(AttributeSet old, AttributeSet attr);

    AttributeSet removeAttribute(AttributeSet old, Object name);

    AttributeSet removeAttributes(AttributeSet old, Enumeration<?> names);

    AttributeSet removeAttributes(AttributeSet old, AttributeSet attrs);

    AttributeSet getEmptySet();

    void reclaim(AttributeSet a);
  }

  public abstract class AbstractElement implements Element, MutableAttributeSet, Serializable, TreeNode {
    private Element parent;
    private transient AttributeSet attributes;

    public AbstractElement(Element parent, AttributeSet a) {
      this.parent = parent;
      attributes = getAttributeContext().getEmptySet();
      if (a != null) {
        addAttributes(a);
      }
    }

    public void dump(PrintStream out, int indentAmount) {
      StringBuilder pad = new StringBuilder();
      for (int i = 0; i < indentAmount; i++) {
        pad.append(' ');
      }
      out.print(pad);
      out.println("<" + getName() + (attributes.getAttributeCount() > 0 ? " " + attributes : "") + ">");
      if (isLeaf()) {
        try {
          String t = getDocument().getText(getStartOffset(), getEndOffset() - getStartOffset());
          out.println(pad + "  [" + getStartOffset() + "," + getEndOffset() + "][" + t.replace("\n", "\\n") + "]");
        } catch (BadLocationException e) {
          // ignored
        }
      } else {
        for (int i = 0; i < getElementCount(); i++) {
          ((AbstractElement) getElement(i)).dump(out, indentAmount + 2);
        }
      }
    }

    public int getAttributeCount() { return attributes.getAttributeCount(); }

    public boolean isDefined(Object attrName) { return attributes.isDefined(attrName); }

    public boolean isEqual(AttributeSet attr) { return attributes.isEqual(attr); }

    public AttributeSet copyAttributes() { return attributes.copyAttributes(); }

    public Object getAttribute(Object attrName) {
      Object value = attributes.getAttribute(attrName);
      if (value == null) {
        AttributeSet a = getResolveParent();
        if (a != null) {
          value = a.getAttribute(attrName);
        }
      }
      return value;
    }

    public Enumeration<?> getAttributeNames() { return attributes.getAttributeNames(); }

    public boolean containsAttribute(Object name, Object value) { return attributes.containsAttribute(name, value); }

    public boolean containsAttributes(AttributeSet attrs) { return attributes.containsAttributes(attrs); }

    public AttributeSet getResolveParent() {
      AttributeSet a = attributes.getResolveParent();
      if (a == null && parent != null) {
        a = parent.getAttributes();
      }
      return a;
    }

    public void addAttribute(Object name, Object value) {
      checkForIllegalCast();
      attributes = getAttributeContext().addAttribute(attributes, name, value);
    }

    public void addAttributes(AttributeSet attr) {
      checkForIllegalCast();
      attributes = getAttributeContext().addAttributes(attributes, attr);
    }

    public void removeAttribute(Object name) {
      checkForIllegalCast();
      attributes = getAttributeContext().removeAttribute(attributes, name);
    }

    public void removeAttributes(Enumeration<?> names) {
      checkForIllegalCast();
      attributes = getAttributeContext().removeAttributes(attributes, names);
    }

    public void removeAttributes(AttributeSet attrs) {
      checkForIllegalCast();
      if (attrs == this) {
        attributes = getAttributeContext().getEmptySet();
      } else {
        attributes = getAttributeContext().removeAttributes(attributes, attrs);
      }
    }

    public void setResolveParent(AttributeSet parent) {
      checkForIllegalCast();
      AttributeContext c = getAttributeContext();
      if (parent != null) {
        attributes = c.addAttribute(attributes, StyleConstants.ResolveAttribute, parent);
      } else {
        attributes = c.removeAttribute(attributes, StyleConstants.ResolveAttribute);
      }
    }

    private void checkForIllegalCast() {
      Thread t = getCurrentWriter();
      if (t == null || t != Thread.currentThread()) {
        throw new StateInvariantError("Illegal cast to MutableAttributeSet");
      }
    }

    public Document getDocument() { return AbstractDocument.this; }

    public Element getParentElement() { return parent; }

    public AttributeSet getAttributes() { return this; }

    public String getName() {
      if (attributes.isDefined(ElementNameAttribute)) {
        return (String) attributes.getAttribute(ElementNameAttribute);
      }
      return null;
    }

    public abstract int getStartOffset();

    public abstract int getEndOffset();

    public abstract Element getElement(int index);

    public abstract int getElementCount();

    public abstract int getElementIndex(int offset);

    public abstract boolean isLeaf();

    public TreeNode getChildAt(int childIndex) { return (TreeNode) getElement(childIndex); }

    public int getChildCount() { return getElementCount(); }

    public TreeNode getParent() { return (TreeNode) getParentElement(); }

    public int getIndex(TreeNode node) {
      for (int counter = getChildCount() - 1; counter >= 0; counter--) {
        if (getChildAt(counter) == node) {
          return counter;
        }
      }
      return -1;
    }

    public abstract boolean getAllowsChildren();

    public abstract Enumeration<TreeNode> children();
  }

  static class StateInvariantError extends Error {
    StateInvariantError(String s) { super(s); }
  }

  public class BranchElement extends AbstractElement {
    private Element[] children = new Element[1];
    private int nchildren;
    private int lastIndex;

    public BranchElement(Element parent, AttributeSet a) {
      super(parent, a);
      children = new Element[1];
      nchildren = 0;
      lastIndex = -1;
    }

    public Element positionToElement(int pos) {
      int index = getElementIndex(pos);
      Element child = children[index];
      int p0 = child.getStartOffset();
      int p1 = child.getEndOffset();
      if (pos >= p0 && pos < p1) {
        return child;
      }
      return null;
    }

    public void replace(int offset, int length, Element[] elems) {
      int delta = elems.length - length;
      int src = offset + length;
      int nmove = nchildren - src;
      int dest = src + delta;
      if (nchildren + delta >= children.length) {
        int newLength = Math.max(2 * children.length, nchildren + delta);
        Element[] newChildren = new Element[newLength];
        System.arraycopy(children, 0, newChildren, 0, offset);
        System.arraycopy(elems, 0, newChildren, offset, elems.length);
        System.arraycopy(children, src, newChildren, dest, nmove);
        children = newChildren;
      } else {
        System.arraycopy(children, src, children, dest, nmove);
        System.arraycopy(elems, 0, children, offset, elems.length);
      }
      nchildren = nchildren + delta;
    }

    public String toString() { return "BranchElement(" + getName() + ") " + getStartOffset() + "," + getEndOffset() + "\n"; }

    public String getName() {
      String nm = super.getName();
      if (nm == null) {
        nm = ParagraphElementName;
      }
      return nm;
    }

    public int getStartOffset() { return nchildren > 0 ? children[0].getStartOffset() : 0; }

    public int getEndOffset() {
      return nchildren > 0 ? children[nchildren - 1].getEndOffset() : getStartOffset();
    }

    public Element getElement(int index) { return index < nchildren ? children[index] : null; }

    public int getElementCount() { return nchildren; }

    public int getElementIndex(int offset) {
      if (nchildren == 0) {
        return 0;
      }
      int lo = 0;
      int hi = nchildren - 1;
      while (lo < hi) {
        int mid = (lo + hi + 1) >>> 1;
        if (children[mid].getStartOffset() <= offset) {
          lo = mid;
        } else {
          hi = mid - 1;
        }
      }
      return lo;
    }

    public boolean isLeaf() { return false; }

    public boolean getAllowsChildren() { return true; }

    public Enumeration<TreeNode> children() {
      if (nchildren == 0) {
        return java.util.Collections.emptyEnumeration();
      }
      java.util.Vector<TreeNode> tempVector = new java.util.Vector<>(nchildren);
      for (int counter = 0; counter < nchildren; counter++) {
        tempVector.addElement((TreeNode) children[counter]);
      }
      return tempVector.elements();
    }
  }

  public class LeafElement extends AbstractElement {
    private transient Position p0;
    private transient Position p1;

    public LeafElement(Element parent, AttributeSet a, int offs0, int offs1) {
      super(parent, a);
      try {
        p0 = createPosition(offs0);
        p1 = createPosition(offs1);
      } catch (BadLocationException e) {
        p0 = null;
        p1 = null;
        throw new StateInvariantError("Can't create Position references");
      }
    }

    public String toString() { return "LeafElement(" + getName() + ") " + p0 + "," + p1 + "\n"; }

    public int getStartOffset() { return p0.getOffset(); }

    public int getEndOffset() { return p1.getOffset(); }

    public String getName() {
      String nm = super.getName();
      if (nm == null) {
        nm = ContentElementName;
      }
      return nm;
    }

    public int getElementIndex(int pos) { return -1; }

    public Element getElement(int index) { return null; }

    public int getElementCount() { return 0; }

    public boolean isLeaf() { return true; }

    public boolean getAllowsChildren() { return false; }

    public Enumeration<TreeNode> children() { return null; }
  }

  private class BidiRootElement extends BranchElement {
    BidiRootElement() { super(null, null); }

    public String getName() { return BidiElementName; }

    public int getEndOffset() { return getLength() + 1; }

    public int getElementCount() { return 1; }

    public Element getElement(int index) { return index == 0 ? bidiLeaf() : null; }

    private Element bidiLeaf() {
      SimpleAttributeSet a = new SimpleAttributeSet();
      a.addAttribute(StyleConstants.BidiLevel, Integer.valueOf(0));
      return new LeafElement(this, a, 0, getLength() + 1);
    }
  }

  public class DefaultDocumentEvent extends CompoundEdit implements DocumentEvent {
    private final int offset;
    private final int length;
    private final DocumentEvent.EventType type;
    private Map<Element, ElementChange> changeLookup;

    public DefaultDocumentEvent(int offs, int len, DocumentEvent.EventType type) {
      offset = offs;
      length = len;
      this.type = type;
    }

    public String toString() { return edits.toString(); }

    public boolean addEdit(UndoableEdit anEdit) {
      if (false && changeLookup == null && edits.size() > 10) {
        changeLookup = new IdentityHashMap<>();
        for (Object o : edits) {
          if (o instanceof DocumentEvent.ElementChange) {
            changeLookup.put(((DocumentEvent.ElementChange) o).getElement(), (DocumentEvent.ElementChange) o);
          }
        }
      }
      if (changeLookup != null && anEdit instanceof DocumentEvent.ElementChange) {
        DocumentEvent.ElementChange ec = (DocumentEvent.ElementChange) anEdit;
        changeLookup.put(ec.getElement(), ec);
      }
      return super.addEdit(anEdit);
    }

    public void redo() throws CannotRedoException {
      writeLock();
      try {
        super.redo();
        UndoRedoDocumentEvent ev = new UndoRedoDocumentEvent(this, false);
        if (type == DocumentEvent.EventType.INSERT) {
          fireInsertUpdate(ev);
        } else if (type == DocumentEvent.EventType.REMOVE) {
          fireRemoveUpdate(ev);
        } else {
          fireChangedUpdate(ev);
        }
      } finally {
        writeUnlock();
      }
    }

    public void undo() throws CannotUndoException {
      writeLock();
      try {
        super.undo();
        UndoRedoDocumentEvent ev = new UndoRedoDocumentEvent(this, true);
        if (type == DocumentEvent.EventType.REMOVE) {
          fireInsertUpdate(ev);
        } else if (type == DocumentEvent.EventType.INSERT) {
          fireRemoveUpdate(ev);
        } else {
          fireChangedUpdate(ev);
        }
      } finally {
        writeUnlock();
      }
    }

    public boolean isSignificant() { return true; }

    public String getPresentationName() {
      DocumentEvent.EventType t = getType();
      if (t == DocumentEvent.EventType.INSERT) {
        return "addition";
      }
      if (t == DocumentEvent.EventType.REMOVE) {
        return "deletion";
      }
      return "style change";
    }

    public String getUndoPresentationName() { return "Undo " + getPresentationName(); }

    public String getRedoPresentationName() { return "Redo " + getPresentationName(); }

    public DocumentEvent.EventType getType() { return type; }

    public int getOffset() { return offset; }

    public int getLength() { return length; }

    public Document getDocument() { return AbstractDocument.this; }

    public DocumentEvent.ElementChange getChange(Element elem) {
      if (changeLookup != null) {
        return changeLookup.get(elem);
      }
      DocumentEvent.ElementChange found = null;
      for (int i = 0; i < edits.size(); i++) {
        Object o = edits.get(i);
        if (o instanceof DocumentEvent.ElementChange && ((DocumentEvent.ElementChange) o).getElement() == elem) {
          DocumentEvent.ElementChange next = (DocumentEvent.ElementChange) o;
          found = found == null ? next : mergeChanges(found, next);
        }
      }
      return found;
    }

    private DocumentEvent.ElementChange mergeChanges(DocumentEvent.ElementChange a, DocumentEvent.ElementChange b) {
      Element[] ra = a.getChildrenRemoved();
      Element[] aa = a.getChildrenAdded();
      Element[] rb = b.getChildrenRemoved();
      Element[] ab = b.getChildrenAdded();
      ra = ra == null ? new Element[0] : ra;
      aa = aa == null ? new Element[0] : aa;
      rb = rb == null ? new Element[0] : rb;
      ab = ab == null ? new Element[0] : ab;
      int ia = a.getIndex();
      int ib = b.getIndex();
      if (ib > ia + aa.length || ib + rb.length < ia) {
        return b;
      }
      java.util.List<Element> removed = new java.util.ArrayList<Element>();
      for (int k = 0; k < rb.length; k++) {
        if (ib + k < ia) {
          removed.add(rb[k]);
        }
      }
      for (Element e : ra) {
        removed.add(e);
      }
      for (int k = 0; k < rb.length; k++) {
        if (ib + k >= ia + aa.length) {
          removed.add(rb[k]);
        }
      }
      java.util.List<Element> added = new java.util.ArrayList<Element>();
      for (int k = 0; k < aa.length; k++) {
        if (ia + k < ib) {
          added.add(aa[k]);
        }
      }
      for (Element e : ab) {
        added.add(e);
      }
      for (int k = 0; k < aa.length; k++) {
        if (ia + k >= ib + rb.length) {
          added.add(aa[k]);
        }
      }
      return new ElementEdit(a.getElement(), Math.min(ia, ib), removed.toArray(new Element[0]),
          added.toArray(new Element[0]));
    }
  }

  static class UndoRedoDocumentEvent implements DocumentEvent {
    private final DefaultDocumentEvent src;
    private EventType type;

    UndoRedoDocumentEvent(DefaultDocumentEvent src, boolean isUndo) {
      this.src = src;
      if (isUndo) {
        if (src.getType().equals(EventType.INSERT)) {
          type = EventType.REMOVE;
        } else if (src.getType().equals(EventType.REMOVE)) {
          type = EventType.INSERT;
        } else {
          type = src.getType();
        }
      } else {
        type = src.getType();
      }
    }

    public int getLength() { return src.getLength(); }

    public int getOffset() { return src.getOffset(); }

    public EventType getType() { return type; }

    public Document getDocument() { return src.getDocument(); }

    public ElementChange getChange(Element elem) { return src.getChange(elem); }
  }

  public static class ElementEdit extends AbstractUndoableEdit implements DocumentEvent.ElementChange {
    private final Element e;
    private final int index;
    private final Element[] removed;
    private final Element[] added;

    public ElementEdit(Element e, int index, Element[] removed, Element[] added) {
      super();
      this.e = e;
      this.index = index;
      this.removed = removed;
      this.added = added;
    }

    public Element getElement() { return e; }

    public int getIndex() { return index; }

    public Element[] getChildrenRemoved() { return removed; }

    public Element[] getChildrenAdded() { return added; }

    public void redo() throws CannotRedoException {
      super.redo();
      Element[] tmp = removed;
      Element[] removed2 = added;
      ((BranchElement) e).replace(index, tmp.length, removed2);
    }

    public void undo() throws CannotUndoException {
      super.undo();
      ((BranchElement) e).replace(index, added.length, removed);
    }
  }

  private class DefaultFilterBypass extends DocumentFilter.FilterBypass {
    public Document getDocument() { return AbstractDocument.this; }

    public void remove(int offset, int length) throws BadLocationException { handleRemove(offset, length); }

    public void insertString(int offset, String string, AttributeSet attr) throws BadLocationException {
      handleInsertString(offset, string, attr);
    }

    public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
      handleRemove(offset, length);
      handleInsertString(offset, text, attrs);
    }
  }
}
