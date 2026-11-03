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
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

public class StyleContext implements Serializable, AbstractDocument.AttributeContext {
  public static final String DEFAULT_STYLE = "default";

  private static StyleContext defaultContext;
  private static final Hashtable<Object, String> freezeKeyMap = new Hashtable<>();
  private static final Hashtable<String, Object> thawKeyMap = new Hashtable<>();

  private final Map<String, Style> styles = new LinkedHashMap<>();
  private final SmallAttributeSet emptySet = new SmallAttributeSet(new LinkedHashMap<Object, Object>());
  private final List<ChangeListener> changeListeners = new ArrayList<>();
  private final Hashtable<Font, FontMetrics> fontMetricsCache = new Hashtable<>();

  public static final StyleContext getDefaultStyleContext() {
    synchronized (StyleContext.class) {
      if (defaultContext == null) {
        defaultContext = new StyleContext();
      }
      return defaultContext;
    }
  }

  public StyleContext() {
    addStyle(DEFAULT_STYLE, null);
  }

  public Style addStyle(String nm, Style parent) {
    Style style = new NamedStyle(nm, parent);
    if (nm != null) {
      styles.put(nm, style);
    }
    return style;
  }

  public void removeStyle(String nm) { styles.remove(nm); }

  public Style getStyle(String nm) { return styles.get(nm); }

  public Enumeration<?> getStyleNames() { return Collections.enumeration(new ArrayList<>(styles.keySet())); }

  public void addChangeListener(ChangeListener l) { changeListeners.add(l); }

  public void removeChangeListener(ChangeListener l) { changeListeners.remove(l); }

  public ChangeListener[] getChangeListeners() { return changeListeners.toArray(new ChangeListener[0]); }

  public Font getFont(AttributeSet attr) {
    int style = Font.PLAIN;
    if (StyleConstants.isBold(attr)) {
      style |= Font.BOLD;
    }
    if (StyleConstants.isItalic(attr)) {
      style |= Font.ITALIC;
    }
    String family = StyleConstants.getFontFamily(attr);
    int size = StyleConstants.getFontSize(attr);
    if (StyleConstants.isSuperscript(attr) || StyleConstants.isSubscript(attr)) {
      size -= 2;
    }
    return getFont(family, style, size);
  }

  public Font getFont(String family, int style, int size) { return new Font(family, style, size); }

  public FontMetrics getFontMetrics(Font f) {
    FontMetrics fm = fontMetricsCache.get(f);
    if (fm == null) {
      fm = Toolkit.getDefaultToolkit().getFontMetrics(f);
      fontMetricsCache.put(f, fm);
    }
    return fm;
  }

  public Color getForeground(AttributeSet attr) { return StyleConstants.getForeground(attr); }

  public Color getBackground(AttributeSet attr) { return StyleConstants.getBackground(attr); }

  private static Map<Object, Object> copyOf(AttributeSet a) {
    Map<Object, Object> m = new LinkedHashMap<>();
    Enumeration<?> names = a.getAttributeNames();
    while (names.hasMoreElements()) {
      Object n = names.nextElement();
      m.put(n, a.getAttribute(n));
    }
    return m;
  }

  private AttributeSet build(Map<Object, Object> m) {
    if (m.size() > getCompressionThreshold()) {
      SimpleAttributeSet large = new SimpleAttributeSet();
      for (Map.Entry<Object, Object> e : m.entrySet()) {
        large.addAttribute(e.getKey(), e.getValue());
      }
      return large;
    }
    return new SmallAttributeSet(m);
  }

  public synchronized AttributeSet addAttribute(AttributeSet old, Object name, Object value) {
    Map<Object, Object> m = copyOf(old);
    m.put(name, value);
    return build(m);
  }

  public synchronized AttributeSet addAttributes(AttributeSet old, AttributeSet attr) {
    Map<Object, Object> m = copyOf(old);
    m.putAll(copyOf(attr));
    return build(m);
  }

  public synchronized AttributeSet removeAttribute(AttributeSet old, Object name) {
    Map<Object, Object> m = copyOf(old);
    m.remove(name);
    return build(m);
  }

  public synchronized AttributeSet removeAttributes(AttributeSet old, Enumeration<?> names) {
    Map<Object, Object> m = copyOf(old);
    while (names.hasMoreElements()) {
      m.remove(names.nextElement());
    }
    return build(m);
  }

  public synchronized AttributeSet removeAttributes(AttributeSet old, AttributeSet attrs) {
    Map<Object, Object> m = copyOf(old);
    Enumeration<?> names = attrs.getAttributeNames();
    while (names.hasMoreElements()) {
      Object n = names.nextElement();
      Object v = attrs.getAttribute(n);
      if (v != null && v.equals(m.get(n))) {
        m.remove(n);
      }
    }
    return build(m);
  }

  public AttributeSet getEmptySet() { return emptySet; }

  public void reclaim(AttributeSet a) { }

  protected int getCompressionThreshold() { return 9; }

  protected SmallAttributeSet createSmallAttributeSet(AttributeSet a) { return new SmallAttributeSet(copyOf(a)); }

  protected MutableAttributeSet createLargeAttributeSet(AttributeSet a) { return new SimpleAttributeSet(a); }

  void fireChanged() {
    ChangeEvent e = new ChangeEvent(this);
    for (ChangeListener l : changeListeners.toArray(new ChangeListener[0])) {
      l.stateChanged(e);
    }
  }

  public String toString() { return "StyleContext{" + styles.keySet() + "}"; }

  public void writeAttributes(ObjectOutputStream out, AttributeSet a) throws IOException { writeAttributeSet(out, a); }

  public void readAttributes(ObjectInputStream in, MutableAttributeSet a) throws ClassNotFoundException, IOException {
    readAttributeSet(in, a);
  }

  public static void writeAttributeSet(ObjectOutputStream out, AttributeSet attr) throws IOException {
    int count = attr.getAttributeCount();
    out.writeInt(count);
    Enumeration<?> names = attr.getAttributeNames();
    while (names.hasMoreElements()) {
      Object key = names.nextElement();
      out.writeObject(key instanceof Serializable ? key : freezeKeyMap.get(key));
      Object val = attr.getAttribute(key);
      out.writeObject(val instanceof Serializable ? val : freezeKeyMap.get(val));
    }
  }

  public static void readAttributeSet(ObjectInputStream in, MutableAttributeSet a)
      throws ClassNotFoundException, IOException {
    int n = in.readInt();
    for (int i = 0; i < n; i++) {
      Object key = in.readObject();
      Object value = in.readObject();
      if (thawKeyMap.containsKey(key)) {
        key = thawKeyMap.get(key);
      }
      if (thawKeyMap.containsKey(value)) {
        value = thawKeyMap.get(value);
      }
      a.addAttribute(key, value);
    }
  }

  public static void registerStaticAttributeKey(Object key) {
    String ioFmt = key.getClass().getName() + "." + key.toString();
    freezeKeyMap.put(key, ioFmt);
    thawKeyMap.put(ioFmt, key);
  }

  public static Object getStaticAttribute(Object key) {
    if (thawKeyMap == null || key == null) {
      return null;
    }
    return thawKeyMap.get(key);
  }

  public static Object getStaticAttributeKey(Object key) {
    return key.getClass().getName() + "." + key.toString();
  }

  static {
    Object[] keys = {StyleConstants.NameAttribute, StyleConstants.ResolveAttribute, StyleConstants.BidiLevel,
        StyleConstants.FontFamily, StyleConstants.FontSize, StyleConstants.Bold, StyleConstants.Italic,
        StyleConstants.Underline, StyleConstants.StrikeThrough, StyleConstants.Superscript, StyleConstants.Subscript,
        StyleConstants.Foreground, StyleConstants.Background, StyleConstants.FirstLineIndent,
        StyleConstants.LeftIndent, StyleConstants.RightIndent, StyleConstants.LineSpacing, StyleConstants.SpaceAbove,
        StyleConstants.SpaceBelow, StyleConstants.Alignment, StyleConstants.TabSet, StyleConstants.Orientation};
    for (Object k : keys) {
      registerStaticAttributeKey(k);
    }
  }

  public class SmallAttributeSet implements AttributeSet {
    private final Map<Object, Object> table;

    public SmallAttributeSet(Object[] attributes) {
      table = new LinkedHashMap<>();
      for (int i = 0; i + 1 < attributes.length; i += 2) {
        table.put(attributes[i], attributes[i + 1]);
      }
    }

    public SmallAttributeSet(AttributeSet attrs) { table = copyOf(attrs); }

    SmallAttributeSet(Map<Object, Object> m) { table = m; }

    public Object clone() { return this; }

    public int hashCode() {
      int code = 0;
      for (Map.Entry<Object, Object> e : table.entrySet()) {
        code ^= e.getKey().hashCode() ^ e.getValue().hashCode();
      }
      return code;
    }

    public boolean equals(Object obj) {
      if (obj instanceof AttributeSet) {
        AttributeSet attrs = (AttributeSet) obj;
        return isEqual(attrs);
      }
      return false;
    }

    public String toString() {
      StringBuilder s = new StringBuilder("{");
      for (Map.Entry<Object, Object> e : table.entrySet()) {
        Object v = e.getValue();
        s.append(e.getKey()).append('=').append(v instanceof AttributeSet ? "AttributeSet" : String.valueOf(v)).append(',');
      }
      return s.append('}').toString();
    }

    public int getAttributeCount() { return table.size(); }

    public boolean isDefined(Object key) { return table.containsKey(key); }

    public boolean isEqual(AttributeSet attr) {
      if (attr instanceof SmallAttributeSet) {
        return table.equals(((SmallAttributeSet) attr).table);
      }
      return getAttributeCount() == attr.getAttributeCount() && containsAttributes(attr);
    }

    public AttributeSet copyAttributes() { return this; }

    public Object getAttribute(Object key) {
      Object value = table.get(key);
      if (value == null) {
        AttributeSet parent = getResolveParent();
        if (parent != null) {
          value = parent.getAttribute(key);
        }
      }
      return value;
    }

    public Enumeration<?> getAttributeNames() { return Collections.enumeration(new ArrayList<>(table.keySet())); }

    public boolean containsAttribute(Object name, Object value) { return value.equals(getAttribute(name)); }

    public boolean containsAttributes(AttributeSet attrs) {
      Enumeration<?> names = attrs.getAttributeNames();
      while (names.hasMoreElements()) {
        Object n = names.nextElement();
        if (!attrs.getAttribute(n).equals(getAttribute(n))) {
          return false;
        }
      }
      return true;
    }

    public AttributeSet getResolveParent() { return (AttributeSet) table.get(StyleConstants.ResolveAttribute); }
  }

  public class NamedStyle implements Style, Serializable {
    protected EventListenerList listenerList = new EventListenerList();
    protected transient ChangeEvent changeEvent;
    private AttributeSet attributes = getEmptySet();

    public NamedStyle(String name, Style parent) {
      if (name != null) {
        setName(name);
      }
      if (parent != null) {
        setResolveParent(parent);
      }
    }

    public NamedStyle(Style parent) { this(null, parent); }

    public NamedStyle() { }

    public String toString() { return "NamedStyle:" + getName() + " " + attributes; }

    public String getName() {
      return isDefined(StyleConstants.NameAttribute) ? getAttribute(StyleConstants.NameAttribute).toString() : null;
    }

    public void setName(String name) {
      if (name != null) {
        addAttribute(StyleConstants.NameAttribute, name);
      }
    }

    public void addChangeListener(ChangeListener l) { listenerList.add(ChangeListener.class, l); }

    public void removeChangeListener(ChangeListener l) { listenerList.remove(ChangeListener.class, l); }

    public ChangeListener[] getChangeListeners() { return listenerList.getListeners(ChangeListener.class); }

    protected void fireStateChanged() {
      Object[] listeners = listenerList.getListenerList();
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] == ChangeListener.class) {
          if (changeEvent == null) {
            changeEvent = new ChangeEvent(this);
          }
          ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
        }
      }
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
      return listenerList.getListeners(listenerType);
    }

    public int getAttributeCount() { return attributes.getAttributeCount(); }

    public boolean isDefined(Object attrName) { return attributes.isDefined(attrName); }

    public boolean isEqual(AttributeSet attr) { return attributes.isEqual(attr); }

    public AttributeSet copyAttributes() {
      NamedStyle a = new NamedStyle();
      a.attributes = attributes.copyAttributes();
      return a;
    }

    public Object getAttribute(Object attrName) { return attributes.getAttribute(attrName); }

    public Enumeration<?> getAttributeNames() { return attributes.getAttributeNames(); }

    public boolean containsAttribute(Object name, Object value) { return attributes.containsAttribute(name, value); }

    public boolean containsAttributes(AttributeSet attrs) { return attributes.containsAttributes(attrs); }

    public AttributeSet getResolveParent() { return attributes.getResolveParent(); }

    public void addAttribute(Object name, Object value) {
      attributes = StyleContext.this.addAttribute(attributes, name, value);
      fireStateChanged();
    }

    public void addAttributes(AttributeSet attr) {
      attributes = StyleContext.this.addAttributes(attributes, attr);
      fireStateChanged();
    }

    public void removeAttribute(Object name) {
      attributes = StyleContext.this.removeAttribute(attributes, name);
      fireStateChanged();
    }

    public void removeAttributes(Enumeration<?> names) {
      attributes = StyleContext.this.removeAttributes(attributes, names);
      fireStateChanged();
    }

    public void removeAttributes(AttributeSet attrs) {
      if (attrs == this) {
        attributes = getEmptySet();
      } else {
        attributes = StyleContext.this.removeAttributes(attributes, attrs);
      }
      fireStateChanged();
    }

    public void setResolveParent(AttributeSet parent) {
      if (parent != null) {
        addAttribute(StyleConstants.ResolveAttribute, parent);
      } else {
        removeAttribute(StyleConstants.ResolveAttribute);
      }
    }
  }
}
