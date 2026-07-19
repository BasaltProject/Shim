/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.CellRendererPane;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.plaf.UIResource;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BasicTreeUI extends javax.swing.plaf.TreeUI {
  private javax.swing.Icon bp_ExpandedIcon;
  private javax.swing.Icon bp_CollapsedIcon;
  private int bp_LeftChildIndent;
  private int bp_RightChildIndent;
  private java.awt.Dimension bp_PreferredMinSize;
  protected javax.swing.Icon collapsedIcon;
  protected javax.swing.Icon expandedIcon;
  protected int leftChildIndent;
  protected int rightChildIndent;
  protected int totalChildIndent;
  protected java.awt.Dimension preferredMinSize;
  protected int lastSelectedRow;
  protected javax.swing.JTree tree;
  protected javax.swing.tree.TreeCellRenderer currentCellRenderer;
  protected boolean createdRenderer;
  protected javax.swing.tree.TreeCellEditor cellEditor;
  protected boolean createdCellEditor;
  protected boolean stopEditingInCompleteEditing;
  protected javax.swing.CellRendererPane rendererPane;
  protected java.awt.Dimension preferredSize;
  protected boolean validCachedPreferredSize;
  protected javax.swing.tree.AbstractLayoutCache treeState;
  protected java.util.Hashtable<javax.swing.tree.TreePath, java.lang.Boolean> drawingCache;
  protected boolean largeModel;
  protected javax.swing.tree.AbstractLayoutCache.NodeDimensions nodeDimensions;
  protected javax.swing.tree.TreeModel treeModel;
  protected javax.swing.tree.TreeSelectionModel treeSelectionModel;
  protected int depthOffset;
  protected java.awt.Component editingComponent;
  protected javax.swing.tree.TreePath editingPath;
  protected int editingRow;
  protected boolean editorHasDifferentSize;
  public BasicTreeUI() {
  }
  protected boolean isLeaf(int row) {
    TreePath path = getPathForRow(tree, row);
    return path == null || tree.getModel().isLeaf(path.getLastPathComponent());
  }
  protected TreeModel getModel() {
    return tree.getModel();
  }
  protected void setModel(TreeModel model) {
    treeModel = model;
  }
  protected java.awt.event.FocusListener createFocusListener() {
    return new FocusHandler();
  }
  protected java.awt.event.MouseListener createMouseListener() {
    return new MouseHandler();
  }
  protected java.awt.event.ComponentListener createComponentListener() {
    return new ComponentHandler();
  }
  protected void paintDropLine(Graphics g) {
  }
  protected Rectangle getDropLineRect(JTree.DropLocation loc) {
    return new Rectangle();
  }
  protected void updateRenderer() {
    if (tree != null) {
      TreeCellRenderer newCellRenderer = tree.getCellRenderer();
      if (newCellRenderer == null) {
        tree.setCellRenderer(createDefaultCellRenderer());
        createdRenderer = true;
      } else {
        createdRenderer = false;
        currentCellRenderer = newCellRenderer;
      }
      currentCellRenderer = tree.getCellRenderer();
    }
  }
  protected void installKeyboardActions() {
    javax.swing.InputMap focusMap = (javax.swing.InputMap) UIManager.get("Tree.focusInputMap");
    if (focusMap != null && !tree.getComponentOrientation().isLeftToRight()) {
      javax.swing.InputMap rtl = (javax.swing.InputMap) UIManager.get("Tree.focusInputMap.RightToLeft");
      if (rtl != null) {
        javax.swing.InputMap merged = new javax.swing.plaf.InputMapUIResource();
        for (KeyStroke ks : rtl.allKeys()) {
          merged.put(ks, rtl.get(ks));
        }
        merged.setParent(focusMap);
        focusMap = merged;
      }
    }
    SwingUtilities.replaceUIInputMap(tree, JComponent.WHEN_FOCUSED, focusMap);
    SwingUtilities.replaceUIInputMap(tree, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
        (javax.swing.InputMap) UIManager.get("Tree.ancestorInputMap"));
    SwingUtilities.replaceUIActionMap(tree, createActionMap());
  }
  protected void uninstallKeyboardActions() {
    SwingUtilities.replaceUIActionMap(tree, null);
    SwingUtilities.replaceUIInputMap(tree, JComponent.WHEN_FOCUSED, null);
    SwingUtilities.replaceUIInputMap(tree, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
  }
  private javax.swing.ActionMap createActionMap() {
    javax.swing.ActionMap map = new javax.swing.plaf.ActionMapUIResource();
    String[] increments = {"selectPrevious", "selectNext"};
    for (int i = 0; i < 2; i++) {
      int dir = i == 0 ? -1 : 1;
      for (String suffix : new String[] {"", "ExtendSelection", "ChangeLead"}) {
        map.put(increments[i] + suffix, new TreeIncrementAction(dir, increments[i] + suffix));
      }
    }
    for (String base : new String[] {"selectFirst", "selectLast"}) {
      int dir = base.equals("selectFirst") ? -1 : 1;
      for (String suffix : new String[] {"", "ExtendSelection", "ChangeLead"}) {
        map.put(base + suffix, new TreeHomeAction(dir, base + suffix));
      }
    }
    for (String base : new String[] {"scrollUp", "scrollDown"}) {
      int dir = base.equals("scrollUp") ? -1 : 1;
      for (String suffix : new String[] {"ChangeSelection", "ExtendSelection", "ChangeLead"}) {
        map.put(base + suffix, new TreePageAction(dir, base + suffix));
      }
    }
    map.put("selectChildChangeLead", new TreeTraverseAction(1, "selectChildChangeLead"));
    map.put("selectParentChangeLead", new TreeTraverseAction(-1, "selectParentChangeLead"));
    map.put("moveSelectionToParent", new AbstractTreeAction("moveSelectionToParent") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        TreePath lead = tree.getLeadSelectionPath();
        TreePath parent = lead == null ? null : lead.getParentPath();
        int row = parent == null ? -1 : tree.getRowForPath(parent);
        if (row >= 0) {
          tree.setSelectionRow(row);
          ensureRowsAreVisible(row, row);
        }
      }
    });
    map.put("selectChild", new TreeTraverseAction(1, "selectChild"));
    map.put("selectParent", new TreeTraverseAction(-1, "selectParent"));
    map.put("toggle", new TreeToggleAction("toggle"));
    map.put("cancel", new TreeCancelEditingAction("cancel"));
    map.put("expand", new AbstractTreeAction("expand") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        TreePath lead = tree.getLeadSelectionPath();
        if (lead != null) {
          tree.expandPath(lead);
        }
      }
    });
    map.put("collapse", new AbstractTreeAction("collapse") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        TreePath lead = tree.getLeadSelectionPath();
        if (lead != null) {
          tree.collapsePath(lead);
        }
      }
    });
    map.put("scrollLeft", new HorizontalScrollAction("scrollLeft", -1, false));
    map.put("scrollRight", new HorizontalScrollAction("scrollRight", 1, false));
    map.put("scrollLeftChangeLead", new HorizontalScrollAction("scrollLeftChangeLead", -1, true));
    map.put("scrollRightChangeLead", new HorizontalScrollAction("scrollRightChangeLead", 1, true));
    map.put("scrollLeftExtendSelection", new HorizontalScrollAction("scrollLeftExtendSelection", -1, true));
    map.put("scrollRightExtendSelection", new HorizontalScrollAction("scrollRightExtendSelection", 1, true));
    map.put("addToSelection", new AbstractTreeAction("addToSelection") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        TreePath lead = tree.getLeadSelectionPath();
        if (lead != null && !tree.isPathSelected(lead)) {
          tree.addSelectionPath(lead);
        }
      }
    });
    map.put("toggleAndAnchor", new AbstractTreeAction("toggleAndAnchor") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        TreePath lead = tree.getLeadSelectionPath();
        if (lead != null) {
          if (tree.isPathSelected(lead)) {
            tree.removeSelectionPath(lead);
          } else {
            tree.addSelectionPath(lead);
          }
          tree.setAnchorSelectionPath(lead);
        }
      }
    });
    map.put("extendTo", new AbstractTreeAction("extendTo") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        int row = tree.getLeadSelectionRow();
        if (row >= 0) {
          moveTo(tree, row, true);
        }
      }
    });
    map.put("moveSelectionTo", new AbstractTreeAction("moveSelectionTo") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        int row = tree.getLeadSelectionRow();
        if (row >= 0) {
          tree.setSelectionRow(row);
        }
      }
    });
    map.put("selectAll", new AbstractTreeAction("selectAll") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        int n = tree.getRowCount();
        if (n > 0 && tree.getSelectionModel().getSelectionMode() != TreeSelectionModel.SINGLE_TREE_SELECTION) {
          TreePath lead = tree.getLeadSelectionPath();
          TreePath anchor = tree.getAnchorSelectionPath();
          tree.setSelectionInterval(0, n - 1);
          tree.setLeadSelectionPath(lead);
          tree.setAnchorSelectionPath(anchor);
        }
      }
    });
    map.put("clearSelection", new AbstractTreeAction("clearSelection") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        TreePath lead = tree.getLeadSelectionPath();
        TreePath anchor = tree.getAnchorSelectionPath();
        tree.clearSelection();
        tree.setLeadSelectionPath(lead);
        tree.setAnchorSelectionPath(anchor);
      }
    });
    map.put("startEditing", new AbstractTreeAction("startEditing") {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        TreePath lead = tree.getLeadSelectionPath();
        if (lead != null) {
          tree.startEditingAtPath(lead);
        }
      }
    });
    map.put("copy", javax.swing.TransferHandler.getCopyAction());
    map.put("cut", javax.swing.TransferHandler.getCutAction());
    map.put("paste", javax.swing.TransferHandler.getPasteAction());
    return map;
  }
  private class HorizontalScrollAction extends AbstractTreeAction {
    private final int direction;
    HorizontalScrollAction(String name, int direction, boolean unused) {
      super(name);
      this.direction = direction;
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
      Rectangle r = tree.getVisibleRect();
      r.x += direction * 8;
      tree.scrollRectToVisible(r);
    }
  }
  private abstract class AbstractTreeAction extends javax.swing.AbstractAction {
    AbstractTreeAction(String name) {
      super(name);
    }
    public boolean isEnabled() {
      return tree != null && tree.isEnabled();
    }
  }
  protected void installDefaults() {
    if (tree.getBackground() == null || tree.getBackground() instanceof UIResource) {
      tree.setBackground(UIManager.getColor("Tree.background"));
    }
    if (tree.getForeground() == null || tree.getForeground() instanceof UIResource) {
      tree.setForeground(UIManager.getColor("Tree.foreground"));
    }
    if (tree.getFont() == null || tree.getFont() instanceof UIResource) {
      tree.setFont(UIManager.getFont("Tree.font"));
    }
    setExpandedIcon(UIManager.getIcon("Tree.expandedIcon"));
    setCollapsedIcon(UIManager.getIcon("Tree.collapsedIcon"));
    setLeftChildIndent(UIManager.getInt("Tree.leftChildIndent"));
    setRightChildIndent(UIManager.getInt("Tree.rightChildIndent"));
    totalChildIndent = leftChildIndent + rightChildIndent;
    setRowHeight(UIManager.getInt("Tree.rowHeight"));
    largeModel = tree.isLargeModel();
    hashColor = UIManager.getColor("Tree.hash");
    if (hashColor == null) {
      hashColor = Color.GRAY;
    }
    Object lines = UIManager.get("Tree.paintLines");
    paintLines = lines == null || ((Boolean) lines).booleanValue();
    lineTypeDashed = UIManager.getBoolean("Tree.lineTypeDashed");
    LookAndFeel.installProperty(tree, "opaque", Boolean.TRUE);
  }
  protected void installListeners() {
    propertyChangeListener = createPropertyChangeListener();
    tree.addPropertyChangeListener(propertyChangeListener);
    mouseListener = createMouseListener();
    tree.addMouseListener(mouseListener);
    focusListener = createFocusListener();
    tree.addFocusListener(focusListener);
    selectionListener = createTreeSelectionListener();
    if (treeSelectionModel != null) {
      treeSelectionModel.addTreeSelectionListener(selectionListener);
    }
    expansionListener = createTreeExpansionListener();
    tree.addTreeExpansionListener(expansionListener);
    modelListener = createTreeModelListener();
    if (treeModel != null) {
      treeModel.addTreeModelListener(modelListener);
    }
    keyListener = createKeyListener();
    tree.addKeyListener(keyListener);
    componentListener = createComponentListener();
    selectionModelListener = createSelectionModelPropertyChangeListener();
    if (treeSelectionModel != null && selectionModelListener != null) {
      treeSelectionModel.addPropertyChangeListener(selectionModelListener);
    }
    editorListener = createCellEditorListener();
    if (cellEditor != null) {
      cellEditor.addCellEditorListener(editorListener);
    }
  }
  protected java.awt.event.KeyListener createKeyListener() {
    return new KeyHandler();
  }
  protected void uninstallListeners() {
    tree.removePropertyChangeListener(propertyChangeListener);
    tree.removeMouseListener(mouseListener);
    tree.removeFocusListener(focusListener);
    if (treeSelectionModel != null) {
      treeSelectionModel.removeTreeSelectionListener(selectionListener);
    }
    tree.removeTreeExpansionListener(expansionListener);
    if (treeModel != null) {
      treeModel.removeTreeModelListener(modelListener);
    }
    tree.removeKeyListener(keyListener);
    tree.removeComponentListener(componentListener);
    componentListener = null;
    if (treeSelectionModel != null && selectionModelListener != null) {
      treeSelectionModel.removePropertyChangeListener(selectionModelListener);
    }
    if (cellEditor != null && editorListener != null) {
      cellEditor.removeCellEditorListener(editorListener);
    }
  }
  protected void uninstallDefaults() {
    if (tree.getTransferHandler() instanceof UIResource) {
      tree.setTransferHandler(null);
    }
  }
  protected void installComponents() {
    if ((rendererPane = createCellRendererPane()) != null) {
      tree.add(rendererPane);
    }
  }
  protected PropertyChangeListener createPropertyChangeListener() {
    return new PropertyChangeHandler();
  }
  protected void uninstallComponents() {
    if (rendererPane != null) {
      tree.remove(rendererPane);
    }
  }
  protected void setRootVisible(boolean newValue) {
    updateDepthOffset();
  }
  protected void setShowsRootHandles(boolean newValue) {
    updateDepthOffset();
  }
  protected TreeCellEditor getCellEditor() {
    return tree.getCellEditor();
  }
  protected TreeModelListener createTreeModelListener() {
    return new TreeModelHandler();
  }
  public TreePath getPathForRow(JTree tree, int row) {
    return tree.getPathForRow(row);
  }
  public int getRowCount(JTree tree) {
    return tree.getRowCount();
  }
  public Rectangle getPathBounds(JTree tree, TreePath path) {
    return tree.getPathBounds(path);
  }
  public int getRowForPath(JTree tree, TreePath path) {
    return tree.getRowForPath(path);
  }
  public TreePath getClosestPathForLocation(JTree tree, int x, int y) {
    return tree.getClosestPathForLocation(x, y);
  }
  public boolean isEditing(JTree tree) {
    return editingComponent != null;
  }
  public boolean stopEditing(JTree tree) {
    if (editingComponent != null && cellEditor != null) {
      if (cellEditor.stopCellEditing()) {
        completeEditing(false, false, false);
        return true;
      }
      return false;
    }
    return true;
  }
  public void cancelEditing(JTree tree) {
    if (editingComponent != null) {
      completeEditing(false, true, false);
    }
  }
  public void startEditingAtPath(JTree tree, TreePath path) {
    tree.scrollPathToVisible(path);
    if (path != null && tree.isVisible(path)) {
      startEditing(path, null);
    }
  }
  public TreePath getEditingPath(JTree tree) {
    return editingPath;
  }
  protected int getRowHeight() {
    return tree.getRowHeight();
  }
  protected void setRowHeight(int rowHeight) {
  }
  protected void setCellRenderer(TreeCellRenderer tcr) {
    currentCellRenderer = tcr;
  }
  protected void setCellEditor(TreeCellEditor editor) {
    cellEditor = editor;
  }
  protected boolean isRootVisible() {
    return tree.isRootVisible();
  }
  protected boolean getShowsRootHandles() {
    return tree.getShowsRootHandles();
  }
  protected void setLargeModel(boolean largeModel) {
    this.largeModel = largeModel;
  }
  protected boolean isLargeModel() {
    return largeModel;
  }
  protected int getLeadSelectionRow() {
    return tree.getLeadSelectionRow();
  }
  protected void completeEditing(boolean messageStop, boolean messageCancel, boolean messageTree) {
    if (stopEditingInCompleteEditing && editingComponent != null) {
      Component oldComponent = editingComponent;
      TreePath oldPath = editingPath;
      TreeCellEditor oldEditor = cellEditor;
      Object newValue = oldEditor.getCellEditorValue();
      Rectangle r = tree.getPathBounds(editingPath);
      boolean isFocused = tree.isFocusOwner();
      editingPath = null;
      editingComponent = null;
      if (messageStop) {
        oldEditor.stopCellEditing();
      } else if (messageCancel) {
        oldEditor.cancelCellEditing();
      }
      tree.remove(oldComponent);
      if (messageTree && oldPath != null) {
        tree.getModel().valueForPathChanged(oldPath, newValue);
      }
      if (r != null) {
        tree.repaint(r.x, r.y, r.width, r.height);
      }
      if (isFocused) {
        tree.requestFocus();
      }
    }
  }
  protected void completeEditing() {
    if (tree != null && tree.getInvokesStopCellEditing() && stopEditingInCompleteEditing && editingComponent != null) {
      cellEditor.stopCellEditing();
    }
    completeEditing(false, true, false);
  }
  protected javax.swing.tree.AbstractLayoutCache createLayoutCache() {
    return new javax.swing.tree.VariableHeightLayoutCache();
  }
  protected void configureLayoutCache() {
  }
  protected void updateDepthOffset() {
    if (isRootVisible()) {
      depthOffset = getShowsRootHandles() ? 1 : 0;
    } else if (!getShowsRootHandles()) {
      depthOffset = -1;
    } else {
      depthOffset = 0;
    }
  }
  protected void updateCellEditor() {
    completeEditing();
    if (tree == null) {
      cellEditor = null;
    } else if (tree.isEditable()) {
      TreeCellEditor newEditor = tree.getCellEditor();
      if (newEditor == null) {
        newEditor = createDefaultCellEditor();
        if (newEditor != null) {
          tree.setCellEditor(newEditor);
          createdCellEditor = true;
        }
      } else {
        createdCellEditor = false;
      }
      cellEditor = newEditor;
    } else {
      cellEditor = null;
      createdCellEditor = false;
    }
  }
  protected void prepareForUIInstall() {
    drawingCache = new java.util.Hashtable<TreePath, Boolean>(7);
    treeModel = tree.getModel();
    treeSelectionModel = tree.getSelectionModel();
    stopEditingInCompleteEditing = true;
    lastSelectedRow = -1;
    leftChildIndent = 7;
    rightChildIndent = 13;
  }
  protected void completeUIInstall() {
    setShowsRootHandles(tree.getShowsRootHandles());
    updateRenderer();
    updateDepthOffset();
    updateCellEditor();
    updateLayoutCacheExpandedNodes();
    tree.revalidate();
  }
  protected java.awt.Color getHashColor() {
    return hashColor;
  }
  protected void setHashColor(java.awt.Color color) {
    hashColor = color;
  }
  public void setExpandedIcon(Icon newG) {
    expandedIcon = newG;
  }
  public void setCollapsedIcon(Icon newG) {
    collapsedIcon = newG;
  }
  public void setLeftChildIndent(int newAmount) {
    leftChildIndent = newAmount;
    totalChildIndent = leftChildIndent + rightChildIndent;
  }
  public void setRightChildIndent(int newAmount) {
    rightChildIndent = newAmount;
    totalChildIndent = leftChildIndent + rightChildIndent;
  }
  protected TreeExpansionListener createTreeExpansionListener() {
    return new TreeExpansionHandler();
  }
  protected PropertyChangeListener createSelectionModelPropertyChangeListener() {
    return new SelectionModelPropertyChangeHandler();
  }
  protected TreeSelectionListener createTreeSelectionListener() {
    return new TreeSelectionHandler();
  }
  protected CellRendererPane createCellRendererPane() {
    return new CellRendererPane();
  }
  protected void prepareForUIUninstall() {
    completeEditing();
  }
  protected void completeUIUninstall() {
    if (createdRenderer) {
      tree.setCellRenderer(null);
    }
    if (createdCellEditor) {
      tree.setCellEditor(null);
    }
    cellEditor = null;
    currentCellRenderer = null;
    rendererPane = null;
    componentListenerCleanup();
    treeState = null;
    drawingCache = null;
    tree = null;
  }
  protected TreeCellRenderer createDefaultCellRenderer() {
    return new javax.swing.tree.DefaultTreeCellRenderer();
  }
  protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, TreePath path) {
    int depth = path.getPathCount() - 1;
    if (depth == 0 && !getShowsRootHandles() && !isRootVisible()) {
      return;
    }
    int lineX = getRowX(-1, depth + 1);
    if (tree.getComponentOrientation().isLeftToRight()) {
      lineX = lineX - getRightChildIndent() + insets.left;
    } else {
      lineX = tree.getWidth() - lineX - insets.right + getRightChildIndent() - 1;
    }
    Rectangle parentBounds = tree.getPathBounds(path);
    Rectangle lastChildBounds = tree.getPathBounds(getLastChildPath(path));
    if (lastChildBounds == null) {
      return;
    }
    int top;
    if (parentBounds == null) {
      top = Math.max(insets.top + getVerticalLegBuffer(), clipBounds.y);
    } else {
      top = Math.max(parentBounds.y + parentBounds.height + getVerticalLegBuffer(), clipBounds.y);
    }
    if (path.getPathCount() == 1 && !isRootVisible()) {
      Rectangle rb = tree.getRowBounds(0);
      if (rb != null) {
        top = Math.max(top, rb.y + rb.height / 2);
      }
    }
    int bottom = Math.min(lastChildBounds.y + lastChildBounds.height / 2, clipBounds.y + clipBounds.height);
    if (top <= bottom) {
      paintVerticalLine(g, tree, lineX, top, bottom);
    }
  }
  protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
    int depth = path.getPathCount() - 1;
    if ((depth == 0 || (depth == 1 && !isRootVisible())) && !getShowsRootHandles()) {
      return;
    }
    int clipLeft = clipBounds.x;
    int clipRight = clipBounds.x + clipBounds.width;
    int clipTop = clipBounds.y;
    int clipBottom = clipBounds.y + clipBounds.height;
    int lineY = bounds.y + bounds.height / 2;
    if (tree.getComponentOrientation().isLeftToRight()) {
      int leftX = bounds.x - getRightChildIndent();
      int nodeX = bounds.x - getHorizontalLegBuffer();
      if (lineY >= clipTop && lineY < clipBottom && nodeX >= clipLeft && leftX < clipRight && leftX < nodeX) {
        paintHorizontalLine(g, tree, lineY, leftX, nodeX - 1);
      }
    } else {
      int nodeX = bounds.x + bounds.width + getHorizontalLegBuffer();
      int rightX = bounds.x + bounds.width + getRightChildIndent();
      if (lineY >= clipTop && lineY < clipBottom && rightX >= clipLeft && nodeX < clipRight && nodeX < rightX) {
        paintHorizontalLine(g, tree, lineY, nodeX, rightX - 1);
      }
    }
  }
  protected boolean shouldPaintExpandControl(TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
    if (isLeaf) {
      return false;
    }
    int depth = path.getPathCount() - 1;
    if ((depth == 0 || (depth == 1 && !isRootVisible())) && !getShowsRootHandles()) {
      return false;
    }
    return true;
  }
  protected void paintExpandControl(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
    if (!shouldPaintExpandControl(path, row, isExpanded, hasBeenExpanded, isLeaf)) {
      return;
    }
    int middleXOfKnob;
    if (tree.getComponentOrientation().isLeftToRight()) {
      middleXOfKnob = bounds.x - getRightChildIndent() + 1;
    } else {
      middleXOfKnob = bounds.x + bounds.width + getRightChildIndent() - 1;
    }
    int middleYOfKnob = bounds.y + bounds.height / 2;
    Icon icon = isExpanded ? getExpandedIcon() : getCollapsedIcon();
    if (icon != null) {
      drawCentered(tree, g, icon, middleXOfKnob, middleYOfKnob);
    }
  }
  protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
    if (editingComponent != null && editingRow == row) {
      return;
    }
    int leadIndex = tree.hasFocus() ? tree.getLeadSelectionRow() : -1;
    Component component = currentCellRenderer.getTreeCellRendererComponent(tree, path.getLastPathComponent(),
        tree.isRowSelected(row), isExpanded, isLeaf, row, leadIndex == row);
    rendererPane.paintComponent(g, component, tree, bounds.x, bounds.y, bounds.width, bounds.height, true);
  }
  protected boolean isDropLine(JTree.DropLocation loc) {
    return loc != null && loc.getPath() != null && loc.getChildIndex() != -1;
  }
  public int getRightChildIndent() {
    return rightChildIndent;
  }
  protected int getHorizontalLegBuffer() {
    return 3;
  }
  protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right) {
    g.setColor(hashColor);
    if (lineTypeDashed) {
      drawDashedHorizontalLine(g, y, left, right);
    } else {
      g.drawLine(left, y, right, y);
    }
  }
  protected int getRowX(int row, int depth) {
    return totalChildIndent * (depth + depthOffset);
  }
  protected TreePath getLastChildPath(TreePath parent) {
    TreeModel model = tree.getModel();
    Object last = parent.getLastPathComponent();
    int n = model.getChildCount(last);
    return n == 0 ? null : parent.pathByAddingChild(model.getChild(last, n - 1));
  }
  protected int getVerticalLegBuffer() {
    return 0;
  }
  protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom) {
    g.setColor(hashColor);
    if (lineTypeDashed) {
      drawDashedVerticalLine(g, x, top, bottom);
    } else {
      g.drawLine(x, top, x, bottom);
    }
  }
  public Icon getExpandedIcon() {
    return expandedIcon;
  }
  protected void drawCentered(Component c, Graphics graphics, Icon icon, int x, int y) {
    icon.paintIcon(c, graphics, x - icon.getIconWidth() / 2, y - icon.getIconHeight() / 2);
  }
  public Icon getCollapsedIcon() {
    return collapsedIcon;
  }
  protected void drawDashedVerticalLine(Graphics g, int x, int y1, int y2) {
    if (y1 < y2) {
      for (int y = y1 + (y1 % 2); y <= y2; y += 2) {
        g.drawLine(x, y, x, y);
      }
    }
  }
  protected void drawDashedHorizontalLine(Graphics g, int y, int x1, int x2) {
    if (x1 < x2) {
      for (int x = x1 + (x1 % 2); x <= x2; x += 2) {
        g.drawLine(x, y, x, y);
      }
    }
  }
  protected void updateExpandedDescendants(TreePath path) {
  }
  protected void updateLayoutCacheExpandedNodes() {
  }
  protected void updateLeadSelectionRow() {
    leadRow = tree.getLeadSelectionRow();
  }
  protected TreeCellEditor createDefaultCellEditor() {
    if (currentCellRenderer != null && (currentCellRenderer instanceof javax.swing.tree.DefaultTreeCellRenderer)) {
      return new javax.swing.tree.DefaultTreeCellEditor(tree, (javax.swing.tree.DefaultTreeCellRenderer) currentCellRenderer);
    }
    return new javax.swing.tree.DefaultTreeCellEditor(tree, new javax.swing.tree.DefaultTreeCellRenderer());
  }
  protected javax.swing.event.CellEditorListener createCellEditorListener() {
    return new CellEditorHandler();
  }
  protected javax.swing.tree.AbstractLayoutCache.NodeDimensions createNodeDimensions() {
    return new NodeDimensionsHandler();
  }
  public Dimension getPreferredMinSize() {
    return preferredMinSize == null ? null : new Dimension(preferredMinSize);
  }
  protected void updateCachedPreferredSize() {
    validCachedPreferredSize = true;
  }
  protected boolean isLocationInExpandControl(TreePath path, int mouseX, int mouseY) {
    if (path != null && !tree.getModel().isLeaf(path.getLastPathComponent())) {
      Insets in = tree.getInsets();
      Rectangle b = tree.getPathBounds(path);
      if (b == null) {
        return false;
      }
      int boxWidth = getExpandedIcon() != null ? getExpandedIcon().getIconWidth() : 8;
      int boxLeft = b.x - getRightChildIndent() + 1 + 0 - boxWidth / 2 + 0;
      if (!tree.getComponentOrientation().isLeftToRight()) {
        boxLeft = b.x + b.width + getRightChildIndent() - 1 - boxWidth / 2;
      }
      boxLeft = Math.max(boxLeft, in.left);
      return mouseX >= boxLeft && mouseX < boxLeft + boxWidth;
    }
    return false;
  }
  protected void handleExpandControlClick(TreePath path, int mouseX, int mouseY) {
    toggleExpandState(path);
  }
  protected void toggleExpandState(TreePath path) {
    if (!tree.isExpanded(path)) {
      tree.expandPath(path);
    } else {
      tree.collapsePath(path);
    }
  }
  protected void ensureRowsAreVisible(int beginRow, int endRow) {
    if (beginRow >= 0 && endRow < tree.getRowCount()) {
      Rectangle b = tree.getRowBounds(beginRow);
      Rectangle e = tree.getRowBounds(endRow);
      if (b != null && e != null) {
        tree.scrollRectToVisible(b.union(e));
      }
    }
  }
  protected boolean isMultiSelectEvent(MouseEvent event) {
    return SwingUtilities.isLeftMouseButton(event) && event.isShiftDown();
  }
  protected boolean isToggleSelectionEvent(MouseEvent event) {
    return SwingUtilities.isLeftMouseButton(event) && event.isControlDown();
  }
  protected boolean isToggleEvent(MouseEvent event) {
    if (!SwingUtilities.isLeftMouseButton(event)) {
      return false;
    }
    int clickCount = tree.getToggleClickCount();
    if (clickCount <= 0) {
      return false;
    }
    return event.getClickCount() % clickCount == 0;
  }
  public int getLeftChildIndent() {
    return leftChildIndent;
  }
  protected void pathWasExpanded(TreePath path) {
    tree.revalidate();
    tree.repaint();
  }
  protected void pathWasCollapsed(TreePath path) {
    tree.revalidate();
    tree.repaint();
  }
  public void setPreferredMinSize(Dimension newSize) {
    preferredMinSize = newSize;
  }
  protected void checkForClickInExpandControl(TreePath path, int mouseX, int mouseY) {
    if (isLocationInExpandControl(path, mouseX, mouseY)) {
      handleExpandControlClick(path, mouseX, mouseY);
    }
  }
  protected void selectPathForEvent(TreePath path, MouseEvent event) {
    if (isToggleSelectionEvent(event)) {
      if (tree.isPathSelected(path)) {
        tree.removeSelectionPath(path);
      } else {
        tree.addSelectionPath(path);
        tree.setAnchorSelectionPath(path);
      }
    } else if (isMultiSelectEvent(event)) {
      TreePath anchor = tree.getAnchorSelectionPath();
      int anchorRow = anchor == null ? -1 : tree.getRowForPath(anchor);
      if (anchorRow == -1 || tree.getSelectionModel().getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION) {
        tree.setSelectionPath(path);
      } else {
        int row = tree.getRowForPath(path);
        TreePath lastAnchor = anchor;
        tree.setSelectionInterval(Math.min(anchorRow, row), Math.max(anchorRow, row));
        tree.setAnchorSelectionPath(lastAnchor);
        tree.setLeadSelectionPath(path);
      }
    } else if (SwingUtilities.isLeftMouseButton(event)) {
      tree.setSelectionPath(path);
    }
  }
  protected TreeCellRenderer getCellRenderer() {
    return currentCellRenderer;
  }
  public static javax.swing.plaf.ComponentUI createUI(JComponent x) {
    return new BasicTreeUI();
  }
  protected void updateSize() {
    validCachedPreferredSize = false;
    tree.treeDidChange();
  }
  protected boolean startEditing(TreePath path, MouseEvent event) {
    completeEditing();
    if (cellEditor != null && tree.isPathEditable(path)) {
      int row = tree.getRowForPath(path);
      if (cellEditor.isCellEditable(event)) {
        editingComponent = cellEditor.getTreeCellEditorComponent(tree, path.getLastPathComponent(),
            tree.isPathSelected(path), tree.isExpanded(path), tree.getModel().isLeaf(path.getLastPathComponent()), row);
        Rectangle nodeBounds = tree.getPathBounds(path);
        editingRow = row;
        editingComponent.setBounds(nodeBounds);
        tree.add(editingComponent);
        editingPath = path;
        editorHasDifferentSize = false;
        if (cellEditor.shouldSelectCell(event)) {
          stopEditingInCompleteEditing = false;
          tree.setSelectionRow(row);
          stopEditingInCompleteEditing = true;
        }
        return true;
      }
    }
    return false;
  }
  protected void setEditable(boolean newValue) {
    updateCellEditor();
  }
  protected boolean isEditable() {
    return tree.isEditable();
  }
  public Dimension getMaximumSize(JComponent c) {
    return getPreferredSize(c);
  }
  public void paint(Graphics g, JComponent c) {
    if (tree != c || currentCellRenderer == null) {
      return;
    }
    Rectangle clipBounds = g.getClipBounds();
    if (clipBounds == null) {
      clipBounds = new Rectangle(0, 0, tree.getWidth(), tree.getHeight());
    }
    Insets insets = tree.getInsets();
    int rows = tree.getRowCount();
    if (rows == 0) {
      return;
    }
    int first = tree.getClosestRowForLocation(clipBounds.x, clipBounds.y);
    int last = tree.getClosestRowForLocation(clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height);
    if (first < 0) {
      return;
    }
    boolean lines = paintLines && !"None".equals(tree.getClientProperty("JTree.lineStyle"));
    java.util.HashSet<TreePath> done = new java.util.HashSet<TreePath>();
    for (int row = first; row <= last; row++) {
      TreePath path = tree.getPathForRow(row);
      if (path == null) {
        continue;
      }
      Rectangle bounds = tree.getPathBounds(path);
      if (bounds == null) {
        continue;
      }
      bounds.x += insets.left;
      bounds.y += insets.top;
      boolean isExpanded = tree.isExpanded(path);
      boolean isLeaf = tree.getModel().isLeaf(path.getLastPathComponent());
      if (lines) {
        TreePath parent = path.getParentPath();
        while (parent != null && done.add(parent)) {
          paintVerticalPartOfLeg(g, clipBounds, insets, parent);
          parent = parent.getParentPath();
        }
        paintHorizontalPartOfLeg(g, clipBounds, insets, bounds, path, row, isExpanded, isExpanded, isLeaf);
      }
      paintExpandControl(g, clipBounds, insets, bounds, path, row, isExpanded, isExpanded, isLeaf);
      paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, isExpanded, isLeaf);
    }
    paintDropLine(g);
    rendererPane.removeAll();
  }
  public Dimension getPreferredSize(JComponent c) {
    return getPreferredSize(c, true);
  }
  public Dimension getPreferredSize(JComponent c, boolean checkConsistency) {
    Dimension pSize = getPreferredMinSize();
    Insets in = tree.getInsets();
    int width = 0;
    int height = 0;
    int rows = tree.getRowCount();
    for (int i = 0; i < rows; i++) {
      Rectangle b = tree.getRowBounds(i);
      if (b != null) {
        width = Math.max(width, b.x + b.width);
        height = Math.max(height, b.y + b.height);
      }
    }
    Dimension d = new Dimension(width + in.left + in.right, height + in.top + in.bottom);
    if (pSize != null) {
      d.width = Math.max(d.width, pSize.width);
      d.height = Math.max(d.height, pSize.height);
    }
    return d;
  }
  public Dimension getMinimumSize(JComponent c) {
    Dimension d = getPreferredMinSize();
    return d == null ? new Dimension(0, 0) : d;
  }
  public int getBaseline(JComponent c, int width, int height) {
    super.getBaseline(c, width, height);
    if (tree.getRowCount() == 0) {
      return -1;
    }
    Rectangle b = tree.getRowBounds(0);
    TreePath path = tree.getPathForRow(0);
    if (path == null || b == null) {
      return -1;
    }
    Component rc = currentCellRenderer == null ? null : currentCellRenderer.getTreeCellRendererComponent(tree,
        path.getLastPathComponent(), false, tree.isExpanded(0), tree.getModel().isLeaf(path.getLastPathComponent()), 0,
        false);
    if (rc == null) {
      return -1;
    }
    int base = rc.getBaseline(b.width, b.height);
    return base < 0 ? -1 : b.y + base + tree.getInsets().top;
  }
  public java.awt.Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
    super.getBaselineResizeBehavior(c);
    return java.awt.Component.BaselineResizeBehavior.CONSTANT_ASCENT;
  }
  protected void setSelectionModel(TreeSelectionModel model) {
    treeSelectionModel = model;
  }
  protected TreeSelectionModel getSelectionModel() {
    return tree.getSelectionModel();
  }
  public void installUI(JComponent c) {
    if (c == null) {
      throw new NullPointerException("null component passed to BasicTreeUI.installUI()");
    }
    tree = (JTree) c;
    prepareForUIInstall();
    installDefaults();
    installKeyboardActions();
    installComponents();
    installListeners();
    completeUIInstall();
  }
  public void uninstallUI(JComponent c) {
    prepareForUIUninstall();
    uninstallDefaults();
    uninstallListeners();
    uninstallKeyboardActions();
    uninstallComponents();
    completeUIUninstall();
  }

  public class NodeDimensionsHandler extends javax.swing.tree.AbstractLayoutCache.NodeDimensions {
    public NodeDimensionsHandler() {
    }
    protected int getRowX(int row, int depth) {
      return BasicTreeUI.this.getRowX(row, depth);
    }
    public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle size) {
      if (currentCellRenderer == null) {
        return new Rectangle(0, 0, 0, 0);
      }
      Component aComponent = currentCellRenderer.getTreeCellRendererComponent(tree, value, tree.isRowSelected(row),
          expanded, tree.getModel().isLeaf(value), row, false);
      Dimension prefSize = aComponent.getPreferredSize();
      if (size != null) {
        size.x = getRowX(row, depth);
        size.width = prefSize.width;
        size.height = prefSize.height;
      } else {
        size = new Rectangle(getRowX(row, depth), 0, prefSize.width, prefSize.height);
      }
      return size;
    }
  }

  public class ComponentHandler extends java.awt.event.ComponentAdapter implements java.awt.event.ActionListener {
    protected javax.swing.Timer timer;
    protected javax.swing.JScrollBar scrollBar;
    public ComponentHandler() {
    }
    public void componentMoved(java.awt.event.ComponentEvent e) {
      if (timer != null) {
        return;
      }
      javax.swing.JScrollPane scrollPane = getScrollPane();
      if (scrollPane == null) {
        updateSize();
        return;
      }
      scrollBar = scrollPane.getVerticalScrollBar();
      if (scrollBar == null || !scrollBar.getValueIsAdjusting()) {
        scrollBar = scrollPane.getHorizontalScrollBar();
        if (scrollBar != null && scrollBar.getValueIsAdjusting()) {
          startTimer();
        } else {
          updateSize();
        }
      } else {
        startTimer();
      }
    }
    protected void startTimer() {
      if (timer == null) {
        timer = new javax.swing.Timer(200, this);
        timer.setRepeats(true);
      }
      timer.start();
    }
    protected javax.swing.JScrollPane getScrollPane() {
      Component parent = tree.getParent();
      if (parent instanceof javax.swing.JViewport) {
        Component grand = parent.getParent();
        if (grand instanceof javax.swing.JScrollPane) {
          return (javax.swing.JScrollPane) grand;
        }
      }
      return null;
    }
    public void actionPerformed(java.awt.event.ActionEvent ae) {
      if (scrollBar == null || !scrollBar.getValueIsAdjusting()) {
        if (timer != null) {
          timer.stop();
        }
        updateSize();
        timer = null;
        scrollBar = null;
      }
    }
  }

  public class MouseInputHandler implements javax.swing.event.MouseInputListener {
    protected java.awt.Component source;
    protected java.awt.Component destination;
    public MouseInputHandler(java.awt.Component source, java.awt.Component destination, java.awt.event.MouseEvent event) {
      this.source = source;
      this.destination = destination;
      this.source.addMouseListener(this);
      this.source.addMouseMotionListener(this);
      destination.dispatchEvent(SwingUtilities.convertMouseEvent(source, event, destination));
    }
    protected void removeFromSource() {
      if (source != null) {
        source.removeMouseListener(this);
        source.removeMouseMotionListener(this);
      }
      source = null;
      destination = null;
    }
    private void forward(MouseEvent e) {
      if (destination != null && source != null) {
        MouseEvent converted = SwingUtilities.convertMouseEvent(source, e, destination);
        destination.dispatchEvent(converted);
      }
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
      if (destination != null) {
        forward(e);
      }
      removeFromSource();
    }
    public void mouseClicked(MouseEvent e) {
      forward(e);
    }
    public void mouseExited(MouseEvent e) {
      if (destination != null) {
        forward(e);
      }
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseMoved(MouseEvent e) {
      removeFromSource();
    }
    public void mouseDragged(MouseEvent e) {
      forward(e);
    }
  }

  public class TreeCancelEditingAction extends javax.swing.AbstractAction {
    public TreeCancelEditingAction(java.lang.String name) {
      super(name);
    }
    public boolean isEnabled() {
      return tree != null && tree.isEnabled() && isEditing(tree);
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (tree != null) {
        tree.cancelEditing();
      }
    }
  }

  public class TreeToggleAction extends javax.swing.AbstractAction {
    public TreeToggleAction(java.lang.String name) {
      super(name);
    }
    public boolean isEnabled() {
      return tree != null && tree.isEnabled();
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
      TreePath lead = tree.getLeadSelectionPath();
      if (lead != null) {
        toggleExpandState(lead);
      }
    }
  }

  public class TreeHomeAction extends javax.swing.AbstractAction {
    protected int direction;
    private final boolean extend;
    public TreeHomeAction(int direction, java.lang.String name) {
      super(name);
      this.direction = direction;
      this.extend = name != null && name.endsWith("ExtendSelection");
    }
    public boolean isEnabled() {
      return tree != null && tree.isEnabled();
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
      int rows = tree.getRowCount();
      if (rows == 0) {
        return;
      }
      int target = direction < 0 ? 0 : rows - 1;
      if (getValue(javax.swing.Action.NAME).toString().endsWith("ChangeLead")) {
        tree.setLeadSelectionPath(tree.getPathForRow(target));
        ensureRowsAreVisible(target, target);
      } else {
        moveTo(tree, target, extend);
      }
    }
  }

  public class TreeIncrementAction extends javax.swing.AbstractAction {
    protected int direction;
    private final boolean extend;
    private final boolean changeLead;
    public TreeIncrementAction(int direction, java.lang.String name) {
      super(name);
      this.direction = direction;
      this.extend = name != null && name.endsWith("ExtendSelection");
      this.changeLead = name != null && name.endsWith("ChangeLead");
    }
    public boolean isEnabled() {
      return tree != null && tree.isEnabled();
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
      int rows = tree.getRowCount();
      if (rows == 0) {
        return;
      }
      int lead = tree.getLeadSelectionRow();
      int target = lead < 0 ? 0 : Math.max(0, Math.min(rows - 1, lead + direction));
      if (changeLead) {
        tree.setLeadSelectionPath(tree.getPathForRow(target));
        ensureRowsAreVisible(target, target);
      } else {
        moveTo(tree, target, extend);
      }
    }
  }

  public class TreePageAction extends javax.swing.AbstractAction {
    protected int direction;
    private final boolean extend;
    public TreePageAction(int direction, java.lang.String name) {
      super(name);
      this.direction = direction;
      this.extend = name != null && name.endsWith("ExtendSelection");
    }
    public boolean isEnabled() {
      return tree != null && tree.isEnabled();
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
      int rows = tree.getRowCount();
      if (rows == 0) {
        return;
      }
      Rectangle visible = tree.getVisibleRect();
      int lead = Math.max(0, tree.getLeadSelectionRow());
      Rectangle leadBounds = tree.getRowBounds(lead);
      int target;
      if (leadBounds == null) {
        target = lead;
      } else if (direction < 0) {
        target = tree.getClosestRowForLocation(leadBounds.x, Math.max(0, leadBounds.y - visible.height));
      } else {
        target = tree.getClosestRowForLocation(leadBounds.x, leadBounds.y + visible.height);
      }
      if (target == lead) {
        target = Math.max(0, Math.min(rows - 1, lead + direction));
      }
      if (getValue(javax.swing.Action.NAME).toString().endsWith("ChangeLead")) {
        tree.setLeadSelectionPath(tree.getPathForRow(target));
        ensureRowsAreVisible(target, target);
      } else {
        moveTo(tree, target, extend);
      }
    }
  }

  public class TreeTraverseAction extends javax.swing.AbstractAction {
    protected int direction;
    public TreeTraverseAction(int direction, java.lang.String name) {
      super(name);
      this.direction = direction;
    }
    public boolean isEnabled() {
      return tree != null && tree.isEnabled();
    }
    private void goTo(int row) {
      if (getValue(javax.swing.Action.NAME).toString().endsWith("ChangeLead")) {
        tree.setLeadSelectionPath(tree.getPathForRow(row));
      } else {
        tree.setSelectionRow(row);
      }
      ensureRowsAreVisible(row, row);
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
      int row = tree.getLeadSelectionRow();
      if (row < 0) {
        return;
      }
      TreePath path = tree.getPathForRow(row);
      if (path == null) {
        return;
      }
      boolean leaf = tree.getModel().isLeaf(path.getLastPathComponent());
      boolean ltr = tree.getComponentOrientation().isLeftToRight();
      int dir = ltr ? direction : -direction;
      if (dir > 0) {
        if (leaf) {
          return;
        }
        if (!tree.isExpanded(path)) {
          tree.expandPath(path);
        } else if (tree.getModel().getChildCount(path.getLastPathComponent()) > 0 && row + 1 < tree.getRowCount()) {
          goTo(row + 1);
        }
      } else {
        if (!leaf && tree.isExpanded(path)) {
          tree.collapsePath(path);
        } else if (path.getPathCount() > 1) {
          TreePath parent = path.getParentPath();
          int parentRow = tree.getRowForPath(parent);
          if (parentRow >= 0) {
            goTo(parentRow);
          }
        }
      }
    }
  }

  private void moveTo(JTree tree, int row, boolean extend) {
    if (extend && tree.getSelectionModel().getSelectionMode() != TreeSelectionModel.SINGLE_TREE_SELECTION) {
      TreePath anchor = tree.getAnchorSelectionPath();
      int anchorRow = anchor == null ? row : Math.max(0, tree.getRowForPath(anchor));
      TreePath lead = tree.getPathForRow(row);
      tree.setSelectionInterval(Math.min(anchorRow, row), Math.max(anchorRow, row));
      tree.setAnchorSelectionPath(anchor == null ? lead : anchor);
      tree.setLeadSelectionPath(lead);
    } else {
      tree.setSelectionRow(row);
    }
    ensureRowsAreVisible(row, row);
  }

  public class SelectionModelPropertyChangeHandler implements java.beans.PropertyChangeListener {
    public SelectionModelPropertyChangeHandler() {
    }
    public void propertyChange(PropertyChangeEvent event) {
      tree.repaint();
    }
  }

  public class PropertyChangeHandler implements java.beans.PropertyChangeListener {
    public PropertyChangeHandler() {
    }
    public void propertyChange(PropertyChangeEvent event) {
      String name = event.getPropertyName();
      if ("rootVisible".equals(name) || "showsRootHandles".equals(name)) {
        updateDepthOffset();
        tree.revalidate();
        tree.repaint();
      } else if ("cellRenderer".equals(name)) {
        updateRenderer();
        tree.revalidate();
        tree.repaint();
      } else if ("editable".equals(name)) {
        updateCellEditor();
      } else if ("model".equals(name)) {
        if (treeModel != null) {
          treeModel.removeTreeModelListener(modelListener);
        }
        treeModel = tree.getModel();
        if (treeModel != null) {
          treeModel.addTreeModelListener(modelListener);
        }
        tree.revalidate();
        tree.repaint();
      } else if ("largeModel".equals(name) || "rowHeight".equals(name)) {
        largeModel = tree.isLargeModel();
        tree.revalidate();
        tree.repaint();
      } else if ("selectionModel".equals(name)) {
        if (treeSelectionModel != null) {
          treeSelectionModel.removeTreeSelectionListener(selectionListener);
          treeSelectionModel.removePropertyChangeListener(selectionModelListener);
        }
        treeSelectionModel = tree.getSelectionModel();
        if (treeSelectionModel != null) {
          treeSelectionModel.addTreeSelectionListener(selectionListener);
          treeSelectionModel.addPropertyChangeListener(selectionModelListener);
        }
        tree.repaint();
      } else if ("componentOrientation".equals(name) || "font".equals(name)) {
        tree.revalidate();
        tree.repaint();
      }
    }
  }

  public class MouseHandler extends java.awt.event.MouseAdapter implements java.awt.event.MouseMotionListener {
    public MouseHandler() {
    }
    public void mousePressed(MouseEvent e) {
      if (e.isConsumed() || !tree.isEnabled()) {
        return;
      }
      if (tree.isRequestFocusEnabled()) {
        tree.requestFocus();
      }
      TreePath closest = tree.getClosestPathForLocation(e.getX(), e.getY());
      if (closest == null) {
        return;
      }
      Rectangle bounds = tree.getPathBounds(closest);
      if (bounds == null || e.getY() >= bounds.y + bounds.height || e.getY() < bounds.y) {
        return;
      }
      if (SwingUtilities.isLeftMouseButton(e)) {
        if (isLocationInExpandControl(closest, e.getX(), e.getY())) {
          checkForClickInExpandControl(closest, e.getX(), e.getY());
          return;
        }
        if (e.getX() > bounds.x && e.getX() <= bounds.x + bounds.width) {
          if (tree.getDragEnabled() || !startEditing(closest, e)) {
            selectPathForEvent(closest, e);
          }
          if (e.getClickCount() > 1 && isToggleEvent(e)) {
            toggleExpandState(closest);
          }
        }
      }
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseMoved(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
    }
  }

  public class FocusHandler implements java.awt.event.FocusListener {
    public FocusHandler() {
    }
    private void repaintLead() {
      TreePath lead = tree.getLeadSelectionPath();
      Rectangle r = lead == null ? null : tree.getPathBounds(lead);
      if (r != null) {
        tree.repaint(0, r.y, tree.getWidth(), r.height);
      }
      for (TreePath p : tree.getSelectionPaths() == null ? new TreePath[0] : tree.getSelectionPaths()) {
        Rectangle b = tree.getPathBounds(p);
        if (b != null) {
          tree.repaint(0, b.y, tree.getWidth(), b.height);
        }
      }
    }
    public void focusGained(java.awt.event.FocusEvent e) {
      if (tree != null) {
        repaintLead();
      }
    }
    public void focusLost(java.awt.event.FocusEvent e) {
      if (tree != null) {
        repaintLead();
      }
    }
  }

  public class KeyHandler extends java.awt.event.KeyAdapter {
    protected javax.swing.Action repeatKeyAction;
    protected boolean isKeyDown;
    private String typed = "";
    private long lastTime;
    public KeyHandler() {
    }
    public void keyTyped(java.awt.event.KeyEvent e) {
      if (tree == null || tree.getRowCount() == 0 || !tree.isEnabled() || e.isAltDown() || e.isControlDown()
          || e.isMetaDown()) {
        return;
      }
      char c = e.getKeyChar();
      if (c < ' ' || c == 0x7f) {
        return;
      }
      long now = e.getWhen();
      Object tf = UIManager.get("Tree.timeFactor");
      long factor = tf instanceof Number ? ((Number) tf).longValue() : 1000;
      if (factor <= 0) {
        factor = 1000;
      }
      if (now - lastTime > factor) {
        typed = "";
      }
      lastTime = now;
      String prefix = typed + c;
      int start = Math.max(0, tree.getLeadSelectionRow());
      if (typed.length() > 0 || start < 0) {
        start = Math.max(0, tree.getLeadSelectionRow());
      } else {
        start = (start + 1) % tree.getRowCount();
      }
      TreePath match = tree.getNextMatch(prefix, start, javax.swing.text.Position.Bias.Forward);
      if (match == null && typed.length() == 0 && c != ' ') {
        match = tree.getNextMatch(prefix, 0, javax.swing.text.Position.Bias.Forward);
      }
      typed = prefix;
      if (match != null) {
        tree.setSelectionPath(match);
        int row = tree.getRowForPath(match);
        ensureRowsAreVisible(row, row);
      }
    }
    public void keyPressed(java.awt.event.KeyEvent e) {
      isKeyDown = true;
    }
    public void keyReleased(java.awt.event.KeyEvent e) {
      isKeyDown = false;
      repeatKeyAction = null;
    }
  }

  public class CellEditorHandler implements javax.swing.event.CellEditorListener {
    public CellEditorHandler() {
    }
    public void editingStopped(javax.swing.event.ChangeEvent e) {
      completeEditing(false, false, true);
    }
    public void editingCanceled(javax.swing.event.ChangeEvent e) {
      completeEditing(false, false, false);
    }
  }

  public class TreeSelectionHandler implements javax.swing.event.TreeSelectionListener {
    public TreeSelectionHandler() {
    }
    public void valueChanged(javax.swing.event.TreeSelectionEvent event) {
      completeEditing();
      updateLeadSelectionRow();
      TreePath[] changed = event.getPaths();
      if (changed == null) {
        tree.repaint();
        return;
      }
      for (TreePath p : changed) {
        Rectangle r = tree.getPathBounds(p);
        if (r != null) {
          tree.repaint(0, r.y, tree.getWidth(), r.height);
        }
      }
      lastSelectedRow = tree.getMinSelectionRow();
    }
  }

  public class TreeModelHandler implements javax.swing.event.TreeModelListener {
    public TreeModelHandler() {
    }
    public void treeNodesChanged(javax.swing.event.TreeModelEvent e) {
      updateSize();
      tree.revalidate();
      tree.repaint();
    }
    public void treeNodesInserted(javax.swing.event.TreeModelEvent e) {
      updateSize();
      tree.revalidate();
      tree.repaint();
    }
    public void treeNodesRemoved(javax.swing.event.TreeModelEvent e) {
      completeEditing();
      updateSize();
      tree.revalidate();
      tree.repaint();
    }
    public void treeStructureChanged(javax.swing.event.TreeModelEvent e) {
      completeEditing();
      updateSize();
      tree.revalidate();
      tree.repaint();
    }
  }

  public class TreeExpansionHandler implements javax.swing.event.TreeExpansionListener {
    public TreeExpansionHandler() {
    }
    public void treeExpanded(javax.swing.event.TreeExpansionEvent event) {
      pathWasExpanded(event.getPath());
    }
    public void treeCollapsed(javax.swing.event.TreeExpansionEvent event) {
      pathWasCollapsed(event.getPath());
    }
  }
  private java.awt.event.MouseListener mouseListener;
  private java.awt.event.FocusListener focusListener;
  private java.beans.PropertyChangeListener propertyChangeListener;
  private TreeSelectionListener selectionListener;
  private TreeExpansionListener expansionListener;
  private TreeModelListener modelListener;
  private java.awt.event.KeyListener keyListener;
  private java.awt.event.ComponentListener componentListener;
  private java.beans.PropertyChangeListener selectionModelListener;
  private javax.swing.event.CellEditorListener editorListener;
  private Color hashColor;
  private boolean paintLines = true;
  private boolean lineTypeDashed;
  private int leadRow = -1;

  private void componentListenerCleanup() {
  }
}
