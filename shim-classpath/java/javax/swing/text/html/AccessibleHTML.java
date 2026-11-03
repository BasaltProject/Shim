/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text.html;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleTable;
import javax.accessibility.AccessibleText;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

class AccessibleHTML implements Accessible {
  private final JEditorPane editor;
  private Document model;
  private ElementInfo rootElementInfo;
  private RootHTMLAccessibleContext rootContext;
  private final DocumentListener docListener = new DocumentListener() {
    public void insertUpdate(DocumentEvent e) { invalidate(); }

    public void removeUpdate(DocumentEvent e) { invalidate(); }

    public void changedUpdate(DocumentEvent e) { invalidate(); }
  };
  private final PropertyChangeListener propListener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt) {
      if ("document".equals(evt.getPropertyName())) {
        setDocument(editor.getDocument());
      }
    }
  };

  public AccessibleHTML(JEditorPane pane) {
    editor = pane;
    setDocument(pane.getDocument());
  }

  private void setDocument(Document document) {
    if (model != null) {
      model.removeDocumentListener(docListener);
    }
    editor.removePropertyChangeListener(propListener);
    model = document;
    if (model != null) {
      model.addDocumentListener(docListener);
      editor.addPropertyChangeListener(propListener);
    }
    invalidate();
  }

  private void invalidate() { rootElementInfo = null; }

  ElementInfo getRootInfo() {
    if (rootElementInfo == null && model != null) {
      rootElementInfo = new ElementInfo(model.getDefaultRootElement(), null);
      rootElementInfo.flatten = true;
    }
    return rootElementInfo;
  }

  public AccessibleContext getAccessibleContext() {
    if (rootContext == null) {
      rootContext = new RootHTMLAccessibleContext(getRootInfo());
    }
    return rootContext;
  }

  private static boolean isTable(Element e) {
    return e.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.TABLE;
  }

  private static boolean isTag(Element e, HTML.Tag tag) {
    return e.getAttributes().getAttribute(StyleConstants.NameAttribute) == tag;
  }

  private abstract class HTMLAccessibleContext extends AccessibleContext implements Accessible, AccessibleComponent {
    protected ElementInfo elementInfo;

    HTMLAccessibleContext(ElementInfo elementInfo) {
      this.elementInfo = elementInfo;
    }

    public AccessibleContext getAccessibleContext() { return this; }

    public AccessibleStateSet getAccessibleStateSet() {
      AccessibleStateSet states = new AccessibleStateSet();
      Rectangle r = elementInfo == null ? null : elementInfo.getBounds();
      if (editor.isEnabled()) {
        states.add(AccessibleState.ENABLED);
      }
      if (editor.isVisible()) {
        states.add(AccessibleState.VISIBLE);
      }
      if (editor.isShowing()) {
        states.add(AccessibleState.SHOWING);
      }
      return states;
    }

    public int getAccessibleIndexInParent() { return elementInfo == null ? -1 : elementInfo.getIndexInParent(); }

    public int getAccessibleChildrenCount() { return elementInfo == null ? 0 : elementInfo.getChildCount(); }

    public Accessible getAccessibleChild(int i) {
      ElementInfo child = elementInfo == null ? null : elementInfo.getChild(i);
      return child != null && child instanceof Accessible ? (Accessible) child : null;
    }

    public Locale getLocale() throws java.awt.IllegalComponentStateException { return editor.getLocale(); }

    public AccessibleComponent getAccessibleComponent() { return this; }

    public String getAccessibleDescription() { return editor.getContentType(); }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.TEXT; }

    public Color getBackground() { return editor.getBackground(); }

    public void setBackground(Color c) { editor.setBackground(c); }

    public Color getForeground() { return editor.getForeground(); }

    public void setForeground(Color c) { editor.setForeground(c); }

    public Cursor getCursor() { return editor.getCursor(); }

    public void setCursor(Cursor cursor) { editor.setCursor(cursor); }

    public Font getFont() { return editor.getFont(); }

    public void setFont(Font f) { editor.setFont(f); }

    public FontMetrics getFontMetrics(Font f) { return editor.getFontMetrics(f); }

    public boolean isEnabled() { return editor.isEnabled(); }

    public void setEnabled(boolean b) { editor.setEnabled(b); }

    public boolean isVisible() { return editor.isVisible(); }

    public void setVisible(boolean b) { editor.setVisible(b); }

    public boolean isShowing() { return editor.isShowing(); }

    public boolean contains(Point p) {
      Rectangle r = getBounds();
      return r != null && r.contains(p.x, p.y);
    }

    public Point getLocationOnScreen() {
      Point editorLocation = editor.getLocationOnScreen();
      Rectangle r = getBounds();
      return r == null ? null : new Point(editorLocation.x + r.x, editorLocation.y + r.y);
    }

    public Point getLocation() {
      Rectangle r = getBounds();
      return r == null ? null : new Point(r.x, r.y);
    }

    public void setLocation(Point p) { }

    public Rectangle getBounds() { return elementInfo == null ? null : elementInfo.getBounds(); }

    public void setBounds(Rectangle r) { }

    public Dimension getSize() {
      Rectangle r = getBounds();
      return r == null ? null : r.getSize();
    }

    public void setSize(Dimension d) { }

    public Accessible getAccessibleAt(Point p) { return null; }

    public boolean isFocusTraversable() { return editor.isFocusable(); }

    public void requestFocus() { editor.requestFocus(); }

    public void addFocusListener(FocusListener l) { editor.addFocusListener(l); }

    public void removeFocusListener(FocusListener l) { editor.removeFocusListener(l); }
  }

  private class RootHTMLAccessibleContext extends HTMLAccessibleContext {
    RootHTMLAccessibleContext(ElementInfo elementInfo) {
      super(elementInfo);
    }

    public String getAccessibleName() {
      if (model != null) {
        return (String) model.getProperty(Document.TitleProperty);
      }
      return null;
    }

    public String getAccessibleDescription() { return editor.getContentType(); }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.TEXT; }
  }

  class ElementInfo {
    private final Element element;
    private final ElementInfo parent;
    private List<ElementInfo> children;
    boolean flatten;

    ElementInfo(Element element, ElementInfo parent) {
      this.element = element;
      this.parent = parent;
    }

    private void load() {
      if (children == null) {
        children = new ArrayList<ElementInfo>();
        loadChildren(element);
      }
    }

    protected void loadChildren(Element parentElement) {
      if (parentElement.isLeaf()) {
        return;
      }
      for (int i = 0; i < parentElement.getElementCount(); i++) {
        Element child = parentElement.getElement(i);
        if (child.isLeaf()) {
          children.add(isTag(child, HTML.Tag.IMG) ? new IconElementInfo(child, this) : new TextElementInfo(child, this));
        } else if (isTable(child)) {
          children.add(new TableElementInfo(child, this));
        } else {
          loadChildren(child);
        }
      }
    }

    int getChildCount() {
      load();
      return children.size();
    }

    ElementInfo getChild(int index) {
      load();
      return index >= 0 && index < children.size() ? children.get(index) : null;
    }

    int getIndexInParent() {
      if (parent == null) {
        return -1;
      }
      parent.load();
      return parent.children.indexOf(this);
    }

    Element getElement() { return element; }

    ElementInfo getParent() { return parent; }

    View getView() {
      View root = editor.getUI().getRootView(editor);
      return findView(root, element.getStartOffset(), element);
    }

    private View findView(View v, int pos, Element e) {
      if (v.getElement() == e) {
        return v;
      }
      for (int i = 0; i < v.getViewCount(); i++) {
        View found = findView(v.getView(i), pos, e);
        if (found != null) {
          return found;
        }
      }
      return null;
    }

    Rectangle getBounds() {
      try {
        Rectangle a = editor.modelToView(element.getStartOffset());
        Rectangle b = editor.modelToView(Math.max(element.getStartOffset(), element.getEndOffset() - 1));
        return a == null || b == null ? null : a.union(b);
      } catch (BadLocationException ble) {
        return null;
      }
    }

    AttributeSet getAttributes() { return element.getAttributes(); }

    Object getAttribute(Object key) { return element.getAttributes().getAttribute(key); }
  }

  class TextElementInfo extends ElementInfo implements Accessible {
    private TextAccessibleContext accessibleContext;

    TextElementInfo(Element element, ElementInfo parent) {
      super(element, parent);
    }

    public AccessibleContext getAccessibleContext() {
      if (accessibleContext == null) {
        accessibleContext = new TextAccessibleContext(this);
      }
      return accessibleContext;
    }

    public class TextAccessibleContext extends HTMLAccessibleContext implements AccessibleText {
      public TextAccessibleContext(ElementInfo elementInfo) {
        super(elementInfo);
      }

      public AccessibleText getAccessibleText() { return this; }

      public String getAccessibleName() {
        return model != null ? (String) model.getProperty(Document.TitleProperty) : null;
      }

      public int getAccessibleChildrenCount() { return 0; }

      public AccessibleRole getAccessibleRole() { return AccessibleRole.TEXT; }

      public int getIndexAtPoint(Point p) {
        View v = editor.getUI().getRootView(editor);
        return v == null ? -1 : editor.viewToModel(p);
      }

      public Rectangle getCharacterBounds(int i) {
        try {
          return editor.modelToView(i);
        } catch (BadLocationException e) {
          return null;
        }
      }

      public int getCharCount() { return elementInfo.getElement().getEndOffset() - elementInfo.getElement().getStartOffset(); }

      public int getCaretPosition() { return editor.getCaretPosition(); }

      public String getAtIndex(int part, int index) { return getText(part, index, 0); }

      public String getAfterIndex(int part, int index) { return getText(part, index, 1); }

      public String getBeforeIndex(int part, int index) { return getText(part, index, -1); }

      private String getText(int part, int index, int dir) {
        try {
          int len = model.getLength() + 1;
          if (index < 0 || index >= len) {
            return null;
          }
          switch (part) {
            case AccessibleText.CHARACTER: {
              int i = index + dir;
              return i < 0 || i >= len ? null : model.getText(i, 1);
            }
            case AccessibleText.WORD:
            case AccessibleText.SENTENCE:
            default: {
              int start = elementInfo.getElement().getStartOffset();
              int end = Math.min(elementInfo.getElement().getEndOffset(), len);
              return dir == 0 ? model.getText(start, end - start) : null;
            }
          }
        } catch (BadLocationException e) {
          return null;
        }
      }

      public AttributeSet getCharacterAttribute(int i) {
        if (model instanceof javax.swing.text.StyledDocument) {
          return ((javax.swing.text.StyledDocument) model).getCharacterElement(i).getAttributes();
        }
        return null;
      }

      public int getSelectionStart() { return editor.getSelectionStart(); }

      public int getSelectionEnd() { return editor.getSelectionEnd(); }

      public String getSelectedText() { return editor.getSelectedText(); }
    }
  }

  class IconElementInfo extends ElementInfo implements Accessible {
    private IconAccessibleContext accessibleContext;

    IconElementInfo(Element element, ElementInfo parent) {
      super(element, parent);
    }

    public AccessibleContext getAccessibleContext() {
      if (accessibleContext == null) {
        accessibleContext = new IconAccessibleContext(this);
      }
      return accessibleContext;
    }

    private String altText() {
      Object alt = getAttribute(HTML.Attribute.ALT);
      return alt == null ? null : alt.toString();
    }

    private class IconAccessibleContext extends HTMLAccessibleContext implements javax.accessibility.AccessibleIcon {
      IconAccessibleContext(ElementInfo elementInfo) {
        super(elementInfo);
      }

      public AccessibleRole getAccessibleRole() { return AccessibleRole.ICON; }

      public String getAccessibleName() { return getAccessibleIconDescription(); }

      public String getAccessibleDescription() { return editor.getContentType(); }

      public AccessibleIcon[] getAccessibleIcon() { return new AccessibleIcon[] {this}; }

      public String getAccessibleIconDescription() {
        View v = getView();
        if (v instanceof ImageView) {
          return ((ImageView) v).getAltText();
        }
        return altText();
      }

      public void setAccessibleIconDescription(String description) { }

      public int getAccessibleIconWidth() {
        View v = getView();
        return v == null ? 0 : (int) v.getPreferredSpan(View.X_AXIS);
      }

      public int getAccessibleIconHeight() {
        View v = getView();
        return v == null ? 0 : (int) v.getPreferredSpan(View.Y_AXIS);
      }
    }
  }

  class TableElementInfo extends ElementInfo implements Accessible {
    private TableAccessibleContext accessibleContext;
    private List<List<ElementInfo>> grid;
    private int columnCount;

    TableElementInfo(Element element, ElementInfo parent) {
      super(element, parent);
    }

    protected void loadChildren(Element table) {
      for (int r = 0; r < table.getElementCount(); r++) {
        Element row = table.getElement(r);
        if (isTag(row, HTML.Tag.TR)) {
          for (int c = 0; c < row.getElementCount(); c++) {
            Element cell = row.getElement(c);
            if (isTag(cell, HTML.Tag.TD) || isTag(cell, HTML.Tag.TH)) {
              addCell(cell);
            }
          }
        }
      }
    }

    private void addCell(Element cell) {
      getCellList().add(new TableCellElementInfo(cell, this));
    }

    private List<ElementInfo> cellList;

    private List<ElementInfo> getCellList() {
      if (cellList == null) {
        cellList = new ArrayList<ElementInfo>();
      }
      return cellList;
    }

    int getChildCount() {
      cells();
      return cellList.size();
    }

    ElementInfo getChild(int index) {
      cells();
      return index >= 0 && index < cellList.size() ? cellList.get(index) : null;
    }

    private void cells() {
      if (cellList == null) {
        cellList = new ArrayList<ElementInfo>();
        loadChildren(getElement());
      }
    }

    public AccessibleContext getAccessibleContext() {
      if (accessibleContext == null) {
        accessibleContext = new TableAccessibleContext(this);
      }
      return accessibleContext;
    }

    int getRowCount() { return getElement().getElementCount(); }

    int getColumnCount() {
      int max = 0;
      for (int r = 0; r < getElement().getElementCount(); r++) {
        max = Math.max(max, getElement().getElement(r).getElementCount());
      }
      return max;
    }

    class TableCellElementInfo extends TextElementInfo {
      TableCellElementInfo(Element element, ElementInfo parent) {
        super(element, parent);
      }

      boolean isHeaderCell() { return isTag(getElement(), HTML.Tag.TH); }
    }

    public class TableAccessibleContext extends HTMLAccessibleContext implements AccessibleTable {
      public TableAccessibleContext(ElementInfo elementInfo) {
        super(elementInfo);
      }

      public String getAccessibleName() {
        return AccessibleRole.TABLE.toString();
      }

      public AccessibleRole getAccessibleRole() { return AccessibleRole.TABLE; }

      public AccessibleTable getAccessibleTable() { return this; }

      public Accessible getAccessibleCaption() { return null; }

      public void setAccessibleCaption(Accessible a) { }

      public Accessible getAccessibleSummary() { return null; }

      public void setAccessibleSummary(Accessible a) { }

      public int getAccessibleRowCount() { return TableElementInfo.this.getRowCount(); }

      public int getAccessibleColumnCount() { return TableElementInfo.this.getColumnCount(); }

      public Accessible getAccessibleAt(int r, int c) {
        if (r < 0 || r >= getRowCount()) {
          return null;
        }
        Element row = getElement().getElement(r);
        if (c < 0 || c >= row.getElementCount()) {
          return null;
        }
        Element cell = row.getElement(c);
        ElementInfo info = new TextElementInfo(cell, TableElementInfo.this);
        return (Accessible) info;
      }

      public int getAccessibleRowExtentAt(int r, int c) { return 1; }

      public int getAccessibleColumnExtentAt(int r, int c) { return 1; }

      public AccessibleTable getAccessibleRowHeader() { return null; }

      public void setAccessibleRowHeader(AccessibleTable table) { }

      public AccessibleTable getAccessibleColumnHeader() { return null; }

      public void setAccessibleColumnHeader(AccessibleTable table) { }

      public Accessible getAccessibleRowDescription(int r) { return null; }

      public void setAccessibleRowDescription(int r, Accessible a) { }

      public Accessible getAccessibleColumnDescription(int c) { return null; }

      public void setAccessibleColumnDescription(int c, Accessible a) { }

      public boolean isAccessibleSelected(int r, int c) { return false; }

      public boolean isAccessibleRowSelected(int r) { return false; }

      public boolean isAccessibleColumnSelected(int c) { return false; }

      public int[] getSelectedAccessibleRows() { return new int[0]; }

      public int[] getSelectedAccessibleColumns() { return new int[0]; }

      public int getAccessibleRow(int index) {
        int cols = Math.max(1, TableElementInfo.this.getColumnCount());
        return index / cols;
      }

      public int getAccessibleColumn(int index) {
        int cols = Math.max(1, TableElementInfo.this.getColumnCount());
        return index % cols;
      }

      public int getAccessibleIndex(int r, int c) { return r * TableElementInfo.this.getColumnCount() + c; }
    }
  }
}
