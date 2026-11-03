/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransferHandler implements Serializable {
  public static final int NONE = 0x00;
  public static final int COPY = 0x01;
  public static final int MOVE = 0x02;
  public static final int COPY_OR_MOVE = 0x03;
  public static final int LINK = 0x40000000;

  private static final Action cutAction = new TransferAction("cut");
  private static final Action copyAction = new TransferAction("copy");
  private static final Action pasteAction = new TransferAction("paste");

  private String propertyName;
  private Image dragImage;
  private Point dragImageOffset;

  public static Action getCutAction() { return cutAction; }

  public static Action getCopyAction() { return copyAction; }

  public static Action getPasteAction() { return pasteAction; }

  public TransferHandler(String property) { propertyName = property; }

  protected TransferHandler() { this(null); }

  public void setDragImage(Image img) { dragImage = img; }

  public Image getDragImage() { return dragImage; }

  public void setDragImageOffset(Point p) { dragImageOffset = new Point(p); }

  public Point getDragImageOffset() {
    if (dragImageOffset == null) {
      return new Point(0, 0);
    }
    return new Point(dragImageOffset);
  }

  public void exportAsDrag(JComponent comp, InputEvent e, int action) {
    int srcActions = getSourceActions(comp);
    if (!(e instanceof MouseEvent) || !(action == COPY || action == MOVE || action == LINK)
        || (srcActions & action) == 0) {
      action = NONE;
    }
    if (action != NONE && !java.awt.GraphicsEnvironment.isHeadless()) {
      Transferable t = createTransferable(comp);
      if (t != null) {
        return;
      } else {
        exportDone(comp, null, NONE);
      }
    } else {
      exportDone(comp, null, NONE);
    }
  }

  public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
    if ((action & getSourceActions(comp)) != 0) {
      Transferable t = createTransferable(comp);
      if (t != null) {
        try {
          clip.setContents(t, null);
          exportDone(comp, t, action);
          return;
        } catch (IllegalStateException ise) {
          exportDone(comp, t, NONE);
          throw ise;
        }
      }
    }
    exportDone(comp, null, NONE);
  }

  public boolean importData(TransferSupport support) {
    return support.getComponent() instanceof JComponent
        && importData((JComponent) support.getComponent(), support.getTransferable());
  }

  public boolean importData(JComponent comp, Transferable t) {
    PropertyDescriptor prop = getPropertyDescriptor(comp);
    if (prop != null) {
      Method writer = prop.getWriteMethod();
      if (writer == null) {
        return false;
      }
      Class<?>[] params = writer.getParameterTypes();
      if (params.length != 1) {
        return false;
      }
      DataFlavor flavor = getPropertyDataFlavor(params[0], t.getTransferDataFlavors());
      if (flavor != null) {
        try {
          Object value = t.getTransferData(flavor);
          Object[] args = {value};
          writer.invoke(comp, args);
          return true;
        } catch (InvocationTargetException ex) {
          return false;
        } catch (IllegalAccessException ex) {
          return false;
        } catch (UnsupportedFlavorException ex) {
          return false;
        } catch (IOException ex) {
          return false;
        }
      }
    }
    return false;
  }

  public boolean canImport(TransferSupport support) {
    return support.getComponent() instanceof JComponent
        && canImport((JComponent) support.getComponent(), support.getDataFlavors());
  }

  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    PropertyDescriptor prop = getPropertyDescriptor(comp);
    if (prop != null) {
      Method writer = prop.getWriteMethod();
      if (writer == null) {
        return false;
      }
      Class<?>[] params = writer.getParameterTypes();
      if (params.length != 1) {
        return false;
      }
      DataFlavor flavor = getPropertyDataFlavor(params[0], transferFlavors);
      if (flavor != null) {
        return true;
      }
    }
    return false;
  }

  public int getSourceActions(JComponent c) {
    PropertyDescriptor prop = getPropertyDescriptor(c);
    if (prop != null) {
      return COPY;
    }
    return NONE;
  }

  public Icon getVisualRepresentation(Transferable t) { return null; }

  protected Transferable createTransferable(JComponent c) {
    PropertyDescriptor property = getPropertyDescriptor(c);
    if (property != null) {
      return new PropertyTransferable(property, c);
    }
    return null;
  }

  protected void exportDone(JComponent source, Transferable data, int action) { }

  private PropertyDescriptor getPropertyDescriptor(JComponent comp) {
    if (propertyName == null) {
      return null;
    }
    Class<?> k = comp.getClass();
    BeanInfo bi;
    try {
      bi = Introspector.getBeanInfo(k);
    } catch (java.beans.IntrospectionException ex) {
      return null;
    }
    PropertyDescriptor[] props = bi.getPropertyDescriptors();
    for (int i = 0; i < props.length; i++) {
      if (propertyName.equals(props[i].getName())) {
        Method reader = props[i].getReadMethod();
        if (reader != null) {
          Class<?>[] params = reader.getParameterTypes();
          if (params == null || params.length == 0) {
            return props[i];
          }
        }
      }
    }
    return null;
  }

  private DataFlavor getPropertyDataFlavor(Class<?> k, DataFlavor[] flavors) {
    for (int i = 0; i < flavors.length; i++) {
      DataFlavor flavor = flavors[i];
      if ("application".equals(flavor.getPrimaryType()) && "x-java-jvm-local-objectref".equals(flavor.getSubType())
          && flavor.getRepresentationClass().isAssignableFrom(k)) {
        return flavor;
      }
    }
    return null;
  }

  static class PropertyTransferable implements Transferable {
    final PropertyDescriptor property;
    final JComponent component;

    PropertyTransferable(PropertyDescriptor p, JComponent c) {
      property = p;
      component = c;
    }

    public DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] flavors = new DataFlavor[1];
      Class<?> propertyType = property.getPropertyType();
      String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + propertyType.getName();
      try {
        flavors[0] = new DataFlavor(mimeType);
      } catch (ClassNotFoundException cnfe) {
        flavors = new DataFlavor[0];
      }
      return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      Class<?> propertyType = property.getPropertyType();
      if ("application".equals(flavor.getPrimaryType()) && "x-java-jvm-local-objectref".equals(flavor.getSubType())
          && flavor.getRepresentationClass().isAssignableFrom(propertyType)) {
        return true;
      }
      return false;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
      if (!isDataFlavorSupported(flavor)) {
        throw new UnsupportedFlavorException(flavor);
      }
      Method reader = property.getReadMethod();
      Object value = null;
      try {
        value = reader.invoke(component, (Object[]) null);
      } catch (Exception ex) {
        throw new IOException("Property read failed: " + property.getName());
      }
      return value;
    }
  }

  static class TransferAction extends AbstractAction implements UIResource_ {
    TransferAction(String name) { super(name); }

    public boolean isEnabled(Object sender) {
      if (sender instanceof JComponent) {
        JComponent c = (JComponent) sender;
        TransferHandler th = c.getTransferHandler();
        return th != null;
      }
      return true;
    }

    public void actionPerformed(final ActionEvent e) {
      final Object src = e.getSource();
      if (src instanceof JComponent) {
        JComponent c = (JComponent) src;
        TransferHandler th = c.getTransferHandler();
        Clipboard clipboard = getClipboard(c);
        String name = (String) getValue(Action.NAME);
        Transferable trans = null;
        try {
          if ((clipboard != null) && (th != null) && (name != null)) {
            if ("cut".equals(name)) {
              th.exportToClipboard(c, clipboard, MOVE);
            } else if ("copy".equals(name)) {
              th.exportToClipboard(c, clipboard, COPY);
            } else if ("paste".equals(name)) {
              trans = clipboard.getContents(null);
            }
          }
        } catch (IllegalStateException ise) {
          return;
        }
        if (trans != null) {
          th.importData(new TransferSupport(c, trans));
        }
      }
    }

    private Clipboard getClipboard(JComponent c) {
      try {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
      } catch (HeadlessException he) {
        return sandboxClipboard();
      }
    }
  }

  private static Clipboard sandbox;

  static synchronized Clipboard sandboxClipboard() {
    if (sandbox == null) {
      sandbox = new Clipboard("Sandboxed Component Clipboard");
    }
    return sandbox;
  }

  private interface UIResource_ { }

  interface HasGetTransferHandler {
    TransferHandler getTransferHandler();
  }

  public static final class TransferSupport {
    private boolean isDrop;
    private Component component;
    private boolean showDropLocationIsSet;
    private boolean showDropLocation;
    private int dropAction = -1;
    private Transferable transferable;

    public TransferSupport(Component component, Transferable transferable) {
      if (component == null) {
        throw new NullPointerException("component is null");
      }
      if (transferable == null) {
        throw new NullPointerException("transferable is null");
      }
      isDrop = false;
      this.component = component;
      this.transferable = transferable;
    }

    private void assureIsDrop() {
      if (!isDrop) {
        throw new IllegalStateException("Not a drop");
      }
    }

    public boolean isDrop() { return isDrop; }

    public Component getComponent() { return component; }

    public DropLocation getDropLocation() {
      assureIsDrop();
      return null;
    }

    public void setShowDropLocation(boolean showDropLocation) {
      assureIsDrop();
      this.showDropLocation = showDropLocation;
      this.showDropLocationIsSet = true;
    }

    public void setDropAction(int dropAction) {
      assureIsDrop();
      this.dropAction = dropAction;
    }

    public int getDropAction() {
      assureIsDrop();
      return dropAction;
    }

    public int getUserDropAction() {
      assureIsDrop();
      return NONE;
    }

    public int getSourceDropActions() {
      assureIsDrop();
      return NONE;
    }

    public DataFlavor[] getDataFlavors() { return transferable.getTransferDataFlavors(); }

    public boolean isDataFlavorSupported(DataFlavor df) { return transferable.isDataFlavorSupported(df); }

    public Transferable getTransferable() { return transferable; }
  }

  public static class DropLocation {
    private final Point dropPoint;

    protected DropLocation(Point dropPoint) {
      if (dropPoint == null) {
        throw new IllegalArgumentException("Point cannot be null");
      }
      this.dropPoint = new Point(dropPoint);
    }

    public final Point getDropPoint() { return new Point(dropPoint); }

    public String toString() { return getClass().getName() + "[dropPoint=" + dropPoint + "]"; }
  }
}
