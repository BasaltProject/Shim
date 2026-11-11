/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

import java.io.Serializable;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

public class DefaultTreeModel implements Serializable, TreeModel {
  protected TreeNode root;
  protected EventListenerList listenerList = new EventListenerList();
  protected boolean asksAllowsChildren;

  @java.beans.ConstructorProperties({"root"})
  public DefaultTreeModel(TreeNode root) { this(root, false); }

  public DefaultTreeModel(TreeNode root, boolean asksAllowsChildren) {
    this.root = root;
    this.asksAllowsChildren = asksAllowsChildren;
  }

  public void setAsksAllowsChildren(boolean newValue) { asksAllowsChildren = newValue; }

  public boolean asksAllowsChildren() { return asksAllowsChildren; }

  public void setRoot(TreeNode newRoot) {
    Object oldRoot = root;
    root = newRoot;
    if (newRoot == null && oldRoot != null) {
      fireTreeStructureChanged(this, null);
    } else {
      nodeStructureChanged(newRoot);
    }
  }

  public Object getRoot() { return root; }

  public int getIndexOfChild(Object parent, Object child) {
    if (parent == null || child == null) {
      return -1;
    }
    return ((TreeNode) parent).getIndex((TreeNode) child);
  }

  public Object getChild(Object parent, int index) { return ((TreeNode) parent).getChildAt(index); }

  public int getChildCount(Object parent) { return ((TreeNode) parent).getChildCount(); }

  public boolean isLeaf(Object node) {
    TreeNode n = (TreeNode) node;
    return asksAllowsChildren ? !n.getAllowsChildren() : n.isLeaf();
  }

  public void reload() { reload(root); }

  public void reload(TreeNode node) {
    if (node != null) {
      fireTreeStructureChanged(this, getPathToRoot(node), null, null);
    }
  }

  public void valueForPathChanged(TreePath path, Object newValue) {
    MutableTreeNode aNode = (MutableTreeNode) path.getLastPathComponent();
    aNode.setUserObject(newValue);
    nodeChanged(aNode);
  }

  public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {
    parent.insert(newChild, index);
    nodesWereInserted(parent, new int[] {index});
  }

  public void removeNodeFromParent(MutableTreeNode node) {
    MutableTreeNode parent = (MutableTreeNode) node.getParent();
    if (parent == null) {
      throw new IllegalArgumentException("node does not have a parent.");
    }
    int[] childIndex = {parent.getIndex(node)};
    Object[] removedArray = {node};
    parent.remove(childIndex[0]);
    nodesWereRemoved(parent, childIndex, removedArray);
  }

  public void nodeChanged(TreeNode node) {
    if (listenerList != null && node != null) {
      TreeNode parent = node.getParent();
      if (parent != null) {
        int anIndex = parent.getIndex(node);
        if (anIndex != -1) {
          nodesChanged(parent, new int[] {anIndex});
        }
      } else if (node == getRoot()) {
        nodesChanged(node, null);
      }
    }
  }

  public void nodesWereInserted(TreeNode node, int[] childIndices) {
    if (listenerList != null && node != null && childIndices != null && childIndices.length > 0) {
      Object[] newChildren = new Object[childIndices.length];
      for (int i = 0; i < childIndices.length; i++) {
        newChildren[i] = node.getChildAt(childIndices[i]);
      }
      fireTreeNodesInserted(this, getPathToRoot(node), childIndices, newChildren);
    }
  }

  public void nodesWereRemoved(TreeNode node, int[] childIndices, Object[] removedChildren) {
    if (node != null && childIndices != null) {
      fireTreeNodesRemoved(this, getPathToRoot(node), childIndices, removedChildren);
    }
  }

  public void nodesChanged(TreeNode node, int[] childIndices) {
    if (node == null) {
      return;
    }
    if (childIndices != null) {
      if (childIndices.length > 0) {
        Object[] cChildren = new Object[childIndices.length];
        for (int i = 0; i < childIndices.length; i++) {
          cChildren[i] = node.getChildAt(childIndices[i]);
        }
        fireTreeNodesChanged(this, getPathToRoot(node), childIndices, cChildren);
      }
    } else if (node == getRoot()) {
      fireTreeNodesChanged(this, getPathToRoot(node), null, null);
    }
  }

  public void nodeStructureChanged(TreeNode node) {
    if (node != null) {
      fireTreeStructureChanged(this, getPathToRoot(node), null, null);
    }
  }

  public TreeNode[] getPathToRoot(TreeNode aNode) { return getPathToRoot(aNode, 0); }

  protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
    if (aNode == null) {
      return depth == 0 ? null : new TreeNode[depth];
    }
    TreeNode[] retNodes;
    if (aNode == root) {
      retNodes = new TreeNode[depth + 1];
    } else {
      retNodes = getPathToRoot(aNode.getParent(), depth + 1);
    }
    retNodes[retNodes.length - depth - 1] = aNode;
    return retNodes;
  }

  public void addTreeModelListener(TreeModelListener l) { listenerList.add(TreeModelListener.class, l); }

  public void removeTreeModelListener(TreeModelListener l) { listenerList.remove(TreeModelListener.class, l); }

  public TreeModelListener[] getTreeModelListeners() { return listenerList.getListeners(TreeModelListener.class); }

  private interface Dispatch {
    void send(TreeModelListener l, TreeModelEvent e);
  }

  private void dispatch(TreeModelEvent[] holder, Object source, Object[] path, int[] childIndices, Object[] children,
      Dispatch how) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        if (holder[0] == null) {
          holder[0] = new TreeModelEvent(source, path, childIndices, children);
        }
        how.send((TreeModelListener) listeners[i + 1], holder[0]);
      }
    }
  }

  protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
    dispatch(new TreeModelEvent[1], source, path, childIndices, children, new Dispatch() {
      public void send(TreeModelListener l, TreeModelEvent e) { l.treeNodesChanged(e); }
    });
  }

  protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
    dispatch(new TreeModelEvent[1], source, path, childIndices, children, new Dispatch() {
      public void send(TreeModelListener l, TreeModelEvent e) { l.treeNodesInserted(e); }
    });
  }

  protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
    dispatch(new TreeModelEvent[1], source, path, childIndices, children, new Dispatch() {
      public void send(TreeModelListener l, TreeModelEvent e) { l.treeNodesRemoved(e); }
    });
  }

  protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
    dispatch(new TreeModelEvent[1], source, path, childIndices, children, new Dispatch() {
      public void send(TreeModelListener l, TreeModelEvent e) { l.treeStructureChanged(e); }
    });
  }

  private void fireTreeStructureChanged(Object source, TreePath path) {
    Object[] listeners = listenerList.getListenerList();
    TreeModelEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        if (e == null) {
          e = new TreeModelEvent(source, path);
        }
        ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
      }
    }
  }

  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    return listenerList.getListeners(listenerType);
  }
}
