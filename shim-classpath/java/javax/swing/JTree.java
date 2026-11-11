/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.Position;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.VariableHeightLayoutCache;

public class JTree extends JComponent implements Scrollable, Accessible {
  public static final String CELL_RENDERER_PROPERTY = "cellRenderer";
  public static final String TREE_MODEL_PROPERTY = "model";
  public static final String ROOT_VISIBLE_PROPERTY = "rootVisible";
  public static final String SHOWS_ROOT_HANDLES_PROPERTY = "showsRootHandles";
  public static final String ROW_HEIGHT_PROPERTY = "rowHeight";
  public static final String CELL_EDITOR_PROPERTY = "cellEditor";
  public static final String EDITABLE_PROPERTY = "editable";
  public static final String LARGE_MODEL_PROPERTY = "largeModel";
  public static final String SELECTION_MODEL_PROPERTY = "selectionModel";
  public static final String VISIBLE_ROW_COUNT_PROPERTY = "visibleRowCount";
  public static final String INVOKES_STOP_CELL_EDITING_PROPERTY = "invokesStopCellEditing";
  public static final String SCROLLS_ON_EXPAND_PROPERTY = "scrollsOnExpand";
  public static final String TOGGLE_CLICK_COUNT_PROPERTY = "toggleClickCount";
  public static final String LEAD_SELECTION_PATH_PROPERTY = "leadSelectionPath";
  public static final String ANCHOR_SELECTION_PATH_PROPERTY = "anchorSelectionPath";
  public static final String EXPANDS_SELECTED_PATHS_PROPERTY = "expandsSelectedPaths";

  protected transient TreeModel treeModel;
  protected transient TreeSelectionModel selectionModel;
  protected boolean rootVisible;
  protected transient TreeCellRenderer cellRenderer;
  protected int rowHeight;
  protected boolean showsRootHandles;
  protected transient TreeCellEditor cellEditor;
  protected boolean editable;
  protected boolean largeModel;
  protected int visibleRowCount;
  protected boolean invokesStopCellEditing;
  protected boolean scrollsOnExpand;
  protected int toggleClickCount;
  protected transient TreeModelListener treeModelListener;

  private transient Hashtable<TreePath, Boolean> expandedState = new Hashtable<TreePath, Boolean>();
  private transient TreeSelectionRedirector selectionRedirector;
  private transient AbstractLayoutCache treeState = new VariableHeightLayoutCache();
  private transient TreePath leadPath;
  private transient TreePath anchorPath;
  private transient TreePath editingPath;
  private boolean expandsSelectedPaths;
  private boolean dragEnabled;
  private DropMode dropMode = DropMode.USE_SELECTION;
  private transient DropLocation dropLocation;
  private AccessibleContext accessibleContext;
  private final Stack<Stack<TreePath>> expandedStack = new Stack<Stack<TreePath>>();

  public JTree() { this(getDefaultTreeModel()); }

  public JTree(Object[] value) {
    this(createTreeModel(value));
    this.setRootVisible(false);
    this.setShowsRootHandles(true);
    expandRoot();
  }

  public JTree(Vector<?> value) {
    this(createTreeModel(value));
    this.setRootVisible(false);
    this.setShowsRootHandles(true);
    expandRoot();
  }

  public JTree(Hashtable<?, ?> value) {
    this(createTreeModel(value));
    this.setRootVisible(false);
    this.setShowsRootHandles(true);
    expandRoot();
  }

  public JTree(TreeNode root) { this(root, false); }

  public JTree(TreeNode root, boolean asksAllowsChildren) { this(new DefaultTreeModel(root, asksAllowsChildren)); }

  @java.beans.ConstructorProperties({"model"})
  public JTree(TreeModel newModel) {
    super();
    setLayout(null);
    toggleClickCount = 2;
    rowHeight = 16;
    visibleRowCount = 20;
    rootVisible = true;
    selectionModel = new DefaultTreeSelectionModel();
    cellRenderer = null;
    scrollsOnExpand = true;
    setOpaque(true);
    expandsSelectedPaths = true;
    treeState.setNodeDimensions(new ShimNodeDimensions());
    treeState.setRootVisible(rootVisible);
    treeState.setRowHeight(0);
    treeState.setSelectionModel(selectionModel);
    selectionRedirector = new TreeSelectionRedirector();
    selectionModel.addTreeSelectionListener(selectionRedirector);
    updateUI();
    rowHeight = UIManager.getInt("Tree.rowHeight");
    treeState.setRowHeight(rowHeight);
    setModel(newModel);
  }

  private final class ShimNodeDimensions extends AbstractLayoutCache.NodeDimensions {
    public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle size) {
      TreeCellRenderer renderer = getCellRenderer();
      Dimension pref = new Dimension(0, 16);
      if (renderer != null) {
        Component c = renderer.getTreeCellRendererComponent(JTree.this, value, false, expanded,
            treeModel == null || treeModel.isLeaf(value), row, false);
        if (c != null) {
          pref = c.getPreferredSize();
        }
      }
      int indent = 20;
      int depthOffset = rootVisible ? (showsRootHandles ? 1 : 0) : (showsRootHandles ? 0 : -1);
      if (size == null) {
        size = new Rectangle();
      }
      size.x = indent * (depth + depthOffset);
      size.width = pref.width;
      size.height = rowHeight > 0 ? rowHeight : pref.height;
      size.y = 0;
      return size;
    }
  }

  private void expandRoot() {
    TreeModel model = getModel();
    if (model != null && model.getRoot() != null) {
      expandPath(new TreePath(model.getRoot()));
    }
  }

  public static class DynamicUtilTreeNode extends DefaultMutableTreeNode {
    protected boolean hasChildren;
    protected Object childValue;
    protected boolean loadedChildren;

    public static void createChildren(DefaultMutableTreeNode parent, Object children) {
      if (children instanceof Vector) {
        Vector<?> childVector = (Vector<?>) children;
        for (int counter = 0, maxCounter = childVector.size(); counter < maxCounter; counter++) {
          parent.add(new DynamicUtilTreeNode(childVector.elementAt(counter), childVector.elementAt(counter)));
        }
      } else if (children instanceof Hashtable) {
        Hashtable<?, ?> childHT = (Hashtable<?, ?>) children;
        Enumeration<?> keys = childHT.keys();
        Object aKey;
        while (keys.hasMoreElements()) {
          aKey = keys.nextElement();
          parent.add(new DynamicUtilTreeNode(aKey, childHT.get(aKey)));
        }
      } else if (children instanceof Object[]) {
        Object[] childArray = (Object[]) children;
        for (int counter = 0, maxCounter = childArray.length; counter < maxCounter; counter++) {
          parent.add(new DynamicUtilTreeNode(childArray[counter], childArray[counter]));
        }
      }
    }

    public DynamicUtilTreeNode(Object value, Object children) {
      super(value);
      loadedChildren = false;
      childValue = children;
      if (children != null) {
        if (children instanceof Vector) {
          setAllowsChildren(true);
        } else if (children instanceof Hashtable) {
          setAllowsChildren(true);
        } else if (children instanceof Object[]) {
          setAllowsChildren(true);
        } else {
          setAllowsChildren(false);
        }
      } else {
        setAllowsChildren(false);
      }
    }

    public boolean isLeaf() { return !getAllowsChildren(); }

    public int getChildCount() {
      if (!loadedChildren) {
        loadChildren();
      }
      return super.getChildCount();
    }

    protected void loadChildren() {
      loadedChildren = true;
      createChildren(this, childValue);
    }

    public TreeNode getChildAt(int index) {
      if (!loadedChildren) {
        loadChildren();
      }
      return super.getChildAt(index);
    }

    public Enumeration<TreeNode> children() {
      if (!loadedChildren) {
        loadChildren();
      }
      return super.children();
    }
  }

  protected static TreeModel getDefaultTreeModel() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("JTree");
    DefaultMutableTreeNode parent;
    parent = new DefaultMutableTreeNode("colors");
    root.add(parent);
    parent.add(new DefaultMutableTreeNode("blue"));
    parent.add(new DefaultMutableTreeNode("violet"));
    parent.add(new DefaultMutableTreeNode("red"));
    parent.add(new DefaultMutableTreeNode("yellow"));
    parent = new DefaultMutableTreeNode("sports");
    root.add(parent);
    parent.add(new DefaultMutableTreeNode("basketball"));
    parent.add(new DefaultMutableTreeNode("soccer"));
    parent.add(new DefaultMutableTreeNode("football"));
    parent.add(new DefaultMutableTreeNode("hockey"));
    parent = new DefaultMutableTreeNode("food");
    root.add(parent);
    parent.add(new DefaultMutableTreeNode("hot dogs"));
    parent.add(new DefaultMutableTreeNode("pizza"));
    parent.add(new DefaultMutableTreeNode("ravioli"));
    parent.add(new DefaultMutableTreeNode("bananas"));
    return new DefaultTreeModel(root);
  }

  protected static TreeModel createTreeModel(Object value) {
    DefaultMutableTreeNode root;
    if ((value instanceof Object[]) || (value instanceof Hashtable) || (value instanceof Vector)) {
      root = new DefaultMutableTreeNode("root");
      DynamicUtilTreeNode.createChildren(root, value);
    } else {
      root = new DynamicUtilTreeNode("root", value);
    }
    return new DefaultTreeModel(root, false);
  }

  public TreeUI getUI() { return (TreeUI) ui; }

  public void setUI(TreeUI ui) {
    if (this.ui != ui) {
      super.setUI(ui);
      repaint();
    }
  }

  public void updateUI() {
    setUI((javax.swing.plaf.ComponentUI) UIManager.getUI(this));
    if (cellRenderer == null || cellRenderer instanceof UIResource) {
      cellRenderer = new DefaultTreeCellRenderer();
    } else {
      TreeCellRenderer renderer = getCellRenderer();
      if (renderer instanceof Component) {
        SwingUtilities.updateComponentTreeUI((Component) renderer);
      }
    }
  }

  public String getUIClassID() { return "TreeUI"; }

  public TreeCellRenderer getCellRenderer() { return cellRenderer; }

  public void setCellRenderer(TreeCellRenderer x) {
    TreeCellRenderer oldValue = cellRenderer;
    cellRenderer = x;
    firePropertyChange(CELL_RENDERER_PROPERTY, oldValue, cellRenderer);
    treeState.invalidateSizes();
    invalidate();
  }

  public void setEditable(boolean flag) {
    boolean oldValue = this.editable;
    this.editable = flag;
    firePropertyChange(EDITABLE_PROPERTY, oldValue, flag);
  }

  public boolean isEditable() { return editable; }

  public void setCellEditor(TreeCellEditor cellEditor) {
    TreeCellEditor oldEditor = this.cellEditor;
    this.cellEditor = cellEditor;
    firePropertyChange(CELL_EDITOR_PROPERTY, oldEditor, cellEditor);
    invalidate();
  }

  public TreeCellEditor getCellEditor() { return cellEditor; }

  public TreeModel getModel() { return treeModel; }

  public void setModel(TreeModel newModel) {
    clearSelection();
    TreeModel oldModel = treeModel;
    if (treeModel != null && treeModelListener != null) {
      treeModel.removeTreeModelListener(treeModelListener);
    }
    treeModel = newModel;
    clearToggledPaths();
    if (treeModel != null) {
      if (treeModelListener == null) {
        treeModelListener = createTreeModelListener();
      }
      if (treeModelListener != null) {
        treeModel.addTreeModelListener(treeModelListener);
      }
      if (treeModel.getRoot() != null && !treeModel.isLeaf(treeModel.getRoot())) {
        expandedState.put(new TreePath(treeModel.getRoot()), Boolean.TRUE);
      }
    }
    treeState.setModel(treeModel);
    if (treeModel != null && treeModel.getRoot() != null && !treeModel.isLeaf(treeModel.getRoot())) {
      treeState.setExpandedState(new TreePath(treeModel.getRoot()), true);
    }
    firePropertyChange(TREE_MODEL_PROPERTY, oldModel, treeModel);
    invalidate();
  }

  public boolean isRootVisible() { return rootVisible; }

  public void setRootVisible(boolean rootVisible) {
    boolean oldValue = this.rootVisible;
    this.rootVisible = rootVisible;
    treeState.setRootVisible(rootVisible);
    if (!rootVisible && treeModel != null && treeModel.getRoot() != null) {
      TreePath rootPath = new TreePath(treeModel.getRoot());
      if (selectionModel.isPathSelected(rootPath)) {
        selectionModel.removeSelectionPath(rootPath);
      }
    }
    selectionModel.resetRowSelection();
    firePropertyChange(ROOT_VISIBLE_PROPERTY, oldValue, this.rootVisible);
    repaint();
  }

  public void setShowsRootHandles(boolean newValue) {
    boolean oldValue = showsRootHandles;
    TreeModel model = getModel();
    showsRootHandles = newValue;
    firePropertyChange(SHOWS_ROOT_HANDLES_PROPERTY, oldValue, showsRootHandles);
    treeState.invalidateSizes();
    repaint();
  }

  public boolean getShowsRootHandles() { return showsRootHandles; }

  public void setRowHeight(int rowHeight) {
    int oldValue = this.rowHeight;
    this.rowHeight = rowHeight;
    firePropertyChange(ROW_HEIGHT_PROPERTY, oldValue, rowHeight);
    treeState.setRowHeight(rowHeight);
    treeState.invalidateSizes();
    invalidate();
  }

  public int getRowHeight() { return rowHeight; }

  public boolean isFixedRowHeight() { return rowHeight > 0; }

  public void setLargeModel(boolean newValue) {
    boolean oldValue = largeModel;
    largeModel = newValue;
    firePropertyChange(LARGE_MODEL_PROPERTY, oldValue, newValue);
  }

  public boolean isLargeModel() { return largeModel; }

  public void setInvokesStopCellEditing(boolean newValue) {
    boolean oldValue = invokesStopCellEditing;
    invokesStopCellEditing = newValue;
    firePropertyChange(INVOKES_STOP_CELL_EDITING_PROPERTY, oldValue, newValue);
  }

  public boolean getInvokesStopCellEditing() { return invokesStopCellEditing; }

  public void setScrollsOnExpand(boolean newValue) {
    boolean oldValue = scrollsOnExpand;
    scrollsOnExpand = newValue;
    firePropertyChange(SCROLLS_ON_EXPAND_PROPERTY, oldValue, newValue);
  }

  public boolean getScrollsOnExpand() { return scrollsOnExpand; }

  public void setToggleClickCount(int clickCount) {
    int oldCount = toggleClickCount;
    toggleClickCount = clickCount;
    firePropertyChange(TOGGLE_CLICK_COUNT_PROPERTY, oldCount, clickCount);
  }

  public int getToggleClickCount() { return toggleClickCount; }

  public void setExpandsSelectedPaths(boolean newValue) {
    boolean oldValue = expandsSelectedPaths;
    expandsSelectedPaths = newValue;
    firePropertyChange(EXPANDS_SELECTED_PATHS_PROPERTY, oldValue, newValue);
  }

  public boolean getExpandsSelectedPaths() { return expandsSelectedPaths; }

  public void setDragEnabled(boolean b) {
    if (b && GraphicsEnvironment.isHeadless()) {
      throw new HeadlessException();
    }
    dragEnabled = b;
  }

  public boolean getDragEnabled() { return dragEnabled; }

  public final void setDropMode(DropMode dropMode) { this.dropMode = dropMode; }

  public final DropMode getDropMode() { return dropMode; }

  public final DropLocation getDropLocation() { return dropLocation; }

  public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row,
      boolean hasFocus) {
    if (value != null) {
      String sValue = value.toString();
      if (sValue != null) {
        return sValue;
      }
    }
    return "";
  }

  public int getRowCount() { return treeState.getRowCount(); }

  public void setSelectionModel(TreeSelectionModel selectionModel) {
    if (selectionModel == null) {
      selectionModel = EmptySelectionModel.sharedInstance();
    }
    TreeSelectionModel oldValue = this.selectionModel;
    if (this.selectionModel != null && selectionRedirector != null) {
      this.selectionModel.removeTreeSelectionListener(selectionRedirector);
    }
    this.selectionModel = selectionModel;
    treeState.setSelectionModel(selectionModel);
    if (this.selectionModel != null && selectionRedirector != null) {
      this.selectionModel.addTreeSelectionListener(selectionRedirector);
    }
    firePropertyChange(SELECTION_MODEL_PROPERTY, oldValue, this.selectionModel);
  }

  public TreeSelectionModel getSelectionModel() { return selectionModel; }

  public void setVisibleRowCount(int newCount) {
    int oldCount = visibleRowCount;
    visibleRowCount = newCount;
    firePropertyChange(VISIBLE_ROW_COUNT_PROPERTY, oldCount, visibleRowCount);
    invalidate();
  }

  public int getVisibleRowCount() { return visibleRowCount; }

  public Rectangle getPathBounds(TreePath path) {
    if (path == null) {
      return null;
    }
    return treeState.getBounds(path, null);
  }

  public Rectangle getRowBounds(int row) { return getPathBounds(getPathForRow(row)); }

  public TreePath getPathForRow(int row) { return treeState.getPathForRow(row); }

  public int getRowForPath(TreePath path) { return treeState.getRowForPath(path); }

  public TreePath getClosestPathForLocation(int x, int y) { return treeState.getPathClosestTo(x, y); }

  public int getClosestRowForLocation(int x, int y) { return getRowForPath(getClosestPathForLocation(x, y)); }

  public TreePath getPathForLocation(int x, int y) {
    TreePath closestPath = getClosestPathForLocation(x, y);
    if (closestPath != null) {
      Rectangle pathBounds = getPathBounds(closestPath);
      if (pathBounds != null && x >= pathBounds.x && x < (pathBounds.x + pathBounds.width) && y >= pathBounds.y
          && y < (pathBounds.y + pathBounds.height)) {
        return closestPath;
      }
    }
    return null;
  }

  public int getRowForLocation(int x, int y) { return getRowForPath(getPathForLocation(x, y)); }

  public boolean isEditing() { return editingPath != null; }

  public boolean stopEditing() {
    if (editingPath != null && cellEditor != null) {
      if (cellEditor.stopCellEditing()) {
        editingPath = null;
        return true;
      }
      return false;
    }
    return false;
  }

  public void cancelEditing() {
    if (editingPath != null && cellEditor != null) {
      cellEditor.cancelCellEditing();
    }
    editingPath = null;
  }

  public void startEditingAtPath(TreePath path) {
    if (path != null && isPathEditable(path) && cellEditor != null && isVisible(path)) {
      editingPath = path;
    }
  }

  public TreePath getEditingPath() { return editingPath; }

  public boolean isPathEditable(TreePath path) { return isEditable(); }

  public void setSelectionPath(TreePath path) { getSelectionModel().setSelectionPath(path); }

  public void setSelectionPaths(TreePath[] paths) { getSelectionModel().setSelectionPaths(paths); }

  public void addSelectionPath(TreePath path) { getSelectionModel().addSelectionPath(path); }

  public void addSelectionPaths(TreePath[] paths) { getSelectionModel().addSelectionPaths(paths); }

  public void removeSelectionPath(TreePath path) { getSelectionModel().removeSelectionPath(path); }

  public void removeSelectionPaths(TreePath[] paths) { getSelectionModel().removeSelectionPaths(paths); }

  public void clearSelection() { getSelectionModel().clearSelection(); }

  public TreePath getSelectionPath() { return getSelectionModel().getSelectionPath(); }

  public TreePath[] getSelectionPaths() {
    TreePath[] selectionPaths = getSelectionModel().getSelectionPaths();
    return (selectionPaths != null && selectionPaths.length > 0) ? selectionPaths : null;
  }

  public int getSelectionCount() { return selectionModel.getSelectionCount(); }

  public boolean isPathSelected(TreePath path) { return getSelectionModel().isPathSelected(path); }

  public boolean isSelectionEmpty() { return getSelectionModel().isSelectionEmpty(); }

  public Object getLastSelectedPathComponent() {
    TreePath selPath = getSelectionModel().getSelectionPath();
    if (selPath != null) {
      return selPath.getLastPathComponent();
    }
    return null;
  }

  public TreePath getLeadSelectionPath() { return leadPath; }

  public void setLeadSelectionPath(TreePath newPath) {
    TreePath oldValue = leadPath;
    leadPath = newPath;
    firePropertyChange(LEAD_SELECTION_PATH_PROPERTY, oldValue, newPath);
  }

  public TreePath getAnchorSelectionPath() { return anchorPath; }

  public void setAnchorSelectionPath(TreePath newPath) {
    TreePath oldValue = anchorPath;
    anchorPath = newPath;
    firePropertyChange(ANCHOR_SELECTION_PATH_PROPERTY, oldValue, newPath);
  }

  public int getLeadSelectionRow() {
    TreePath leadPath = getLeadSelectionPath();
    if (leadPath != null) {
      return getRowForPath(leadPath);
    }
    return -1;
  }

  public int getMinSelectionRow() { return getSelectionModel().getMinSelectionRow(); }

  public int getMaxSelectionRow() { return getSelectionModel().getMaxSelectionRow(); }

  public int[] getSelectionRows() { return getSelectionModel().getSelectionRows(); }

  public boolean isRowSelected(int row) { return getSelectionModel().isRowSelected(row); }

  public void setSelectionRow(int row) { setSelectionRows(new int[] {row}); }

  public void setSelectionRows(int[] rows) {
    if (rows != null) {
      int numRows = rows.length;
      TreePath[] paths = new TreePath[numRows];
      for (int counter = 0; counter < numRows; counter++) {
        paths[counter] = getPathForRow(rows[counter]);
      }
      setSelectionPaths(paths);
    }
  }

  public void addSelectionRow(int row) { addSelectionRows(new int[] {row}); }

  public void addSelectionRows(int[] rows) {
    if (rows != null) {
      int numRows = rows.length;
      TreePath[] paths = new TreePath[numRows];
      for (int counter = 0; counter < numRows; counter++) {
        paths[counter] = getPathForRow(rows[counter]);
      }
      addSelectionPaths(paths);
    }
  }

  public void removeSelectionRow(int row) { removeSelectionRows(new int[] {row}); }

  public void removeSelectionRows(int[] rows) {
    if (rows != null) {
      int numRows = rows.length;
      TreePath[] paths = new TreePath[numRows];
      for (int counter = 0; counter < numRows; counter++) {
        paths[counter] = getPathForRow(rows[counter]);
      }
      removeSelectionPaths(paths);
    }
  }

  protected TreePath[] getPathBetweenRows(int index0, int index1) {
    int newMinIndex;
    int newMaxIndex;
    int count = getRowCount();
    if (count == 0 || (index0 >= count && index1 >= count)) {
      return null;
    }
    if (index0 >= count) {
      index0 = count - 1;
    }
    if (index1 >= count) {
      index1 = count - 1;
    }
    newMinIndex = Math.min(index0, index1);
    newMaxIndex = Math.max(index0, index1);
    TreePath[] selection = new TreePath[newMaxIndex - newMinIndex + 1];
    for (int counter = newMinIndex; counter <= newMaxIndex; counter++) {
      selection[counter - newMinIndex] = getPathForRow(counter);
    }
    return selection;
  }

  public void setSelectionInterval(int index0, int index1) {
    TreePath[] paths = getPathBetweenRows(index0, index1);
    this.getSelectionModel().setSelectionPaths(paths);
  }

  public void addSelectionInterval(int index0, int index1) {
    TreePath[] paths = getPathBetweenRows(index0, index1);
    if (paths != null) {
      this.getSelectionModel().addSelectionPaths(paths);
    }
  }

  public void removeSelectionInterval(int index0, int index1) {
    TreePath[] paths = getPathBetweenRows(index0, index1);
    if (paths != null) {
      this.getSelectionModel().removeSelectionPaths(paths);
    }
  }

  public void scrollPathToVisible(TreePath path) {
    if (path != null) {
      makeVisible(path);
      Rectangle bounds = getPathBounds(path);
      if (bounds != null) {
        scrollRectToVisible(bounds);
      }
    }
  }

  public void scrollRowToVisible(int row) { scrollPathToVisible(getPathForRow(row)); }

  public void makeVisible(TreePath path) {
    if (path != null) {
      TreePath parentPath = path.getParentPath();
      if (parentPath != null) {
        expandPath(parentPath);
      }
    }
  }

  public boolean isVisible(TreePath path) {
    if (path != null) {
      TreePath parentPath = path.getParentPath();
      if (parentPath != null) {
        return isExpanded(parentPath);
      }
      return true;
    }
    return false;
  }

  public void expandPath(TreePath path) {
    TreeModel model = getModel();
    if (path != null && model != null && !model.isLeaf(path.getLastPathComponent())) {
      setExpandedState(path, true);
    }
  }

  public void expandRow(int row) { expandPath(getPathForRow(row)); }

  public void collapsePath(TreePath path) { setExpandedState(path, false); }

  public void collapseRow(int row) { collapsePath(getPathForRow(row)); }

  public boolean isCollapsed(TreePath path) { return !isExpanded(path); }

  public boolean isExpanded(TreePath path) {
    if (path == null) {
      return false;
    }
    Object value = expandedState.get(path);
    if (value == null || !((Boolean) value).booleanValue()) {
      return false;
    }
    TreePath parentPath = path.getParentPath();
    if (parentPath != null) {
      return isExpanded(parentPath);
    }
    return true;
  }

  public boolean isExpanded(int row) {
    TreePath path = getPathForRow(row);
    if (path != null) {
      Boolean value = expandedState.get(path);
      return (value != null && value.booleanValue());
    }
    return false;
  }

  public boolean isCollapsed(int row) {
    TreePath path = getPathForRow(row);
    if (path != null) {
      Boolean value = expandedState.get(path);
      return (value != null && !value.booleanValue());
    }
    return true;
  }

  public boolean hasBeenExpanded(TreePath path) { return (path != null && expandedState.get(path) != null); }

  public Enumeration<TreePath> getExpandedDescendants(TreePath parent) {
    if (!isExpanded(parent)) {
      return null;
    }
    Enumeration<TreePath> toggledPaths = expandedState.keys();
    Vector<TreePath> elements = null;
    if (toggledPaths != null) {
      while (toggledPaths.hasMoreElements()) {
        TreePath path = toggledPaths.nextElement();
        Object value = expandedState.get(path);
        if (path != parent && value != null && ((Boolean) value).booleanValue() && parent.isDescendant(path)
            && isVisible(path)) {
          if (elements == null) {
            elements = new Vector<TreePath>();
          }
          elements.addElement(path);
        }
      }
    }
    if (elements == null) {
      Set<TreePath> empty = Collections.emptySet();
      return Collections.enumeration(empty);
    }
    return elements.elements();
  }

  public TreePath getNextMatch(String prefix, int startingRow, Position.Bias bias) {
    int max = getRowCount();
    if (prefix == null) {
      throw new IllegalArgumentException();
    }
    if (startingRow < 0 || startingRow >= max) {
      throw new IllegalArgumentException();
    }
    prefix = prefix.toUpperCase();
    int increment = (bias == Position.Bias.Forward) ? 1 : -1;
    int row = startingRow;
    do {
      TreePath path = getPathForRow(row);
      String text = convertValueToText(path.getLastPathComponent(), isRowSelected(row), isExpanded(row), true, row,
          false);
      if (text.toUpperCase().startsWith(prefix)) {
        return path;
      }
      row = (row + increment + max) % max;
    } while (row != startingRow);
    return null;
  }

  public void addTreeExpansionListener(TreeExpansionListener tel) {
    listenerList.add(TreeExpansionListener.class, tel);
  }

  public void removeTreeExpansionListener(TreeExpansionListener tel) {
    listenerList.remove(TreeExpansionListener.class, tel);
  }

  public TreeExpansionListener[] getTreeExpansionListeners() {
    return listenerList.getListeners(TreeExpansionListener.class);
  }

  public void addTreeWillExpandListener(TreeWillExpandListener tel) {
    listenerList.add(TreeWillExpandListener.class, tel);
  }

  public void removeTreeWillExpandListener(TreeWillExpandListener tel) {
    listenerList.remove(TreeWillExpandListener.class, tel);
  }

  public TreeWillExpandListener[] getTreeWillExpandListeners() {
    return listenerList.getListeners(TreeWillExpandListener.class);
  }

  public void fireTreeExpanded(TreePath path) {
    Object[] listeners = listenerList.getListenerList();
    TreeExpansionEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeExpansionListener.class) {
        if (e == null) {
          e = new TreeExpansionEvent(this, path);
        }
        ((TreeExpansionListener) listeners[i + 1]).treeExpanded(e);
      }
    }
  }

  public void fireTreeCollapsed(TreePath path) {
    Object[] listeners = listenerList.getListenerList();
    TreeExpansionEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeExpansionListener.class) {
        if (e == null) {
          e = new TreeExpansionEvent(this, path);
        }
        ((TreeExpansionListener) listeners[i + 1]).treeCollapsed(e);
      }
    }
  }

  public void fireTreeWillExpand(TreePath path) throws ExpandVetoException {
    Object[] listeners = listenerList.getListenerList();
    TreeExpansionEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeWillExpandListener.class) {
        if (e == null) {
          e = new TreeExpansionEvent(this, path);
        }
        ((TreeWillExpandListener) listeners[i + 1]).treeWillExpand(e);
      }
    }
  }

  public void fireTreeWillCollapse(TreePath path) throws ExpandVetoException {
    Object[] listeners = listenerList.getListenerList();
    TreeExpansionEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeWillExpandListener.class) {
        if (e == null) {
          e = new TreeExpansionEvent(this, path);
        }
        ((TreeWillExpandListener) listeners[i + 1]).treeWillCollapse(e);
      }
    }
  }

  public void addTreeSelectionListener(TreeSelectionListener tsl) {
    listenerList.add(TreeSelectionListener.class, tsl);
  }

  public void removeTreeSelectionListener(TreeSelectionListener tsl) {
    listenerList.remove(TreeSelectionListener.class, tsl);
  }

  public TreeSelectionListener[] getTreeSelectionListeners() {
    return listenerList.getListeners(TreeSelectionListener.class);
  }

  protected void fireValueChanged(TreeSelectionEvent e) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeSelectionListener.class) {
        ((TreeSelectionListener) listeners[i + 1]).valueChanged(e);
      }
    }
  }

  public void treeDidChange() {
    revalidate();
    repaint();
  }

  protected void setExpandedState(TreePath path, boolean state) {
    if (path != null) {
      Stack<TreePath> stack;
      TreePath parentPath = path.getParentPath();
      if (expandedStack.size() == 0) {
        stack = new Stack<TreePath>();
      } else {
        stack = expandedStack.pop();
      }
      try {
        while (parentPath != null) {
          if (isExpanded(parentPath)) {
            parentPath = null;
          } else {
            stack.push(parentPath);
            parentPath = parentPath.getParentPath();
          }
        }
        for (int counter = stack.size() - 1; counter >= 0; counter--) {
          parentPath = stack.pop();
          if (!isExpanded(parentPath)) {
            try {
              fireTreeWillExpand(parentPath);
            } catch (ExpandVetoException eve) {
              return;
            }
            expandedState.put(parentPath, Boolean.TRUE);
            treeState.setExpandedState(parentPath, true);
            fireTreeExpanded(parentPath);
          }
        }
      } finally {
        if (expandedStack.size() < 11) {
          stack.removeAllElements();
          expandedStack.push(stack);
        }
      }
      if (!state) {
        if (isExpanded(path)) {
          try {
            fireTreeWillCollapse(path);
          } catch (ExpandVetoException eve) {
            return;
          }
          expandedState.put(path, Boolean.FALSE);
          treeState.setExpandedState(path, false);
          fireTreeCollapsed(path);
          if (removeDescendantSelectedPaths(path, false) && !isPathSelected(path)) {
            addSelectionPath(path);
          }
          selectionModel.resetRowSelection();
        }
      } else {
        if (!isExpanded(path)) {
          try {
            fireTreeWillExpand(path);
          } catch (ExpandVetoException eve) {
            return;
          }
          expandedState.put(path, Boolean.TRUE);
          treeState.setExpandedState(path, true);
          fireTreeExpanded(path);
          selectionModel.resetRowSelection();
        }
      }
    }
  }

  protected void clearToggledPaths() { expandedState.clear(); }

  protected Enumeration<TreePath> getDescendantToggledPaths(TreePath parent) {
    if (parent == null) {
      return null;
    }
    Vector<TreePath> descendants = new Vector<TreePath>();
    Enumeration<TreePath> nodes = expandedState.keys();
    while (nodes.hasMoreElements()) {
      TreePath path = nodes.nextElement();
      if (parent.isDescendant(path)) {
        descendants.addElement(path);
      }
    }
    return descendants.elements();
  }

  protected void removeDescendantToggledPaths(Enumeration<TreePath> toRemove) {
    if (toRemove != null) {
      while (toRemove.hasMoreElements()) {
        Enumeration<?> descendants = getDescendantToggledPaths(toRemove.nextElement());
        if (descendants != null) {
          while (descendants.hasMoreElements()) {
            expandedState.remove(descendants.nextElement());
          }
        }
      }
    }
  }

  protected boolean removeDescendantSelectedPaths(TreePath path, boolean includePath) {
    TreePath[] toRemove = getDescendantSelectedPaths(path, includePath);
    if (toRemove != null) {
      getSelectionModel().removeSelectionPaths(toRemove);
      return true;
    }
    return false;
  }

  private TreePath[] getDescendantSelectedPaths(TreePath path, boolean includePath) {
    TreeSelectionModel sm = getSelectionModel();
    TreePath[] selPaths = (sm != null) ? sm.getSelectionPaths() : null;
    if (selPaths != null) {
      boolean shouldRemove = false;
      for (int counter = selPaths.length - 1; counter >= 0; counter--) {
        if (selPaths[counter] != null && path.isDescendant(selPaths[counter])
            && (!path.equals(selPaths[counter]) || includePath)) {
          shouldRemove = true;
        } else {
          selPaths[counter] = null;
        }
      }
      if (!shouldRemove) {
        selPaths = null;
      }
      return selPaths;
    }
    return null;
  }

  void removeDescendantSelectedPaths(TreeModelEvent e) {
    TreePath pPath = e.getTreePath();
    Object[] oldChildren = e.getChildren();
    TreeSelectionModel sm = getSelectionModel();
    if (sm != null && pPath != null && oldChildren != null && oldChildren.length > 0) {
      for (int counter = oldChildren.length - 1; counter >= 0; counter--) {
        removeDescendantSelectedPaths(pPath.pathByAddingChild(oldChildren[counter]), true);
      }
    }
  }

  protected TreeModelListener createTreeModelListener() { return new TreeModelHandler(); }

  protected class TreeModelHandler implements TreeModelListener {
    public void treeNodesChanged(TreeModelEvent e) {
      treeState.treeNodesChanged(e);
      selectionModel.resetRowSelection();
    }

    public void treeNodesInserted(TreeModelEvent e) {
      treeState.treeNodesInserted(e);
      selectionModel.resetRowSelection();
    }

    public void treeStructureChanged(TreeModelEvent e) {
      if (e == null) {
        return;
      }
      TreePath parent = e.getTreePath();
      if (parent == null) {
        return;
      }
      if (parent.getPathCount() == 1) {
        clearToggledPaths();
        if (treeModel.getRoot() != null && !treeModel.isLeaf(treeModel.getRoot())) {
          expandedState.put(parent, Boolean.TRUE);
        }
      } else if (expandedState.get(parent) != null) {
        Vector<TreePath> toRemove = new Vector<TreePath>(1);
        boolean isExpanded = isExpanded(parent);
        toRemove.addElement(parent);
        removeDescendantToggledPaths(toRemove.elements());
        if (isExpanded) {
          TreeModel model = getModel();
          if (model == null || model.isLeaf(parent.getLastPathComponent())) {
            collapsePath(parent);
          } else {
            expandedState.put(parent, Boolean.TRUE);
          }
        }
      }
      treeState.treeStructureChanged(e);
      removeDescendantSelectedPaths(parent, false);
      selectionModel.resetRowSelection();
    }

    public void treeNodesRemoved(TreeModelEvent e) {
      if (e == null) {
        return;
      }
      TreePath parent = e.getTreePath();
      Object[] children = e.getChildren();
      if (children == null) {
        return;
      }
      Vector<TreePath> toRemove = new Vector<TreePath>(Math.max(1, children.length));
      for (int counter = children.length - 1; counter >= 0; counter--) {
        TreePath rPath = parent.pathByAddingChild(children[counter]);
        if (expandedState.get(rPath) != null) {
          toRemove.addElement(rPath);
        }
      }
      if (toRemove.size() > 0) {
        removeDescendantToggledPaths(toRemove.elements());
      }
      TreeModel model = getModel();
      if (model == null || model.isLeaf(parent.getLastPathComponent())) {
        expandedState.remove(parent);
      }
      removeDescendantSelectedPaths(e);
      treeState.treeNodesRemoved(e);
      selectionModel.resetRowSelection();
    }
  }

  protected class TreeSelectionRedirector implements TreeSelectionListener, java.io.Serializable {
    public void valueChanged(TreeSelectionEvent e) {
      TreeSelectionEvent newEvent;
      if (e.getSource() == null) {
        newEvent = e;
      } else {
        newEvent = (TreeSelectionEvent) e.cloneWithSource(JTree.this);
      }
      setLeadSelectionPath(e.getNewLeadSelectionPath());
      setAnchorSelectionPath(e.getNewLeadSelectionPath());
      if (expandsSelectedPaths) {
        TreePath[] paths = selectionModel.getSelectionPaths();
        if (paths != null) {
          for (TreePath p : paths) {
            if (treeModel != null) {
              makeVisible(p);
            }
          }
        }
      }
      fireValueChanged(newEvent);
    }
  }

  public Dimension getPreferredScrollableViewportSize() {
    int width = getPreferredSize().width;
    int visRows = getVisibleRowCount();
    int height = -1;
    if (isFixedRowHeight()) {
      height = visRows * getRowHeight();
    } else {
      Rectangle firstRow = getRowBounds(0);
      if (firstRow != null) {
        height = visRows * firstRow.height;
      }
    }
    if (height == -1) {
      height = getPreferredSize().height;
    }
    return new Dimension(width, height);
  }

  public Dimension getPreferredSize() {
    if (getUI() != null) {
      Dimension d = getUI().getPreferredSize(this);
      if (d != null) {
        return d;
      }
    }
    Insets insets = getInsets();
    return new Dimension(treeState.getPreferredWidth(null) + insets.left + insets.right,
        treeState.getPreferredHeight() + insets.top + insets.bottom);
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.VERTICAL) {
      Rectangle rowBounds;
      int firstIndex = getClosestRowForLocation(0, visibleRect.y);
      if (firstIndex != -1) {
        rowBounds = getRowBounds(firstIndex);
        if (rowBounds.y != visibleRect.y) {
          if (direction < 0) {
            return Math.max(0, (visibleRect.y - rowBounds.y));
          }
          return (rowBounds.y + rowBounds.height - visibleRect.y);
        }
        if (direction < 0) {
          if (firstIndex > 0) {
            rowBounds = getRowBounds(firstIndex - 1);
            return rowBounds.height;
          }
        } else {
          return rowBounds.height;
        }
      }
      return 0;
    }
    return 4;
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return (orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
  }

  public boolean getScrollableTracksViewportWidth() {
    Container parent = SwingUtilities.getUnwrappedParent(this);
    if (parent instanceof JViewport) {
      return parent.getWidth() > getPreferredSize().width;
    }
    return false;
  }

  public boolean getScrollableTracksViewportHeight() {
    Container parent = SwingUtilities.getUnwrappedParent(this);
    if (parent instanceof JViewport) {
      return parent.getHeight() > getPreferredSize().height;
    }
    return false;
  }

  public String getToolTipText(MouseEvent event) {
    String tip = null;
    if (event != null) {
      Point p = event.getPoint();
      int selRow = getRowForLocation(p.x, p.y);
      TreeCellRenderer r = getCellRenderer();
      if (selRow != -1 && r != null) {
        TreePath path = getPathForRow(selRow);
        Object lastPath = path.getLastPathComponent();
        Component rComponent = r.getTreeCellRendererComponent(this, lastPath, isRowSelected(selRow),
            isExpanded(selRow), getModel().isLeaf(lastPath), selRow, true);
        if (rComponent instanceof JComponent) {
          Rectangle pathBounds = getPathBounds(path);
          p.translate(-pathBounds.x, -pathBounds.y);
          MouseEvent newEvent = new MouseEvent(rComponent, event.getID(), event.getWhen(), event.getModifiers(), p.x,
              p.y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(), event.isPopupTrigger(),
              MouseEvent.NOBUTTON);
          tip = ((JComponent) rComponent).getToolTipText(newEvent);
        }
      }
    }
    if (tip == null) {
      tip = getToolTipText();
    }
    return tip;
  }

  protected String paramString() {
    String rootVisibleString = (rootVisible ? "true" : "false");
    String showsRootHandlesString = (showsRootHandles ? "true" : "false");
    String editableString = (editable ? "true" : "false");
    String largeModelString = (largeModel ? "true" : "false");
    String invokesStopCellEditingString = (invokesStopCellEditing ? "true" : "false");
    String scrollsOnExpandString = (scrollsOnExpand ? "true" : "false");
    return super.paramString() + ",editable=" + editableString + ",invokesStopCellEditing="
        + invokesStopCellEditingString + ",largeModel=" + largeModelString + ",rootVisible=" + rootVisibleString
        + ",rowHeight=" + rowHeight + ",scrollsOnExpand=" + scrollsOnExpandString + ",showsRootHandles="
        + showsRootHandlesString + ",toggleClickCount=" + toggleClickCount + ",visibleRowCount=" + visibleRowCount;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleJTree();
    }
    return accessibleContext;
  }

  protected class AccessibleJTree extends AccessibleJComponent {
    public AccessibleJTree() { }

    public AccessibleRole getAccessibleRole() { return AccessibleRole.TREE; }

    public int getAccessibleChildrenCount() { return getRowCount(); }

    public Accessible getAccessibleChild(int i) { return null; }
  }

  protected static class EmptySelectionModel extends DefaultTreeSelectionModel {
    protected static final EmptySelectionModel sharedInstance = new EmptySelectionModel();

    public static EmptySelectionModel sharedInstance() { return sharedInstance; }

    public void setSelectionPaths(TreePath[] pPaths) { }

    public void addSelectionPaths(TreePath[] paths) { }

    public void removeSelectionPaths(TreePath[] paths) { }
  }

  public static final class DropLocation extends TransferHandler.DropLocation {
    private final TreePath path;
    private final int index;

    DropLocation(Point p, TreePath path, int index) {
      super(p);
      this.path = path;
      this.index = index;
    }

    public int getChildIndex() { return index; }

    public TreePath getPath() { return path; }

    public String toString() {
      return getClass().getName() + "[dropPoint=" + getDropPoint() + ",path=" + path + ",childIndex=" + index + "]";
    }
  }
}
