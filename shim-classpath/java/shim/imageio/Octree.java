// MIT License
//
// Copyright (c) Basalt Project
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the "Software"), to deal in the Software without restriction, subject to the MIT License.
package shim.imageio;

final class Octree {
  private static final class Node {
    boolean leaf;
    int childCount;
    int colorCount;
    int level;
    long red;
    long green;
    long blue;
    int index;
    Node next;
    final Node[] children = new Node[8];
  }

  private final Node[] reduceList = new Node[9];
  private Node root;
  private int size;
  private int required = 256;
  private boolean transparent;
  private final boolean alphaModel;

  int[] palette;
  int paletteSize;
  int transparentIndex = -1;

  Octree(boolean alphaModel) {
    this.alphaModel = alphaModel;
  }

  static Octree build(int[] argb, int w, int h, boolean alphaModel) {
    Octree t = new Octree(alphaModel);
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        int p = argb[(h - i - 1) * w + (w - j - 1)];
        if (alphaModel && (p >>> 24) != 255) {
          if (!t.transparent) {
            t.transparent = true;
            t.required--;
          }
        } else {
          t.root = t.insert(t.root, p, 0);
        }
        if (t.size > t.required) {
          t.reduce();
        }
      }
    }
    t.finish();
    return t;
  }

  private static int branch(int p, int level) {
    int shift = 8 - level;
    return (1 & (((p >> 16) & 0xff) >> shift)) << 2 | (1 & (((p >> 8) & 0xff) >> shift)) << 1
        | (1 & ((p & 0xff) >> shift));
  }

  private Node insert(Node n, int p, int level) {
    if (n == null) {
      n = new Node();
      n.level = level;
      n.leaf = level > 8;
      if (n.leaf) {
        size++;
      }
    }
    n.colorCount++;
    n.red += (p >> 16) & 0xff;
    n.green += (p >> 8) & 0xff;
    n.blue += p & 0xff;
    if (!n.leaf) {
      int b = branch(p, level);
      if (n.children[b] == null) {
        n.childCount++;
        if (n.childCount == 2) {
          n.next = reduceList[level];
          reduceList[level] = n;
        }
      }
      n.children[b] = insert(n.children[b], p, level + 1);
    }
    return n;
  }

  private static int leaves(Node n) {
    if (n.leaf) {
      return 0;
    }
    int c = 0;
    for (Node ch : n.children) {
      if (ch != null) {
        c += ch.leaf ? 1 : leaves(ch);
      }
    }
    return c;
  }

  private void reduce() {
    int level = reduceList.length - 1;
    while (reduceList[level] == null && level >= 0) {
      level--;
    }
    Node node = reduceList[level];
    if (node == null) {
      return;
    }
    Node cur = node;
    int count = cur.colorCount;
    while (cur.next != null) {
      if (count > cur.next.colorCount) {
        node = cur;
        count = cur.colorCount;
      }
      cur = cur.next;
    }
    if (node == reduceList[level]) {
      reduceList[level] = node.next;
    } else {
      cur = node.next;
      node.next = cur.next;
      node = cur;
    }
    if (node.leaf) {
      return;
    }
    int lc = leaves(node);
    node.leaf = true;
    size -= lc - 1;
    java.util.Arrays.fill(node.children, null);
    node.childCount = 0;
  }

  private void finish() {
    int extra = transparent ? 1 : 0;
    palette = new int[size + extra];
    paletteSize = size + extra;
    if (transparent) {
      transparentIndex = 0;
    }
    if (root != null) {
      assign(root, extra);
    }
  }

  private int assign(Node n, int next) {
    if (n.leaf) {
      palette[next] = 0xff000000 | (int) (n.red / n.colorCount) << 16 & 0xff0000
          | (int) (n.green / n.colorCount) << 8 & 0xff00 | (int) (n.blue / n.colorCount) & 0xff;
      n.index = next;
      return next + 1;
    }
    for (Node ch : n.children) {
      if (ch != null) {
        next = assign(ch, next);
      }
    }
    return next;
  }

  int indexOf(int p) {
    if (alphaModel && (p >>> 24) != 255) {
      return 0;
    }
    Node n = root;
    while (!n.leaf) {
      n = n.children[branch(p, n.level)];
    }
    return n.index;
  }
}
