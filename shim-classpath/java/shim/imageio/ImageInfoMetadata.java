/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;

public final class ImageInfoMetadata extends IIOMetadata {
  private final BufferedImage image;
  private final String compression;
  private final boolean lossless;
  private Node nativeTree;

  public ImageInfoMetadata(BufferedImage image, String nativeName, String compression, boolean lossless) {
    super(true, nativeName, null, null, null);
    this.image = image;
    this.compression = compression;
    this.lossless = lossless;
  }

  public ImageInfoMetadata withNativeTree(Node tree) {
    nativeTree = tree;
    return this;
  }

  public boolean isReadOnly() { return true; }

  public Node getAsTree(String formatName) {
    if (IIOMetadataFormatImpl.standardMetadataFormatName.equals(formatName)) {
      return getStandardTree();
    }
    if (nativeMetadataFormatName.equals(formatName) && nativeTree != null) {
      return nativeTree.cloneNode(true);
    }
    if (nativeMetadataFormatName.equals(formatName)) {
      IIOMetadataNode root = new IIOMetadataNode(nativeMetadataFormatName);
      IIOMetadataNode d = new IIOMetadataNode("ImageDescriptor");
      d.setAttribute("imageWidth", Integer.toString(image.getWidth()));
      d.setAttribute("imageHeight", Integer.toString(image.getHeight()));
      root.appendChild(d);
      return root;
    }
    throw new IllegalArgumentException("Not a recognized format!");
  }

  public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
    throw new IllegalStateException("Metadata is read-only!");
  }

  public void reset() {
    throw new IllegalStateException("Metadata is read-only!");
  }

  private static IIOMetadataNode node(String name, String attr, String value) {
    IIOMetadataNode n = new IIOMetadataNode(name);
    n.setAttribute(attr, value);
    return n;
  }

  protected IIOMetadataNode getStandardChromaNode() {
    IIOMetadataNode chroma = new IIOMetadataNode("Chroma");
    boolean gray = image.getColorModel().getColorSpace().getType() == java.awt.color.ColorSpace.TYPE_GRAY;
    boolean indexed = image.getColorModel() instanceof IndexColorModel;
    chroma.appendChild(node("ColorSpaceType", "name", gray ? "GRAY" : "RGB"));
    int channels = image.getColorModel().getNumComponents();
    chroma.appendChild(node("NumChannels", "value", Integer.toString(channels)));
    chroma.appendChild(node("BlackIsZero", "value", "TRUE"));
    if (indexed) {
      IndexColorModel icm = (IndexColorModel) image.getColorModel();
      IIOMetadataNode palette = new IIOMetadataNode("Palette");
      for (int i = 0; i < icm.getMapSize(); i++) {
        IIOMetadataNode e = new IIOMetadataNode("PaletteEntry");
        e.setAttribute("index", Integer.toString(i));
        e.setAttribute("red", Integer.toString(icm.getRed(i)));
        e.setAttribute("green", Integer.toString(icm.getGreen(i)));
        e.setAttribute("blue", Integer.toString(icm.getBlue(i)));
        palette.appendChild(e);
      }
      chroma.appendChild(palette);
    }
    return chroma;
  }

  protected IIOMetadataNode getStandardCompressionNode() {
    IIOMetadataNode c = new IIOMetadataNode("Compression");
    c.appendChild(node("CompressionTypeName", "value", compression));
    c.appendChild(node("Lossless", "value", lossless ? "TRUE" : "FALSE"));
    c.appendChild(node("NumProgressiveScans", "value", "1"));
    return c;
  }

  protected IIOMetadataNode getStandardDataNode() {
    IIOMetadataNode d = new IIOMetadataNode("Data");
    d.appendChild(node("PlanarConfiguration", "value", "PixelInterleaved"));
    d.appendChild(node("SampleFormat", "value",
        image.getColorModel() instanceof IndexColorModel ? "Index" : "UnsignedIntegral"));
    int[] bits = image.getColorModel().getComponentSize();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bits.length; i++) {
      sb.append(i == 0 ? "" : " ").append(bits[i]);
    }
    d.appendChild(node("BitsPerSample", "value", sb.toString()));
    return d;
  }

  protected IIOMetadataNode getStandardDimensionNode() {
    IIOMetadataNode d = new IIOMetadataNode("Dimension");
    d.appendChild(node("PixelAspectRatio", "value", "1.0"));
    d.appendChild(node("ImageOrientation", "value", "Normal"));
    return d;
  }

  protected IIOMetadataNode getStandardTransparencyNode() {
    IIOMetadataNode t = new IIOMetadataNode("Transparency");
    t.appendChild(node("Alpha", "value", image.getColorModel().hasAlpha() ? "nonpremultipled" : "none"));
    return t;
  }
}
