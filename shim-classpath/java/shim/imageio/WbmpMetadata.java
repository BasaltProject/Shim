/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;

public final class WbmpMetadata extends IIOMetadata {
  public static final String kFormat = "javax_imageio_wbmp_1.0";
  private final short type;
  private final int width;
  private final int height;

  public WbmpMetadata(int width, int height) {
    super(true, kFormat, null, null, null);
    this.type = 0;
    this.width = width;
    this.height = height;
  }

  public boolean isReadOnly() { return true; }

  private static void add(IIOMetadataNode parent, String name, Object value) {
    IIOMetadataNode n = new IIOMetadataNode(name);
    n.setUserObject(value);
    n.setNodeValue(value.toString());
    parent.appendChild(n);
  }

  public Node getAsTree(String formatName) {
    if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
      return getStandardTree();
    }
    if (kFormat.equals(formatName)) {
      IIOMetadataNode root = new IIOMetadataNode(kFormat);
      add(root, "WBMPType", Short.valueOf(type));
      add(root, "Width", Integer.valueOf(width));
      add(root, "Height", Integer.valueOf(height));
      return root;
    }
    throw new IllegalArgumentException("Not a recognized format!");
  }

  protected IIOMetadataNode getStandardChromaNode() {
    IIOMetadataNode chroma = new IIOMetadataNode("Chroma");
    IIOMetadataNode n = new IIOMetadataNode("BlackIsZero");
    n.setAttribute("value", "TRUE");
    chroma.appendChild(n);
    return chroma;
  }

  protected IIOMetadataNode getStandardDimensionNode() {
    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
    IIOMetadataNode n = new IIOMetadataNode("ImageOrientation");
    n.setAttribute("value", "Normal");
    dim.appendChild(n);
    return dim;
  }

  public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
    throw new IllegalStateException("Metadata is read-only!");
  }

  public void reset() {
    throw new IllegalStateException("Metadata is read-only!");
  }
}
