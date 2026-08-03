/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package javax.swing.text;

import java.awt.Shape;
import java.util.Vector;
import javax.swing.event.DocumentEvent;

public class ZoneView extends BoxView {
  private int maxZoneSize = 8 * 1024;
  private int maxZonesLoaded = 3;
  private final Vector<View> loadedZones = new Vector<View>();

  public ZoneView(Element elem, int axis) {
    super(elem, axis);
  }

  public int getMaximumZoneSize() { return maxZoneSize; }

  public void setMaximumZoneSize(int size) { maxZoneSize = size; }

  public int getMaxZonesLoaded() { return maxZonesLoaded; }

  public void setMaxZonesLoaded(int mzl) {
    if (mzl < 1) {
      throw new IllegalArgumentException("ZoneView.setMaxZonesLoaded must be greater than 0.");
    }
    maxZonesLoaded = mzl;
    unloadOldZones();
  }

  protected void zoneWasLoaded(View zone) {
    loadedZones.addElement(zone);
    unloadOldZones();
  }

  void unloadOldZones() {
    while (loadedZones.size() > getMaxZonesLoaded()) {
      View zone = loadedZones.elementAt(0);
      loadedZones.removeElementAt(0);
      unloadZone(zone);
    }
  }

  protected void unloadZone(View zone) { zone.removeAll(); }

  protected boolean isZoneLoaded(View zone) { return loadedZones.contains(zone); }

  protected View createZone(int p0, int p1) {
    Document doc = getDocument();
    try {
      return new Zone(getElement(), doc.createPosition(p0), doc.createPosition(p1));
    } catch (BadLocationException ble) {
      throw new StateInvariantError(ble.getMessage());
    }
  }

  protected void loadChildren(ViewFactory f) {
    int offs0 = getStartOffset();
    int offs1 = getEndOffset();
    append(createZone(offs0, offs1));
    handleInsert(offs0, offs1 - offs0);
  }

  protected int getViewIndexAtPosition(int pos) {
    if (pos >= getStartOffset() && pos < getEndOffset()) {
      for (int i = 0; i < getViewCount(); i++) {
        View v = getView(i);
        if (pos >= v.getStartOffset() && pos < v.getEndOffset()) {
          return i;
        }
      }
    }
    return -1;
  }

  protected boolean updateChildren(DocumentEvent.ElementChange ec, DocumentEvent e, ViewFactory f) { return false; }

  protected void forwardUpdateToView(View v, DocumentEvent e, Shape a, ViewFactory f) {
    if (isZoneLoaded(v)) {
      super.forwardUpdateToView(v, e, a, f);
      unloadOldZones();
    }
  }

  public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    handleInsert(e.getOffset(), e.getLength());
    super.insertUpdate(e, a, f);
  }

  public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
    handleRemove(e.getOffset(), e.getLength());
    super.removeUpdate(e, a, f);
  }

  void handleInsert(int pos, int length) {
    int index = getViewIndex(pos, Position.Bias.Forward);
    View v = getView(index);
    int offs0 = v.getStartOffset();
    int offs1 = v.getEndOffset();
    if (offs1 - offs0 > maxZoneSize) {
      splitZone(index, offs0, offs1);
    }
  }

  void handleRemove(int pos, int length) { }

  void splitZone(int index, int offs0, int offs1) {
    Vector<View> zones = new Vector<View>();
    int offs = offs0;
    do {
      offs0 = offs;
      offs = Math.min(getDesiredZoneEnd(offs0), offs1);
      zones.addElement(createZone(offs0, offs));
    } while (offs < offs1);
    View[] newZones = new View[zones.size()];
    zones.copyInto(newZones);
    replace(index, 1, newZones);
  }

  int getDesiredZoneEnd(int pos) {
    Element elem = getElement();
    int index = elem.getElementIndex(pos + maxZoneSize / 2);
    Element child = elem.getElement(index);
    int offs0 = child.getStartOffset();
    int offs1 = child.getEndOffset();
    if (offs1 - pos > maxZoneSize && offs0 > pos) {
      return offs0;
    }
    return offs1;
  }

  class Zone extends BoxView {
    private final Position start;
    private final Position end;

    Zone(Element elem, Position start, Position end) {
      super(elem, ZoneView.this.getAxis());
      this.start = start;
      this.end = end;
    }

    public boolean isLoaded() { return ZoneView.this.isZoneLoaded(this); }

    public int getViewIndexAtPosition(int pos) {
      Element elem = getElement();
      return elem.getElementIndex(pos) - elem.getElementIndex(getStartOffset());
    }

    protected boolean updateChildren(DocumentEvent.ElementChange ec, DocumentEvent e, ViewFactory f) {
      Element[] removedElems = ec.getChildrenRemoved();
      Element[] addedElems = ec.getChildrenAdded();
      View[] added = new View[addedElems.length];
      for (int i = 0; i < addedElems.length; i++) {
        added[i] = f.create(addedElems[i]);
      }
      replace(ec.getIndex(), removedElems.length, added);
      return true;
    }

    public void paint(java.awt.Graphics g, Shape alloc) {
      load();
      super.paint(g, alloc);
    }

    public int getViewIndex(int pos, Position.Bias b) {
      load();
      return super.getViewIndex(pos, b);
    }

    public Shape getChildAllocation(int index, Shape a) {
      load();
      return super.getChildAllocation(index, a);
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
      load();
      return super.modelToView(pos, a, b);
    }

    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
      load();
      return super.viewToModel(x, y, a, bias);
    }

    public int getNextVisualPositionFrom(int pos, Position.Bias b, Shape a, int direction, Position.Bias[] biasRet)
        throws BadLocationException {
      load();
      return super.getNextVisualPositionFrom(pos, b, a, direction, biasRet);
    }

    private boolean loading;

    protected void loadChildren(ViewFactory f) {
      if (!loading) {
        return;
      }
      Element elem = getElement();
      int index0 = elem.getElementIndex(getStartOffset());
      int index1 = elem.getElementIndex(getEndOffset() - 1);
      View[] added = new View[index1 - index0 + 1];
      for (int i = index0; i <= index1; i++) {
        added[i - index0] = f.create(elem.getElement(i));
      }
      replace(0, 0, added);
    }

    public int getStartOffset() { return start.getOffset(); }

    public int getEndOffset() { return end.getOffset(); }

    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
      if (isLoaded()) {
        super.insertUpdate(e, a, f);
      }
    }

    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
      if (isLoaded()) {
        super.removeUpdate(e, a, f);
      }
    }

    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
      if (isLoaded()) {
        super.changedUpdate(e, a, f);
      }
    }

    private void load() {
      if (!isLoaded()) {
        loading = true;
        loadChildren(getViewFactory());
        loading = false;
        zoneWasLoaded(this);
      }
    }
  }
}
