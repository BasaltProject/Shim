/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.tree;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class DefaultMutableTreeNode implements Cloneable, MutableTreeNode, Serializable {
  private static final long serialVersionUID = -4298474751201349152L;

  public static final Enumeration<TreeNode> EMPTY_ENUMERATION = Collections.emptyEnumeration();

  protected MutableTreeNode parent;
  protected Vector<TreeNode> children;
  protected transient Object userObject;
  protected boolean allowsChildren;

  public DefaultMutableTreeNode() { this(null); }

  public DefaultMutableTreeNode(Object userObject) { this(userObject, true); }

  public DefaultMutableTreeNode(Object userObject, boolean allowsChildren) {
    this.parent = null;
    this.userObject = userObject;
    this.allowsChildren = allowsChildren;
  }

  private static Enumeration<TreeNode> enumerate(final Iterator<TreeNode> it) {
    return new Enumeration<TreeNode>() {
      public boolean hasMoreElements() { return it.hasNext(); }

      public TreeNode nextElement() { return it.next(); }
    };
  }

  private static Iterator<TreeNode> preorderIterator(final TreeNode start) {
    final Deque<Iterator<? extends TreeNode>> pending = new ArrayDeque<Iterator<? extends TreeNode>>();
    pending.push(Collections.singletonList(start).iterator());
    return new Iterator<TreeNode>() {
      public boolean hasNext() {
        while (!pending.isEmpty() && !pending.peek().hasNext()) {
          pending.pop();
        }
        return !pending.isEmpty();
      }

      public TreeNode next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements");
        }
        TreeNode n = pending.peek().next();
        Enumeration<? extends TreeNode> kids = n.children();
        List<TreeNode> list = new ArrayList<TreeNode>();
        while (kids != null && kids.hasMoreElements()) {
          list.add(kids.nextElement());
        }
        pending.push(list.iterator());
        return n;
      }

      public void remove() { throw new UnsupportedOperationException(); }
    };
  }

  private static void collectPostorder(TreeNode n, List<TreeNode> out) {
    int count = n.getChildCount();
    for (int i = 0; i < count; i++) {
      collectPostorder(n.getChildAt(i), out);
    }
    out.add(n);
  }

  private void checkNoCycle(MutableTreeNode candidate) {
    if (candidate == this || isNodeAncestor(candidate)) {
      throw new IllegalArgumentException("new child is an ancestor");
    }
  }

  public void insert(MutableTreeNode newChild, int childIndex) {
    if (!allowsChildren) {
      throw new IllegalStateException("node does not allow children");
    }
    if (newChild == null) {
      throw new IllegalArgumentException("new child is null");
    }
    checkNoCycle(newChild);
    MutableTreeNode previousParent = (MutableTreeNode) newChild.getParent();
    if (previousParent != null) {
      previousParent.remove(newChild);
    }
    newChild.setParent(this);
    if (children == null) {
      children = new Vector<TreeNode>();
    }
    children.insertElementAt(newChild, childIndex);
  }

  public void remove(int childIndex) {
    MutableTreeNode child = (MutableTreeNode) getChildAt(childIndex);
    children.removeElementAt(childIndex);
    child.setParent(null);
  }

  public void setParent(MutableTreeNode newParent) { parent = newParent; }

  public TreeNode getParent() { return parent; }

  public TreeNode getChildAt(int index) {
    if (children == null) {
      throw new ArrayIndexOutOfBoundsException("node has no children");
    }
    return children.elementAt(index);
  }

  public int getChildCount() { return children == null ? 0 : children.size(); }

  public int getIndex(TreeNode aChild) {
    if (aChild == null) {
      throw new IllegalArgumentException("argument is null");
    }
    return children == null ? -1 : children.indexOf(aChild);
  }

  public Enumeration<TreeNode> children() { return children == null ? EMPTY_ENUMERATION : children.elements(); }

  public void setAllowsChildren(boolean allows) {
    if (allows != allowsChildren) {
      allowsChildren = allows;
      if (!allowsChildren) {
        removeAllChildren();
      }
    }
  }

  public boolean getAllowsChildren() { return allowsChildren; }

  public void setUserObject(Object userObject) { this.userObject = userObject; }

  public Object getUserObject() { return userObject; }

  public void removeFromParent() {
    MutableTreeNode p = (MutableTreeNode) getParent();
    if (p != null) {
      p.remove(this);
    }
  }

  public void remove(MutableTreeNode aChild) {
    if (aChild == null) {
      throw new IllegalArgumentException("argument is null");
    }
    int at = getIndex(aChild);
    if (at < 0) {
      throw new IllegalArgumentException("argument is not a child");
    }
    remove(at);
  }

  public void removeAllChildren() {
    for (int i = getChildCount() - 1; i >= 0; i--) {
      remove(i);
    }
  }

  public void add(MutableTreeNode newChild) {
    if (newChild != null && newChild.getParent() == this) {
      insert(newChild, getChildCount() - 1);
    } else {
      insert(newChild, getChildCount());
    }
  }

  public boolean isNodeAncestor(TreeNode anotherNode) {
    if (anotherNode == null) {
      return false;
    }
    for (TreeNode n = this; n != null; n = n.getParent()) {
      if (n == anotherNode) {
        return true;
      }
    }
    return false;
  }

  public boolean isNodeDescendant(DefaultMutableTreeNode anotherNode) {
    return anotherNode != null && anotherNode.isNodeAncestor(this);
  }

  public TreeNode getSharedAncestor(DefaultMutableTreeNode aNode) {
    if (aNode == this) {
      return this;
    }
    if (aNode == null) {
      return null;
    }
    List<TreeNode> mine = new ArrayList<TreeNode>();
    for (TreeNode n = this; n != null; n = n.getParent()) {
      mine.add(n);
    }
    List<TreeNode> theirs = new ArrayList<TreeNode>();
    for (TreeNode n = aNode; n != null; n = n.getParent()) {
      theirs.add(n);
    }
    TreeNode shared = null;
    int i = mine.size() - 1;
    int j = theirs.size() - 1;
    while (i >= 0 && j >= 0 && mine.get(i) == theirs.get(j)) {
      shared = mine.get(i);
      i--;
      j--;
    }
    return shared;
  }

  public boolean isNodeRelated(DefaultMutableTreeNode aNode) {
    return aNode != null && getRoot() == aNode.getRoot();
  }

  public int getDepth() {
    int deepest = 0;
    Enumeration<TreeNode> all = breadthFirstEnumeration();
    TreeNode last = null;
    while (all.hasMoreElements()) {
      last = all.nextElement();
    }
    for (TreeNode n = last; n != this && n != null; n = n.getParent()) {
      deepest++;
    }
    return deepest;
  }

  public int getLevel() {
    int level = 0;
    for (TreeNode n = getParent(); n != null; n = n.getParent()) {
      level++;
    }
    return level;
  }

  public TreeNode[] getPath() { return getPathToRoot(this, 0); }

  protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
    if (aNode == null) {
      return depth == 0 ? null : new TreeNode[depth];
    }
    TreeNode[] path = getPathToRoot(aNode.getParent(), depth + 1);
    path[path.length - depth - 1] = aNode;
    return path;
  }

  public Object[] getUserObjectPath() {
    TreeNode[] realPath = getPath();
    Object[] result = new Object[realPath.length];
    for (int i = 0; i < realPath.length; i++) {
      result[i] = ((DefaultMutableTreeNode) realPath[i]).getUserObject();
    }
    return result;
  }

  public TreeNode getRoot() {
    TreeNode n = this;
    while (n.getParent() != null) {
      n = n.getParent();
    }
    return n;
  }

  public boolean isRoot() { return getParent() == null; }

  public Enumeration<TreeNode> preorderEnumeration() { return enumerate(preorderIterator(this)); }

  public Enumeration<TreeNode> postorderEnumeration() {
    List<TreeNode> out = new ArrayList<TreeNode>();
    collectPostorder(this, out);
    return enumerate(out.iterator());
  }

  public Enumeration<TreeNode> breadthFirstEnumeration() {
    List<TreeNode> out = new ArrayList<TreeNode>();
    Deque<TreeNode> queue = new ArrayDeque<TreeNode>();
    queue.add(this);
    while (!queue.isEmpty()) {
      TreeNode n = queue.poll();
      out.add(n);
      Enumeration<? extends TreeNode> kids = n.children();
      while (kids != null && kids.hasMoreElements()) {
        queue.add(kids.nextElement());
      }
    }
    return enumerate(out.iterator());
  }

  public Enumeration<TreeNode> depthFirstEnumeration() { return postorderEnumeration(); }

  public Enumeration<TreeNode> pathFromAncestorEnumeration(TreeNode ancestor) {
    if (ancestor == null) {
      throw new IllegalArgumentException("argument is null");
    }
    Deque<TreeNode> chain = new ArrayDeque<TreeNode>();
    TreeNode n = this;
    chain.addFirst(n);
    while (n != ancestor) {
      n = n.getParent();
      if (n == null) {
        if (ancestor == null) {
          break;
        }
        throw new IllegalArgumentException("node " + ancestor + " is not an ancestor of " + this);
      }
      chain.addFirst(n);
    }
    return enumerate(new ArrayList<TreeNode>(chain).iterator());
  }

  public boolean isNodeChild(TreeNode aNode) { return aNode != null && getChildCount() > 0 && aNode.getParent() == this; }

  public TreeNode getFirstChild() {
    if (getChildCount() == 0) {
      throw new NoSuchElementException("node has no children");
    }
    return getChildAt(0);
  }

  public TreeNode getLastChild() {
    if (getChildCount() == 0) {
      throw new NoSuchElementException("node has no children");
    }
    return getChildAt(getChildCount() - 1);
  }

  public TreeNode getChildAfter(TreeNode aChild) {
    if (aChild == null) {
      throw new IllegalArgumentException("argument is null");
    }
    int at = getIndex(aChild);
    if (at == -1) {
      throw new IllegalArgumentException("node is not a child");
    }
    return at < getChildCount() - 1 ? getChildAt(at + 1) : null;
  }

  public TreeNode getChildBefore(TreeNode aChild) {
    if (aChild == null) {
      throw new IllegalArgumentException("argument is null");
    }
    int at = getIndex(aChild);
    if (at == -1) {
      throw new IllegalArgumentException("argument is not a child");
    }
    return at > 0 ? getChildAt(at - 1) : null;
  }

  public boolean isNodeSibling(TreeNode anotherNode) {
    if (anotherNode == null) {
      return false;
    }
    if (anotherNode == this) {
      return true;
    }
    TreeNode myParent = getParent();
    return myParent != null && myParent == anotherNode.getParent() && ((DefaultMutableTreeNode) getParent()).isNodeChild(anotherNode);
  }

  public int getSiblingCount() {
    TreeNode myParent = getParent();
    return myParent == null ? 1 : myParent.getChildCount();
  }

  public DefaultMutableTreeNode getNextSibling() {
    DefaultMutableTreeNode p = (DefaultMutableTreeNode) getParent();
    return p == null ? null : (DefaultMutableTreeNode) p.getChildAfter(this);
  }

  public DefaultMutableTreeNode getPreviousSibling() {
    DefaultMutableTreeNode p = (DefaultMutableTreeNode) getParent();
    return p == null ? null : (DefaultMutableTreeNode) p.getChildBefore(this);
  }

  public boolean isLeaf() { return getChildCount() == 0; }

  public DefaultMutableTreeNode getFirstLeaf() {
    DefaultMutableTreeNode node = this;
    while (!node.isLeaf()) {
      node = (DefaultMutableTreeNode) node.getFirstChild();
    }
    return node;
  }

  public DefaultMutableTreeNode getLastLeaf() {
    DefaultMutableTreeNode node = this;
    while (!node.isLeaf()) {
      node = (DefaultMutableTreeNode) node.getLastChild();
    }
    return node;
  }

  public DefaultMutableTreeNode getNextLeaf() {
    DefaultMutableTreeNode up = (DefaultMutableTreeNode) getParent();
    if (up == null) {
      return null;
    }
    DefaultMutableTreeNode sibling = (DefaultMutableTreeNode) up.getChildAfter(this);
    if (sibling != null) {
      return sibling.getFirstLeaf();
    }
    return up.getNextLeaf();
  }

  public DefaultMutableTreeNode getPreviousLeaf() {
    DefaultMutableTreeNode up = (DefaultMutableTreeNode) getParent();
    if (up == null) {
      return null;
    }
    DefaultMutableTreeNode sibling = (DefaultMutableTreeNode) up.getChildBefore(this);
    if (sibling != null) {
      return sibling.getLastLeaf();
    }
    return up.getPreviousLeaf();
  }

  public int getLeafCount() {
    int leaves = 0;
    Enumeration<TreeNode> all = breadthFirstEnumeration();
    while (all.hasMoreElements()) {
      if (all.nextElement().isLeaf()) {
        leaves++;
      }
    }
    return leaves;
  }

  public DefaultMutableTreeNode getNextNode() {
    if (getChildCount() == 0) {
      DefaultMutableTreeNode n = this;
      while (n != null) {
        DefaultMutableTreeNode s = n.getNextSibling();
        if (s != null) {
          return s;
        }
        n = (DefaultMutableTreeNode) n.getParent();
      }
      return null;
    }
    return (DefaultMutableTreeNode) getChildAt(0);
  }

  public DefaultMutableTreeNode getPreviousNode() {
    DefaultMutableTreeNode p = (DefaultMutableTreeNode) getParent();
    if (p == null) {
      return null;
    }
    DefaultMutableTreeNode s = getPreviousSibling();
    if (s == null) {
      return p;
    }
    return s.getChildCount() == 0 ? s : s.getLastLeaf();
  }

  public String toString() { return userObject == null ? "" : userObject.toString(); }

  public Object clone() {
    try {
      DefaultMutableTreeNode copy = (DefaultMutableTreeNode) super.clone();
      copy.children = null;
      copy.parent = null;
      return copy;
    } catch (CloneNotSupportedException e) {
      throw new Error(e.toString());
    }
  }
}
